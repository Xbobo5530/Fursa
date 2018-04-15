package com.nyayozangu.labs.fursa;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "Sean";
    //cat texts Array

    public CategoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categories, container, false);


        String[] catTitle = new String[]{

                getResources().getString(R.string.cat_featured),
                getResources().getString(R.string.cat_popular),
                getResources().getString(R.string.cat_upcoming),
                getResources().getString(R.string.cat_events),
                getResources().getString(R.string.cat_business),
                getResources().getString(R.string.cat_buysell),
                getResources().getString(R.string.cat_education),
                getResources().getString(R.string.cat_jobs),
                getResources().getString(R.string.cat_queries)

        };

        //categories images Array
        int catImages[] = {

                R.drawable.ic_action_image_placeholder,
                R.drawable.ic_thumb_person,
                R.drawable.ic_action_like_unclicked,
                R.drawable.ic_action_bookmark,
                R.drawable.ic_action_call,
                R.drawable.ic_action_email,
                R.drawable.ic_action_location,
                R.drawable.ic_action_time,
                R.drawable.ic_action_contact

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

        SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity().getBaseContext(), aList, R.layout.cat_grid_item_layout, from, to);
        GridView catGridView = view.findViewById(R.id.catsGridView);
        catGridView.setAdapter(simpleAdapter);

        catGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /*
                "Featured",
                "Popular",
                "UpComing",
                "Events",
                "Business",
                "Buy and sell",
                "Education",
                "Jobs",
                "Queries"*/

                //use position to open catViewActivity
                switch (position) {

                    case 0:
                        openCat("featured");
                        break;
                    case 1:
                        openCat("popular");
                        break;
                    case 2:
                        openCat("upcoming");
                        break;
                    case 3:
                        openCat("events");
                        break;
                    case 4:
                        openCat("business");
                        break;
                    case 5:
                        openCat("buysell");
                        break;
                    case 6:
                        openCat("education");
                        break;
                    case 7:
                        openCat("jobs");
                        break;
                    case 8:
                        openCat("queries");
                        break;

                    default:
                        Log.d(TAG, "onItemClick: default");

                }

            }
        });


        return view;

    }


    @Override
    public void onClick(View v) {

        MainActivity activity = (MainActivity) getActivity();

        //set on click listener
        switch (v.getId()) {

            case R.id.catBusinessLayout:
                //open the cat view
                openCat("business");
                break;

            case R.id.catPopularLayout:
                openCat("popular");
                break;

            case R.id.catFeaturedLayout:
                openCat("featured");
                break;

            case R.id.catComingupLayout:
                openCat("comingup");
                break;

            case R.id.catEducationLayout:
                openCat("education");
                break;

            case R.id.catBuySellLayout:
                openCat("buysell");
                break;

            case R.id.catEventsLayout:
                openCat("events");
                break;

            case R.id.catJobsLayout:
                openCat("jobs");
                break;

            case R.id.catPlacesLayout:
                openCat("places");
                break;

            default:
                Log.d(TAG, "onClick: at default");
        }

    }

    private void openCat(String catValue) {
        Intent openCatIntent = new Intent(getContext(), ViewCategoryActivity.class);
        openCatIntent.putExtra("category", catValue);
        startActivity(openCatIntent);
    }


}
