package dupd.hku.com.hkusap.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.orhanobut.logger.Logger;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.logging.LogManager;
import org.altbeacon.beacon.logging.Loggers;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.Collection;
import java.util.Date;

import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.manager.core.Core;
import dupd.hku.com.hkusap.manager.core.Locationer;
import dupd.hku.com.hkusap.manager.core.Locationer.OnLocationUpdateListener;
import dupd.hku.com.hkusap.model.Often;
import dupd.hku.com.hkusap.utils.GeoUtils;
import dupd.hku.com.hkusap.utils.LocationConverter;

public class CLLocationManager {

    private static final String TAG = "CLLocationManager";
    private static final long DEFAULT_BACKGROUND_SCAN_PERIOD = 1000L;
    private static final long DEFAULT_BACKGROUND_BETWEEN_SCAN_PERIOD = 1000L;
    private static final String ALTBEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    //    private final SensorManager mSensorManager;
    private final BeaconManager mBeaconManager;
    private final AMapLocationClient mLocationClient;
    //    private PublishSubject<Float> mSubject = PublishSubject.create();
    public CLLocationManagerDelegate delegate;
    private Core mCore;
    private Locationer mLocationer;

    public CLLocationManager() {
//        mSensorManager = (SensorManager) HKUApplication.sAPP.getSystemService(Context.SENSOR_SERVICE);
        mCore = new Core(mOnStepUpdateListener);
        mLocationer = new Locationer(mLocationUpdateListener);
        mLocationClient = new AMapLocationClient(HKUApplication.sAPP.getApplicationContext());
//
//        mSubject.buffer(1_000, TimeUnit.MILLISECONDS)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .filter(floats -> !floats.isEmpty())
//                .subscribe(floats -> {
//                    float azimuth = floats.get(0);
//                    DataIOManager.getInstance().mOften.onNext(new Often(4, "方向角:" + azimuth));
//                    delegate.didUpdateHeading(azimuth);
//                }, Throwable::printStackTrace).isDisposed();

        LogManager.setLogger(Loggers.verboseLogger());
        LogManager.setVerboseLoggingEnabled(true);
        mBeaconManager = BeaconManager.getInstanceForApplication(HKUApplication.sAPP);
        mBeaconManager.getBeaconParsers().clear();
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(ALTBEACON_LAYOUT));
        Region region = new Region("all-region-beacon", null, null, null);
        new RegionBootstrap(mBootstrapNotifier, region);
        new BackgroundPowerSaver(HKUApplication.sAPP);

        mBeaconManager.setBackgroundScanPeriod(DEFAULT_BACKGROUND_SCAN_PERIOD);
        mBeaconManager.setBackgroundBetweenScanPeriod(DEFAULT_BACKGROUND_BETWEEN_SCAN_PERIOD);
        mBeaconManager.bind(new BeaconConsumer() {
            @Override
            public void onBeaconServiceConnect() {
                mBeaconManager.addRangeNotifier((collections, region) -> delegate.didRangeBeacons(collections, region));
            }

            @Override
            public Context getApplicationContext() {
                return HKUApplication.sAPP;
            }

            @Override
            public void unbindService(ServiceConnection serviceConnection) {

            }

            @Override
            public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
                return false;
            }
        });
    }

