package com.nyayozangu.labs.fursa.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.MainActivity;
import com.nyayozangu.labs.fursa.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.models.Posts;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Users;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class SavedTabFragment extends Fragment {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private RecyclerView savedPostsView;

    //retrieve posts
    private List<Posts> savedPostsList;
    private List<Users> usersList;

    //recycler adapter
    private PostsRecyclerAdapter savedPostsRecyclerAdapter;
    private DocumentSnapshot lastVisiblePost;
    private Boolean isFirstPageFirstLoad = true;
    private String currentUserId;
    private ProgressBar progressBar;

    public SavedTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        //initiate elements
        savedPostsView = view.findViewById(R.id.savedPostView);
        progressBar = view.findViewById(R.id.savedProgressBar);

        //initiate an arrayList to hold all the posts
        savedPostsList = new ArrayList<>();
        usersList = new ArrayList<>();

        String className = "SavedTabFragment";
        savedPostsRecyclerAdapter = new PostsRecyclerAdapter(savedPostsList, usersList, className,
                Glide.with(this), getActivity());
        coMeth.handlePostsView(getContext(), getActivity(), savedPostsView);
        savedPostsView.setAdapter(savedPostsRecyclerAdapter);

        if (coMeth.isConnected() && coMeth.isLoggedIn()) {
            currentUserId = coMeth.getUid();
        }

        loadPosts();

        //handle nav button re-select
        ((MainActivity) Objects.requireNonNull(getActivity())).mainBottomNav
                .setOnNavigationItemReselectedListener(
                        new BottomNavigationView.OnNavigationItemReselectedListener() {
                            @Override
                            public void onNavigationItemReselected(@NonNull MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.bottomNavHomeItem:
                                        savedPostsView.smoothScrollToPosition(0);
                                        break;
                                    default:
                                        Log.d(TAG, "onNavigationItemReselected: at default");
                                }
                            }
                        });
        return view;
    }

    private void loadPosts() {
        coMeth.getDb()
                .collection(CoMeth.USERS + "/" + currentUserId + "/" + CoMeth.SUBSCRIPTIONS)
                .document(CoMeth.SAVED_POSTS_DOC).collection(CoMeth.SAVED_POSTS)
                .orderBy(CoMeth.TIMESTAMP, Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (final QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                //get post id
                                final String postId = document.getId();
                                getSavedPostDetails(postId);
                            }
                        } else {
                            //user has no saved posts
//                            showSnack(getString(R.string.dont_have_saves_text));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get saved posts\n" + e.getMessage());
//                        showSnack(getResources().getString(R.string.failed_to_get_saves) + ": " +
//                                e.getMessage());
                    }
                });
    }

    private void getSavedPostDetails(final String postId) {
        Log.d(TAG, "getSavedPostDetails: ");
        coMeth.getDb()
                .collection(CoMeth.POSTS)
                .document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //get post
                        if (documentSnapshot.exists()) {
                            //the post exists
                            //convert doc to post object
                            final Posts post = Objects.requireNonNull(
                                    documentSnapshot.toObject(Posts.class)).withId(postId);
                            //get post user
                            final String postUserId = post.getUser_id();
                            getUserData(post, postUserId, postId);
                        } else {
                            //post does not exists anymore
                            //delete reference
                            coMeth.getDb()
                                    .collection(CoMeth.USERS + "/" +
                                            currentUserId + "/" + CoMeth.SUBSCRIPTIONS + "/" +
                                            CoMeth.SAVED_POSTS_DOC + "/" + CoMeth.SAVED_POSTS)
                                    .document(postId)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: deleted saved doc reference ");
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressBar.setVisibility(View.GONE);
                                            Log.d(TAG, "onFailure: failed to delete doc" +
                                                    " reference on saved ref\n" +
                                                    e.getMessage());
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get saved document\n" +
                                e.getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    private void getUserData(final Posts post, final String postUserId, final String postId) {
        coMeth.getDb()
                .collection(CoMeth.USERS)
                .document(postUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //user exists
                            //get user object
                            Users user = Objects.requireNonNull(
                                    documentSnapshot.toObject(Users.class)).withId(postUserId);
                            //add post and user to post list and user list
                            savedPostsList.add(post);
                            usersList.add(user);
                            savedPostsRecyclerAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get user details\n" + e.getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

}