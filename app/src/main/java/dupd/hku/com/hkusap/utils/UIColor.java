package dupd.hku.com.hkusap.utils;

import android.graphics.Color;

import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.model.IEnum.SPSelectedIndexType;

public class UIColor {

    public static int rgb(float red, float green, float blue) {
        return 0xff000000 |
                ((int) (red * 255.0f + 0.5f) << 16) |
                ((int) (green * 255.0f + 0.5f) << 8) |
                (int) (blue * 255.0f + 0.5f);
    }

    public static int systemLightBlueColor() {
        return rgb(102 / 255.0f, 157 / 255.0f, 247 / 255.0f);
    }

    public static int systemBlueColor() {
        return HKUApplication.sAPP.getResources().getColor(R.color.colorPrimary);
    }

    public static int systemDarkBlueColor() {
        return rgb(67 / 255.0f, 132 / 255.0f, 1.0f);
    }

    public static int systemBackgroundColor() {
        return rgb(238 / 255.0f, 238 / 255.0f, 243 / 255.0f);
    }

    public static int systemIconBackgroundColor() {
        return rgb(145 / 255.0f, 163 / 255.0f, 175 / 255.0f);
    }

    public static int lightGrayColor() {
        return HKUApplication.sAPP.getResources().getColor(R.color.black_33);
    }

    public static int flatOrange() {
        return HKUApplication.sAPP.getResources().getColor(R.color.flatOrange);
    }

    public static int blackColor() {
        return Color.BLACK;
    }

    public static int black40Color() {
        return HKUApplication.sAPP.getResources().getColor(R.color.black_40);
    }

    public static int whiteColor() {
        return Color.WHITE;
    }

    public static int clearColor() {
        return Color.TRANSPARENT;
    }


    public static int colorForIndicatorType(SPSelectedIndexType type) {
        switch (type) {
            case SPSelectedIndexTypeNone:
            case SPSelectedIndexTypeFrom:
                return UIColor.systemBlueColor();
            case SPSelectedIndexTypeTransition:
                return UIColor.lightGrayColor();
            case SPSelectedIndexTypeTo:
            default:
                return flatOrange();
        }
    }

    public static int pointForIndicatorType(SPSelectedIndexType type) {
        switch (type) {
            case SPSelectedIndexTypeNone:
            case SPSelectedIndexTypeFrom:
                return R.drawable.point_from;
            case SPSelectedIndexTypeTransition:
                return R.drawable.point_gray;
            case SPSelectedIndexTypeTo:
            default:
                return R.drawable.point_to;
        }
    }
}
