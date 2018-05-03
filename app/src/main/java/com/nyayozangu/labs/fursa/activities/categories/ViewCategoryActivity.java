package com.nyayozangu.labs.fursa.activities.categories;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class
ViewCategoryActivity extends AppCompatActivity {

    private static final String TAG = "Sean";

    private RecyclerView catFeed;
    private SwipeRefreshLayout swipeRefresh;

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
                finish();
            }
        });

        //initiate items
        catFeed = findViewById(R.id.catRecyclerView);

        //initiate an arrayList to hold all the posts
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();

        String className = "ViewCategoryActivity";
        categoryRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className);
        catFeed.setLayoutManager(new LinearLayoutManager(this));
        catFeed.setAdapter(categoryRecyclerAdapter);

        //get the sent intent
        if (getIntent() != null) {
            Log.d(TAG, "getIntent is not null");
            Intent getPostIdIntent = getIntent();
            String category = getPostIdIntent.getStringExtra("category");
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

                    default:
                        Log.d(TAG, "onCreate: default is selected");
                }

            }


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
                        showProgress("Subscribing...");

                        userId = coMeth.getUid();

                        coMeth.getDb().collection("Users/" + userId + "/Subscriptions")
                                .document("categories")
                                .collection("Categories").document(currentCat).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

                        progressDialog.dismiss();

                    } else {

                        //prompt login
                        showLoginAlertDialog(getString(R.string.log_to_subscribe_text));

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
                .limit(10)
                .orderBy("timestamp", Query.Direction.DESCENDING);
        loadPosts(firstQuery);



        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //get new posts
                postsList.clear();
                usersList.clear();
                catFeed.getRecycledViewPool().clear();
                loadPosts(firstQuery);


            }
        });
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
        final String category = getIntent().getStringExtra("category");

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

        Log.d(TAG, "filterCat: at filter cat");
        //check if current post contains business

        final Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
        ArrayList catsArray = post.getCategories();
        Log.d(TAG, "filterCat: \ncatsArray is: " + catsArray);
        if (catsArray != null) {

            Log.d(TAG, "filterCat: catsArray is not null");
            //check if post contains cat
            if (catsArray.contains(category)) {

                String postUserId = post.getUser_id();
                coMeth.getDb()
                        .collection("Users")
                        .document(postUserId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                if (task.isSuccessful()) {

                                    //check if user exists
                                    if (task.getResult().exists()) {

                                        Users user = task.getResult().toObject(Users.class);
                                        //add new post to the local postsList
                                        if (isFirstPageFirstLoad) {

                                            //if the first page is loaded the add new post normally
                                            postsList.add(post);
                                            usersList.add(user);

                                        } else {

                                            //add the post at position 0 of the postsList
                                            postsList.add(0, post);
                                            usersList.add(0, user);

                                        }
                                        //notify the recycler adapter of the set change
                                        categoryRecyclerAdapter.notifyDataSetChanged();

                                    } else {

                                        //cat has no posts
                                        showSnack("There are no posts in this category");
                                        Log.d(TAG, "onComplete: cat has no posts");

                                    }

                                } else {

                                    //task has failed
                                    Log.d(TAG, "onComplete: task has failed: " + task.getException());

                                }

                            }
                        });

            } else {

                //posts dont have current cat
                // TODO: 5/3/18 show the no posts in current cat

            }

        }


    }


    //for loading more posts
    public void loadMorePosts() {

        Log.d(TAG, "loadMorePosts: ");
        Query nextQuery = coMeth.getDb()
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(10);

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
                        coMeth.goToMySubscriptions();

                    }
                })
                .show();
    }

    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(ViewCategoryActivity.this);
        loginAlertBuilder.setTitle("Login")
                .setIcon(getDrawable(R.drawable.ic_action_red_alert))
                .setMessage("You are not logged in\n" + message)
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send user to login activity
                        Intent loginIntent = new Intent(ViewCategoryActivity.this, LoginActivity.class);
                        loginIntent.putExtra("source", "categories");
                        loginIntent.putExtra("category", currentCat);
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

    //go to login page
    private void goToLogin() {
        startActivity(new Intent(ViewCategoryActivity.this, LoginActivity.class));
    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

}
