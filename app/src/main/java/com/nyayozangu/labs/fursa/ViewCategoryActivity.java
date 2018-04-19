package com.nyayozangu.labs.fursa;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
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

public class ViewCategoryActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView catFeed;
    private SwipeRefreshLayout swipeRefresh;

    private FloatingActionButton subscribeFab;

    //retrieve posts
    private List<Posts> postsList;

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

        //initiate the PostsRecyclerAdapter
        categoryRecyclerAdapter = new PostsRecyclerAdapter(postsList);

        //set a layout manager for catFeed (recycler view)
        catFeed.setLayoutManager(new LinearLayoutManager(this));

        //set an adapter for the recycler view
        catFeed.setAdapter(categoryRecyclerAdapter);

        //initiate firebase auth
        mAuth = FirebaseAuth.getInstance();

        //initiate the firebase elements
        db = FirebaseFirestore.getInstance();


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


        if (isConnected()) {

            if (isLoggedIn()) {

                //check if user is subscribed and set fab
                setFab();
            }

        } else {

            //user is not connected to internet
            showSnack(R.id.viewCatLayout, getString(R.string.connection_error_message));

        }

        //handle fab
        subscribeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if device is connected
                if (isConnected()) {
                    Log.d(TAG, "onClick: is connected");

                    //check if user is logged in
                    if (isLoggedIn()) {
                        Log.d(TAG, "onClick: is logged in");


                        //show progress
                        showProgress("Subscribing...");

                        userId = mAuth.getCurrentUser().getUid();

                        db.collection("Users/" + userId + "/Subscriptions").document("categories").collection("Categories").document(currentCat).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                                    db.collection("Users/" + userId + "/Subscriptions").document("categories").collection("Categories").document(currentCat).set(catsMap);
                                    //user is not subscribed
                                    subscribeFab.setImageResource(R.drawable.ic_action_subscribed);
                                    //notify user
                                    showSnack(R.id.viewCatLayout, "Subscribed to " + getSupportActionBar().getTitle());

                                } else {
                                    //unsubscribe
                                    db.collection("Users/" + userId + "/Subscriptions").document("categories").collection("Categories").document(currentCat).delete();
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
                    showSnack(R.id.viewCatLayout, getString(R.string.connection_error_message));

                }

            }
        });

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

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                categoryRecyclerAdapter.notifyDataSetChanged();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        swipeRefresh.setRefreshing(false);
                    }
                }, 1500);

            }
        });


        Query firstQuery = db.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
        //get all posts from the database
        //use snapshotListener to get all the data real time
        firstQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                //check if the data is loaded for the first time
                /**
                 * if new data is added it will be added to the first query not the second query
                 */
                if (isFirstPageFirstLoad) {

                    //get the last visible post
                    try {
                        lastVisiblePost = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                    } catch (Exception exception) {
                        Log.d(TAG, "error: " + exception.getMessage());
                    }

                }


                //create a for loop to check for document changes
                for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    //check if an item is added
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        //a new item/ post is added

                        //get the post id for likes feature
                        final String postId = doc.getDocument().getId();
                        processCategories(doc, postId);


                    }
                }

                //the first page has already loaded
                isFirstPageFirstLoad = false;

            }
        });


    }

    private void setFab() {

        Log.d(TAG, "setFab: called");

        userId = mAuth.getCurrentUser().getUid();
        //check if user is subscribed
        db.collection("Users/" + userId + "/Subscriptions").document("categories").collection("Categories").document(currentCat).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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


    private String getKey(String catValue) {


        switch (catValue) {

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

            case "Featured":
                return "featured";

            case "Popular":
                return "popular";

            case "Up coming":
                return "upcoming";

            case "Events":
                return "events";

            case "Business":
                return "business";

            case "Buying and sell":
                return "buysell";

            case "Education":
                return "education";

            case "Jobs":
                return "jobs";


            case "Queries":
                return "queries";

            default:
                return "catValue";

        }

    }

    private void processCategories(DocumentChange doc, String postId) {
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
        //check if current post contains business
        db.collection("Posts").document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                //check if post exists
                if (documentSnapshot.exists()) {
                    //post exists
                    if (documentSnapshot.get("categories") != null) {

                        //post has categories
                        //get cats
                        ArrayList categories = (ArrayList) documentSnapshot.get("categories");

                        if (categories.contains(category)) {

                            //cat has business
                            Posts post = doc.getDocument().toObject(Posts.class).withId(postId);

                            if (isFirstPageFirstLoad) {

                                //if the first page is loaded the add new post normally
                                postsList.add(post);

                            } else {

                                //add the post at position 0 of the postsList
                                postsList.add(0, post);

                            }
                            //notify the recycler adapter of the set change
                            categoryRecyclerAdapter.notifyDataSetChanged();

                        }

                    }

                }

            }
        });
    }


    //for loading more posts
    public void loadMorePosts() {

        Query nextQuery = db.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(10);


        //get all posts from the database
        //use snapshotListener to get all the data real time
        nextQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

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

                                //get the post id for likes feature
                                String postId = doc.getDocument().getId();

                                processCategories(doc, postId);

                            }
                        }

                    }
                } catch (NullPointerException nullExeption) {
                    //the Query is null
                    Log.e(TAG, "error: " + nullExeption.getMessage());
                }
            }
        });
    }


    //show snack
    private void showSnack(int layoutId, String message) {
        Snackbar.make(findViewById(layoutId),
                message, Snackbar.LENGTH_LONG).show();
    }


    //check if device is connected to the internet
    private boolean isConnected() {

        //check if there's a connection
        Log.d(TAG, "at isConnected");
        Context context = getApplicationContext();
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {

            activeNetwork = cm.getActiveNetworkInfo();

        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }

    private boolean isLoggedIn() {
        //determine if user is logged in
        return mAuth.getCurrentUser() != null;
    }


    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(ViewCategoryActivity.this);
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
