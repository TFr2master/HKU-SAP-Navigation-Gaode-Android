package dupd.hku.com.hkusap.utils;

import android.content.res.AssetManager;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.model.BeaconDebugForm;
import dupd.hku.com.hkusap.model.Sapdb;

public class AssetsUtil {

    public static String readAssetJson(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            //获取assets资源管理器
            AssetManager assetManager = HKUApplication.sAPP.getAssets();
            inputStream = assetManager.open(fileName);
            //通过管理器打开文件并读取
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

    public static InputStream readAssetInputStream(String fileName) {
        try {
            AssetManager assetManager = HKUApplication.sAPP.getAssets();
            return assetManager.open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeSapdb() {
        File dir = HKUApplication.sAPP.getFilesDir();
        if (dir == null) return;
        File file = new File(dir, "sapdb.json");
        InputStream stream = readAssetInputStream("sapdb.json");
        if (stream == null) return;
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            InputStreamReader streamReader = new InputStreamReader(stream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outputStream.write(line.getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeSapdb(Sapdb sapdb) {
        String json = new Gson().toJson(sapdb);
        File dir = HKUApplication.sAPP.getFilesDir();
        if (dir == null) return;
        File file = new File(dir, "sapdb.json");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(json.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static File writeDebugFile() {
        BeaconDebugForm form = new BeaconDebugForm();
        form.mBeaconDebugList = DataIOManager.getInstance().mBeaconDebugList;
        String json = new Gson().toJson(form);
        File dir = HKUApplication.sAPP.getExternalCacheDir();
        if (dir == null) return null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        File file = new File(dir, format.format(new Date()) + ".json");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(json.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return file;
        }
    }
}
