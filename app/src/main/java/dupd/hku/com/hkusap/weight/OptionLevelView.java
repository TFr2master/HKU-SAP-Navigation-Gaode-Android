package dupd.hku.com.hkusap.weight;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dupd.hku.com.hkusap.ParameterDialog;
import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.base.BaseAdapter;
import dupd.hku.com.hkusap.base.BaseViewHolder;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.manager.DeadReckoningManager;
import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.manager.RangingManager;
import dupd.hku.com.hkusap.manager.ShakeDetector;
import dupd.hku.com.hkusap.model.IDMatchingModel;
import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.model.SPSelectedIndex;
import dupd.hku.com.hkusap.utils.UIColor;

public class OptionLevelView extends FrameLayout implements BottomSheetCallback {

    private TextView mTvFloor;
    private RecyclerView mRvFloor;
    private CardView mCvFloor;
    private TextView mTvParameter;
    private CardView mCvParameter;
    private TextView mTvMail;
    private CardView mCvMail;
    private CardView mCvLocation;
    private LevelAdapter mAdapter;
    private int selectedIndex;
    private List<SPSelectedIndex> indicatorIndexs;
    private String hightlightLevel;
    private boolean mTvFloorVisible=false;
    ShakeDetector sensorHelper;

    public OptionLevelView(@NonNull Context context) {
        this(context, null);
    }

