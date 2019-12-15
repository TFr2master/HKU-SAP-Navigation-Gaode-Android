package dupd.hku.com.hkusap.window;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.model.SPPlateModel;

public class OptionFragment extends BottomSheetDialogFragment {

    public static final String TAG = "OptionFragment";
    @BindView(R.id.btn_events)
    Button btnEvents;
    @BindView(R.id.btn_share)
    Button btnShare;
    @BindView(R.id.btn_cancel)
    Button btnCancel;
    Unbinder unbinder;


    public static OptionFragment newInstance(SPPlateModel plate) {
        OptionFragment fragment = new OptionFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(SPPlateModel.class.getName(), plate);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_option, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SPPlateModel plate = getArguments().getParcelable(SPPlateModel.class.getName());

        btnEvents.setOnClickListener(v -> {
            dismiss();
            FragmentActivity activity = getActivity();
            EventFragment.newInstance(plate).show(activity.getSupportFragmentManager(), EventFragment.TAG);
        });
        btnShare.setOnClickListener(v -> {

            //<data android:scheme="http" android:host="baidu" android:path="/news" android:port="8080"/>
            //Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("http://baidu:8080/news?system=pc&id=45464"));

            String path = getResourcesUri(R.drawable.ic_launcher);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/jpg");
            intent.setType("text/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Navigate to " + plate.name);//添加分享内容标题
            intent.putExtra(Intent.EXTRA_TEXT, "sapsmartcampus://?sap=" + plate.UUID);
            intent.putExtra("sms_body", "sapsmartcampus://?sap=" + plate.UUID);//添加分享内容
            //创建分享的Dialog
            intent = Intent.createChooser(intent, "Navigate to" + plate.name);
            startActivity(intent);
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private String getResourcesUri(@DrawableRes int id) {
        Resources resources = getResources();
        String uriPath = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                resources.getResourcePackageName(id) + "/" +
                resources.getResourceTypeName(id) + "/" +
                resources.getResourceEntryName(id);
        return uriPath;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
