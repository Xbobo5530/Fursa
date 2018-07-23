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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.UserPageActivity;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Users;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID;

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

        Users user = usersList.get(position);
        if (user != null) {
            String username = user.getName();
            String bio = user.getBio();
            String imageUrl = user.getImage();
            String thumbUrl = user.getThumb();
            holder.usernameField.setText(username);
            ImageView userImageView = holder.userImageView;
            if (bio != null && !bio.isEmpty()){
                holder.bioField.setText(bio);
            }else{
                holder.bioField.setVisibility(View.GONE);
            }
            if (thumbUrl != null){
                coMeth.setCircleImage(R.drawable.ic_action_person_placeholder, thumbUrl, userImageView);
            } else if (imageUrl != null){
                coMeth.setCircleImage(R.drawable.ic_action_person_placeholder, imageUrl, userImageView);
            }else{
                userImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_person_placeholder));
            }
            //set click listener to user item view
            final String userId = user.UserId;
            holder.pageItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToUserPage(userId);
                }
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        glide.clear(holder.userImageView);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    private void goToUserPage(String userId) {
        Intent intent = new Intent(context, UserPageActivity.class);
        intent.putExtra(USER_ID, userId);
        context.startActivity(intent);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View mView;
        //initiate items on view holder
        private TextView usernameField, bioField;
        private ImageView userImageView;
        private ConstraintLayout pageItemView;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            usernameField = mView.findViewById(R.id.userListItemUsernameTextView);
            bioField = mView.findViewById(R.id.userListItemBioTextView);
            userImageView = mView.findViewById(R.id.userListItemUserImageView);
            pageItemView = mView.findViewById(R.id.userListItemItemView);
        }
    }
}