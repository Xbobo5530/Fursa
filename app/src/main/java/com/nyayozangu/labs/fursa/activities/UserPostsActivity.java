package com.nyayozangu.labs.fursa.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.models.Post;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.ACTION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.DESTINATION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MESSAGE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MY_POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MY_POSTS_DOC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NOTIFY;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SAVED_POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SAVED_POSTS_DOC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SAVED_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SUBSCRIPTIONS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_POSTS;

public class UserPostsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();

    private FloatingActionButton newPostFab;
    private List<Post> postsList;
    private List<User> usersList;
    private PostsRecyclerAdapter mAdapter;
    private DocumentSnapshot lastVisiblePost;
    private Boolean isFirstPageFirstLoad = true;
    private ProgressDialog progressDialog;
    private String userId = null, currentUserId;
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
        mAdapter =
                new PostsRecyclerAdapter(postsList, usersList, className,
                        Glide.with(this), this);
        coMeth.handlePostsView(
                UserPostsActivity.this, UserPostsActivity.this, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        newPostFab = findViewById(R.id.userPostsNewPostFab);

        currentUserId = coMeth.getUid();
        handleIntent();
        if (!coMeth.isConnected(this)){
            coMeth.stopLoading(progressDialog);
            showSnack(getResources().getString(R.string.failed_to_connect_text));
        }
        newPostFab.setOnClickListener(this);
        toolbar.setOnClickListener(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(DESTINATION) != null){
                String destination = intent.getStringExtra(DESTINATION);
                showProgress(getString(R.string.loading_text));
                switch (destination){
                    case SAVED_VAL:
                        Log.d(TAG, "getPostId:  at saved val");
                        processShowingFab();
                        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.my_saved_posts_text));
                        loadSavedPosts();
                        break;
                    case USER_POSTS:
                        if (intent.getStringExtra(USER_ID) != null) {
                            userId = intent.getStringExtra(USER_ID);
                            processShowingFab();
                            currentUserId = coMeth.getUid();
                            if (userId.equals(currentUserId)) {
                                Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.my_posts_text));
                            } else {
                                if (intent.getStringExtra(CoMeth.USERNAME) != null) {
                                    String username = intent.getStringExtra(CoMeth.USERNAME);
                                    String pageTitle = username + "'s posts";
                                    Objects.requireNonNull(getSupportActionBar()).setTitle(pageTitle);
                                }
                            }
                            loadUserPosts(userId);
                        } else {
                            //intent has no user id
                            goToMain(getString(R.string.something_went_wrong_text));
                        }
                        break;
                    default:
                        Log.d(TAG, "getPostId: get intent at default");

                }
            }

        }else{
            goToMain(getString(R.string.something_went_wrong_text));
        }
    }

    private void loadSavedPosts() {
        if (coMeth.isLoggedIn()) {
            CollectionReference savedRef = coMeth.getDb().collection(
                    USERS + "/" + currentUserId + "/" +
                            SUBSCRIPTIONS + "/" + SAVED_POSTS_DOC + "/" + SAVED_POSTS);
            savedRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots.isEmpty()) {
                        coMeth.stopLoading(progressDialog);
                        showActionSnack(getResources().getString(R.string.no_saved_posts_text));
                    } else {
                        for (DocumentChange document : queryDocumentSnapshots.getDocumentChanges()) {
                            if (document.getType() == DocumentChange.Type.ADDED) {
                                String postId = document.getDocument().getId();
                                getPost(postId);
                            }
                        }
                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: failed to get saved posts\n" + e.getMessage(), e);
                            showSnack(getResources().getString(R.string.error_text) + ": " + e.getMessage());
                        }
                    });
        }else{
            coMeth.stopLoading(progressDialog);
            goToLogin(getResources().getString(R.string.login_to_view_saved_items_text));
        }
    }

    private void goToLogin(String message) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(MESSAGE, message);
        startActivity(intent);
        finish();
    }

    private void showActionSnack(String message) {
        Snackbar.make(findViewById(R.id.userPostLayout), message, Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.secondaryLightColor))
                .setAction(getResources().getString(R.string.view_posts_text), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToMain();
                    }
                })
                .show();
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void getPost(final String postId) {
        DocumentReference postRef = coMeth.getDb().collection(POSTS).document(postId);
        postRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    Post post = Objects.requireNonNull(documentSnapshot.toObject(Post.class)).withId(postId);
                    getPostUser(post);
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: failed to get post\n" + e.getMessage(), e);
                    }
                });
    }

    private void getPostUser(final Post post) {
        final String userId = post.getUser_id();
        DocumentReference userRef = coMeth.getDb().collection(USERS).document(userId);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    User user = Objects.requireNonNull(documentSnapshot.toObject(User.class)).withId(userId);
                    if (!postsList.contains(post)){
                        postsList.add(post);
                        usersList.add(user);
                        mAdapter.notifyItemInserted(postsList.size() - 1);
                        coMeth.stopLoading(progressDialog);
                    }
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: failed to get user\n" + e.getMessage(), e);
                    }
                });
    }

    private void loadUserPosts(final String userId) {
        coMeth.getDb()
                .collection(USERS + "/" + userId + "/" + SUBSCRIPTIONS)
                .document(MY_POSTS_DOC).collection(MY_POSTS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e == null) {
                            if (queryDocumentSnapshots != null) {
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
                            final Post post = Objects.requireNonNull(
                                    documentSnapshot.toObject(Post.class)).withId(postId);
                            getTheUserDetails(post, userId);

                        } else {
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

    private void getTheUserDetails(final Post post, final String userId) {
        coMeth.getDb()
                .collection(USERS).document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = Objects.requireNonNull(
                                    documentSnapshot.toObject(User.class)).withId(userId);
                            postsList.add(post);
                            usersList.add(user);
                            mAdapter.notifyItemInserted(postsList.size() - 1);
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
        currentUserId = coMeth.getUid();
        if (currentUserId != null && currentUserId.equals(userId)) {
            //show fab
            newPostFab.setVisibility(View.VISIBLE);
        } else {
            newPostFab.setVisibility(View.GONE);
        }
    }

    private void goToMain(String message) {
        Intent goToMainIntent = new Intent(
                UserPostsActivity.this, MainActivity.class);
        goToMainIntent.putExtra(ACTION , NOTIFY);
        goToMainIntent.putExtra(MESSAGE, message);
        startActivity(goToMainIntent);
        finish();
    }

    private void showProgress(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.userPostsNewPostFab:
                startActivity(new Intent(
                        UserPostsActivity.this, CreatePostActivity.class));
                finish();
                break;
            case R.id.userPostsToolbar:
                mRecyclerView.smoothScrollToPosition(0);
                break;
            default:
                Log.d(TAG, "onClick: user posts click listener on default");
        }
    }
}
