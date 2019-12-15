package dupd.hku.com.hkusap.window;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.model.LatLng;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.manager.DeadReckoningManager;
import dupd.hku.com.hkusap.utils.LocationConverter;

public class AlertFragment extends BottomSheetDialogFragment {

    public static final String IBEACON_UNAVAILABLE = "Please go to nearest lift lobby to activate our service";
    public static final String LOCATION_UNAVAILABLE = "You are not in our service area, please go to HKU Centennial Campus to activate our service";
    public static final String TAG = "AlertFragment";
    @BindView(R.id.tv_message)
    TextView tvMessage;
    @BindView(R.id.btn_got_it)
    Button btnGotIt;
    Unbinder unbinder;


    public static AlertFragment newInstance(String message) {
        AlertFragment fragment = new AlertFragment();
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alert, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnGotIt.setOnClickListener(v -> dismiss());
        String message = getArguments().getString("message");
        tvMessage.setText(message);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AMapLocationListener locationListener = (aMapLocation) -> {
                LatLng targetLocation = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                DeadReckoningManager.getInstance().processDeadReckoningCoordinate(targetLocation, false);
            };
            AMapLocationClient locationClient = new AMapLocationClient(HKUApplication.sAPP.getApplicationContext());
            locationClient.setLocationListener(locationListener);
            AMapLocationClientOption locationClientOption = new AMapLocationClientOption();
            locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            locationClientOption.setOnceLocation(true);
            locationClientOption.setOnceLocationLatest(true);
            locationClient.setLocationOption(locationClientOption);
            locationClient.startLocation();
        }
    }
}
