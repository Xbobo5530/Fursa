package com.nyayozangu.labs.fursa.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.LoginActivity;
import com.nyayozangu.labs.fursa.activities.MainActivity;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlertFragment extends Fragment {


    public AlertFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alert, container, false);

        //initiate
        Button alertLoginButton = view.findViewById(R.id.alrerLoginButton);
        alertLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });

        FloatingActionButton newPostFab = ((MainActivity)
                Objects.requireNonNull(getActivity())).getNewPostFab();
        newPostFab.setImageResource(R.drawable.ic_action_add_white);
        newPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).handleNewPostFab();
            }
        });

        return view;
    }

    private void goToLogin() {
        //open the login page
        startActivity(new Intent(getContext(), LoginActivity.class));
    }

}
