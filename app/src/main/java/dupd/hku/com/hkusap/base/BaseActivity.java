package dupd.hku.com.hkusap.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import dupd.hku.com.hkusap.R;

/**
 * author: 13060393903@163.com
 * created on: 2018/09/20 18:14
 * description:
 */
public class BaseActivity extends AppCompatActivity {

    protected Activity mActivity;
    private ProgressDialog mLoadDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
    }


    public void showLoadDialog() {
        showLoadDialog(getResources().getString(R.string.loading));
    }

    public void showLoadDialog(String message) {
        showLoadDialog(message, false);
    }

    public void showLoadDialog(String message, boolean cancel) {
        if (mLoadDialog == null) {
            mLoadDialog = ProgressDialog.show(mActivity, "", message);
            mLoadDialog.setCancelable(cancel);
        } else if (!mLoadDialog.isShowing()) {
            mLoadDialog.show();
            mLoadDialog.setCancelable(cancel);
        }
        mLoadDialog.setMessage(message);
    }

    public void dismissDialog() {
        if (mLoadDialog != null && mLoadDialog.isShowing()) {
            mLoadDialog.dismiss();
        }
    }

    public void onChangeTheme() {
    }

    public void showToast(int message) {
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
    }

    public void showToast(String message) {
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
    }
}
