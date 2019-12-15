package dupd.hku.com.hkusap.weight;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import dupd.hku.com.hkusap.ChooseDestinationActivity;
import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.manager.DeadReckoningManager;
import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.manager.ViewController;
import dupd.hku.com.hkusap.model.IDPathResultModel;
import dupd.hku.com.hkusap.model.SPBuildingModel;
import dupd.hku.com.hkusap.model.SPLevelModel;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.utils.AnimatorUtil;
import dupd.hku.com.hkusap.window.EventFragment;
import dupd.hku.com.hkusap.window.OptionFragment;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static dupd.hku.com.hkusap.MainActivity.REQ_SEARCH;
import static dupd.hku.com.hkusap.manager.MapManagerMode.MapManagerModePositioning;
import static dupd.hku.com.hkusap.manager.MapManagerMode.MapManagerModeRouteFinding;
import static dupd.hku.com.hkusap.weight.BottomSheetRoute.State.STATE_COLLAPSED;
import static dupd.hku.com.hkusap.weight.BottomSheetRoute.State.STATE_HIDDEN;

public class RouteFindingToolbar extends CompatToolbar {

    private final TextView mTvOption;
    private ImageView mIvBack;
    private TextView mTvSearchDestination;
    private TextView mTvLocation;
    private SPBuildingModel mBuilding;
    private SPLevelModel mLevel;
    private SPPlateModel plate;

    public RouteFindingToolbar(@NonNull Context context) {
        this(context, null);
    }

    public RouteFindingToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RouteFindingToolbar(@NonNull final Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View view = LayoutInflater.from(context).inflate(R.layout.toolabr_route_finding, this, false);
        addView(view);
        mIvBack = findViewById(R.id.iv_back);
        mTvLocation = findViewById(R.id.tv_location);
        mTvOption = findViewById(R.id.tv_option);
        mTvSearchDestination = findViewById(R.id.tv_search_destination);

        FragmentActivity activity = (FragmentActivity) context;

        mTvSearchDestination.setOnClickListener(v -> {
            Intent intent = ChooseDestinationActivity.newIntent(activity, plate);
            activity.startActivityForResult(intent, REQ_SEARCH);
        });

        mIvBack.setOnClickListener(v -> {
            MapIOManager.getInstance().cleanShortestPath();
            DeadReckoningManager.getInstance().switchToMapMatchingMode(false, null);

            MapIOManager.getInstance().updateMapManagerMode(MapManagerModePositioning, true, null);
        });

        mTvOption.setOnClickListener(v -> OptionFragment.newInstance(plate).show(activity.getSupportFragmentManager(), EventFragment.TAG));
    }

    public void initialize(SPPlateModel plate) {
        this.plate = plate;
        if (plate == null) {
            mTvSearchDestination.setText("");
        } else {
            mTvSearchDestination.setText(plate.name);
        }
    }

    public void didSelectedPlate(SPPlateModel plate) {
        initialize(plate);
        MapIOManager.getInstance().cleanShortestPath();
        MapIOManager.getInstance().updateDestinationPlateAlone(plate, false, null);

        ViewController viewController = AnimatorUtil.getInstance().mViewController;
        viewController.showLoadDialog("Searching Route...");

        if (!MapIOManager.getInstance().prepareForShortestPathCalculation()) {
            viewController.showLoadDialog("Route not found");
            viewController.dismissDialogDelay(3_000);

            this.plate = null;
            MapIOManager.getInstance().updateDestinationPlateAlone(null, false, null);
            MapIOManager.getInstance().removeStartingPoint();
            MapIOManager.getInstance().removeDestination();
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

                    MapIOManager.getInstance().boundsToShortestPath(
                            () -> BottomSheetState.getInstance().stateRouteFinding(STATE_HIDDEN, plate, result,
                                    () -> BottomSheetState.getInstance().stateRouteFinding(STATE_COLLAPSED, plate, result, null)));

                }, throwable -> {
                    throwable.printStackTrace();
                    viewController.showLoadDialog("Route not found");
                    viewController.dismissDialogDelay(3_000);

                    MapIOManager.getInstance().updateDestinationPlateAlone(null, false, null);
                    MapIOManager.getInstance().removeStartingPoint();
                    MapIOManager.getInstance().removeDestination();
                }).isDisposed();
    }
}
