package com.nyayozangu.labs.fursa.activities.tags;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nyayozangu.labs.fursa.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TagsTabFragment extends Fragment {


    public TagsTabFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tags_tab, container, false);


        return view;
    }

}
