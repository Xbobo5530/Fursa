package com.nyayozangu.labs.fursa.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.nyayozangu.labs.fursa.adapters.CommentsRecyclerAdapter;
import com.nyayozangu.labs.fursa.models.Comments;
import com.nyayozangu.labs.fursa.models.Post;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.helpers.Notify;
import com.nyayozangu.labs.fursa.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import static com.nyayozangu.labs.fursa.helpers.CoMeth.ACTION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.COMMENTS_COLL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.COMMENTS_DOC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.COMMENT_UPDATES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.DESTINATION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.GOTO;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MESSAGE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NOTIFICATIONS_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NOTIFY;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POST_ID;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POST_ID_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SOURCE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SUBSCRIPTIONS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TIMESTAMP;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID_VAL;

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: 6/14/18 for view reward  notifications use viewsRewardPostId as topic
    // TODO: 6/14/18 add reply to comment

    private static final String TAG = "Sean";
    private EditText chatField;
    private ImageView currentUserImage, postUserImage;
    private TextView postUsernameField, postTitleField;
    private RecyclerView commentsRecyclerView;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;
    private CoMeth coMeth = new CoMeth();
    private ProgressDialog progressDialog;

    private String postId;
    private String currentUserId;

    private android.support.v7.widget.Toolbar toolbar;

    private DocumentReference postDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        postUserImage = findViewById(R.id.commentsPostUserImageView);
        postUsernameField = findViewById(R.id.commentsPostUsernameTextView);
        postTitleField = findViewById(R.id.commentsPostTitleTextView);
        toolbar = findViewById(R.id.commentsToolbar);
        ImageButton sendButton = findViewById(R.id.commentsSendButtonImageView);
        chatField = findViewById(R.id.commentsChatEditText);
        currentUserImage = findViewById(R.id.commentsCurrentUserImageView);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        ConstraintLayout postDetailsLayout = findViewById(R.id.commentpostDetailsConstraintLayout);

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
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.comments_text));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handleBackBehaviour();
        handleIntent();
        updateViews(postId);

        //handle clicks
        postDetailsLayout.setOnClickListener(this);
        postUserImage.setOnClickListener(this);
        postTitleField.setOnClickListener(this);
        currentUserImage.setOnClickListener(this);

        setPostDetails();
        retrieveComments();
        handleSendButton(sendButton);
        checkConnection();

        postDocRef = coMeth.getDb().collection(USERS +
                "/" + currentUserId + "/" + SUBSCRIPTIONS + "/" + COMMENTS_DOC + "/" +
                COMMENTS_COLL).document(postId);
    }

    private void checkConnection() {
        if (!coMeth.isConnected(this)) {
            coMeth.stopLoading(progressDialog);
            showSnack(getResources().getString(R.string.failed_to_connect_text));
        }
    }

    private void handleSendButton(ImageView sendButton) {
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
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToLogin(getString(R.string.login_to_comment));
                }

            });
        }
    }

    private void handleBackBehaviour() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                if (intent != null && intent.getStringExtra(SOURCE) != null){
                    if (intent.getStringExtra(SOURCE).equals(NOTIFICATIONS_VAL)) {
                        Intent goToNotificationsIntent =
                                new Intent(CommentsActivity.this, MainActivity.class);
                        goToNotificationsIntent.putExtra(ACTION, GOTO);
                        goToNotificationsIntent.putExtra(DESTINATION, NOTIFICATIONS_VAL);
                        startActivity(goToNotificationsIntent);
                        finish();
                    }else{
                        //from in app but not notifications fragment
                        finish();
                    }
                }else {
                    if (intent == null){
                        goToMain(getResources().getString(R.string.something_went_wrong_text));
                    }
                    if (intent != null &&intent.getStringExtra(SOURCE) == null){
                        goToMain();
                    }
                }
            }
        });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void updateViews(final String postId) {
        Log.d(TAG, "updateViews: ");
        coMeth.getDb().collection(POSTS).document(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Post post = documentSnapshot.toObject(Post.class);
                            if (post != null) {
                                int views = post.getViews();
                                String postUserId = post.getUser_id();
                                if (!coMeth.isLoggedIn() ||
                                        (coMeth.isLoggedIn() && !coMeth.getUid().equals(postUserId))) {
                                    addNewView(views, postId);
                                }
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
                        Log.d(TAG, "onFailure: failed to get views for comments view\n" +
                                e.getMessage());
                    }
                });
    }

    private void addNewView(int views, String postId) {
        Log.d(TAG, "addNewView: ");
        Map<String, Object> viewsMap = new HashMap<>();
        viewsMap.put("views", views + 1);
        coMeth.getDb().collection(POSTS).document(postId).update(viewsMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: views from comments updated");
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleBackBehaviour();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            postId = getPostIdFromIntent(intent);
            Log.d(TAG, "postId is: " + postId);
        } else {
            goToMain(getResources().getString(R.string.something_went_wrong_text));
        }
    }

    private String getPostIdFromIntent(Intent intent) {
        if (intent != null &&
                intent.getStringExtra(POST_ID) != null) {
            return intent.getStringExtra(POST_ID);
        } else {
            return null;
        }
    }

    private void goToMain(String message) {
        Intent goToMainIntent = new Intent(this, MainActivity.class);
        goToMainIntent.putExtra(ACTION, NOTIFY);
        goToMainIntent.putExtra(MESSAGE, message);
        startActivity(goToMainIntent);
        finish();
    }

    private void postComment() {
        if (!chatField.getText().toString().trim().isEmpty()) {
            showProgress(getString(R.string.posting_comment_text));
            final String comment = chatField.getText().toString().trim();
            final Map<String, Object> commentsMap = new HashMap<>();
            commentsMap.put(TIMESTAMP, FieldValue.serverTimestamp());
            commentsMap.put(CoMeth.COMMENT, comment);
            commentsMap.put(USER_ID_VAL, currentUserId);
            coMeth.getDb().collection(POSTS + "/" + postId + "/" + COMMENTS_COLL)
                    .add(commentsMap)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            FirebaseMessaging.getInstance().subscribeToTopic(postId);
                            new Notify().execute(COMMENT_UPDATES, postId);
                            coMeth.stopLoading(progressDialog);
                            addCommentRef();
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
            chatField.setText("");
        }

    }

    private void setUserDetails() {
        currentUserId = coMeth.getUid();
        coMeth.getDb().collection(USERS).document(currentUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot,
                                        FirebaseFirestoreException e) {

                        if (e == null) {
                            if (documentSnapshot.exists()) {
                                //user exists
                                User user = documentSnapshot.toObject(User.class);
                                String profileImageUrl = Objects.requireNonNull(user).getImage();
                                String profileThumbUrl = user.getThumb();
                                if (profileThumbUrl != null){
                                    coMeth.setCircleImage(R.drawable.ic_action_person_placeholder,
                                            profileThumbUrl, currentUserImage, CommentsActivity.this);
                                }else if (profileImageUrl != null){
                                    coMeth.setCircleImage(R.drawable.ic_action_person_placeholder,
                                            profileImageUrl, currentUserImage, CommentsActivity.this);
                                }else{
                                    currentUserImage.setImageDrawable(getResources()
                                            .getDrawable(R.drawable.ic_action_person_placeholder));
                                }
                            }
                        }else{
                            Log.d(TAG, "onEvent: failed to set user details\n" + e.getMessage());
                            showSnack(getResources().getString(R.string.error_text) +": " +
                                    e.getMessage());
                        }

                    }
                });
    }

    private void setPostDetails() {
        coMeth.getDb().collection(POSTS).document(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Post post = documentSnapshot.toObject(Post.class);
                            assert post != null;
                            final String postUserId = post.getUser_id();
                            postUserImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToUserPage(postUserId);
                                }
                            });
                            String title = post.getTitle();
                            String desc = post.getDesc();
                            if (title != null){
                                postTitleField.setText(title);
                            }else{
                                if (desc != null){
                                    postTitleField.setText(desc);
                                }
                            }
                            getUserData(postUserId);
                        } else {
                            goToMain(getString(R.string.post_not_found_text));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get post: " + e.getMessage());
                        showSnack(getString(R.string.error_text) +  ": " + e.getMessage());
                    }
                });
    }

    private void getUserData(String postUserId) {
        coMeth.getDb().collection(USERS).document(postUserId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            String username = Objects.requireNonNull(user).getName();
                            postUsernameField.setText(username);
                            //set user image
                            String userImageDownloadUrl = user.getImage();
                            String userThumbDownloadUrl = user.getThumb();
                            if (userThumbDownloadUrl != null) {
                                coMeth.setCircleImage(R.drawable.ic_action_person_placeholder,
                                        userThumbDownloadUrl, postUserImage, CommentsActivity.this);
                            } else {
                                coMeth.setCircleImage(
                                        R.drawable.ic_action_person_placeholder, userImageDownloadUrl,
                                        postUserImage, CommentsActivity.this);
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
        coMeth.getDb().collection(POSTS + "/" + postId + "/" + COMMENTS_COLL)
                .orderBy(TIMESTAMP, Query.Direction.ASCENDING)
                .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if (e == null) {

                            //check if query is empty
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        String commentId = doc.getDocument().getId();
                                        Comments comment =
                                                doc.getDocument().toObject(Comments.class).withId(commentId);
                                        commentsList.add(comment);
                                        commentsRecyclerAdapter.notifyDataSetChanged();
                                        commentsRecyclerView.scrollToPosition(commentsList.size() - 1);
                                    }
                                }
                            } else {
                                Log.d(TAG, "onEvent: post has no comments");
                            }
                            coMeth.stopLoading(progressDialog);
                        }else{
                            Log.d(TAG, "onEvent: failed to get comments\n" + e.getMessage());
                            showSnack(getResources().getString(R.string.error_text) + ": " +
                                    e.getMessage());
                        }
                    }
                });
    }


    //setting sub icon on toolbar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setSubscribeIconStatus(menu);
        return true;

    }

    private void setSubscribeIconStatus(Menu menu) {
        if (coMeth.isConnected(this)) {
            if (coMeth.isLoggedIn()) {
                currentUserId = coMeth.getUid();

                final MenuItem subscribeButton = menu.findItem(R.id.comSubMenuItem);
                coMeth.getDb()
                        .collection(USERS + "/" + currentUserId + "/" + SUBSCRIPTIONS)
                        .document(COMMENTS_DOC).collection(COMMENTS_COLL)
                        .whereEqualTo(POST_ID_VAL, postId)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.d(TAG, "onEvent: error on set subscribed icon "
                                            + e.getMessage());
                                } else {
                                    if (queryDocumentSnapshots != null) {
                                        if (queryDocumentSnapshots.isEmpty()) {
                                            subscribeButton.setIcon(R.drawable.ic_action_subscribe);
                                        } else {
                                            subscribeButton.setIcon(R.drawable.ic_action_subscribed);
                                        }
                                    }
                                }
                            }
                        });
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

    private void subscribe() {
        if (coMeth.isLoggedIn()) {
            postDocRef.get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        postDocRef.delete()
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: failed to unsub " +
                                                "user from comments\n" + e.getMessage());
                                        //alert user
                                        showSnack(getResources().getString(R.string.error_text) + ": " + e.getMessage());
                                    }
                                });
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(postId);
                        Log.d(TAG, "user subscribed to topic {CURRENT POST}");
                    } else {
                        addCommentRef();
                        showSnack(getResources().getString(R.string.subd_to_post_text));
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: failed to sub user\n" + e.getMessage());
                    showSnack(getResources().getString(R.string.error_text) + ": " + e.getMessage());
                }
            });
        } else {
            goToLogin(getString(R.string.login_to_sub_comments));
        }
    }

    private void addCommentRef() {
        Map<String, Object> commentsSubMap = new HashMap<>();
        commentsSubMap.put(POST_ID_VAL, postId);
        commentsSubMap.put(TIMESTAMP, FieldValue.serverTimestamp());
        postDocRef.set(commentsSubMap);
        FirebaseMessaging.getInstance().subscribeToTopic(postId);
    }

    private void goToLogin(String message) {
        Intent loginIntent = new Intent(CommentsActivity.this, LoginActivity.class);
        loginIntent.putExtra(MESSAGE, message);
        startActivity(loginIntent);
        finish();
    }

    private void showProgress(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.comment_activity_layout),
                message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.commentpostDetailsConstraintLayout:
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
