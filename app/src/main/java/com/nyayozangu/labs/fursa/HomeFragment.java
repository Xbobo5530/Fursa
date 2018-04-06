package com.nyayozangu.labs.fursa;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class HomeFragment extends Fragment {

    // TODO: 4/6/18 fix the FAB on home fragment hides the last save

    private static final String TAG = "Sean";
    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView homeFeedView;

    //retrieve posts
    private List<Posts> postsList;

    //recycler adapter
    private PostsRecyclerAdapter postsRecyclerAdapter;

    private DocumentSnapshot lastVisiblePost;

    private Boolean isFirstPageFirstLoad = true;



    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //initiate items
        homeFeedView = view.findViewById(R.id.homeFeedView);

        //initiate an arrayList to hold all the posts
        postsList = new ArrayList<>();

        //initiate the PostsRecyclerAdapter
        postsRecyclerAdapter = new PostsRecyclerAdapter(postsList);


        //set a layout manager for homeFeedView (recycler view)
        homeFeedView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //set an adapter for the recycler view
        homeFeedView.setAdapter(postsRecyclerAdapter);

        //initiate firebase auth
        mAuth = FirebaseAuth.getInstance();

        //requires permissions
        //check permissions

        //initiate the firebase elements
        db = FirebaseFirestore.getInstance();

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

                        //add new post to the local postsList
                        if (isFirstPageFirstLoad) {
                            //if the first page is loaded the add new post normally
                            postsList.add(post);
                        } else {
                            //add the post at position 0 of the postsList
                            postsList.add(0, post);

                        }
                        //notify the recycler adapter of the set change
                        postsRecyclerAdapter.notifyDataSetChanged();
                    }
                }

                //the first page has already loaded
                isFirstPageFirstLoad = false;

            }
        });
        // Inflate the layout for this fragment
        return view;
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
                                postsList.add(post);
                                //notify the recycler adapter of the set change
                                postsRecyclerAdapter.notifyDataSetChanged();
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

}
