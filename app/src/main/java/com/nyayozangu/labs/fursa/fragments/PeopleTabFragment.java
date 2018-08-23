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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.MainActivity;
import com.nyayozangu.labs.fursa.adapters.UsersRecyclerAdapter;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.NAME;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;


/**
 * A simple {@link Fragment} subclass.
 */
public class PeopleTabFragment extends Fragment {

    private static final String TAG = "PeopleTabFragment";
    private CoMeth coMeth = new CoMeth();
    private List<User> usersList;
    private UsersRecyclerAdapter mAdapter;
    private DocumentSnapshot lastVisibleUser;
    private Boolean isFirstPageFirstLoad = true;
    private ProgressBar progressBar;

    public PeopleTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_people, container, false);

        final RecyclerView mRecyclerView = view.findViewById(R.id.peopleRecyclerView);
        progressBar = view.findViewById(R.id.peopleProgressBar);
        usersList = new ArrayList<>();
        mAdapter = new UsersRecyclerAdapter(usersList, Glide.with(this));
        coMeth.handlePostsView(getContext(), getActivity(), mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        handleScrolling(mRecyclerView);
        loadUsers();
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
                    loadMoreUsers();
                }
            }
        });

    }

    private void loadMoreUsers() {
        coMeth.showProgress(progressBar);
        Query nextQuery = coMeth.getDb().collection(USERS).orderBy(NAME).startAfter(lastVisibleUser).limit(20);
        nextQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()){
                    lastVisibleUser = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.getDocuments().size() - 1);

                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
                        if (documentChange.getType() == DocumentChange.Type.ADDED){
                            String userId = documentChange.getDocument().getId();
                            User user = documentChange.getDocument().toObject(User.class).withId(userId);
                            addUser(user);
                        }
                    }
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

    private void addUser(User user) {
        if (!usersList.contains(user)) {
            if (isFirstPageFirstLoad) {
                usersList.add(0, user);
                mAdapter.notifyItemInserted(usersList.size() - 1);
//                mAdapter.notifyDataSetChanged();
                coMeth.stopLoading(progressBar);
            }else{
                usersList.add(user);
                mAdapter.notifyItemInserted(usersList.size() - 1);
//                mAdapter.notifyDataSetChanged();
                coMeth.stopLoading(progressBar);
            }
        }
    }

    private void loadUsers() {
        coMeth.showProgress(progressBar);
        Query firstQuery = coMeth.getDb().collection(USERS).orderBy(NAME).limit(20);
        firstQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()){
                    if (isFirstPageFirstLoad) {
                        lastVisibleUser = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.getDocuments().size() - 1);
                        usersList.clear();

                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
                            if (documentChange.getType() == DocumentChange.Type.ADDED){
                                String userId = documentChange.getDocument().getId();
                                User user = documentChange.getDocument().toObject(User.class).withId(userId);
                                addUser(user);
                            }
                        }
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

    private void showSnack(String message) {
        Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(R.id.mainSnack),
                message, Snackbar.LENGTH_LONG)
                .show();
    }
}