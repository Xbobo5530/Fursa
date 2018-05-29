package com.nyayozangu.labs.fursa.activities.main;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

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

    // TODO: 4/18/18 handle search when the search result has no content
    private static final String TAG = "Sean";
    //common methods
    private CoMeth coMeth = new CoMeth();
    private String searchQuery;
    private RecyclerView searchFeed;
    private SearchView searchView;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_toolbar_menu, menu);
        //handle search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        //handle search
        Toolbar toolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.search_text));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        coMeth.handlePostsView(
                SearchableActivity.this, SearchableActivity.this, searchFeed);
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

        } else if (getIntent() != null &&
                getIntent().getStringExtra("tag") != null) {
            String searchQuery = getIntent().getStringExtra("tag");
            doMySearch(searchQuery);
        }

        hideKeyBoard();
    }

    private void doMySearch(final String query) {

        Log.d(TAG, "doMySearch: \nquery is: " + query);
        //loading
        showProgress(getString(R.string.searching_text));
        //clear old search
        postsList.clear();
        usersList.clear();
        //search for new content
        final Query firstQuery = coMeth.getDb()
                .collection("Posts");
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
                    String postId = doc.getDocument().getId();
                    Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        //filter posts
                        filterPosts(post, searchQuery);
                    } else if (doc.getType() == DocumentChange.Type.REMOVED) {
                        // TODO: 5/25/18 test if documet is removed
                        if (postsList.contains(post)) {
                            postsList.remove(post);
                        }
                    }
                }
                // TODO: 5/2/18 handle no posts found notif
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
            String price = post.getPrice().toLowerCase();
        }
        //image labels
        String imageLabels = "";
        if (post.getImage_labels() != null) {
            imageLabels = post.getImage_labels().toLowerCase().trim();
        }
        //image text
        String imageText = "";
        if (post.getImage_text() != null) {
            imageText = post.getImage_text().toLowerCase().trim();
        }

        //handle categories
        String catString = "";
        if (post.getCategories() != null) {
            ArrayList catsArray = post.getCategories();
            for (int i = 0; i < catsArray.size(); i++) {

                catString = catString.concat(
                        coMeth.getCatValue(
                                (catsArray.get(i)).toString()).toLowerCase() + " ");

            }
        }
        //handle tags
        String tagsString = "";
        if (post.getTags() != null) {
            ArrayList tags = post.getTags();
            for (int i = 0; i < tags.size(); i++) {
                tagsString = tagsString.concat(tags.get(i) + " ");
            }
            Log.d(TAG, "filterPosts: \ntags string is: " + tagsString + "\ntags are: " + tags);
        }
        // handle contact
        ArrayList contactArray;
        String contactString = "";
        if (post.getContact_details() != null) {
            contactArray = post.getContact_details();
            for (int i = 0; i < contactArray.size(); i++) {
                contactString = contactString.concat(
                        contactArray.get(i).toString().toLowerCase() + " ");
            }
        }
        //handle location search
        locString = "";
        if (post.getLocation() != null) {

            ArrayList locArray = post.getLocation();
            for (int i = 0; i < locArray.size(); i++) {
                locString = locString.concat(
                        locArray.get(i).toString().toLowerCase() + " ");
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
                            String username = user.getName().toLowerCase();
                            if (username.contains(searchQuery)) getFilteredPosts(post);
                            if (user.getBio() != null) {
                                String bio = user.getBio().toLowerCase();
                                if (bio.contains(searchQuery)) getFilteredPosts(post);
                            }
                        }

                    }
                });
        //handle event date
        String eventDateString = "";
        if (post.getEvent_date() != null) {

            Date eventDate = post.getEvent_date();
            long eventDateMils = eventDate.getTime();
            eventDateString = DateFormat.format(
                    "EEE, MMM d, 20yy", new Date(eventDateMils)).toString().toLowerCase();
        }

        if (title.contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (desc.contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (imageLabels.contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (tagsString.contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (imageText.contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (locString.contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (catString.contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (contactString.contains(searchQuery)) {
            getFilteredPosts(post);
        }
        if (eventDateString.contains(searchQuery)) {
            getFilteredPosts(post);
        }
    }

    private void getFilteredPosts(final Posts post) {

        Log.d(TAG, "getFilteredPosts: called");
        //get user_id for post
        final String postUserId = post.getUser_id();
        coMeth.getDb()
                .collection("Users")
                .document(postUserId)
                .get()
                .addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        //check if task is successful
                        if (task.isSuccessful() && task.getResult().exists()) {

                            String psotUserId = task.getResult().getId();
                            Users user = task.getResult().toObject(Users.class).withId(postUserId);
                            //check if post is already added to the post list
                            if (!postsList.contains(post)) {
                                //add new post to the local postsList
                                postsList.add(post);
                                usersList.add(user);
                                //notify the recycler adapter of the set change
                                searchRecyclerAdapter.notifyDataSetChanged();
                                Log.d(TAG, "onComplete: filtered posts are " + postsList);
                                //stop loading when post list has items
                                coMeth.onResultStopLoading(postsList, progressDialog, null);
                            }


                        } else {

                            //task failed
                            if (!task.isSuccessful()) {

                                Log.d(TAG, "onComplete: getting users task failed " + task.getException());
                                showSnack(getString(R.string.failed_to_complete_text));

                            } else if (!task.getResult().exists()) {

                                //user does not exist
                                Log.d(TAG, "onComplete: user does not exist");

                            }

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

    private void hideKeyBoard() {

        Log.d(TAG, "hideKeyBoard: ");
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.d(TAG, "onClick: exception on hiding keyboard " + e.getMessage());
        }
    }

}
