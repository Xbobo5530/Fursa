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

    private static final String TAG = "HomeFragment";
    public TabLayout mTabsLayout;
    ViewPager mViewPager;
    private int[] tabIcons = {
            R.drawable.ic_recent,
            R.drawable.ic_recomended_light,
            R.drawable.ic_users_light
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

        FloatingActionButton newPostFab = ((MainActivity) Objects.requireNonNull(getActivity())).getNewPostFab();
        newPostFab.setImageResource(R.drawable.ic_action_add_white);
        newPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).handleNewPostFab();
            }
        });

        return view;
    }

    private void setupViewPager(ViewPager mViewPager) {
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mAdapter.addFragment(new RecentTabFragment(), getResources().getString(R.string.recent_text));
        mAdapter.addFragment(new RecommendedTabFragment(), getResources().getString(R.string.recommended_text));
        mAdapter.addFragment(new UsersTabFragment(), getResources().getString(R.string.people_text));
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
}