package com.nyayozangu.labs.fursa;

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
            final String finalCurrentUserId = currentUserId;
            db.collection("Posts/" + postId + "/Likes").document(finalCurrentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
            db.collection("Posts/" + postId + "/Saves").document(finalCurrentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                                    //db.collection("Posts").document(postId).collection("Likes");
                                    //can alternatively ne written
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
                        //notify user

                        String message = "Log in to like items";
                        showLoginAlertDialog(message);

                    }
                } else {

                    //alert user is not connected
                    showSnack(holder, "Failed to connect to the internet\nCheck your connection and try again");

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
                                    //notify user that post has been saved
                                    showSnack(holder, "Added to saved items");
                                } else {
                                    //delete saved post
                                    db.collection("Posts/" + postId + "/Saves").document(currentUserId).delete();
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
                    showSnack(holder, "Failed to connect to the internet\nCheck your connection and try again");


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

        //clicking the post image to start the ViewPost page
        holder.postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "post image is clicked");
                Intent openPostIntent = new Intent(context, ViewPostActivity.class);
                openPostIntent.putExtra("postId", postId);
                context.startActivity(openPostIntent);
                /*((Activity)context).finish();*/
            }
        });


        //create query to count comments
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

        //clicking the comment icon to go to the comment page of post
        holder.postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "post image is clicked");
                Intent openPostIntent = new Intent(context, CommentsActivity.class);
                openPostIntent.putExtra("postId", postId);
                context.startActivity(openPostIntent);
            }
        });


        //open menu on clicking post menu
        holder.postMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                db.collection("Posts/").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

                        }

                    }
                });


            }
        });


    }

    private void openPostMenu(final String postId, final String currentUserId, final String postUserId) {


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


    }

    private String[] getPostMenuItems(String currentUserId, String postUserId) {

        if (isConnected()) {

            if (isLoggedIn()) {


                //get user id for post


                if (currentUserId.equals(postUserId)) {

                    //menu items
                    String[] menuItems = new String[]{

                            "Edit",
                            "Report"

                    };
                    return menuItems;
                } else {

                    //menu items
                    String[] menuItems = new String[]{

                            "Report",

                    };
                    return menuItems;

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
        loginAlertBuilder.setTitle("Login")
                .setIcon(context.getDrawable(R.drawable.ic_action_alert))
                .setMessage("You are not logged in\n" + message)
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send user to login activity
                        goToLogin();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

            RequestOptions requestOptions = new RequestOptions();
            // TODO: 4/5/18 replace postImage placeholder image
            requestOptions.placeholder(R.drawable.ic_action_image_placeholder);

            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(imageDownloadUrl)
                    .thumbnail(Glide.with(context).load(thumbDownloadUrl))
                    .into(postImageView);
        }

        // set post username
        public void setPostUserName(String userName) {

            postUserNameTextView = mView.findViewById(R.id.postUsernameTextView);
            postUserNameTextView.setText(userName);

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
        //set userImage
        public void setUserImage(String userImageDownloadUri) {

            postUserImageCircleView = mView.findViewById(R.id.postUserImageCircleImageView);
            //add the placeholder image
            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.ic_action_image_placeholder);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOptions).load(userImageDownloadUri).into(postUserImageCircleView);
        }

        public void updateCommentsCount(int numberOfComments) {

            postLikesCount = mView.findViewById(R.id.postCommentCountText);
            postLikesCount.setText(String.valueOf(numberOfComments));

        }
    }
}
