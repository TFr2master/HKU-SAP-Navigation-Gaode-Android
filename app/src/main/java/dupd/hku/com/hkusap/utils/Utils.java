package dupd.hku.com.hkusap.utils;

import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

import java.lang.reflect.Field;

import dupd.hku.com.hkusap.HKUApplication;

public class Utils {

    public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int sbar = 38;//默认为38，貌似大部分是这样的
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            sbar = HKUApplication.sAPP.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

    public static int getResourcesDrawable(String name) {
        if (TextUtils.isEmpty(name)) {
            return 0;
        }
        ApplicationInfo appInfo = HKUApplication.sAPP.getApplicationInfo();
        return HKUApplication.sAPP.getResources().getIdentifier(name, "drawable", appInfo.packageName);
    }
}
