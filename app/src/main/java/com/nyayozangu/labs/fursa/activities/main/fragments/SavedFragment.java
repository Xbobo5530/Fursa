package com.nyayozangu.labs.fursa.activities.main.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class SavedFragment extends Fragment {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private RecyclerView savedPostsView;
    private SwipeRefreshLayout swipeRefresh;

    //retrieve posts
    private List<Posts> savedPostsList;
    private List<Users> usersList;

    //recycler adapter
    private PostsRecyclerAdapter savedPostsRecyclerAdapter;
    private DocumentSnapshot lastVisiblePost;
    private Boolean isFirstPageFirstLoad = true;
    private String currentUserId;
    private ProgressDialog progressDialog;

    public SavedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        //initiate elements
        savedPostsView = view.findViewById(R.id.savedPostView);
        swipeRefresh = view.findViewById(R.id.savedSwipeRefresh);

        //initiate an arrayList to hold all the posts
        savedPostsList = new ArrayList<>();
        usersList = new ArrayList<>();

        String className = "SavedFragment";
        savedPostsRecyclerAdapter = new PostsRecyclerAdapter(savedPostsList, usersList, className, Glide.with(this));
        coMeth.handlePostsView(getContext(), getActivity(), savedPostsView);
        savedPostsView.setAdapter(savedPostsRecyclerAdapter);

        //loading
        showProgress(getString(R.string.loading_text));

        if (coMeth.isConnected() && coMeth.isLoggedIn()) {
            currentUserId = coMeth.getUid();
        } else {

            if (!coMeth.isConnected()) {
                showSnack(getString(R.string.failed_to_connect_text));
            }

        }
        Log.d(TAG, "onCreateView: \ncurrentUserId is: " + currentUserId);

        loadPosts();

        //handle refresh
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //get new posts
                savedPostsView.getRecycledViewPool().clear();
                savedPostsList.clear();
                usersList.clear();
                loadPosts();
            }
        });

        //handle nav button re-select
        ((MainActivity) Objects.requireNonNull(getActivity())).mainBottomNav
                .setOnNavigationItemReselectedListener(
                        new BottomNavigationView.OnNavigationItemReselectedListener() {
                            @Override
                            public void onNavigationItemReselected(@NonNull MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.bottomNavSavedItem:
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
                .document(CoMeth.SAVED_POSTS_DOC)
                .collection(CoMeth.SAVED_POSTS)
                .orderBy(CoMeth.TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //user has saved posts

                            for (final QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                //get post id
                                final String postId = document.getId();
                                coMeth.getDb()
                                        .collection("Posts")
                                        .document(postId)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                //get post
                                                if (documentSnapshot.exists()) {
                                                    //the post exists
                                                    //convert doc to post object
                                                    final Posts post = documentSnapshot.toObject(Posts.class).withId(postId);
                                                    //get post user
                                                    final String postUserId = post.getUser_id();
                                                    coMeth.getDb()
                                                            .collection("Users")
                                                            .document(postUserId)
                                                            .get()
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                    if (documentSnapshot.exists()) {
                                                                        //user exists
                                                                        //get user object
                                                                        Users user = documentSnapshot.toObject(Users.class).withId(postUserId);
                                                                        //add post and user to post list and user list
                                                                        savedPostsList.add(post);
                                                                        usersList.add(user);
                                                                        savedPostsRecyclerAdapter.notifyDataSetChanged();
                                                                    } else {
                                                                        //user does not exist
                                                                        //delete post
                                                                        Log.d(TAG, "onSuccess: user does not exist");
                                                                        coMeth.getDb()
                                                                                .collection("Posts")
                                                                                .document(postId)
                                                                                .delete()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Log.d(TAG, "onSuccess:deleted post because post user does not exist");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.d(TAG, "onFailure: failed to delete post of user who does nt exist");
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d(TAG, "onFailure: failed to get user details\n" + e.getMessage());
                                                                }
                                                            });
                                                } else {
                                                    //post does not exists anymore
                                                    //delete reference
                                                    coMeth.getDb()
                                                            .collection("Users/" + currentUserId + "/Subscriptions/saved_posts/SavedPosts")
                                                            .document(postId)
                                                            .delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d(TAG, "onSuccess: deleted saved doc reference ");
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d(TAG, "onFailure: failed to delete doc reference on saved ref\n" + e.getMessage());
                                                                }
                                                            });
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: failed to get saved document\n" + e.getMessage());
                                            }
                                        });
                                coMeth.stopLoading(progressDialog, swipeRefresh);
                            }


                        } else {
                            //user has no saved posts
                            showSnack(getString(R.string.dont_have_saves_text));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get saved posts\n" + e.getMessage());
                        showSnack(getResources().getString(R.string.failed_to_get_saves) + ": " +
                                e.getMessage());
                    }
                });
    }

    //failed to get users
    private void showSnack(String message) {
        try {
            Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(R.id.mainSnack),
                    message, Snackbar.LENGTH_SHORT).show();
        } catch (NullPointerException nullE) {
            Log.d(TAG, "showSnack: null at saved posts\n" + nullE.getMessage());
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
