package dupd.hku.com.hkusap;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.manager.RangingManager;
import dupd.hku.com.hkusap.weight.OptionLevelView;

public class BeaconrecordDialog extends Dialog {
    private Context context;

    public EditText filename;
    public EditText UUID;
    public EditText dis;
    public Button start_record;
    public Button cacel_record;





    public BeaconrecordDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.record_dialog, null);
        setContentView(view);

        filename = findViewById(R.id.file_name);
        UUID = findViewById(R.id.UUID);
        dis = findViewById(R.id.distance_now);
        start_record = findViewById(R.id.start_record);
        cacel_record = findViewById(R.id.cancel_record);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        filename.setText(simpleDateFormat.format(new Date(System.currentTimeMillis())));
        dis.setText("1");
        start_record.setOnClickListener(v -> {
            MainActivity.Maa.recordingFilename = filename.getText().toString()+".txt";
            MainActivity.Maa.uuid_only = UUID.getText().toString();
            MainActivity.Maa.distance = dis.getText().toString();
            MapIOManager.getInstance().startedrecord = true;
            MainActivity.Maa.mOptionLevelView.setRecordImage();
            BeaconrecordDialog.this.dismiss();
            MainActivity.Maa.startrecord();
        });
        cacel_record.setOnClickListener(v -> {
            BeaconrecordDialog.this.dismiss();
            MapIOManager.getInstance().startedrecord = false;
            MainActivity.Maa.mOptionLevelView.setRecordImage();
        });

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int)(d.widthPixels * 0.8);
        dialogWindow.setAttributes(lp);
    }
}
