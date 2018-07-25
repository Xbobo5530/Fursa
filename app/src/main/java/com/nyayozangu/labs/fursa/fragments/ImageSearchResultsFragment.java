package com.nyayozangu.labs.fursa.fragments;


import android.app.Activity;
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
public class ImageSearchResultsFragment extends Fragment {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private List<Posts> postsList;
    private List<Users> usersList;
    private SearchableActivity mActivity;

    private PostsRecyclerAdapter imageSearchRecyclerAdapter;

    public ImageSearchResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image_search_results, container,
                false);

        //initialize
        RecyclerView imageSearchFeed = view.findViewById(R.id.imageSearchRecyclerView);
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();
        mActivity = ((SearchableActivity)getActivity());

        String className = "SearchableActivity";
        imageSearchRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className,
                Glide.with(this), getActivity());
        coMeth.handlePostsView(getContext(), getActivity(), imageSearchFeed);
        //set an adapter for the recycler view
        imageSearchFeed.setAdapter(imageSearchRecyclerAdapter);

        //get search query
        if (mActivity != null) {
            String searchQuery = mActivity.getSearchQuery();
            Log.d(TAG, "onCreateView: search query is " + searchQuery);
            if (searchQuery != null && !searchQuery.isEmpty())
                doMySearch(searchQuery.toLowerCase());
        } else {
            //alert user when failed to get search query from activity
            mActivity.showSnack(getResources().getString(R.string.something_went_wrong_text));
        }
        return view;
    }

    private void doMySearch(String searchQuery) {
        postsList.clear();
        usersList.clear();
        if (mActivity != null) {
            List<Posts> mPostList = mActivity.getPostList();
            List<Users> mUserList = mActivity.getUserList();
            for (Posts post : mPostList) {
                String imageMetaData = "";
                if (post.getImage_labels() != null) {
                    imageMetaData = imageMetaData.concat(post.getImage_labels());
                }
                if (post.getImage_text() != null) {
                    imageMetaData = imageMetaData.concat(" " + post.getImage_text());
                }
                if (imageMetaData.toLowerCase().contains(searchQuery)) {
                    if (!postsList.contains(post)) {
                        postsList.add(post);
                        usersList.add(mUserList.get(mPostList.indexOf(post)));
                        imageSearchRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }
}
