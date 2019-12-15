package dupd.hku.com.hkusap.manager;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.orhanobut.logger.Logger;

import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.model.Often;

public class PedometerManager {
    private static final String TAG = "PedometerManager";
    private final PedometerManagerDelegate delegate;
    private SensorManager mSensorManager;

    public PedometerManager(PedometerManagerDelegate delegate) {
        this.delegate = delegate;
    }

    public void startPedometerUpdates() {
        mSensorManager = (SensorManager) HKUApplication.sAPP.getSystemService(Context.SENSOR_SERVICE);

        boolean pedometerAvailable = registerSensor();
        DataIOManager.getInstance().mSubject.onNext("运动传感器:" + (pedometerAvailable ? "可用" : "不可用") + "\n");
    }

    public void stopPedometerUpdates() {
        mSensorManager.unregisterListener(mEventListener);
    }

    public boolean registerSensor() {
        if (mSensorManager == null) {
            return false;
        }
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor detectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        boolean countAvailable = mSensorManager.registerListener(mEventListener, countSensor, SensorManager.SENSOR_DELAY_GAME);
        boolean detectorAvailable = mSensorManager.registerListener(mEventListener, detectorSensor, SensorManager.SENSOR_DELAY_GAME);
        if (countAvailable && detectorAvailable) {
            return true;
        } else {
            return false;
        }
    }

    private SensorEventListener mEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_STEP_COUNTER:
                    DataIOManager.getInstance().mOften.onNext(new Often(2, event.values[0]));
                    //只回掉一次 算一步
                    delegate.pedometerManagerDidRecognizedStep(Sensor.TYPE_STEP_COUNTER, event.values[0]);
                    break;
                case Sensor.TYPE_STEP_DETECTOR:
                    delegate.pedometerManagerDidRecognizedStep(Sensor.TYPE_STEP_DETECTOR, event.values[0]);
                    DataIOManager.getInstance().mOften.onNext(new Often(3, event.values[0]));
                    break;
            }
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public interface PedometerManagerDelegate {
        void pedometerManagerDidRecognizedStep(int type, float step);
    }
}
