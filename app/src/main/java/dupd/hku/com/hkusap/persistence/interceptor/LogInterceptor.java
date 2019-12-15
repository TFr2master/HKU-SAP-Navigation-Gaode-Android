package dupd.hku.com.hkusap.persistence.interceptor;

import com.orhanobut.logger.Logger;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class LogInterceptor implements Interceptor {

    private static final String F_BREAK = " %n";
    private static final String F_URL = " %s";
    private static final String F_TIME = " in %.1fms";
    private static final String F_HEADERS = "%s";
    private static final String F_RESPONSE = F_BREAK + "Response: %d";
    private static final String F_BODY = "body: %s";

    private static final String F_BREAKER = F_BREAK + "-------------------------------------------" + F_BREAK;
    private static final String F_REQUEST_WITHOUT_BODY = F_URL + F_TIME + F_BREAK + F_HEADERS;
    private static final String F_RESPONSE_WITHOUT_BODY = F_RESPONSE + F_BREAK + F_HEADERS + F_BREAKER;
    private static final String F_REQUEST_WITH_BODY = F_URL + F_TIME + F_BREAK + F_HEADERS + F_BODY + F_BREAK;
    //    private static final String F_RESPONSE_WITH_BODY = F_RESPONSE + F_BREAK + F_HEADERS + F_BODY + F_BREAK + F_BREAKER;

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        long t1 = System.nanoTime();
        Response response = chain.proceed(request);
        long t2 = System.nanoTime();

        double time = (t2 - t1) / 1e6d;

        switch (request.method()) {
            case "GET":
                Logger.i("GET " + F_REQUEST_WITHOUT_BODY + F_RESPONSE_WITHOUT_BODY, request.url(), time, request.headers(), response.code(), response.headers());
                break;
            case "POST":
                Logger.i("POST " + F_REQUEST_WITH_BODY + F_RESPONSE_WITHOUT_BODY, request.url(), time, request.headers(), stringifyRequestBody(request), response.code(), response.headers());
                break;
            case "PUT":
                Logger.i("PUT " + F_REQUEST_WITH_BODY + F_RESPONSE_WITHOUT_BODY, request.url(), time, request.headers(), request.body().toString(), response.code(), response.headers());
                break;
            case "DELETE":
                Logger.i("DELETE " + F_REQUEST_WITHOUT_BODY + F_RESPONSE_WITHOUT_BODY, request.url(), time, request.headers(), response.code(), response.headers());
                break;
        }

        if (response.body() != null) {
            MediaType contentType = response.body().contentType();
            if (contentType != null &&
                    ("video".equals(contentType.type())
                            || "image".equals(contentType.type())
                            || "audio".equals(contentType.type()))) {
                Logger.d(contentType);
                return response;
            }
            String bodyString = response.body().string();
            interceptorResponse(response.newBuilder().body(ResponseBody.create(contentType, bodyString)).build());
            return response.newBuilder().body(ResponseBody.create(contentType, bodyString)).build();
        } else {
            return response;
        }
    }


    private static String stringifyRequestBody(Request request) {
        try {
            if (request.body() != null && request.body().contentType() != null
                    && "multipart/form-data".equals(request.body().contentType().type() + "/" + request.body().contentType().subtype())) {
                return request.body().contentType().toString();
            }
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    private void interceptorResponse(Response response) {
        try {
            String json = response.body().string();
            if (!json.trim().startsWith("{") && !json.trim().startsWith("[")) {
                Logger.e(json);
            } else {
//                Logger.json(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
