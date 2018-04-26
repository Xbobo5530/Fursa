package com.nyayozangu.labs.fursa.activities.main.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SavedFragment extends Fragment {

    private static final String TAG = "Sean";
    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView savedPostsView;

    //retrieve posts
    private List<Posts> savedPostsList;
    private List<Users> usersList;

    //recycler adapter
    private PostsRecyclerAdapter savedPostsRecyclerAdapter;

    private DocumentSnapshot lastVisiblePost;

    private Boolean isFirstPageFirstLoad = true;

    private String currentUserId;

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

        //initiate an arrayList to hold all the posts
        savedPostsList = new ArrayList<>();
        usersList = new ArrayList<>();

        //initiate the PostsRecyclerAdapter
        savedPostsRecyclerAdapter = new PostsRecyclerAdapter(savedPostsList, usersList);


        //set a layout manager for homeFeedView (recycler view)
        savedPostsView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //set an adapter for the recycler view
        savedPostsView.setAdapter(savedPostsRecyclerAdapter);

        //initiate firebase auth
        mAuth = FirebaseAuth.getInstance();

        //initiate the firebase elements
        db = FirebaseFirestore.getInstance();

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

        if (isLoggedIn()) {

            currentUserId = mAuth.getCurrentUser().getUid();
        }

        Log.d(TAG, "onCreateView: \ncurrentUserId is: " + currentUserId);

        Query firstQuery = db
                .collection("Users/" + currentUserId + "/Subscriptions").document("saved_posts")
                .collection("SavedPosts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);
        //get all posts from the database
        firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                //check if the data is loaded for the first time
                /**
                 * if new data is added it will be added to the first query not the second query
                 */
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
                        String postId = doc.getDocument().getId();
                        //uses postId to retrieve post details from Posts collections
                        db.collection("Posts").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                //check if task is success
                                if (task.isSuccessful()) {

                                    //check if post exists
                                    if (task.getResult().exists()) {

                                        //post exists, convert post to object
                                        final Posts post = task.getResult().toObject(Posts.class);

                                        db.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                //check if task is success
                                                if (task.isSuccessful()) {

                                                    //add user convert current user to object
                                                    Users user = task.getResult().toObject(Users.class);
                                                    //add post to saved posts list
                                                    if (isFirstPageFirstLoad) {

                                                        //if the first page is loaded the add new post normally
                                                        savedPostsList.add(post);
                                                        usersList.add(user);

                                                    } else {

                                                        //add the post at position 0 of the postsList
                                                        savedPostsList.add(0, post);
                                                        usersList.add(0, user);

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

                            }
                        });

                        /*final Posts post = doc.getDocument().toObject(Posts.class).withId(postId);

                        //get user id
                        String postUserId = doc.getDocument().getString("user_id");

                        //get user_id for post
                        db.collection("Users").document(postUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<DocumentSnapshot> task) {

                                //check if task is successful
                                if (task.isSuccessful()) {

                                    if (isLoggedIn()) {
                                        currentUserId = mAuth.getCurrentUser().getUid();

                                        // filter saved posts
                                        db.collection("Posts/" + postId + "/Saves").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                //check if task is successful
                                                if(task.isSuccessful()){

                                                    //check if user has liked post
                                                    if (task.getResult().exists()){

                                                        //user has liked post
                                                        Users user = task.getResult().toObject(Users.class);
                                                        //user has liked current post, add post to saved posts
                                                        if (isFirstPageFirstLoad) {

                                                            //if the first page is loaded the add new post normally
                                                            savedPostsList.add(post);
                                                            usersList.add(user);

                                                        } else {

                                                            //add the post at position 0 of the postsList
                                                            savedPostsList.add(0, post);
                                                            usersList.add(0, user);

                                                        }

                                                        //notify the recycler adapter of the set change
                                                        savedPostsRecyclerAdapter.notifyDataSetChanged();

                                                    }else{

                                                        //user has not saved curr post
                                                        Log.d(TAG, "onComplete: user has not saved curr post");

                                                    }

                                                }else{

                                                    //task failed
                                                    Log.d(TAG, "onComplete: task has failed\n" + task.getException());

                                                }

                                            }
                                        });



                                                *//*addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                                                //check if user liked post
                                                if (documentSnapshot.exists()) {

                                                    Users user = task.getResult().toObject(Users.class);
                                                    usersList.add(user);
                                                    //user has liked current post, add post to saved posts
                                                    if (isFirstPageFirstLoad) {
                                                        //if the first page is loaded the add new post normally
                                                        savedPostsList.add(post);
                                                    } else {
                                                        //add the post at position 0 of the postsList
                                                        savedPostsList.add(0, post);
                                                        usersList.add(0, user);

                                                    }
                                                    //notify the recycler adapter of the set change
                                                    savedPostsRecyclerAdapter.notifyDataSetChanged();

                                                }

                                            }
                                        });*//*
                                    } else {

                                        //tell user to log in
                                        showLoginAlertDialog(getString(R.string.login_to_view_saved_items_alert_fragment_text));

                                    }

                                } else {

                                    //task failed, faled to get users
                                    Log.d(TAG, "onComplete: failed to get users");

                                }

                            }
                        });*/
                    }
                }

                //the first page has already loaded
                isFirstPageFirstLoad = false;

            }
        });

        return view;
    }

    private void goToLogin() {
        //go to login page
        startActivity(new Intent(getActivity(), LoginActivity.class));
    }


    //loading more posts
    public void loadMorePosts() {

        Query nextQuery = db
                .collection("Users/" + currentUserId + "/Subscriptions").document("saved_posts")
                .collection("SavedPosts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);


        //get all posts from the database
        //use snapshotListener to get all the data real time
        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(final QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                try {
                    //check if there area more posts
                    if (!queryDocumentSnapshots.isEmpty()) {

                        //get the last visible post
                        lastVisiblePost = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        //create a for loop to check for document changes
                        for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            //check if an item is added
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String postId = doc.getDocument().getId();
                                //uses postId to retrieve post details from Posts collections
                                db.collection("Posts").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        //check if task is success
                                        if (task.isSuccessful()) {

                                            //check if post exists
                                            if (task.getResult().exists()) {

                                                //post exists, convert post to object
                                                final Posts post = task.getResult().toObject(Posts.class);

                                                db.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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


                                /*
                                //converting database data into objects
                                final User post = doc.getDocument().toObject(Posts.class).withId(postId);
                                //get user id
                                String postUserId = doc.getDocument().getString("user_id");
                                //get user_id for post
                                db.collection("Users").document(postUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull final Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            if (task.getResult().exists()) {

                                                db.collection("Posts/" + postId + "/Saves").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                        //check if task is successful
                                                        if (task.isSuccessful()) {

                                                            //check if user has liked pots
                                                            if (task.getResult().exists()) {

                                                                Users user = task.getResult().toObject(Users.class);
                                                                usersList.add(user);
                                                                savedPostsList.add(post);
                                                                savedPostsRecyclerAdapter.notifyDataSetChanged();

                                                            } else {

                                                                //user has not liked post
                                                                Log.d(TAG, "onComplete: user has not liked post");

                                                            }

                                                        } else {

                                                            //task failed
                                                            Log.d(TAG, "onComplete: task failed\n" + task.getException());

                                                        }


                                                    }
                                                });

                                            }else{

                                                //post user does not exist
                                                Log.d(TAG, "onComplete: psot user does not exist");

                                            }


                                        }else{

                                            //checking for post user nd current user task failed
                                            Log.d(TAG, "onComplete: checking for post user nd current user task failed" + task.getException());

                                        }



                                                *//*.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                                                //check if user liked post
                                                if (documentSnapshot.exists()) {

                                                    Users user = task.getResult().toObject(Users.class);
                                                    usersList.add(user);
                                                    //user has liked current post, add post to saved posts
                                                    usersList.add(user);
                                                    savedPostsList.add(post);
                                                    savedPostsRecyclerAdapter.notifyDataSetChanged();
                                                    //notify the recycler adapter of the set change
                                                    savedPostsRecyclerAdapter.notifyDataSetChanged();

                                                }

                                            }
                                        });*//*

                                    }
                                });*/
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

    private boolean isLoggedIn() {
        //determine if user is logged in
        return mAuth.getCurrentUser() != null;
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
                .setIcon(getActivity().getDrawable(R.drawable.ic_action_alert))
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
}
