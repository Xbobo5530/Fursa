package com.nyayozangu.labs.fursa.activities.main.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.categories.ViewCategoryActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    //cat texts Array

    public CategoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        final String[] catTitle = new String[]{

//                getResources().getString(R.string.cat_featured),
                getResources().getString(R.string.cat_popular), getResources().getString(R.string.cat_jobs),
                getResources().getString(R.string.cat_buysell), getResources().getString(R.string.cat_business),
                getResources().getString(R.string.cat_upcoming), getResources().getString(R.string.cat_events),
                getResources().getString(R.string.cat_places), getResources().getString(R.string.cat_services),
                getResources().getString(R.string.cat_education), getString(R.string.cat_qna_text)

        };

        //categories images Array
        int catImages[] = {

//                R.drawable.featured,
                R.drawable.popular, R.drawable.jobs,
                R.drawable.buysell, R.drawable.business,
                R.drawable.upcoming, R.drawable.events,
                R.drawable.places, R.drawable.services,
                R.drawable.school, R.drawable.help

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
                new SimpleAdapter(getActivity().getBaseContext(), aList, R.layout.cat_grid_item_layout, from, to);
        GridView catGridView = view.findViewById(R.id.catsGridView);

        View gridCell = inflater.inflate(R.layout.cat_grid_item_layout, null);
        int cellWidth = measureCellWidth(getContext(), gridCell);
        catGridView.setColumnWidth(cellWidth);
        catGridView.setAdapter(simpleAdapter);

        catGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /*
                "Featured",
                "Popular",
                "UpComing",
                "Events",
                "Places"
                "Business",
                "Buy and sell",
                "Education",
                "Jobs",
                "Queries"*/

                Log.d(TAG, "onItemClick: ");
                openCat(coMeth.getCatKey(coMeth.catTitle[position]));

            }
        });


        return view;

    }


    private void openCat(String catKey) {
        Intent openCatIntent = new Intent(getContext(), ViewCategoryActivity.class);
        openCatIntent.putExtra("category", catKey);
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
