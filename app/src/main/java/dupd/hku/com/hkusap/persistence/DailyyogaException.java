package dupd.hku.com.hkusap.persistence;

import java.io.IOException;

/**
 * author: 13060393903@163.com
 * created on: 2018/05/04 16:12
 * description:
 */
public class DailyyogaException extends IOException {
   public int code;
   public String error;

    public DailyyogaException(int code, String error) {
        super(error);
        this.code = code;
        this.error = error;
    }
}
