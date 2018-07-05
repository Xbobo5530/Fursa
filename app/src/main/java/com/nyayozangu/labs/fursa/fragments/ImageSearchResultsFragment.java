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
public class ImageSearchResultsFragment extends Fragment {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private ProgressBar progressBar;
    private List<Posts> postsList;
    private List<Users> usersList;

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
        progressBar = view.findViewById(R.id.imageSearchProgressBar);
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();

        String className = "SearchableActivity";
        imageSearchRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className,
                Glide.with(this), getActivity());
        coMeth.handlePostsView(getContext(), getActivity(), imageSearchFeed);
        //set an adapter for the recycler view
        imageSearchFeed.setAdapter(imageSearchRecyclerAdapter);

        //get search query
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
        //show loading
//        showProgress(getResources().getString(R.string.loading_text));
        //clear old posts
        postsList.clear();
        usersList.clear();
        List<Posts> mPostList = ((SearchableActivity) getActivity()).getPostList();
        List<Users> mUserList = ((SearchableActivity) getActivity()).getUserList();
        Log.d(TAG, "doMySearch: \nmPostlist size is " + mPostList.size() +
                "\nmUserList is " + mUserList.size());
        for (Posts post : mPostList) {
            String imageMetaData = "";
            if (post.getImage_labels() != null) {
                imageMetaData = imageMetaData.concat(post.getImage_labels());
                Log.d(TAG, "doMySearch: psot has image labels " + imageMetaData);
            }
            if (post.getImage_text() != null) {
                imageMetaData = imageMetaData.concat(" " + post.getImage_text());
                Log.d(TAG, "doMySearch: post has image text " + imageMetaData);
            }
            if (imageMetaData.toLowerCase().contains(searchQuery)) {
                Log.d(TAG, "doMySearch: iamge meta data is " + imageMetaData);
                if (!postsList.contains(post)) {
                    Log.d(TAG, "doMySearch: image meta has query");
                    postsList.add(post);
                    usersList.add(mUserList.get(mPostList.indexOf(post)));
                    imageSearchRecyclerAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
