package dupd.hku.com.hkusap.weight;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.RouteDetailAdapter;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.manager.SPTeleprompter;
import dupd.hku.com.hkusap.model.IDPathResultModel;
import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.model.IEnum;
import dupd.hku.com.hkusap.model.SPBuildingModel;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.utils.GeoUtils;

import static dupd.hku.com.hkusap.manager.MapManagerMode.MapManagerModeNavigation;
import static dupd.hku.com.hkusap.weight.BottomSheetRoute.State.STATE_COLLAPSED;
import static dupd.hku.com.hkusap.weight.BottomSheetRoute.State.STATE_COLLAPSED_MINI;
import static dupd.hku.com.hkusap.weight.BottomSheetRoute.State.STATE_EXPANDED;
import static dupd.hku.com.hkusap.weight.BottomSheetRoute.State.STATE_HIDDEN;

/**
 * author: 13060393903@163.com
 * created on: 2018/09/30 17:16
 * description:
 */
public class BottomSheetRoute extends LinearLayout implements IEnum {

    private TextView mTvName;
    private TextView mTvBuilding;
    private TextView mTvTime;
    private Button mTvLevel;
    private FrameLayout mBtvEvents;
    private TextView mTvEvents;
    private FrameLayout mBtnDirective;
    private TextView mTvDirective;
    private FrameLayout mFlBottom;
    private RecyclerView mRvEvents;
    private TextView mTvEmpty;
    private float mLastY;
    public State mState = State.STATE_HIDDEN;
    private IDPathResultModel result;

    public BottomSheetRoute(Context context) {
        this(context, null);
    }

    public BottomSheetRoute(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomSheetRoute(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.bottom_sheet_route, this, true);

        mTvName = findViewById(R.id.tv_name);
        mTvBuilding = findViewById(R.id.tv_building);
        mTvTime = findViewById(R.id.tv_time);
        mTvLevel = findViewById(R.id.tv_level);
        mBtvEvents = findViewById(R.id.btv_events);
        mTvEvents = findViewById(R.id.tv_events);
        mBtnDirective = findViewById(R.id.btn_directive);
        mTvDirective = findViewById(R.id.tv_directive);
        mFlBottom = findViewById(R.id.fl_bottom);
        mRvEvents = findViewById(R.id.rv_events);
        mTvEmpty = findViewById(R.id.tv_empty);
        mRvEvents.setLayoutManager(new LinearLayoutManager(context));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, (int) STATE_HIDDEN.bottomHeight);
        mFlBottom.setLayoutParams(params);

        mBtvEvents.setOnClickListener(v -> {
            switch (mState) {
                case STATE_EXPANDED:
                    setState(STATE_COLLAPSED, null);
                    BottomSheetState.showToolbar(true);
                    break;
                case STATE_COLLAPSED:
                    setState(STATE_EXPANDED, null);
                    BottomSheetState.showToolbar(false);
                    break;
            }
        });
        mBtnDirective.setOnClickListener(v -> {
            MapIOManager.getInstance().updateMapManagerMode(MapManagerModeNavigation, false, null);
        });

