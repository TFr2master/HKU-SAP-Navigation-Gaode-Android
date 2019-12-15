package dupd.hku.com.hkusap.window;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dupd.hku.com.hkusap.EventAdapter;
import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.utils.ApiUtil;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class EventFragment extends BottomSheetDialogFragment {

    public static final String TAG = "EventFragment";
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.iv_down)
    ImageView ivDown;
    @BindView(R.id.rv_events)
    RecyclerView rvEvents;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    private Unbinder unbinder;
    private EventAdapter mEventAdapter;

    public static EventFragment newInstance(SPPlateModel plate) {
        EventFragment fragment = new EventFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(SPPlateModel.class.getName(), plate);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT; //可以写入自己想要的高度
        }
        final View view = getView();
        view.post(() -> {
            View parent = (View) view.getParent();
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) (parent).getLayoutParams();
            CoordinatorLayout.Behavior behavior = params.getBehavior();
            BottomSheetBehavior bottomSheetBehavior = (BottomSheetBehavior) behavior;
            bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());
            parent.setBackgroundColor(Color.WHITE);
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        mEventAdapter = new EventAdapter();
        rvEvents.setAdapter(mEventAdapter);

        SPPlateModel plate = getArguments().getParcelable(SPPlateModel.class.getName());

        tvName.setText(plate.name);
        progressBar.setVisibility(VISIBLE);
        tvEmpty.setVisibility(GONE);
        ApiUtil.getInstance().querytimetable(plate, eventList -> {
            progressBar.setVisibility(GONE);
            mEventAdapter.refresh(eventList);
            tvEmpty.setVisibility(mEventAdapter.getItemCount() == 0 ? VISIBLE : GONE);
        });

        ivDown.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
