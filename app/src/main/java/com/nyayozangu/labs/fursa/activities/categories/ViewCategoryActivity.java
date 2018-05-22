package com.nyayozangu.labs.fursa.activities.categories;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.main.SearchableActivity;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.activities.settings.MySubscriptionsActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class
ViewCategoryActivity extends AppCompatActivity {

    private static final String TAG = "Sean";

    private RecyclerView catFeed;
    private SwipeRefreshLayout swipeRefresh;
    private SearchView searchView;
    private FloatingActionButton subscribeFab;

    //retrieve posts
    private List<Posts> postsList;
    private List<Users> usersList;

    //common methods
    private CoMeth coMeth;

    //recycler adapter
    private PostsRecyclerAdapter categoryRecyclerAdapter;
    private DocumentSnapshot lastVisiblePost;
    private Boolean isFirstPageFirstLoad = true;
    private String userId;
    private String currentCat;
    private String catDesc;
    private ProgressDialog progressDialog;

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_cat_toolbar_menu, menu);
        //handle search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.viewCatSearchMenuItem).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(this, SearchableActivity.class)));
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        return true;
    }

    @Override
    public void onBackPressed() {
        goToMain();
    }

    private void goToMain() {
        Intent goToMainIntent = new Intent(ViewCategoryActivity.this, MainActivity.class);
        goToMainIntent.putExtra("action", "goto");
        goToMainIntent.putExtra("destination", "categories");
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

        Log.d(TAG, "Sharing cat");
        showProgress(getString(R.string.loading_text));
        //create cat url
        String catUrl = getResources().getString(R.string.fursa_url_cat_head) + currentCat;
        Task<ShortDynamicLink> shortLinkTask =
                FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse(catUrl))
                        .setDynamicLinkDomain(getString(R.string.dynamic_link_domain))
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                                .setMinimumVersion(12)
                                .setFallbackUrl(Uri.parse(getString(R.string.playstore_url)))
                                .build())
                        // TODO: 5/18/18 handle opening links on ios
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
                                    Uri flowchartLink = task.getResult().getPreviewLink();
                                    Log.d(TAG, "onComplete: short link is: " + shortLink);

                                    //show share dialog
                                    String catTitle = coMeth.getCatValue(currentCat);
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
                                    showSnack(getString(R.string.failed_to_share_text));
                                }
                            }
                        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_category);

        //initiate common methods
        coMeth = new CoMeth();

        Toolbar toolbar = findViewById(R.id.viewCatToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMain();
            }
        });

        //initiate items
        catFeed = findViewById(R.id.catRecyclerView);

        //initiate an arrayList to hold all the posts
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();

        String className = "ViewCategoryActivity";
        categoryRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className);
        coMeth.handlePostsView(ViewCategoryActivity.this, ViewCategoryActivity.this, catFeed);
