package dupd.hku.com.hkusap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.amap.api.maps.MapView;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.CameraUpdateFactory;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dupd.hku.com.hkusap.base.BaseFragment;
import dupd.hku.com.hkusap.utils.AnimatorUtil;
import dupd.hku.com.hkusap.weight.BottomSheetCallback;
import rxpermissions.PermissionTips;
import rxpermissions.RxPermissions;

public class MapFragment extends BaseFragment implements BottomSheetCallback {

    private static final String MAP_VIEW_BUNDLE_KEY = "MAP_VIEW_BUNDLE_KEY";
    @BindView(R.id.mapView)
    MapView mMapView;
    Unbinder unbinder;
    private AMap mMap;
    private RxPermissions mRxPermissions;
    private OnMapFragmentListener mOnMapListener;
    private boolean mOnMapReady;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMapFragmentListener) {
            mOnMapListener = (OnMapFragmentListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mRxPermissions = new RxPermissions(this);
        mMap = mMapView.getMap();
        mMap.setOnMapLoadedListener(() -> {
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.setOnMapClickListener(latLng -> mOnMapListener.onMapClick(latLng));
            moveCompass();
            enableMyLocation();
        });
        /*
        mMapView.getMapAsync(googleMap -> {
            mMap = googleMap;
//            mMap.setMapType(MAP_TYPE_SATELLITE);
//            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.setOnMapClickListener(latLng -> mOnMapListener.onMapClick(latLng));
            moveCompass();
            enableMyLocation();
        });
         */
    }

    private void requestPermission() {
        if (mRxPermissions == null) return;
        mRxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BODY_SENSORS)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        enableMyLocation();
                    } else {
                        PermissionTips.showPermissionTipsDialog(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                }, Throwable::printStackTrace).isDisposed();
    }


    @SuppressLint("MissingPermission")
    public void enableMyLocation() {
        if (mRxPermissions == null) return;
        if (!mRxPermissions.isGranted(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                || !mRxPermissions.isGranted(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                || !mRxPermissions.isGranted(getActivity(), Manifest.permission.BLUETOOTH_ADMIN)
                ) {
            requestPermission();
            return;
        }

        if (mMap == null || mOnMapReady) return;
        // Access to the location has been granted to the app.
        mMap.setMyLocationEnabled(false);
        mOnMapListener.onMapReady(mMap);
        mOnMapReady = true;
    }

    @Override
    public void onSlideAnimator(float offset, long duration) {
        mMap.animateCamera(CameraUpdateFactory.scrollBy(0, -offset), (int) duration, null);
    }

    @Override
    public void onSlide(float offset) {
        mMap.moveCamera(CameraUpdateFactory.scrollBy(0, -offset));
    }

    public void moveCompass() {
        mMapView.post(() -> {
            try {
                ViewGroup parent = (ViewGroup) mMapView.findViewWithTag("GoogleMapMyLocationButton").getParent();
                View mapCompass = parent.getChildAt(4);
                // create layoutParams, giving it our wanted width and height(important, by default the width is "match parent")
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(mapCompass.getHeight(), mapCompass.getHeight());
                // position on top right
                rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);

                rlp.rightMargin = (int) getResources().getDimension(R.dimen.dp_20);
                rlp.topMargin = AnimatorUtil.getInstance().getToolbar().getHeight() + (int) getResources().getDimension(R.dimen.dp_20);

                mapCompass.setLayoutParams(rlp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        //mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        //mMapView.onStop();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) mMapView.onDestroy();
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //apView.onLowMemory();
    }

    public interface OnMapFragmentListener {
        void onMapReady(AMap map);

        void onMapClick(LatLng latLng);
    }
}
