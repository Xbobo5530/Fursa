package com.nyayozangu.labs.fursa;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Sean on 4/4/18.
 * <p>
 * Adapter class for the Recycler view
 */

public class PostsRecyclerAdapter extends RecyclerView.Adapter<PostsRecyclerAdapter.ViewHolder> {


    private static final String TAG = "Sean";
    //member variables for storing posts
    public List<Posts> postsList;

    public Context context;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    //empty constructor for receiving the posts
    public PostsRecyclerAdapter(List<Posts> postsList) {

        //store received posts
        this.postsList = postsList;
    }

    @NonNull
    @Override
    public PostsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the viewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent, false);
        context = parent.getContext();

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();


        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostsRecyclerAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "at onBindViewHolder");

        holder.setIsRecyclable(false);

        //set the data after the viewHolder has been bound
        String descData = postsList.get(position).getDesc();
        //set the data to the view holder
        holder.setDesc(descData);

        String currentUserId;

        //handling getting the user who clicked like
        if (isLoggedIn()) {
            currentUserId = mAuth.getCurrentUser().getUid();
            Log.d(TAG, "user is logged in\n current userId is :" + currentUserId);
        } else {
            currentUserId = null;
            Log.d(TAG, "user is logged in\n current userId is :" + currentUserId);
        }

        //handle post image
        String imageUrl = postsList.get(position).getImage_url();
        String thumbUrl = postsList.get(position).getThumb_url();
        holder.setPostImage(imageUrl, thumbUrl);

        //receive the post id
        final String postId = postsList.get(position).PostId;

        //handle username and userImage
        final String userId = postsList.get(position).getUser_id();
        //retrieve username from db
        db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //get the result and retrieve userName and Image Url
                Log.d(TAG, "at getUserId from db query");
                if (task.isSuccessful()) {
                    //task is successful
                    Log.d(TAG, "task is successful");
                    //get userName
                    String userName = task.getResult().get("name").toString();
                    Log.d(TAG, "userName is: " + userName);
                    holder.setPostUserName(userName);

                    //get userImage
                    String userImageDownloadUri = task.getResult().get("image").toString();
                    holder.setUserImage(userImageDownloadUri);

                } else {
                    //failed to retrieve the userData
                    Log.d(TAG, "task not successful");
                    String errorMessage = task.getException().getMessage();
                    Log.d(TAG, "Failed to retrieve userData: " + errorMessage);
                    // TODO: 4/4/18 notify users on errors(maybe use Snackbars)
                    // TODO: 4/4/18 load default images
                }
            }
        });

        //handle date for posts
        // TODO: 4/5/18 looking into the timestamp bug; app crashes on creating new post
        long millis = postsList.get(position).getTimestamp().getTime();
        //convert millis to date time format
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(millis)).toString();
        holder.setPostDate(dateString);


        //get likes count
        //create query to count
        db.collection("Posts/" + postId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                Log.d(TAG, "at onEvent, when likes change");
                if (!queryDocumentSnapshots.isEmpty()) {

                    //post has likes
                    int numberOfLikes = queryDocumentSnapshots.size();
                    holder.updateLikesCount(numberOfLikes);

                } else {
                    //post has no likes
                    holder.updateLikesCount(0);

                }
            }
        });

        if (isLoggedIn()) {
            //get likes
            //determine likes by current user
            final String finalCurrentUserId = currentUserId;
            db.collection("Posts/" + postId + "/Likes").document(finalCurrentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    //update the like button real time
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "at get likes");
                        //user has liked
                        holder.postLikeButton.setImageDrawable(context.getDrawable(R.drawable.ic_action_liked));
                    } else {
                        //current user has not liked the post
                        holder.postLikeButton.setImageDrawable(context.getDrawable(R.drawable.ic_action_like_unclicked));
                    }
                }
            });

        }


        //likes feature
        final String finalCurrentUserId = currentUserId;
        holder.postLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isLoggedIn()) {

                    db.collection("Posts/" + postId + "/Likes").document(finalCurrentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            //get data from teh likes collection

                            //check if current user has already liked post
                            if (!task.getResult().exists()) {
                                Map<String, Object> likesMap = new HashMap<>();
                                likesMap.put("timestamp", FieldValue.serverTimestamp());

                                //db.collection("Posts").document(postId).collection("Likes");
                                //can alternatively ne written
                                db.collection("Posts/" + postId + "/Likes").document(finalCurrentUserId).set(likesMap);

                            } else {

                                //delete the like
                                db.collection("Posts/" + postId + "/Likes").document(finalCurrentUserId).delete();


                            }

                        }
                    });

                } else {
                    //user is not signed in, send to log in page
                    goToLogin();
                }


            }
        });

    }

    private void goToLogin() {
        //take user to the login page
        Log.d(TAG, "at goToLogin");
        // TODO: 4/5/18 take user to the login activity

    }

    private boolean isLoggedIn() {
        //determine if user is logged in
        return mAuth.getCurrentUser() != null;
    }

    @Override
    public int getItemCount() {
        //the number of posts
        return postsList.size();
    }

    //implement the viewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {


        //initiate view
        private View mView;

        //initiate elements in the view holder
        private TextView descTextView;
        private ImageView postImageView;
        private TextView postUserNameTextView;
        private TextView postDateTextView;
        private CircleImageView postUserImageCircleView;

        //likes
        private ImageView postLikeButton;
        private TextView postLikesCount;

        public ViewHolder(View itemView) {
            super(itemView);
            //use mView to populate other methods
            mView = itemView;

            postLikeButton = mView.findViewById(R.id.postLikeImageView);
            postLikesCount = mView.findViewById(R.id.postLikeCountText);

        }

        //retrieves description on post
        public void setDesc(String descText) {

            descTextView = mView.findViewById(R.id.postDescTextView);
            descTextView.setText(descText);
        }

        //retrieve the image
        public void setPostImage(String imageDownloadUrl, String thumbDownloadUrl) {

            postImageView = mView.findViewById(R.id.postImageView);

            RequestOptions requestOptions = new RequestOptions();
            // TODO: 4/5/18 replace postImage placeholder image
            requestOptions.placeholder(R.drawable.common_google_signin_btn_text_dark);

            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(imageDownloadUrl)
                    .thumbnail(Glide.with(context).load(thumbDownloadUrl))
                    .into(postImageView);
        }

        //add set post username
        public void setPostUserName(String userName) {

            postUserNameTextView = mView.findViewById(R.id.postUsernameTextView);
            postUserNameTextView.setText(userName);

        }

        //set post date
        public void setPostDate(String date) {

            // TODO: 4/4/18 show different time status  like one minute ago ++

            postDateTextView = mView.findViewById(R.id.postDateTextView);
            postDateTextView.setText(date);

        }

        //update the number of likes
        public void updateLikesCount(int likesCount) {

            postLikesCount = mView.findViewById(R.id.postLikeCountText);

            //check the number of likes
            if (likesCount == 1) {
                //use like
                postLikesCount.setText(String.valueOf(likesCount) + " Like");
            } else {
                //else use likes
                postLikesCount.setText(String.valueOf(likesCount) + " Likes");
            }



        }

        //set userImage
        public void setUserImage(String userImageDownloadUri) {

            postUserImageCircleView = mView.findViewById(R.id.postUserImageCircleImageView);
            //add the placeholder image
            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.crop_image_menu_flip);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOptions).load(userImageDownloadUri).into(postUserImageCircleView);

        }
    }
}
