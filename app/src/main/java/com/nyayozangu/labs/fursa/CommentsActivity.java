package com.nyayozangu.labs.fursa;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

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


        db.collection("Posts/" + postId + "/Comments").addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
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

                        }
                    }
                } else {

                    //there are no comments
                    // TODO: 4/13/18 handle there are no comments


                }

            }
        });



        //inform user to login to comment
        if (isLoggedin()) {

            //user is logged in
            final String userId = mAuth.getCurrentUser().getUid();
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

                            Glide.with(getApplicationContext()).applyDefaultRequestOptions(placeHolderOptions)
                                    .load(userProfileImageDownloadUrl).into(currentUserImage);


                        } catch (NullPointerException noImageFoundException) {

                            currentUserImage.setImageDrawable(getDrawable(R.drawable.ic_action_person_placeholder));

                            Log.d(TAG, "onEvent: error: no thum found");
                        }

                    }

                }
            });

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //get user comment
                    if (!chatField.getText().toString().isEmpty()) {

                        showProgress("Posting comment...");

                        final String comment = chatField.getText().toString();
                        //generate randomString name for image based on firebase time stamp
                        final String randomCommentId = UUID.randomUUID().toString();
                        //get the user id of the user posing
                        final String userId = mAuth.getCurrentUser().getUid();


                        //post a comment
                        db.collection("Posts/" + postId + "/Comments").document(randomCommentId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                //add new comment
                                Map<String, Object> commentsMap = new HashMap<>();
                                commentsMap.put("timestamp", FieldValue.serverTimestamp());
                                commentsMap.put("comment", comment);
                                commentsMap.put("user_id", userId);

                                //upload comment to cloud
                                db.collection("Posts/" + postId + "/Comments").document(randomCommentId).set(commentsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        //chek if task is successful
                                        if (!task.isSuccessful()) {

                                            Snackbar.make(findViewById(R.id.comment_activity_layout),
                                                    "Failed to post comment: " + task.getResult().toString(), Snackbar.LENGTH_SHORT).show();

                                        }

                                    }
                                });

                                progressDialog.dismiss();

                            }
                        });
                        //clear text field
                        chatField.setText("");
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


    }

    private boolean isLoggedin() {
        return mAuth.getCurrentUser() != null;
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

}
