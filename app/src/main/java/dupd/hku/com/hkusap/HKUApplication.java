package dupd.hku.com.hkusap;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.amap.api.maps.model.LatLng;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;

import dupd.hku.com.hkusap.manager.BeaconManager;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.manager.POIManager;
import dupd.hku.com.hkusap.model.Beacon;
import dupd.hku.com.hkusap.model.BeaconLayer;
import dupd.hku.com.hkusap.model.IEnum;
import dupd.hku.com.hkusap.model.POI;
import dupd.hku.com.hkusap.model.POILayer;
import dupd.hku.com.hkusap.model.POIPoint;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.utils.ActivityStack;
import dupd.hku.com.hkusap.utils.AppLifecycleHandler;
import dupd.hku.com.hkusap.utils.AssetsUtil;
import dupd.hku.com.hkusap.utils.GeoUtils;
import dupd.hku.com.hkusap.utils.YogaUncaughtExceptionHandler;

import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeLift;
import static dupd.hku.com.hkusap.utils.AppLifecycleHandler.LifeCycleDelegate;

/**
 * Created by liuwei on 2018/9/14.
 */

public class HKUApplication extends Application implements LifeCycleDelegate {
    public static HKUApplication sAPP;

    @Override
    public void onCreate() {
        super.onCreate();
        sAPP = this;
        ActivityStack.init(this);
        Utils.init(this);
        CrashReport.initCrashReport(getApplicationContext(), "5327e22371", true);

        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return true;
            }
        });



        String beaconJson = AssetsUtil.readAssetJson("JSON/beacon.json");
        List<BeaconLayer> beaconLayers = BeaconManager.getInstance().storeBeaconJson(beaconJson);
        List<SPPlateModel> plates = new ArrayList<SPPlateModel>();
        List<Beacon> beacons = new ArrayList<Beacon>();
        for (BeaconLayer layer: beaconLayers) {
            beacons.addAll(layer.beacons);
        }
        for (Beacon beacon: beacons) {
            SPPlateModel plate = new SPPlateModel();
            plate.UUID = beacon.uuid;
            plate.plateID = beacon.uuid.replace("-", "");
            //plate.plateID = beacon.plate_id;
            plate.levelCode = beacon.level_code;
            plate.mPower = beacon.m_power;
            IEnum.SPWeightType type = IEnum.SPWeightType.SPWeightTypeOthers;
            if (beacon.type == 1) {
                type = IEnum.SPWeightType.SPWeightTypeEntrance;
            } else if (beacon.type == 2) {
                type = SPWeightTypeLift;
            } else if (beacon.type == 3) {
                type = IEnum.SPWeightType.SPWeightTypeDoor;
            } else if (beacon.type == 4) {
                type = IEnum.SPWeightType.SPWeightTypeEscalator;
            }
            plate.type = type;
            plate.name = beacon.name;
            plate.speakOut = beacon.speakout;
            plate.searchable = beacon.searchable ? 1 : 0;
            plate.roomID  = beacon.room_id;
            LatLng coordinate = GeoUtils.convertCoordinate(new LatLng(beacon.latitude, beacon.longitude));
            plate.latitude = coordinate.latitude;
            plate.longitude = coordinate.longitude;
            plate.latitude = beacon.latitude;
            plate.longitude = beacon.longitude;
            plate.postalCode = beacon.postal_code;
            plate.init();
            plates.add(plate);
        }
        DataIOManager.getInstance().sapdb.plate = plates;

        String poiJson = AssetsUtil.readAssetJson("JSON/poi.json");
        List<POILayer> poiLayers = POIManager.getInstance().storePOIJson(poiJson);
        List<POI> pois = new ArrayList<POI>();
        for (POILayer layer: poiLayers) {
            pois.addAll(layer.pois);
        }
        List<POIPoint> poiPoints = new ArrayList<POIPoint>();
        for (POI poi: pois) {
            POIPoint poiPoint = new POIPoint();
            poiPoint.pointID = new Integer(poi.id).toString();
            poiPoint.levelCode = poi.level_code;
            poiPoint.type = poi.interest_type;
            poiPoint.title = poi.title;
            LatLng coordinate = GeoUtils.convertCoordinate(new LatLng(poi.latitude, poi.longitude));
            poiPoint.latitude = coordinate.latitude;
            poiPoint.longitude = coordinate.longitude;
            poiPoint.building_name = poi.building_name;
            poiPoint.poi_type = poi.poi_type;
            poiPoint.mapScale = poi.map_scale;
            poiPoints.add(poiPoint);
        }
        DataIOManager.getInstance().POI = poiPoints;



        AssetsUtil.writeSapdb();

        AppLifecycleHandler.getInstance().registerLifecycleHandler(this);
        AppLifecycleHandler.getInstance().registerLifeCycleDelegate(this);

        YogaUncaughtExceptionHandler.getInstance().setDefaultUncaughtExceptionHandler();
    }

    @Override
    public void onAppBackgrounded() {
        Logger.d("后台");
    }

    @Override
    public void onAppForegrounded() {
        Logger.d("前台");
    }
}
