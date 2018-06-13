package com.nyayozangu.labs.fursa.activities.search.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.search.SearchableActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageSearchResultsFragment extends Fragment {

    private static final String TAG = "Sean";
    private RecyclerView imageSearchFeed;
    private CoMeth coMeth = new CoMeth();
    private ProgressDialog progressDialog;
    //retrieve posts
    private List<Posts> postsList;
    private List<Users> usersList;

    private PostsRecyclerAdapter imageSearchRecyclerAdapter;
    private String searchQuery;
    private DocumentSnapshot lastVisiblePost;
    private Boolean isFirstPageFirstLoad = true;

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
        imageSearchFeed = view.findViewById(R.id.imageSearchRecyclerView);


        postsList = ((SearchableActivity) getActivity()).getPostList();
        usersList = ((SearchableActivity) getActivity()).getUserList();

        Log.d(TAG, "onCreateView: " +
                "\npost list size: " + postsList.size() +
                "\nuser list size: " + usersList.size());

        String className = "SearchableActivity";
        imageSearchRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className);
        coMeth.handlePostsView(
                getContext(), getActivity(), imageSearchFeed);
        //set an adapter for the recycler view
        imageSearchFeed.setAdapter(imageSearchRecyclerAdapter);


        //get search query
        if (getActivity() != null) {
            searchQuery = ((SearchableActivity) getActivity()).getSearchQuery();
            Log.d(TAG, "onCreateView: search query is " + searchQuery);
            if (searchQuery != null && !searchQuery.isEmpty())
                doMySearch(searchQuery);
        } else {
            //alert user when failed to get search query from activity

            ((SearchableActivity) Objects.requireNonNull(getActivity()))
                    .showSnack(getResources().getString(R.string.something_went_wrong_text));
        }

        return view;

    }

    private void doMySearch(String searchQuery) {
        Log.d(TAG, "doMySearch: ");

        Query firstQuery = coMeth.getDb()
                .collection(CoMeth.POSTS);
//                .orderBy("timestamp");
//                .limit(50);
        //show loading
        showProgress(getResources().getString(R.string.loading_text));

    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(message);
        progressDialog.show();
    }

}
