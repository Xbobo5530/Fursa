package com.nyayozangu.labs.fursa.activities.comments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.comments.adapters.CommentsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.comments.models.Comments;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.activities.settings.SettingsActivity;
import com.nyayozangu.labs.fursa.notifications.Notify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {


    private static final String TAG = "Sean";
    private ImageView sendButton;
    private EditText chatField;
    private CircleImageView currentUserImage;

    private RecyclerView commentsRecyclerView;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;

    //progress
    private ProgressDialog progressDialog;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String postId;
    private String userId;

    private android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        toolbar = findViewById(R.id.commentsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();


        sendButton = findViewById(R.id.commentsSendBottonImageView);
        chatField = findViewById(R.id.commentsChatEditText);
        currentUserImage = findViewById(R.id.commentsCurrentUserImageView);

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);

        //initiate an arrayList to hold all the posts
        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setHasFixedSize(true);
        commentsRecyclerView.setAdapter(commentsRecyclerAdapter);


        //get the sent intent
        Intent getPostIdIntent = getIntent();
        postId = getPostIdIntent.getStringExtra("postId");
        Log.d(TAG, "postId is: " + postId);

        //go to user profile
        currentUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //go to current user profile
                startActivity(new Intent(CommentsActivity.this, SettingsActivity.class));

            }
        });

        //check if device is connected
        if (isConnected()) {

            db.collection("Posts/" + postId + "/Comments")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                    //check if query is empty
                    if (!queryDocumentSnapshots.isEmpty()) {

                        //create a for loop to check for document changes
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            //check if an item is added
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                //a new comment is added
                                //get the comment id for likes feature
                                String commentId = doc.getDocument().getId();
                                Comments comment = doc.getDocument().toObject(Comments.class);
                                commentsList.add(comment);
                                Log.d(TAG, "onEvent: commentsList is: " + commentsList.toString());
                                commentsRecyclerAdapter.notifyDataSetChanged();
                                commentsRecyclerView.scrollToPosition(commentsList.size() - 1);

                            }
                        }
                    } else {

                        //there are no comments
                        // TODO: 4/13/18 handle there are no comments


                    }

                }
            });


            //inform user to login to comment
            if (isLoggedIn()) {

                //user is logged in
                userId = mAuth.getCurrentUser().getUid();
                //user is logged in
                db.collection("Users").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        //check if user exists
                        if (documentSnapshot.exists()) {

                            //user exists
                            try {

                                //set image
                                String userProfileImageDownloadUrl = documentSnapshot.get("image").toString();
                                RequestOptions placeHolderOptions = new RequestOptions();
                                placeHolderOptions.placeholder(R.drawable.ic_action_person_placeholder);
                                Glide.with(getApplicationContext())
                                        .applyDefaultRequestOptions(placeHolderOptions)
                                        .load(userProfileImageDownloadUrl)
                                        .into(currentUserImage);


                            } catch (NullPointerException noImageFoundException) {

                                currentUserImage.setImageDrawable(getDrawable(R.drawable.ic_action_person_placeholder));
                                Log.d(TAG, "onEvent: error: no thumb found");

                            }

                        }

                    }
                });

                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //check is user has verified email
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        boolean emailVerified = user.isEmailVerified();
                        if (emailVerified
                                || user.getProviders().contains("facebook.com")
                                || user.getProviders().contains("twitter.com")
                                || user.getProviders().contains("google.com")) {

                            //get user comment
                            if (!chatField.getText().toString().isEmpty()) {

                                showProgress("Posting comment...");
                                final String comment = chatField.getText().toString();
                                //generate randomString name for image based on firebase time stamp
                                final String randomCommentId = UUID.randomUUID().toString();
                                //get the user id of the user posing
                                //post a comment
                                db.collection("Posts/" + postId + "/Comments")
                                        .document(randomCommentId)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                //add new comment
                                                final Map<String, Object> commentsMap = new HashMap<>();
                                                commentsMap.put("timestamp", FieldValue.serverTimestamp());
                                                commentsMap.put("comment", comment);
                                                commentsMap.put("user_id", userId);

                                                //upload comment to cloud
                                                db.collection("Posts/" + postId + "/Comments")
                                                        .document(randomCommentId)
                                                        .set(commentsMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                //check if task is successful
                                                                if (task.isSuccessful()) {

                                                                    //subscribe user to post
                                                                    db.collection("Users/" + userId + "/Subscriptions")
                                                                            .document("comments")
                                                                            .collection("Comments")
                                                                            .document(postId)
                                                                            .set(commentsMap);
                                                                    //subscribe user to topic
                                                                    FirebaseMessaging.getInstance().subscribeToTopic(postId); //subscribe to app updates
                                                                    Log.d(TAG, "user subscribed to topic COMMENTS");
                                                                    new Notify().execute("comment_updates", postId); //notify subscribers
                                                                    Log.d(TAG, "onComplete: sending notification");

                                                                } else {

                                                                    showSnack("Failed to post comment: " + task.getResult().toString());

                                                                }

                                                            }
                                                        });

                                                progressDialog.dismiss();

                                            }
                                        });
                                //clear text field
                                chatField.setText("");
                            }
                        } else {

                            //user has not verified email
                            showVerEmailDialog(); //alert user is not verified

                        }

                    }
                });


            } else {

                currentUserImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_person_placeholder));
                //clicking send to go to login with postId intent
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "login button has clicked");
                        String message = "Log in to comment";
                        showLoginAlertDialog(message);
                    }

                });
            }

        } else {

            Log.d(TAG, "onCreate: device is not connected to the internet");

        }


    }


    //setting sub icon on toolbar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (isConnected()) {
            if (isLoggedIn()) {
                userId = mAuth.getCurrentUser().getUid();

                final MenuItem subscribeButton = menu.findItem(R.id.comSubMenuItem);

                db.collection("Users/" + userId + "/Subscriptions").document("comments").collection("Comments").document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        //check if user is already subscribed
                        if (documentSnapshot.exists()) {

                            //set subscribed icon
                            subscribeButton.setIcon(R.drawable.ic_action_subscribed);

                        } else {


                            //user is not subscribed
                            subscribeButton.setIcon(R.drawable.ic_action_subscribe);

                        }

                    }
                });

            }

        }
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.comments_toolbar_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.comSubMenuItem:

                if (isLoggedIn()) {

                    //check if user is already subscribed
                    db.collection("Users/" + userId + "/Subscriptions").document("comments").collection("Comments").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            //check if task is successful
                            if (task.isSuccessful()) {

                                //check if post exists
                                if (task.getResult().exists()) {

                                    //user has already subscribed to current post
                                    //unsubscribe user
                                    db.collection("Users/" + userId + "/Subscriptions").document("comments").collection("Comments").document(postId).delete();
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic(postId);
                                    Log.d(TAG, "user subscribed to topic {CURRENT POST}");


                                } else {

                                    //subscribe user
                                    Map<String, Object> commentsSubMap = new HashMap<>();
                                    commentsSubMap.put("timestamp", FieldValue.serverTimestamp());
                                    //user is not yet subscribed
                                    db.collection("Users/" + userId + "/Subscriptions").document("comments").collection("Comments").document(postId).set(commentsSubMap);
                                    //subscribe to topic
                                    FirebaseMessaging.getInstance().subscribeToTopic(postId);
                                    Log.d(TAG, "user subscribed to topic COMMENTS");
                                    //notify user
                                    String message = "Subscribed to post updates";
                                    showSnack(message);

                                }

                            } else {

                                Log.d(TAG, "onComplete: task failed " + task.getException().getMessage());

                            }

                        }
                    });

                } else {

                    showLoginAlertDialog("Log in to subscribe to comments notifications");

                }
                break;

            case R.id.commentSearchMenuItem:
                // TODO: 4/25/18 handle searching comments
                break;

            default:
                break;

        }
        return true;

    }

    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(CommentsActivity.this);
        loginAlertBuilder.setTitle("Login")
                .setIcon(getDrawable(R.drawable.ic_action_alert))
                .setMessage("You are not logged in\n" + message)
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send user to login activity
                        Intent loginIntent = new Intent(CommentsActivity.this, LoginActivity.class);
                        loginIntent.putExtra("source", "comments");
                        loginIntent.putExtra("postId", postId);
                        startActivity(loginIntent);
                        finish();

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

    private void goToLogin() {
        Intent loginIntent = new Intent(CommentsActivity.this, LoginActivity.class);
        loginIntent.putExtra("postId", postId);
        startActivity(loginIntent);
        finish();
    }


    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.comment_activity_layout),
                message, Snackbar.LENGTH_LONG).show();
    }


    private boolean isLoggedIn() {
        //determine if user is logged in
        return mAuth.getCurrentUser() != null;
    }

    private boolean isConnected() {

        //check if there's a connection
        Log.d(TAG, "at isConnected");
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {

            activeNetwork = cm.getActiveNetworkInfo();

        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }

    private void showVerEmailDialog() {
        android.app.AlertDialog.Builder emailVerBuilder = new android.app.AlertDialog.Builder(CommentsActivity.this);
        emailVerBuilder.setTitle(R.string.email_ver_text)
                .setIcon(R.drawable.ic_action_info_grey) // TODO: 4/27/18 change the black icon to the grey icon
                .setMessage(R.string.verify_to_comment_text)
                .setPositiveButton("Resend Email", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        //send ver email
                        FirebaseUser user = mAuth.getCurrentUser();
                        //show progress
                        String sendEmailMessage = getString(R.string.send_email_text);
                        showProgress(sendEmailMessage);
                        sendVerEmail(dialog, user);
                        //hide progress
                        progressDialog.dismiss();

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
                            AlertDialog.Builder logoutConfirmEmailBuilder = new AlertDialog.Builder(CommentsActivity.this);
                            logoutConfirmEmailBuilder.setTitle(getString(R.string.email_ver_text))
                                    .setIcon(R.drawable.ic_action_info_grey)
                                    .setMessage("A verification email has been sent to your email address.\nLogin after verifying your email to create posts.")
                                    .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            //log use out
                                            //take user to login screen
                                            mAuth.signOut();
                                            startActivity(new Intent(CommentsActivity.this, LoginActivity.class));
                                            finish();

                                        }
                                    }).show();

                        }
                    }
                });
    }

}
