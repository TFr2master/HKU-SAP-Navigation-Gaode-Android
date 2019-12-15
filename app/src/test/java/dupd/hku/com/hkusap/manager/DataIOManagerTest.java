package dupd.hku.com.hkusap.manager;

import com.google.gson.Gson;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.model.Sapdb;

public class DataIOManagerTest {

    @Test
    public void getInstance() {

        String json = readAssetJson("D:\\StudioProjects\\HKAPP\\app\\src\\main\\assets\\sapdb.json");
        Sapdb sapdb = new Gson().fromJson(json, Sapdb.class);

        for (SPPlateModel plate : sapdb.plate) {
            System.out.println(plate.UUID);
        }
    }


    public static String readAssetJson(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            File file = new File(fileName);
            inputStream = new FileInputStream(file);
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
}