package com.nyayozangu.labs.fursa.activities.main;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.providers.MySuggestionProvider;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * search for items
 * Created by Sean on 4/13/18.
 */

public class SearchableActivity extends AppCompatActivity {

    // TODO: 4/18/18 handle saerch when the search result has no content
    private static final String TAG = "Sean";
    //common methods
    private CoMeth coMeth = new CoMeth();
    private String searchQuery;
    private RecyclerView searchFeed;

    //retrieve posts
    private List<Posts> postsList;
    private List<Users> usersList;

    //recycler adapter
    private PostsRecyclerAdapter searchRecyclerAdapter;
    private DocumentSnapshot lastVisiblePost;
    private Boolean isFirstPageFirstLoad = true;
    private String locString = "";
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);


        Toolbar toolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.search_text)); // TODO: 4/29/18 saerch title not showing
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SearchableActivity.this, MainActivity.class));
                finish();

            }
        });


        //initiate items
        searchFeed = findViewById(R.id.searchRecyclerView);

        //initiate an arrayList to hold all the posts
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();

        //initiate the PostsRecyclerAdapter
        String className = "SearchableActivity";
        searchRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className);
        //set a layout manager for searchFeed (recycler view)
        searchFeed.setLayoutManager(new LinearLayoutManager(this));
        //set an adapter for the recycler view
        searchFeed.setAdapter(searchRecyclerAdapter);
        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            searchQuery = intent.getStringExtra(SearchManager.QUERY).toLowerCase();
            Log.d(TAG, "handleIntent: \nquery is" + searchQuery);
            // TODO: 4/16/18 continue suggested query search
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
                    this,
                    MySuggestionProvider.AUTHORITY,
                    MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(searchQuery, null);
            doMySearch(searchQuery);

        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {

            // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
            searchQuery = intent.getStringExtra(SearchManager.QUERY).toLowerCase();
            doMySearch(searchQuery);

        }
    }

    private void doMySearch(final String query) {

        Log.d(TAG, "doMySearch: ");
        //listen for scrolling on the searchFeed
        searchFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !searchFeed.canScrollVertically(1);
                if (reachedBottom) {

                    loadMorePosts(query);
                }
            }
        });


        //loading
        showProgress(getString(R.string.searching_text));

        final Query firstQuery = coMeth.getDb()
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);
        //get all posts from the database
        loadPosts(firstQuery, query);

    }

    private void loadPosts(Query firstQuery, final String searchQuery) {
        firstQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    //check if the data is loaded for the first time
                    if (isFirstPageFirstLoad) {

                        lastVisiblePost = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        postsList.clear();
                        usersList.clear();

                    }

                }


                //create a for loop to check for document changes
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    //check if an item is added
                    if (doc.getType() == DocumentChange.Type.ADDED) {

                        String postId = doc.getDocument().getId();
                        Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
                        //filter posts
                        filterPosts(post, searchQuery);

                    }
                }
                // TODO: 5/2/18 hanlde no posts found notif
                /*if (postsList.isEmpty()) {

                    //no posts
                    showSnack("Posts not found");

                }*/

                //the first page has already loaded
                isFirstPageFirstLoad = false;

            }
        });
    }


    private void filterPosts(final Posts post, final String searchQuery) {

        String title = post.getTitle().toLowerCase();
        String desc = post.getDesc().toLowerCase();
        //handle price
        if (post.getPrice() != null) {
            String price = post.getPrice();
        }
        //handle categories
        String catString = "";
        if (post.getCategories() != null) {
            ArrayList catsArray = post.getCategories();
            for (int i = 0; i < catsArray.size(); i++) {

                catString = catString.concat(coMeth.getCatValue((String) catsArray.get(i)) + " ");

            }
        }
        // handle contact
        ArrayList contactArray = new ArrayList();
        if (post.getContact_details() != null) {

            contactArray = post.getContact_details();

        }
        //handle location search
        locString = "";
        if (post.getLocation() != null) {

            ArrayList locArray = post.getLocation();
            for (int i = 0; i < locArray.size(); i++) {

                locString = locString.concat(locArray.get(i).toString() + " ");

            }

        }
        //handle postUser
        String postUserId = post.getUser_id();
        coMeth.getDb()
                .collection("Users")
                .document(postUserId)
                .get()
                .addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful() && task.getResult().exists()) {

                            Users user = task.getResult().toObject(Users.class);
                            String username = user.getName();
                            if (username.contains(searchQuery)) getFilteredPosts(post);

                        }

                    }
                });
        //handle event date
        String eventDateString = "";
        if (post.getEvent_date() != null) {

            Date eventDate = post.getEvent_date();
            long eventDateMils = eventDate.getTime();
            eventDateString = DateFormat.format("EEE, MMM d, 20yy\nh:mm a", new Date(eventDateMils)).toString();

        }

        if (title.toLowerCase().contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (desc.toLowerCase().contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (locString.toLowerCase().contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (catString.toLowerCase().contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (contactArray.contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (eventDateString.toLowerCase().contains(searchQuery)) {
            getFilteredPosts(post);
        }

        Log.d(TAG, "filterPosts: " +
                "\nlocString: " + locString +
                "\ncatString: " + catString +
                "\ncontactArray: " + contactArray +
                "\neventDateString: " + eventDateString);
    }

    private void getFilteredPosts(final Posts post) {

        Log.d(TAG, "getFilteredPosts: called");
        //get user_id for post
        String postUserId = post.getUser_id();
        coMeth.getDb()
                .collection("Users")
                .document(postUserId)
                .get()
                .addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        //check if task is successful
                        if (task.isSuccessful() && task.getResult().exists()) {

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
                            searchRecyclerAdapter.notifyDataSetChanged();
                            coMeth.stopLoading(progressDialog, null);

                        } else {

                            //task failed
                            if (!task.isSuccessful()) {

                                Log.d(TAG, "onComplete: getting users task failed " + task.getException());

                            } else if (!task.getResult().exists()) {

                                //user does not exist
                                Log.d(TAG, "onComplete: user does not exist");

                            }

                            coMeth.stopLoading(progressDialog, null);


                        }

                        coMeth.stopLoading(progressDialog, null);

                    }
                });
    }

    //for loading more posts
    public void loadMorePosts(final String searchQuery) {

        Query nextQuery = coMeth.getDb()
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(10);

        nextQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                //check if there area more posts
                if (!queryDocumentSnapshots.isEmpty()) {

                    //get the last visible post
                    lastVisiblePost = queryDocumentSnapshots
                            .getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);
                    //create a for loop to check for document changes
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        //check if an item is added
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String postId = doc.getDocument().getId();
                            Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
                            //filter posts
                            filterPosts(post, searchQuery);

                        }
                    }
                    Log.d(TAG, "onEvent: \nadded posts to postlist, postlist is: " + postsList);

                }

            }
        });

    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    //show snack
    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.searchView),
                message, Snackbar.LENGTH_LONG)
                .show();
    }

}
