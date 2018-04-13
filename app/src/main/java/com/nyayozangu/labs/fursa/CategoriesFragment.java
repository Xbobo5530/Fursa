package com.nyayozangu.labs.fursa;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SimpleAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment {

    private static final String TAG = "Sean";
    //cat texts Array
    String[] catTitle = new String[]{

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

    };

    private SimpleAdapter adapter;
    private GridView gridView;


    public CategoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        // TODO: 4/12/18 handle the adapter on the categories


        return view;

    }

}
