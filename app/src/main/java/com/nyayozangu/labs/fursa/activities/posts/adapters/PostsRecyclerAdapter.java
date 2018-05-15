package com.nyayozangu.labs.fursa.activities.posts.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.comments.CommentsActivity;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.ViewPostActivity;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.UserPageActivity;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Sean on 4/4/18.
 * <p>
 * Adapter class for the Recycler view
 */

public class PostsRecyclerAdapter extends RecyclerView.Adapter<PostsRecyclerAdapter.ViewHolder> implements View.OnClickListener {


    private static final String TAG = "Sean";

    //member variables for storing posts
    public List<Posts> postsList;
    public List<Users> usersList;

    public Context context;

    private int lastPosition = -1;

    private String userId;
    private String className;
    private ProgressDialog progressDialog;
    private CoMeth coMeth = new CoMeth();

    private String postTitle;
    private String postUserId;

    //empty constructor for receiving the posts
    public PostsRecyclerAdapter(List<Posts> postsList, List<Users> usersList, String className) {

        //store received posts
        this.postsList = postsList;
        this.usersList = usersList;
        this.className = className;
    }

    @NonNull
    @Override
    public PostsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                              int viewType) {

        //inflate the viewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_list_item, parent, false);
        context = parent.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostsRecyclerAdapter.ViewHolder holder,
                                 int position) {

        Log.d(TAG, "at onBindViewHolder");
        postTitle = postsList.get(position).getTitle();
        holder.setTitle(postTitle);
        String descData = postsList.get(position).getDesc();
        holder.setDesc(descData);

        //set location
        ArrayList locationArray = postsList.get(position).getLocation();
        holder.setPostLocation(locationArray);

        final String currentUserId;

        //handling getting the user who clicked like
        if (coMeth.isLoggedIn()) {
            currentUserId = coMeth.getUid();
            Log.d(TAG, "user is logged in\n current user_id is :" + currentUserId);
        } else {
            currentUserId = null;
            Log.d(TAG, "user is logged in\n current user_id is :" + currentUserId);
        }

        //handle post image
        String imageUrl = postsList.get(position).getImage_url();
        String thumbUrl = postsList.get(position).getThumb_url();
        holder.setPostImage(imageUrl, thumbUrl);

        //receive the post id
        final String postId = postsList.get(position).PostId;
        Log.d(TAG, "onBindViewHolder: \npostId is " + postId);
        //handle name and image
        String userName = usersList.get(position).getName();
        String userImageDownloadUri = usersList.get(position).getImage();
        postUserId = usersList.get(position).UserId;
        Log.d(TAG, "onBindViewHolder: \npostUseId is " + postUserId);
        holder.setUserData(userName, userImageDownloadUri);

        //use image click listener
        holder.postUserImageCircleView.setOnClickListener(this);
        holder.postUsernameTextView.setOnClickListener(this);

        //handle date for posts
        if (postsList.get(position).getTimestamp() != null) {

            long millis = postsList.get(position).getTimestamp().getTime();
            String dateString = coMeth.processPostDate(millis);
            holder.setPostDate(dateString);

        }

        //get likes count
        //create query to count
        coMeth.getDb()
                .collection("Posts/" + postId + "/Likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
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


        if (coMeth.isLoggedIn()) {
            //get likes
            //determine likes by current user
            coMeth.getDb()
                    .collection("Posts/" + postId + "/Likes")
                    .document(currentUserId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            //update the like button real time
                            if (documentSnapshot.exists()) {
                                Log.d(TAG, "at get likes, updating likes real time");
                                //user has liked
                                holder.postLikeButton.setImageDrawable(context.getDrawable(R.drawable.ic_action_liked));
                            } else {
                                //current user has not liked the post
                                holder.postLikeButton.setImageDrawable(context.getDrawable(R.drawable.ic_action_like_unclicked));
                            }
                        }
                    });

            //get saves
            coMeth.getDb()
                    .collection("Posts/" + postId + "/Saves")
                    .document(currentUserId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            //update the save button real time
                            if (documentSnapshot.exists()) {
                                Log.d(TAG, "at get saves, updating saves realtime");
                                //user has saved post
                                holder.postSaveButton.setImageDrawable(context.getDrawable(R.drawable.ic_action_bookmarked));
                            } else {
                                //user has not liked post
                                holder.postSaveButton.setImageDrawable(context.getDrawable(R.drawable.ic_action_bookmark_outline));
                            }
                        }
                    });

        }

        //likes feature
        //set an a click listener to the like button
        holder.postLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //disable button
                holder.postLikeButton.setClickable(false);

                if (coMeth.isConnected() && coMeth.isLoggedIn()) {

                    coMeth.getDb()
                            .collection("Posts/" + postId + "/Likes")
                            .document(currentUserId)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    //get data from teh likes collection
                                    if (task.isSuccessful()) {
                                        //check if current user has already liked post
                                        if (!task.getResult().exists()) {

                                            Map<String, Object> likesMap = new HashMap<>();
                                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                                            coMeth.getDb()
                                                    .collection("Posts/" + postId + "/Likes")
                                                    .document(currentUserId)
                                                    .set(likesMap);

                                        } else {
                                            //delete the like
                                            coMeth.getDb()
                                                    .collection("Posts/" + postId + "/Likes")
                                                    .document(currentUserId)
                                                    .delete();
                                        }
                                    } else {

                                        Log.d(TAG, "onComplete: like post task failed\n" + task.getException());

                                    }
                                }
                            });
                } else {

                    if (!coMeth.isLoggedIn()) {

                        goToLogin(context.getString(R.string.login_to_like));

                    }
                    if (!coMeth.isConnected()) {

                        //alert user is not connected
                        showSnack(holder, context.getString(R.string.failed_to_connect_text));
                    }

                }

                //enable button
                holder.postLikeButton.setClickable(true);

            }
        });

        //save button click listener
        holder.postSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //disable button
                holder.postSaveButton.setClickable(false);

                //check if user is connected to the internet
                if (coMeth.isConnected() && coMeth.isLoggedIn()) {

                    coMeth.getDb()
                            .collection("Posts/" + postId + "/Saves")
                            .document(currentUserId)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if (task.isSuccessful()) {

                                        //get data from the saves collections
                                        //check if user has already saved the post
                                        if (!task.getResult().exists()) {

                                            Map<String, Object> savesMap = new HashMap<>();
                                            savesMap.put("timestamp", FieldValue.serverTimestamp());
                                            //save new post
                                            coMeth.getDb()
                                                    .collection("Posts/" + postId + "/Saves")
                                                    .document(currentUserId)
                                                    .set(savesMap);
                                            userId = coMeth.getUid();
                                            coMeth.getDb()
                                                    .collection("Users/" + userId + "/Subscriptions")
                                                    .document("saved_posts").collection("SavedPosts")
                                                    .document(postId)
                                                    .set(savesMap);
                                            showSaveSnack(holder, context.getString(R.string.added_to_saved_text));

                                        } else {

                                            //delete saved post
                                            coMeth.getDb()
                                                    .collection("Posts/" + postId + "/Saves")
                                                    .document(currentUserId)
                                                    .delete();
                                            coMeth.getDb()
                                                    .collection("Users/" + userId + "/Subscriptions")
                                                    .document("saved_posts").collection("SavedPosts")
                                                    .document(postId)
                                                    .delete();


                                        }
                                    } else {

                                        //save post task has failed
                                        Log.d(TAG, "onComplete: save post task failed\n" + task.getException());

                                    }

                                }
                            });
                } else {

                    if (!coMeth.isConnected()) {
                        //user is not connected to the internet
                        //show alert dialog
                        showSnack(holder, context.getString(R.string.internet_fail));
                    }
                    if (!coMeth.isLoggedIn()) {
                        goToLogin(context.getString(R.string.login_to_save_text));
                    }

                }

                //enable button
                holder.postSaveButton.setClickable(true);

            }
        });

        //share post
        //set onclick listener to the share button
        final String postTitle = getTittle(postId);
        holder.postSharePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Sharing post");
                //create post url
                String postUrl = context.getResources().getString(R.string.fursa_url_post_head) + postId;
                // TODO: 5/9/18 check anomaly in sharing posts title mixup

                String fullShareMsg = context.getString(R.string.app_name) + ":\n" +
                        postTitle + "\n" +
                        postUrl;
                Log.d(TAG, "postUrl is: " + postUrl);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
                shareIntent.putExtra(Intent.EXTRA_TEXT, fullShareMsg);
                context.startActivity(Intent.createChooser(shareIntent, "Share this post with"));

            }
        });

        //post image click action to start the ViewPost page
        holder.postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "post image is clicked");
                openPost(postId);
            }
        });

        //post title click action
        holder.titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "post title is clicked");
                openPost(postId);

            }
        });

        //post desc is clicked
        holder.descTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "post desc is clicked");
                openPost(postId);

            }
        });


        //count comments
        coMeth.getDb()
                .collection("Posts/" + postId + "/Comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        Log.d(TAG, "at onEvent, when likes change");
                        if (!queryDocumentSnapshots.isEmpty()) {

                            //post has likes
                            int numberOfComments = queryDocumentSnapshots.size();
                            holder.updateCommentsCount(numberOfComments);
                        } else {
                            //post has no likes
                            holder.updateCommentsCount(0);
                        }
                    }
                });

        //comment icon click action
        holder.postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "post image is clicked");
                Intent openPostIntent = new Intent(context, CommentsActivity.class);
                openPostIntent.putExtra("postId", postId);
                context.startActivity(openPostIntent);

            }
        });


        //set animation
        setAnimation(holder.itemView, position);

    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
