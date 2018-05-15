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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.CreatePostActivity;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.List;

public class UserPostsActivity extends AppCompatActivity implements View.OnClickListener {

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
    private String userId = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);

        Toolbar toolbar = findViewById(R.id.userPostsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setTitle("User Posts");

        //initiate items
        myPostsFeed = findViewById(R.id.userPostsRecyclerView);
        swipeRefresh = findViewById(R.id.userPostsSwipeRefresh);

        //initiate an arrayList to hold all the posts
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();

        //initiate the PostsRecyclerAdapter
        String className = "UserPostsActivity";
        postsRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className);
        coMeth.handlePostsView(
                UserPostsActivity.this, UserPostsActivity.this, myPostsFeed);
        myPostsFeed.setAdapter(postsRecyclerAdapter);

        //initialize items
        newPostFab = findViewById(R.id.userPostsNewPostFab);

        //get intent
        if (getIntent() != null) {
            handleIntent();
        } else {
            goToMainOnException(getString(R.string.something_went_wrong_text));
        }
        newPostFab.setOnClickListener(this);

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
                    if (getIntent() != null && getIntent().getStringExtra("userId") != null) {
                        userId = getIntent().getStringExtra("userId");
                        loadMorePosts(userId);
                    }
                }
            }
        });
    }

    private void handleIntent() {
        if (getIntent().getStringExtra("userId") != null) {
            userId = getIntent().getStringExtra("userId");
            //process showing fab
            processShowingFab();
            //set title
            setPageTitle();
            //proceed to processing
            final Query firstQuery = coMeth.getDb()
                    .collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(10);
            loadPosts(firstQuery, userId);

            //handle swipe refresh
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    myPostsFeed.getRecycledViewPool().clear();
                    postsList.clear();
                    usersList.clear();
                    loadPosts(firstQuery, userId);

                }
            });
        } else {
            //intent has no user id
            Log.d(TAG, "handleIntent: intent has no userId");
            goToMainOnException(getString(R.string.something_went_wrong_text));
        }
    }

    private void processShowingFab() {
        String currentUserId = coMeth.getUid();
        if (currentUserId != null && currentUserId.equals(userId)) {
            //show fab
            newPostFab.setVisibility(View.VISIBLE);
        } else {
            newPostFab.setVisibility(View.GONE);
        }
    }

    private void setPageTitle() {
        String currentUser = coMeth.getUid();
        if (userId.equals(currentUser)) {
            //set page title
            getSupportActionBar().setTitle(getString(R.string.my_posts_text));
        } else {
            coMeth.getDb()
                    .collection("Users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                //create user object
                                Users user = documentSnapshot.toObject(Users.class);
                                String username = user.getName();
                                //set page title
                                getSupportActionBar().setTitle(username + "\'s " +
                                        getString(R.string.posts_text));
                            } else {
                                //user does not exist
                                goToMainOnException(getString(R.string.user_not_found_text));
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            goToMainOnException(getString(R.string.something_went_wrong_text));
                        }
                    });
        }
    }

    /**
     * open the main activity on exception
     *
     * @param message a message to show to the user on the main activity
     */
    private void goToMainOnException(String message) {
        Intent goToMainIntent = new Intent(
                UserPostsActivity.this, MainActivity.class);
        goToMainIntent.putExtra("action", "notify");
        goToMainIntent.putExtra("message", message);
        startActivity(goToMainIntent);
        finish();
    }

    /**
     * begin the loading posts process
     *
     * @param firstQuery a query to fetch data from db
     */
    private void loadPosts(Query firstQuery, final String userId) {
        firstQuery.addSnapshotListener(UserPostsActivity.this, new EventListener<QuerySnapshot>() {
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
                            filterPosts(post, userId);
                        }
                    }

                    //the first page has already loaded
                    isFirstPageFirstLoad = false;

                }

            }
        });
    }

    private void filterPosts(final Posts post, String userId) {

        //get current user id
        String postUserId = post.getUser_id();
        //check if is current user's post
        if (postUserId.equals(userId)) {

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

                                String postUserId = task.getResult().getId();
                                Users user = task.getResult().toObject(Users.class).withId(postUserId);
                                //add new post to the local postsList
                                if (isFirstPageFirstLoad) {
                                    postsList.add(0, post);
                                    usersList.add(0, user);
                                } else {
                                    usersList.add(user);
                                    postsList.add(post);
                                }
                                postsRecyclerAdapter.notifyDataSetChanged();
                                coMeth.onResultStopLoading(postsList, progressDialog, swipeRefresh);
                            } else {

                                //task failed
                                Log.d(TAG, "onComplete: getting users task failed");
                                coMeth.stopLoading(progressDialog, swipeRefresh);
                            }

                        }
                    });

        } else {

            //user has no posts
            coMeth.stopLoading(progressDialog);
            // TODO: 5/1/18 set no posts view

        }


    }

    //for loading more posts
    public void loadMorePosts(final String userId) {

        Query nextQuery = coMeth.getDb()
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(20);


        //get all posts from the database
        //use snapshotListener to get all the data real time
        nextQuery.addSnapshotListener(UserPostsActivity.this, new EventListener<QuerySnapshot>() {
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
                            filterPosts(post, userId);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.userPostsNewPostFab:
                //check if user is verified
                FirebaseUser user = coMeth.getAuth().getCurrentUser();
                if (user.isEmailVerified() ||
                        user.getProviders().contains("facebook.com") ||
                        user.getProviders().contains("twitter.com") ||
                        user.getProviders().contains("google.com")) {
                    startActivity(new Intent(
                            UserPostsActivity.this, CreatePostActivity.class));
                    finish();
                } else {
                    showVerEmailDialog();
                }
                break;
            default:
                Log.d(TAG, "onClick: user posts click listener on default");
        }
    }

    private void showVerEmailDialog() {
        android.app.AlertDialog.Builder emailVerBuilder =
                new android.app.AlertDialog.Builder(UserPostsActivity.this);
        emailVerBuilder.setTitle(R.string.email_ver_text)
                .setIcon(R.drawable.ic_action_info_grey)
                .setMessage("You have to verify your email address to create a post.")
                .setPositiveButton("Resend Email", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        //send ver email
                        FirebaseUser user = coMeth.getAuth().getCurrentUser();
                        //show progress
                        String sendEmailMessage = getString(R.string.send_email_text);
                        showProgress(sendEmailMessage);
                        sendVerEmail(dialog, user);
                        //hide progress
                        coMeth.stopLoading(progressDialog);

                    }
                })
                .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .show();
    }

    private void sendVerEmail(final DialogInterface dialog, FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "Email sent.");
                            //inform user email is sent
                            //close the
                            dialog.dismiss();
                            AlertDialog.Builder logoutConfirmEmailBuilder = new AlertDialog.Builder(UserPostsActivity.this);
                            logoutConfirmEmailBuilder.setTitle(getString(R.string.email_ver_text))
                                    .setIcon(R.drawable.ic_action_info_grey)
                                    .setMessage("A verification email has been sent to your email address.\nLogin after verifying your email to create posts.")
                                    .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            //log use out
                                            //take user to login screen
                                            coMeth.signOut();
                                            startActivity(new Intent(UserPostsActivity.this, LoginActivity.class));
                                            finish();

                                        }
                                    }).show();

                        }
                    }
                });
    }
}
