package com.nyayozangu.labs.fursa.activities.search.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.search.SearchableActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

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

        //initiate the PostsRecyclerAdapter
        String className = "SearchableActivity";
        searchRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className);
        coMeth.handlePostsView(
                getContext(), getActivity(), searchFeed);
        //set an adapter for the recycler view
        searchFeed.setAdapter(searchRecyclerAdapter);

        //show progress
        progressBar.setVisibility(View.VISIBLE);
        //get post list

//        Log.d(TAG, "run: fetching results");
//        usersList = ((SearchableActivity) getActivity()).getUserList();
//        postsList = ((SearchableActivity)
//                Objects.requireNonNull(getActivity())).getPostList();
//        if (!postsList.isEmpty() && progressBar.getVisibility() == View.VISIBLE) {
//            //hide the progress bar
//            progressBar.setVisibility(View.GONE);
//            searchRecyclerAdapter.notifyDataSetChanged();
//        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: fetching results after 1 sec");
                usersList = ((SearchableActivity) getActivity()).getUserList();
                postsList = ((SearchableActivity)
                        Objects.requireNonNull(getActivity())).getPostList();
                if (!postsList.isEmpty() && progressBar.getVisibility() == View.VISIBLE) {
                    //hide the progress bar
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "run: post list size is " + postsList.size());
                    searchRecyclerAdapter.notifyDataSetChanged();
                }
//                searchRecyclerAdapter.notifyDataSetChanged();
            }
        }, 2000);
//        usersList = ((SearchableActivity) getActivity()).getUserList();
//        postsList = ((SearchableActivity) Objects.requireNonNull(getActivity())).getPostList();



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

}
