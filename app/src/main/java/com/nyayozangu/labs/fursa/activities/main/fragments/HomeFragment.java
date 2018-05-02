package com.nyayozangu.labs.fursa.activities.main.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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

        //initiate the PostsRecyclerAdapter
        postsRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList);
        homeFeedView.setLayoutManager(new LinearLayoutManager(getActivity()));
        homeFeedView.setAdapter(postsRecyclerAdapter);

        //loading
        showProgress(getString(R.string.loading_text));

        //listen for scrolling on the homeFeedView
        homeFeedView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
                Boolean reachedBottom = !homeFeedView.canScrollVertically(1);
                if (reachedBottom) {

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
                postsList.clear();
                usersList.clear();
                homeFeedView.getRecycledViewPool().clear();
                loadPosts(firstQuery);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        swipeRefresh.setRefreshing(false);

                    }
                }, 1500);
            }
        });

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

                            //a new item/ post is added
                            String postId = doc.getDocument().getId();
                            final Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
                            //get user id
                            final String postUserId = post.getUser_id();
                            Log.d(TAG, "onEvent: user_id is " + postUserId);
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

                                        Users user = task.getResult().toObject(Users.class);
                                        //add new post to the local postsList
                                        if (isFirstPageFirstLoad) {

                                            usersList.add(user);
                                            postsList.add(post);

                                        } else {

                                            usersList.add(0, user);
                                            postsList.add(0, post);

                                        }
                                        //notify the recycler adapter of the set change
                                        postsRecyclerAdapter.notifyDataSetChanged();
                                        progressDialog.dismiss();

                                    } else {

                                        //no posts
                                        progressDialog.dismiss();

                                    }

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

    //for loading more posts
    public void loadMorePosts() {

        Query nextQuery = coMeth.getDb()
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(10);

        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                //check if there area more posts
                if (!queryDocumentSnapshots.isEmpty()) {

                    //get the last visible post
                    lastVisiblePost = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);

                    //create a for loop to check for document changes
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        //check if an item is added
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            //get the post id for likes feature
                            String postId = doc.getDocument().getId();
                            final Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
                            String postUserId = doc.getDocument().getString("user_id");

                            //get user_id for post
                            new CoMeth().getDb().collection("Users")
                                    .document(postUserId)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            //check if task is successful
                                            if (task.isSuccessful()) {

                                                Users user = task.getResult().toObject(Users.class);
                                                usersList.add(user);
                                                postsList.add(post);
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
