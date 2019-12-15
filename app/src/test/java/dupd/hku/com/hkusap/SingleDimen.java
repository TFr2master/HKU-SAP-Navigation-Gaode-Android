package dupd.hku.com.hkusap;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class SingleDimen {

    static final String FILE_DIR = "D:/Android/Dimen";
    static final String[] SUPPORT_PIXELS = new String[]{
            "1x1920",//xxhdpi
    };

    public void generate() {
        File dir = new File(FILE_DIR);
        if (!dir.exists()) dir.mkdirs();

        for (String pixel : SUPPORT_PIXELS) {
            String[] array = pixel.split("x");
            float ratio = Float.valueOf(array[0]);
            int dimens = Integer.valueOf(array[1]);

            File fileDir = new File(dir.getPath() + File.separator + "values-" + ratio + "x" + dimens);
            if (!fileDir.exists()) fileDir.mkdirs();
            File dFile = new File(fileDir, "dimens.xml");

            writeXml(dFile, changeStrings(dimens, ratio, "dp_"));
        }
    }

    private void writeXml(File file, String strings) {
        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(file));
            writer.print(strings);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String changeStrings(int dimens, float ratio, String prefix) {
        //320 / x = 720 / 320;
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        builder.append("<resources>\n");
        for (int i = 1; i <= dimens; i++) {
            builder.append("<dimen name=\"" + prefix + i + "\">" + change(i * ratio) + "dp</dimen>\n");
        }
        builder.append("</resources>");
        return builder.toString();
    }

    public static float change(float a) {
        int temp = (int) (a * 100);
        return temp / 100f;
    }

    @Test
    public void Test() {
        generate();
    }
}
