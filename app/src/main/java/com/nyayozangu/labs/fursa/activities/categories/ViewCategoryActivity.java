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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.search.SearchableActivity;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.UserPageActivity;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class
ViewCategoryActivity extends AppCompatActivity {

    // TODO: 5/30/18 sort events based on event dates

    private static final String TAG = "Sean";
    private static final String CATEGORIES_COLL = "Categories";
    private static final String CATEGORIES_DOC = "categories";
    private static final String POSTS_COLL = "Posts";
    private static final String SUBSCRIPTIONS_COLL = "Subscriptions";
    private static final String USERS_COLL = "Users";

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
        Intent goToMainIntent = new Intent(
                ViewCategoryActivity.this, MainActivity.class);
        goToMainIntent.putExtra(getResources().getString(R.string.ACTION_NAME),
                getResources().getString(R.string.GOTO_VAL));
        goToMainIntent.putExtra(getResources().getString(R.string.DESTINATION_NAME), getResources().getString(R.string.CATEGORIES_VAL));
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
                                    showNoActionSnack(getString(R.string.failed_to_share_text));
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
        coMeth.handlePostsView(
                ViewCategoryActivity.this, ViewCategoryActivity.this, catFeed);
        catFeed.setAdapter(categoryRecyclerAdapter);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catFeed.smoothScrollToPosition(0);
            }
        });

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
            showNoActionSnack(getString(R.string.failed_to_connect_text));

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
                                .collection(USERS_COLL + "/" + userId + "/" + SUBSCRIPTIONS_COLL)
                                .document("categories")
                                .collection(CATEGORIES_COLL).document(currentCat)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            //user has already subd to this cat
                                            //unsub user
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
                                            coMeth.stopLoading(progressDialog);
                                        } else {
                                            //user has not subd to this cat
                                            //sub user to cur cat
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
                                            coMeth.stopLoading(progressDialog);
                                            //notify user
                                            showSnack(getString(R.string.sub_to_text) +
                                                    getSupportActionBar().getTitle());
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: failed to sub user to cat\n" +
                                                e.getMessage());
                                        coMeth.stopLoading(progressDialog);
                                        showSnack(getResources().getString(R.string.failed_to_sub) +
                                                getSupportActionBar().getTitle()
                                                + ": " + e.getMessage());
                                    }
                                });
