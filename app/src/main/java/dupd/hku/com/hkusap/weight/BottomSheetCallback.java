package dupd.hku.com.hkusap.weight;

/**
 * author: 13060393903@163.com
 * created on: 2018/10/16 9:32
 * description:
 */
public interface BottomSheetCallback {
    void onSlideAnimator(float offset, long duration);

    void onSlide(float offset);
}
