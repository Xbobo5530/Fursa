package com.nyayozangu.labs.fursa.activities.main.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class TermsFragment extends Fragment {

    private WebView termsWebView;
    private Button termsButton;

    public TermsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_terms, container, false);

        //initialize items
        termsWebView = view.findViewById(R.id.termsWeb);
        termsButton = view.findViewById(R.id.acceptTermsButton);

        //load webView
        termsWebView.loadUrl(getResources().getString(R.string.privacy_policy_url));

        //handle button click
        termsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set the has accepted terms value to true
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.has_accepted_terms),
                        getString(R.string.true_value));
                editor.apply();
                //show bottom nav and go home
                ((MainActivity) getActivity()).mainBottomNav.setVisibility(View.VISIBLE);
                ((MainActivity) getActivity()).mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);

            }
        });

        return view;
    }

}
