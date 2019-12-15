package dupd.hku.com.hkusap.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import java.util.Stack;

public class ActivityStack {
    private static final String TAG = "ActivityStack";
    private static Stack<Activity> mActivityStack;

    private ActivityStack() {
    }

    public static void init(Application app) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                ActivityStack.push(activity);
                Log.e(TAG, String.format("%s onCreated()", activity.getClass().getSimpleName()));
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

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
                ActivityStack.pop(activity);
                Log.e(TAG, String.format("%s onDestroyed()", activity.getClass().getSimpleName()));
            }
        });
    }

    private static void push(Activity activity) {
        mActivityStack.add(activity);
    }

    private static void pop(Activity activity) {
        mActivityStack.remove(activity);
    }

    public static void finishAllActivity() {
        for (Activity activity : mActivityStack) {
            Log.d(TAG, activity.getClass().getName());
            activity.finish();
        }
    }

    public static Activity finishOtherActivity(String className) {
        Activity result = null;
        for (Activity activity : mActivityStack) {
            if (activity.getClass().getName().equals(className)) {
                result = activity;
            } else {
                activity.finish();
            }
        }
        return result;
    }

    public static boolean finishActivity(String className) {
        if (mActivityStack.isEmpty()) return false;
        boolean result = false;
        for (Activity activity : mActivityStack) {
            if (activity.getClass().getName().equals(className)) {
                activity.finish();
                result = true;
            }
        }
        return result;
    }

    public static boolean hasActivity(String className) {
        if (mActivityStack.isEmpty()) return false;
        for (Activity activity : mActivityStack) {
            if (activity.getClass().getName().equals(className)) {
                return true;
            }
        }
        return false;
    }
}
