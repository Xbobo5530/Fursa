package com.nyayozangu.labs.fursa.activities.comments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.comments.adapters.CommentsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.comments.models.Comments;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.ViewPostActivity;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.notifications.Notify;
import com.nyayozangu.labs.fursa.users.UserPageActivity;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: 6/14/18 for view reward  notifications use viewsRewardPostId as topic
    // TODO: 6/14/18 add reply to comment

    private static final String TAG = "Sean";
    private ImageView sendButton;
    private EditText chatField;
    private CircleImageView currentUserImage, postUserImage;
    private ConstraintLayout postDetailsLayout;
    private TextView postUsernameField, postTitleField;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView commentsRecyclerView;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;

    //common methods
    private CoMeth coMeth;

    //progress
    private ProgressDialog progressDialog;

    private String postId;
    private String currentUserId;

    private android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        //common methods
        coMeth = new CoMeth();
        //initiate items
        postUserImage = findViewById(R.id.commentsPostUserImageView);
        postUsernameField = findViewById(R.id.commentsPostUsernameTextView);
        postTitleField = findViewById(R.id.commentsPostTitleTextView);
        toolbar = findViewById(R.id.commentsToolbar);
        sendButton = findViewById(R.id.commentsSendBottonImageView);
        chatField = findViewById(R.id.commentsChatEditText);
        currentUserImage = findViewById(R.id.commentsCurrentUserImageView);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.commentsSwipeRefresh);
        postDetailsLayout = findViewById(R.id.commentpostDetailsConstraintLayout);

        //initiate an arrayList to hold all the posts
        commentsList = new ArrayList<>();

        //get post id
        if (getIntent() != null &&
                getPostIdFromIntent(getIntent()) != null) {
            postId = getPostIdFromIntent(getIntent());
        }

        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList, postId);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setHasFixedSize(true);
        commentsRecyclerView.setAdapter(commentsRecyclerAdapter);

        //handle toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.comments_text));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //get the sent intent
        handleIntent();

        //add register view
        updateViews(postId);

        //handle clicks
        postDetailsLayout.setOnClickListener(this);
        postUserImage.setOnClickListener(this);
        postTitleField.setOnClickListener(this);
        currentUserImage.setOnClickListener(this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                commentsRecyclerView.getRecycledViewPool().clear();
                commentsList.clear();
                retrieveComments();
            }
        });

