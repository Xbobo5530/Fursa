package com.nyayozangu.labs.fursa.activities.main.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.tags.TagsTabFragment;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();


    private Button catsTabButton, tagsTabButton;

    //Fragments
    private CatsTabFragment catsTabFragment;
    private TagsTabFragment tagsTabFragment;

    public CategoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        //initiate tab fragments
        tagsTabFragment = new TagsTabFragment();
        catsTabFragment = new CatsTabFragment();

        //initiate items
        catsTabButton = view.findViewById(R.id.catsTabButton);
        tagsTabButton = view.findViewById(R.id.tagsTabButton);

        //set cats tab
        setFragment(catsTabFragment);

        //handle cats click
        catsTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(catsTabFragment);
            }
        });

        tagsTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(tagsTabFragment);
            }
        });


        /*final String[] catTitle = new String[]{

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
                R.drawable.popular, R.drawable.featured,
                R.drawable.business, R.drawable.appic,
                R.drawable.jobs, R.drawable.buysell,
                R.drawable.upcoming, R.drawable.events,
                R.drawable.places, R.drawable.services,
                R.drawable.school, R.drawable.help,
                R.drawable.appic, R.drawable.appic

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
        final GridView catGridView = view.findViewById(R.id.catsGridView);

        View gridCell = inflater.inflate(R.layout.cat_grid_item_layout, null);
        int cellWidth = measureCellWidth(getContext(), gridCell);
        catGridView.setColumnWidth(cellWidth);
        catGridView.setAdapter(simpleAdapter);
        catGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                *//*
                "Featured",
                "Popular",
                "UpComing",
                "Events",
                "Places"
                "Business",
                "Buy and sell",
                "Education",
                "Jobs",
                "Queries",
                "Exhibitions"*//*

                Log.d(TAG, "onItemClick: ");
                openCat(coMeth.getCatKey(coMeth.catTitle[position]));

            }
        });

        *//**
         * handle the re-selcting the categories tab on the main bottom navigation on MainActivity
         * *//*
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
                });*/

        return view;
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.catsFrame, fragment);
        fragmentTransaction.commit();
    }


}
