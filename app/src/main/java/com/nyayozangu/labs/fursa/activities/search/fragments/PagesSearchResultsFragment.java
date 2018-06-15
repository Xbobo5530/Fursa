package com.nyayozangu.labs.fursa.activities.search.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.search.SearchableActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;
import com.nyayozangu.labs.fursa.users.adapters.UsersRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class PagesSearchResultsFragment extends Fragment {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private ProgressBar progressBar;

    private List<Users> usersList;

    private UsersRecyclerAdapter pagesRecyclerAdapter;


    public PagesSearchResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pages_search_results, container, false);

        RecyclerView pagesSearchFeed = view.findViewById(R.id.pagesSearchRecyclerView);
        progressBar = view.findViewById(R.id.pagesSearchProgressBar);

        usersList = new ArrayList<>();

        pagesRecyclerAdapter = new UsersRecyclerAdapter(usersList);
        pagesSearchFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
        pagesSearchFeed.setHasFixedSize(true);
        pagesSearchFeed.setAdapter(pagesRecyclerAdapter);

        if (getActivity() != null) {
            String searchQuery = ((SearchableActivity) getActivity()).getSearchQuery();
            Log.d(TAG, "onCreateView: search query is " + searchQuery);
            if (searchQuery != null && !searchQuery.isEmpty())
                doMySearch(searchQuery.toLowerCase());
        } else {
            //alert user when failed to get search query from activity

            ((SearchableActivity) Objects.requireNonNull(getActivity()))
                    .showSnack(getResources().getString(R.string.something_went_wrong_text));
        }

        return view;
    }

    private void doMySearch(String searchQuery) {
        Log.d(TAG, "doMySearch: ");
        //clear old posts
        usersList.clear();
        List<Users> mUserList = ((SearchableActivity)
                Objects.requireNonNull(getActivity())).getUserList();
        ArrayList<String> userIds = new ArrayList<String>();
        for (Users user : mUserList) {
            String userData = user.getName();
            if (user.getBio() != null) {
                userData = userData.concat(" " + user.getBio());
            }
            if (userData.toLowerCase().contains(searchQuery)) {
                Log.d(TAG, "doMySearch: user had search query");
                String userId = user.UserId;
                if (!userIds.contains(userId)) {
                    Log.d(TAG, "doMySearch: userId in not in iserIds");
                    usersList.add(user);
                    pagesRecyclerAdapter.notifyDataSetChanged();
                    userIds.add(userId);
                }
            }

        }
    }

}
