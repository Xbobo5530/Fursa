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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Objects;

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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putString("postList", postsList);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) Objects.requireNonNull(getActivity())).hideProgress();
    }

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
        postsRecyclerAdapter = new PostsRecyclerAdapter(postsList, usersList, className, Glide.with(this));
        coMeth.handlePostsView(getContext(), getActivity(), homeFeedView);
        postsRecyclerAdapter.setHasStableIds(true);
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
//                    //show progress
//                    ((MainActivity) Objects.requireNonNull(getActivity()))
//                            .progressBar.setVisibility(View.VISIBLE);
                    loadMorePosts();
//                    new LoadPostsTask().execute();
                } else {
                    ((MainActivity) Objects.requireNonNull(getActivity())).hideProgress();
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

                    ArrayList<String> postIds = new ArrayList<>();
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
//                            populateTags(post, postId);

                            // TODO: 6/10/18 populate cats
//                            populateCats(post, postId);

                            //get user_id for post
                            coMeth.getDb()
                                    .collection(CoMeth.USERS)
                                    .document(postUserId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists()) {
                                                //user exists
                                                String userId = documentSnapshot.getId();
                                                try {
                                                    Users user = Objects.requireNonNull(
                                                            documentSnapshot.toObject(Users.class)).withId(userId);
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
                                                    coMeth.stopLoading(progressDialog, swipeRefresh);
                                                } catch (NullPointerException userNull) {
                                                    Log.d(TAG, "onSuccess: user id is null\n" +
                                                            userNull.getMessage());
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //failed to get user details for post
                                            Log.d(TAG, "onFailure: failed to get user details for post\n" +
                                                    e.getMessage());
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

//    private void populateCats(Posts post, final String postId) {
//        Log.d(TAG, "populateCats: ");
//        if (post.getCategories() != null){
//            //put cats in array
//            ArrayList<String> catsList = post.getCategories();
//            for (final String cat : catsList){
//                if (!cat.isEmpty()){
//                    //create map
//                    final Map<String, Object> catMap = new HashMap<>();
//                    Log.d(TAG, "populateCats: cat is " + cat);
//                    catMap.put("title", cat);
//                    coMeth.getDb()
//                            .collection("Categories")
//                            .document(cat)
//                            .update(catMap)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    Log.d(TAG, "onSuccess: updated cat");
//                                    //add postId to cat
//                                    addPostIdToCat(cat, postId);
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.d(TAG, "onFailure: failed to update cats\n" + e.getMessage());
//                                    //set cat
//                                    coMeth.getDb()
//                                            .collection("Categories")
//                                            .document(cat)
//                                            .set(catMap)
//                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    Log.d(TAG, "onSuccess: set cat");
//                                                    addPostIdToCat(cat, postId);
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Log.d(TAG, "onFailure: failed to set cat\n" + e.getMessage());
//                                                }
//                                            });
//                                }
//                            });
//                }
//            }
//        }
//    }
//
//    private void addPostIdToCat(String cat, String postId) {
//        Log.d(TAG, "addPostIdToCat: ");
//        Map<String, Object> catPostMap = new HashMap<>();
//        catPostMap.put("timestamp", FieldValue.serverTimestamp());
//        coMeth.getDb()
//                .collection("Categories/" + cat + "/Posts")
//                .document(postId)
//                .set(catPostMap)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "onSuccess: post id added to cat");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "onFailure: failed to add post to cat\n" + e.getMessage());
//                    }
//                });
//    }

//    private void populateTags(final String postId, Posts post) {
//        Log.d(TAG, "populateTags: ");
//        if (post.getTags() != null){
//            //put tags in array
//            ArrayList<String> tagsList = post.getTags();
//            for (final String tag : tagsList){
//                //check if tag in empty
//                if (!tag.isEmpty()) {
//                    //create a tagMap
//                    Log.d(TAG, "onSuccess: tag is " + tag);
//                    Map<String, Object> tagMap = new HashMap<>();
//                    tagMap.put("title", tag);
//                    coMeth.getDb()
//                            .collection("Tags")
//                            .document(tag)
//                            .update(tagMap)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    //add post id to tag collection
//                                    Log.d(TAG, "onSuccess: updating tags");
//                                    addPostIdToTagColl(tag, postId);
//
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.d(TAG, "onFailure: failed to update tag\n" + e.getMessage());
//                                    Map<String, Object> tagMap = new HashMap<>();
//                                    tagMap.put("title", tag);
//                                    coMeth.getDb()
//                                            .collection("Tags")
//                                            .document(tag)
//                                            .set(tagMap)
//                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    //add post id to tag collection
//                                                    Log.d(TAG, "onSuccess: updating tag");
//                                                    addPostIdToTagColl(tag, postId);
//
//                                                }
//                                            });
//                                }
//                            });
//                }
//            }
//        }
//    }

//    private void populateTags(Posts post, final String postId) {
//        Log.d(TAG, "populateTags: ");
//
//        if (post.getTags() != null){
//            //put tags in array
//            ArrayList<String> tagsList = post.getTags();
//            for (final String tag : tagsList){
//                //check if tag in empty
//                if (!tag.isEmpty()) {
//                    //create a tagMap
//                    Log.d(TAG, "onSuccess: tag is " + tag);
//                    Map<String, Object> tagMap = new HashMap<>();
//                    tagMap.put("title", tag);
//                    coMeth.getDb()
//                            .collection("Tags")
//                            .document(tag)
//                            .update(tagMap)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    //add post id to tag collection
//                                    Log.d(TAG, "onSuccess: updating tags");
//                                    addPostIdToTagColl(tag, postId);
//
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.d(TAG, "onFailure: failed to update tag\n" + e.getMessage());
//                                    Map<String, Object> tagMap = new HashMap<>();
//                                    tagMap.put("title", tag);
//                                    coMeth.getDb()
//                                            .collection("Tags")
//                                            .document(tag)
//                                            .set(tagMap)
//                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    //add post id to tag collection
//                                                    Log.d(TAG, "onSuccess: updating tag");
//                                                    addPostIdToTagColl(tag, postId);
//
//                                                }
//                                            });
//                                }
//                            });
//                }
//            }
//        }
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
//
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

//    private void addPostIdToTagColl(final String tag, String postId) {
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
//                        //get post count on tag
//                        coMeth.getDb().collection(CoMeth.TAGS + "/" + tag + "/" +
//                                CoMeth.POSTS)
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                        if (!queryDocumentSnapshots.isEmpty()){
//                                            final int tagPostsCount = queryDocumentSnapshots.size();
//                                            Map<String, Object> tagsPostCountMap = new HashMap<>();
//                                            tagsPostCountMap.put("post_count", tagPostsCount);
//                                            coMeth.getDb().collection(CoMeth.TAGS)
//                                                    .document(tag)
//                                                    .update(tagsPostCountMap)
//                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//                                                            Log.d(TAG, "onSuccess: updated tags post count\n"
//                                                                    + tag + " has " + tagPostsCount + " posts");
//                                                        }
//                                                    })
//                                                    .addOnFailureListener(new OnFailureListener() {
//                                                        @Override
//                                                        public void onFailure(@NonNull Exception e) {
//                                                            Log.d(TAG, "onFailure: failed to update tags posts count\n" +
//                                                                    e.getMessage());
//                                                        }
//                                                    });
//
//                                        }
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Log.d(TAG, "onFailure: failed to add post id to tags\n" +
//                                                e.getMessage());
//                                    }
//                                });
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "onFailure: failed to add post id to tag collection\n" +
//                                e.getMessage());
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

        //show progress
        ((MainActivity) Objects.requireNonNull(getActivity())).progressBar.setVisibility(View.VISIBLE);
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
                    for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            final String postId = doc.getDocument().getId();
                            final Posts post = doc.getDocument().toObject(Posts.class).withId(postId);
                            final String postUserId = post.getUser_id();
//                            populateTags(post, postId);
//                            populateCats(post, postId);
                            coMeth.getDb()
                                    .collection("Users")
                                    .document(postUserId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            //on success
                                            if (documentSnapshot.exists()) {
                                                //user exists
                                                Log.d(TAG, "onComplete: adding posts");
                                                Users user = Objects.requireNonNull(
                                                        documentSnapshot.toObject(Users.class))
                                                        .withId(postUserId);
                                                if (!postsList.contains(post)) {
                                                    usersList.add(user);
                                                    postsList.add(post);
                                                }
                                                postsRecyclerAdapter.notifyDataSetChanged();
                                                try {
                                                    ((MainActivity) getActivity()).hideProgress();
                                                } catch (NullPointerException activityIsNull) {
                                                    Log.d(TAG, "onSuccess: actvity is null\n" +
                                                            activityIsNull.getMessage());
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: failed to get user info\n" +
                                                    e.getMessage());
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

    //load post on a background thread
//    public class LoadPostsTask extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            Log.d(TAG, "doInBackground: ");
//            loadMorePosts();
//            return null;
//        }
//    }

}
