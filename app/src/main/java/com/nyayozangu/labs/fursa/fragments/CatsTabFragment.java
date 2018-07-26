package com.nyayozangu.labs.fursa.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.ViewCategoryActivity;
import com.nyayozangu.labs.fursa.activities.MainActivity;
import com.nyayozangu.labs.fursa.helpers.CoMeth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.CATEGORY;

/**
 * A simple {@link Fragment} subclass.
 */
public class CatsTabFragment extends Fragment {


    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private int mLastFirstVisibleItem;
    public CatsTabFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cats_tab, container, false);


        final String[] catTitle = new String[]{

                getResources().getString(R.string.cat_business),
                getResources().getString(R.string.cat_jobs),
                getResources().getString(R.string.cat_education),
                getResources().getString(R.string.cat_art),
                getResources().getString(R.string.cat_buysell),
                getString(R.string.cat_exhibitions),
                getResources().getString(R.string.cat_places),
                getResources().getString(R.string.cat_events),
                getResources().getString(R.string.cat_services),
                getResources().getString(R.string.cat_apps),
                getResources().getString(R.string.cat_groups),
                getResources().getString(R.string.cat_queries)

        };

        //categories images Array
        int catImages[] = {

//                R.drawable.featured,
                R.drawable.business,
                R.drawable.jobs,
                R.drawable.school,
                R.drawable.art,
                R.drawable.buysell,
                R.drawable.exhibitions,
                R.drawable.places,
                R.drawable.events,
                R.drawable.services,
                R.drawable.apps,
                R.drawable.groups,
                R.drawable.discussions

        };

        //create a simple adapter
        List<HashMap<String, String>> aList = new ArrayList<>();

        for (int i = 0; i < catTitle.length; i++) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("listView_title", catTitle[i]);
            hm.put("listView_image", Integer.toString(catImages[i]));
            aList.add(hm);
        }

        String[] from = {"listView_image", "listView_title"};
        int[] to = {R.id.catGridItemImageView, R.id.catGridItemTextView};

        SimpleAdapter simpleAdapter =
                new SimpleAdapter(Objects.requireNonNull(getActivity()).getBaseContext(),
                        aList, R.layout.cat_grid_item_layout, from, to);
        final GridView catGridView = view.findViewById(R.id.catsGridView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            catGridView.setNestedScrollingEnabled(true);
        }

        View gridCell = inflater.inflate(R.layout.cat_grid_item_layout, null);
        int cellWidth = measureCellWidth(getContext(), gridCell);
        catGridView.setColumnWidth(cellWidth);
        catGridView.setAdapter(simpleAdapter);
        catGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d(TAG, "onItemClick: ");
                openCat(coMeth.getCatKey(coMeth.getCatTitle(getContext())[position]));

            }
        });

        // handle the re-selecting the categories tab on the main bottom navigation on MainActivity
        MainActivity mainActivity = (MainActivity)getActivity();
        if (mainActivity != null) {
            mainActivity.mainBottomNav.setOnNavigationItemReselectedListener(
                        new BottomNavigationView.OnNavigationItemReselectedListener() {
                            @Override
                            public void onNavigationItemReselected(@NonNull MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.bottomNavCatItem:
                                        catGridView.smoothScrollToPosition(0);
                                        break;
                                    default:
                                        Log.d(TAG, "onNavigationItemReselected: " +
                                                "at default cat fragment on reselect");
                                }
                            }
                        });
        }

        //handle hiding views on scroll
        catGridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.i("SCROLLING DOWN", "TRUE");
                    //hide views
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.i("SCROLLING UP", "TRUE");
                    //show views
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });

        return view;
    }

    private void openCat(String catKey) {
        Intent openCatIntent = new Intent(getContext(), ViewCategoryActivity.class);
        openCatIntent.putExtra(CATEGORY, catKey);
        startActivity(openCatIntent);
    }

    public int measureCellWidth(Context context, View cell) {

        // We need a fake parent
        FrameLayout buffer = new FrameLayout(context);
        android.widget.AbsListView.LayoutParams layoutParams =
                new android.widget.AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        buffer.addView(cell, layoutParams);
        cell.forceLayout();
        cell.measure(1000, 1000);
        int width = cell.getMeasuredWidth();
        buffer.removeAllViews();

        return width;
    }
}
