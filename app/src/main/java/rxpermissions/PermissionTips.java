package rxpermissions;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import dupd.hku.com.hkusap.R;
import io.reactivex.annotations.NonNull;

/**
 * author: 13060393903@163.com
 * created on: 2018/09/20 18:11
 * description:
 */
public class PermissionTips {

    private static final String TAG = "PermissionTipsDialog";

    public static void showPermissionTipsDialog(@NonNull Activity activity, @NonNull String permission) {
        FragmentManager manager = activity.getFragmentManager();
        Fragment fragment = manager.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = PermissionTipsDialog.newInstance(permission);
        }
        if (!fragment.isAdded()) {
            ((PermissionTipsDialog) fragment).show(manager, TAG);
        } else {
            Log.e(TAG, "fragment is already Added");
        }
    }


    public static class PermissionTipsDialog extends DialogFragment {

        public static PermissionTipsDialog newInstance(@NonNull String permission) {
            PermissionTipsDialog dialog = new PermissionTipsDialog();
            Bundle args = new Bundle();
            args.putString("permission", permission);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String permission = getArguments().getString("permission");
            StringBuilder builder = new StringBuilder();
            builder.append("在设置-应用-")
                    .append(getString(R.string.app_name))
                    .append("-权限中开启")
                    .append(describePermission(permission, getString(R.string.app_name)));
            setCancelable(false);
            return new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
                    .setTitle("权限申请")
                    .setMessage(builder.toString())
                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create();
        }
    }


    private static String describePermission(@NonNull String permission, @NonNull String appName) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return String.format("位置信息权限,以正常使用%s功能", appName);
            default:
                return String.format("应用所需权限,以正常使用%s功能", appName);
        }
    }
}
