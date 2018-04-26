package com.nyayozangu.labs.fursa.activities.posts.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.comments.CommentsActivity;
import com.nyayozangu.labs.fursa.activities.posts.CreatePostActivity;
import com.nyayozangu.labs.fursa.activities.posts.ViewPostActivity;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.users.Users;

import java.sql.Date;
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

public class PostsRecyclerAdapter extends RecyclerView.Adapter<PostsRecyclerAdapter.ViewHolder> {


    private static final String TAG = "Sean";
    //member variables for storing posts
    public List<Posts> postsList;
    public List<Users> usersList;

    public Context context;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    //empty constructor for receiving the posts
    public PostsRecyclerAdapter(List<Posts> postsList, List<Users> usersList) {

        //store received posts
        this.postsList = postsList;
        this.usersList = usersList;
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

        //get title from holder
        String titleData = postsList.get(position).getTitle();
        //set the post title
        holder.setTitle(titleData);

        //set the data after the viewHolder has been bound
        String descData = postsList.get(position).getDesc();
        //set the description to the view holder
        holder.setDesc(descData);

        //set location
        ArrayList locationArray = postsList.get(position).getLocation();
        holder.setPostLocation(locationArray);


        final String currentUserId;

        //handling getting the user who clicked like
        if (isLoggedIn()) {
            currentUserId = mAuth.getCurrentUser().getUid();
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

        //handle name and image
        String userName = usersList.get(position).getName();
        String userImageDownloadUri = usersList.get(position).getImage();
        Log.d(TAG, "onBindViewHolder: \nname is: " + userName + "\nuser image is: " + userImageDownloadUri);

        holder.setUserData(userName, userImageDownloadUri);

        //handle date for posts
        // TODO: 4/5/18 looking into the timestamp bug; app crashes on creating new post
        try {
            long millis = postsList.get(position).getTimestamp().getTime();
            //convert millis to date time format
            String dateString = DateFormat.format("EEE, MMM d, ''yy - h:mm a", new Date(millis)).toString();
            holder.setPostDate(dateString);
        } catch (NullPointerException nullException) {
            Log.d(TAG, "error: " + nullException);
        }


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
            db.collection("Posts/" + postId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    //update the like button real time
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "at get likes, updating likes real time");
                        //user has liked
                        holder.postLikeButton.setImageDrawable(context.getDrawable(R.drawable.ic_action_like_accent));
                    } else {
                        //current user has not liked the post
                        holder.postLikeButton.setImageDrawable(context.getDrawable(R.drawable.ic_action_like_unclicked));
                    }
                }
            });

            //get saves
            db.collection("Posts/" + postId + "/Saves").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                if (isConnected()) {

                    if (isLoggedIn()) {

                        db.collection("Posts/" + postId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                //get data from teh likes collection

                                //check if current user has already liked post
                                if (!task.getResult().exists()) {
                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());

                                    db.collection("Posts/" + postId + "/Likes").document(currentUserId).set(likesMap);

                                } else {
                                    //delete the like
                                    db.collection("Posts/" + postId + "/Likes").document(currentUserId).delete();
                                }
                            }
                        });

                    } else {
                        //user is not logged in
                        Log.d(TAG, "use is not logged in");

                        String message = context.getString(R.string.login_to_like);
                        showLoginAlertDialog(message);

                    }
                } else {

                    //alert user is not connected
                    showSnack(holder, context.getString(R.string.failed_to_connect_text));

                }

                //enable button
                holder.postLikeButton.setClickable(true);

            }
        });


        //set a click listener to the save button
        holder.postSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //disable button
                holder.postSaveButton.setClickable(false);

                //check if user is connected to the internet
                if (isConnected()) {

                    //check if user is logged in
                    if (isLoggedIn()) {

                        db.collection("Posts/" + postId + "/Saves").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                //get data from the saves collections

                                //check if user has already saved the post
                                if (!task.getResult().exists()) {

                                    Map<String, Object> savesMap = new HashMap<>();
                                    savesMap.put("timestamp", FieldValue.serverTimestamp());
                                    //save new post
                                    db.collection("Posts/" + postId + "/Saves").document(currentUserId).set(savesMap);
                                    userId = mAuth.getCurrentUser().getUid();
                                    db.collection("Users/" + userId + "/Subscriptions").document("saved_posts").collection("SavedPosts").document(postId).set(savesMap);
                                    //notify user that post has been saved
                                    showSnack(holder, context.getString(R.string.added_to_saved_text));

                                } else {

                                    //delete saved post
                                    db.collection("Posts/" + postId + "/Saves").document(currentUserId).delete();
                                    db.collection("Users/" + userId + "/Subscriptions").document("saved_posts").collection("SavedPosts").document(postId).delete();


                                }
                            }
                        });
                    } else {

                        //user is not logged in
                        Log.d(TAG, "user is not logged in");
                        //notify user
                        String message = "Log in to save items";
                        showLoginAlertDialog(message);


                    }
                } else {

                    //user is not connected to the internet
                    //show alert dialog
                    showSnack(holder, context.getString(R.string.internet_fail));

                }

                //enable button
                holder.postSaveButton.setClickable(true);

            }
        });

        //share post
        //set onclick listener to the share button
        holder.postSharePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Sharing post");
                //create post url
                String postUrl = context.getResources().getString(R.string.fursa_url_head) + postId;
                Log.d(TAG, "postUrl is: " + postUrl);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
                shareIntent.putExtra(Intent.EXTRA_TEXT, postUrl);
                context.startActivity(Intent.createChooser(shareIntent, "Share this post with"));

            }
        });

        //post image click action to start the ViewPost page
        holder.postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "post image is clicked");
                openPost(postId);
                /*((Activity)context).finish();*/
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
        db.collection("Posts/" + postId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
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


        //post menu click action
        holder.postMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                db.collection("Posts").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        //check if task is successful
                        if (task.isSuccessful()) {

                            //get postUserId
                            String postUserId = task.getResult().get("user_id").toString();
                            //open menu
                            openPostMenu(postId, currentUserId, postUserId);

                        } else {

                            //task failed
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());

                        }

                    }
                });


            }
        });


    }

    private void openPost(String postId) {
        Intent openPostIntent = new Intent(context, ViewPostActivity.class);
        openPostIntent.putExtra("postId", postId);
        context.startActivity(openPostIntent);
    }

    private void openPostMenu(final String postId, final String currentUserId, final String postUserId) {

        if (isConnected()) {

            if (isLoggedIn()) {

                //normal menu
                AlertDialog.Builder postMenuBuilder = new AlertDialog.Builder(context);
                postMenuBuilder.setItems(getPostMenuItems(currentUserId, postUserId), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //open the feedback page
                        switch (getPostMenuItems(currentUserId, postUserId)[which].toLowerCase()) {

                            case "report":
                                //open report page
                                Toast.makeText(context, "Report", Toast.LENGTH_SHORT).show();
                                break;
                            case "edit":
                                //open edit post
                                Intent editIntent = new Intent(context, CreatePostActivity.class);
                                editIntent.putExtra("editPost", postId);
                                context.startActivity(editIntent);
                                break;

                        }


                    }
                })
                        .show();

            } else {

                //user is not logged in
                showLoginAlertDialog(context.getString(R.string.login_to_view_options_text));

            }

        } else {

            // TODO: 4/24/18 notify user failed to connect
            /*showSnack (holder, "Failed to connect to the internet");*/

        }

    }

    private String[] getPostMenuItems(String currentUserId, String postUserId) {

        if (isConnected()) {

            if (isLoggedIn()) {

                if (currentUserId.equals(postUserId)) {

                    //menu items
                    return new String[]{

                            context.getString(R.string.edit_text),
                            context.getString(R.string.report_text),

                    };
                } else {

                    //menu items
                    return new String[]{

                            context.getString(R.string.report_text)

                    };

                }

            }

        }

        return new String[0];
    }

    private void showSnack(@NonNull ViewHolder holder, String message) {
        Snackbar.make(holder.mView.findViewById(R.id.postLayout),
                message, Snackbar.LENGTH_LONG).show();
    }

    private void goToLogin() {
        //take user to the login page
        Log.d(TAG, "at goToLogin");
        context.startActivity(new Intent(context, LoginActivity.class));

    }

    private boolean isLoggedIn() {
        //determine if user is logged in
        return mAuth.getCurrentUser() != null;
    }

    private boolean isConnected() {

        //check if there's a connection
        Log.d(TAG, "at isConnected");
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {

            activeNetwork = cm.getActiveNetworkInfo();

        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }

    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(context);
        loginAlertBuilder.setTitle(context.getString(R.string.login_text))
                .setIcon(context.getDrawable(R.drawable.ic_action_alert))
                .setMessage(context.getString(R.string.not_logged_in_text) + message)
                .setPositiveButton(context.getString(R.string.login_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send user to login activity
                        goToLogin();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        //the number of posts
        return postsList.size();
    }

    //implement the viewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {

        //initiate view
        View mView;

        //initiate elements in the view holder
        private TextView titleTextView;
        private TextView descTextView;
        private ImageView postImageView;
        private TextView postUserNameTextView;
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
                requestOptions.placeholder(R.drawable.ic_action_image_placeholder);
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

            // TODO: 4/4/18 show different time status  like one minute ago ++

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

            postUserNameTextView = mView.findViewById(R.id.postUsernameTextView);
            postUserNameTextView.setText(userName);

            postUserImageCircleView = mView.findViewById(R.id.postUserImageCircleImageView);
            //add the placeholder image
            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.ic_action_person_placeholder);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOptions).load(userImageDownloadUri).into(postUserImageCircleView);

        }
    }
}
