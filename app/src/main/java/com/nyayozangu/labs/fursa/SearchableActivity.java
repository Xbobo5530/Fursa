package com.nyayozangu.labs.fursa;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * search for items
 * Created by Sean on 4/13/18.
 */

public class SearchableActivity extends AppCompatActivity {


    // TODO: 4/18/18 handle saerch when the search result has no content
    private static final String TAG = "Sean";

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String searchQuery;

    private RecyclerView homeFeedView;

    //retrieve posts
    private List<Posts> postsList;

    //recycler adapter
    private PostsRecyclerAdapter searchRecyclerAdapter;

    private DocumentSnapshot lastVisiblePost;

    private Boolean isFirstPageFirstLoad = true;

    private String locAddress = "+#%+";
    private String locName = "+#%+";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);


        Toolbar toolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.search_text));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SearchableActivity.this, MainActivity.class));
                finish();

            }
        });


        //initiate items
        homeFeedView = findViewById(R.id.searchRecyclerView);

        //initiate an arrayList to hold all the posts
        postsList = new ArrayList<>();

        //initiate the PostsRecyclerAdapter
        searchRecyclerAdapter = new PostsRecyclerAdapter(postsList);

        //set a layout manager for homeFeedView (recycler view)
        homeFeedView.setLayoutManager(new LinearLayoutManager(this));

        //set an adapter for the recycler view
        homeFeedView.setAdapter(searchRecyclerAdapter);

        //initiate firebase auth
        mAuth = FirebaseAuth.getInstance();

        //initiate the firebase elements
        db = FirebaseFirestore.getInstance();


        // TODO: 4/16/18 set the toolbar for search activity


        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        Log.d(TAG, "handleIntent: at search intent");

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchQuery = intent.getStringExtra(SearchManager.QUERY).toLowerCase();

            // TODO: 4/20/18 setting search query for title doesnt work
            getSupportActionBar().setTitle(intent.getStringExtra(SearchManager.QUERY));

            // TODO: 4/16/18 continue suggested query search
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(searchQuery, null);

            Log.d(TAG, "handleIntent: query is " + searchQuery);

            doMySearch(searchQuery);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {

            // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
            searchQuery = intent.getStringExtra(SearchManager.QUERY).toLowerCase();

            Log.d(TAG, "handleIntent: query is " + searchQuery);

            doMySearch(searchQuery);

        }
    }

    private void doMySearch(final String query) {

        Log.d(TAG, "doMySearch: ");

        //listen for scrolling on the homeFeedView
        homeFeedView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !homeFeedView.canScrollVertically(1);

                if (reachedBottom) {

                    loadMorePosts(query);
                }
            }
        });


        final Query firstQuery = db.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
        //get all posts from the database
        //use snapshotListener to get all the data real time
        loadPosts(firstQuery, query);

    }

    private void loadPosts(Query firstQuery, final String searchQuery) {
        firstQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                //check if the data is loaded for the first time
                /*
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
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    //check if an item is added
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        //a new item/ post is added

                        //get the post id for likes feature
                        String postId = doc.getDocument().getId();

                        //converting database data into objects
                        //get the newly added post
                        //pass the postId to the post model class Posts.class
                        Posts post = doc.getDocument().toObject(Posts.class).withId(postId);


                        //filter posts
                        filterPosts(post, searchQuery);

                    }
                }

                //the first page has already loaded
                isFirstPageFirstLoad = false;

            }
        });
    }


    private void filterPosts(Posts post, String searchQuery) {
        String title = post.getTitle().toLowerCase();
        String desc = post.getDesc().toLowerCase();

        if (post.getLocation_address() != null) {
            locAddress = post.getLocation_address().toLowerCase();
        }
        if (post.getContact_name() != null) {
            locName = post.getContact_name().toLowerCase();
        }
        // TODO: 4/19/18 find better solution to handle locations null

        // TODO: 4/19/18 test case sensitivity

        //check if query is in title / desc
        // TODO: 4/19/18 refine search, the || might have errors
        if (title.contains(searchQuery) ||
                desc.contains(searchQuery)
                || locAddress.contains(searchQuery) ||
                locName.contains(searchQuery)) {

            //add post to search results
            if (isFirstPageFirstLoad) {
                //if the first page is loaded the add new post normally
                postsList.add(post);
            } else {
                //add the post at position 0 of the postsList
                postsList.add(0, post);

            }
            //notify the recycler adapter of the set change
            searchRecyclerAdapter.notifyDataSetChanged();

        }

    }

    //for loading more posts
    public void loadMorePosts(final String searchQuery) {

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

                                //converting database data into objects
                                //get the newly added post
                                //pass the postId to the post model class Posts.class
                                Posts post = doc.getDocument().toObject(Posts.class).withId(postId);

                                //filter posts
                                filterPosts(post, searchQuery);

                            }
                        }
                        Log.d(TAG, "onEvent: \nadded posts to postlist, postlist is: " + postsList);

                    }
                } catch (NullPointerException nullException) {
                    //the Query is null
                    Log.e(TAG, "error: " + nullException.getMessage());
                }
            }
        });


    }

}
