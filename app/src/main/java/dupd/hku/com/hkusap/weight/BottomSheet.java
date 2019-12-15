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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import dupd.hku.com.hkusap.EventAdapter;
import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.manager.DeadReckoningManager;
import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.manager.ViewController;
import dupd.hku.com.hkusap.model.IDPathResultModel;
import dupd.hku.com.hkusap.model.IEnum;
import dupd.hku.com.hkusap.model.SPBuildingModel;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.utils.AnimatorUtil;
import dupd.hku.com.hkusap.utils.ApiUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static dupd.hku.com.hkusap.manager.MapManagerMode.MapManagerModeRouteFinding;
import static dupd.hku.com.hkusap.weight.BottomSheet.State.STATE_COLLAPSED;
import static dupd.hku.com.hkusap.weight.BottomSheet.State.STATE_COLLAPSED_MINI;
import static dupd.hku.com.hkusap.weight.BottomSheet.State.STATE_EXPANDED;
import static dupd.hku.com.hkusap.weight.BottomSheet.State.STATE_HIDDEN;

/**
 * author: 13060393903@163.com
 * created on: 2018/09/30 17:16
 * description:
 */
public class BottomSheet extends LinearLayout implements IEnum {

    private final ProgressBar mProgressBar;
    private TextView mTvName;
    private TextView mTvBuilding;
    private Button mTvLevel;
    private FrameLayout mBtvEvents;
    private TextView mTvEvents;
    private FrameLayout mBtnDirective;
    private TextView mTvDirective;
    private FrameLayout mFlBottom;
    private RecyclerView mRvEvents;
    private TextView mTvEmpty;
    private float mLastY;
    private State mState = STATE_HIDDEN;
    private EventAdapter mEventAdapter;

    public BottomSheet(Context context) {
        this(context, null);
    }

    public BottomSheet(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomSheet(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.bottom_sheet, this, true);

        mTvName = findViewById(R.id.tv_name);
        mTvBuilding = findViewById(R.id.tv_building);
        mTvLevel = findViewById(R.id.tv_level);
        mBtvEvents = findViewById(R.id.btv_events);
        mTvEvents = findViewById(R.id.tv_events);
        mBtnDirective = findViewById(R.id.btn_directive);
        mTvDirective = findViewById(R.id.tv_directive);
        mFlBottom = findViewById(R.id.fl_bottom);
        mRvEvents = findViewById(R.id.rv_events);
        mTvEmpty = findViewById(R.id.tv_empty);
        mProgressBar = findViewById(R.id.progressBar);
        mRvEvents.setLayoutManager(new LinearLayoutManager(context));
        mEventAdapter = new EventAdapter();
        mRvEvents.setAdapter(mEventAdapter);
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
            ViewController viewController = AnimatorUtil.getInstance().mViewController;
            viewController.showLoadDialog("Searching Route...");

            if (!MapIOManager.getInstance().prepareForShortestPathCalculation()) {
                viewController.showLoadDialog("Route not found");
                viewController.dismissDialogDelay(3_000);

                MapIOManager.getInstance().removeStartingPoint();
                return;
            }

            Observable.timer(2, TimeUnit.SECONDS)
                    .flatMap((Function<Long, ObservableSource<IDPathResultModel>>) aLong -> {
                        final IDPathResultModel result = MapIOManager.getInstance().getShortestPathResult();
                        if (result == null || result.routes == null || result.routes.isEmpty()) {
                            return Observable.error(new RuntimeException("Route not found"));
                        } else {
                            return Observable.just(result);
                        }
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        viewController.dismissDialogDelay(0);

                        MapIOManager.getInstance().displayShortestPathResult(result);
                        DeadReckoningManager.getInstance().switchToMapMatchingMode(true, result.routes);
                        MapIOManager.getInstance().updateMapManagerMode(MapManagerModeRouteFinding, true, () -> MapIOManager.getInstance().boundsToShortestPath(null));
                    }, throwable -> {
                        throwable.printStackTrace();
                        viewController.showLoadDialog("Route not found");
                        viewController.dismissDialogDelay(3_000);

                        MapIOManager.getInstance().removeStartingPoint();
                    }).isDisposed();
        });

        setTranslationY(STATE_HIDDEN.getTranslationY());
    }

    public State getState() {
        return mState;
    }

    public void initialize(SPPlateModel plate) {
        mProgressBar.setVisibility(VISIBLE);
        mTvEmpty.setVisibility(GONE);
        ApiUtil.getInstance().querytimetable(plate, eventList -> {
            mProgressBar.setVisibility(GONE);
            mEventAdapter.refresh(eventList);
            mTvEmpty.setVisibility(mEventAdapter.getItemCount() == 0 ? VISIBLE : GONE);
        });

        /*SPBuildingModel building = DataIOManager.getInstance().BuildingNameToBuilding(plate.building_name);

        if (building != null) {
            mTvBuilding.setText(building.name);
        } else {
            mTvBuilding.setText("");
        }*/

        mTvBuilding.setText(plate.building_name);
        mTvName.setText(plate.name);
        mTvBuilding.setCompoundDrawablesRelativeWithIntrinsicBounds(imageForType(plate.type), 0, 0, 0);
        mTvLevel.setText(DataIOManager.getInstance().abbreviationForLevelCode(plate.levelCode));
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
        mTvEvents.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_down, 0, 0, 0);
        mTvEvents.setText("Hide");
    }

    public void collapse(Runnable onAnimationEnd) {
        translationYAnimator(STATE_COLLAPSED.getTranslationY(), translationYOffset(STATE_COLLAPSED), onAnimationEnd);
        mTvEvents.setCompoundDrawablesWithIntrinsicBounds(R.drawable.events, 0, 0, 0);
        mTvEvents.setText("Events");
    }

    public void collapseMini(Runnable onAnimationEnd) {
        translationYAnimator(STATE_COLLAPSED_MINI.getTranslationY(), translationYOffset(STATE_COLLAPSED_MINI), onAnimationEnd);
        mTvEvents.setCompoundDrawablesWithIntrinsicBounds(R.drawable.events, 0, 0, 0);
        mTvEvents.setText("Events");
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
        public float bottomHeight; //底部距离

        State(int type) {
            this.type = type;

            Resources resources = HKUApplication.sAPP.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float directiveHeight = resources.getDimension(R.dimen.dp_50);
            float levelHeight = resources.getDimension(R.dimen.dp_84);
            bottomHeight = (metrics.heightPixels - levelHeight - directiveHeight) / 2;

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
