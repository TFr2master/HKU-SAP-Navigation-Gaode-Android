package dupd.hku.com.hkusap.manager;

import android.content.res.Resources;
import android.graphics.Rect;

public interface ViewController {

    Resources getResources();

    void showLoadDialog(String message);

    void dismissDialogDelay(long delay);

    void moveCompass();

    Rect mapViewBounds();
}
