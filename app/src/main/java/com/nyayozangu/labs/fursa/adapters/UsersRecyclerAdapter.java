package com.nyayozangu.labs.fursa.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.UserPageActivity;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Users;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.ViewHolder> {

    private static final String TAG = "Sean";
    public List<Users> usersList;
    public Context context;
    public RequestManager glide;
    private CoMeth coMeth = new CoMeth();


    public UsersRecyclerAdapter(List<Users> usersList, RequestManager glide) {
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
    public void onBindViewHolder(@NonNull UsersRecyclerAdapter.ViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder: users recycler adapter");
        String username = usersList.get(position).getName();
        String bio = usersList.get(position).getBio();
        if (usersList.get(position).getThumb() != null) {
            String thumb = usersList.get(position).getThumb();
            holder.setUserData(username, bio, thumb);
        } else if (usersList.get(position).getImage() != null) {
            String image = usersList.get(position).getThumb();
            holder.setUserData(username, bio, image);
        } else {
            holder.setUserData(username, bio, null);
        }
        //set click listener to user item view
        final String userId = usersList.get(position).UserId;
        holder.pageItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUserPage(userId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    private void goToUserPage(String userId) {
        Intent goToUserPageIntnet = new Intent(context, UserPageActivity.class);
        goToUserPageIntnet.putExtra("userId", userId);
        context.startActivity(goToUserPageIntnet);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;

        //initiate items on view holder
        private TextView usernameField, bioField;
        private CircleImageView userImageView;
        private ConstraintLayout pageItemView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            usernameField = mView.findViewById(R.id.userListItemUsernameTextView);
            bioField = mView.findViewById(R.id.userListItemBioTextView);
            userImageView = mView.findViewById(R.id.userListItemUserImageView);
            pageItemView = mView.findViewById(R.id.userListItemItemView);
        }

        public void setUserData(String username, String bio, String userImage) {
            usernameField.setText(username);
            if (bio != null) {
                bioField.setText(bio.trim());
            } else {
                bioField.setVisibility(View.GONE);
            }
            if (userImage != null) {
                try {
                    RequestOptions placeHolderRequest = new RequestOptions();
                    placeHolderRequest.placeholder(R.drawable.ic_action_person_placeholder);
                    glide.applyDefaultRequestOptions(placeHolderRequest)
                            .load(userImage)
//                    .transition(withCrossFade())
                            .into(userImageView);
                } catch (Exception e) {
                    Log.d(TAG, "setImage: failed to set image " + e.getMessage());
                }
            } else {
                userImageView.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_action_person_placeholder));
            }
        }
    }
}
