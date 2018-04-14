package com.nyayozangu.labs.fursa;


import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "Sean";
    //cat texts Array
    /*String[] catTitle = new String[]{

            "Featured",
            "Popular",
            "UpComing",
            "Events",
            "Business",
            "Buy and sell",
            "Education",
            "Jobs",
            "Queries"

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

    };*/


    private ConstraintLayout popular;
    private ConstraintLayout featured;
    private ConstraintLayout comingup;
    private ConstraintLayout business;
    private ConstraintLayout education;
    private ConstraintLayout jobs;
    private ConstraintLayout buysell;
    private ConstraintLayout places;
    private ConstraintLayout events;


    public CategoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        // TODO: 4/12/18 handle the adapter on the categories

        /*//create a simple adapter


        List<HashMap<String, String>> aList = new ArrayList<>();

        for (int i = 0; i < catTitle.length; i++) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("listView_title", catTitle[i]);
            hm.put("listView_image", Integer.toString(catImages[i]));
            aList.add(hm);
        }

        String[] from = {"listView_image", "listView_title"};
        int[] to = {R.id.catListItemImageVIew, R.id.catListItemTextView};

        SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity().getBaseContext(), aList, R.layout.cat_list_item, from, to);
        ListView androidListView = container.findViewById(R.id.catListView);
//        androidListView.setAdapter(simpleAdapter);*/


        //initiate items
        popular = view.findViewById(R.id.catPopularLayout);
        featured = view.findViewById(R.id.catFeaturedLayout);
        comingup = view.findViewById(R.id.catComingupLayout);
        business = view.findViewById(R.id.catBusinessLayout);
        education = view.findViewById(R.id.catEducationLayout);
        jobs = view.findViewById(R.id.catJobsLayout);
        buysell = view.findViewById(R.id.catBuySellLayout);
        places = view.findViewById(R.id.catPlacesLayout);
        events = view.findViewById(R.id.catEventsLayout);


        popular.setOnClickListener(this);
        featured.setOnClickListener(this);
        comingup.setOnClickListener(this);
        business.setOnClickListener(this);
        education.setOnClickListener(this);
        education.setOnClickListener(this);
        jobs.setOnClickListener(this);
        buysell.setOnClickListener(this);
        places.setOnClickListener(this);
        events.setOnClickListener(this);



        return view;

    }


    @Override
    public void onClick(View v) {

        MainActivity activity = (MainActivity) getActivity();

        //set on click listener
        switch (v.getId()) {

            case R.id.catBusinessLayout:
                //open the cat view

                Intent openCatIntent = new Intent(getContext(), ViewCategoryActivity.class);
                openCatIntent.putExtra("category", "business");
                startActivity(openCatIntent);
                break;

        }

    }
}
