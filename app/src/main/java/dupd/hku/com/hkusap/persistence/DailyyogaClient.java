package dupd.hku.com.hkusap.persistence;


import java.util.concurrent.TimeUnit;

import dupd.hku.com.hkusap.persistence.converter.DailyyogaConverterFactory;
import dupd.hku.com.hkusap.persistence.interceptor.HeaderInterceptor;
import dupd.hku.com.hkusap.persistence.interceptor.LogInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class DailyyogaClient {


    private static Retrofit mRetrofit;

    public static Retrofit retrofit() {
        if (mRetrofit == null) {
            synchronized (DailyyogaClient.class) {
                if (mRetrofit == null) {
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .addInterceptor(new HeaderInterceptor())
                            .addInterceptor(new LogInterceptor())
                            // 超时设置
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            // 错误重连
                            .retryOnConnectionFailure(true)
                            .build();

                    mRetrofit = new Retrofit.Builder()
                            .baseUrl("https://hkusap.hku.hk/")
                            .addConverterFactory(DailyyogaConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .client(okHttpClient)
                            .build();
                }
            }
        }
        return mRetrofit;
    }
}
