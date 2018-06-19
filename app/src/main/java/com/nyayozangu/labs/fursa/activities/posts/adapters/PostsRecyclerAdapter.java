package com.nyayozangu.labs.fursa.activities.posts.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.comments.CommentsActivity;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.ViewPostActivity;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Sean on 4/4/18.
 * <p>
 * Adapter class for the Recycler view
 */

public class PostsRecyclerAdapter extends RecyclerView.Adapter<PostsRecyclerAdapter.ViewHolder> {

    // TODO: 6/14/18 add an impressions field for posts viewed on feed

    private static final String TAG = "Sean";
    private static final String HOME_FRAGMENT = "HomeFragment";
    private static final String VIEW_CAT_ACTIVITY = "ViewCategoryActivity";
    private static final String SAVED_FRAGMENT = "SavedFragment";

    //member variables for storing posts
    public List<Posts> postsList;
    public List<Users> usersList;
    public Context context;
    //memory optimization
    public RequestManager glide;

    public static Posts post;

    private int lastPosition = -1;
    private String userId;
    private String className;
    private ProgressDialog progressDialog;
    private CoMeth coMeth = new CoMeth();

    private String postUserId;

    private ArrayList<ViewHolder> holders = new ArrayList<>();

    //empty constructor for receiving the posts
    public PostsRecyclerAdapter(List<Posts> postsList,
                                List<Users> usersList,
                                String className,
                                RequestManager glide) {

        //store received posts
        this.postsList = postsList;
        this.usersList = usersList;
        this.className = className;
        //mem opt
        this.glide = glide;
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

        Log.d(TAG, "onBindViewHolder: normal post");
        loadPostContent(holder, position);
    }


    @Override
    public int getItemCount() {
        //the number of posts
        return postsList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull final ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Log.d(TAG, "onViewDetachedFromWindow: starting timer to delete view holder images");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: 10s are up deleting old images");
                glide.clear(holder.postImageView);
            }
        }, 30000);
    }

    static void updateFeedViews(Posts post) {
        Log.d(TAG, "updateFeedViews: ");
        Map<String, Object> feedViewMap = new HashMap<>();
        int feedViews = post.getFeed_views() + 1;
        feedViewMap.put("feed_views", feedViews);
        FirebaseFirestore.getInstance()
                .collection(CoMeth.POSTS)
                .document(post.PostId)
                .update(feedViewMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: feed views updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update feed views\n" +
                                e.getMessage());
                    }
                });
    }

    private void loadPostContent(@NonNull final ViewHolder holder, int position) {
        //        holder.setIsRecyclable(false);

        Log.d(TAG, "loadPostContent: position is " + position);
        Random random = new Random();
        int randNum = random.nextInt((((position + 20) - position) + 1)) + position;
        if (position == randNum &&
                (className.equals(HOME_FRAGMENT) ||
                        className.equals(VIEW_CAT_ACTIVITY))) {
            Log.d(TAG, "loadPostContent: rand num = pos, showing ad");
            holder.adCard.setVisibility(View.VISIBLE);
            holder.setAd();
        } else {
            holder.adCard.setVisibility(View.GONE);
        }

        //get post
        post = postsList.get(position);
        Users user = usersList.get(position);
//        postTitle = postsList.get(position).getTitle();
        String postTitle1 = post.getTitle();
        holder.setTitle(postTitle1);

        final String descData = postsList.get(position).getDesc();
        holder.setDesc(descData);

        //set location
//        ArrayList locationArray = postsList.get(position).getLocation();
        ArrayList locationArray = post.getLocation();
        holder.setPostLocation(locationArray);

        final String currentUserId;

        //handling getting the user who clicked like
        currentUserId = coMeth.getUid();


        //update feed_views
        new UpdatePostActivityTask().execute();

        //handle post image
        final String imageUrl = postsList.get(position).getImage_url();
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
        holder.postUserImageCircleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPost(postId);
            }
        });
        holder.postUsernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPost(postId);
            }
        });

        //handle date for posts
        if (postsList.get(position).getTimestamp() != null) {

            long millis = postsList.get(position).getTimestamp().getTime();
            String dateString = coMeth.processPostDate(millis);
            holder.setPostDate(dateString);

        }

        //get likes count