//        //retrieve comments on reach bottom
//        commentsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                Boolean reachedBottom = !commentsRecyclerView.canScrollVertically(1);
//                if (reachedBottom) {
//                    //retrieve comments on reach bottom
//                    retrieveComments();
//                }
//            }
//        });

        //check if device is connected
        if (coMeth.isConnected()) {
            setPostDetails();
            retrieveComments();
            if (coMeth.isLoggedIn()) {
                setUserDetails();
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postComment();
                    }
                });
            } else {
                currentUserImage.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_action_person_placeholder));
                //clicking send to go to login with postId intent
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "login button has clicked");
                        goToLogin(getString(R.string.login_to_comment));
                    }

                });
            }

        } else {
            Log.d(TAG, "onCreate: device is not connected to the internet");
        }
    }

    private void updateViews(final String postId) {
        Log.d(TAG, "updateViews: ");
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //post exists
                            //create post object
                            Posts post = documentSnapshot.toObject(Posts.class);
                            int views = post.getViews();
                            String postUserId = post.getUser_id();
                            if (!coMeth.isLoggedIn() ||
                                    (coMeth.isLoggedIn() && !coMeth.getUid().equals(postUserId))) {
                                //current viewer is either not logged in or not current logged in user
                                //create Map
                                addNewView(views, postId);
                            }
                        } else {
                            //post does not exist
                            goToMain(getResources().getString(R.string.post_not_found_text));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to access db to get views for comments view\n" +
                                e.getMessage());
                    }
                });
    }

    private void addNewView(int views, String postId) {
        Log.d(TAG, "addNewView: ");
        Map<String, Object> viewsMap = new HashMap<>();
        viewsMap.put("views", views + 1);
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .update(viewsMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: views from comments updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update views from comments\n" +
                                e.getMessage());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get views from db for comments view\n" +
                                e.getMessage());
                    }
                });
    }


    private void handleIntent() {
        if (getIntent() != null) {
            Intent getPostIdIntent = getIntent();
            postId = getPostIdFromIntent(getPostIdIntent);
            Log.d(TAG, "postId is: " + postId);
        } else {
            //intent is empty
            //go to main with waring
            goToMain(getResources().getString(R.string.something_went_wrong_text));
        }
    }

    private String getPostIdFromIntent(Intent intent) {
        if (getIntent() != null &&
                getIntent().getStringExtra("postId") != null) {
            return intent.getStringExtra("postId");
        } else {
            return null;
        }
    }

    private void goToMain(String message) {
        Intent goToMainIntent = new Intent(this, MainActivity.class);
        goToMainIntent.putExtra(getString(R.string.ACTION_NAME), getString(R.string.notify_value_text));
        goToMainIntent.putExtra(getString(R.string.MESSAGE_NAME), message);
        startActivity(goToMainIntent);
        finish();
    }

    private void postComment() {

        //check is user has verified email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean emailVerified = user.isEmailVerified();
        if (emailVerified
                || user.getProviders().contains("facebook.com")
                || user.getProviders().contains("twitter.com")
                || user.getProviders().contains("google.com")) {

            //get user comment
            if (!chatField.getText().toString().trim().isEmpty()) {

                //hide keyboard
                // TODO: 6/16/18 review hide keyboard user experience
//                hideKeyBoard();
                //show progress
                showProgress(getString(R.string.posting_comment_text));
                final String comment = chatField.getText().toString().trim();
                //generate randomString name for image based on firebase time stamp
//                final String randomCommentId = UUID.randomUUID().toString();
                //get the user id of the user posing

                //add new comment
                final Map<String, Object> commentsMap = new HashMap<>();
                commentsMap.put("timestamp", FieldValue.serverTimestamp());
                commentsMap.put("comment", comment);
                commentsMap.put("user_id", currentUserId);
                //post a comment
                coMeth.getDb()
                        .collection("Posts/" + postId + "/Comments")
                        .add(commentsMap)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                //comment has been posted
                                Log.d(TAG, "onComplete: subscribing user to post");
                                //subscribe user to topic
                                FirebaseMessaging.getInstance().subscribeToTopic(postId);
                                //notify subscribers
                                Log.d(TAG, "onComplete: notifying user");
                                new Notify().execute("comment_updates", postId);
                                Log.d(TAG, "onSuccess: ");
                                coMeth.stopLoading(progressDialog, swipeRefreshLayout);
                                addCommentRef();
                                //get latest comments
                                // TODO: 6/8/18 test new comments
//                                retrieveComments();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: failed to post comment\n" +
                                        e.getMessage());
                                showSnack(getResources().getString(R.string.failed_to_comment_text)
                                        + ": " + e.getMessage());
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

    private void setUserDetails() {
        //user is logged in
        currentUserId = coMeth.getUid();
        //user is logged in
        coMeth.getDb()
                .collection("Users")
                .document(currentUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        //check if user exists
                        if (documentSnapshot.exists()) {

                            //user exists
                            try {

                                //set image
                                String userProfileImageDownloadUrl =
                                        documentSnapshot.get("image").toString();
                                coMeth.setImage(R.drawable.ic_action_person_placeholder,
                                        userProfileImageDownloadUrl,
                                        currentUserImage);


                            } catch (NullPointerException noImageFoundException) {

                                currentUserImage.setImageDrawable(getResources()
                                        .getDrawable(R.drawable.ic_action_person_placeholder));
                                Log.d(TAG, "onEvent: error: no thumb found");

                            }

                        }

                    }
                });
    }

    private void setPostDetails() {
        //set post details
        //access posts and get post user id
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //post exists
                            Posts post = documentSnapshot.toObject(Posts.class);
                            final String postUserId = post.getUser_id();
                            //on click user image
                            postUserImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToUserPage(postUserId);
                                }
                            });
                            //set post details
                            String postTitle = post.getTitle();
                            postTitleField.setText(postTitle);

                            //access users to get user details
                            getUserData(postUserId);
                        } else {
                            //post does not exist
                            //go to main
                            goToMain(getString(R.string.post_not_found_text));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get post: " + e.getMessage());
                        showSnack(getString(R.string.something_went_wrong_text));
                    }
                });
    }

    private void getUserData(String postUserId) {
        coMeth.getDb()
                .collection("Users")
                .document(postUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Users user = documentSnapshot.toObject(Users.class);
                            String username = user.getName();
                            //set username
                            postUsernameField.setText(username);
                            //set user image
                            String userImageDownloadUrl = user.getImage();
                            String userThumbDownloadUrl = user.getThumb();
                            if (userThumbDownloadUrl != null) {
                                coMeth.setImage(
                                        R.drawable.ic_action_person_placeholder,
                                        userThumbDownloadUrl,
                                        postUserImage);
                            } else {
                                coMeth.setImage(
                                        R.drawable.ic_action_person_placeholder,
                                        userImageDownloadUrl,
                                        postUserImage);
                            }
                        } else {
                            //user does not exist
                            showSnack(getString(R.string.something_went_wrong_text));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " +
                                "\nfailed to get user details:" +
                                e.getMessage());
                    }
                });
    }

    private void retrieveComments() {
        Log.d(TAG, "retrieveComments: ");
        coMeth.getDb()
                .collection("Posts/" + postId + "/Comments")
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
                                    String commentId = doc.getDocument().getId();
                                    Comments comment =
                                            doc.getDocument().toObject(Comments.class).withId(commentId);
                                    commentsList.add(comment);
                                    commentsRecyclerAdapter.notifyDataSetChanged();
                                    commentsRecyclerView.scrollToPosition(commentsList.size() - 1);
                                }
                            }
                        } else {
                            //there are no comments
                            Log.d(TAG, "onEvent: post has no comments");
                        }
                        coMeth.stopLoading(progressDialog, swipeRefreshLayout);
                    }
                });
    }

    private void hideKeyBoard() {

        Log.d(TAG, "hideKeyBoard: ");
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.d(TAG, "onClick: exception on hiding keyboard " + e.getMessage());
        }
    }


    //setting sub icon on toolbar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setSubscribeIconStatus(menu);
        return true;

    }

    private void setSubscribeIconStatus(Menu menu) {
        if (coMeth.isConnected()) {
            if (coMeth.isLoggedIn()) {
                currentUserId = coMeth.getUid();

                final MenuItem subscribeButton = menu.findItem(R.id.comSubMenuItem);
                coMeth.getDb()
                        .collection("Users/" + currentUserId + "/Subscriptions")
                        .document("comments").collection("Comments")
                        .whereEqualTo("post_id", postId)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.d(TAG, "onEvent: error checking sub status");
                                } else {

                                    if (queryDocumentSnapshots.isEmpty()) {
                                        //user is not subscribed
                                        subscribeButton.setIcon(R.drawable.ic_action_subscribe);
                                    } else {
                                        //set subscribed icon
                                        subscribeButton.setIcon(R.drawable.ic_action_subscribed);
                                    }
                                }
                            }
                        });


