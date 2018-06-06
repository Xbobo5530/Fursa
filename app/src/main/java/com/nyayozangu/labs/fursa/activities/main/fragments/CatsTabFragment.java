package com.nyayozangu.labs.fursa.activities.main.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nyayozangu.labs.fursa.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CatsTabFragment extends Fragment {


    public CatsTabFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cats_tab, container, false);


        return view;
    }

}
