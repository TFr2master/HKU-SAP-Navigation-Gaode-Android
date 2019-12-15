package dupd.hku.com.hkusap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import dupd.hku.com.hkusap.MapFragment.OnMapFragmentListener;
import dupd.hku.com.hkusap.base.BaseActivity;
import dupd.hku.com.hkusap.manager.BeaconManager;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.manager.DeadReckoningManager;
import dupd.hku.com.hkusap.manager.IDPathFinderManager;
import dupd.hku.com.hkusap.manager.IDRouteCollection;
import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.manager.MapIOManager.MapIOManagerObserverableProtocol;
import dupd.hku.com.hkusap.manager.POILayerDownloader;
import dupd.hku.com.hkusap.manager.POIManager;
import dupd.hku.com.hkusap.manager.RangingManager;
import dupd.hku.com.hkusap.manager.TileManager;
import dupd.hku.com.hkusap.manager.ViewController;
import dupd.hku.com.hkusap.model.Beacon;
import dupd.hku.com.hkusap.model.BeaconLayer;
import dupd.hku.com.hkusap.model.IEnum;
import dupd.hku.com.hkusap.model.POI;
import dupd.hku.com.hkusap.model.POILayer;
import dupd.hku.com.hkusap.model.POIPoint;
import dupd.hku.com.hkusap.model.SPNavigationRouteModel;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.model.SPSelectedIndex;
import dupd.hku.com.hkusap.utils.AnimatorUtil;
import dupd.hku.com.hkusap.utils.ApiUtil;
import dupd.hku.com.hkusap.utils.AssetsUtil;
import dupd.hku.com.hkusap.utils.GeoUtils;
import dupd.hku.com.hkusap.utils.MainUtils;
import dupd.hku.com.hkusap.utils.SpUtil;
import dupd.hku.com.hkusap.weight.BottomSheet;
import dupd.hku.com.hkusap.weight.BottomSheetCallback;
import dupd.hku.com.hkusap.weight.BottomSheetNavigation;
import dupd.hku.com.hkusap.weight.BottomSheetRoute;
import dupd.hku.com.hkusap.weight.BottomSheetState;
import dupd.hku.com.hkusap.weight.NavigationToolbar;
import dupd.hku.com.hkusap.weight.OptionLevelView;
import dupd.hku.com.hkusap.weight.PositioningToolbar;
import dupd.hku.com.hkusap.weight.RouteFindingToolbar;
import dupd.hku.com.hkusap.window.AlertFragment;
import dupd.hku.com.hkusap.manager.ShakeDetector;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import com.crashlytics.android.Crashlytics;



import io.fabric.sdk.android.Fabric;

import static dupd.hku.com.hkusap.DebugActivity.PARAMS_KEY;
import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeLift;
import static dupd.hku.com.hkusap.window.AlertFragment.IBEACON_UNAVAILABLE;
import static dupd.hku.com.hkusap.window.AlertFragment.LOCATION_UNAVAILABLE;

public class MainActivity extends BaseActivity implements ViewController, OnMapFragmentListener, MapIOManagerObserverableProtocol {
    public static final int REQ_CHOOSE = 111;
    public static final int REQ_SEARCH = 112;

