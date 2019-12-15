package dupd.hku.com.hkusap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dupd.hku.com.hkusap.HKUApplication;

/**
 * author: 13060393903@163.com
 */

public class SpUtil {

    private static SharedPreferences getSharedPreferences() {
        return HKUApplication.sAPP.getSharedPreferences("hkusap", Context.MODE_PRIVATE);
    }

    public static void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    public static String getStringPreferences(String key) {
        return getSharedPreferences().getString(key, null);
    }

    public static boolean getBooleanPreferences(String key) {
        return getSharedPreferences().getBoolean(key, false);
    }

    public static int getIntPreferences(String key) {
        return getSharedPreferences().getInt(key, 0);
    }

    public static long getLongPreferences(String key) {
        return getSharedPreferences().getLong(key, 0L);
    }

    public static void putStringPreferences(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void putBooleanPreferences(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void putIntPreferences(String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void putLongPreferences(String key, long value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putLong(key, value);
        editor.apply();
    }


    public static <T> void putPreferences(T entity) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        try {
            ByteArrayOutputStream toByte = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(toByte);
            oos.writeObject(entity);
            String obj64 = new String(Base64.encode(toByte.toByteArray(), Base64.DEFAULT));
            editor.putString(entity.getClass().getSimpleName(), obj64);
            editor.apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getPreferences(Class clazz) {
        SharedPreferences sp = getSharedPreferences();
        try {
            String obj64 = sp.getString(clazz.newInstance().getClass().getSimpleName(), "");
            if (TextUtils.isEmpty(obj64)) {
                return null;
            }
            byte[] base64Bytes = Base64.decode(obj64, Base64.DEFAULT);
            ByteArrayInputStream stream = new ByteArrayInputStream(base64Bytes);
            ObjectInputStream ois = new ObjectInputStream(stream);
            return (T) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void remove(String... keys) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.apply();
    }

    public static void clear(String... keys) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
//        for (String key : keys) {
//            editor.remove(key);
//        }
        editor.clear();
        editor.apply();
    }
}
