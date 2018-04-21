package com.nyayozangu.labs.fursa;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Sean on 4/9/18.
 */

class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {


    private static final String TAG = "Sean";
    //member variables for storing posts
    public List<Comments> commentsList;

    public Context context;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    //empty constructor for receiving the posts
    public CommentsRecyclerAdapter(List<Comments> commentsList) {

        //store received posts
        this.commentsList = commentsList;

    }


    @NonNull
    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the viewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        return new CommentsRecyclerAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final CommentsRecyclerAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "at onBindViewHolder");

        holder.setIsRecyclable(false);

        //set comment
        String comment = commentsList.get(position).getComment();
        Log.d(TAG, "onBindViewHolder: \ncomment is: " + comment);
        holder.setComment(comment);

        //set username
        String userId = commentsList.get(position).getUser_id();
        db.collection("Users").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                //check if user exists
                if (documentSnapshot.exists()) {

                    // TODO: 4/16/18 test when user is not available


                    try {
                        //user exists
                        String username = documentSnapshot.get("name").toString();

                        //set username to username textView
                        holder.setUsername(username);

                        //set user image

                        try {
                            String userImageDownloadUrl = documentSnapshot.get("image").toString();
                            holder.setImage(userImageDownloadUrl);
                        } catch (NullPointerException userImageException) {

                            //user image is null
                            Log.e(TAG, "onEvent: ", userImageException);


                        }

                    } catch (NullPointerException userInfoErrorException) {

                        Log.e(TAG, "onEvent: \nuserInfoErrorException: " + userInfoErrorException.getMessage(), userInfoErrorException);

                    }


                } else {

                    //user does not exist
                    Log.d(TAG, "onEvent: user does not exist");


                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //initiate view
        private View mView;

        //initiate items
        private CircleImageView userImageView;
        private TextView commentTextView;
        private TextView usernameTextView;


        public ViewHolder(View itemView) {
            super(itemView);
            //use mView to populate other methods
            mView = itemView;

            userImageView = mView.findViewById(R.id.commentUserImage);
            commentTextView = mView.findViewById(R.id.commentTextView);
            usernameTextView = mView.findViewById(R.id.commentUsernameTextView);


        }

        public void setImage(String imageUrl) {

            userImageView = mView.findViewById(R.id.commentUserImage);

            //add the placeholder image
            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.ic_action_image_placeholder);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOptions).load(imageUrl).into(userImageView);


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