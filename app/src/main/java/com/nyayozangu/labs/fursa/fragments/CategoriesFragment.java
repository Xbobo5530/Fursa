package com.nyayozangu.labs.fursa.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.MainActivity;
import com.nyayozangu.labs.fursa.adapters.ViewPagerAdapter;
import com.nyayozangu.labs.fursa.helpers.CoMeth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment {

    public TabLayout catsTabsLayout;
    private int[] tabIcons = {
            R.drawable.ic_categories_white,
            R.drawable.ic_action_tags_white
    };

    public CategoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        ViewPager catsViewPager = view.findViewById(R.id.catsViewPager);
        setupViewPager(catsViewPager);
        catsTabsLayout = view.findViewById(R.id.catsTabsLayout);
        catsTabsLayout.setupWithViewPager(catsViewPager);
        setupTabIcons();
        final MainActivity mainActivity = (MainActivity)getActivity();
        if (mainActivity != null) {
            FloatingActionButton newPostFab = mainActivity.getNewPostFab();
            newPostFab.setImageResource(R.drawable.ic_action_add_white);
            newPostFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainActivity.handleNewPostFab();
                }
            });
        }

        return view;
    }

    private void setupViewPager(ViewPager catsViewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new CatsTabFragment(), getResources().getString(R.string.categories_text));
        adapter.addFragment(new TagsTabFragment(), getResources().getString(R.string.trending_tags_text));
        catsViewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        try {
            catsTabsLayout.getTabAt(0).setIcon(tabIcons[0]);
            catsTabsLayout.getTabAt(1).setIcon(tabIcons[1]);
        } catch (NullPointerException e) {
            Log.d(CoMeth.TAG, "setupTabIcons: failed to set tab icons\n" + e.getMessage());
        }
    }
}