//            Animation animation = AnimationUtils.loadAnimation(context, R.anim.fall_down);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    private String getTittle(String postId) {
        //get data from db
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        //check if exists
                        if (documentSnapshot.exists()) {

                            //post exists
                            Posts post = documentSnapshot.toObject(Posts.class);
                            postTitle = post.getTitle();
                            Log.d(TAG, "onSuccess: post title is " + postTitle);

                        } else {

                            //post does not exist

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get post");
                    }
                });
        Log.d(TAG, "getPostTitle: post title is " + postTitle);
        return postTitle;
    }

    private void openPost(String postId) {
        Intent openPostIntent = new Intent(context, ViewPostActivity.class);
        openPostIntent.putExtra("postId", postId);
        context.startActivity(openPostIntent);
    }

    private void showSnack(@NonNull ViewHolder holder, String message) {
        Snackbar.make(holder.mView.findViewById(R.id.postLayout),
                message, Snackbar.LENGTH_LONG).show();
    }

    private void showSaveSnack(@NonNull final ViewHolder holder, String message) {

        //check current class
        String homeFragmentText = "HomeFragment";
        String savedFragmentText = "SavedFragment";
        if (className.equals(homeFragmentText)) {

            //dont show the see list snackbar action 
            Snackbar.make(holder.mView.findViewById(R.id.postLayout),
                    message, Snackbar.LENGTH_SHORT)
                    .show();

        } else if (className.equals(savedFragmentText)) {

            //do nothing
            Log.d(TAG, "showSaveSnack: liked on saved fragment");

        } else {

            Snackbar.make(holder.mView.findViewById(R.id.postLayout),
                    message, Snackbar.LENGTH_LONG)
                    .setAction(context.getString(R.string.see_list_text), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent goToSavedIntent = new Intent(context, MainActivity.class);
                            goToSavedIntent.putExtra("action", "goto");
                            goToSavedIntent.putExtra("destination", "saved");
                            context.startActivity(goToSavedIntent);

                        }
                    })
                    .show();

        }
    }

    /**
     * takes user to the login screen
     *
     * @param message the message to display to the user on the login screen
     */
    private void goToLogin(String message) {
        Intent goToLoginIntent = new Intent(context, LoginActivity.class);
        goToLoginIntent.putExtra("message", message);
        context.startActivity(goToLoginIntent);
    }

    @Override
    public int getItemCount() {
        //the number of posts
        return postsList.size();
    }

    /**
     * go to the user profile page
     *
     * @param postUserId the user id of the current post
     */
    private void goToUserProfile(String postUserId) {
        Intent goToUserProfileIntent = new Intent(context, UserPageActivity.class);
        goToUserProfileIntent.putExtra("userId", postUserId);
        Log.d(TAG, "goToUserProfile: \nuserId is " + postUserId);
        context.startActivity(goToUserProfileIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.postUserImageCircleImageView:
                goToUserProfile(postUserId);
                break;
            case R.id.postUsernameTextView:
                goToUserProfile(postUserId);
                break;
            default:
                Log.d(TAG, "onClick: post recycker adapter on default");
        }
    }

    //implement the viewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {

        //initiate view
        public View mView;

        //initiate elements in the view holder
        private TextView titleTextView;
        private TextView descTextView;
        private ImageView postImageView;
        private TextView postUsernameTextView;
        private TextView postDateTextView;
        private CircleImageView postUserImageCircleView;


        //likes
        private ImageView postLikeButton;
        private TextView postLikesCount;
        private TextView postCommentCount;

        //saves
        private ImageView postSaveButton;

        //share
        private ImageView postSharePostButton;

        //comment
        private ImageView postCommentButton;

        //location
        private TextView postLocationTextView;

        //menu
        private ImageView postMenuButton;

        public ViewHolder(View itemView) {
            super(itemView);
            //use mView to populate other methods
            mView = itemView;

            postLikeButton = mView.findViewById(R.id.postLikeImageView);
            postLikesCount = mView.findViewById(R.id.postLikeCountText);

            postSaveButton = mView.findViewById(R.id.postSaveImageView);

            postSharePostButton = mView.findViewById(R.id.postShareImageView);

            postImageView = mView.findViewById(R.id.postImageView);

            postCommentButton = mView.findViewById(R.id.postCommetnImageView);
            postCommentCount = mView.findViewById(R.id.postCommentCountText);

            postLocationTextView = mView.findViewById(R.id.postLocationTextView);

            postMenuButton = mView.findViewById(R.id.postMenuImageView);

        }

        //retrieve the title
        public void setTitle(String title) {

            titleTextView = mView.findViewById(R.id.postTitleTextView);
            titleTextView.setText(title);
        }

        //retrieves description on post
        public void setDesc(String descText) {

            descTextView = mView.findViewById(R.id.postDescTextView);
            descTextView.setText(descText);
        }

        //retrieve the image
        public void setPostImage(String imageDownloadUrl, String thumbDownloadUrl) {

            postImageView = mView.findViewById(R.id.postImageView);


            if (imageDownloadUrl != null && thumbDownloadUrl != null) {

                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.appiconshadow);
                Glide.with(context)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(imageDownloadUrl)
                        .thumbnail(Glide.with(context).load(thumbDownloadUrl))
                        .into(postImageView);

            } else {

                //post has no image, hide imageView
                postImageView.setVisibility(View.GONE);

            }
        }



        //set post location
        public void setPostLocation(ArrayList locationArray) {

            if (locationArray != null) {

                postLocationTextView = mView.findViewById(R.id.postLocationTextView);
                String locationString = "";
                for (int i = 0; i < locationArray.size(); i++) {
                    locationString = locationString.concat(locationArray.get(i).toString() + "\n");
                }
                postLocationTextView.setText(locationString.trim());

            } else {
                Log.d(TAG, "location details are null");
                postLocationTextView.setVisibility(View.GONE);
            }

        }

        //set post date
        public void setPostDate(String date) {
            postDateTextView = mView.findViewById(R.id.postDateTextView);
            postDateTextView.setText(date);
        }

        //update the number of likes
        public void updateLikesCount(int likesCount) {

            postLikesCount = mView.findViewById(R.id.postLikeCountText);
            postLikesCount.setText(String.valueOf(likesCount));

        }

        // TODO: 4/7/18 use user thumb instead of user image

        public void updateCommentsCount(int numberOfComments) {

            postLikesCount = mView.findViewById(R.id.postCommentCountText);
            postLikesCount.setText(String.valueOf(numberOfComments));

        }

        public void setUserData(String userName, String userImageDownloadUri) {

            postUsernameTextView = mView.findViewById(R.id.postUsernameTextView);
            postUsernameTextView.setText(userName);

            postUserImageCircleView = mView.findViewById(R.id.postUserImageCircleImageView);
            //add the placeholder image
            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.ic_action_person_placeholder);
            Glide.with(context)
                    .applyDefaultRequestOptions(placeHolderOptions)
                    .load(userImageDownloadUri)
                    .into(postUserImageCircleView);

        }
    }
}