        setTranslationY(STATE_HIDDEN.getTranslationY());
    }

    public void setState(State state, Runnable onAnimationEnd) {
        switch (state) {
            case STATE_EXPANDED:
                expanded(onAnimationEnd);
                break;
            case STATE_COLLAPSED:
                collapse(onAnimationEnd);
                break;
            case STATE_COLLAPSED_MINI:
                collapseMini(onAnimationEnd);
                break;
            case STATE_HIDDEN:
                hide(onAnimationEnd);
                break;
        }
        mState = state;
    }

    public void initialize(SPPlateModel plate, IDPathResultModel pathResult) {
        result = pathResult;
        /*
        SPBuildingModel building = DataIOManager.getInstance().postalCodeToBuilding(plate.postalCode);
        if (building != null) {
            mTvBuilding.setText(building.name);
        } else {
            mTvBuilding.setText("");
        }*/
        mTvBuilding.setText(plate.building_name);
        mTvName.setText(plate.name);
        mTvBuilding.setCompoundDrawablesRelativeWithIntrinsicBounds(imageForType(plate.type), 0, 0, 0);
        mTvLevel.setText(DataIOManager.getInstance().abbreviationForLevelCode(plate.levelCode));

        SPTeleprompter teleprompter = new SPTeleprompter(pathResult.routes);

        double distance = calculateDistanceInMeters();
        String minutes = teleprompter.timeIntervalForDistance(distance);
        String meters = teleprompter.distanceDescription(distance);

        mTvTime.setText(String.format("%s(%s)", minutes, meters));

        RouteDetailAdapter adapter = new RouteDetailAdapter(teleprompter);
        mRvEvents.setAdapter(adapter);
        mTvEmpty.setVisibility(GONE);
    }


    public double calculateDistanceInMeters() {
        double distance = 0.0;
        for (IDRouteModel route : result.routes) {
            distance += GeoUtils.distance(route.start.coordinate, route.end.coordinate);
        }

        return distance;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                mLastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float offsetY = event.getY() - mLastY;//手指移动距离
                float targetY = getTranslationY() + offsetY;//偏移目标值
                //偏移目标值应在最小状态并与展开的状态之间
                if (targetY > STATE_COLLAPSED_MINI.getTranslationY() || targetY < STATE_EXPANDED.getTranslationY())
                    return true;
                mCallback.onSlide(offsetY);
                setTranslationY(targetY);
                break;
            case MotionEvent.ACTION_UP://松开回弹
                float translationY = getTranslationY();
                if (translationY >= STATE_COLLAPSED.thresholdTranslationY) {
                    setState(STATE_COLLAPSED_MINI, null);
                    BottomSheetState.showToolbar(true);
                    return true;
                } else if (translationY >= STATE_EXPANDED.thresholdTranslationY && translationY < STATE_COLLAPSED.thresholdTranslationY) {
                    setState(STATE_COLLAPSED, null);
                    BottomSheetState.showToolbar(true);
                    return true;
                } else if (translationY >= 0 && translationY < STATE_EXPANDED.thresholdTranslationY) {
                    setState(STATE_EXPANDED, null);
                    BottomSheetState.showToolbar(false);
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void expanded(Runnable onAnimationEnd) {
        translationYAnimator(STATE_EXPANDED.getTranslationY(), translationYOffset(STATE_EXPANDED), onAnimationEnd);
        mTvEvents.setCompoundDrawablesWithIntrinsicBounds(R.drawable.map, 0, 0, 0);
        mTvEvents.setText("Map");
    }

    public void collapse(Runnable onAnimationEnd) {
        translationYAnimator(STATE_COLLAPSED.getTranslationY(), translationYOffset(STATE_COLLAPSED), onAnimationEnd);
        mTvEvents.setCompoundDrawablesWithIntrinsicBounds(R.drawable.steps, 0, 0, 0);
        mTvEvents.setText("Steps");
    }

    public void collapseMini(Runnable onAnimationEnd) {
        translationYAnimator(STATE_COLLAPSED_MINI.getTranslationY(), translationYOffset(STATE_COLLAPSED_MINI), onAnimationEnd);
        mTvEvents.setCompoundDrawablesWithIntrinsicBounds(R.drawable.steps, 0, 0, 0);
        mTvEvents.setText("Steps");
    }

    public void hide(Runnable onAnimationEnd) {
        translationYAnimator(STATE_HIDDEN.getTranslationY(), translationYOffset(STATE_HIDDEN), onAnimationEnd);
    }

    /**
     * @param target 目标状态
     * @return 平移到目标状态位置与当前位置的偏移量
     */
    public float translationYOffset(State target) {
        return target.getTranslationY() - getTranslationY();
    }

    private void translationYAnimator(float translationY, float offset, Runnable onAnimationEnd) {
        if (offset == 0) return;
        long duration = 300;

        mCallback.onSlideAnimator(offset, duration);

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "translationY", translationY)
                .setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEnd != null) onAnimationEnd.run();
            }
        });
        animator.start();
    }

    private BottomSheetCallback mCallback;

    public void addBottomSheetCallback(BottomSheetCallback callback) {
        mCallback = callback;
    }

    public enum State {
        STATE_EXPANDED(0),//完全展开
        STATE_COLLAPSED(1),//折叠
        STATE_COLLAPSED_MINI(2),//折叠到最小位置
        STATE_HIDDEN(3);//隐藏

        public int type;
        public float height;//当前高度
        public float totalHeight;//总高度 == 完全展开的高度
        public float thresholdTranslationY; //平移回弹阈值
        public float bottomHeight;

        State(int type) {
            this.type = type;
            Resources resources = HKUApplication.sAPP.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float directiveHeight = resources.getDimension(R.dimen.dp_50);
            float levelHeight = resources.getDimension(R.dimen.dp_104);
            bottomHeight = (metrics.heightPixels - levelHeight - directiveHeight - resources.getDimension(R.dimen.dp_60));

            totalHeight = levelHeight + directiveHeight + bottomHeight;

            switch (type) {
                case 0:
                    height = totalHeight;
                    thresholdTranslationY = bottomHeight / 2;
                    break;
                case 1:
                    height = levelHeight + directiveHeight;
                    thresholdTranslationY = bottomHeight + directiveHeight / 2;
                    break;
                case 2:
                    height = levelHeight;
                    break;
                case 3:
                    height = 0;
                    break;
            }
        }

        /**
         * @return 平移到当前状态所需的平移量
         */
        public float getTranslationY() {
            return totalHeight - height;
        }
    }
}
