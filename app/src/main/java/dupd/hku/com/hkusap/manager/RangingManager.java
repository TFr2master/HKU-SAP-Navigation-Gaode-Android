package dupd.hku.com.hkusap.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.orhanobut.logger.Logger;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;


import java.util.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.iBeacon.BeaconComparator;
import dupd.hku.com.hkusap.model.BeaconDebugForm.BeaconDebug;
import dupd.hku.com.hkusap.model.Often;
import dupd.hku.com.hkusap.model.SPPlateModel;

import static dupd.hku.com.hkusap.iBeacon.BeaconComparator.distanceToBeacon;
import static dupd.hku.com.hkusap.manager.RangingManager.RaningMode.RangingIndoorMode;

public class RangingManager {


    private static RangingManager INSTANCE;
    private final RaningMode mode;
    private List<RangingManagerProtocol> observerArray = new ArrayList<>();
    private Map<String, Collection<Beacon>> rangingCache = new HashMap<>();
    private BeaconComparator mComparator = new BeaconComparator();
    private Beacon nearestBeacon;
    private int mEnviParams;
    private DecimalFormat format = new DecimalFormat("0.00");
    public long refreshedTime;
    public int precisionLevel;
    public List<Beacon> recordbeacons= new ArrayList<>();
    public String record = "";

    private RangingManager() {
        mode = RangingIndoorMode;
    }

