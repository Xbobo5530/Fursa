package com.nyayozangu.labs.fursa.activities.settings;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.posts.CreatePostActivity;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    // TODO: 4/17/18 add delete post feature

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();

    private FloatingActionButton newPostFab;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView myPostsFeed;

    //retrieve posts
    private List<Posts> postsList;
    private List<Users> usersList;

    //recycler adapter
    private PostsRecyclerAdapter postsRecyclerAdapter;

    private DocumentSnapshot lastVisiblePost;

    private Boolean isFirstPageFirstLoad = true;
    private ProgressDialog progressDialog;


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

        //initiate items
        myPostsFeed = findViewById(R.id.myPostsRecyclerView);
        swipeRefresh = findViewById(R.id.myPostsSwipeRefresh);

        //initiate an arrayList to hold all the posts
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();

        //initiate the PostsRecyclerAdapter
        String className = "MyPostsActivity";
        postsRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className);
        myPostsFeed.setLayoutManager(new LinearLayoutManager(MyPostsActivity.this));
        myPostsFeed.setAdapter(postsRecyclerAdapter);

        //initialize items
        newPostFab = findViewById(R.id.myPostsNewPostFab);

        newPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //only allow the user to post if user is signed in
                if (coMeth.isLoggedIn()) {
                    //start the new post activity
                    goToCreatePost();
                } else {
                    String message = getString(R.string.login_to_post_text);

                    //user is not logged in show dialog
                    showLoginAlertDialog(message);
                }

            }
        });

        //loading
        showProgress(getString(R.string.loading_text));

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

        final Query firstQuery = coMeth.getDb()
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);
        loadPosts(firstQuery);

        //handle swipe refresh
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                myPostsFeed.getRecycledViewPool().clear();
                postsList.clear();
                usersList.clear();
                loadPosts(firstQuery);

            }
        });

    }

    private void goToCreatePost() {
        startActivity(new Intent(this, CreatePostActivity.class));
    }

    private void loadPosts(Query firstQuery) {
        firstQuery.addSnapshotListener(MyPostsActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    if (isFirstPageFirstLoad) {

                        //get the last visible post
                        lastVisiblePost = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        postsList.clear();
                        usersList.clear();

                    }

                    //create a for loop to check for document changes
                    for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        //check if an item is added
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String postId = doc.getDocument().getId();
                            Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
                            filterPosts(post);

                        }
                    }

                    //the first page has already loaded
                    isFirstPageFirstLoad = false;

                }

            }
        });
    }

    private void filterPosts(final Posts post) {

        //get current user id
        String currentUserId = coMeth.getUid();
        String postUserId = post.getUser_id();
        //check if is current user's post
        if (postUserId.equals(currentUserId)) {

            //get user_id for post
            coMeth.getDb()
                    .collection("Users")
                    .document(postUserId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            //check if task is successful
                            if (task.isSuccessful()) {

                                Users user = task.getResult().toObject(Users.class);
                                //add new post to the local postsList
                                if (isFirstPageFirstLoad) {

                                    postsList.add(0, post);
                                    usersList.add(0, user);

                                } else {

                                    usersList.add(user);
                                    postsList.add(post);

                                }

                                postsRecyclerAdapter.notifyDataSetChanged();
                            } else {

                                //task failed
                                Log.d(TAG, "onComplete: getting users task failed");

                            }

                            coMeth.stopLoading(progressDialog, swipeRefresh);

                        }
                    });

        } else {

            //user has no posts
            progressDialog.dismiss();
            // TODO: 5/1/18 set no posts view

        }


    }

    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(MyPostsActivity.this);
        loginAlertBuilder.setTitle(R.string.login_text)
                .setIcon(getDrawable(R.drawable.ic_action_red_alert))
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

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    //for loading more posts
    public void loadMorePosts() {

        Query nextQuery = coMeth.getDb()
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(20);


        //get all posts from the database
        //use snapshotListener to get all the data real time
        nextQuery.addSnapshotListener(MyPostsActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                //check if there area more posts
                if (!queryDocumentSnapshots.isEmpty()) {

                    //get the last visible post
                    lastVisiblePost = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);
                    //create a for loop to check for document changes
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        //check if an item is added
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String postId = doc.getDocument().getId();
                            Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
                            filterPosts(post);

                        }
                    }

                }
            }
        });

    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

}
