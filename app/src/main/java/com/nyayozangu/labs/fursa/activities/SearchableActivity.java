package com.nyayozangu.labs.fursa.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.models.Posts;
import com.nyayozangu.labs.fursa.fragments.AllSearchResultsFragment;
import com.nyayozangu.labs.fursa.fragments.ImageSearchResultsFragment;
import com.nyayozangu.labs.fursa.fragments.LocationSearchResultsFragment;
import com.nyayozangu.labs.fursa.fragments.PagesSearchResultsFragment;
import com.nyayozangu.labs.fursa.fragments.TagsSearchResultsFragment;
import com.nyayozangu.labs.fursa.providers.MySuggestionProvider;
import com.nyayozangu.labs.fursa.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Users;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * search for items
 * Created by Sean on 4/13/18.
 */

public class SearchableActivity extends AppCompatActivity {

    // TODO: 4/18/18 handle search when the search result has no content
    private static final String TAG = "Sean";
    //page titles
    private static final String ALL = "All";
    private static final String TAGS = "Tags";
    private static final String PAGES = "Pages";
    private static final String LOCATION = "Location";
    private static final String IMAGES = "Images";
    //common methods
    private CoMeth coMeth = new CoMeth();
    public String searchQuery;
    String searchableText = "";

    private int[] tabIcons = {
            R.drawable.ic_action_all_white,
            R.drawable.ic_action_tags_white,
            R.drawable.ic_action_pages_white,
            R.drawable.ic_action_location_white,
            R.drawable.ic_action_iamge_search_white
    };

