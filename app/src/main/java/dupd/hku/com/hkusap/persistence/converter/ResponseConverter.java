package dupd.hku.com.hkusap.persistence.converter;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;

import dupd.hku.com.hkusap.persistence.DailyyogaException;
import okhttp3.ResponseBody;
import retrofit2.Converter;

public class ResponseConverter<T> implements Converter<ResponseBody, T> {
    private final Gson mGson;
    private final Type mType;

    ResponseConverter(Gson gson, Type type) {
        mGson = gson;
        mType = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        try {
            String response = value.string();
            if (mType == String.class) {
                return (T) response;
            } else {
                return mGson.fromJson(response, mType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DailyyogaException(1001, e.getMessage());
        }
    }
}
