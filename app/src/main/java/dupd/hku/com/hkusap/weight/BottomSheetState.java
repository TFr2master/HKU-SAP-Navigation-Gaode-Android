package dupd.hku.com.hkusap.weight;

import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.model.IDPathResultModel;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.utils.AnimatorUtil;


public class BottomSheetState {

    public BottomSheet mBottomSheet;
    public BottomSheetRoute mBottomSheetRoute;
    public BottomSheetNavigation mBottomSheetNavigation;
    private static BottomSheetState INSTANCE;

    private BottomSheetState() {
    }

    public static BottomSheetState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BottomSheetState();
        }
        return INSTANCE;
    }

    public void registerBottomSheet(BottomSheet bottomSheet, BottomSheetRoute bottomSheetRoute, BottomSheetNavigation bottomSheetNavigation) {
        mBottomSheet = bottomSheet;
        mBottomSheetRoute = bottomSheetRoute;
        mBottomSheetNavigation = bottomSheetNavigation;
    }

    public void statePositioning(BottomSheet.State state, SPPlateModel plate, Runnable onAnimationEnd) {
        switch (state) {
            case STATE_HIDDEN:
                mBottomSheet.setState(BottomSheet.State.STATE_HIDDEN, onAnimationEnd);
                break;
            case STATE_COLLAPSED:
                mBottomSheet.initialize(plate);
                mBottomSheet.setState(BottomSheet.State.STATE_COLLAPSED, onAnimationEnd);
                break;
        }
    }

    public void stateRouteFinding(BottomSheetRoute.State state, SPPlateModel selectedPlate, IDPathResultModel pathResult, Runnable onAnimationEnd) {
        switch (state) {
            case STATE_HIDDEN:
                mBottomSheetRoute.setState(BottomSheetRoute.State.STATE_HIDDEN, onAnimationEnd);
                break;
            case STATE_COLLAPSED:
                mBottomSheetRoute.initialize(selectedPlate, pathResult);
                mBottomSheetRoute.setState(BottomSheetRoute.State.STATE_COLLAPSED, onAnimationEnd);
                break;
        }
    }

    public void stateNavigation(BottomSheetNavigation.State state, SPPlateModel selectedPlate, IDPathResultModel pathResult, Runnable onAnimationEnd) {
        switch (state) {
            case STATE_HIDDEN:
                mBottomSheetNavigation.setState(BottomSheetNavigation.State.STATE_HIDDEN, onAnimationEnd);
                break;
            case STATE_EXPANDED:
                mBottomSheetNavigation.initialize(selectedPlate, pathResult);
                mBottomSheetNavigation.setState(BottomSheetNavigation.State.STATE_EXPANDED, onAnimationEnd);
                break;
        }
    }

    public float getBottomSheetCollapseHeight() {
        switch (MapIOManager.getInstance().mode) {
            default:
            case MapManagerModePositioning:
                return BottomSheet.State.STATE_COLLAPSED.height;
            case MapManagerModeRouteFinding:
                return BottomSheetRoute.State.STATE_COLLAPSED.height;
            case MapManagerModeNavigation:
                return BottomSheetNavigation.State.STATE_COLLAPSED.height;
        }
    }

    public void onMapClick() {
        switch (MapIOManager.getInstance().mode) {
            case MapManagerModePositioning:
                if (mBottomSheet.getState() == BottomSheet.State.STATE_HIDDEN) return;
                mBottomSheet.setState(BottomSheet.State.STATE_COLLAPSED_MINI, null);
                break;
            case MapManagerModeRouteFinding:
                if (mBottomSheetRoute.mState == BottomSheetRoute.State.STATE_HIDDEN) return;
                mBottomSheetRoute.setState(BottomSheetRoute.State.STATE_COLLAPSED_MINI, null);
                break;
        }
    }

    public static void showToolbar(boolean show) {
        switch (MapIOManager.getInstance().mode) {
            case MapManagerModePositioning:
                AnimatorUtil.getInstance().showOrHidePositioning(show).start();
                break;
            case MapManagerModeRouteFinding:
                AnimatorUtil.getInstance().showOrHideRouteFinding(show).start();
                break;
        }
    }
}
