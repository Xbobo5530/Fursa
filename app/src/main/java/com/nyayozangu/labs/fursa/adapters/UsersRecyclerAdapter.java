package com.nyayozangu.labs.fursa.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.LoginActivity;
import com.nyayozangu.labs.fursa.activities.UserPageActivity;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.annotation.Nullable;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWING;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MESSAGE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TIMESTAMP;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID_VAL;

public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.ViewHolder> /*implements View.OnClickListener*/{

    private static final String TAG = "UsersRecyclerAdapter";
    private List<User> usersList;
    public Context context;
    public RequestManager glide;
    private CoMeth coMeth = new CoMeth();
    private String userId, currentUserId;
    private ViewHolder mHolder;

    public UsersRecyclerAdapter(List<User> usersList, RequestManager glide) {
        this.usersList = usersList;
        this.glide = glide;
    }

    @NonNull
    @Override
    public UsersRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate the viewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersRecyclerAdapter.ViewHolder holder, int position) {

        User user = usersList.get(position);
        mHolder = holder;
        currentUserId = coMeth.getUid();
        if (user != null) {
            userId = usersList.get(position).UserId;
            Log.d(TAG, "onBindViewHolder: userId at position " + position + " is " + userId);
            holder.setUserDetails(user);
            holder.handleFollowButtonVisibility();
//            holder.pageItemView.setOnClickListener(this);
            holder.pageItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToUserPage(userId);
                }
            });
//            holder.mFollowButton.setOnClickListener(this);
            holder.mFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (coMeth.isLoggedIn()){
                        followUser(userId, holder);
                    }else{
                        goToLogin(context.getResources().getString(R.string.login_to_follow));
                    }
                }
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        glide.clear(holder.userImageView);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    private void goToUserPage(String userId) {
        Intent intent = new Intent(context, UserPageActivity.class);
        intent.putExtra(USER_ID, userId);
        context.startActivity(intent);
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.usersFollowButton:
//                if (coMeth.isLoggedIn()){
//                    followUser(userId, mHolder);
//                }else{
//                    goToLogin(context.getResources().getString(R.string.login_to_follow));
//                }
//                break;
//            case R.id.userListItemItemView:
//                goToUserPage(userId);
//        }
//    }

    private void followUser(final String userId, final ViewHolder holder) {
        holder.mFollowButton.setText(context.getResources().getString(R.string.loading_text));
        Map<String, Object> followersMap = new HashMap<>();
        followersMap.put(TIMESTAMP, FieldValue.serverTimestamp());
        followersMap.put(USER_ID_VAL, currentUserId);
        CollectionReference followersRef = coMeth.getDb().collection(USERS + "/" + userId + "/" + FOLLOWERS);
        followersRef.document(currentUserId).set(followersMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //add follower ref
                addFollowingRef(userId, holder);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: failed to add follower\n" + e.getMessage(), e);
                        String errorMessage = context.getResources().getString(R.string.error_text) + ": " + e.getMessage();
                        holder.showSnack(errorMessage);
                    }
                });
    }

    private void addFollowingRef(String userId, final ViewHolder holder) {
        Map<String, Object> followingMap = new HashMap<>();
        followingMap.put(TIMESTAMP, FieldValue.serverTimestamp());
        followingMap.put(USER_ID_VAL, userId);
        DocumentReference followingRef = coMeth.getDb().collection(USERS + "/" + currentUserId + "/" + FOLLOWING).document(userId);
        followingRef.set(followingMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //hide following button
                holder.mFollowButton.setVisibility(View.GONE);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: failed to add following ref\n" + e.getMessage(), e);
                        String errorMessage = context.getResources().getString(R.string.error_text) + ": " + e.getMessage();
                        holder.showSnack(errorMessage);
                    }
                });
    }

    private void goToLogin(String message) {
        Intent mIntent = new Intent(context, LoginActivity.class);
        mIntent.putExtra(MESSAGE, message);
        context.startActivity(mIntent);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View mView;
        //initiate items on view holder
        private TextView usernameField, bioField;
        private ImageView userImageView;
        private ConstraintLayout pageItemView;
        private Button mFollowButton;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            usernameField = mView.findViewById(R.id.userListItemUsernameTextView);
            bioField = mView.findViewById(R.id.userListItemBioTextView);
            userImageView = mView.findViewById(R.id.userListItemUserImageView);
            pageItemView = mView.findViewById(R.id.userListItemItemView);
            mFollowButton = mView.findViewById(R.id.usersFollowButton);
        }

        void handleFollowButtonVisibility(){
            if (coMeth.isLoggedIn()){
                // create a user reference,
                currentUserId = coMeth.getUid();
                if (currentUserId.equals(userId)){
                    mFollowButton.setVisibility(View.GONE);
                }else{
                    DocumentReference userRef = coMeth.getDb()
                            .collection(USERS + "/" + userId + "/" + FOLLOWERS)
                            .document(currentUserId);
                    userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e == null){
                                if (documentSnapshot != null) {
                                    if (documentSnapshot.exists()) {
                                        mFollowButton.setVisibility(View.GONE);
                                    } else {
                                        mFollowButton.setVisibility(View.VISIBLE);
                                    }
                                }else{
                                    mFollowButton.setVisibility(View.VISIBLE);
                                }
                            }else{
                                Log.e(TAG, "onEvent: failed to get follower\n" + e.getMessage(), e);
                            }
                        }
                    });
                }
            }else{
                mFollowButton.setVisibility(View.VISIBLE);
            }
        }

        void setUserDetails(User user) {
            String username = user.getName();
            String bio = user.getBio();
            String imageUrl = user.getImage();
            String thumbUrl = user.getThumb();

            usernameField.setText(username);
            if (bio != null && !bio.isEmpty()){
                bioField.setText(bio);
            }else{
                bioField.setVisibility(View.GONE);
            }
            if (thumbUrl != null){
                coMeth.setCircleImage(R.drawable.ic_action_person_placeholder, thumbUrl,
                        userImageView, context);
            } else if (imageUrl != null){
                coMeth.setCircleImage(R.drawable.ic_action_person_placeholder, imageUrl,
                        userImageView,context);
            }else{
                userImageView.setImageDrawable(
                        context.getResources().getDrawable(R.drawable.ic_action_person_placeholder));
            }
        }

        void showSnack(String message){
            Snackbar.make(mView.findViewById(R.id.usersListLayout), message, Snackbar.LENGTH_LONG).show();
        }
    }
}