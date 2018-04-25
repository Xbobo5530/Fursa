package com.nyayozangu.labs.fursa.activities.main.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

    private ConstraintLayout promptLoginLayout;
    private Button promptLoginButton;

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

                    String desc = lastVisiblePost.getString("desc");
                    loadMorePosts();
                }

            }
        });


        Query firstQuery = db.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
        //get all posts from the database
        //use snapshotListener to get all the data real time
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
                    //check if an item is added
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        //a new item/ post is added
                        //get the post id for likes feature
                        final String postId = doc.getDocument().getId();

                        final Posts post = doc.getDocument().toObject(Posts.class).withId(postId);

                        //get user id
                        String postUserId = doc.getDocument().getString("user_id");

                        //get userId for post
                        db.collection("Users").document(postUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<DocumentSnapshot> task) {

                                //check if task is successful
                                if (task.isSuccessful()) {

                                    if (isLoggedIn()) {
                                        currentUserId = mAuth.getCurrentUser().getUid();


                                        // TODO: 4/24/18 filter saved posts
                                        filterPosts(task, postId, post);
                                    } else {

                                        //tell user to log in
                                        showLoginAlertDialog(getString(R.string.login_to_view_saved_items_alert_fragment_text));

                                    }

                                } else {

                                    //task failed, faled to get users
                                    Log.d(TAG, "onComplete: failed to get users");

                                }

                            }
                        });
                    }
                }

                //the first page has already loaded
                isFirstPageFirstLoad = false;

            }
        });

        return view;
    }

    private void filterPosts(@NonNull final Task<DocumentSnapshot> task, String postId, final Posts post) {
        db.collection("Posts/" + postId + "/Saves").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
        });
    }

    private void goToLogin() {
        //go to login page
        startActivity(new Intent(getActivity(), LoginActivity.class));
    }


    //for loading more posts
    public void loadMorePosts() {

        Query nextQuery = db.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
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
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            //check if an item is added
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                //a new item/ post is added

                                //get the post id for likes feature
                                final String postId = doc.getDocument().getId();

                                //converting database data into objects
                                //get the newly added post
                                //pass the postId to the post model class Posts.class
                                final Posts post = doc.getDocument().toObject(Posts.class).withId(postId);

                                //get user id
                                String postUserId = doc.getDocument().getString("user_id");

                                //get userId for post
                                db.collection("Users").document(postUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull final Task<DocumentSnapshot> task) {


                                        db.collection("Posts/" + postId + "/Saves").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                        });

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
