package dupd.hku.com.hkusap.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import java.util.List;

import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.manager.MapManagerMode;
import dupd.hku.com.hkusap.manager.ViewController;
import dupd.hku.com.hkusap.model.IDMatchingModel;
import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.weight.NavigationToolbar;
import dupd.hku.com.hkusap.weight.PositioningToolbar;
import dupd.hku.com.hkusap.weight.RouteFindingToolbar;

import static dupd.hku.com.hkusap.manager.MapManagerMode.MapManagerModeNavigation;
import static dupd.hku.com.hkusap.manager.MapManagerMode.MapManagerModePositioning;
import static dupd.hku.com.hkusap.manager.MapManagerMode.MapManagerModeRouteFinding;

public class AnimatorUtil {


    private int mRouteFindingTranslationY = 500;
    private int mPositioningTranslationY = 300;
    private int mNavigationTranslationY = 800;
    private static AnimatorUtil INSTANCE;
    private PositioningToolbar mPositioningToolbar;
    private RouteFindingToolbar mRouteFindingToolbar;
    private NavigationToolbar mNavigationToolbar;
    public ViewController mViewController;

    private AnimatorUtil() {
    }

    public static AnimatorUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AnimatorUtil();
        }
        return INSTANCE;
    }

    public void registerToolbar(PositioningToolbar positioningToolbar, RouteFindingToolbar routeFindingToolbar, NavigationToolbar navigationToolbar,
                                ViewController viewController) {
        mPositioningToolbar = positioningToolbar;
        mRouteFindingToolbar = routeFindingToolbar;
        mNavigationToolbar = navigationToolbar;
        mViewController = viewController;
        mRouteFindingToolbar.setTranslationY(-mRouteFindingTranslationY);
        mNavigationToolbar.setTranslationY(-mNavigationTranslationY);
    }

    public ObjectAnimator showOrHidePositioning(boolean show) {
        float translationY = show ? 0 : -mPositioningTranslationY;
        return ObjectAnimator.ofFloat(mPositioningToolbar, "translationY", translationY)
                .setDuration(500);
    }

    public ObjectAnimator showOrHideRouteFinding(boolean show) {
        float translationY = show ? 0 : -mRouteFindingTranslationY;
        return ObjectAnimator.ofFloat(mRouteFindingToolbar, "translationY", translationY)
                .setDuration(500);
    }

    public ObjectAnimator showOrHideNavigation(boolean show) {
        float translationY = show ? 0 : -mNavigationTranslationY;
        return ObjectAnimator.ofFloat(mNavigationToolbar, "translationY", translationY)
                .setDuration(500);
    }

    public AnimatorSet hidePositioningShowRouteFinding(SPPlateModel plate) {
        ObjectAnimator hidePositioning = showOrHidePositioning(false);
        ObjectAnimator showRouteFinding = showOrHideRouteFinding(true);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(hidePositioning, showRouteFinding);
        animatorSet.start();

        mRouteFindingToolbar.initialize(plate);
        mViewController.moveCompass();
        return animatorSet;
    }

    public AnimatorSet hideRouteFindingShowPositioning() {
        ObjectAnimator hideRouteFinding = showOrHideRouteFinding(false);
        ObjectAnimator showPositioning = showOrHidePositioning(true);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(hideRouteFinding, showPositioning);
        animatorSet.start();

        mViewController.moveCompass();
        return animatorSet;
    }

    public AnimatorSet hideRouteFindingShowNavigation(List<IDRouteModel> routes, IDMatchingModel position, float updatedHeading) {
        ObjectAnimator hideRouteFinding = showOrHideRouteFinding(false);
        ObjectAnimator showPositioning = showOrHideNavigation(true);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(hideRouteFinding, showPositioning);
        animatorSet.start();

        mNavigationToolbar.initialize(routes, position, updatedHeading);
        mViewController.moveCompass();
        return animatorSet;
    }

    public AnimatorSet hideNavigationShowRouteFinding() {
        ObjectAnimator hideRouteFinding = showOrHideNavigation(false);
        ObjectAnimator showPositioning = showOrHideRouteFinding(true);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(hideRouteFinding, showPositioning);
        animatorSet.start();

        mViewController.moveCompass();
        return animatorSet;
    }

    public View getToolbar() {
        switch (MapIOManager.getInstance().mode) {
            default:
            case MapManagerModePositioning:
                return mPositioningToolbar;
            case MapManagerModeRouteFinding:
                return mRouteFindingToolbar;
            case MapManagerModeNavigation:
                return mNavigationToolbar;
        }
    }
}