//                        .document(postId)
//                        .get()
//                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                            @Override
//                            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                if (documentSnapshot.exists()){
//                                    //set subscribed icon
//                                    subscribeButton.setIcon(R.drawable.ic_action_subscribed);
//                                }else{
//                                    //user is not subscribed
//                                    subscribeButton.setIcon(R.drawable.ic_action_subscribe);
//                                }
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.d(TAG, "onFailure: failed to get subscription status\n" +
//                                        e.getMessage());
//                            }
//                        });
                //set the sub icon to subd if current user is post user
                // because there's no ref ..... (post users are always subd to their own posts)
//                coMeth.getDb()
//                        .collection("Posts")
//                        .document(postId)
//                        .get()
//                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                                if (task.isSuccessful() && task.getResult().exists()) {
//
//                                    //check if postUserId equals current currentUserId
//                                    String currentUserId = coMeth.getUid();
//                                    Posts post = task.getResult().toObject(Posts.class);
//                                    String postUserId = post.getUser_id();
//                                    if (currentUserId.equals(postUserId)) {
//
//                                        //post is current user's post
//                                        //set subscribed icon
//                                        subscribeButton.setIcon(R.drawable.ic_action_subscribed);
//
//                                    }
//
//                                } else {
//
//                                    if (!task.isSuccessful()) {
//
//                                        Log.d(TAG, "onComplete: getting post for comments task failed");
//
//                                    }
//                                    if (!task.getResult().exists()) {
//
//                                        Log.d(TAG, "onComplete: post does not exists");
//
//                                    }
//
//                                }
//
//                            }
//                        });

            }

        }
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
                subscribe();
                break;
            default:
                break;

        }
        return true;
    }

    /**
     * subscribe current user to post comments
     */
    private void subscribe() {
        if (coMeth.isLoggedIn()) {

            //check if user is already subscribed
            coMeth.getDb()
                    .collection("Users/" + currentUserId + "/Subscriptions")
                    .document("comments")
                    .collection("Comments")
                    .document(postId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                //user is already subscribed, unsubscribe
                                //unsubscribe user
                                CollectionReference commentsSubRef = coMeth.getDb().collection(
                                        "Users/" + currentUserId +
                                                "/Subscriptions/comments/Comments");
                                commentsSubRef.document(postId).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //change the icon
                                                Log.d(TAG, "onSuccess: user has been unsubd");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: failed to unsub " +
                                                        "user from comments\n" + e.getMessage());
                                                //alert user
                                                showSnack(getResources().getString(R.string.error_text)
                                                        + ": " + e.getMessage());
                                            }
                                        });

                                CollectionReference commentsCollRef = coMeth.getDb().collection(
                                        CoMeth.USERS + "/" + currentUserId + "/" +
                                                CoMeth.SUBSCRIPTIONS)
                                        .document("comments")
                                        .collection("Comments");
                                commentsCollRef.document(postId).delete();
                                FirebaseMessaging.getInstance().unsubscribeFromTopic(postId);
                                Log.d(TAG, "user subscribed to topic {CURRENT POST}");
                            } else {
                                //subscribe user
                                addCommentRef();
                                //notify user
                                showSnack(getResources().getString(R.string.subd_to_post_text));
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: failed to sub user\n" + e.getMessage());
                            showSnack(getResources().getString(R.string.error_text)
                                    + ": " + e.getMessage());
                        }
                    });

        } else {
            goToLogin(getString(R.string.login_to_sub_comments));
        }
    }

    /**
     * add a comment refference to user comments subs
     */
    private void addCommentRef() {
        Map<String, Object> commentsSubMap = new HashMap<>();
        commentsSubMap.put("post_id", postId);
        commentsSubMap.put("timestamp", FieldValue.serverTimestamp());
        //user is not yet subscribed
        coMeth.getDb()
                .collection("Users/" + currentUserId + "/Subscriptions")
                .document("comments")
                .collection("Comments")
                .document(postId)
                .set(commentsSubMap);
        //subscribe to topic
        FirebaseMessaging.getInstance().subscribeToTopic(postId);
        Log.d(TAG, "user subscribed to topic COMMENTS");
    }

    private void goToLogin(String message) {
        Intent loginIntent = new Intent(CommentsActivity.this, LoginActivity.class);
        loginIntent.putExtra("message", message);
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

    private void showVerEmailDialog() {
        android.app.AlertDialog.Builder emailVerBuilder =
                new android.app.AlertDialog.Builder(CommentsActivity.this);
        emailVerBuilder.setTitle(R.string.email_ver_text)
                .setIcon(R.drawable.ic_action_info_grey)
                .setMessage(R.string.verify_to_comment_text)
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
                            AlertDialog.Builder logoutConfirmEmailBuilder =
                                    new AlertDialog.Builder(CommentsActivity.this);
                            logoutConfirmEmailBuilder.setTitle(getString(R.string.email_ver_text))
                                    .setIcon(R.drawable.ic_action_info_grey)
                                    .setMessage(R.string.verification_email_message_text)
                                    .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            //log use out
                                            //take user to login screen
                                            coMeth.signOut();
                                            startActivity(
                                                    new Intent(CommentsActivity.this,
                                                            LoginActivity.class));
                                            finish();

                                        }
                                    }).show();

                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.commentsPostTitleTextView:
                goToPost();
                break;
            case R.id.commentsPostUsernameTextView:
                goToPost();
                break;
            case R.id.commentsCurrentUserImageView:
                //go to current user profile
                if (coMeth.isLoggedIn()) {
                    goToUserPage(coMeth.getUid());
                } else {
                    goToLogin();
                }
                break;
            default:
                Log.d(TAG, "onClick: at default");
        }
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void goToUserPage(String userId) {
        Intent goToUserPageIntent = new Intent(
                CommentsActivity.this, UserPageActivity.class);
        goToUserPageIntent.putExtra("userId", userId);
        startActivity(goToUserPageIntent);
    }

    private void goToPost() {
        Intent openPostIntent = new Intent(this, ViewPostActivity.class);
        openPostIntent.putExtra("postId", postId);
        startActivity(openPostIntent);
        finish();
    }
}
