package dupd.hku.com.hkusap.manager;

import android.location.Location;

import java.util.Timer;
import java.util.TimerTask;

public class PrecisionLevelManager {
    private Timer mTimer;
    private int currentLevel = 3;
    private int seccount = 0;
    private static PrecisionLevelManager instance = new PrecisionLevelManager();

    public static PrecisionLevelManager getInstance() {
        return instance;
    }

    public PrecisionLevelManager() {
        mTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                seccount++;
                if (currentLevel < 3 && seccount >= 30) {
                    currentLevel++;
                }
            }
        };
        mTimer.schedule(timerTask, 1000, 1000);
    }

    public boolean canUpdateLocation(Location location) {
        float radius = location.getAccuracy();
        boolean retVal;
        if (radius < 10) {
            seccount = 0;
            currentLevel = 1;
            retVal = true;
        } else if (radius < 30 && currentLevel > 1) {
            seccount = 0;
            currentLevel = 2;
            retVal = true;
        } else if (currentLevel > 2) {
            retVal = true;
        } else {
            retVal = false;
        }
        return retVal;
    }

}
