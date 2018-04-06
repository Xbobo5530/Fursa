package com.nyayozangu.labs.fursa;


import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
 * A simple {@link Fragment} subclass.
 */
public class SavedFragment extends Fragment {

    private static final String TAG = "Sean";
    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView savedPostsView;

    //retrieve posts
    private List<Posts> savedPostsList;

    //recycler adapter
    private PostsRecyclerAdapter savedPostsRecyclerAdapter;

    private DocumentSnapshot lastVisiblePost;

    private Boolean isFirstPageFirstLoad = true;

    private ConstraintLayout promptLoginLayout;
    private Button promptLoginButton;

    public SavedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        //initiate elements
        savedPostsView = view.findViewById(R.id.savedPostView);


        //initiate an arrayList to hold all the posts
        savedPostsList = new ArrayList<>();

        //initiate the PostsRecyclerAdapter
        savedPostsRecyclerAdapter = new PostsRecyclerAdapter(savedPostsList);


        //set a layout manager for homeFeedView (recycler view)
        savedPostsView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //set an adapter for the recycler view
        savedPostsView.setAdapter(savedPostsRecyclerAdapter);

        //initiate firebase auth
        mAuth = FirebaseAuth.getInstance();

        //initiate the firebase elements
        db = FirebaseFirestore.getInstance();


        //listen for scrolling on the homeFeedView
        savedPostsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !savedPostsView.canScrollVertically(1);

                if (reachedBottom) {

                    String desc = lastVisiblePost.getString("desc");
                    loadMorePosts();
                }

            }
        });


        Query firstQuery = db.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
        //get all posts from the database
        //use snapshotListener to get all the data real time
        firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
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
                        Log.d(TAG, "Error: " + exception.getMessage());
                    }

                }


                //create a for loop to check for document changes
                for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    //check if an item is added
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        //a new item/ post is added
                        //get the post id for likes feature
                        final String postId = doc.getDocument().getId();

                        //converting database data into objects
                        //get the newly saved post
                        //pass the postId to the post model class Posts.class
                        final Posts post = doc.getDocument().toObject(Posts.class).withId(postId);

                        //check if user is logged in to access the saved posts
                        if (isLoggedIn()) {
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "size is: " + queryDocumentSnapshots.getDocuments().size());

                            //query for saved posts
                            db.collection("Posts/" + postId + "/Saves").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                    //check if the current user saved current post
                                    if (documentSnapshot.exists()) {
                                        Log.d(TAG, "current user saved this post");
                                        //add new post to the local postsList
                                        if (isFirstPageFirstLoad) {
                                            //if the first page is loaded the add new post normally
                                            savedPostsList.add(post);
                                            Log.d(TAG, "isFirstPageFirstLoad: post added to savedPostList" + savedPostsList.toString());
                                            //notify the recycler adapter of the set change
                                            savedPostsRecyclerAdapter.notifyDataSetChanged();
                                            Log.d(TAG, "Nofitied Dataset changed");
                                        } else {
                                            //add the post at position 0 of the postsList
                                            savedPostsList.add(0, post);
                                            Log.d(TAG, "!isFirstPageFirstLoad: post added to savedPostList" + savedPostsList.toString());
                                            //notify the recycler adapter of the set change
                                            savedPostsRecyclerAdapter.notifyDataSetChanged();
                                            Log.d(TAG, "Notified DataSet changed");
                                        }
                                    } else {
                                        Log.d(TAG, "current user did not saved this post");

                                    }
                                }
                            });
                        } else {
                            //user is not logged in
                            //notify user
                            //user is not logged in, hence cant view saved posts
                            Log.d(TAG, "user is not logged in");
                            /*Snackbar.make(container.findViewById(R.id.saved_fragment_container),
                                    "Log in to view saved items", Snackbar.LENGTH_SHORT)
                                    .setAction("Login", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            goToLogin();
                                        }
                                    }).show();*/
                        }
                    }
                }

                //the first page has already loaded
                isFirstPageFirstLoad = false;

            }
        });

        return view;
    }

    private void goToLogin() {
        //go to login page
        startActivity(new Intent(getActivity(), LoginActivity.class));
    }


    //for loading more posts
    public void loadMorePosts() {

        Query nextQuery = db.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(10);


        //get all posts from the database
        //use snapshotListener to get all the data real time
        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
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

                                //add new post to the local postsList
                                savedPostsList.add(post);
                                //notify the recycler adapter of the set change
                                savedPostsRecyclerAdapter.notifyDataSetChanged();
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

    private boolean isLoggedIn() {
        //determine if user is logged in
        return mAuth.getCurrentUser() != null;
    }
}
