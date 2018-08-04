package com.nyayozangu.labs.fursa.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
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
import com.nyayozangu.labs.fursa.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.models.Post;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.ACTION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.APPS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.ART;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.BUSINESS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.BUY_AND_SELL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.CATEGORIES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.CATEGORIES_DOC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.CATEGORIES_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.DESTINATION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.DISCUSSIONS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.EDUCATION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.EVENTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.EXHIBITIONS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.GOTO;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.GROUPS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.JOBS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.PLACES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SERVICES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SUBSCRIPTIONS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TAGS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TAG_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TIMESTAMP;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;

public class
ViewCategoryActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    private static final String CATEGORY = "category";
    private static final String KEY = "key";
    private static final String VALUE = "value";

    private RecyclerView mRecyclerView;
    private FloatingActionButton subscribeFab;
    private List<Post> postsList;
    private List<User> usersList;
    private CoMeth coMeth = new CoMeth();
    private PostsRecyclerAdapter mAdapter;
    private DocumentSnapshot lastVisiblePost;
    private Boolean isFirstPageFirstLoad = true;
    private String userId, currentCat;
    private ProgressDialog progressDialog;
    private ProgressBar mProgressBar;
    private ActionBar actionBar;
    private CollectionReference catPostsRef;
    private CollectionReference tagPostRef;
    private boolean isCats = false;
    private boolean isTags = false;

    private Intent intent;

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_cat_toolbar_menu, menu);
        //handle search
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.viewCatSearchMenuItem).getActionView();
//        if (searchManager != null) {
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(
//                    new ComponentName(this, SearchableActivity.class)));
//            searchView.setQueryHint(getResources().getString(R.string.search_hint));
//        }
        return true;
    }

