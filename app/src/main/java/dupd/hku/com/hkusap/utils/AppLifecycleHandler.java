package dupd.hku.com.hkusap.utils;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;

/**
 * @author: YougaKingWu@gmail.com
 * @created on: 2019/01/02 12:28
 * @description:
 */
public class AppLifecycleHandler implements ActivityLifecycleCallbacks, ComponentCallbacks2 {


    private boolean appInForeground = false;
    private LifeCycleDelegate mLifeCycleDelegate;
    private static volatile AppLifecycleHandler INSTANCE;

    public static AppLifecycleHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (AppLifecycleHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppLifecycleHandler();
                }
            }
        }
        return INSTANCE;
    }

    public void registerLifecycleHandler(Application application) {
        if (application == null) {
            return;
        }
        application.registerActivityLifecycleCallbacks(this);
        application.registerComponentCallbacks(this);
    }

    public void registerLifeCycleDelegate(LifeCycleDelegate lifeCycleDelegate) {
        mLifeCycleDelegate = lifeCycleDelegate;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (!appInForeground) {
            appInForeground = true;
            if (mLifeCycleDelegate != null) {
                mLifeCycleDelegate.onAppForegrounded();
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public void onTrimMemory(int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            appInForeground = false;
            if (mLifeCycleDelegate != null) {
                mLifeCycleDelegate.onAppBackgrounded();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public void onLowMemory() {

    }

    public static boolean isAppInBackgrounded() {
        return !getInstance().appInForeground;
    }

    public interface LifeCycleDelegate {
        void onAppBackgrounded();

        void onAppForegrounded();
    }
}
