package dupd.hku.com.hkusap.persistence.converter;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import dupd.hku.com.hkusap.persistence.DailyyogaException;
import io.reactivex.functions.Consumer;

/**
 * author: 13060393903@163.com
 * created on: 2018/05/31 15:42
 * description:
 */
public class ThrowableConverters {

    public static Consumer<Throwable> transformThrowable(Consumer<Throwable> onError) {
        return new ThrowableConsumer(onError);
    }

    private static class ThrowableConsumer implements Consumer<Throwable> {
        private Consumer<Throwable> mOnError;

        ThrowableConsumer(Consumer<Throwable> onError) {
            mOnError = onError;
        }

        @Override
        public void accept(Throwable throwable) throws Exception {
            throwable.printStackTrace();
            if (throwable instanceof SocketTimeoutException) {
                mOnError.accept(new Throwable("网络连接超时"));
            } else if (throwable instanceof UnknownHostException) {
                mOnError.accept(new Throwable("网络错误"));
            } else if (throwable instanceof SocketException) {
                mOnError.accept(new Throwable("网络错误"));
            } else if (throwable instanceof DailyyogaException) {
                DailyyogaException e = (DailyyogaException) throwable;
                mOnError.accept(new Throwable(e.error));
            } else {
                mOnError.accept(new Throwable("网络错误"));
            }
        }
    }
}
