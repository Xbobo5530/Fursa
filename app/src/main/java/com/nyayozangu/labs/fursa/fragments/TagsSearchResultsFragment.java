package com.nyayozangu.labs.fursa.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.SearchableActivity;
import com.nyayozangu.labs.fursa.adapters.TagsRecyclerAdapter;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Posts;
import com.nyayozangu.labs.fursa.models.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class TagsSearchResultsFragment extends Fragment {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private ProgressBar progressBar;
    private List<Posts> postsList;
    private List<Tags> tagsList;

    private TagsRecyclerAdapter tagsRecyclerAdapter;

    public TagsSearchResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tags_search_results, container, false);

        RecyclerView tagsSearchFeed = view.findViewById(R.id.tagsSearchRecyclerView);
        progressBar = view.findViewById(R.id.tagsSearchProgressBar);
        postsList = new ArrayList<>();
        tagsList = new ArrayList<>();

        tagsRecyclerAdapter = new TagsRecyclerAdapter(tagsList);
        coMeth.handlePostsView(getContext(), getActivity(), tagsSearchFeed);
//        tagsSearchFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
        tagsSearchFeed.setHasFixedSize(true);
        tagsSearchFeed.setAdapter(tagsRecyclerAdapter);

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
        progressBar.setVisibility(View.VISIBLE);
        //clear posts
        postsList.clear();
        tagsList.clear();
        List<Posts> mPostList = ((SearchableActivity)
                Objects.requireNonNull(getActivity())).getPostList();
        final ArrayList<String> tagTitles = new ArrayList<>();
        for (Posts post : mPostList) {
            if (post.getTags() != null && !post.getTags().isEmpty()) {
                for (String tag : post.getTags()) {
                    if (tag.toLowerCase().contains(searchQuery)) {
                        //get tag from db
                        coMeth.getDb()
                                .collection(CoMeth.TAGS).document(tag).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            // tags with no posts are deleted
                                            //convert snapshot to object
                                            Tags tag = documentSnapshot.toObject(Tags.class);
                                            if (tag != null) {
                                                String tagTitle = tag.getTitle();
                                                if (!tagTitles.contains(tagTitle)) {
                                                    tagsList.add(tag);
                                                    tagsRecyclerAdapter.notifyDataSetChanged();
                                                    tagTitles.add(tagTitle);
                                                    if (progressBar.getVisibility() == View.VISIBLE) {
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: failed to get tag from db\n" +
                                                e.getMessage());
                                    }
                                });
                    }
                }

            }
        }



        //get post list
        coMeth.getDb().collection(CoMeth.TAGS).get()
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
                                    tagsRecyclerAdapter.notifyDataSetChanged();
                                    if (progressBar.getVisibility() == View.VISIBLE) {
                                        progressBar.setVisibility(View.GONE);
                                    }
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

}
