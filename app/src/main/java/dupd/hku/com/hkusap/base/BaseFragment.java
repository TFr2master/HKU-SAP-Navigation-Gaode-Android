package dupd.hku.com.hkusap.base;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import dupd.hku.com.hkusap.R;

/**
 * author: 13060393903@163.com
 * created on: 2018/09/20 11:11
 * description:
 */
public class BaseFragment extends Fragment {

    private ProgressDialog mLoadDialog;


    public void showLoadDialog() {
        showLoadDialog(getResources().getString(R.string.loading));
    }

    public void showLoadDialog(String message) {
        if (mLoadDialog == null) {
            mLoadDialog = ProgressDialog.show(getActivity(), "", message);
            mLoadDialog.setCancelable(true);
        } else if (!mLoadDialog.isShowing()) {
            mLoadDialog.show();
            mLoadDialog.setCancelable(true);
        }
    }

    public void showLoadDialog(String message, boolean cancel) {
        if (mLoadDialog == null) {
            mLoadDialog = ProgressDialog.show(getActivity(), "", message);
            mLoadDialog.setCancelable(cancel);
        } else if (!mLoadDialog.isShowing()) {
            mLoadDialog.show();
            mLoadDialog.setCancelable(cancel);
        }
    }

    public void dismissDialog() {
        if (mLoadDialog != null && mLoadDialog.isShowing()) {
            mLoadDialog.dismiss();
        }
    }

    public void onChangeTheme() {

    }

    protected void showToast(int message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    protected void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
