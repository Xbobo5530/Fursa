package com.nyayozangu.labs.fursa.activities.search.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.search.SearchableActivity;
import com.nyayozangu.labs.fursa.activities.tags.Tags;
import com.nyayozangu.labs.fursa.activities.tags.adapter.TagsRecyclerAdapter;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class TagsSearchResultsFragment extends Fragment {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private ProgressDialog progressDialog;

    private List<Posts> postsList;
    private List<Tags> tagsList;

    private TagsRecyclerAdapter tagsSearchRecyclerAdapter;

    public TagsSearchResultsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tags_search_results, container, false);

        RecyclerView tagsSearchFeed = view.findViewById(R.id.tagsSearchRecyclerView);
        postsList = new ArrayList<>();
        tagsList = new ArrayList<>();

        tagsSearchRecyclerAdapter = new TagsRecyclerAdapter(tagsList);
        tagsSearchFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
        tagsSearchFeed.setHasFixedSize(true);
        tagsSearchFeed.setAdapter(tagsSearchRecyclerAdapter);

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

    private void doMySearch(final String searchQuery) {
        Log.d(TAG, "doMySearch: ");
        //show progress
        showProgress(getResources().getString(R.string.loading_text));

        //get post list


        coMeth.getDb()
                .collection(CoMeth.TAGS)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                if (document.get("title")
                                        .toString().toLowerCase().contains(searchQuery)) {
                                    Log.d(TAG, "onSuccess: tag has search query");
                                    //convert tag to object
                                    Tags tag = document.toObject(Tags.class);
                                    if (!tagsList.contains(tag)) {
                                        tagsList.add(tag);
                                    }
                                    tagsSearchRecyclerAdapter.notifyDataSetChanged();
                                    coMeth.stopLoading(progressDialog);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get tags\n" + e.getMessage());
                    }
                });
    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(message);
        progressDialog.show();
    }

}
