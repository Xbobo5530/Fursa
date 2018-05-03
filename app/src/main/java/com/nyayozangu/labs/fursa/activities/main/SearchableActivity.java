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
        searchRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList);
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
        showProgress(getString(R.string.loading_text));

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


    private void filterPosts(final Posts post, String searchQuery) {

        String title = post.getTitle().toLowerCase();
        String desc = post.getDesc().toLowerCase();
        locString = "";
        if (post.getLocation() != null) {

            ArrayList locArray = post.getLocation();
            for (int i = 0; i < locArray.size(); i++) {

                locString = locString.concat(locArray.get(i).toString() + " ");

            }

        }

        // TODO: 4/19/18 refine search, the || might have errors
        if (title.contains(searchQuery) ||
                desc.contains(searchQuery) ||
                locString.contains(searchQuery)) {

            //get user_id for post
            String postUserId = post.getUser_id();
            coMeth.getDb()
                    .collection("Users")
                    .document(postUserId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                                progressDialog.dismiss();
                            } else {

                                //task failed
                                if (!task.isSuccessful()) {

                                    Log.d(TAG, "onComplete: getting users task failed " + task.getException());

                                } else if (!task.getResult().exists()) {

                                    //user does not exist
                                    Log.d(TAG, "onComplete: user does not exist");

                                }

                            }


                        }
                    });
        } else {

            progressDialog.dismiss();

        }


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


    /*public class MySearchRecentSuggestionsProvider extends SearchRecentSuggestionsProvider {

        String mIconUri = String.valueOf(R.drawable.ic_action_time); // a drawable ID as a String will also do!

        public Cursor query(Uri uri, String[] projection, String selection,
                            String[] selectionArgs, String sortOrder) {

            class Wrapper extends CursorWrapper {
                Wrapper(Cursor c) {
                    super(c);
                }

                public String getString(int columnIndex) {
                    if (columnIndex != -1
                            && columnIndex == getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1))
                        return mIconUri;

                    return super.getString(columnIndex);
                }
            }

            return new Wrapper(super.query(Uri uri, String[] projection, String selection,
                    String[] selectionArgs, String sortOrder);
        }

    }*/

    //show snack
    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.searchView),
                message, Snackbar.LENGTH_LONG)
                .show();
    }






}