    @BindView(R.id.optionLevelView)
    OptionLevelView mOptionLevelView;
    @BindView(R.id.positioningToolbar)
    PositioningToolbar mPositioningToolbar;
    @BindView(R.id.navigationToolbar)
    NavigationToolbar mNavigationToolbar;
    @BindView(R.id.routeFindingToolbar)
    RouteFindingToolbar mRouteFindingToolbar;
    @BindView(R.id.bottomSheet)
    BottomSheet mBottomSheet;
    @BindView(R.id.bottomSheetRoute)
    BottomSheetRoute mBottomSheetRoute;
    @BindView(R.id.bottomSheetNavigation)
    BottomSheetNavigation mBottomSheetNavigation;
    @BindView(R.id.tv_1)
    TextView mTv1;
    @BindView(R.id.tv_2)
    TextView mTv2;
    @BindView(R.id.tv_3)
    TextView mTv3;
    @BindView(R.id.tv_4)
    TextView mTv4;
    @BindView(R.id.tv_5)
    TextView mTv5;
    @BindView(R.id.tv_6)
    TextView mTv6;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.tv_test)
    TextView tvTest;
    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheet;

    private MapFragment mMapFragment;
    private AlertDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainUtils.setStatusBar(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        AnimatorUtil.getInstance().registerToolbar(mPositioningToolbar, mRouteFindingToolbar, mNavigationToolbar, this);
        BottomSheetState.getInstance().registerBottomSheet(mBottomSheet, mBottomSheetRoute, mBottomSheetNavigation);

        MapIOManager.getInstance().addNotifyObserver(this);

        List<SPNavigationRouteModel> routes = DataIOManager.getInstance().routes;
        DeadReckoningManager.getInstance().registerNavigationRoutes(routes);
        IDPathFinderManager.getInstance().registerNavigationRoutes(routes);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map);
        if (fragment instanceof MapFragment) {
            mMapFragment = (MapFragment) fragment;
        } else {
            mMapFragment = new MapFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map, mMapFragment)
                .commitAllowingStateLoss();

        mBottomSheet.addBottomSheetCallback(mBottomSheetCallback);
        mBottomSheetRoute.addBottomSheetCallback(mBottomSheetCallback);
        mBottomSheetNavigation.addBottomSheetCallback(mBottomSheetCallback);

        ApiUtil.getInstance().sapdb();
        DataIOManager.getInstance().mSubject.subscribe(text -> DataIOManager.getInstance().mNormalDebug.append(text), throwable -> {
        }).isDisposed();

        TextView[] textView = new TextView[]{
                mTv1, mTv2, mTv3, mTv4, mTv5,
        };
        DataIOManager.getInstance().mOften.subscribe(often -> {
            if (often.index == 5) {
                mTv6.setText(often.message);
            } else if (often.index == 3) {
                int number = 0;
                try {
                    number = Integer.valueOf(textView[3].getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mTv6.setText(String.valueOf(number + often.value));
            } else {
                textView[often.index].setText(often.getMessage());
            }
        }, throwable -> {
        }).isDisposed();

        tvTest.setMovementMethod(ScrollingMovementMethod.getInstance());
        float height = getResources().getDimension(R.dimen.dp_300);
        DataIOManager.getInstance().mSubject.subscribe(text -> {
            tvTest.append(text);
            int offset = tvTest.getLineCount() * tvTest.getLineHeight();
            if (offset > height) {
                tvTest.scrollTo(0, offset - tvTest.getHeight());
            }
        }, throwable -> {
        }).isDisposed();

        btnSend.setOnClickListener(v -> {
            Intent intent = DebugActivity.newIntent(mActivity);
            startActivity(intent);
        });
        SharedPreferences sharedPreferences = HKUApplication.sAPP.getApplicationContext().getSharedPreferences(HKUApplication.sAPP.getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("Estimote_N", (float)3.259);
        editor.commit();

        Observable.timer(60, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (mPositioningToolbar == null) {
                        return;
                    }
                    AlertFragment alertFragment = (AlertFragment) getSupportFragmentManager().findFragmentByTag(AlertFragment.TAG);
                    if (alertFragment != null && alertFragment.isVisible()) {
                        return;
                    }
                    String message = null;
                    if(!mPositioningToolbar.checkDeadReckoning()) {
                        if (!mPositioningToolbar.checkIbeacon()) {
                            message = IBEACON_UNAVAILABLE;
                        }
                        if (!mPositioningToolbar.insideLatLng()) {
                            message = LOCATION_UNAVAILABLE;
                        }
                    }
                    if (message == null) {
                        return;
                    }
                    AlertFragment af = AlertFragment.newInstance(message);
                    getSupportFragmentManager().beginTransaction().add(af, AlertFragment.TAG).commitAllowingStateLoss();
                }, Throwable::printStackTrace).isDisposed();
        Fabric.with(this, new Crashlytics());

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(permissions, 0);

    }

    private void checkBluetoothAndGPS() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            mDialog = new AlertDialog.Builder(this)
                    .setTitle("错误")
                    .setMessage("你的设备不具备蓝牙功能!")
                    .create();
            mDialog.show();
            return;
        }
        if (!adapter.isEnabled()) {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            mDialog = new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setCancelable(false)
                    .setMessage("蓝牙设备未打开,请开启此功能后重试!")
                    .setPositiveButton("确认", (arg0, arg1) -> {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(intent);
                    })
                    .create();
            mDialog.show();
            return;
        }
        if (!GeoUtils.isOpenGPS(this)) {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            mDialog = new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setCancelable(false)
                    .setMessage("请开启GPS定位")
                    .setPositiveButton("确认", (arg0, arg1) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .create();
            mDialog.show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        RangingManager.getInstance().updateEnviParams(SpUtil.getIntPreferences(PARAMS_KEY));
        checkBluetoothAndGPS();
        if (mMapFragment == null) return;
        mMapFragment.enableMyLocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) return;
        SPPlateModel plate = data.getParcelableExtra(SPPlateModel.class.getName());
        if (plate == null) return;
        switch (requestCode) {
            case REQ_CHOOSE:
                mPositioningToolbar.didSelectedPlate(plate);
                break;
            case REQ_SEARCH:
                mRouteFindingToolbar.didSelectedPlate(plate);
                break;
        }
    }

    @Override
    public void dismissDialogDelay(long delay) {
        if (delay == 0) {
            dismissDialog();
            return;
        }
        getWindow().getDecorView().postDelayed(this::dismissDialog, delay);
    }

    @Override
    public void moveCompass() {
        if (mMapFragment != null) {
            mMapFragment.moveCompass();
        }
    }

    @Override
    public Rect mapViewBounds() {
        if (mMapFragment == null || mMapFragment.mMapView == null) {
            return new Rect();
        }
        int height = mMapFragment.mMapView.getHeight();
        int width = mMapFragment.mMapView.getWidth();
        int top = AnimatorUtil.getInstance().getToolbar().getHeight();
        int bottom = (int) BottomSheetState.getInstance().getBottomSheetCollapseHeight();
        return new Rect(0, top, width, height - top - bottom);
    }

    @Override
    public void onMapReady(AMap map) {
//        CrashReport.testJavaCrash();
        DataIOManager.getInstance().mSubject.onNext("地图初始化成功\n");
        MapIOManager.getInstance().registerViewController(this, map);
        MapIOManager.getInstance().setupInitialDisplay();
        MapIOManager.getInstance().centerToFloorPlan();
        MapIOManager.getInstance().startManagerMapMatching(false);
        map.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {

            }

            @Override
            public void onCameraChangeFinish(CameraPosition position) {
                int zoomLevel;
                zoomLevel = GeoUtils.getZoomCode((int)map.getCameraPosition().zoom);
                if(zoomLevel!=MapIOManager.getInstance().oldZoomLevel){
                    MapIOManager.getInstance().updatePOIPointWithZoom(zoomLevel);
                }
            }
        });
        map.setOnMapLongClickListener(latLng -> bottomSheet.setVisibility(View.VISIBLE));
        map.setOnMarkerClickListener(marker -> {
            POIPoint p = (POIPoint)marker.getObject();
            SPPlateModel plate = new SPPlateModel();
            if (p != null) {
                plate.levelCode = p.levelCode;
                plate.name = p.title;
                plate.longitude = p.longitude;
                plate.latitude = p.latitude;
                plate.building_name = p.building_name;
                plate.type = SPWeightTypeLift;
                mPositioningToolbar.didSelectedPlate(plate);
            }
            return true;
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
        bottomSheet.setVisibility(View.GONE);
        mOptionLevelView.hideLevelBar();
        BottomSheetState.getInstance().onMapClick();
    }

    private BottomSheetCallback mBottomSheetCallback = new BottomSheetCallback() {
        @Override
        public void onSlideAnimator(float offset, long duration) {
            mOptionLevelView.onSlideAnimator(offset, duration);
            mMapFragment.onSlideAnimator(offset, duration);
        }

        @Override
        public void onSlide(float offset) {
            mOptionLevelView.onSlide(offset);
            mMapFragment.onSlide(offset);
        }
    };

    @Override
    public void managerDidInsideLift(MapIOManager manager) {
        mOptionLevelView.setTitle();
    }

    @Override
    public void shouldChangeToLevel(String level, boolean preview) {
        if (mOptionLevelView == null) return;
        mOptionLevelView.updateLevelButtonWithLevel(level);
        int index = MapIOManager.getInstance().registeredLevels().indexOf(level);

        mOptionLevelView.setSelectedIndex(index);
        if (!preview) {
            mOptionLevelView.setHightlightLevel(level);
        }
    }

    @Override
    public void didUpdateDestinationPlate(SPPlateModel plate) {

    }

    @Override
    public void didDisplayedShortestPath(IDRouteCollection collection, List<SPSelectedIndex> indexes) {
        if (mOptionLevelView == null) return;
        mOptionLevelView.setIndicatorIndexs(indexes);
    }

    @Override
    public void managerDidRemovedShortestPath(MapIOManager manager) {
        if (mOptionLevelView == null) return;
        mOptionLevelView.setIndicatorIndexs(null);
    }

    @Override
    public void didArrivedDestination(SPPlateModel plate) {

    }

    @Override
    public void didUpdatedHeading(float heading) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MapIOManager.getInstance().dealloc();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
//        super.onBackPressed();
    }

    @Override
    public void didUpdateLocations(Location location) {
        LatLng targetLocation = new LatLng(location.getLatitude(), location.getLongitude());
        DeadReckoningManager.getInstance().processDeadReckoningCoordinate(targetLocation, false);
    }
}
