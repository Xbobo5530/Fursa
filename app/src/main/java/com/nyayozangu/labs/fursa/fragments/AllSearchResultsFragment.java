package com.nyayozangu.labs.fursa.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.SearchableActivity;
import com.nyayozangu.labs.fursa.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Posts;
import com.nyayozangu.labs.fursa.models.Users;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class AllSearchResultsFragment extends Fragment {


    private static final String TAG = "Sean";
    private RecyclerView allResultsView;
    private CoMeth coMeth = new CoMeth();
    private ProgressBar progressBar;
    //retrieve posts
    private List<Posts> postsList;
    private List<Users> usersList;

    //recycler adapter
    private PostsRecyclerAdapter searchRecyclerAdapter;
    private String searchableText = "";
    private String locString = "";

    //search query
    private String searchQuery;
    private RecyclerView searchFeed;

    public AllSearchResultsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_search_resuslts, container, false);

        //initiate items
        searchFeed = view.findViewById(R.id.allSearchRecyclerView);
        progressBar = view.findViewById(R.id.allSearchProgressBar);
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);

        //initiate the PostsRecyclerAdapter
        String className = "SearchableActivity";
        searchRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className, Glide.with(this));
        coMeth.handlePostsView(getContext(), getActivity(), searchFeed);
        //set an adapter for the recycler view
        searchFeed.setAdapter(searchRecyclerAdapter);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        showSearchResults();

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }, 1000);



        //add scroll to top on tool bar click
        ((SearchableActivity) Objects.requireNonNull(getActivity())).toolbar.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFeed.smoothScrollToPosition(0);
            }
        });
        return view;
    }

    private void showSearchResults() {
        Log.d(TAG, "showSearchResults: ");
        //clear previous data
        postsList.clear();
        usersList.clear();
        List<Posts> mPostList = ((SearchableActivity) getActivity()).getPostList();
        List<Users> mUsersList = ((SearchableActivity) getActivity()).getUserList();
        for (Posts post : mPostList) {
            if (!postsList.contains(post)) {
                postsList.add(post);
                usersList.add(mUsersList.get(mPostList.indexOf(post)));
                searchRecyclerAdapter.notifyDataSetChanged();
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.d(TAG, "showSearchResults: post list size is " + postsList.size());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        showSearchResults();
    }
}
