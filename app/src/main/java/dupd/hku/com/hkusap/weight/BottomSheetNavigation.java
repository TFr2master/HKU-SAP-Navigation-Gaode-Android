package dupd.hku.com.hkusap.weight;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.manager.SPTeleprompter;
import dupd.hku.com.hkusap.model.IDPathResultModel;
import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.model.SPBuildingModel;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.utils.GeoUtils;
import dupd.hku.com.hkusap.window.EventFragment;

import static dupd.hku.com.hkusap.manager.MapManagerMode.MapManagerModeRouteFinding;
import static dupd.hku.com.hkusap.weight.BottomSheetNavigation.State.STATE_COLLAPSED;
import static dupd.hku.com.hkusap.weight.BottomSheetNavigation.State.STATE_EXPANDED;
import static dupd.hku.com.hkusap.weight.BottomSheetNavigation.State.STATE_HIDDEN;

/**
 * author: 13060393903@163.com
 * created on: 2018/09/30 17:16
 * description:
 */
public class BottomSheetNavigation extends LinearLayout implements MapIOManager.MapIOManagerObserverableProtocol {


    private final FragmentActivity mActivity;
    private TextView mTvName;
    private TextView mTvTime;
    private ImageButton mIbEvent;
    private Button mBtvExit;
    private float mLastY;
    public State mState = State.STATE_HIDDEN;
    private SPPlateModel mPlate;
    private IDPathResultModel result;
    private static AlertDialog.Builder normalDialog;

    public BottomSheetNavigation(Context context) {
        this(context, null);
    }

    public BottomSheetNavigation(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomSheetNavigation(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mActivity = (FragmentActivity) context;
        LayoutInflater.from(context).inflate(R.layout.bottom_sheet_navigation, this, true);

        mTvName = findViewById(R.id.tv_name);
        mTvTime = findViewById(R.id.tv_time);
        mIbEvent = findViewById(R.id.ib_event);
        mBtvExit = findViewById(R.id.btn_exit);

        mIbEvent.setOnClickListener(v -> {
            EventFragment.newInstance(mPlate).show(mActivity.getSupportFragmentManager(), EventFragment.TAG);
        });
        mBtvExit.setOnClickListener(v -> {
            MapIOManager.getInstance().updateMapManagerMode(MapManagerModeRouteFinding, false, null);
        });

        normalDialog =new AlertDialog.Builder(mActivity);
        normalDialog.setMessage("Would you like to exit the navigation mode?");
        normalDialog.setPositiveButton("Exit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //mBtvExit.callOnClick();
                        MapIOManager.getInstance().updateMapManagerMode(MapManagerModeRouteFinding, false, null);
                        //EventFragment.newInstance(mPlate).show(mActivity.getSupportFragmentManager(), EventFragment.TAG);
                    }
                });
        normalDialog.setNeutralButton("Show event",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventFragment.newInstance(mPlate).show(mActivity.getSupportFragmentManager(), EventFragment.TAG);
                    }
                });
        normalDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //EventFragment.newInstance(mPlate).show(mActivity.getSupportFragmentManager(), EventFragment.TAG);
                    }
                });



        setTranslationY(STATE_HIDDEN.getTranslationY());
    }

    public static void arriveExit(String exitName){
        normalDialog.setTitle("You have arrived at "+exitName);
        normalDialog.show();
    }

    public void setState(State state, Runnable onAnimationEnd) {
        switch (state) {
            case STATE_EXPANDED:
                expanded(onAnimationEnd);
                break;
            case STATE_COLLAPSED:
                collapse(onAnimationEnd);
                break;
            case STATE_HIDDEN:
                hide(onAnimationEnd);
                break;
        }
        mState = state;
    }


    public void initialize(SPPlateModel plate, IDPathResultModel pathResult) {
        mPlate = plate;
        result = pathResult;

        /*
        SPBuildingModel building = DataIOManager.getInstance().postalCodeToBuilding(plate.postalCode);
        if (building != null) {
            mTvName.setText(building.name);
        } else {
            mTvName.setText("");
        }
        */
        mTvName.setText(plate.building_name);

        SPTeleprompter teleprompter = new SPTeleprompter(pathResult.routes);
        double distance = calculateDistanceInMeters();
        String minutes = teleprompter.timeIntervalForDistance(distance);
        String meters = teleprompter.distanceDescription(distance);

        mTvTime.setText(String.format("%s(%s)", minutes, meters));
        MapIOManager.getInstance().addNotifyObserver(this);
    }

    public double calculateDistanceInMeters() {
        double distance = 0.0;
        for (IDRouteModel route : result.routes) {
            distance += GeoUtils.distance(route.start.coordinate, route.end.coordinate);
        }

        return distance;
    }

    @Override
    public void didArrivedDestination(SPPlateModel plate) {
        mIbEvent.performClick();
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
                if (targetY > STATE_COLLAPSED.getTranslationY() || targetY < STATE_EXPANDED.getTranslationY())
                    return true;
                mCallback.onSlide(offsetY);
                setTranslationY(targetY);
                break;
            case MotionEvent.ACTION_UP://松开回弹
                float translationY = getTranslationY();
                if (translationY >= STATE_COLLAPSED.thresholdTranslationY) {
                    setState(STATE_COLLAPSED, null);
                    return true;
                } else {
                    setState(STATE_EXPANDED, null);
                    return true;
                }
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void expanded(Runnable onAnimationEnd) {
        translationYAnimator(STATE_EXPANDED.getTranslationY(), translationYOffset(STATE_EXPANDED), onAnimationEnd);
    }

    public void collapse(Runnable onAnimationEnd) {
        translationYAnimator(STATE_COLLAPSED.getTranslationY(), translationYOffset(STATE_COLLAPSED), onAnimationEnd);
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
        STATE_HIDDEN(2);//隐藏

        public int type;
        public float height;//当前高度
        public float totalHeight;//总高度 == 完全展开的高度
        public float thresholdTranslationY; //平移回弹阈值

        State(int type) {
            this.type = type;

            Resources resources = HKUApplication.sAPP.getResources();
            float topHeight = resources.getDimension(R.dimen.dp_54);
            float bottomHeight = resources.getDimension(R.dimen.dp_30);

            totalHeight = topHeight + bottomHeight;

            switch (type) {
                case 0:
                    height = totalHeight;
                    break;
                case 1:
                    height = topHeight;
                    thresholdTranslationY = bottomHeight / 2;
                    break;
                case 2:
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
