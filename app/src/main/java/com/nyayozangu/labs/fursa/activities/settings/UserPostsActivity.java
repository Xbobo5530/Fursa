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

import javax.annotation.Nullable;

public class UserPostsActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: 4/17/18 add delete post feature

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();

    private FloatingActionButton newPostFab;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView userPostsFeed;

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
        userPostsFeed = findViewById(R.id.userPostsRecyclerView);
        swipeRefresh = findViewById(R.id.userPostsSwipeRefresh);

        //initiate an arrayList to hold all the posts
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();

        //initiate the PostsRecyclerAdapter
        String className = "UserPostsActivity";
        postsRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className);
        coMeth.handlePostsView(
                UserPostsActivity.this, UserPostsActivity.this, userPostsFeed);
        userPostsFeed.setAdapter(postsRecyclerAdapter);

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
        /*userPostsFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean reachedBottom = !userPostsFeed.canScrollVertically(1);
                if (reachedBottom) {
                    Log.d(TAG, "at addOnScrollListener\n reached bottom");
                    if (getIntent() != null && getIntent().getStringExtra("userId") != null) {
                        userId = getIntent().getStringExtra("userId");
                        loadMorePosts(userId);
                    }
                }
            }
        });*/
    }

    private void handleIntent() {
        if (getIntent().getStringExtra("userId") != null) {
            userId = getIntent().getStringExtra("userId");
            //process showing fab
            processShowingFab();
            //set title
            setPageTitle();
            //proceed to processing
            /*final Query firstQuery = coMeth.getDb()
                    .collection("Posts");
//                    .orderBy("timestamp", Query.Direction.DESCENDING);
            loadPosts(firstQuery, userId);*/

            loadPosts(userId);

            //handle swipe refresh
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    userPostsFeed.getRecycledViewPool().clear();
                    postsList.clear();
                    usersList.clear();
                    loadPosts(userId);

                }
            });
        } else {
            //intent has no user id
            Log.d(TAG, "handleIntent: intent has no userId");
            goToMainOnException(getString(R.string.something_went_wrong_text));
        }
    }

    private void loadPosts(final String userId) {
        coMeth.getDb()
                .collection("Users/" + userId + "/Subscriptions")
                .document("my_posts")
                .collection("MyPosts")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //create a for loop to check for document changes
                            for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                //check if an item is added
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    final String postId = doc.getDocument().getId();
                                    //retrieve post from database
                                    retrievePost(postId, userId);

                                }
                            }
                        }
                    }
                });
    }

    private void retrievePost(final String postId, final String userId) {
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //post exists
                            final Posts post = documentSnapshot.toObject(Posts.class).withId(postId);
                            //get the user details
                            getTheUserDetails(post, userId);

                        } else {
                            //post does not exist
                            //update my posts
                            //delete my post entry
                            updateMyPosts(userId, postId);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: faied to fetch post\n" +
                                e.getMessage());
                    }
                });
    }

    private void getTheUserDetails(final Posts post, final String userId) {
        coMeth.getDb()
                .collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //user exists
                            //convert user to object
                            Users user = documentSnapshot.toObject(Users.class).withId(userId);
                            postsList.add(post);
                            usersList.add(user);
                            postsRecyclerAdapter.notifyDataSetChanged();
                            coMeth.stopLoading(progressDialog, swipeRefresh);
                            Log.d(TAG, "onSuccess: added post and user");
                        } else {
                            //user does not exist
                            //notify user
//                                                                           showSnack("User does not exist");
                            Log.d(TAG, "onSuccess: user does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get get user details\n" +
                                e.getMessage());
                    }
                });
    }

    private void updateMyPosts(String userId, String postId) {
        Log.d(TAG, "updateMyPosts: ");
        coMeth.getDb()
                .collection("Users/" + userId + "/Subscriptions/my_posts/MyPosts")
                .document(postId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: updated my posts list");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update my posts\n" +
                                e.getMessage());
                    }
                });
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
