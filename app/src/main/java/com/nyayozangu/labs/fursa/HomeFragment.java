package com.nyayozangu.labs.fursa;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView homeFeedView;

    //retrieve posts
    private List<Posts> postsList;

    //recycler adapter
    private PostsRecyclerAdapter postsRecyclerAdapter;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //initiate items
        homeFeedView = view.findViewById(R.id.homeFeedView);

        //initiate an arraylist to hold all the posts
        postsList = new ArrayList<>();

        //initiate the PostsRecyclerAdapter
        postsRecyclerAdapter = new PostsRecyclerAdapter(postsList);

        //set a layout manager for homeFeedView (recycler view)
        homeFeedView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //set an adapter for the recycler view
        homeFeedView.setAdapter(postsRecyclerAdapter);

        //initiate the firebase elements
        db = FirebaseFirestore.getInstance();
        //get all posts from the database
        //use snapshotListener to get all the data real time
        db.collection("Posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                //create a for loop to check for document changes
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    //check if an item is added
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        //a new item/ post is added

                        //converting database data into objects
                        //get the newly added post
                        Posts post = doc.getDocument().toObject(Posts.class);
                        //add new post to the local postsList
                        postsList.add(post);
                        //notify the recycler adapter of the set change
                        postsRecyclerAdapter.notifyDataSetChanged();
                    }
                }

            }
        });


        // Inflate the layout for this fragment
        return view;
    }

}
