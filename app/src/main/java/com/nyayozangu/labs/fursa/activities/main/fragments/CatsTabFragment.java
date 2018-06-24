package com.nyayozangu.labs.fursa.activities.main.fragments;


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
import com.nyayozangu.labs.fursa.activities.categories.ViewCategoryActivity;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cats_tab, container, false);


        final String[] catTitle = new String[]{

//                getResources().getString(R.string.cat_featured),
                getResources().getString(R.string.cat_popular), getString(R.string.cat_exhibitions),
                getResources().getString(R.string.cat_business), getResources().getString(R.string.cat_art),
                getResources().getString(R.string.cat_jobs), getResources().getString(R.string.cat_buysell),
                getResources().getString(R.string.cat_upcoming), getResources().getString(R.string.cat_events),
                getResources().getString(R.string.cat_places), getResources().getString(R.string.cat_services),
                getResources().getString(R.string.cat_education), getResources().getString(R.string.cat_queries),
                getResources().getString(R.string.cat_apps), getResources().getString(R.string.cat_groups)

        };

        //categories images Array
        int catImages[] = {

//                R.drawable.featured,
                R.drawable.hot, R.drawable.exhibitions,
                R.drawable.business, R.drawable.art,
                R.drawable.jobs, R.drawable.buysell,
                R.drawable.upcoming, R.drawable.events,
                R.drawable.places, R.drawable.services,
                R.drawable.school, R.drawable.discussions,
                R.drawable.apps, R.drawable.groups

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
                new SimpleAdapter(getActivity().getBaseContext(),
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
                openCat(coMeth.getCatKey(coMeth.catTitle[position]));

            }
        });


        // handle the re-selecting the categories tab on the main bottom navigation on MainActivity
        try {
            ((MainActivity) getActivity()).mainBottomNav.setOnNavigationItemReselectedListener(
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
        } catch (NullPointerException nullE) {
            Log.d(TAG, "onCreateView: null on reselect cats tab\n" + nullE.getMessage());
        }

        //handle hiding views on scroll
        catGridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int mInitialScroll = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                int scrolledOffset = catGridView.getVerticalScrollOffset();
//                if (scrolledOffset!=mInitialScroll) {
//                    //if scroll position changed
//                    boolean scrollUp = (scrolledOffset - mInitialScroll) < 0;
//
//                    hideViews(scrollUp);
//
//                    mInitialScroll = scrolledOffset;
//                }
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

    /**
     * open the view category page
     *
     * @param catKey the category key to be passed to the view category page
     */
    private void openCat(String catKey) {
        Intent openCatIntent = new Intent(getContext(), ViewCategoryActivity.class);
        openCatIntent.putExtra("category", catKey);
        startActivity(openCatIntent);
    }

    /**
     * calculate the width of the device to determine the number of rows
     *
     * @param context the context containing the view
     * @param cell    the view of the grid view
     * @return the number of rows to display
     */
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

//    public class ScrollDetectingGridView extends GridView {
//        public ScrollDetectingGridView(Context context) {
//            super(context);
//        }
//
//        public ScrollDetectingGridView(Context context, AttributeSet attrs) {
//            super(context,attrs);
//        }
//
//        public ScrollDetectingGridView(Context context, AttributeSet attrs, int defStyle) {
//            super(context, attrs, defStyle);
//        }
//
//        //we need this protected method for scroll detection
//        public int getVerticalScrollOffset() {
//            return computeVerticalScrollOffset();
//        }
//    }

}
