package com.nyayozangu.labs.fursa.activities.main.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();

    private RecyclerView homeFeedView;
    private SwipeRefreshLayout swipeRefresh;

    //retrieve posts
    private List<Posts> postsList;
    private List<Users> usersList;

    //recycler adapter
    private PostsRecyclerAdapter postsRecyclerAdapter;
    private DocumentSnapshot lastVisiblePost;
    private Boolean isFirstPageFirstLoad = true;
    private ProgressDialog progressDialog;
    private ArrayList<String> tags;

    public HomeFragment() {
    } // Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //initiate items
        homeFeedView = view.findViewById(R.id.homeFeedView);
        swipeRefresh = view.findViewById(R.id.homeSwipeRefresh);

        //initiate an arrayList to hold all the posts
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();
        tags = new ArrayList<>();

        //initiate the PostsRecyclerAdapter
        String className = "HomeFragment";
        postsRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className);
        coMeth.handlePostsView(getContext(), getActivity(), homeFeedView);
        homeFeedView.setAdapter(postsRecyclerAdapter);

        // TODO: 5/21/18 load old data then show new data notification
        // TODO: 5/21/18 check if user is firs time loading
        // TODO: 5/21/18 check if there is cached data


        //loading
        showProgress(getString(R.string.loading_text));
//        populateTags();

        //listen for scrolling on the homeFeedView
        homeFeedView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
                Boolean reachedBottom = !homeFeedView.canScrollVertically(1);
                if (reachedBottom) {
                    //clear post list
                    Log.d(TAG, "at addOnScrollListener\n reached bottom");
                    loadMorePosts();
                }
            }
        });


        final Query firstQuery = coMeth.getDb()
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);
        //get all posts from the database
        loadPosts(firstQuery);

        //handle refresh
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //get new posts
                homeFeedView.getRecycledViewPool().clear();
                postsList.clear();
                usersList.clear();
                loadPosts(firstQuery);
            }
        });


        try {
            ((MainActivity) getActivity()).mainBottomNav.setOnNavigationItemReselectedListener(
                    new BottomNavigationView.OnNavigationItemReselectedListener() {
                        @Override
                        public void onNavigationItemReselected(@NonNull MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.bottomNavHomeItem:
                                    homeFeedView.smoothScrollToPosition(0);
                                    break;
                                default:
                                    Log.d(TAG, "onNavigationItemReselected: at default");
                            }
                        }
                    });
        } catch (NullPointerException nullE) {
            Log.d(TAG, "onCreateView: null on home reselect\n" + nullE.getMessage());
        }

        // Inflate the layout for this fragment
        return view;

    }

    private void loadPosts(Query firstQuery) {
        firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
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

                    //create a for loop to check for document changes
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        //check if an item is added
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            final String postId = doc.getDocument().getId();
                            final Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
                            final String postUserId = post.getUser_id();
                            Log.d(TAG, "onEvent: user_id is " + postUserId);

                            // TODO: 5/21/18 clean tags code
//                            cleanTags(doc, postId);

                            // TODO: 6/7/18 organise tags
//                            populateTags(postId, post);
                            //get user_id for post
                            coMeth.getDb()
                                    .collection("Users")
                                    .document(postUserId)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    //check if task is successful
                                    if (task.isSuccessful()) {

                                        String userId = task.getResult().getId();
                                        Users user = task.getResult().toObject(Users.class).withId(userId);
                                        //add new post to the local postsList
                                        if (isFirstPageFirstLoad) {
                                            if (!postsList.contains(post)) {
                                                usersList.add(0, user);
                                                postsList.add(0, post);
                                            }
                                        } else {
                                            if (!postsList.contains(post)) {
                                                usersList.add(user);
                                                postsList.add(post);
                                            }
                                        }
                                        postsRecyclerAdapter.notifyDataSetChanged();
                                    } else {

                                        //no posts
                                        Log.d(TAG, "onComplete: no posts");

                                    }
                                    coMeth.stopLoading(progressDialog, swipeRefresh);
                                }
                            });
                        }
                    }
                    //the first page has already loaded
                    isFirstPageFirstLoad = false;
                }
            }
        });
    }

