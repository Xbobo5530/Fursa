package com.nyayozangu.labs.fursa.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.MainActivity;
import com.nyayozangu.labs.fursa.adapters.ViewPagerAdapter;
import com.nyayozangu.labs.fursa.helpers.CoMeth;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "sean";
    public TabLayout mTabsLayout;
    ViewPager mViewPager;
    private int[] tabIcons = {
            R.drawable.ic_recent,
            R.drawable.ic_recomended_light,
            R.drawable.ic_saved
    };

    public HomeFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mViewPager = view.findViewById(R.id.homeViewPager);
        setupViewPager(mViewPager);
        mTabsLayout = view.findViewById(R.id.homeTabsLayout);
        mTabsLayout.setupWithViewPager(mViewPager);
        setupTabIcons();

        FloatingActionButton newPostFab = ((MainActivity)
                Objects.requireNonNull(getActivity())).getNewPostFab();
        newPostFab.setImageResource(R.drawable.ic_action_add_white);
        newPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).handleNewPostFab();
            }
        });

        Bundle args = getArguments();
        int tab;
        if (args != null) {
            tab = args.getInt("tab");
            Log.d(TAG, "onCreateView: tab is " + tab);
            selectPage(2);
        }

        return view;
    }

    private void setupViewPager(ViewPager mViewPager) {
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mAdapter.addFragment(new RecentTabFragment(), getResources().getString(R.string.recent_text));
        mAdapter.addFragment(new RecommendedTabFragment(), getResources().getString(R.string.recommended_text));
        CoMeth coMeth = new CoMeth();
        if (coMeth.isLoggedIn()) {
            mAdapter.addFragment(new SavedTabFragment(), getResources().getString(R.string.saved_text));
        }else{
            mAdapter.addFragment(new AlertFragment(), getResources().getString(R.string.saved_text));
        }
        mViewPager.setAdapter(mAdapter);
    }

    private void setupTabIcons() {
        try {
            Objects.requireNonNull(mTabsLayout.getTabAt(0)).setIcon(tabIcons[0]);
            Objects.requireNonNull(mTabsLayout.getTabAt(1)).setIcon(tabIcons[1]);
            Objects.requireNonNull(mTabsLayout.getTabAt(2)).setIcon(tabIcons[2]);
        } catch (NullPointerException setIconNull) {
            Log.d(CoMeth.TAG, "setupTabIcons: failed to set tab icons\n" + setIconNull.getMessage());
        }
    }
    public void selectPage(int pageIndex){
        try {
            mTabsLayout.setScrollPosition(pageIndex, 0f, true);
            mViewPager.setCurrentItem(pageIndex);
        }catch (NullPointerException tabIsNull){
            Log.d(CoMeth.TAG, "selectPage: tab is null\n" + tabIsNull.getMessage());
        }
    }
}