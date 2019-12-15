package dupd.hku.com.hkusap.iBeacon;

import android.content.Context;
import android.content.SharedPreferences;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

import java.util.Comparator;

import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.manager.RangingManager;
import dupd.hku.com.hkusap.model.SPPlateModel;

public class BeaconComparator implements Comparator<Beacon> {
    /*
    @Override
    public int compare(Beacon o1, Beacon o2) {

        double distance1 = RangingManager.getInstance().distanceToBeacon(o1);
        double distance2 = RangingManager.getInstance().distanceToBeacon(o2);

        return (int) (distance1 - distance2);
    }
    */
    @Override
    public int compare(Beacon o1, Beacon o2) {

//        double distance1 = distanceToBeacon(o1);
//        double distance2 = distanceToBeacon(o2);

        //return (int) (o1.getDistance() - o2.getDistance());
        return (int) (distanceToBeacon(o1) - distanceToBeacon(o2));
    }


    public static SPPlateModel plateForBeacon(Beacon beacon) {
        Identifier identifier = beacon.getId1();
        for (SPPlateModel plate : DataIOManager.getInstance().sapdb.plate) {
            if (plate.UUID.equals(identifier.toUuid().toString().replace("-","").toUpperCase())) {
                return plate;
            }
        }
        return null;
    }

    private static double calDisAltbeacon(int rssi, double mPower) {
        double A = 0.42093;
        double B = 6.9476;
        double C = 0.54992;

        if (rssi == 0) {
            return -1.0;
        }

        double ratio = (double)rssi/mPower;
        return A*Math.pow(ratio,B)+C;
    }

    private static double calDisEstimote(int rssi, double mPower) {
        Context context = HKUApplication.sAPP.getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        float N = sharedPreferences.getFloat("Estimote_N", (float)2.0);
        double tmp = (mPower - rssi)*1.0;
        double dis = Math.pow(10,tmp/(10.0 * N));
        return dis;
    }

    public static double distanceToBeacon(Beacon beacon) {
//        SPPlateModel plate = plateForBeacon(beacon);
//        if (plate == null) {
//            return Double.MAX_VALUE;
//        }
//        double result = Math.pow(10, ((plate.mPower - beacon.getRssi()) / (10 * 2)));

        /*
        return beacon.getDistance();
        */
        SPPlateModel plate = plateForBeacon(beacon);
        if (plate == null) {
            return Double.MAX_VALUE;
        }
        double distanceAltbeacon = calDisAltbeacon(beacon.getRssi(), plate.mPower);
        double distanceEstimote = calDisEstimote(beacon.getRssi(), plate.mPower);
        //return (distanceAltbeacon + distanceEstimote) / 2.0;
        return distanceEstimote;

    }

}
