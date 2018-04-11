package com.nyayozangu.labs.fursa;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {


    // TODO: 4/10/18 fix the pagenation on comments page


    private static final String TAG = "Sean";
    private ImageView sendButton;
    private EditText chatField;
    private CircleImageView currentUserImage;

    private RecyclerView commentsRecyclerView;
    //recycler adapter
    private CommentsRecyclerAdapter commentsRecyclerAdapter;

    //retrieve posts
    private List<Comments> commentsList;

    //progress
    private ProgressDialog progressDialog;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String postId;
    private boolean isFirstPageFirstLoad = true;
    private DocumentSnapshot lastVisibleComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);


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

        //initiate the PostsRecyclerAdapter
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);

        //set a layout manager for homeFeedView (recycler view)
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(CommentsActivity.this));

        //set an adapter for the recycler view
        commentsRecyclerView.setAdapter(commentsRecyclerAdapter);

        //get the sent intent
        Intent getPostIdIntent = getIntent();
        postId = getPostIdIntent.getStringExtra("postId");
        Log.d(TAG, "postId is: " + postId);


        //listen for scrolling on the homeFeedView
        commentsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedTop = !commentsRecyclerView.canScrollVertically(-1);

                if (reachedTop) {

                    Log.d(TAG, "at addOnScrollListener\n reached bottom");
                    loadMoreComments();
                }
            }
        });


        Query firstQuery = db.collection("Posts/" + postId + "/Comments").orderBy("timestamp", Query.Direction.ASCENDING).limit(10);

        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                if (isFirstPageFirstLoad) {

                    //get the last visible post
                    try {
                        lastVisibleComment = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                    } catch (Exception exception) {
                        Log.d(TAG, "error: " + exception.getMessage());
                    }

                }


                //create a for loop to check for document changes
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    //check if an item is added
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        //a new item/ post is added


                        //get the post id for likes feature
                        String commentId = doc.getDocument().getId();

                        //converting database data into objects
                        //get the newly added post
                        //pass the postId to the post model class Posts.class
                        Comments comment = doc.getDocument().toObject(Comments.class).withId(commentId);

                        //add new post to the local postsList
                        if (isFirstPageFirstLoad) {
                            //if the first page is loaded the add new post normally
                            commentsList.add(comment);
                        } else {
                            //add the post at position 0 of the postsList
                            commentsList.add(0, comment);

                        }
                        Log.d(TAG, "onEvent: commentsList is: " + commentsList.toString());
                        //notify the recycler adapter of the set change
                        commentsRecyclerAdapter.notifyDataSetChanged();
                    }
                }

                //the first page has already loaded
                isFirstPageFirstLoad = false;

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

                            //in case user has no thumb
                            String thumbUrl = documentSnapshot.get("thumb").toString();
                            RequestOptions placeHolderOptions = new RequestOptions();
                            placeHolderOptions.placeholder(R.drawable.ic_thumb_person);

                            Glide.with(getApplicationContext()).applyDefaultRequestOptions(placeHolderOptions)
                                    .load(thumbUrl).into(currentUserImage);


                        } catch (NullPointerException noImageFoundException) {

                            currentUserImage.setImageDrawable(getDrawable(R.drawable.ic_thumb_person));

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
                                db.collection("Posts/" + postId + "/Comments").document(randomCommentId).set(commentsMap);

                            }
                        });
                        //clear text field
                        chatField.setText("");
                    }

                }
            });


        } else {

            currentUserImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_person));
            chatField.setHint("Log in to post a comment, click button to login");
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

    private void loadMoreComments() {

        Query nextQuery = db.collection("Posts/" + postId + "/Comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .startAfter(lastVisibleComment)
                .limit(10);


        //get all comments from the database
        //use snapshotListener to get all the data real time
        nextQuery.addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                try {
                    //check if there area more posts
                    if (!queryDocumentSnapshots.isEmpty()) {


                        //get the last visible comment
                        lastVisibleComment = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);


                        //create a for loop to check for document changes
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            //check if an item is added
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                //a new item/ comment is added

                                //get the post id for likes feature
                                String commentId = doc.getDocument().getId();

                                //converting database data into objects
                                //get the newly added comment
                                //pass the commentId to the comment model class Comment.class
                                Comments comment = doc.getDocument().toObject(Comments.class).withId(commentId);

                                //add new post to the local postsList
                                commentsList.add(comment);
                                //notify the recycler adapter of the set change
                                commentsRecyclerAdapter.notifyDataSetChanged();
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
}
