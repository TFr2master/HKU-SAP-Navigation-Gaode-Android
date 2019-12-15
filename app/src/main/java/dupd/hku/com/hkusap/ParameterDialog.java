package dupd.hku.com.hkusap;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.manager.MapIOManager;

public class ParameterDialog extends Dialog {

    private Context context;
    private EditText etEstimoteN;
    private TextView tvOK;

    private EditText mPowerText;
    private Button useCheck;
    public ParameterDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.para_dialog, null);
        setContentView(view);

        etEstimoteN = view.findViewById(R.id.estimote_n);
        tvOK = view.findViewById(R.id.para_dialog_ok);
        tvOK.setOnClickListener(new ClickListener());
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        float N = sharedPreferences.getFloat("Estimote_N", (float)2.0);
        etEstimoteN.setText(Float.toString(N));

        useCheck=view.findViewById(R.id.usecheck);
        if(MapIOManager.useCheck==true)
            useCheck.setText("停用区域检测");
        else useCheck.setText("启用区域检测");
        useCheck.setOnClickListener(v -> {
            if (MapIOManager.useCheck==true){
                MapIOManager.useCheck=false;
                //useCheck.setText("启用区域检测");
                MapIOManager.getInstance().setmLocation();
                ParameterDialog.this.dismiss();
            }
            else{
                MapIOManager.useCheck=true;
                //useCheck.setText("停用区域检测");
                ParameterDialog.this.dismiss();
            }
        });

        mPowerText=view.findViewById(R.id.mPower_params);
        String mpowertxt="0.0";
        try {
            mpowertxt=String.valueOf(DataIOManager.getInstance().sapdb.plate.get(0).mPower);
        } catch (Exception e) {
        }
        mPowerText.setText(mpowertxt);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int)(d.widthPixels * 0.8);
        dialogWindow.setAttributes(lp);
    }

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.para_dialog_ok:
                    String paraEstimoteN = etEstimoteN.getText().toString();
                    if (!paraEstimoteN.equals("")) {
                        float estimoteN = Float.valueOf(paraEstimoteN);
                        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putFloat("Estimote_N", estimoteN);
                        editor.commit();

                        double mpower = Double.valueOf(mPowerText.getText().toString());
                        for(int i=0;i<DataIOManager.getInstance().sapdb.plate.size();i++){
                            DataIOManager.getInstance().sapdb.plate.get(i).mPower=mpower;
                        }
                        ParameterDialog.this.dismiss();
                    }
                    break;
            }
        }
    }
}
