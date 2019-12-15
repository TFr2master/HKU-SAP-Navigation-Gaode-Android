package dupd.hku.com.hkusap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by liuwei on 2017/8/5.
 * version 1.0
 */

public class PagerAdapter extends FragmentPagerAdapter {
    private String[] mTitles;
    private List<Fragment> mFragmentList;

    public PagerAdapter(FragmentManager fm, List<Fragment> mFragments, String[] strings) {
        super(fm);
        this.mFragmentList = mFragments;
        this.mTitles = strings;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

}