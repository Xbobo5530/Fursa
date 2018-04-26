package com.nyayozangu.labs.fursa.activities.main.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
        /*savedPostsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean reachedBottom = !savedPostsView.canScrollVertically(1);
                if (reachedBottom) {

                    Log.d(TAG, "at addOnScrollListener\n reached bottom");
                    loadMorePosts();

                }

            }
        });*/

        if (isLoggedIn()) {

            currentUserId = mAuth.getCurrentUser().getUid();
        }

        Log.d(TAG, "onCreateView: \ncurrentUserId is: " + currentUserId);

        db.collection("Users/" + currentUserId + "/Subscriptions")
                .document("saved_posts")
                .collection("SavedPosts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                final String postId = doc.getDocument().getId();
                                db.collection("Posts")
                                        .document(postId)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                //check if task is successful
                                                if (task.isSuccessful()) {

                                                    if (task.getResult().exists()) {

                                                        final Posts post = task.getResult().toObject(Posts.class).withId(postId);
                                                        String postUserId = post.getUser_id();
                                                        db.collection("Users")
                                                                .document(postUserId)
                                                                .get()
                                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                                        if (task.isSuccessful()) {

                                                                            Users user = task.getResult().toObject(Users.class);
                                                                            savedPostsList.add(post);
                                                                            usersList.add(user);
                                                                            savedPostsRecyclerAdapter.notifyDataSetChanged();

                                                                        } else {

                                                                            Log.d(TAG, "onComplete: task filed\n" + task.getException());

                                                                        }

                                                                    }
                                                                });

                                                    } else {

                                                        Log.d(TAG, "onComplete: post does not exist");

                                                    }

                                                } else {

                                                    Log.d(TAG, "onComplete: task for getting posts failed\n" + task.getException());
                                                }

                                            }
                                        });

                            }

                        } else {

                            Log.d(TAG, "onEvent: user has no liked posts");

                        }

                    }
                });


        /*Query firstQuery = db
                .collection("Users/" + currentUserId + "/Subscriptions").document("saved_posts")
                .collection("SavedPosts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);
        //get all posts from the database
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
                        db.collection("Posts")
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

                                        db.collection("Users")
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


                    }
                }

                //the first page has already loaded
                isFirstPageFirstLoad = false;

            }
        });*/

        return view;
    }

    private void goToLogin() {
        //go to login page
        startActivity(new Intent(getActivity(), LoginActivity.class));
    }


    //loading more posts
    public void loadMorePosts() {

        Log.d(TAG, "loadMorePosts: ");
        Query nextQuery = db
                .collection("Users/" + currentUserId + "/Subscriptions").document("saved_posts")
                .collection("SavedPosts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);

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
                                db.collection("Posts")
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

                                                db.collection("Users")
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
