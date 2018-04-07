package com.nyayozangu.labs.fursa;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
 * Created by Sean on 4/6/18.
 */

public class SavedPostsRecyclerAdapter extends RecyclerView.Adapter<SavedPostsRecyclerAdapter.ViewHolder> {


    private static final String TAG = "Sean";
    //member variables for storing posts
    public List<Posts> savedPostsList;

    public Context context;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    //empty constructor for receiving the posts
    public SavedPostsRecyclerAdapter(List<Posts> savedPostsList) {

        //store received posts
        this.savedPostsList = savedPostsList;
    }

    @NonNull
    @Override
    public SavedPostsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the viewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent, false);
        context = parent.getContext();

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();


        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        return new SavedPostsRecyclerAdapter.ViewHolder(view);
    }

   /* @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }*/


    @Override
    public void onBindViewHolder(@NonNull final SavedPostsRecyclerAdapter.ViewHolder holder, final int position) {
        Log.d(TAG, "at onBindViewHolder");

        holder.setIsRecyclable(false);

        //get title from holder
        String titleData = savedPostsList.get(position).getTitle();
        //set the post title
        holder.setTitle(titleData);

        //set the data after the viewHolder has been bound
        String descData = savedPostsList.get(position).getDesc();
        //set the description to the view holder
        holder.setDesc(descData);


        final String currentUserId;
        //handling getting the user who clicked like
        currentUserId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "user is logged in\n current userId is :" + currentUserId);

        //handle post image
        String imageUrl = savedPostsList.get(position).getImage_url();
        String thumbUrl = savedPostsList.get(position).getThumb_url();
        holder.setPostImage(imageUrl, thumbUrl);

        //receive the post id
        final String postId = savedPostsList.get(position).PostId;

        //handle username and userImage
        final String userId = savedPostsList.get(position).getUser_id();
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
                    // TODO: 4/6/18 fix when username is not provided
                    try {
                        String userName = task.getResult().get("name").toString();
                        Log.d(TAG, "userName is: " + userName);
                        holder.setPostUserName(userName);

                        //get userImage
                        String userImageDownloadUri = task.getResult().get("image").toString();
                        holder.setUserImage(userImageDownloadUri);
                    } catch (Exception exception) {
                        Log.d(TAG, "error: " + exception.getMessage());
                    }

                } else {
                    //failed to retrieve the userData
                    Log.d(TAG, "task not successful");
                    String errorMessage = task.getException().getMessage();
                    Log.d(TAG, "Failed to retrieve userData: " + errorMessage);
                    // TODO: 4/4/18 load default images
                }
            }
        });

        //handle date for posts
        // TODO: 4/5/18 looking into the timestamp bug; app crashes on creating new post
        long millis = savedPostsList.get(position).getTimestamp().getTime();
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


        //get likes
        //determine likes by current user
        final String finalCurrentUserId = currentUserId;
        db.collection("Posts/" + postId + "/Likes").document(finalCurrentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                //update the like button real time
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "at get likes, updating likes real time");
                    //user has liked
                    holder.savedPostLikeButton.setImageDrawable(context.getDrawable(R.drawable.ic_action_liked));
                } else {
                    //current user has not liked the post
                    holder.savedPostLikeButton.setImageDrawable(context.getDrawable(R.drawable.ic_action_like_unclicked));
                }
            }
        });

        //get saves
        db.collection("Posts/" + postId + "/Saves").document(finalCurrentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                //update the save button real time
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "at get saves, updating saves realtime");
                    //user has saved post
                    holder.savedPostSaveButton.setImageDrawable(context.getDrawable(R.drawable.ic_action_bookmarked));
                } else {
                    //user has not liked post
                    holder.savedPostSaveButton.setImageDrawable(context.getDrawable(R.drawable.ic_action_bookmark_outline));
                }
            }
        });




        //likes feature
        //set an a click listener to the like button
        holder.savedPostLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Posts/" + postId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

            }
        });


        //set a click listener to the save button
        holder.savedPostSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Posts/" + postId + "/Saves").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        //get data from the saves collections
                        //check if user has already saved the post
                        if (!task.getResult().exists()) {
                            Map<String, Object> savesMap = new HashMap<>();
                            savesMap.put("timestamp", FieldValue.serverTimestamp());
                            //save new post
                            db.collection("Posts/" + postId + "/Saves").document(finalCurrentUserId).set(savesMap);
                            //notify user that post has been saved
                            Snackbar.make(holder.mView.findViewById(R.id.savedPostLayout),
                                    "Saved...", Snackbar.LENGTH_SHORT).show();
                        } else {
                            //delete saved post
                            db.collection("Posts/" + postId + "/Saves").document(finalCurrentUserId).delete();
                        }
                    }
                });
            }
        });

        //share post
        //set onclick listener to the share button
        holder.shareSavedPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Sharing post");
                //create post url
                String savedPostUrl = context.getResources().getString(R.string.fursa_url_head) + postId;
                Log.d(TAG, "savedPostUrl is: " + savedPostUrl);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
                shareIntent.putExtra(Intent.EXTRA_TEXT, savedPostUrl);
                context.startActivity(Intent.createChooser(shareIntent, "Share this post with"));

            }
        });
    }


    private boolean isLoggedIn() {
        //determine if user is logged in
        return mAuth.getCurrentUser() != null;
    }

    @Override
    public int getItemCount() {
        //the number of posts
        return savedPostsList.size();
    }

    //implement the viewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {


        //initiate view
        private View mView;

        //initiate elements in the view holder
        private TextView titleTextView;
        private TextView descTextView;
        private ImageView savedPostImageView;
        private TextView savedPostUserNameTextView;
        private TextView savedPostDateTextView;
        private CircleImageView savedPostUserImageCircleView;

        //likes
        private ImageView savedPostLikeButton;
        private TextView savedPostLikesCount;

        //saves
        private ImageView savedPostSaveButton;

        //share
        private ImageView shareSavedPostButton;

        public ViewHolder(View itemView) {
            super(itemView);
            //use mView to populate other methods
            mView = itemView;

            savedPostLikeButton = mView.findViewById(R.id.savedPostLikeImageView);
            savedPostLikesCount = mView.findViewById(R.id.savedPostLikeCountText);

            savedPostSaveButton = mView.findViewById(R.id.savedPostSaveImageView);

            shareSavedPostButton = mView.findViewById(R.id.savedPostShareImageView);

        }

        //retrieve the title
        public void setTitle(String title) {

            titleTextView = mView.findViewById(R.id.savedPostTitleTextView);
            titleTextView.setText(title);
        }

        //retrieves description on post
        public void setDesc(String descText) {

            descTextView = mView.findViewById(R.id.savedPostDescTextView);
            descTextView.setText(descText);
        }

        //retrieve the image
        public void setPostImage(String imageDownloadUrl, String thumbDownloadUrl) {

            savedPostImageView = mView.findViewById(R.id.savedPostImageView);

            RequestOptions requestOptions = new RequestOptions();
            // TODO: 4/5/18 replace postImage placeholder image
            requestOptions.placeholder(R.drawable.ic_action_image_placeholder);

            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(imageDownloadUrl)
                    .thumbnail(Glide.with(context).load(thumbDownloadUrl))
                    .into(savedPostImageView);
        }

        //add set post username
        public void setPostUserName(String userName) {

            savedPostUserNameTextView = mView.findViewById(R.id.postUsernameTextView);
            savedPostUserNameTextView.setText(userName);

        }

        //set post date
        public void setPostDate(String date) {

            // TODO: 4/4/18 show different time status  like one minute ago ++

            savedPostDateTextView = mView.findViewById(R.id.savedPostDateTextView);
            savedPostDateTextView.setText(date);

        }

        //update the number of likes
        public void updateLikesCount(int likesCount) {

            savedPostLikesCount = mView.findViewById(R.id.postLikeCountText);

            //check the number of likes
            if (likesCount == 1) {
                //use like
                savedPostLikesCount.setText(String.valueOf(likesCount) + " Like");
            } else {
                //else use likes
                savedPostLikesCount.setText(String.valueOf(likesCount) + " Likes");
            }


        }

        //set userImage
        public void setUserImage(String userImageDownloadUri) {

            savedPostUserImageCircleView = mView.findViewById(R.id.postUserImageCircleImageView);
            //add the placeholder image
            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.crop_image_menu_flip);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOptions).load(userImageDownloadUri).into(savedPostUserImageCircleView);

        }
    }


}