    private PostsRecyclerAdapter searchRecyclerAdapter;
    private ProgressDialog progressDialog;
    public TabLayout tabsLayout;
    public Toolbar toolbar;
    private List<Posts> postList;
    private List<Users> userList;
    private ViewPager searchViewPager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_toolbar_menu, menu);
        //handle search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setSearchableInfo(Objects
                .requireNonNull(searchManager).getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        toolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.search_text));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //initiate post list
        postList = new ArrayList<>();
        userList = new ArrayList<>();
        handleTabs();
        handleIntent(getIntent());
    }

    private void handleTabs() {
        //set up view pager
        searchViewPager = findViewById(R.id.searchViewPager);
        setupViewPager(searchViewPager);
        tabsLayout = findViewById(R.id.saerchTabsLayout);
        tabsLayout.setupWithViewPager(searchViewPager);
        setupTabIcons();
        coMeth.stopLoading(progressDialog);
    }

    private void loadPosts(Query firstQuery, final String searchQuery) {
        Log.d(TAG, "loadPosts: ");
        try {
            Objects.requireNonNull(getSupportActionBar()).setTitle(searchQuery);
        } catch (NullPointerException titleNull) {
            Log.d(TAG, "loadPosts: failed to set title\n" + titleNull.getMessage());
        }
        firstQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                //create a for loop to check for document changes
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    //check if an item is added
                    String postId = doc.getDocument().getId();
                    Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        //filter posts
                        filterPosts(post, searchQuery, postId);
                    }
                }
            }
        });
    }

    private void filterPosts(final Posts post, final String searchQuery, final String postId) {
        Log.d(TAG, "filterPosts: ");

        String title = post.getTitle().toLowerCase();
        String desc = post.getDesc().toLowerCase();
        searchableText = searchableText.concat(title + " ");
        searchableText = searchableText.concat(desc + " ");

        //handle price
        if (post.getPrice() != null) {
            String price = post.getPrice().toLowerCase();
            searchableText = searchableText.concat(price + " ");

        }
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
                                (catsArray.get(i)).toString(), this).toLowerCase() + " ");

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
        String locString = "";
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
                                searchableText = searchableText.concat(bio + " ");

                            }
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

    private void getFilteredPosts(final Posts post) {

        Log.d(TAG, "getFilteredPosts: called");
        //get user_id for post
        final String postUserId = post.getUser_id();
        coMeth.getDb()
                .collection(CoMeth.USERS)
                .document(postUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Users user = Objects.requireNonNull(
                                    documentSnapshot.toObject(Users.class)).withId(postUserId);
                            //check if post is already added to the post list
                            if (!postList.contains(post)) {
                                //add new post to the local postsList
                                postList.add(post);
                                userList.add(user);
                                handleTabs();
//                                coMeth.stopLoading(progressDialog);
                                Log.d(TAG, "onComplete: filtered posts are " + postList);
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

    private void doMySearch(final String query) {

        Log.d(TAG, "doMySearch: \nquery is: " + query);
        //clear old search
        postList.clear();
        postList.clear();
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

    private void getPostsWithTags(final String searchTag) {
        Log.d(TAG, "getPostsWithTags: ");
        Objects.requireNonNull(getSupportActionBar()).setTitle("#" + searchTag);
        coMeth.getDb()
                .collection(CoMeth.TAGS + "/" + searchTag + "/" + CoMeth.POSTS)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    //get the post from db
                                    getPostWithTag(doc, searchTag);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get posts with tags\n" +
                                e.getMessage());
                    }
                });
    }

    /**
     * get the posts with tags from the posts list
     *
     * @param doc the document with the post id for the post with tag
     * @param searchTag the search tag
     * */
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
                            Posts post = Objects.requireNonNull(
                                    documentSnapshot.toObject(Posts.class)).withId(postId);
                            getFilteredPosts(post);
                        } else {
                            //post does not exist
                            Log.d(TAG, "onSuccess: post does not exist");
                            //update tags info
                            coMeth.getDb()
                                    .collection(CoMeth.TAGS + "/" +
                                            searchTag + "/" + CoMeth.POSTS)
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
                        Log.d(TAG, "onFailure: failed to get post " +
                                "with search tag from posts db\n" +
                                e.getMessage());
                    }
                });
    }

    private void setupTabIcons() {
        Log.d(TAG, "setupTabIcons: ");
        try {
            tabsLayout.getTabAt(0).setIcon(tabIcons[0]);
            tabsLayout.getTabAt(1).setIcon(tabIcons[1]);
            tabsLayout.getTabAt(2).setIcon(tabIcons[2]);
            tabsLayout.getTabAt(3).setIcon(tabIcons[3]);
            tabsLayout.getTabAt(4).setIcon(tabIcons[4]);
        }catch (NullPointerException setIconNull){
            Log.d(TAG, "setupTabIcons: failed to set tab icons\n" + setIconNull.getMessage());
        }

    }

    private void setupViewPager(ViewPager searchViewPager) {
        Log.d(TAG, "setupViewPager: ");
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new AllSearchResultsFragment(), ALL);
        adapter.addFragment(new TagsSearchResultsFragment(), TAGS);
        adapter.addFragment(new PagesSearchResultsFragment(), PAGES);
        adapter.addFragment(new LocationSearchResultsFragment(), LOCATION);
        adapter.addFragment(new ImageSearchResultsFragment(), IMAGES);
        searchViewPager.setAdapter(adapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        //show progress
//        showProgress(getResources().getString(R.string.searching_text));

        if (intent != null &&
                intent.getAction() != null &&
                intent.getData() != null) {
            //handle deep link
            Log.d(TAG, "handleIntent: handling deep link");
            String searchUrl = String.valueOf(intent.getData());
            int endOfUrlHead = getResources().getString(R.string.fursa_url_search_head).length();
            searchQuery = searchUrl.substring(endOfUrlHead);
            Log.d(TAG, "handleIntent: search query from deep link is " + searchQuery);
//            selectPage(0);
            doMySearch(searchQuery);
            getSearchQuery();
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            searchQuery = intent.getStringExtra(SearchManager.QUERY).toLowerCase();
            Log.d(TAG, "handleIntent: \nquery is" + searchQuery);
            // TODO: 4/16/18 continue suggested query search
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
                    this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(searchQuery, null);
//            selectPage(0);
            getSearchQuery();
            doMySearch(searchQuery);

        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {

            // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
            searchQuery = intent.getStringExtra(SearchManager.QUERY).toLowerCase();
//            selectPage(0);
            getSearchQuery();
            doMySearch(searchQuery);

        } else if (intent.getStringExtra(CoMeth.TAG_NAME ) != null) {
            final String searchTag = intent.getStringExtra(
                    getResources().getString(R.string.TAG_NAME));
            //set search query is search tag
            searchQuery = searchTag;
            getSearchQuery();
//            getSupportActionBar().setTitle("#" + searchTag);
            selectPage(0);
            getPostsWithTags(searchTag);
        }

        hideKeyBoard();
    }

    void selectPage(int pageIndex){
        try {
            tabsLayout.setScrollPosition(pageIndex, 0f, true);
            searchViewPager.setCurrentItem(pageIndex);
        }catch (NullPointerException tabIsNull){
            Log.d(TAG, "selectPage: tab is null\n" + tabIsNull.getMessage());
        }
    }

    public String getSearchQuery() {
        Log.d(TAG, "getSearchQuery: search query is " + searchQuery);
        return searchQuery;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.searchShareItemMenu:
                try {
                    String searchQuery = getSupportActionBar().getTitle().toString();
                    shareSearch(searchQuery);
                } catch (NullPointerException nullE) {
                    Log.d(TAG, "onOptionsItemSelected: " +
                            "failed to get search query from action bar\n" +
                            nullE.getMessage());
                    showSnack(getResources().getString(R.string.error_text) + ": " +
                            nullE.getMessage());
                }
                break;
            default:
                Log.d(TAG, "onOptionsItemSelected: on view search toolbar menu default");
        }
        return true;
    }


    private void shareSearch(final String searchQuery) {

        Log.d(TAG, "Sharing search");
        showProgress(getString(R.string.loading_text));
        //create cat url
        String searchUrl = getResources().getString(R.string.fursa_url_search_head) + searchQuery;
        Task<ShortDynamicLink> shortLinkTask =
                FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse(searchUrl))
                        .setDynamicLinkDomain(getString(R.string.dynamic_link_domain))
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                                .setMinimumVersion(coMeth.minVerCode)
                                .setFallbackUrl(Uri.parse(getString(R.string.playstore_url)))
                                .build())
                        .setSocialMetaTagParameters(
                                new DynamicLink.SocialMetaTagParameters.Builder()
                                        .setTitle(getString(R.string.app_name))
                                        .setDescription(searchQuery)
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
                                    String searchTitle = searchQuery;
                                    String fullShareMsg = getString(R.string.app_name) + "\n" +
                                            searchTitle + "\n" +
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

    public void showSnack(String message) {
        Snackbar.make(findViewById(R.id.searchLayout),
                message, Snackbar.LENGTH_LONG).show();
    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
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

//    public void updatePostList(Posts post, Users user) {
//        Log.d(TAG, "updatePostList: ");
//        postList.add(post);
//        userList.add(user);
//    }



    public List<Posts> getPostList(){
        return postList;
    }
    public List<Users> getUserList(){
        return userList;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


}



