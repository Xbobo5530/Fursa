package com.nyayozangu.labs.fursa.adapters;

import android.app.ProgressDialog;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.UserPageActivity;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Comments;
import com.nyayozangu.labs.fursa.models.Users;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID;

/**
 *
 * Created by Sean on 4/9/18.
 */

public class CommentsRecyclerAdapter extends
        RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    private static final String TAG = "Sean";
    private List<Comments> commentsList;
    public String postId;
    public Context context;
    private CoMeth coMeth = new CoMeth();

    private String reportDetails;
    private ProgressDialog progressDialog;
    private String[] reportOptionsList;
    private boolean isAdmin = false;

    private Comments comment;


    //empty constructor for receiving the posts
    public CommentsRecyclerAdapter(List<Comments> commentsList, String postId) {
        //store received posts
        this.commentsList = commentsList;
        this.postId = postId;
    }

    @NonNull
    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                 int viewType) {
        //inflate the viewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();
        ArrayList flags = new ArrayList<String>();
        return new CommentsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentsRecyclerAdapter.ViewHolder holder,
                                 final int position) {

        comment = commentsList.get(position);
        String userComment = comment.getComment();
        holder.setComment(userComment);
        String userId = comment.getUser_id();
        setUserData(holder);

    }

    private void setUserData(@NonNull final ViewHolder holder) {
        final String userId = comment.getUser_id();
        coMeth.getDb().collection(USERS).document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Users user = documentSnapshot.toObject(Users.class);
                            String username = user.getName();
                            holder.setUsername(username);
                            String userImageDownloadUrl = user.getImage();
                            holder.setImage(userImageDownloadUrl);
                            holder.userImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openUserPage(userId);
                                }
                            });
                        } else {
                            //user does not exist
                            Log.d(TAG, "onEvent: user does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: error getting user details\n" + e.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    private void openUserPage(String userId) {
        Intent intent = new Intent(context, UserPageActivity.class);
        intent.putExtra(USER_ID, userId);
        context.startActivity(intent);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //initiate view
        private View mView;

        private ConstraintLayout commentItemView;
        private CircleImageView userImageView;
        private TextView commentTextView, usernameTextView;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            commentItemView = mView.findViewById(R.id.commentsItemLayout);
            userImageView = mView.findViewById(R.id.commentUserImage);
            commentTextView = mView.findViewById(R.id.commentTextView);
            usernameTextView = mView.findViewById(R.id.commentUsernameTextView);
        }

        public void setImage(String imageUrl) {
            if (imageUrl != null) {
                coMeth.setImage(R.drawable.ic_action_person_placeholder,
                        imageUrl, userImageView);
            } else {
                userImageView.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_action_person_placeholder));
            }
        }

        public void setUsername(String username) {
            usernameTextView = mView.findViewById(R.id.commentUsernameTextView);
            usernameTextView.setText(username);
        }

        public void setComment(String comment) {
            commentTextView = mView.findViewById(R.id.commentTextView);
            commentTextView.setText(comment);
        }
    }
}
