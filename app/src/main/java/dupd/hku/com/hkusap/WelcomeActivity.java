package dupd.hku.com.hkusap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dupd.hku.com.hkusap.base.BaseActivity;
import dupd.hku.com.hkusap.utils.SpUtil;

/**
 * author: 13060393903@163.com
 * created on: 2018/03/27 17:51
 * description:
 */
public class WelcomeActivity extends BaseActivity {

    private static final String HAD_GOTO_WELCOME = "HAD_GOTO_WELCOME";
    @BindView(R.id.viewPager)
    ViewPager mViewPager;
    @BindView(R.id.circles)
    LinearLayout mCircles;
    private int[] mImages = new int[]{
            R.drawable.guide_1,
            R.drawable.guide_2,
            R.drawable.guide_3,
    };
    private WelFragmentAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);

        BarUtils.setStatusBarAlpha(this, 0);

        boolean hadGotoWelcome = SpUtil.getBooleanPreferences(HAD_GOTO_WELCOME);
        if (hadGotoWelcome) {
            gotoMain();
            return;
        }
        mAdapter = new WelFragmentAdapter(getSupportFragmentManager(), mImages);
        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == mImages.length - 1) {
                    mViewPager.postDelayed(() -> gotoMain(), 1_000);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (ContextCompat.checkSelfPermission(WelcomeActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WelcomeActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
        if (ContextCompat.checkSelfPermission(WelcomeActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WelcomeActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
        }
    }

    private void gotoMain() {
        SpUtil.putBooleanPreferences(HAD_GOTO_WELCOME, true);
        ActivityUtils.startActivity(MainActivity.class);
        finish();
    }

    public class WelFragmentAdapter extends FragmentPagerAdapter {

        private int[] images;

        public WelFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        public WelFragmentAdapter(FragmentManager fm,
                                  int[] images) {
            this(fm);
            this.images = images;

        }

        @Override
        public Fragment getItem(int position) {
            ImageViewFragment fragment = new ImageViewFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("resId", images[position]);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return this.images.length;
        }
    }

    public static class ImageViewFragment extends Fragment {
        @BindView(R.id.imageView)
        ImageView mImageView;
        Unbinder unbinder;
        private int imageId;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            imageId = getArguments().getInt("resId");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
            unbinder = ButterKnife.bind(this, rootView);
            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mImageView.setImageResource(imageId);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            unbinder.unbind();
        }
    }
}