//        catFeed.setLayoutManager(new LinearLayoutManager(this));
        catFeed.setAdapter(categoryRecyclerAdapter);

        //handle intent
        if (getIntent() != null) {
            handleIntent();
        } else {
            goToMain();
        }

        //initiate items
        subscribeFab = findViewById(R.id.subscribeCatFab);
        swipeRefresh = findViewById(R.id.catSwipeRefresh);

        if (coMeth.isConnected()) {
            if (coMeth.isLoggedIn()) {
                //check if user is subscribed and set fab
                setFab();
            }
        } else {

            //user is not connected to internet
            showSnack(getString(R.string.failed_to_connect_text));

        }

        //handle fab
        subscribeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if device is connected
                if (coMeth.isConnected()) {
                    Log.d(TAG, "onClick: is connected");

                    //check if user is logged in
                    if (coMeth.isLoggedIn()) {
                        Log.d(TAG, "onClick: is logged in");

                        //show progress
                        showProgress(getString(R.string.subscribing_text));
                        userId = coMeth.getUid();

                        coMeth.getDb()
                                .collection("Users/" + userId + "/Subscriptions")
                                .document("categories")
                                .collection("Categories").document(currentCat).get()
                                .addOnCompleteListener(ViewCategoryActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        //get data from teh likes collection

                                        //check if current user has already subscribed
                                        if (!task.getResult().exists()) {
                                            Map<String, Object> catsMap = new HashMap<>();
                                            catsMap.put("key", currentCat);
                                            catsMap.put("value", getSupportActionBar().getTitle());
                                            catsMap.put("desc", catDesc);
                                            catsMap.put("timestamp", FieldValue.serverTimestamp());

                                            //subscribe user
                                            coMeth.getDb().collection("Users/" + userId + "/Subscriptions")
                                                    .document("categories")
                                                    .collection("Categories")
                                                    .document(currentCat).set(catsMap);
                                            //set image
                                            subscribeFab.setImageResource(R.drawable.ic_action_subscribed);
                                            //subscribe to notifications
                                            //subscribe to app updates
                                            FirebaseMessaging.getInstance().subscribeToTopic(currentCat);
                                            Log.d(TAG, "user subscribed to topic {CURRENT CAT}");
                                            //notify user
                                            showSnack("Subscribed to " + getSupportActionBar().getTitle());

                                        } else {

                                            //unsubscribe
                                            coMeth.getDb()
                                                    .collection("Users/" + userId + "/Subscriptions")
                                                    .document("categories")
                                                    .collection("Categories")
                                                    .document(currentCat).delete();
                                            //unsubscribe to app updates
                                            FirebaseMessaging.getInstance().unsubscribeFromTopic(currentCat);
                                            Log.d(TAG, "user unSubscribe to topic {CURRENT CAT}");
                                            //set fab image
                                            subscribeFab.setImageResource(R.drawable.ic_action_subscribe);
                                        }
                                    }
                                });

                        coMeth.stopLoading(progressDialog);

                    } else {

                        //prompt login
                        goToLogin(getString(R.string.log_to_subscribe_text));

                    }

                } else {

                    //user is not connected to internet
                    showSnack(getString(R.string.failed_to_connect_text));

                }

            }
        });

        //loading
        showProgress(getString(R.string.loading_text));
        //handle showing posts
        //listen for scrolling on the homeFeedView
        catFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
                Boolean reachedBottom = !catFeed.canScrollVertically(1);
                if (reachedBottom) {
                    Log.d(TAG, "at addOnScrollListener\n reached bottom");
                    loadMorePosts();
                }
            }
        });

        final Query firstQuery = coMeth.getDb().
                collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING);
        postsList.clear();
        usersList.clear();
        loadPosts(firstQuery);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //get new posts
                catFeed.getRecycledViewPool().clear();
                postsList.clear();
                usersList.clear();
                loadPosts(firstQuery);


            }
        });

    }


    /**
     * handles incoming intents
     */
    private void handleIntent() {

        Log.d(TAG, "handleIntent: ");
        Intent getPostIdIntent = getIntent();
        if (getPostIdIntent.getStringExtra("category") != null) {
            String category = getPostIdIntent.getStringExtra("category");
            setCurrentCat(category);
        } else {
            //intent is from deep link
            String category = handleDeepLink(getIntent());
            setCurrentCat(category);
        }
    }

    private String handleDeepLink(Intent intent) {

        // handle app links
        Log.i(TAG, "at handleDeepLinkIntent");
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
            Log.d(TAG, "cat is: " + category);

            currentCat = category;

        /*
        "Featured",
        "Popular",
        "UpComing",
        "Events",
        "Business",
        "Buy and sell",
        "Education",
        "Jobs",
        "Queries"*/

            //set the category name ot toolbar
            switch (category) {

                case "featured":
                    getSupportActionBar().setTitle(getString(R.string.cat_featured));
                    break;

                case "popular":
                    getSupportActionBar().setTitle(getString(R.string.cat_popular));
                    break;

                case "upcoming":
                    getSupportActionBar().setTitle(getString(R.string.cat_upcoming));
                    break;

                case "events":
                    getSupportActionBar().setTitle(getString(R.string.cat_events));
                    break;

                case "places":
                    getSupportActionBar().setTitle(getString(R.string.cat_places));
                    break;

                case "services":
                    getSupportActionBar().setTitle(getString(R.string.cat_services));
                    break;

                case "business":
                    getSupportActionBar().setTitle(getString(R.string.cat_business));
                    break;

                case "buysell":
                    getSupportActionBar().setTitle(getString(R.string.cat_buysell));
                    break;

                case "education":
                    getSupportActionBar().setTitle(getString(R.string.cat_education));
                    break;

                case "jobs":
                    getSupportActionBar().setTitle(getString(R.string.cat_jobs));
                    break;

                case "queries":
                    getSupportActionBar().setTitle(getString(R.string.cat_qna_text));
                    break;

                default:
                    Log.d(TAG, "onCreate: default is selected");
            }

        }
    }

    private void loadPosts(Query firstQuery) {
        firstQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                Log.d(TAG, "onEvent: first Query");
                //check if the data is loaded for the first time

                if (isFirstPageFirstLoad) {

                    //get the last visible post
                    try {

                        lastVisiblePost = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        postsList.clear();
                        usersList.clear();

                    } catch (Exception exception) {
                        Log.d(TAG, "error: " + exception.getMessage());
                    }

                }

                //create a for loop to check for document changes
                for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    //check if an item is added
                    if (doc.getType() == DocumentChange.Type.ADDED) {

                        //get the post id for likes feature
                        final String postId = doc.getDocument().getId();
                        processCategories(doc, postId);

                    }
                }

                //the first page has already loaded
                isFirstPageFirstLoad = false;
                coMeth.stopLoading(progressDialog, swipeRefresh);

            }
        });
    }

    private void setFab() {

        Log.d(TAG, "setFab: called");

        userId = coMeth.getUid();
        //check if user is subscribed
        coMeth.getDb()
                .collection("Users/" + userId + "/Subscriptions")
                .document("categories")
                .collection("Categories").document(currentCat)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                //check if user exists
                if (documentSnapshot.exists()) {

                    //user is already subscribed
                    //set fab image
                    subscribeFab.setImageResource(R.drawable.ic_action_subscribed);

                } else {

                    //user is already subscribed
                    //set fab image
                    subscribeFab.setImageResource(R.drawable.ic_action_subscribe);

                }

            }
        });

    }


    private void processCategories(DocumentChange doc, String postId) {

        Log.d(TAG, "processCategories: ");
        //get received intent

        final String category = currentCat;
        Log.d(TAG, "processCategories: \ncategory is: " + category);

        switch (category) {

            /*
            "Featured",
            "Popular",
            "UpComing",
            "Events",
            "Business",
            "Buy and sell",
            "Education",
            "Jobs",
            "Queries"*/

            case "featured":
                filterCat(doc, postId, "featured");
                break;

            case "popular":
                filterCat(doc, postId, "popular");
                break;

            case "upcoming":
                filterCat(doc, postId, "upcoming");
                break;

            case "events":
                filterCat(doc, postId, "events");
                break;

            case "places":
                filterCat(doc, postId, "places");
                break;

            case "services":
                filterCat(doc, postId, "services");
                break;

            case "business":
                filterCat(doc, postId, "business");
                break;

            case "buysell":
                filterCat(doc, postId, "buysell");
                break;

            case "education":
                filterCat(doc, postId, "education");
                break;

            case "jobs":
                filterCat(doc, postId, "jobs");
                break;

            case "queries":
                filterCat(doc, postId, "queries");
                break;

            default:
                Log.d(TAG, "onEvent: default");

        }
    }

    private void filterCat(final DocumentChange doc, final String postId, final String category) {

        Log.d(TAG, "filterCat: at filter cat\ncat is: " + category);
        //check if current post contains business

        final Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
        ArrayList catsArray = post.getCategories();
        Log.d(TAG, "filterCat: \ncatsArray is: " + catsArray);

        if (category.equals("upcoming")) {

            Log.d(TAG, "filterCat: cat is upcoming");
            //check if post has event date
            if (post.getEvent_date() != null) {

                Date eventDate = post.getEvent_date();
                Log.d(TAG, "filterCat: post has event date\nevent date is: " + eventDate);
                Long eventDateMils = (eventDate.getTime());
                Long nowMils = new Date().getTime();

                Calendar endCal = Calendar.getInstance();
                Log.d(TAG, "filterCat: event cal is " + endCal);
                endCal.add(Calendar.MONTH, 6);
                Log.d(TAG, "filterCat: event cal after 6 months is " + endCal);

                Calendar eventCal = Calendar.getInstance();
                eventCal.set(eventDate.getYear(),
                        eventDate.getMonth(),
                        eventDate.getDay());
                Log.d(TAG, "filterCat: eventCal is " + eventCal);


                if (eventDate.after(new Date()) && eventCal.before(endCal)) {

                    postsList.clear();
                    usersList.clear();
                    getFilteredPosts(post);

                }

            }

        } else if (category.equals("featured")) {

            //cat is featured
            Log.d(TAG, "filterCat: cat is " + category);

        } else if (category.equals("popular")) {

            ///cat is popular
            Log.d(TAG, "filterCat: cat is " + category);
            //open db and get post likes

            processCounts(postId, post, "Likes");
            processCounts(postId, post, "Saves");
            processCounts(postId, post, "Comments");


        } else {

            if (catsArray != null) {

                Log.d(TAG, "filterCat: catsArray is not null");
                //check if post contains cat
                if (catsArray.contains(category)) {

                    getFilteredPosts(post);

                } else {

                    //posts dont have current cat
                    coMeth.stopLoading(progressDialog, swipeRefresh);

                }

            }
        }
    }

    private void processCounts(String postId, final Posts post, final String collectionName) {

        Log.d(TAG, "processCounts: ");
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .collection(collectionName)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            int count = queryDocumentSnapshots.size();
                            Log.d(TAG, "onEvent: likes count inside is " + count);
                            switch (collectionName) {

                                case "Likes":
                                    if (count > 10) {
                                        if (!postsList.contains(post)) {
                                            getFilteredPosts(post);
                                        }
                                    }
                                    break;
                                case "Saves":
                                    if (count > 10) {
                                        if (!postsList.contains(post)) {
                                            getFilteredPosts(post);
                                        }
                                    }
                                    break;
                                case "Comments":
                                    if (count > 10) {
                                        if (!postsList.contains(post)) {
                                            getFilteredPosts(post);
                                        }
                                    }
                                    break;
                                default:
                                    Log.d(TAG, "onEvent: on popular default");
                            }
                        }

                    }
                });
    }

    private void getFilteredPosts(final Posts post) {
        String postUserId = post.getUser_id();
        coMeth.getDb()
                .collection("Users")
                .document(postUserId)
                .get()
                .addOnCompleteListener(
                        ViewCategoryActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            //check if user exists
                            if (task.getResult().exists()) {

                                String userId = task.getResult().getId();
                                Users user = task.getResult().toObject(Users.class).withId(userId);
                                //add new post to the local postsList
                                if (isFirstPageFirstLoad) {

                                    //add the post at position 0 of the postsList
                                    if (!postsList.contains(post)) {
                                        postsList.add(0, post);
                                        usersList.add(0, user);
                                    }


                                } else {

                                    //if the first page is loaded the add new post normally
                                    if (!postsList.contains(post)) {
                                        postsList.add(post);
                                        usersList.add(user);
                                    }

                                }
                                //notify the recycler adapter of the set change
                                categoryRecyclerAdapter.notifyDataSetChanged();
                                //stop loading after first post is visible
                                coMeth.onResultStopLoading(postsList, progressDialog, swipeRefresh);

                            } else {

                                //cat has no posts
                                coMeth.stopLoading(progressDialog, swipeRefresh);
                                Log.d(TAG, "onComplete: cat has no posts");

                            }

                        } else {

                            //task has failed
                            Log.d(TAG, "onComplete: task has failed: " + task.getException());

                        }

                    }
                });
    }


    //for loading more posts
    public void loadMorePosts() {

        Log.d(TAG, "loadMorePosts: ");
        Query nextQuery = coMeth.getDb()
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(20);

        //get all posts from the database
        nextQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                Log.d(TAG, "onEvent: nextQuery");
                try {
                    //check if there area more posts
                    if (!queryDocumentSnapshots.isEmpty()) {

                        //get the last visible post
                        lastVisiblePost = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        //create a for loop to check for document changes
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            //check if an item is added
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                //a new item/ post is added
                                String postId = doc.getDocument().getId();
                                processCategories(doc, postId);

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


    //show snack
    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.viewCatLayout),
                message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.see_list_text), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //go to my subscriptions
                        goToMySubscriptions();

                    }
                })
                .show();
    }

    private void goToMySubscriptions() {
        startActivity(new Intent(ViewCategoryActivity.this, MySubscriptionsActivity.class));

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
