package dupd.hku.com.hkusap.persistence.converter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * author: 13060393903@163.com
 * created on: 2018/05/04 15:56
 * description:
 */
public class DailyyogaConverterFactory extends Converter.Factory {

    public static DailyyogaConverterFactory create() {
        return create(new Gson());
    }

    public static DailyyogaConverterFactory create(Gson gson) {
        return new DailyyogaConverterFactory(gson);
    }

    private final Gson mGson;

    public DailyyogaConverterFactory(Gson gson) {
        mGson = gson;
    }


    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        return new ResponseConverter<>(mGson, type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = mGson.getAdapter(TypeToken.get(type));
        return new RequestConverter<>(mGson, adapter);
    }

}
