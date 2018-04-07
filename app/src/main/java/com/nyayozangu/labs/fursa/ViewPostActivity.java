package com.nyayozangu.labs.fursa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.sql.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostActivity extends AppCompatActivity {


    private static final String TAG = "Sean";


    private ImageView viewPostImage;
    private FloatingActionButton viewPostActionsFAB;
    private ImageView closeButton;
    private TextView descTextView;
    private TextView timeTextView;
    private TextView likesTextView;
    private TextView priceTextView;
    private TextView locationTextView;
    private CircleImageView userImage; //image of user who posted post

    //progress
    private ProgressDialog progressDialog;


    // TODO: 4/7/18 handle populating data from postId
    // TODO: 4/7/18 if user is not logged in change the comment hint to "login to comment" and send user to login page when comments are clicked

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        //initialize items
        descTextView = findViewById(R.id.viewPostDescTextView);
        closeButton = findViewById(R.id.viewPostCloseImageView);
        timeTextView = findViewById(R.id.viewPostTimeTextView);
        priceTextView = findViewById(R.id.viewPostPriceTextView);
        locationTextView = findViewById(R.id.viewPostLocationcTextView);
        viewPostImage = findViewById(R.id.viewPostImageView);
        likesTextView = findViewById(R.id.viewPostLikesTextView);
        userImage = findViewById(R.id.viewPostUserImage);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        //get the sent intent
        Intent getPostIdIntent = getIntent();
        final String postId = getPostIdIntent.getStringExtra("postId");
        Log.d(TAG, "postId is: " + postId);


        db.collection("Posts").document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                Log.d(TAG, "at view post query");
                //show progress
                showProgress("Loading...");
                //check if post exists
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "Post  exist");
                    final Posts post = documentSnapshot.toObject(Posts.class).withId(postId);
                    //set items
                    //set title

                    //set the description
                    String desc = post.getDesc();
                    descTextView.setText(desc);

                    //set the time
                    long millis = post.getTimestamp().getTime();
                    String dateString = DateFormat.format("EEE, MMM d, ''yy - h:mm a", new Date(millis)).toString();
                    timeTextView.setText(dateString);

                    //set post image

                    //add the placeholder image
                    String postImageUri = post.getImage_url();
                    String postThumbUrl = post.getThumb_url();
                    RequestOptions placeHolderOptions = new RequestOptions();
                    placeHolderOptions.placeholder(R.drawable.ic_action_image_placeholder);

                    Glide.with(getApplicationContext())
                            .applyDefaultRequestOptions(placeHolderOptions)
                            .load(postImageUri)
                            .thumbnail(Glide.with(getApplicationContext()).load(postThumbUrl))
                            .into(viewPostImage);

                    //set likes
                    // TODO: 4/7/18 set likes
                    // TODO: 4/7/18 set price
                    // TODO: 4/7/18 set categories

                    //set user image
                    //query for users and get user details
                    db.collection("Users").document(post.getUser_id()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            //check if user exists
                            if (documentSnapshot.exists()) {
                                Log.d(TAG, "User exist");
                                //user exists
                                //get ID of user hwo posted
                                String userId = post.getUser_id();
                                Log.d(TAG, "UserId is: " + userId);
                                /*Posts post = documentSnapshot.toObject(Posts.class).withId(postId);*/
                                Users user = documentSnapshot.toObject(Users.class).withId(userId);
                                Log.d(TAG, "user is: " + user.toString());
                                //get user image url
                                String userImageUrl = user.getUserImage();
                                Log.d(TAG, "userImageUrl is: " + userImageUrl);

                                //set user image
                                // TODO: 4/7/18 load user thumb instead of image
                                RequestOptions placeHolderOptions = new RequestOptions();
                                placeHolderOptions.placeholder(R.drawable.ic_thumb_person);
                                Glide.with(getApplicationContext())
                                        .applyDefaultRequestOptions(placeHolderOptions)
                                        .load(userImageUrl)
                                        .into(userImage);

                            } else {
                                //user does not exists
                                Log.d(TAG, "user does not exist");
                            }
                        }
                    });


                } else {
                    //post does not exist
                    Log.d(TAG, "Error: post does not exist");
                }
                progressDialog.dismiss();
            }
        });


        //close post button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go back
                finish();
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