    public OptionLevelView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OptionLevelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.option_level, this, true);

        mTvFloor = findViewById(R.id.tv_floor);
        mRvFloor = findViewById(R.id.rv_floor);
        mCvFloor = findViewById(R.id.cv_floor);
        mTvParameter = findViewById(R.id.tv_parameter);
        mCvParameter = findViewById(R.id.cv_parameter);
        mTvMail = findViewById(R.id.tv_mail);
        mCvMail = findViewById(R.id.cv_mail);
        mCvLocation = findViewById(R.id.cv_location);

        mAdapter = new LevelAdapter();
        mRvFloor.setAdapter(mAdapter);
        mRvFloor.setLayoutManager(new LinearLayoutManager(context));
        mCvLocation.setOnClickListener(v -> MapIOManager.getInstance().centerToMyLocation());

        mTvFloor.setVisibility(View.VISIBLE);

        mTvFloor.setOnClickListener(v -> {
            if (mCvFloor.isShown()) {
                mCvFloor.setVisibility(View.GONE);
            } else {
                mCvFloor.setVisibility(View.VISIBLE);
                displayLevelBarWithIndicatorIndexes(MapIOManager.getInstance().shortestPathLevelIndicators());
            }
        });
        mTvParameter.setOnClickListener(v -> {
            final ParameterDialog parameterDialog = new ParameterDialog(context);
            parameterDialog.show();
        });
        mTvMail.setOnClickListener(v -> {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("brand", Build.BRAND);
                jsonObject.put("model", Build.MODEL);
                jsonObject.put("Version", Build.VERSION.RELEASE);
                SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                float N = sharedPreferences.getFloat("Estimote_N", (float)2.0);
                jsonObject.put("ParameterN", N);
                String jsonString = jsonObject.toString();
                Intent data = new Intent(Intent.ACTION_SENDTO);
                data.setData(Uri.parse("mailto:williamc.wh.chn@gmail.com"));
                data.putExtra(Intent.EXTRA_SUBJECT, "Parameter");
                data.putExtra(Intent.EXTRA_TEXT, jsonString);
                context.startActivity(data);
            } catch (Exception e) {

            }
        });
        sensorHelper = new ShakeDetector(context);
        sensorHelper.registerOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                mTvParameter.callOnClick();
            }
        });
        sensorHelper.start();
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        mAdapter.notifyDataSetChanged();
    }

    public void setIndicatorIndexs(List<SPSelectedIndex> indicatorIndexs) {
        this.indicatorIndexs = indicatorIndexs;
        mAdapter.notifyDataSetChanged();
    }

    public void setHightlightLevel(String hightlightLevel) {
        this.hightlightLevel = hightlightLevel;
        mAdapter.notifyDataSetChanged();
    }

    public void hideLevelBar() {
        if (mCvFloor.isShown()) {
            mCvFloor.setVisibility(GONE);
        }
    }

    public void updateLevelButtonWithLevel(String level) {
        int color = level.equals(MapIOManager.getInstance().level) ? UIColor.systemBlueColor() : UIColor.blackColor();
        mTvFloor.setTextColor(color);
        mTvFloor.setText(DataIOManager.getInstance().abbreviationForLevelCode(level));
    }
    /*
    public void didCalculatedDeadReckoningModel(IDMatchingModel model, IDRouteModel route) {
        if(mTvFloorVisible==false&&DeadReckoningManager.getInstance().checkDeadReckoning()) {
            mTvFloor.setVisibility(View.VISIBLE);
            mTvFloorVisible=true;
        }
    }
    */

    public void displayLevelBarWithIndicatorIndexes(List<SPSelectedIndex> indexes) {
        List<String> levels = MapIOManager.getInstance().registeredLevels();
        selectedIndex = levels.indexOf(MapIOManager.getInstance().previewLevel);
        if (selectedIndex < 0) {
            selectedIndex = -1;
        }
        hightlightLevel = MapIOManager.getInstance().level;
        indicatorIndexs = indexes;
        mAdapter.refresh(levels);
    }

    private void translationYAnimator(float translationY, long duration) {
        ObjectAnimator.ofFloat(this, "translationY", translationY)
                .setDuration(duration)
                .start();
    }

    @Override
    public void onSlideAnimator(float offset, long duration) {
        float translationY = getTranslationY();
        translationYAnimator(translationY + offset, duration);
    }

    @Override
    public void onSlide(float offset) {
        float translationY = getTranslationY();
        setTranslationY(translationY + offset);
    }

    public void setTitle() {
        mTvFloor.setText("Lift");
    }

    public class LevelAdapter extends BaseAdapter<String> {


        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_floor, parent, false);
            return new ViewHolder(view);
        }

        public class ViewHolder extends BaseViewHolder<String> {
            private final Resources mResources;
            @BindView(R.id.tv_name)
            TextView mTvName;
            @BindView(R.id.point)
            View indicatorView;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                mResources = itemView.getContext().getResources();
            }

            @Override
            public void bindPosition(final int position, final String level) {
                mTvName.setText(DataIOManager.getInstance().abbreviationForLevelCode(level));

                boolean isSelected = position == selectedIndex;
                boolean highlighted = level.equals(hightlightLevel);

                mTvName.setTextColor(mResources.getColor(isSelected ? R.color.white : R.color.text_color));

                if (indicatorIndexs != null && !indicatorIndexs.isEmpty()) {
                    SPSelectedIndex indexIndicator = null;
                    for (SPSelectedIndex selectedIndex : indicatorIndexs) {
                        if (selectedIndex.indicatorIndex == position) {
                            indexIndicator = selectedIndex;
                        }
                    }
                    boolean shouldShowIndicator = indexIndicator != null;

                    itemView.setBackgroundColor(isSelected ? (shouldShowIndicator ? UIColor.colorForIndicatorType(indexIndicator.type) : UIColor.systemBlueColor()) : UIColor.whiteColor());
                    indicatorView.setBackgroundResource(shouldShowIndicator ? (isSelected ? R.drawable.point_white : UIColor.pointForIndicatorType(indexIndicator.type)) : 0);
                    mTvName.setTextColor(isSelected ? UIColor.whiteColor() : (highlighted ? UIColor.systemBlueColor() : UIColor.blackColor()));
                } else {
                    itemView.setBackgroundColor(isSelected ? UIColor.systemBlueColor() : UIColor.whiteColor());
                    indicatorView.setBackgroundColor(UIColor.clearColor());
                    mTvName.setTextColor(isSelected ? UIColor.whiteColor() : (highlighted ? UIColor.systemBlueColor() : UIColor.blackColor()));
                }
                itemView.setOnClickListener(v -> {
                    MapIOManager.getInstance().setPreviewLevel(level);
                });
            }
        }
    }
}
