package com.nyayozangu.labs.fursa.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.MainActivity;
import com.nyayozangu.labs.fursa.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.models.Post;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.EVENT_DATE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;


/**
 * A simple {@link Fragment} subclass.
 */
public class UpcomingTabFragment extends Fragment {

    // TODO: 7/24/18 continue with this mess

    private static final String TAG = "Sean";
    private static final String UPCOMING_FRAGMENT = "UpcomingTabFragment";
    private CoMeth coMeth = new CoMeth();
    private List<Post> postsList;
    private List<User> usersList;
    private PostsRecyclerAdapter mAdapter;
    private DocumentSnapshot lastVisiblePost;
    private Boolean isFirstPageFirstLoad = true;
    private ProgressBar progressBar;
    private Date today = new Date();
    private Calendar date = new GregorianCalendar();

    public UpcomingTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_up_coming_tab, container, false);

        final RecyclerView mRecyclerView = view.findViewById(R.id.upcomingPostRecyclerView);
        progressBar = view.findViewById(R.id.savedProgressBar);
        postsList = new ArrayList<>();
        usersList = new ArrayList<>();
        mAdapter = new PostsRecyclerAdapter(postsList, usersList, UPCOMING_FRAGMENT,
                Glide.with(this), getActivity());
        coMeth.handlePostsView(getContext(), getActivity(), mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        handleScrolling(mRecyclerView);
        date.add(Calendar.MONTH, 2);
        Query firstQuery = coMeth.getDb().collection(POSTS)
                .orderBy(EVENT_DATE, Query.Direction.DESCENDING)
                .whereGreaterThan(EVENT_DATE, date.getTime())
                .limit(10);
        loadPosts(firstQuery);
        handleBottomNavReSelect(mRecyclerView);
        return view;
    }

    private void handleBottomNavReSelect(final RecyclerView mRecyclerView) {
        if (getActivity() != null) {
            ((MainActivity)(getActivity())).mainBottomNav
                    .setOnNavigationItemReselectedListener(
                            new BottomNavigationView.OnNavigationItemReselectedListener() {
                                @Override
                                public void onNavigationItemReselected(@NonNull MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.bottomNavHomeItem:
                                            mRecyclerView.smoothScrollToPosition(0);
                                            break;
                                        default:
                                            Log.d(TAG, "onNavigationItemReselected: at default");
                                    }
                                }
                            });
        }
    }

    private void handleScrolling(final RecyclerView mRecyclerView) {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean reachedBottom = !mRecyclerView.canScrollVertically(1);
                if (reachedBottom){
                    loadMorePosts();
                }
            }
        });

    }

    private void loadMorePosts() {
        showProgress();
        Query nextQuery = coMeth.getDb().collection(POSTS)
                .orderBy(EVENT_DATE, Query.Direction.DESCENDING)
                .whereGreaterThan(EVENT_DATE, date.getTime()).startAfter(lastVisiblePost).limit(10);
        nextQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()){
                    lastVisiblePost = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.getDocuments().size() - 1);
                    filterUpcomingPosts(queryDocumentSnapshots);
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onEvent: failed to get posts\n" + e.getMessage(), e);
                        showSnack(getResources().getString(R.string.error_text) + ": " + e.getMessage());
                    }
                });
    }

    private void loadPosts(final Query query) {
        showProgress();
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()){
                    if (isFirstPageFirstLoad) {
                        lastVisiblePost = queryDocumentSnapshots.getDocuments().get(
                                queryDocumentSnapshots.getDocuments().size() - 1);
                        filterUpcomingPosts(queryDocumentSnapshots);
                    }
                    isFirstPageFirstLoad = false;
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onEvent: failed to get upcoming posts\n" + e.getMessage());
                        showSnack(getResources().getString(R.string.error_text) + ": " + e.getMessage());
                    }
                });
    }

    private void filterUpcomingPosts(@NonNull QuerySnapshot queryDocumentSnapshots) {
        postsList.clear();
        usersList.clear();
        for (DocumentChange document : queryDocumentSnapshots.getDocumentChanges()){
            if (document.getType() == DocumentChange.Type.ADDED){
                String postId = document.getDocument().getId();
                Post post = document.getDocument().toObject(Post.class).withId(postId);
                Date eventDate = post.getEvent_date();
                Date eventEndDate = post.getEvent_end_date();
                Log.d(TAG, "filterUpcomingPosts: \nevent date is: " + eventDate + "\nend date is: " + eventEndDate + "\ntoday is: " + today);
                if (eventDate != null || eventEndDate != null){

                    long twoThouNineHundYears = new Date(1970, 0, 0).getTime();
                    Date mEventDate = new Date(post.getEvent_date().getTime() - twoThouNineHundYears);

                    if (eventEndDate == null){
                        if (mEventDate.after(today)){
                            getUserDetails(post);
                        }
                    }else{
                        //end date is not null
                        Date mEventEndDate = new Date(post.getEvent_end_date().getTime() - twoThouNineHundYears);
                        if (today.before(mEventEndDate)){
                            getUserDetails(post);
                        }
                    }
                }
            }
        }
    }

    private void getUserDetails(final Post post) {
        final String userId = post.getUser_id();
        DocumentReference postUserRef = coMeth.getDb().collection(USERS).document(userId);
        postUserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    User user = Objects.requireNonNull(documentSnapshot.toObject(User.class)).withId(userId);
                    if (!postsList.contains(post)) {
                        if (isFirstPageFirstLoad) {
                            usersList.add(0, user);
                            postsList.add(0, post);
                            mAdapter.notifyItemInserted(postsList.size() - 1);
                        } else {
                            usersList.add(user);
                            postsList.add(post);
                            mAdapter.notifyItemInserted(postsList.size() - 1);
                        }
                    }
                }
                stopLoading();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: failed to get upcoming post user details\n" +
                                e.getMessage());
                        showSnack(getResources().getString(R.string.error_text) + ": " + e.getMessage());
                    }
                });
    }

    private void showProgress() {
        if (progressBar.getVisibility() != View.VISIBLE){
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void stopLoading(){
        if (progressBar.getVisibility() != View.GONE){
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showSnack(String message) {
        Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(R.id.mainSnack),
                message, Snackbar.LENGTH_LONG)
                .show();
    }
}