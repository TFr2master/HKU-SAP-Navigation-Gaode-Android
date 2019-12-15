package dupd.hku.com.hkusap.weight;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;

import org.altbeacon.beacon.Beacon;
import org.jgrapht.generate.GeneralizedPetersenGraphGenerator;

import java.util.List;

import dupd.hku.com.hkusap.ChooseDestinationActivity;
import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.manager.DeadReckoningManager;
import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.manager.RangingManager;
import dupd.hku.com.hkusap.model.SPFloorPlanBase;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.utils.AnimatorUtil;
import dupd.hku.com.hkusap.utils.GeoUtils;
import dupd.hku.com.hkusap.window.AlertFragment;
import dupd.hku.com.hkusap.window.SignFragment;
import dupd.hku.com.hkusap.manager.GPSLocation;

import static dupd.hku.com.hkusap.MainActivity.REQ_CHOOSE;
import static dupd.hku.com.hkusap.manager.MapManagerMode.MapManagerModeRouteFinding;
import static dupd.hku.com.hkusap.weight.BottomSheet.State.STATE_COLLAPSED;
import static dupd.hku.com.hkusap.weight.BottomSheet.State.STATE_HIDDEN;
import static dupd.hku.com.hkusap.window.AlertFragment.IBEACON_UNAVAILABLE;
import static dupd.hku.com.hkusap.window.AlertFragment.LOCATION_UNAVAILABLE;

public class PositioningToolbar extends CompatToolbar implements MapIOManager.MapIOManagerObserverableProtocol {

    private final SPFloorPlanBase base;
    private final LatLngBounds mLatLngBounds;
    private ImageView mIvAvatar;
    private TextView mTvTitle;
    private ImageView mIvDirective;
    private FragmentActivity mActivity;
    private SPPlateModel selectedPlate;
    private Beacon mNearest;
    private Location mLocation;
    private GPSLocation mGPSLocation;

    public PositioningToolbar(@NonNull Context context) {
        this(context, null);
    }

    public PositioningToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PositioningToolbar(@NonNull final Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mActivity = (FragmentActivity) context;
        mGPSLocation=new GPSLocation(context);

        View view = LayoutInflater.from(context).inflate(R.layout.toolabr_positioning, this, false);
        addView(view);
        mIvAvatar = findViewById(R.id.iv_avatar);
        mTvTitle = findViewById(R.id.tv_title);
        mIvDirective = findViewById(R.id.iv_directive);

        mTvTitle.setOnClickListener(v -> {
            if(!checkDeadReckoning()) {
                if (!insideLatLng()) {
                    AlertFragment.newInstance(LOCATION_UNAVAILABLE).show(mActivity.getSupportFragmentManager(), AlertFragment.TAG);
                    return;
                }
                if (!checkIbeacon()) {//else if (!RangingManager.getInstance().ibeaconAvailable())
                    AlertFragment.newInstance(IBEACON_UNAVAILABLE).show(mActivity.getSupportFragmentManager(), AlertFragment.TAG);
                    return;
                }
            }
            gotoChooseDestination();
        });

        mIvAvatar.setOnClickListener(v -> {
            if(!checkDeadReckoning()) {
                if (!checkIbeacon()) {
                    AlertFragment.newInstance(IBEACON_UNAVAILABLE).show(mActivity.getSupportFragmentManager(), AlertFragment.TAG);
                }
                if (!insideLatLng()) {
                    AlertFragment.newInstance(LOCATION_UNAVAILABLE).show(mActivity.getSupportFragmentManager(), AlertFragment.TAG);
                }
            }else {
                SignFragment.newInstance().show(mActivity.getSupportFragmentManager(), SignFragment.TAG);
            }
        });

        mIvDirective.setOnClickListener(v -> {
            if(!checkDeadReckoning()) {
                if (!checkIbeacon()) {
                    AlertFragment.newInstance(IBEACON_UNAVAILABLE).show(mActivity.getSupportFragmentManager(), AlertFragment.TAG);
                    return;
                }
                if (!insideLatLng()) {
                    AlertFragment.newInstance(LOCATION_UNAVAILABLE).show(mActivity.getSupportFragmentManager(), AlertFragment.TAG);
                    return;
                }
            }
            if (selectedPlate != null) {
                mIvDirective.setSelected(false);
                mIvDirective.setImageResource(R.drawable.jiantou);
                mTvTitle.setText("");
                selectedPlate = null;
                MapIOManager.getInstance().updateDestinationPlateAlone(null, false, null);
                BottomSheetState.getInstance().statePositioning(STATE_HIDDEN, null, null);
            } else {
                MapIOManager.getInstance().updateMapManagerMode(MapManagerModeRouteFinding, false, this::gotoChooseDestination);
            }
        });
        base = new SPFloorPlanBase();
        MapIOManager.getInstance().addNotifyObserver(this);
        LatLng southWest = bottomLeftCoordinate();
        LatLng northEast = topRightCoordinate();

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        bounds.include(southWest);
        bounds.include(northEast);
        mLatLngBounds = bounds.build();

        mIvAvatar.setImageResource(checkDeadReckoning()||(insideLatLng() && checkIbeacon()) ? R.drawable.avatar_black : R.drawable.warn_s);
    }

    public void didSelectedPlate(SPPlateModel plate) {
        MapIOManager.getInstance().updateDestinationPlateAlone(plate, true, () ->
                BottomSheetState.getInstance().statePositioning(STATE_COLLAPSED, plate, () -> {
                    float height = -AnimatorUtil.getInstance().getToolbar().getHeight();
                    MapIOManager.getInstance().mMap.animateCamera(CameraUpdateFactory.scrollBy(0, height));
                }));
        selectedPlate = plate;
        mIvDirective.setImageResource(R.drawable.delete);
        mTvTitle.setText(plate.name);
    }

    private void gotoChooseDestination() {
        Intent intent = ChooseDestinationActivity.newIntent(mActivity, selectedPlate);
        mActivity.startActivityForResult(intent, REQ_CHOOSE);
    }

    @Override
    public void didUpdateLocations(Location location) {
    }

    @Override
    public void didRangedBeaconsMap(List<Beacon> beacons) {
        if (!beacons.isEmpty()) {
            mNearest = beacons.get(0);
        }
        if (mIvAvatar == null) {
            return;
        }
        mIvAvatar.setImageResource(checkDeadReckoning()||(insideLatLng() && checkIbeacon()) ? R.drawable.avatar_black : R.drawable.warn_s);
    }

    public boolean insideLatLng() {
        mLocation=mGPSLocation.getLocation();
        LatLng coordinate;
        if(mLocation!=null){
            coordinate=new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
            if (mLatLngBounds.contains(coordinate)) {
                return true;
            }
        }
        return false;
    }
    public boolean checkIbeacon(){
        return RangingManager.getInstance().ibeaconAvailable();//change to checkIbeacon()
    }
    public boolean checkDeadReckoning(){
        return DeadReckoningManager.getInstance().checkDeadReckoning();
    }

    public LatLng topRightCoordinate() {
        return GeoUtils.convertCoordinate(new LatLng(base.topRight.latitude, base.topRight.longitude));
    }

    public LatLng bottomLeftCoordinate() {
        return GeoUtils.convertCoordinate(new LatLng(base.bottomLeft.latitude, base.bottomLeft.longitude));
    }

}