//    private SensorEventListener mEventListener = new SensorEventListener() {
//
//        private float[] mAccelerometerValues = new float[3];// 用于保存加速度值
//        private float[] mMagneticValues = new float[3];// 用于保存地磁值
//
//        @Override
//        public void onSensorChanged(SensorEvent event) {
//            switch (event.sensor.getType()) {
//                case Sensor.TYPE_ACCELEROMETER://加速传感器
//                    mAccelerometerValues = event.values;
//                    calculateOrientation();
//                    break;
//                case Sensor.TYPE_MAGNETIC_FIELD://磁场传感器
//                    mMagneticValues = event.values;
//                    calculateOrientation();
//                    break;
//            }
//        }
//
//        /**
//         * <pre>
//         *     计算方向
//         *     根据传感器获取到的加速度值和地磁值先得出旋转矩阵
//         *     再根据旋转矩阵得到最终结果，最终结果包含航向角、俯仰角、翻滚角
//         * </pre>
//         */
//        private void calculateOrientation() {
//            float[] values = new float[3];// 最终结果
//            float[] R = new float[9];// 旋转矩阵
//            SensorManager.getRotationMatrix(R, null, mAccelerometerValues, mMagneticValues);// 得到旋转矩阵
//            SensorManager.getOrientation(R, values);// 得到最终结果
//            float azimuth = (float) Math.toDegrees(values[0]);// 航向角
//            if (azimuth < 0) {
//                azimuth += 360;
//            }
//            azimuth = azimuth / 5 * 5;// 做了一个处理，表示以5°的为幅度
//            mSubject.onNext(azimuth);
//        }
//
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//        }
//    };

    @SuppressLint("MissingPermission")
    public void startUpdatingLocation() {
        mLocationClient.setLocationListener((aMapLocation) -> {
            if (aMapLocation == null) return;
            long currentTime = new Date().getTime() / 1000;
            long timeDiff = currentTime - RangingManager.getInstance().refreshedTime;

            if (RangingManager.getInstance().precisionLevel == 0 && timeDiff < 30) {
                return;
            } else if (RangingManager.getInstance().precisionLevel == 1) {
                if (timeDiff < 30) {
                    if (aMapLocation.getAccuracy() > 10) {
                        return;
                    }
                }

            } else if (RangingManager.getInstance().precisionLevel == 2) {
                if (timeDiff < 30) {
                    if (aMapLocation.getAccuracy() > 30) {
                        return;
                    }
                }
            }
            if (timeDiff > 30) {
                RangingManager.getInstance().refreshedTime = currentTime;
                RangingManager.getInstance().precisionLevel++;
            }
            Location location = aMapLocation;
            delegate.didUpdateLocations(location);
        });
        AMapLocationClientOption locationClientOption = new AMapLocationClientOption();
        locationClientOption.setInterval(5000);
        locationClientOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClient.setLocationOption(locationClientOption);
        mLocationClient.startLocation();
//        mProviderClient.getLastLocation().addOnSuccessListener(location -> delegate.didUpdateLocations(location));
    }

    public boolean startUpdatingHeading() {
        mCore.enableAutocorrect();
        mCore.startSensors();

        return true;
//        if (mSensorManager == null) {
//            return false;
//        }
//        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//加速传感器
//        Sensor magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);//磁场传感器
//        boolean accelerometerAvailable = mSensorManager.registerListener(mEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
//        boolean magneticAvailable = mSensorManager.registerListener(mEventListener, magneticSensor, SensorManager.SENSOR_DELAY_UI);
//        if (accelerometerAvailable && magneticAvailable) {
//            return true;
//        } else {
//            return false;
//        }
    }

    public void startAllRegionBeacon() {
        Identifier minor = Identifier.fromInt(7275);
        //Region region = new Region("all-region-beacon", null, major, null);
        Region region = new Region("all-region-beacon", null, null, null);
        //Region region = new Region("all-region-beacon", null, null, minor);
//        String uuid_145 = "11030713-5148-0103-a000-a000a020a053";
//        UUID uuid = UUID.fromString(uuid_145);
//        Region region = new Region(uuid_145, Identifier.fromUuid(uuid), null, null);
        startRangingBeaconsInRegion(region);
    }

    public void startRangingBeaconsInRegion(Region region) {
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void startMonitoringForRegion(Region region) {
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopUpdatingHeading() {
//        mSensorManager.unregisterListener(mEventListener);
    }

    public void stopUpdatingLocation() {
        // TODO: 2018/10/23
    }

    public void stopAllRegionBeacon() {
        Region region = new Region("all-region-beacon", null, null, null);
        stopRangingBeaconsInRegion(region);
    }

    public void stopRangingBeaconsInRegion(Region region) {
        try {
            mBeaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopMonitoringForRegion(Region region) {
        try {
            mBeaconManager.stopMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public Core.OnStepUpdateListener mOnStepUpdateListener = new Core.OnStepUpdateListener() {
        private float row = 0;

        @Override
        public void onStepUpdate(int event) {
            if (event == 0) {
                float rotation = (float) Core.azimuth;

                if ((rotation > 0 && rotation < 20) && (row > 340 && row < 360)) {
                    Log.d(TAG, "防止显示上多转动");
                } else if ((rotation > 340 && rotation < 360) && (row > 0 && row < 20)) {
                    Log.d(TAG, "防止显示上多转动");
                } else {
                    DataIOManager.getInstance().mOften.onNext(new Often(4, rotation));
                    delegate.didUpdateHeading(rotation);
                }
            } else {
                mLocationer.starteAutocorrect();
            }
        }
    };

    private OnLocationUpdateListener mLocationUpdateListener = event -> {
        switch (event) {
            case 0:
                // First Position from the Locationer
                break;
            case 5:
                //    showGPSDialog();
                break;
            case 8:
                Core.setLocation(Locationer.startLat, Locationer.startLon);
                break;
            case 12:
                // message from Locationer
                break;
            case 14:
                // next position from Locationer
        }
    };

    private BootstrapNotifier mBootstrapNotifier = new BootstrapNotifier() {
        @Override
        public Context getApplicationContext() {
            return HKUApplication.sAPP;
        }

        @Override
        public void didEnterRegion(Region region) {
            Logger.d(region.toString());
        }

        @Override
        public void didExitRegion(Region region) {
            Logger.d(region.toString());
        }

        @Override
        public void didDetermineStateForRegion(int i, Region region) {
            Logger.d(i + "\n" + region.toString());
        }
    };

    public interface CLLocationManagerDelegate {

        void didRangeBeacons(Collection<Beacon> beacons, Region region);

        void didUpdateHeading(float heading);

        void didUpdateLocations(Location location);
    }
}
