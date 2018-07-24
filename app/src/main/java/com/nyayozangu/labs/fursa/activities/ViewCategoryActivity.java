package com.nyayozangu.labs.fursa.activities;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.models.Posts;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Users;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.CATEGORIES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.CATEGORIES_DOC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SUBSCRIPTIONS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TIMESTAMP;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;

public class
ViewCategoryActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    private static final String CATEGORY = "category";

    private RecyclerView catFeed;
    private FloatingActionButton subscribeFab;

    //retrieve posts
    private List<Posts> postsList;
    private List<Users> usersList;
    private CoMeth coMeth = new CoMeth();

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
        SearchView searchView = (SearchView) menu.findItem(R.id.viewCatSearchMenuItem).getActionView();
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

        Toolbar toolbar = findViewById(R.id.viewCatToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMain();
            }
        });

        catFeed = findViewById(R.id.catRecyclerView);
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();

        String className = "ViewCategoryActivity";
        categoryRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className,
                Glide.with(this), this);
        coMeth.handlePostsView(
                ViewCategoryActivity.this, ViewCategoryActivity.this, catFeed);
        catFeed.setAdapter(categoryRecyclerAdapter);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catFeed.smoothScrollToPosition(0);
            }
        });
        handleIntent();
        subscribeFab = findViewById(R.id.subscribeCatFab);
        showProgress(getString(R.string.loading_text));
        if (coMeth.isConnected()) {
            if (coMeth.isLoggedIn()) {
                setFab();
            }
        } else {
            coMeth.stopLoading(progressDialog);
            showNoActionSnack(getString(R.string.failed_to_connect_text));
        }
        handleSubFab();
        loadPosts(currentCat);
    }

    private void handleSubFab() {
        subscribeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coMeth.isConnected()) {
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
                            catsMap.put("key", currentCat);
                            catsMap.put("value", getSupportActionBar().getTitle());
                            catsMap.put("desc", catDesc);
                            catsMap.put(TIMESTAMP, FieldValue.serverTimestamp());
                            //subscribe user
                            currentCatDb.set(catsMap);
                            subscribeFab.setImageResource(R.drawable.ic_action_subscribed);
                            //subscribe to app updates
                            FirebaseMessaging.getInstance().subscribeToTopic(currentCat);
                            coMeth.stopLoading(progressDialog);
                            showSnack(getString(R.string.sub_to_text) + " " + getSupportActionBar().getTitle());
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
                                Objects.requireNonNull(getSupportActionBar()).getTitle()
                                + ": " + e.getMessage());
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
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(CATEGORY) != null) {
                String category = intent.getStringExtra(CATEGORY);
                setCurrentCat(category);
            } else {
                //intent is from deep link
                String category = handleDeepLink(getIntent());
                setCurrentCat(category);
            }
        }else{
            goToMain();
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

    private void loadPosts(String cat) {

        final Query firstQuery = coMeth.getDb().
                collection(CATEGORIES + "/" + currentCat + "/" + POSTS)
                .orderBy(TIMESTAMP, Query.Direction.ASCENDING);
        postsList.clear();
        usersList.clear();
        switch (cat) {
            case "popular":
                getPopularPosts();
                break;
            case "upcoming":
                getUpcomingPosts();
                break;
            default:

                firstQuery.limit(10).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                //handle first load
                                if (isFirstPageFirstLoad) {
                                    try {
                                        lastVisiblePost = queryDocumentSnapshots.getDocuments()
                                                .get(queryDocumentSnapshots.size() - 1);
                                    } catch (Exception e) {
                                        Log.d(TAG, "onSuccess: error\n" + e.getMessage());
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
                break;
        }
    }

    private void getUpcomingPosts() {
        Log.d(TAG, "getUpcomingPosts: ");
        // today
        Calendar date = new GregorianCalendar();
        date.add(Calendar.MONTH, 2);

        Calendar dateLimit = new GregorianCalendar();
        dateLimit.add(Calendar.MONTH, 2);
        Log.d(TAG, "getUpcomingPosts: date is: " + date + "\ndateLimit is: " + dateLimit);

        coMeth.getDb()
                .collection(POSTS)
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
                            coMeth.stopLoading(progressDialog);
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
                            Posts post = Objects.requireNonNull(
                                    documentSnapshot.toObject(Posts.class)).withId(postId);
                            manageCats(post, postId);
                        }
                    }
                });
    }

    private void manageCats(Posts post, String postId) {
        Log.d(TAG, "manageCats: ");
        switch (currentCat) {
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
        coMeth.getDb()
                .collection(POSTS)
                .orderBy(CoMeth.ACTIVITY, Query.Direction.DESCENDING)
                .whereGreaterThan(CoMeth.ACTIVITY, 20)
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
                .collection(USERS + "/" + userId + "/" + SUBSCRIPTIONS)
                .document(CATEGORIES_DOC)
                .collection(CATEGORIES).document(currentCat)
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
                            coMeth.stopLoading(progressDialog);
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
                Log.d(TAG, "onComplete: added post \n" + post.getTitle() +
                        post.getViews());
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
        //stop loading
        coMeth.stopLoading(progressDialog);
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