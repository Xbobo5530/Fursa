package com.nyayozangu.labs.fursa.activities.main.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.List;


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
        savedPostsRecyclerAdapter = new PostsRecyclerAdapter(savedPostsList, usersList, className);
        coMeth.handlePostsView(getContext(), getActivity(), savedPostsView);
//        savedPostsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        savedPostsView.setAdapter(savedPostsRecyclerAdapter);

        //loading
        showProgress(getString(R.string.loading_text));

        //listen for scrolling on the homeFeedView
        savedPostsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean reachedBottom = !savedPostsView.canScrollVertically(1);
                if (reachedBottom) {

                    Log.d(TAG, "at addOnScrollListener\n reached bottom");
                    loadMorePosts();

                }

            }
        });


        if (coMeth.isConnected() && coMeth.isLoggedIn()) {

            currentUserId = coMeth.getUid();

        } else {

            if (!coMeth.isConnected()) {

                showSnack(getString(R.string.failed_to_connect_text));

            }
        }


        Log.d(TAG, "onCreateView: \ncurrentUserId is: " + currentUserId);

        final Query firstQuery = coMeth.getDb()
                .collection("Users/" + currentUserId + "/Subscriptions")
                .document("saved_posts")
                .collection("SavedPosts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);
        //get all posts from the database
        loadPosts(firstQuery);

        //handle refresh
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //get new posts
                savedPostsView.getRecycledViewPool().clear();
                savedPostsList.clear();
                usersList.clear();
                loadPosts(firstQuery);

            }
        });

        return view;
    }


    private void loadPosts(Query firstQuery) {
        firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                //check if the data is loaded for the first time
                if (isFirstPageFirstLoad) {

                    //get the last visible post
                    try {

                        lastVisiblePost = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        savedPostsList.clear();
                        usersList.clear();

                    } catch (Exception exception) {

                        Log.d(TAG, "Error: " + exception.getMessage());

                    }

                }

                //create a for loop to check for document changes
                for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                    if (doc.getType() == DocumentChange.Type.ADDED) {

                        //get the post id for likes feature
                        final String postId = doc.getDocument().getId();
                        //uses postId to retrieve post details from Posts collections
                        coMeth.getDb()
                                .collection("Posts")
                                .document(postId)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                //check if task is success
                                if (task.isSuccessful()) {

                                    Log.d(TAG, "onComplete: task is successful");
                                    //check if post exists
                                    if (task.getResult().exists()) {

                                        Log.d(TAG, "onComplete: post exists\npost is: " + task.getResult());
                                        //post exists, convert post to object
                                        final Posts post = task.getResult().toObject(Posts.class).withId(postId);

                                        coMeth.getDb()
                                                .collection("Users")
                                                .document(currentUserId)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                //check if task is success
                                                if (task.isSuccessful()) {

                                                    String postUserId = task.getResult().getId();
                                                    Users user = task.getResult().toObject(Users.class).withId(postUserId);
                                                    //add post to saved posts list
                                                    if (isFirstPageFirstLoad) {


                                                        //add the post at position 0 of the postsList
                                                        savedPostsList.add(0, post);
                                                        usersList.add(0, user);


                                                    } else {

                                                        //if the first page is loaded the add new post normally
                                                        savedPostsList.add(post);
                                                        usersList.add(user);

                                                    }

                                                    //notify the recycler adapter of the set change
                                                    savedPostsRecyclerAdapter.notifyDataSetChanged();

                                                }

                                            }
                                        });

                                    } else {

                                        //post does not exist
                                        Log.d(TAG, "onComplete: post does not exist");

                                    }

                                } else {

                                    //task failed
                                    Log.d(TAG, "onComplete: getting post from Posts task failed\n" + task.getException());

                                }

                                coMeth.stopLoading(progressDialog, swipeRefresh);

                            }
                        });


                    }
                }

                //the first page has already loaded
                isFirstPageFirstLoad = false;

            }
        });
    }

    private void goToLogin() {
        //go to login page
        startActivity(new Intent(getActivity(), LoginActivity.class));
    }

    //loading more posts
    public void loadMorePosts() {

        Log.d(TAG, "loadMorePosts: ");
        Query nextQuery = coMeth.getDb()
                .collection("Users/" + currentUserId + "/Subscriptions")
                .document("saved_posts")
                .collection("SavedPosts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20);

        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(final QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                Log.d(TAG, "onEvent: next query");
                try {
                    //check if there area more posts
                    if (!queryDocumentSnapshots.isEmpty()) {

                        Log.d(TAG, "onEvent: query is not empty");
                        //get the last visible post
                        lastVisiblePost = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        //create a for loop to check for document changes
                        for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            //check if an item is added
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                final String postId = doc.getDocument().getId();
                                //uses postId to retrieve post details from Posts collections
                                coMeth.getDb().collection("Posts")
                                        .document(postId)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        //check if task is success
                                        if (task.isSuccessful()) {

                                            Log.d(TAG, "onComplete: task is successful");
                                            //check if post exists
                                            if (task.getResult().exists()) {

                                                //post exists, convert post to object
                                                final Posts post = task.getResult().toObject(Posts.class).withId(postId);

                                                coMeth.getDb().collection("Users")
                                                        .document(currentUserId)
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                        //check if task is success
                                                        if (task.isSuccessful()) {

                                                            //add user convert current user to object
                                                            Users user = task.getResult().toObject(Users.class);
                                                            //add post to saved posts list
                                                            savedPostsList.add(post);
                                                            usersList.add(user);
                                                            //notify the recycler adapter of the set change
                                                            savedPostsRecyclerAdapter.notifyDataSetChanged();

                                                        }

                                                    }
                                                });

                                            } else {

                                                //post does not exist
                                                Log.d(TAG, "onComplete: post does not exist");

                                            }

                                        } else {

                                            //task failed
                                            Log.d(TAG, "onComplete: getting post from Posts task failed\n" + task.getException());

                                        }

                                    }
                                });



                            }
                        }

                    }
                } catch (NullPointerException nullException) {

                    //the Query is null
                    Log.e(TAG, "error: " + nullException.getMessage());

                }
            }
        });


    }



    //failed to get users
    private void showSnack(String message) {
        Snackbar.make(getActivity().findViewById(R.id.main_activity_layout),
                message, Snackbar.LENGTH_SHORT).show();
    }

    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(getContext());
        loginAlertBuilder.setTitle("Login")
                .setIcon(getActivity().getDrawable(R.drawable.ic_action_red_alert))
                .setMessage("You are not logged in\n" + message)
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send user to login activity
                        goToLogin();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        dialog.cancel();
                    }
                })
                .show();
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
