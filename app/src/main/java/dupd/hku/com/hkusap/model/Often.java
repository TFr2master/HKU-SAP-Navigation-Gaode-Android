package dupd.hku.com.hkusap.model;

import java.text.DecimalFormat;

public class Often {

    public int index;
    public double value;
    public String message;
    private DecimalFormat format = new DecimalFormat("0.00");

    public Often(int index, double value) {
        this.index = index;
        this.value = value;
    }

    public Often(int index, String message) {
        this.index = index;
        this.message = message;
    }

    public String getMessage() {
        return format.format(value);
    }
}
