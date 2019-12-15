package dupd.hku.com.hkusap.utils;

import android.os.Process;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.TimeUnit;

import dupd.hku.com.hkusap.BuglyUtil;
import io.reactivex.Observable;

import static dupd.hku.com.hkusap.utils.AppLifecycleHandler.isAppInBackgrounded;

/**
 * @author: YougaKingWu@gmail.com
 * @created on: 2018/12/30 9:50
 * @description:
 */
public class YogaUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;
    private final Object synchronizedObject = new Object();

    private static volatile YogaUncaughtExceptionHandler INSTANCE;

    public static YogaUncaughtExceptionHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (YogaUncaughtExceptionHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new YogaUncaughtExceptionHandler();
                }
            }
        }
        return INSTANCE;
    }

    public synchronized void setDefaultUncaughtExceptionHandler() {
        UncaughtExceptionHandler uncaughtExceptionHandler;
        if ((uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()) != null) {
            if (getClass().getName().equals(uncaughtExceptionHandler.getClass().getName())) {
                return;
            }
            mDefaultUncaughtExceptionHandler = uncaughtExceptionHandler;
        }
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    private void uncaughtDefaultException(Thread thread, Throwable throwable) {
        if (isAppInBackgrounded()) {
            BuglyUtil.postCatchedException(throwable);
            Observable.timer(3, TimeUnit.SECONDS)
                    .subscribe(aLong -> {
                        Process.killProcess(Process.myPid());
                        System.exit(1);
                    }).isDisposed();
        } else {
            if (mDefaultUncaughtExceptionHandler != null) {
                mDefaultUncaughtExceptionHandler.uncaughtException(thread, throwable);
            }
        }
    }

    @Override
    public final void uncaughtException(Thread thread, Throwable throwable) {
        synchronized (synchronizedObject) {
            uncaughtDefaultException(thread, throwable);
        }
    }
}