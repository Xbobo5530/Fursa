package com.nyayozangu.labs.fursa.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import com.bumptech.glide.Glide;
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
import com.nyayozangu.labs.fursa.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.models.Posts;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Users;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.ACTION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FACEBOOK_DOT_COM;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.GOOGLE_DOT_COM;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MESSAGE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MY_POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MY_POSTS_DOC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NOTIFY;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SUBSCRIPTIONS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TWITTER_DOT_COM;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;

public class UserPostsActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: 4/17/18 add delete post feature

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();

    private FloatingActionButton newPostFab;
    private List<Posts> postsList;
    private List<Users> usersList;
    private PostsRecyclerAdapter postsRecyclerAdapter;
    private DocumentSnapshot lastVisiblePost;
    private Boolean isFirstPageFirstLoad = true;
    private ProgressDialog progressDialog;
    private String userId = null;
    private RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);

        Toolbar toolbar = findViewById(R.id.userPostsToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setTitle("User posts");

        mRecyclerView = findViewById(R.id.userPostsRecyclerView);

        postsList = new ArrayList<>();
        usersList = new ArrayList<>();
        String className = "UserPostsActivity";
        postsRecyclerAdapter =
                new PostsRecyclerAdapter(postsList, usersList, className,
                        Glide.with(this), this);
        coMeth.handlePostsView(
                UserPostsActivity.this, UserPostsActivity.this, mRecyclerView);
        mRecyclerView.setAdapter(postsRecyclerAdapter);
        newPostFab = findViewById(R.id.userPostsNewPostFab);

        //get intent
        handleIntent();
        if (!coMeth.isConnected()){
            coMeth.stopLoading(progressDialog);
            showSnack(getResources().getString(R.string.failed_to_connect_text));
        }
        newPostFab.setOnClickListener(this);
        toolbar.setOnClickListener(this);

    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(CoMeth.USER_ID) != null) {
                showProgress(getString(R.string.loading_text));
                userId = intent.getStringExtra(CoMeth.USER_ID);
                processShowingFab();
                String currentUser = coMeth.getUid();
                if (userId.equals(currentUser)) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.my_posts_text));
                } else {
                    if (intent.getStringExtra(CoMeth.USERNAME) != null) {
                        String username = intent.getStringExtra(CoMeth.USERNAME);
                        String pageTitle = username + "'s posts";
                        Objects.requireNonNull(getSupportActionBar()).setTitle(pageTitle);
                    }
                }
                loadPosts(userId);
            } else {
                //intent has no user id
                goToMainOnException(getString(R.string.something_went_wrong_text));
            }
        }else{
            goToMainOnException(getString(R.string.something_went_wrong_text));
        }
    }

    private void loadPosts(final String userId) {
        coMeth.getDb()
                .collection(USERS + "/" + userId + "/" + SUBSCRIPTIONS)
                .document(MY_POSTS_DOC).collection(MY_POSTS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e == null) {
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
                        }else{
                            Log.d(TAG, "onEvent: \nerror loading posts: " + e.getMessage());
                            showSnack(getResources().getString(R.string.failed_to_load_posts) + ": "
                                    + e.getMessage());
                        }
                    }
                });
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.userPostLayout), message, Snackbar.LENGTH_LONG).show();
    }

    private void retrievePost(final String postId, final String userId) {
        coMeth.getDb().collection(POSTS).document(postId).get()
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
                .collection(USERS).document(userId).get()
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
                            coMeth.stopLoading(progressDialog);
                            Log.d(TAG, "onSuccess: added post and user");
                        } else {
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
        coMeth.getDb().collection(USERS + "/" + userId + "/"
                + SUBSCRIPTIONS + "/" + MY_POSTS_DOC + "/" +MY_POSTS).document(postId)
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

    /**
     * open the main activity on exception
     * @param message a message to show to the user on the main activity
     */
    private void goToMainOnException(String message) {
        Intent goToMainIntent = new Intent(
                UserPostsActivity.this, MainActivity.class);
        goToMainIntent.putExtra(ACTION , NOTIFY);
        goToMainIntent.putExtra(MESSAGE, message);
        startActivity(goToMainIntent);
        finish();
    }

    private void showProgress(String message) {
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
                if (Objects.requireNonNull(user).isEmailVerified() ||
                        Objects.requireNonNull(user.getProviders()).contains(FACEBOOK_DOT_COM ) ||
                        user.getProviders().contains(TWITTER_DOT_COM ) ||
                        user.getProviders().contains(GOOGLE_DOT_COM)) {
                    startActivity(new Intent(
                            UserPostsActivity.this, CreatePostActivity.class));
                    finish();
                } else {
                    showVerEmailDialog();
                }
                break;

            case R.id.userPostsToolbar:
                mRecyclerView.smoothScrollToPosition(0);
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
                        if (user != null) {
                            String sendEmailMessage = getString(R.string.send_email_text);
                            showProgress(sendEmailMessage);
                            sendVerEmail(dialog, user);
                            //hide progress
                            coMeth.stopLoading(progressDialog);
                        }

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
                            dialog.dismiss();
                            AlertDialog.Builder logoutConfirmEmailBuilder = new AlertDialog.Builder(UserPostsActivity.this);
                            logoutConfirmEmailBuilder.setTitle(getString(R.string.email_ver_text))
                                    .setIcon(R.drawable.ic_action_info_grey)
                                    .setMessage("A verification email has been sent to your email address.\nLogin after verifying your email to create posts.")
                                    .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

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
