package dupd.hku.com.hkusap;

import android.text.TextUtils;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * @author: YougaKingWu@gmail.com
 * @created on: 2019/01/02 11:29
 * @description:
 */
public class BuglyUtil {



    public static void postCatchedException(Throwable throwable) {
        if (throwable == null) return;
        CrashReport.postCatchedException(throwable);
    }

    public static void postCatchedException(String message) {
        if (TextUtils.isEmpty(message)) return;
        RuntimeException exception = new RuntimeException(message);
        exception.printStackTrace();
        CrashReport.postCatchedException(exception);
    }


}
