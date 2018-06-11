package com.nyayozangu.labs.fursa.activities.tags;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.tags.adapter.TagsRecyclerAdapter;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

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
        tagsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        tagsRecyclerView.setHasFixedSize(true);
        tagsRecyclerView.setAdapter(tagsRecyclerAdapter);

        //show loading
        showProgress(getResources().getString(R.string.loading_text));
        //access tags
        Log.d(TAG, "onCreateView: accessing tags");
        coMeth.getDb()
                .collection("Tags")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        Log.d(TAG, "onEvent: checking tags");
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "onEvent: tags not empty");
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                Log.d(TAG, "onEvent: in for loop");
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Log.d(TAG, "onEvent: getting tag id");
                                    final String tag = doc.getDocument().getId();
                                    Log.d(TAG, "onEvent: accessing posts in tag");
                                    //getTags
                                    getTags(tag);
                                }
                            }
                        }
                    }
                });

        // TODO: 6/7/18 account for posts cont == 0
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
                                    Log.d(TAG, "onEvent: tag type is added");
                                    Tags tag = doc.getDocument().toObject(Tags.class);
                                    Log.d(TAG, "onEvent: doc converted to tag object");
                                    tagsList.add(tag);
                                    Log.d(TAG, "onEvent: tag added to tagList");
                                    tagsRecyclerAdapter.notifyDataSetChanged();
                                    coMeth.stopLoading(progressDialog);
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
                        showSnack(getResources().getString(R.string.error_text) + ": " + e.getMessage());
                    }
                });

        //handle reselect tab
        try {
            ((MainActivity) getActivity()).mainBottomNav.setOnNavigationItemReselectedListener(
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
        } catch (NullPointerException nullE) {
            Log.d(TAG, "onCreateView: null on reselect\n" + nullE.getMessage());
        }
        return view;
    }

    private void getTags(final String tag) {
        coMeth.getDb()
                .collection("Tags")
                .document(tag)
                .collection("Posts")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "onEvent: getting posts count");
                            int postsCount = queryDocumentSnapshots.getDocuments().size();
                            Log.d(TAG, "onEvent: post count is " + postsCount);
                            //updateTags
                            updateTags(postsCount, tag);
                        } else {
                            //Tag has no content
                            //delete tag
                            coMeth.getDb()
                                    .collection("Tags")
                                    .document(tag)
                                    .delete();
                            Log.d(TAG, "onEvent: deleted empty tag document");
                        }
                    }
                });
    }

    private void updateTags(int postsCount, String tag) {
        Map<String, Object> postCountMap = new HashMap<>();
        postCountMap.put("post_count", postsCount);
        //setting posts count
        Log.d(TAG, "onEvent: setting posts count");
        coMeth.getDb()
                .collection("Tags")
                .document(tag)
                .update(postCountMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: getting tags to display");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update post count " +
                                e.getMessage());
                    }
                });
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
