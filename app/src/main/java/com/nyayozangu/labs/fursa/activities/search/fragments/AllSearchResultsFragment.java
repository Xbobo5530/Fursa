package com.nyayozangu.labs.fursa.activities.search.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.search.SearchableActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class AllSearchResultsFragment extends Fragment {


    private static final String TAG = "Sean";
    private RecyclerView allResultsView;
    private ProgressDialog progressDialog;
    private CoMeth coMeth = new CoMeth();

    //retrieve posts
    private List<Posts> postsList;
    private List<Users> usersList;

    //recycler adapter
    private PostsRecyclerAdapter searchRecyclerAdapter;
    private String searchableText = "";
    private String locString = "";

    //search query
    private String searchQuery;
    private RecyclerView searchFeed;

    public AllSearchResultsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_search_resuslts, container, false);

        //initiate items
        searchFeed = view.findViewById(R.id.allSearchRecyclerView);

        postsList = new ArrayList<>();
        usersList = new ArrayList<>();

        //initiate the PostsRecyclerAdapter
        String className = "SearchableActivity";
        searchRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className);
        coMeth.handlePostsView(
                getContext(), getActivity(), searchFeed);
        //set an adapter for the recycler view
        searchFeed.setAdapter(searchRecyclerAdapter);

        if (getActivity() != null) {
            searchQuery = ((SearchableActivity) getActivity()).getSearchQuery();
            Log.d(TAG, "onCreateView: search query is " + searchQuery);
            if (searchQuery != null && !searchQuery.isEmpty())
                doMySearch(searchQuery);
        } else {
            //alert user when failed to get search query from activity

            ((SearchableActivity) Objects.requireNonNull(getActivity()))
                    .showSnack(getResources().getString(R.string.something_went_wrong_text));
        }

        //add scroll to top on tool bar click
        ((SearchableActivity) getActivity()).toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFeed.smoothScrollToPosition(0);
            }
        });

        return view;
    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(message);
        progressDialog.show();
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
                                //update postlist on seach activity
                                ((SearchableActivity) getActivity()).updatePostList(post, user);

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

    private void loadPosts(Query firstQuery, final String searchQuery) {
        Log.d(TAG, "loadPosts: ");
        try {
            ((SearchableActivity) getActivity()).getSupportActionBar().setTitle(searchQuery);
        } catch (NullPointerException titleNull) {
            Log.d(TAG, "loadPosts: failed to set title\n" + titleNull.getMessage());
        }
        firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
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
                    } else if (doc.getType() == DocumentChange.Type.REMOVED) {
                        // TODO: 5/25/18 test if document is removed
                        if (postsList.contains(post)) {
                            postsList.remove(post);
                        }
                    }
                }
                //the first page has already loaded
//                isFirstPageFirstLoad = false;

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

    private void doMySearch(final String query) {

        Log.d(TAG, "doMySearch: \nquery is: " + query);
        //show progress
        showProgress(getResources().getString(R.string.loading_text));
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

    private void getPostsWithTags(final String searchTag) {
        Log.d(TAG, "getPostsWithTags: ");
        //update the toolbar title
        try {
            ((SearchableActivity) getActivity()).getSupportActionBar().setTitle("#" + searchTag);
        } catch (NullPointerException titleNull) {
            Log.d(TAG, "getPostsWithTags: failed to update title\n" + titleNull.getMessage());
        }
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

}