//        holder.updateLikesCount(post.getLikes());
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


        //determine likes by current user
        if (coMeth.isConnected() && coMeth.isLoggedIn()) {

            //check post likes
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
                                holder.postLikeButton.setImageDrawable(
                                        context.getResources().getDrawable(R.drawable.ic_action_liked));

                                //set click action for like
                                holder.postLikeButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //when like is clicked and user has already liked, delete existing like
                                        unlikePost(postId, currentUserId);
                                    }
                                });
                                holder.postLikesCount.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        unlikePost(postId, currentUserId);
                                    }
                                });
                            } else {
                                //current user has not liked the post
                                holder.postLikeButton.setImageDrawable(
                                        context.getResources().getDrawable(R.drawable.ic_action_like_unclicked));
                                //set like click action
                                holder.postLikeButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        // when like button is clicked and user has not liked post,
                                        // add like to post
                                        likePost(postId, currentUserId);
                                    }
                                });
                                holder.postLikesCount.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        likePost(postId, currentUserId);
                                    }
                                });
                            }
                        }
                    });

            //check post saves
            coMeth.getDb()
                    .collection("Posts/" + postId + "/Saves")
                    .document(currentUserId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot,
                                            FirebaseFirestoreException e) {
                            //update the save button real time
                            if (documentSnapshot.exists()) {
                                Log.d(TAG, "at get saves, updating saves realtime");
                                //user has saved post
                                holder.postSaveButton.setImageDrawable(
                                        context.getResources().getDrawable(R.drawable.ic_action_bookmarked));
                                //set un-save click action
                                holder.postSaveButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //delete saved post
                                        unsavePost(postId, currentUserId);
                                    }
                                });
                                holder.postSaveText.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        unsavePost(postId, currentUserId);
                                    }
                                });
                            } else {
                                //user has not liked post
                                holder.postSaveButton.setImageDrawable(
                                        context.getResources().getDrawable(R.drawable.ic_action_bookmark_outline));
                                //set save action when user has not saved post
                                holder.postSaveButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        savePost(postId, currentUserId, holder);
                                    }
                                });
                                holder.postSaveText.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        savePost(postId, currentUserId, holder);
                                    }
                                });
                            }
                        }
                    });

        } else {
            if (!coMeth.isConnected()) {
                holder.postSaveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSnack(holder, context.getResources().getString(R.string.failed_to_connect_text));
                    }
                });
                holder.postSaveText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSnack(holder, context.getResources().getString(R.string.failed_to_connect_text));
                    }
                });
                holder.postLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSnack(holder, context.getResources().getString(R.string.failed_to_connect_text));
                    }
                });
                holder.postLikesCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSnack(holder, context.getResources().getString(R.string.failed_to_connect_text));
                    }
                });
            }
            if (!coMeth.isLoggedIn()) {
                holder.postSaveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToLogin(context.getResources().getString(R.string.login_to_save_text));
                    }
                });
                holder.postLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToLogin(context.getResources().getString(R.string.login_to_like));
                    }
                });
            }
        }

        //share post
        //set onclick listener to the share button
        final String postTitle = postsList.get(position).getTitle() /*getPostTitle(postId)*/;
        holder.postSharePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Sharing post");
                sharePost(postId, postTitle, descData, imageUrl, holder);
            }
        });
        holder.postShareText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Sharing post");
                sharePost(postId, postTitle, descData, imageUrl, holder);
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
//        holder.updateCommentsCount(post.getComments());
        //use snapshot listener so update the counts realtime
        coMeth.getDb()
                .collection("Posts/" + postId + "/Comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots,
                                        FirebaseFirestoreException e) {
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

                Log.d(TAG, "post comment is clicked");
                openComments(postId);
            }
        });
        holder.postCommentCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "post comment count is clicked");
                openComments(postId);
            }
        });


        //set animation