//    @Override
//    public void onBackPressed() {
//        goToMain();
//    }

    private void goToMain() {
        Intent goToMainIntent = new Intent(
                ViewCategoryActivity.this, MainActivity.class);
        goToMainIntent.putExtra(ACTION, GOTO);
        goToMainIntent.putExtra(DESTINATION, CATEGORIES_VAL);
        startActivity(goToMainIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.viewCatShareMenuItem:
                shareCat();
                break;
            default:
                Log.d(TAG, "onOptionsItemSelected: on view cat toolbar menu default");
        }
        return true;
    }

    private void shareCat() {

        showProgress(getString(R.string.loading_text));
        //create cat url
        String catUrl = getResources().getString(R.string.fursa_url_cat_head) + currentCat;
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(catUrl))
                .setDynamicLinkDomain(getString(R.string.dynamic_link_domain))
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(coMeth.minVerCode)
                        .setFallbackUrl(Uri.parse(getString(R.string.playstore_url)))
                        .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(getString(R.string.app_name))
                                .setDescription(getString(R.string.sharing_opp_text))
                                .setImageUrl(Uri.parse(getString(R.string.app_icon_url)))
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            Log.d(TAG, "onComplete: short link is: " + shortLink);

                            //show share dialog
                            String catTitle = coMeth.getCatValue(currentCat,
                                    ViewCategoryActivity.this);
                            String fullShareMsg = getString(R.string.app_name) + "\n" +
                                    catTitle + "\n" +
                                    shortLink;
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                                    getResources().getString(R.string.app_name));
                            shareIntent.putExtra(Intent.EXTRA_TEXT, fullShareMsg);
                            coMeth.stopLoading(progressDialog);
                            startActivity(Intent.createChooser(
                                    shareIntent, getString(R.string.share_with_text)));
                        } else {
                            Log.d(TAG, "onComplete: " +
                                    "\ncreating short link task failed\n" +
                                    task.getException());
                            coMeth.stopLoading(progressDialog);
                            showNoActionSnack(getString(R.string.failed_to_share_text));
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_category);

        Toolbar toolbar = findViewById(R.id.viewCatToolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) { actionBar.setDisplayHomeAsUpEnabled(true); }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                goToMain();
                finish();
            }
        });

        mRecyclerView = findViewById(R.id.catRecyclerView);
        mProgressBar = findViewById(R.id.catProgressBar);
        subscribeFab = findViewById(R.id.subscribeCatFab);
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();
        String className = "ViewCategoryActivity";
        mAdapter = new PostsRecyclerAdapter(postsList, usersList, className,
                Glide.with(this), this);
        coMeth.handlePostsView(ViewCategoryActivity.this,
                ViewCategoryActivity.this, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });
        handleIntent();
        coMeth.showProgress(mProgressBar);
        handleSubFab();
        handleScrolling();
    }

    private void handleScrolling() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean reachedBottom = !mRecyclerView.canScrollVertically(1);
                if (reachedBottom){
                    if (isCats) {
                        loadMoreCatPosts();
                    }
                    if (isTags){
                        loadMoreTagPosts();
                    }
                }
            }
        });
    }

    // TODO: 7/31/18 check why tags posts duplicate themselves
    private void loadMoreTagPosts() {
        coMeth.showProgress(mProgressBar);
        tagPostRef.orderBy(TIMESTAMP, Query.Direction.DESCENDING).startAfter(lastVisiblePost)
                .limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()){
                    lastVisiblePost = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
                        if (documentChange.getType() == DocumentChange.Type.ADDED){
                            String postId = documentChange.getDocument().getId();
                            getPost(postId);
                        }
                    }
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: failed to get tag posts\n" + e.getMessage());
                String errorMessage = getResources().getString(R.string.error_text) + ": " + e.getMessage();
                coMeth.stopLoading(mProgressBar);
                showSnack(errorMessage);
            }
        });

    }

    private void loadMoreCatPosts() {
        coMeth.showProgress(mProgressBar);
        Query nextQuery = catPostsRef.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(10);
        nextQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()){
                    lastVisiblePost = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                    for (DocumentChange document : queryDocumentSnapshots.getDocumentChanges()){
                        if (document.getType() == DocumentChange.Type.ADDED){
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
                        String errorMessage = getResources().getString(R.string.error_text) + ": " + e.getMessage();
                        Log.e(TAG, "onFailure: failed to get cat posts\n" + e.getMessage(), e);
                        coMeth.stopLoading(mProgressBar);
                        showSnack(errorMessage);
                    }
                });
    }

    private void getPost(final String postId) {
        DocumentReference postRef = coMeth.getDb().collection(POSTS).document(postId);
        postRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    Post post = Objects.requireNonNull(documentSnapshot.toObject(Post.class)).withId(postId);
                    getUserDetails(post);
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

    private void getUserDetails(final Post post) {
        final String userId = post.getUser_id();
        DocumentReference userRef = coMeth.getDb().collection(USERS).document(userId);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    User user = Objects.requireNonNull(documentSnapshot.toObject(User.class)).withId(userId);

                    if (isFirstPageFirstLoad) {
                        if (!postsList.contains(post)) {
                            postsList.add(0,post);
                            usersList.add(0,user);
                            mAdapter.notifyItemInserted(postsList.size() - 1);
                        }
                    }else{
                        if (!postsList.contains(post)) {
                            postsList.add(post);
                            usersList.add(user);
                            mAdapter.notifyItemInserted(postsList.size() - 1);
                        }
                    }
                    coMeth.stopLoading(mProgressBar);
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: failed to get user details\n" + e.getMessage(), e);
                    }
                });
    }

    private void handleSubFab() {
        if (isCats) {
            if (coMeth.isConnected(this)) {
                if (coMeth.isLoggedIn()) {
                    setFab();
                }
            } else {
                coMeth.stopLoading(progressDialog);
                showNoActionSnack(getString(R.string.failed_to_connect_text));
            }
            subscribeFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (coMeth.isConnected(ViewCategoryActivity.this)) {
                        if (coMeth.isLoggedIn()) {
                            showProgress(getString(R.string.subscribing_text));
                            userId = coMeth.getUid();
                            handleUserCatSubscription();
                        } else {
                            goToLogin(getString(R.string.log_to_subscribe_text));
                        }
                    } else {
                        coMeth.stopLoading(progressDialog);
                        showNoActionSnack(getString(R.string.failed_to_connect_text));
                    }
                }
            });
        }else{
            subscribeFab.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem shareButton = menu.findItem(R.id.viewCatShareMenuItem);
        if (isTags){
            shareButton.setVisible(false);
        }else {
            shareButton.setVisible(true);
        }
        return true;
    }

    private void handleUserCatSubscription() {
        final DocumentReference currentCatDb =  coMeth.getDb()
                .collection(USERS + "/" + userId + "/" + SUBSCRIPTIONS)
                .document( CATEGORY).collection(CATEGORIES).document(currentCat);
        currentCatDb.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {
                            currentCatDb.delete();
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(currentCat);
                            subscribeFab.setImageResource(R.drawable.ic_action_subscribe);
                            coMeth.stopLoading(progressDialog);
                        } else {
                            Map<String, Object> catsMap = new HashMap<>();
                            catsMap.put(KEY, currentCat);
                            if (actionBar != null) {
                                catsMap.put(VALUE, actionBar.getTitle());
                            }
                            catsMap.put(TIMESTAMP, FieldValue.serverTimestamp());
                            //subscribe user
                            currentCatDb.set(catsMap);
                            subscribeFab.setImageResource(R.drawable.ic_action_subscribed);
                            //subscribe to app updates
                            FirebaseMessaging.getInstance().subscribeToTopic(currentCat);
                            coMeth.stopLoading(progressDialog);
                            if (actionBar != null) {
                                showSnack(getString(R.string.sub_to_text) + " " + actionBar.getTitle());
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to sub user to cat\n" +
                                e.getMessage());
                        coMeth.stopLoading(progressDialog);
                        if (actionBar != null) {
                            showSnack(getResources().getString(R.string.failed_to_sub) + actionBar.getTitle() + ": " +
                                    e.getMessage());
                        }
                    }
                });
    }


    private void showNoActionSnack(String message) {
        Snackbar.make(findViewById(R.id.viewCatLayout),
                message, Snackbar.LENGTH_LONG)
                .show();
    }


    private void handleIntent() {
        intent = getIntent();
        if (intent != null) {
            String category = intent.getStringExtra(CATEGORY);
            String tag = intent.getStringExtra(TAG_VAL);
            if (category != null) {
                isCats = true;
                isTags = false;
                currentCat = intent.getStringExtra(CATEGORY);
                catPostsRef = coMeth.getDb().collection(CATEGORIES + "/" + currentCat + "/" + POSTS);
                loadCatPosts(catPostsRef);
                setCurrentCat(currentCat);
            } else if (tag != null){
                isTags = true;
                isCats = false;
                tagPostRef = coMeth.getDb().collection(TAGS).document(tag).collection(POSTS);
                loadTagPosts(tagPostRef);
                actionBar.setTitle("#" + tag);
            } else {
                //intent is from deep link
                currentCat = handleDeepLink();
                setCurrentCat(currentCat);
                catPostsRef = coMeth.getDb().collection(CATEGORIES + "/" + currentCat + "/" + POSTS);
                loadCatPosts(catPostsRef);
            }
        }else{
            goToMain();
        }
    }

    private void loadTagPosts(final CollectionReference tagRef) {
        tagRef.orderBy(TIMESTAMP, Query.Direction.ASCENDING).limit(10)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()){
                                lastVisiblePost = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                if (isFirstPageFirstLoad){
                                    postsList.clear();
                                    usersList.clear();

                                    for (DocumentChange document : queryDocumentSnapshots.getDocumentChanges()){
                                        if (document.getType() == DocumentChange.Type.ADDED){
                                            String postId = document.getDocument().getId();
                                            getPost(postId);
                                        }
                                    }
                                }
                            }
                        }
                    })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: failed to get tag posts\n" + e.getMessage(), e);
                    }
                });
    }

    private void loadCatPosts(CollectionReference catPostsRef) {
        catPostsRef.orderBy(TIMESTAMP, Query.Direction.ASCENDING).limit(10)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    lastVisiblePost = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    if (isFirstPageFirstLoad) {
                        postsList.clear();
                        usersList.clear();

                        for (DocumentChange document : queryDocumentSnapshots.getDocumentChanges()) {
                            if (document.getType() == DocumentChange.Type.ADDED) {
                                String postId = document.getDocument().getId();
                                getPost(postId);
                            }
                        }
                    }
                    isFirstPageFirstLoad = false;
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = getResources().getString(R.string.error_text) + ": " + e.getMessage();
                        Log.d(TAG, "onFailure: failed to get post ids from cats\n" +
                                e.getMessage());
                        coMeth.stopLoading(mProgressBar);
                        showSnack(errorMessage);
                    }
                });
    }

    private String handleDeepLink() {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            String postUrl = String.valueOf(appLinkData);
            int endOfUrlHead = getResources().getString(R.string.fursa_url_cat_head).length();
            currentCat = postUrl.substring(endOfUrlHead);
            Log.i(TAG, "incoming cat is " + currentCat);
        }
        return currentCat;
    }

    private void setCurrentCat(String category) {
        if (category != null) {
            if (actionBar != null) {
                switch (category) {
                    case EVENTS:
                        actionBar.setTitle(getString(R.string.cat_events));
                        break;
                    case PLACES:
                        actionBar.setTitle(getString(R.string.cat_places));
                        break;
                    case SERVICES:
                        actionBar.setTitle(getString(R.string.cat_services));
                        break;
                    case BUSINESS:
                        actionBar.setTitle(getString(R.string.cat_business));
                        break;
                    case BUY_AND_SELL:
                        actionBar.setTitle(getString(R.string.cat_buysell));
                        break;
                    case EDUCATION:
                        actionBar.setTitle(getString(R.string.cat_education));
                        break;
                    case JOBS:
                        actionBar.setTitle(getString(R.string.cat_jobs));
                        break;
                    case DISCUSSIONS:
                        actionBar.setTitle(getString(R.string.cat_queries));
                        break;
                    case EXHIBITIONS:
                        actionBar.setTitle(getString(R.string.cat_exhibitions));
                        break;
                    case ART:
                        actionBar.setTitle(getString(R.string.cat_art));
                        break;
                    case APPS:
                        actionBar.setTitle(getString(R.string.cat_apps));
                        break;
                    case GROUPS:
                        actionBar.setTitle(getString(R.string.cat_groups));
                        break;
                    default:
                        Log.d(TAG, "onCreate: default is selected");
                }
            }
        }
    }

    private void setFab() {
        userId = coMeth.getUid();
        //check if user is subscribed
        coMeth.getDb().collection(USERS + "/" + userId + "/" + SUBSCRIPTIONS)
                .document(CATEGORIES_DOC).collection(CATEGORIES).document(currentCat)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()) {
                            subscribeFab.setImageResource(R.drawable.ic_action_subscribed);
                        } else {
                            subscribeFab.setImageResource(R.drawable.ic_action_subscribe);
                        }
                    }
                });
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.viewCatLayout),
                message, Snackbar.LENGTH_LONG)
                .show();
    }

    private void goToLogin(String message) {
        Intent goToLoginIntent = new Intent(this, LoginActivity.class);
        goToLoginIntent.putExtra("message", message);
        startActivity(goToLoginIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent() != null) {
            handleIntent();
        } else {
            goToMain();
        }
    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }
}