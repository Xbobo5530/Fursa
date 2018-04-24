package com.nyayozangu.labs.fursa.activities.main.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlertFragment extends Fragment {

    //initiate members
    private Button alertLoginButton;


    public AlertFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alert, container, false);

        //initiate
        alertLoginButton = view.findViewById(R.id.alrerLoginButton);
        alertLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });

        return view;
    }

    private void goToLogin() {
        //open the login page
        startActivity(new Intent(getContext(), LoginActivity.class));
    }

}