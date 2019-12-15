package dupd.hku.com.hkusap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dupd.hku.com.hkusap.base.BaseActivity;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.model.SPBuildingModel;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.utils.MainUtils;

public class ChooseDestinationActivity extends BaseActivity {


    @BindView(R.id.iv_back)
    ImageView mIvBack;
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;
    private String[] mTitles;
    private PagerAdapter mAdapter;

    public static Intent newIntent(Context context, SPPlateModel plate) {
        Intent intent = new Intent(context, ChooseDestinationActivity.class);
        intent.putExtra(SPPlateModel.class.getName(), plate);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainUtils.setStatusBar(this);
        setContentView(R.layout.activity_choose_destination);
        ButterKnife.bind(this);

        SPPlateModel plate = getIntent().getParcelableExtra(SPPlateModel.class.getName());

        mIvBack.setOnClickListener(view -> finish());
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);


        int position = 0;
        List<Fragment> fragments = new ArrayList<>();
        List<SPBuildingModel> buildingList = DataIOManager.getInstance().sapdb.building;
        mTitles = new String[buildingList.size()];
        for (int i = 0; i < buildingList.size(); i++) {
            SPBuildingModel b = buildingList.get(i);
            if ((plate != null && plate.postalCode != null) && plate.postalCode.equals(b.postalCode)) {
                position = i;
                fragments.add(ChooseFragment.newInstance(plate, i));
            } else {
                fragments.add(ChooseFragment.newInstance(null, i));
            }
            mTitles[i] = b.name;
        }
        mAdapter = new PagerAdapter(getSupportFragmentManager(), fragments, mTitles);
        mViewPager.setAdapter(mAdapter);
        for (int i = 0; i < mTitles.length; i++) {
            TabLayout.Tab tab = mTabLayout.newTab().setCustomView(createTabView(i));
            mTabLayout.addTab(tab);
        }
        mViewPager.setOffscreenPageLimit(mTitles.length);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mViewPager.setCurrentItem(position, false);
                mTabLayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(position, false);
    }


    public View createTabView(int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_tab, null);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(mTitles[position]);
        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.img_selector);
        return view;
    }

}