//        setAnimation(holder.itemView, position);
    }

    private void openComments(String postId) {
        Intent openPostIntent = new Intent(context, CommentsActivity.class);
        openPostIntent.putExtra("postId", postId);
        context.startActivity(openPostIntent);
    }

    private void likePost(String postId, String currentUserId) {
        Map<String, Object> likesMap = new HashMap<>();
        likesMap.put(CoMeth.TIMESTAMP, FieldValue.serverTimestamp());
        coMeth.getDb()
                .collection(CoMeth.POSTS + "/" + postId + "/" + CoMeth.LIKES_COL)
                .document(currentUserId)
                .set(likesMap);
    }

    private void unlikePost(String postId, String currentUserId) {
        coMeth.getDb()
                .collection(CoMeth.POSTS + "/" + postId + "/" + CoMeth.LIKES_COL)
                .document(currentUserId)
                .delete();
    }

    private void savePost(String postId, String currentUserId, @NonNull ViewHolder holder) {
        Map<String, Object> savesMap = new HashMap<>();
        savesMap.put(CoMeth.TIMESTAMP, FieldValue.serverTimestamp());
        //save new post
        coMeth.getDb()
                .collection(CoMeth.POSTS + "/" + postId + "/" + CoMeth.SAVES)
                .document(currentUserId)
                .set(savesMap);
        userId = coMeth.getUid();
        coMeth.getDb()
                .collection(CoMeth.USERS + "/" + userId + "/" + CoMeth.SUBSCRIPTIONS)
                .document(CoMeth.SAVED_POSTS_DOC).collection(CoMeth.SAVED_POSTS)
                .document(postId)
                .set(savesMap);
        showSaveSnack(holder, context.getString(R.string.added_to_saved_text));
    }

    private void unsavePost(String postId, String currentUserId) {
        coMeth.getDb()
                .collection(CoMeth.POSTS + "/" + postId + "/" + CoMeth.SAVES)
                .document(currentUserId)
                .delete();
        coMeth.getDb()
                .collection(CoMeth.USERS + "/" + userId + "/" + CoMeth.SUBSCRIPTIONS)
                .document(CoMeth.SAVED_POSTS_DOC).collection(CoMeth.SAVED_POSTS)
                .document(postId)
                .delete();
    }

    private void sharePost(String postId,
                           String postTitle,
                           String postDesc,
                           String imageUrl,
                           @NonNull ViewHolder holder) {
        showProgress(context.getString(R.string.loading_text));
        //create post url
        String postUrl = context.getResources().getString(R.string.fursa_url_post_head) + postId;
        shareDynamicLink(postUrl, postTitle, postDesc, imageUrl, holder);
    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    /**
     * share the dynamic link
     *
     * @param holder       the view holder containing the post
     * @param postTitle    the post title
     * @param postDesc     the post description
     * @param postImageUrl the post image url
     */
    private void shareDynamicLink(String postUrl,
                                  final String postTitle,
                                  final String postDesc,
                                  final String postImageUrl,
                                  final ViewHolder holder) {
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(postUrl))
                .setDynamicLinkDomain(context.getString(R.string.dynamic_link_domain))
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(coMeth.minVerCode)
                        .setFallbackUrl(Uri.parse(context.getString(R.string.playstore_url)))
                        .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(postTitle)
                                .setDescription(postDesc)
                                .setImageUrl(Uri.parse(getImageUrl(postImageUrl)))
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Log.d(TAG, "onComplete: short link is: " + shortLink);

                            //show share dialog
                            String fullShareMsg = postTitle + "\n" +
                                    shortLink;
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
                            shareIntent.putExtra(Intent.EXTRA_TEXT, fullShareMsg);
                            coMeth.stopLoading(progressDialog);
                            context.startActivity(Intent.createChooser(shareIntent,
                                    context.getResources().getString(R.string.share_with_text)));
                        } else {
                            Log.d(TAG, "onComplete: \ncreating short link task failed\n" +
                                    task.getException());
                            coMeth.stopLoading(progressDialog);
                            showSnack(holder, context.getString(R.string.failed_to_share_text));
                        }
                    }
                });
    }

    /**
     * checks if post has an image
     *
     * @return String post image download url
     * if the post has an image
     * or the default app icon download url if the post has no image
     */
    private String getImageUrl(String postImageUrl) {
        if (postImageUrl != null) {
            return postImageUrl;
        } else {
            return context.getString(R.string.app_icon_url);
        }
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
        if (className.equals(HOME_FRAGMENT)) {
            //dont show the see list snackbar action 
            Snackbar.make(holder.mView.findViewById(R.id.postLayout),
                    message, Snackbar.LENGTH_SHORT)
                    .show();

        } else if (className.equals(SAVED_FRAGMENT)) {
            //do nothing
            Log.d(TAG, "showSaveSnack: liked on saved fragment");
        } else {

            Snackbar.make(holder.mView.findViewById(R.id.postLayout),
                    message, Snackbar.LENGTH_LONG)
                    .setAction(context.getString(R.string.see_list_text), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent goToSavedIntent = new Intent(context, MainActivity.class);
                            goToSavedIntent.putExtra(CoMeth.ACTION, CoMeth.GOTO);
                            goToSavedIntent.putExtra(CoMeth.DESTINATION, CoMeth.SAVED_VAL);
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


    //implement the viewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {

        //initiate view
        View mView;

        //initiate elements in the view holder
        private TextView postUsernameTextView;
        private CircleImageView postUserImageCircleView;
        private TextView postLikesCount, postCommentCount,
                postLocationTextView, postDateTextView, descTextView, titleTextView,
                postSaveText, postShareText;
        private ImageView postSaveButton, postSharePostButton, postCommentButton,
                postImageView, postLikeButton;
        private CardView adCard;

        ViewHolder(View itemView) {
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
            postSaveText = mView.findViewById(R.id.postSaveTextTextView);
            postShareText = mView.findViewById(R.id.postShareTextTextView);
            titleTextView = mView.findViewById(R.id.postTitleTextView);
            descTextView = mView.findViewById(R.id.postDescTextView);

            adCard = mView.findViewById(R.id.adCard);
        }

        //retrieve the title
        public void setTitle(String title) {
            titleTextView.setText(title);
        }

        //retrieves description on post
        void setDesc(String descText) {
            descTextView.setText(descText);
        }

        //retrieve the image
        void setPostImage(final String imageDownloadUrl, final String thumbDownloadUrl) {

            if (imageDownloadUrl != null && thumbDownloadUrl != null) {

                postImageView.setVisibility(View.VISIBLE);
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.color.colorWhite);
                glide.applyDefaultRequestOptions(requestOptions)
                        .load(imageDownloadUrl)
                        .transition(withCrossFade())
                        .thumbnail(Glide.with(context).load(thumbDownloadUrl))
                        .into(postImageView);
            } else {
                //post has no image, hide imageView
                postImageView.setVisibility(View.GONE);
            }
        }

        //set post location
        void setPostLocation(ArrayList locationArray) {

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
        void setPostDate(String date) {
            postDateTextView = mView.findViewById(R.id.postDateTextView);
            postDateTextView.setText(date);
        }

        //update the number of likes
        public void updateLikesCount(int likesCount) {
            postLikesCount = mView.findViewById(R.id.postLikeCountText);
            postLikesCount.setText(String.valueOf(likesCount));
        }

        public void updateCommentsCount(int numberOfComments) {
            postLikesCount = mView.findViewById(R.id.postCommentCountText);
            postLikesCount.setText(String.valueOf(numberOfComments));
        }

        public void setUserData(String userName, String userImageDownloadUri) {

            postUsernameTextView = mView.findViewById(R.id.postUsernameTextView);
            postUsernameTextView.setText(userName);
            postUserImageCircleView = mView.findViewById(R.id.postUserImageCircleImageView);
            //add the placeholder image
            try {
                RequestOptions placeHolderOptions = new RequestOptions();
                placeHolderOptions.placeholder(R.drawable.ic_action_person_placeholder);
                glide
                        .applyDefaultRequestOptions(placeHolderOptions)
                        .load(userImageDownloadUri)
                        .into(postUserImageCircleView);
            } catch (Exception e) {
                Log.d(TAG, "setUserData: error " + e.getMessage());
            }
        }

        //set ad
        public void setAd() {
            Log.d(TAG, "setAd: ");
            AdView adView = mView.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
//                    .addTestDevice("CD3E657857E4EEBE754743B250DCAB5E")
                    .build();
            adView.loadAd(adRequest);
        }
    }

    public static class UpdatePostActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: post is " + post.getTitle());
            updateFeedViews(post);
            return null;
        }
    }
}