//    private void populateTags() {
//        Log.d(TAG, "populateTags: ");
//        coMeth.getDb()
//                .collection("Posts")
//                .limit(3)
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        Log.d(TAG, "onSuccess: got all posts");
//                        for (DocumentSnapshot document: queryDocumentSnapshots){
//                            Log.d(TAG, "onSuccess: looping through posts");
//                            //convert to posts object
//                            final String postId = document.getId();
//                            Posts post = document.toObject(Posts.class);
//                            //check if post has tags
//                            if (post.getTags() != null){
//                                //put tags in array
//                                ArrayList<String> tagsList = post.getTags();
//                                for (final String tag : tagsList){
//                                    //check if tag in empty
//                                    if (!tag.isEmpty()) {
//                                        //create a tagMap
//                                        Log.d(TAG, "onSuccess: tag is " + tag);
//                                        Map<String, Object> tagMap = new HashMap<>();
//                                        tagMap.put("title", tag);
//                                        coMeth.getDb()
//                                                .collection("Tags")
//                                                .document(tag)
//                                                .update(tagMap)
//                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void aVoid) {
//                                                        //add post id to tag collection
//                                                        Log.d(TAG, "onSuccess: updating tags");
//                                                        addPostIdToTagColl(tag, postId);
//
//                                                    }
//                                                })
//                                                .addOnFailureListener(new OnFailureListener() {
//                                                    @Override
//                                                    public void onFailure(@NonNull Exception e) {
//                                                        Log.d(TAG, "onFailure: failed to update tag\n" + e.getMessage());
//                                                        Map<String, Object> tagMap = new HashMap<>();
//                                                        tagMap.put("title", tag);
//                                                        coMeth.getDb()
//                                                                .collection("Tags")
//                                                                .document(tag)
//                                                                .set(tagMap)
//                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                    @Override
//                                                                    public void onSuccess(Void aVoid) {
//                                                                        //add post id to tag collection
//                                                                        Log.d(TAG, "onSuccess: updating tag");
//                                                                        addPostIdToTagColl(tag, postId);
//
//                                                                    }
//                                                                });
//                                                    }
//                                                });
//                                    }
//                                }
//                            }
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "onFailure: failed to retrieve posts\n" + e.getMessage());
//                    }
//                });
//    }
//
//    private void addPostIdToTagColl(String tag, String postId) {
//        Log.d(TAG, "addPostIdToTagColl: ");
//        //create tagPostMap
//        Map<String, Object> tagPostMap = new HashMap<>();
//        tagPostMap.put("timestamp", FieldValue.serverTimestamp());
//        coMeth.getDb()
//                .collection("Tags/" + tag + "/Posts")
//                .document(postId)
//                .set(tagPostMap)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "onSuccess: post id added to tags list");
//                        coMeth.stopLoading(progressDialog);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "onFailure: failed to add post id to tag collection\n" +
//                                e.getMessage());
//                        coMeth.stopLoading(progressDialog);
//                    }
//                });
//    }

//    private void cleanTags(DocumentChange doc, String postId) {
//        Log.d(TAG, "cleanTags: ");
//        if (doc.getDocument().get("tags") != null) {
//
//            ArrayList oldTags = (ArrayList) doc.getDocument().get("tags");
//            if (oldTags.size() > 10) {
//
//                Map<String, Object> tagsMap = new HashMap<>();
//                coMeth.getDb()
//                        .collection("Posts")
//                        .document(postId)
//                        .update(tagsMap)
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                Log.d(TAG, "onSuccess: tags cleanup successful");
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "onFailure: failed to clean tags");
//                    }
//                });
//            }
//        }
//    }

    //for loading more posts
    public void loadMorePosts() {

        Log.d(TAG, "loadMorePosts: ");
        Query nextQuery = coMeth.getDb()
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(10);

        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                //check if there area more posts
                if (!queryDocumentSnapshots.isEmpty()) {
                    lastVisiblePost = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String postId = doc.getDocument().getId();
                            final Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
                            String postUserId = post.getUser_id();
                            coMeth.getDb()
                                    .collection("Users")
                                    .document(postUserId)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            //check if task is successful
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "onComplete: adding posts");
                                                Users user = task.getResult().toObject(Users.class);
                                                if (!postsList.contains(post)) {
                                                    usersList.add(user);
                                                    postsList.add(post);
                                                }
                                                postsRecyclerAdapter.notifyDataSetChanged();
                                            }

                                        }
                                    });
                        }
                    }

                }

            }
        });


    }

    //show progress
    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(message);
        progressDialog.show();
    }

}
