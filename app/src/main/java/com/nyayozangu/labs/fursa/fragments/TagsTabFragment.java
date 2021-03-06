package com.nyayozangu.labs.fursa.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.MainActivity;
import com.nyayozangu.labs.fursa.adapters.TagsRecyclerAdapter;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TagsTabFragment extends Fragment {

    private static final String TAG = "Sean";
    private static final String POST_COUNT = "post_count";
    private CoMeth coMeth = new CoMeth();
    private List<Tags> tagsList;
    private RecyclerView tagsRecyclerView;
    private TagsRecyclerAdapter tagsRecyclerAdapter;
    private ProgressBar progressBar;

    public TagsTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_tags_tab, container, false);

        tagsRecyclerView = view.findViewById(R.id.tagsRecyclerView);
        tagsList = new ArrayList<>();
        tagsRecyclerAdapter = new TagsRecyclerAdapter(tagsList);
        coMeth.handlePostsView(getContext(), getActivity(), tagsRecyclerView);
        tagsRecyclerView.setHasFixedSize(true);
        tagsRecyclerView.setAdapter(tagsRecyclerAdapter);
        progressBar = view.findViewById(R.id.tagTabsProgressBar);
        coMeth.showProgress(progressBar);
        coMeth.getDb().collection(CoMeth.TAGS).orderBy(POST_COUNT, Query.Direction.DESCENDING)
                .limit(30).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Tags tag = doc.getDocument().toObject(Tags.class);
                                    tagsList.add(tag);
                                    tagsRecyclerAdapter.notifyDataSetChanged();
                                    coMeth.stopLoading(progressBar);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get tags\n" + e.getMessage());
                        progressBar.setVisibility(View.GONE);
                        showSnack(getResources().getString(R.string.error_text) + ": " + e.getMessage());
                    }
                });

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
           mainActivity.mainBottomNav.setOnNavigationItemReselectedListener(
                            new BottomNavigationView.OnNavigationItemReselectedListener() {
                                @Override
                                public void onNavigationItemReselected(@NonNull MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.bottomNavCatItem:
                                            tagsRecyclerView.smoothScrollToPosition(0);
                                            break;
                                        default:
                                            Log.d(TAG, "onNavigationItemReselected: at default");
                                    }
                                }
                            });
        }

        return view;
    }

    private void showSnack(String message) {
            Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(R.id.mainSnack),
                    message, Snackbar.LENGTH_LONG).show();
    }

}