    public static RangingManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RangingManager();
        }
        return INSTANCE;
    }

    public boolean ibeaconAvailable() {
        return !rangingCache.isEmpty();
    }

    public void unregisterRegionWithUUID(Identifier UUIDString) {

    }


    public void updateRegionWithUUID(Region region, Collection<Beacon> beacons) {
        //hb to 65 rangingCache.put(region.getUniqueId(), beacons);
        if (!beacons.isEmpty()) {
            Logger.d(region.getUniqueId());
            Logger.d(beacons.size());
            //rangingCache.removeAll(beacons);
            rangingCache.clear();
            List<Beacon> filteredBeacons = new ArrayList<Beacon>();
            recordbeacons = new ArrayList<>();
            for(Beacon beacon : beacons) {
                if(beacon.getId2().toString().equals("7275")) {
                    filteredBeacons.add(beacon);
                    recordbeacons.add(beacon);
                }
            }
            rangingCache.put(region.getUniqueId(), filteredBeacons);//hb from 59
            for(Beacon beacon : filteredBeacons) {
                Identifier id1 = beacon.getId1();
                DataIOManager.getInstance().mSubject.onNext("Beacon UUID:" + id1.toString() + "\n");
            }
        } else {
            rangingCache.clear();
        }
    }

    public String Recordibeacons(String uuid,String dis){
        String beaconlistinfo = "";
        if(recordbeacons.size()!=0){
            if(!uuid.equals("")){
                for (Beacon beacon:recordbeacons){
                    if(beacon.getId1().toString().equals(uuid)){
                        beaconlistinfo = getBeaconinfo(beacon);
                    }
                }
            }else {
                for(int i=0;i<recordbeacons.size();i++){
                    if(i==0){
                        beaconlistinfo = getBeaconinfo(recordbeacons.get(i));
                    }else {
                        beaconlistinfo = beaconlistinfo + ","+ getBeaconinfo(recordbeacons.get(i));
                    }
                }
            }
        }
        beaconlistinfo = beaconlistinfo + "/" + dis;
        //一次扫描只用一次
        recordbeacons = new ArrayList<>();
        return beaconlistinfo;
    }

    public String getBeaconinfo(Beacon beacon){
        String info="";
        if(beacon != null){
            String uuid = beacon.getId1().toString();
            String RSSI = String.valueOf(beacon.getRssi());
            String major = beacon.getId2().toString();
            String minor = beacon.getId3().toString();
            info = uuid+"/"+major+"/"+minor+"/"+RSSI;
        }
        return info;
    }

    public void prepareNotification() {
        List<Beacon> cache = new ArrayList<>();
        for (String uuid : rangingCache.keySet()) {
            Collection<Beacon> beacons = rangingCache.get(uuid);
            if (!beacons.isEmpty()) {
                cache.addAll(beacons);
            }
        }
        if (cache.isEmpty()) return;
        //if (cache.size() < 5) return;
        DataIOManager.getInstance().mOften.onNext(new Often(1, cache.size()));

        Collections.sort(cache, mComparator);

        Beacon nearest = cache.get(0);

        for (RangingManagerProtocol observer : observerArray) {
            observer.didRangedBeacons(cache);
        }
        double distance = distanceToBeacon(nearest);

//        int rssiSum = 0;
//        for (Beacon beacon: cache) {
//            rssiSum += beacon.getRssi();
//        }
//        int aveRssi = rssiSum/cache.size();
//        for (Beacon beacon: cache) {
//            beacon.setRssi(aveRssi);
//        }
//
//        Logger.d("最近Beacon:" + nearest.getBluetoothName() + " 距离:" + distance + "\n");
        Context context = HKUApplication.sAPP.getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        float N = sharedPreferences.getFloat("Estimote_N", (float)2.0);
        String S_N=format.format(N);

        if (distance > 8) {
            String text="";
            if (distance!=Double.MAX_VALUE){
                String mpower = format.format(DataIOManager.getInstance().sapdb.plate.get(0).mPower);
                String rssi = format.format(nearest.getRssi());

                String function = "Math.pow(10,"+ mpower+"-"+ rssi+"/(10*"+S_N+"))";
                text="最近Beacon距离:" + format.format(distance)
                        +"最近Beacon rssi: "+rssi
                        +"当前公式: "+function
                        +"最近Beacon:" + nearest.toString()
                        + "大于8,移除\n";
            }
            else{
                text="最近Beacon距离:" + format.format(distance)
                        +"找不到对应plate，最近Beacon:" + nearest.toString()
                        + "大于8,移除\n";
            }
            DataIOManager.getInstance().mSubject.onNext(text);
            nearestBeacon = null;
            return;
        }

        if (nearestBeacon == null || !nearestBeacon.getId1().toUuid().equals(nearest.getId1().toUuid())) {
            distance = distanceToBeacon(nearest);
            String text="";
            if (distance!=Double.MAX_VALUE){
                String mpower = format.format(DataIOManager.getInstance().sapdb.plate.get(0).mPower);
                String rssi = format.format(nearest.getRssi());
                //String function = "0.42093*Math.pow(" + rssi + "/" + mpower + ",6.9476)+0.54992";
                String function = "Math.pow(10,("+ mpower+"-"+ rssi+")/(10*"+S_N+"))";
                text="最近Beacon距离:" + format.format(distance)
                        +"最近Beacon rssi: "+rssi
                        +"当前公式: "+function
                        +"最近Beacon:" + nearest.toString();
            }
            else{
                text="最近Beacon距离:" + String.valueOf(distance)
                        +"找不到对应plate，最近Beacon:" + nearest.toString();
            }
            /*if (distance > 8) {
                DataIOManager.getInstance().mSubject.onNext(text + String.valueOf(distance) + "大于8,移除\n");
                return;
            }*/
            nearestBeacon = nearest;

            DataIOManager.getInstance().mSubject.onNext(text + "\n");

            //DataIOManager.getInstance().mSubject.onNext("最近Beacon距离:" + format.format(distance) + "\n");
            //DataIOManager.getInstance().mSubject.onNext("最近Beacon:" + nearestBeacon.toString() + "\n");

            for (RangingManagerProtocol observer : observerArray) {
                observer.didRangedNearestBeacon(nearest);
            }
            DeadReckoningManager.getInstance().beacon_location = true;
            refreshedTime = new Date().getTime()/1000;
            precisionLevel = 0;

        } else {
        }
    }

    public void clear() {
        rangingCache.clear();
    }

    public String levelFromUUID(UUID uuid) {
        String plateID = uuid.toString();
        return plateID.substring(13, 16);
    }

    public void cleanCacheExceptLevel(String level) {

    }


    public void updateEnviParams(int enviParams) {
        mEnviParams = enviParams;
    }

    public SPPlateModel plateForBeacon(Beacon beacon) {
        Identifier identifier = beacon.getId1();
        for (SPPlateModel plate : DataIOManager.getInstance().sapdb.plate) {
            if (plate.UUID.equals(identifier.toUuid().toString().replace("-","").toUpperCase())) {
                return plate;
            }
        }
        return null;
    }

    /*
    public double distanceToBeacon(Beacon beacon) {
        return distanceToBeacon(beacon, false);
    }

    public double distanceToBeacon(Beacon beacon, boolean option) {
        BeaconDebug beaconDebug = null;
        if (option) {
            beaconDebug = new BeaconDebug(beacon);
        }
        SPPlateModel plate = plateForBeacon(beacon);
        if (beaconDebug != null) {
            beaconDebug.plate = plate;
        }
        if (plate == null) {
            return Double.MAX_VALUE;
        }
        int enviParams = mEnviParams == 0 ? 2 : mEnviParams;
        double result = Math.pow(10, ((plate.mPower - beacon.getRunningAverageRssi()) / (10 * enviParams)));
        if (beaconDebug != null) {
            DataIOManager.getInstance().mOften.onNext(new Often(5, "Math.pow(10, ((" + plate.mPower + " - " + format.format(beacon.getRunningAverageRssi()) + ") / (10 * " + enviParams + ")))=" + format.format(result)));
            beaconDebug.enviParams = enviParams;
            beaconDebug.result = result;
            beaconDebug.method = "Math.pow(10, ((plate.mPower - rssi) / (10 * enviParams)))";
            DataIOManager.getInstance().mBeaconDebugList.add(beaconDebug);
        }
        return result;


    }
    */

    public void addNotifyObserver(RangingManagerProtocol observer) {
        synchronized (this) {
            if (observerArray.contains(observer)) {
                return;
            }
            observerArray.add(observer);
        }
    }

    public void removeNotifyObserver(RangingManagerProtocol observer) {
        synchronized (this) {
            if (!observerArray.contains(observer)) {
                return;
            }
            observerArray.remove(observer);
        }
    }

    public enum RaningMode {
        RangingIndoorMode(0),
        RangingOutdoorMode(1);

        private int value;

        RaningMode(int value) {
            this.value = value;
        }
    }

    public interface RangingManagerProtocol {
        default void didRangedBeacons(List<Beacon> beacons) {
        }

        default void didRangedNearestBeacon(Beacon beacon) {
        }
    }
}
