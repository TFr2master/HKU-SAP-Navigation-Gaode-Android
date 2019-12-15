package dupd.hku.com.hkusap.persistence.interceptor;


import java.io.IOException;

import dupd.hku.com.hkusap.BuildConfig;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderInterceptor implements Interceptor {


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request requestOrigin = chain.request();
        Headers headersOrigin = requestOrigin.headers();

        Headers.Builder builder = headersOrigin.newBuilder()
                .set("User-Agent", "Custom UA")
                .add("version", BuildConfig.VERSION_NAME)
                .add("time", String.valueOf((System.currentTimeMillis() / 1000)));
        Request request = requestOrigin.newBuilder()
                .headers(builder.build())
                .build();
        return chain.proceed(request);
    }
}