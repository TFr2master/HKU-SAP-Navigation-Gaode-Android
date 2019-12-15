package dupd.hku.com.hkusap;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dupd.hku.com.hkusap.base.BaseAdapter;
import dupd.hku.com.hkusap.base.BaseFragment;
import dupd.hku.com.hkusap.base.BaseViewHolder;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.model.IEnum;
import dupd.hku.com.hkusap.model.SPBuildingModel;
import dupd.hku.com.hkusap.model.SPLevelModel;
import dupd.hku.com.hkusap.model.SPPlateModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseFragment extends BaseFragment {

    private static final String TAG = "ChooseFragment";
    @BindView(R.id.left)
    RecyclerView mLeft;
    @BindView(R.id.right)
    RecyclerView mRight;
    @BindView(R.id.tv_empty)
    TextView mTvEmpty;
    private Unbinder unbinder;
    private LevelAdapter mLevelAdapter;
    private PlateAdapter mPlateAdapter;
    private SPBuildingModel mBuilding;


    public static ChooseFragment newInstance(SPPlateModel plate, int position) {
        ChooseFragment fragment = new ChooseFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(SPPlateModel.class.getName(), plate);
        bundle.putInt("position", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SPPlateModel plate = getArguments().getParcelable(SPPlateModel.class.getName());
        int position = getArguments().getInt("position");
        mBuilding = DataIOManager.getInstance().sapdb.building.get(position);

        mLevelAdapter = new LevelAdapter(mBuilding.getLevels());
        mLeft.setLayoutManager(new LinearLayoutManager(getContext()));
        mLeft.setAdapter(mLevelAdapter);

        mPlateAdapter = new PlateAdapter();
        mRight.setLayoutManager(new LinearLayoutManager(getContext()));
        mRight.setAdapter(mPlateAdapter);

        if (plate != null) {
            onLevelItemClick(plate.levelCode);
            mTvEmpty.setVisibility(View.GONE);
            mRight.setVisibility(View.VISIBLE);
        } else {
            mTvEmpty.setVisibility(View.VISIBLE);
            mRight.setVisibility(View.GONE);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public int indexOfLevel(String levelCode) {
        for (int i = 0; i < mLevelAdapter.getTList().size(); i++) {
            if (mLevelAdapter.getTList().get(i).levelCode.equals(levelCode)) {
                return i;
            }
        }
        return -1;
    }

    private void onLevelItemClick(String levelCode) {
        mLevelAdapter.notifyItemFloor(indexOfLevel(levelCode));
        mPlateAdapter.refresh(mBuilding.getPlates(levelCode));
        mTvEmpty.setVisibility(View.GONE);
        mRight.setVisibility(View.VISIBLE);
    }

    public class PlateAdapter extends BaseAdapter<SPPlateModel> {
        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choose_room, parent, false);
            return new ViewHolder(view);
        }

        public class ViewHolder extends BaseViewHolder<SPPlateModel> implements IEnum {
            @BindView(R.id.tv_name)
            TextView mTvName;
            Resources mResources;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                mResources = itemView.getContext().getResources();
            }

            @Override
            public void bindPosition(final int position, final SPPlateModel plate) {
                mTvName.setText(plate.name);
                mTvName.setCompoundDrawablesWithIntrinsicBounds(imageForType(plate.type), 0, 0, 0);
                itemView.setOnClickListener(v -> {
                    MapIOManager.getInstance().setLevel(plate.levelCode);
                    Intent intent = new Intent();
                    intent.putExtra(SPPlateModel.class.getName(), plate);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                });
            }
        }
    }

    public class LevelAdapter extends BaseAdapter<SPLevelModel> {

        private int mSelectedPosition = -99;

        public LevelAdapter(List<SPLevelModel> floors) {
            super(floors);
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choose_level, parent, false);
            return new ViewHolder(view);
        }

        public void notifyItemFloor(int position) {
            mSelectedPosition = position;
            notifyDataSetChanged();
        }

        public class ViewHolder extends BaseViewHolder<SPLevelModel> {
            @BindView(R.id.tv_name)
            TextView mTvName;
            Resources mResources;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                mResources = itemView.getContext().getResources();
            }

            @Override
            public void bindPosition(final int position, final SPLevelModel level) {
                mTvName.setText(level.abbreviation);
                mTvName.setBackgroundColor(mResources.getColor(mSelectedPosition == position ? R.color.white : R.color.background));
                itemView.setOnClickListener(v -> onLevelItemClick(level.levelCode));
            }
        }
    }

}
