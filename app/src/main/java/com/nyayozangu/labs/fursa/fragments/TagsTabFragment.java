package com.nyayozangu.labs.fursa.fragments;


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

/**
 * A simple {@link Fragment} subclass.
 */
public class TagsTabFragment extends Fragment {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private List<Tags> tagsList;
    private RecyclerView tagsRecyclerView;
    private TagsRecyclerAdapter tagsRecyclerAdapter;

    //progress
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;

    public TagsTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_tags_tab, container, false);

        tagsRecyclerView = view.findViewById(R.id.tagsRecyclerView);
        tagsList = new ArrayList<>();
        tagsRecyclerAdapter = new TagsRecyclerAdapter(tagsList);
        coMeth.handlePostsView(getContext(), getActivity(), tagsRecyclerView);
//        tagsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        tagsRecyclerView.setHasFixedSize(true);
        tagsRecyclerView.setAdapter(tagsRecyclerAdapter);

        //show loading

//        showProgress(getResources().getString(R.string.loading_text));
        progressBar = view.findViewById(R.id.tagTabsProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        coMeth.getDb()
                .collection("Tags")
                .orderBy("post_count", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                Log.d(TAG, "onEvent: adding tag to tagList");
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Tags tag = doc.getDocument().toObject(Tags.class);
                                    tagsList.add(tag);
                                    tagsRecyclerAdapter.notifyDataSetChanged();
//                                    coMeth.stopLoading(progressDialog);
                                    progressBar.setVisibility(View.GONE);
                                    Log.d(TAG, "onEvent: added");
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

        //handle reselect tab
        ((MainActivity) Objects.requireNonNull(getActivity())).mainBottomNav
                .setOnNavigationItemReselectedListener(
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

        return view;
    }



    private void showSnack(String message) {
        try {
            Snackbar.make(getActivity().findViewById(R.id.mainSnack),
                    message, Snackbar.LENGTH_LONG).show();
        } catch (NullPointerException nullE) {
            Log.d(TAG, "showSnack: null at tags frag snack\n" + nullE.getMessage());
        }

    }

    //show progress
    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(message);
        progressDialog.show();
    }

}
