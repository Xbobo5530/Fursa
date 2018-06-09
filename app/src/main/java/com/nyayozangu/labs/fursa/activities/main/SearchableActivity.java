package com.nyayozangu.labs.fursa.activities.main;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import javax.annotation.Nullable;


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
    String searchableText = "";

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

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFeed.smoothScrollToPosition(0);
            }
        });

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
                getIntent().getStringExtra(getResources().getString(R.string.TAG_NAME)) != null) {
            final String searchTag = getIntent().getStringExtra(
                    getResources().getString(R.string.TAG_NAME));

            //get posts with tags
            getPostsWithTags(searchTag);
        }
        hideKeyBoard();
    }

    private void getPostsWithTags(final String searchTag) {
        Log.d(TAG, "getPostsWithTags: ");
        //update the toolbar title
        getSupportActionBar().setTitle("#" + searchTag);
        coMeth.getDb()
                .collection("Tags/" + searchTag + "/Posts/")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    //get the post from db
                                    getPostWithTag(doc, searchTag);
                                }
                            }
                        }
                    }
                });
    }

    private void getPostWithTag(DocumentChange doc, final String searchTag) {
        final String postId = doc.getDocument().getId();
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //post exists
                            Log.d(TAG, "onSuccess: post exists");
                            //create a post object
                            Posts post = documentSnapshot.toObject(Posts.class).withId(postId);
                            getFilteredPosts(post);
                        } else {
                            //post does not exist
                            Log.d(TAG, "onSuccess: post does not exist");
                            //update tags info
                            coMeth.getDb()
                                    .collection("Tags/" + searchTag + "/Posts")
                                    .document(postId)
                                    .delete();
                            Log.d(TAG, "onSuccess: deleting post entry from search tag " +
                                    "because post does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get post with search tag from posts db\n" +
                                e.getMessage());
                    }
                });
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
        if (String.valueOf(query.charAt(0)).equals("#")) {
            Log.d(TAG, "doMySearch: searching #");

            getPostsWithTags(query.substring(1));
        } else {
            Log.d(TAG, "doMySearch: searching query");
            //get all posts from the database
            loadPosts(firstQuery, query);
        }
    }

    private void loadPosts(Query firstQuery, final String searchQuery) {
        getSupportActionBar().setTitle(searchQuery);
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
                        filterPosts(post, searchQuery, postId);
                    } else if (doc.getType() == DocumentChange.Type.REMOVED) {
                        // TODO: 5/25/18 test if documet is removed
                        if (postsList.contains(post)) {
                            postsList.remove(post);
                        }
                    }
                }
                //the first page has already loaded
                isFirstPageFirstLoad = false;

            }
        });
    }

    private void filterPosts(final Posts post, final String searchQuery, final String postId) {

        String title = post.getTitle().toLowerCase();
        String desc = post.getDesc().toLowerCase();
        searchableText = searchableText.concat(title + " ");
        searchableText = searchableText.concat(desc + " ");

        //handle price
        if (post.getPrice() != null) {
            String price = post.getPrice().toLowerCase();
            searchableText = searchableText.concat(price + " ");

        }
        //image labels
        String imageLabels = "";
        if (post.getImage_labels() != null) {
            imageLabels = post.getImage_labels().toLowerCase().trim();
            searchableText = searchableText.concat(imageLabels + " ");

        }
        //image text
        String imageText = "";
        if (post.getImage_text() != null) {
            imageText = post.getImage_text().toLowerCase().trim();
            searchableText = searchableText.concat(imageText + " ");

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
        searchableText = searchableText.concat(catString + " ");

        //handle tags
        String tagsString = "";
        if (post.getTags() != null) {
            ArrayList tags = post.getTags();
            for (int i = 0; i < tags.size(); i++) {
                tagsString = tagsString.concat(tags.get(i) + " ");
            }
            Log.d(TAG, "filterPosts: \ntags string is: " + tagsString + "\ntags are: " + tags);
        }
        searchableText = searchableText.concat(tagsString + " ");

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
        searchableText = searchableText.concat(contactString + " ");

        //handle location search
        locString = "";
        if (post.getLocation() != null) {

            ArrayList locArray = post.getLocation();
            for (int i = 0; i < locArray.size(); i++) {
                locString = locString.concat(
                        locArray.get(i).toString().toLowerCase() + " ");
            }
        }
        searchableText = searchableText.concat(locString + " ");

        //handle postUser
        String postUserId = post.getUser_id();
        coMeth.getDb()
                .collection("Users")
                .document(postUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //user exists
                            Users user = documentSnapshot.toObject(Users.class);
                            String username = user.getName().toLowerCase();
                            if (username.contains(searchQuery)) getFilteredPosts(post);
                            if (user.getBio() != null) {
                                String bio = user.getBio().toLowerCase();
//                                if (bio.contains(searchQuery)) getFilteredPosts(post);
                                searchableText = searchableText.concat(bio + " ");

                            }
                        } else {
                            //user does not exist
                            //delete post
                            deleteUserlessPost(postId);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get user details\n" + e.getMessage());
                    }
                });
        //handle event date
        String eventDateString = "";
        if (post.getEvent_date() != null) {

            Date eventDate = post.getEvent_date();
            long eventDateMils = eventDate.getTime();
            eventDateString = DateFormat.format(
                    "EEE, MMM d, 20yy", new Date(eventDateMils)).toString().toLowerCase();
            searchableText = searchableText.concat(eventDateString + " ");
        }

        if (searchableText.contains(searchQuery)) {
            getFilteredPosts(post);
        }
        //clear searchable text after searching through 1 post
        searchableText = "";
    }

    private void deleteUserlessPost(String postId) {
        Log.d(TAG, "onSuccess: user does not exist");
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: deleted post with no user");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to delete post with no user\n" +
                                e.getMessage());
                    }
                });
    }

    private void getFilteredPosts(final Posts post) {

        Log.d(TAG, "getFilteredPosts: called");
        //get user_id for post
        final String postUserId = post.getUser_id();
        coMeth.getDb()
                .collection("Users")
                .document(postUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String psotUserId = documentSnapshot.getId();
                            Users user = documentSnapshot.toObject(Users.class).withId(postUserId);
                            //check if post is already added to the post list
                            if (!postsList.contains(post)) {
                                //add new post to the local postsList
                                postsList.add(post);
                                usersList.add(user);
                                //notify the recycler adapter of the set change
                                searchRecyclerAdapter.notifyDataSetChanged();
                                Log.d(TAG, "onComplete: filtered posts are " + postsList);
                                //stop loading when post list has items
                                coMeth.stopLoading(progressDialog);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get search post users\n" +
                                e.getMessage());
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

//    //show snack
//    private void showSnack(String message) {
//        Snackbar.make(findViewById(R.id.searchView),
//                message, Snackbar.LENGTH_LONG)
//                .show();
//    }

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
