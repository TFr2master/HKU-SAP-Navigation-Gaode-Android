package dupd.hku.com.hkusap.model;

import android.os.Build;

import org.altbeacon.beacon.Beacon;

import java.util.List;

import dupd.hku.com.hkusap.utils.SpUtil;

import static android.os.Build.MANUFACTURER;
import static android.os.Build.MODEL;
import static dupd.hku.com.hkusap.DebugActivity.PARAMS_KEY;

public class BeaconDebugForm {

    public int enviParams;
    public String phoneManufacturer;
    public String phoneModel;
    public String systemVersion;
    public List<BeaconDebug> mBeaconDebugList;

    public BeaconDebugForm() {
        // 手机厂商
        phoneManufacturer = MANUFACTURER;
        // 手机型号
        phoneModel = MODEL;
        // 系统版本
        systemVersion = Build.VERSION.RELEASE;
        enviParams = SpUtil.getIntPreferences(PARAMS_KEY);
    }

    public static class BeaconDebug {
        public Beacon beacon;
        public SPPlateModel plate;
        public double result;
        public String method;
        public int enviParams;

        public BeaconDebug(Beacon beacon) {
            this.beacon = beacon;
        }
    }
}
