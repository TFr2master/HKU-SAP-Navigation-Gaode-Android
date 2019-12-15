package dupd.hku.com.hkusap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dupd.hku.com.hkusap.base.BaseActivity;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.manager.IDPathFinderManager;
import dupd.hku.com.hkusap.manager.IDRouteCollection;
import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.model.BeaconDebugForm;
import dupd.hku.com.hkusap.utils.AssetsUtil;
import dupd.hku.com.hkusap.utils.MainUtils;
import dupd.hku.com.hkusap.utils.SpUtil;

import static android.os.Build.*;

public class DebugActivity extends BaseActivity {


    public static final String PARAMS_KEY = "PARAMS_KEY";

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.btn_send_email)
    Button btnSendEmail;
    @BindView(R.id.et_params)
    EditText etParams;
    @BindView(R.id.btn_send_params)
    Button btnSendParams;
    @BindView(R.id.showroute)
    Button showRoute;
    @BindView(R.id.removeroute)
    Button removeRoute;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, DebugActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainUtils.setStatusBar(this);
        setContentView(R.layout.activity_debug);
        ButterKnife.bind(this);

        ivBack.setOnClickListener(v -> finish());


        btnSendEmail.setOnClickListener(v -> {
            String emailAddress = etEmail.getText().toString().trim();
            if (!emailAddress.contains("@")) {
                showToast("邮件格式不合法");
                return;
            }

            sendEmail(emailAddress, "HKU DEBUG", DataIOManager.getInstance().mNormalDebug.toString());
        });
        if(MapIOManager.getInstance().showRoute)
            showRoute.setText("停止显示全部路径");
        else showRoute.setText("启用显示全部路径");
        showRoute.setOnClickListener(v -> {
            MapIOManager.getInstance().showRoute=!MapIOManager.getInstance().showRoute;
            if(MapIOManager.getInstance().showRoute)
                MapIOManager.getInstance().displayAllRoutesForLevel();
            else
                MapIOManager.getInstance().removeNavigationRoutes();
            finish();
        });
        removeRoute.setOnClickListener(v -> {
            MapIOManager.getInstance().removeNavigationRoutes();
            finish();
        });


        int enviParams = SpUtil.getIntPreferences(PARAMS_KEY);
        etParams.setText(String.valueOf(enviParams == 0 ? 2 : enviParams));
        btnSendParams.setOnClickListener(v -> {
            String params = etParams.getText().toString().trim();
            try {
                int result = Integer.valueOf(params);
                SpUtil.putIntPreferences(PARAMS_KEY, result);
                showToast("保存成功");
            } catch (Exception e) {
                e.printStackTrace();
                showToast("环境参数不合法");
            }
        });
    }


    private void sendEmail(String address, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_CC, new String[]{"yougakingwu@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(AssetsUtil.writeDebugFile()));
        intent.setType("application/octet-stream");
        intent.setType("message/rfc882");
        Intent.createChooser(intent, "Choose Email Client");
        startActivity(intent);
    }
}
