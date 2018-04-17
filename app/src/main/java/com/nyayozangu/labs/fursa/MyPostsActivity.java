package com.nyayozangu.labs.fursa;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    // TODO: 4/17/18 add delete post feature

    private static final String TAG = "Sean";

    private FloatingActionButton newPostFab;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView myPostsFeed;

    //retrieve posts
    private List<Posts> postsList;

    //recycler adapter
    private PostsRecyclerAdapter postsRecyclerAdapter;

    private DocumentSnapshot lastVisiblePost;

    private Boolean isFirstPageFirstLoad = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        Toolbar toolbar = findViewById(R.id.myPostsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setTitle("My Posts");

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        //initiate items
        myPostsFeed = findViewById(R.id.myPostsRecyclerView);

        //initiate an arrayList to hold all the posts
        postsList = new ArrayList<>();

        //initiate the PostsRecyclerAdapter
        postsRecyclerAdapter = new PostsRecyclerAdapter(postsList);


        //set a layout manager for myPostsFeed (recycler view)
        myPostsFeed.setLayoutManager(new LinearLayoutManager(MyPostsActivity.this));

        //set an adapter for the recycler view
        myPostsFeed.setAdapter(postsRecyclerAdapter);

        //initialize items
        newPostFab = findViewById(R.id.myPostsNewPostFab);

        newPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //only allow the user to post if user is signed in
                if (isLoggedIn()) {
                    //start the new post activity
                    goToNewPost();
                } else {
                    String message = getString(R.string.login_to_post_text);

                    //user is not logged in show dialog
                    showLoginAlertDialog(message);
                }

            }
        });


        //listen for scrolling on the homeFeedView
        myPostsFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !myPostsFeed.canScrollVertically(1);

                if (reachedBottom) {

                    Log.d(TAG, "at addOnScrollListener\n reached bottom");
                    loadMorePosts();
                }
            }
        });


        Query firstQuery = db.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
        //get all posts from the database
        //use snapshotListener to get all the data real time
        firstQuery.addSnapshotListener(MyPostsActivity.this, new EventListener<QuerySnapshot>() {
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
                    } catch (Exception exception) {
                        Log.d(TAG, "error: " + exception.getMessage());
                    }

                }


                //create a for loop to check for document changes
                for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    //check if an item is added
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        //a new item/ post is added

                        //get the post id for likes feature
                        String postId = doc.getDocument().getId();

                        //converting database data into objects
                        //get the newly added post
                        //pass the postId to the post model class Posts.class
                        final Posts post = doc.getDocument().toObject(Posts.class).withId(postId);

                        // TODO: 4/17/18 filter user posts

                        filterPosts(postId, post);


                    }
                }

                //the first page has already loaded
                isFirstPageFirstLoad = false;

            }
        });

    }

    private void filterPosts(String postId, final Posts post) {
        db.collection("Posts").document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                //check if exists
                if (documentSnapshot.exists()) {

                    //post exists
                    //check user id

                    String postUserId = documentSnapshot.get("user_id").toString();

                    //get current user id
                    String currentUserId = mAuth.getCurrentUser().getUid();

                    //check if is current user's post
                    if (postUserId.equals(currentUserId)) {

                        //is user's post
                        //add new post to the local postsList
                        if (isFirstPageFirstLoad) {
                            //if the first page is loaded the add new post normally
                            postsList.add(post);
                        } else {
                            //add the post at position 0 of the postsList
                            postsList.add(0, post);

                        }
                        //notify the recycler adapter of the set change
                        postsRecyclerAdapter.notifyDataSetChanged();

                    }

                }

            }
        });
    }

    private boolean isLoggedIn() {
        //determine if user is logged in
        return mAuth.getCurrentUser() != null;
    }

    private void goToNewPost() {
        startActivity(new Intent(MyPostsActivity.this, CreatePostActivity.class));
    }

    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(MyPostsActivity.this);
        loginAlertBuilder.setTitle(R.string.login_text)
                .setIcon(getDrawable(R.drawable.ic_action_alert))
                .setMessage(getString(R.string.not_logged_in_text) + message)
                .setPositiveButton(R.string.login_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send user to login activity
                        goToLogin();
                    }
                })
                .setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        dialog.cancel();
                    }
                })
                .show();
    }


    //go to login page
    private void goToLogin() {
        startActivity(new Intent(MyPostsActivity.this, LoginActivity.class));
    }

    //for loading more posts
    public void loadMorePosts() {

        Query nextQuery = db.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(10);


        //get all posts from the database
        //use snapshotListener to get all the data real time
        nextQuery.addSnapshotListener(MyPostsActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

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
                                String postId = doc.getDocument().getId();

                                //converting database data into objects
                                //get the newly added post
                                //pass the postId to the post model class Posts.class
                                Posts post = doc.getDocument().toObject(Posts.class).withId(postId);

                                //filter posts
                                filterPosts(postId, post);

                                /*//add new post to the local postsList
                                postsList.add(post);
                                //notify the recycler adapter of the set change
                                postsRecyclerAdapter.notifyDataSetChanged();*/
                            }
                        }

                    }
                } catch (NullPointerException nullExeption) {
                    //the Query is null
                    Log.e(TAG, "error: " + nullExeption.getMessage());
                }
            }
        });


    }

}