//
//
//
//                                .addOnCompleteListener(ViewCategoryActivity.this, new OnCompleteListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                        //get data from teh likes collection
//
//                                        //check if current user has already subscribed
//                                        if (!task.getResult().exists()) {
//                                            Map<String, Object> catsMap = new HashMap<>();
//                                            catsMap.put("key", currentCat);
//                                            catsMap.put("value", getSupportActionBar().getTitle());
//                                            catsMap.put("desc", catDesc);
//                                            catsMap.put("timestamp", FieldValue.serverTimestamp());
//
//                                            //subscribe user
//                                            coMeth.getDb().collection("Users/" + userId + "/Subscriptions")
//                                                    .document("categories")
//                                                    .collection("Categories")
//                                                    .document(currentCat).set(catsMap);
//                                            //set image
//                                            subscribeFab.setImageResource(R.drawable.ic_action_subscribed);
//                                            //subscribe to notifications
//                                            //subscribe to app updates
//                                            FirebaseMessaging.getInstance().subscribeToTopic(currentCat);
//                                            Log.d(TAG, "user subscribed to topic {CURRENT CAT}");
//                                            //notify user
//                                            showSnack(getString(R.string.sub_to_text) +
//                                                    getSupportActionBar().getTitle());
//
//                                        } else {
//
//                                            //unsubscribe
//                                            coMeth.getDb()
//                                                    .collection("Users/" + userId + "/Subscriptions")
//                                                    .document("categories")
//                                                    .collection("Categories")
//                                                    .document(currentCat).delete();
//                                            //unsubscribe to app updates
//                                            FirebaseMessaging.getInstance().unsubscribeFromTopic(currentCat);
//                                            Log.d(TAG, "user unSubscribe to topic {CURRENT CAT}");
//                                            //set fab image
//                                            subscribeFab.setImageResource(R.drawable.ic_action_subscribe);
//                                        }
//                                    }
//                                });

                    } else {

                        //prompt login
                        goToLogin(getString(R.string.log_to_subscribe_text));

                    }

                } else {

                    //user is not connected to internet
                    showNoActionSnack(getString(R.string.failed_to_connect_text));

                }

            }
        });

        //loading
        showProgress(getString(R.string.loading_text));
        //handle showing posts
        //listen for scrolling on the homeFeedView
        /*catFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
                Boolean reachedBottom = !catFeed.canScrollVertically(1);
                if (reachedBottom) {
                    Log.d(TAG, "at addOnScrollListener\n reached bottom");
                    loadMorePosts();
                }
            }
        });*/

        final Query firstQuery = coMeth.getDb().
                collection("Categories/" + currentCat + "/Posts")
                /*.orderBy("event_date", Query.Direction.ASCENDING)*/;
        postsList.clear();
        usersList.clear();
        loadPosts(firstQuery, currentCat);
        //if post list after loading posts is empty, load more posts


        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //get new posts
                catFeed.getRecycledViewPool().clear();
                postsList.clear();
                usersList.clear();
                loadPosts(firstQuery, currentCat);
            }
        });
    }

    /**
     * show a snackBar without the action button
     */
    private void showNoActionSnack(String message) {
        Snackbar.make(findViewById(R.id.viewCatLayout),
                message, Snackbar.LENGTH_LONG)
                .show();
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

    /**
     * handle the deepLink intent
     *
     * @param intent the deepLink intents
     * @return String the category from a deepLink
     */
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
    /**
     * set the selected category
     * @param category the selected category
     * */
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
        "Queries"
        "Exhibitions"*/

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
                    getSupportActionBar().setTitle(getString(R.string.cat_queries));
                    break;
                case "exhibitions":
                    getSupportActionBar().setTitle(getString(R.string.cat_exhibitions));
                    break;
                case "art":
                    getSupportActionBar().setTitle(getString(R.string.cat_art));
                    break;
                case "apps":
                    getSupportActionBar().setTitle(getString(R.string.cat_apps));
                    break;
                case "groups":
                    getSupportActionBar().setTitle(getString(R.string.cat_groups));
                    break;
                default:
                    Log.d(TAG, "onCreate: default is selected");
            }
        }
    }
    /**
     * load posts from the database
     * @param firstQuery the first query when the page is first loaded
     * */
    private void loadPosts(Query firstQuery, String cat) {

        if (cat.equals("popular")) {
            getPopularPosts();
        } else if (cat.equals("upcoming")) {
            getUpcomingPosts();
        } else {

            firstQuery.limit(10)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            //handle first load
                            if (isFirstPageFirstLoad) {

                                //get the last visible post
                                try {
                                    lastVisiblePost = queryDocumentSnapshots.getDocuments()
                                            .get(queryDocumentSnapshots.size() - 1);
                                } catch (Exception exception) {
                                    Log.d(TAG, "error: " + exception.getMessage());
                                }
                            }
                            //get posts
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                //get post id
                                String postId = document.getId();
                                //get post details from Posts collection
                                getPostDetails(postId);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: failed to get post ids from cats\n" +
                                    e.getMessage());
                        }
                    });
            isFirstPageFirstLoad = false;
        }
    }

    private void getUpcomingPosts() {
        Log.d(TAG, "getUpcomingPosts: ");

        // today
        Calendar date = new GregorianCalendar();
        // reset hour, minutes, seconds and millis
//        date.set(Calendar.HOUR_OF_DAY, 0);
//        date.set(Calendar.MINUTE, 0);
//        date.set(Calendar.SECOND, 0);
//        date.set(Calendar.MILLISECOND, 0);
        date.add(Calendar.MONTH, 2);

        Calendar dateLimit = new GregorianCalendar();
        dateLimit.add(Calendar.MONTH, 2);
        Log.d(TAG, "getUpcomingPosts: date is: " + date + "\ndateLimit is: " + dateLimit);

        coMeth.getDb()
                .collection(CoMeth.POSTS)
                .orderBy("event_date", Query.Direction.DESCENDING)
                .whereGreaterThan("event_date", date.getTime())
//                .whereLessThan("event_date", dateLimit.getTime())
//                .limit(20)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //events are present
                            //loop through documents
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String postId = document.getId();
                                //convert snapshot to post
                                Posts post = document.toObject(Posts.class).withId(postId);
//                                getFilteredPosts(post);
                                getFutureDatedPosts(post);

                            }

                        } else {
                            //no events in that time frame
                            coMeth.stopLoading(progressDialog, swipeRefresh);
                            showSnack("couldn't find posts");
                            Log.d(TAG, "onSuccess: no events in given time frame");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get posts for event\n" +
                                e.getMessage());
                    }
                });

    }

    private void getPostDetails(final String postId) {
        Log.d(TAG, "getPostDetails: ");
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //post exists
                            //convert post to object
                            Posts post = documentSnapshot.toObject(Posts.class).withId(postId);
                            manageCats(post, postId);
                        } else {
                            //post does not exist
                            //delete post id ref from cats
                            coMeth.getDb()
                                    .collection(CATEGORIES_COLL + "/" + currentCat +
                                            "/" + POSTS_COLL)
                                    .document(postId)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: deleted post id ref " +
                                                    "of post that does not exist");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: failed to delete post " +
                                                    "id doc ref for post that does not exist\n" +
                                                    e.getMessage());
                                        }
                                    });
                        }
                    }
                });
    }

    private void manageCats(Posts post, String postId) {
        Log.d(TAG, "manageCats: ");
        switch (currentCat) {
            /*case "featured":
                break;*/
            /*case "popular":
                getPopularPosts();
                break;*/
            /*case "upcoming":
                getFutureDatedPosts(post);
                break;*/
            case "events":
                checkForEventDate(post);
                break;
            case "places":
                checkPostCategories(post);
                break;
            case "services":
                checkPostCategories(post);
                break;
            case "business":
                checkPostCategories(post);
                break;
            case "buysell":
                checkPostCategories(post);
                break;
            case "education":
                checkForEventDate(post);
                break;
            case "jobs":
                checkForEventDate(post);
                break;
            case "queries":
                checkPostCategories(post);
                break;
            case "exhibitions":
                checkForEventDate(post);
                break;
            case "art":
                checkPostCategories(post);
                break;
            case "apps":
                checkPostCategories(post);
                break;
            case "groups":
                checkPostCategories(post);
                break;
            default:
                Log.d(TAG, "onCreate: default is selected");
        }
    }

    private void getPopularPosts() {
        Log.d(TAG, "getPopularPosts: ");
        coMeth.getDb()
                .collection(POSTS_COLL)
                .orderBy("views", Query.Direction.DESCENDING)
                .whereGreaterThan("views", 10)
                .limit(20)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                //convert document to post
                                String postId = document.getId();
                                Posts post = document.toObject(Posts.class).withId(postId);
                                getFilteredPosts(post);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get popular posts\n" +
                                e.getMessage());
                    }
                });

    }

    private void checkPostCategories(Posts post) {
        Log.d(TAG, "checkPostCategories: ");
        if (post.getCategories() != null && post.getCategories().contains(currentCat)) {
            //post has cats, and post cats contain current cat
            Log.d(TAG, "checkPostCategories: " +
                    "post has cats, and post cats contain current cat");
            getFilteredPosts(post);
        }
    }
    /**
     * handle the subscribed fab on the view categories page
     */
    private void setFab() {

        Log.d(TAG, "setFab: called");
        userId = coMeth.getUid();
        //check if user is subscribed
        coMeth.getDb()
                .collection(USERS_COLL + "/" + userId + "/" + SUBSCRIPTIONS_COLL)
                .document(CATEGORIES_DOC)
                .collection(CATEGORIES_COLL).document(currentCat)
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



    private void checkForEventDate(Posts post) {
        if (post.getEvent_date() != null) {
            getFutureDatedPosts(post);
        } else {
            getFilteredPosts(post);
        }
    }

    private void getFutureDatedPosts(Posts post) {
        if (post.getEvent_date() != null) {
            Date now = new Date();
            long twoThouNineHundYears = new Date(1970, 0, 0).getTime();
            Date eventDate = new Date(post.getEvent_date().getTime() - twoThouNineHundYears);
            Log.d(TAG, "filterCat: post has event date" +
                    "\nevent date is: " + eventDate +
                    "\nnow is: " + now);

            if (eventDate.after(now)) {
                Log.d(TAG, "filterCat: cat is upcoming\nposts are after now");
                postsList.clear();
                usersList.clear();
                getFilteredPosts(post);
            }
        }
    }

    /**
     * get the filtered posts
     * @param post the post to process
     * */
    private void getFilteredPosts(final Posts post) {
        String postUserId = post.getUser_id();
        coMeth.getDb()
                .collection("Users")
                .document(postUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //posts with event dates
                            addPost(documentSnapshot, post);
                        } else {
                            //user does not exist
                            coMeth.stopLoading(progressDialog, swipeRefresh);
                            Log.d(TAG, "onComplete: cat has no posts");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get user \n" + e.getMessage());
                    }
                });
    }

    private void addPost(DocumentSnapshot documentSnapshot, Posts post) {
        String userId = documentSnapshot.getId();
        Users user = documentSnapshot.toObject(Users.class).withId(userId);
        //add new post to the local postsList
        if (isFirstPageFirstLoad) {

            //add the post at position 0 of the postsList
            if (!postsList.contains(post)) {
                postsList.add(0, post);
                usersList.add(0, user);
//                                                coMeth.stopLoading(progressDialog, swipeRefresh);
                Log.d(TAG, "onComplete: added post \n" + post.getTitle() +
                        post.getViews());
            }

        } else {

            //if the first page is loaded the add new post normally
            if (!postsList.contains(post)) {
                postsList.add(post);
                usersList.add(user);
//                                                coMeth.stopLoading(progressDialog, swipeRefresh);
                Log.d(TAG, "onComplete: added post \n" + post.getTitle() +
                        post.getViews());
            }
        }
        //notify the recycler adapter of the set change
        categoryRecyclerAdapter.notifyDataSetChanged();
        //stop loading after first post is visible
//                                        coMeth.onResultStopLoading(postsList, progressDialog, swipeRefresh);
        coMeth.stopLoading(progressDialog, swipeRefresh);
    }

    //for loading more posts

    /**
     * load more posts
     * */
    /*public void loadMorePosts() {

        Log.d(TAG, "loadMorePosts: ");
        Query nextQuery = coMeth.getDb()
                .collection("Posts")
                .orderBy("event_date", Query.Direction.ASCENDING)
                .startAfter(lastVisiblePost)
                .limit(20);

        //get all posts from the database
        nextQuery.addSnapshotListener(ViewCategoryActivity.this, new EventListener<QuerySnapshot>() {
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
    }*/


    //show snack

    /**
     * show snackBar to notify user
     * @param message the message to notify the user
     * */
    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.viewCatLayout),
                message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.see_list_text), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //go to userPage to view subscriptions
                        goToUserPage();

                    }
                })
                .show();
    }

    /**
     * open the user page for current user
     */
    private void goToUserPage() {
        Intent goToUserPageIntent =
                new Intent(ViewCategoryActivity.this, UserPageActivity.class);
        goToUserPageIntent.putExtra(
                getResources().getString(R.string.USER_ID_NAME), coMeth.getUid());
        startActivity(goToUserPageIntent);
        finish();

    }

    /**
     * go to the login page with a message for the user
     * @param message the message to display to the user on the login page
     * */
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

    /**
     * show the progress dialog
     * @param message the message to display while showing progress
     * */
    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }
}