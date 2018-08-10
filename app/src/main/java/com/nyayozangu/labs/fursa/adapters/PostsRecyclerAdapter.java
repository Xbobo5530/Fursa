package com.nyayozangu.labs.fursa.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.CommentsActivity;
import com.nyayozangu.labs.fursa.activities.UserPageActivity;
import com.nyayozangu.labs.fursa.activities.UserPostsActivity;
import com.nyayozangu.labs.fursa.activities.ViewPostActivity;
import com.nyayozangu.labs.fursa.helpers.Notify;
import com.nyayozangu.labs.fursa.models.Post;
import com.nyayozangu.labs.fursa.activities.LoginActivity;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.COMMENTS_COLL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.DESTINATION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWERS_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWING;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.LIKES_COL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NEW_FOLLOWERS_UPDATE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POST_ID;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POST_TYPE_AD;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POST_TYPE_POST;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POST_TYPE_SPONSORED;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SAVED_COL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SAVED_POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SAVED_POSTS_DOC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SAVED_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SAVES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SOURCE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SUBSCRIPTIONS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TIMESTAMP;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.CLASS_NAME_VIEW_POST;

/**
 * Created by Sean on 4/4/18.
 * <p>
 * Adapter class for the Recycler view
 */

public class PostsRecyclerAdapter extends RecyclerView.Adapter {
    

    private static final String TAG = "PostsRecyclerAdapter";
    private static final int VIEW_TYPE_POST = 0;
    private static final int VIEW_TYPE_AD = 1;
    private static final String VIEW_CAT_ACTIVITY = "ViewCategoryActivity";
    private static final String USER_POST_ACTIVITY = "UserPostsActivity";
    private static final String RECENT_FRAGMENT = "RecentFragment";
    private static final String POPULAR_FRAGMENT = "PopularFragment";

    //member variables for storing posts
    private List<Post> postsList;
    private List<User> usersList;
    public Context context;
    public Activity activity;
    public RequestManager glide;
    public static Post post;
    private String currentUserId;
    private String className;
    private ProgressDialog progressDialog;
    private CoMeth coMeth = new CoMeth();

    public PostsRecyclerAdapter(List<Post> postsList, List<User> usersList, String className,
                                RequestManager glide, Activity activity) {
        this.postsList = postsList;
        this.usersList = usersList;
        this.className = className;
        this.glide = glide;
        this.activity = activity;
        Log.d(TAG, "PostsRecyclerAdapter: ");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType){
            case VIEW_TYPE_POST:
                View postView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post_list_item, parent, false);
                context = parent.getContext();
                return new PostViewHolder(postView);
            case VIEW_TYPE_AD:
                View adView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ad_list_item, parent, false);
                context = parent.getContext();
                return new AdViewHolder(adView);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        post = postsList.get(position);
        switch (holder.getItemViewType()){
            case VIEW_TYPE_POST:
                ((PostViewHolder)holder).build(position, className, post.getPost_type());
                break;
            case VIEW_TYPE_AD:
                ((AdViewHolder)holder).build();
                break;
        }
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {

        post = postsList.get(position);
        int postType = post.getPost_type();
        switch (postType){
            case POST_TYPE_POST:
            case POST_TYPE_SPONSORED:
                return VIEW_TYPE_POST;
            case POST_TYPE_AD:
                return VIEW_TYPE_AD;
            default:
                Log.w(TAG, "getItemViewType: post type error\npost type is: " + postType);
                return 0; //default stat
        }
    }

    static void updateFeedViews(Post post) {
        try {
            Map<String, Object> feedViewMap = new HashMap<>();
            int feedViews = post.getFeed_views() + 1;
            feedViewMap.put("feed_views", feedViews);
            FirebaseFirestore.getInstance().collection(POSTS)
                    .document(post.PostId)
                    .update(feedViewMap)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: failed to update feed views\n" +
                                    e.getMessage());
                        }
                    });
        }catch (NullPointerException nullPathException){
            Log.w(TAG, "updateFeedViews: the provided path is null\n" +
                    nullPathException.getMessage(), nullPathException);
        }
    }


    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.getItemViewType() == VIEW_TYPE_POST){
            glide.clear(((PostViewHolder)holder).postImageView);
        }
    }

    private void setDescription(@NonNull PostViewHolder holder, String title, String desc) {
        String description = "";
        if (title != null && !title.isEmpty()){
            description = description.concat(title);
            if (desc != null && !desc.isEmpty()){
                description = description.concat("\n" + desc);
                holder.descTextView.setText(description);
                holder.descTextView.setVisibility(View.VISIBLE);
            }
        }else {
            if (desc != null && !desc.isEmpty()) {
                description = description.concat(desc);
                holder.descTextView.setText(description);
                holder.descTextView.setVisibility(View.VISIBLE);
            }else {
                holder.descTextView.setVisibility(View.GONE);
            }
        }
    }

    private void handleSharePost(final Post post, @NonNull final PostViewHolder holder, final String description) {
        holder.shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharePost(holder, post, description);
            }
        });
    }

    private void handlePostCommentsCount(@NonNull final PostViewHolder holder, String postId) {
        coMeth.getDb().collection(POSTS + "/" + postId + "/" + COMMENTS_COLL)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots,
                                        FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            int numberOfComments = queryDocumentSnapshots.size();
                            holder.postCommentCount.setVisibility(View.VISIBLE);
                            holder.postCommentCount.setText(String.valueOf(numberOfComments));
                        } else {
                            holder.postCommentCount.setText(context.getResources().getString(R.string.comment_text));
                        }
                    }
                });
    }

    private void handlePostActionsLoginException(@NonNull PostViewHolder holder) {
        if (!coMeth.isLoggedIn()) {
            holder.saveLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToLogin(context.getResources().getString(R.string.login_to_save_text));
                }
            });
            holder.likeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToLogin(context.getResources().getString(R.string.login_to_like));
                }
            });
        }
    }

    private void handlePostActionsConnectionException(@NonNull final PostViewHolder holder) {
        if (!coMeth.isConnected(context)) {
            holder.saveLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSnack(context.getResources().getString(R.string.failed_to_connect_text), holder);
                }
            });
            holder.likeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSnack(context.getResources().getString(R.string.failed_to_connect_text), holder);
                }
            });
        }
    }

    private void handlePostSaves(@NonNull final PostViewHolder holder, final String postId) {
        coMeth.getDb().collection(POSTS + "/" + postId + "/" + SAVED_COL)
                .document(currentUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot,
                                        FirebaseFirestoreException e) {
                        if (e == null) {
                            if (documentSnapshot.exists()) {
                                holder.postSaveButton.setImageDrawable(
                                        context.getResources().getDrawable(R.drawable.ic_bookmarked));
                                holder.saveLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        unSavePost(postId);
                                    }
                                });
                            } else {
                                holder.postSaveButton.setImageDrawable(
                                        context.getResources().getDrawable(R.drawable.ic_action_bookmark_outline));
                                holder.saveLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        savePost(holder, postId);
                                    }
                                });
                            }
                        } else {
                            Log.d(TAG, "onEvent: error handling saved posts " + e.getMessage());
                        }
                    }
                });
    }

    private void handlePostLikes(@NonNull final PostViewHolder holder, final String postId, CollectionReference likesRef) {
        likesRef.document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot,
                                        FirebaseFirestoreException e) {

                        if (e == null) {
                            //update the like button real time
                            if (documentSnapshot.exists()) {
                                //user has liked
                                holder.postLikeButton.setImageDrawable(
                                        context.getResources().getDrawable(R.drawable.ic_action_liked));
                                //set click action for like
                                holder.likeLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        unlikePost(postId);
                                    }
                                });
                            } else {
                                holder.postLikeButton.setImageDrawable(
                                        context.getResources().getDrawable(R.drawable.ic_action_like_unclicked));
                                //set like click action
                                holder.likeLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        likePost(postId);
                                    }
                                });
                            }
                        } else {
                            Log.d(TAG, "onEvent: error in handling likes " + e.getMessage());
                        }
                    }
                });
    }

    private void handleLikesCount(@NonNull final PostViewHolder holder, CollectionReference likesRef) {
        likesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if (e == null) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                int numberOfLikes = queryDocumentSnapshots.size();
                                holder.postLikesCount.setText(String.valueOf(numberOfLikes));
                            } else {
                                //post has no likes
                                holder.postLikesCount.setText(context.getResources().getString(R.string.like_text));
                            }
                        } else {
                            Log.d(TAG, "onEvent: error " + e.getMessage());
                        }
                    }
                });
    }

    private void handlePostDate(@NonNull PostViewHolder holder) {
        if (post.getTimestamp() != null) {
            long millis = post.getTimestamp().toDate().getTime();
            String dateString = coMeth.processPostDate(millis, context);
            holder.postDateTextView.setText(dateString);
        }else{
            holder.postDateTextView.setVisibility(View.GONE);
        }
    }

    private void setUserImage(@NonNull PostViewHolder holder, final User user, String userImageUrl) {
        coMeth.setCircleImage(R.drawable.ic_action_person_placeholder, userImageUrl,
                holder.postUserImageView, context);
        holder.postUserImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postUserId = user.UserId;
                openUserPage(postUserId);
            }
        });
    }

    private void handleFollowButton(final @NonNull PostViewHolder holder, final String postUserId) {
        final Button mFollowButton = holder.followButton;
        if (coMeth.isLoggedIn()){
            if (postUserId.equals(currentUserId)){
                mFollowButton.setVisibility(View.GONE);
            }else{
                checkUserStatus(holder, postUserId, mFollowButton);
            }
        }else{
            holder.followButton.setVisibility(View.VISIBLE);
            mFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToLogin(context.getResources().getString(R.string.login_to_follow));
                }
            });
        }
        if (!coMeth.isConnected(context)){
            holder.followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSnack(context.getResources().getString(R.string.failed_to_connect_text), holder);
                }
            });
        }
    }

    private void checkUserStatus(@NonNull final PostViewHolder holder, final String postUserId, final Button mFollowButton) {
        CollectionReference followingRef = coMeth.getDb().collection(
                USERS + "/" + currentUserId + "/" + FOLLOWING);
        followingRef.document(postUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e == null){
                    assert documentSnapshot != null;
                    if (documentSnapshot.exists()){
                        mFollowButton.setVisibility(View.GONE);
                    }else{
                        mFollowButton.setVisibility(View.VISIBLE);
                        mFollowButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                followUser(holder, postUserId);
                                //show loading
                                mFollowButton.setText(context.getResources().getString(R.string.loading_text));
                            }
                        });
                    }
                }else{
                    mFollowButton.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onEvent: error on getting user follow status" +
                            e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void followUser(final @NonNull PostViewHolder holder, final String postUserId) {
        Map<String, Object> followerMap = new HashMap<>();
        followerMap.put(TIMESTAMP, FieldValue.serverTimestamp());
        followerMap.put(USER_ID_VAL, currentUserId);
        CollectionReference followersRef = coMeth.getDb().collection(
                USERS + "/" + postUserId + "/" + FOLLOWERS);
        followersRef.document(currentUserId).set(followerMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addFollowRef(holder, postUserId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to followPage page\n"+ e.getMessage());
                        showSnack(context.getResources().getString(
                                R.string.failed_to_follow_text) + ": " + e.getMessage(), holder);
                    }
                });
    }

    private void addFollowRef(final @NonNull PostViewHolder holder, final String postUserId) {
        Map<String, Object> followingMap = new HashMap<>();
        followingMap.put(USER_ID_VAL, postUserId);
        followingMap.put(TIMESTAMP, FieldValue.serverTimestamp());
        CollectionReference curUserFollowingRef =
                coMeth.getDb().collection(USERS + "/" + currentUserId + "/" + FOLLOWING);
        curUserFollowingRef.document(postUserId).set(followingMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        subscribeToUser(postUserId);
                        notifyFollow(postUserId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to add reference to current user\n" +
                                e.getMessage() );
                        showSnack(context.getResources().getString(R.string.error_text) + ": " +
                                e.getMessage(), holder);
                    }
                });
    }

    private void subscribeToUser(String postUserId) {
        String topic = postUserId + FOLLOWERS_VAL;
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }

    private void notifyFollow(String postUserId) {
        //send notification to post user with current user user name
        String mTopic = postUserId + NEW_FOLLOWERS_UPDATE;
        new Notify().execute(NEW_FOLLOWERS_UPDATE, mTopic, currentUserId);
    }


    private void handlePostActivity(final @NonNull PostViewHolder holder, Post post) {
        if (coMeth.isLoggedIn() && coMeth.getUid().equals(post.getUser_id())){
            holder.activityLayout.setVisibility(View.VISIBLE);
            int activityCount = post.getActivity();
            if (activityCount > 0) {
                holder.activityTextView.setText(String.valueOf(activityCount));
            }else{
                holder.activityTextView.setText(context.getResources().getString(R.string.activity_text));
            }
        }else{
            holder.activityLayout.setVisibility(View.GONE);
        }
    }

    private void openUserPage(String postUserId) {
        Intent intent = new Intent(context, UserPageActivity.class);
        intent.putExtra(USER_ID, postUserId);
        context.startActivity(intent);
    }

    private void openComments(String postId) {
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra(POST_ID, postId);
        intent.putExtra(SOURCE, className);
        context.startActivity(intent);
    }

    private void likePost(String postId) {
        Map<String, Object> likesMap = new HashMap<>();
        likesMap.put(CoMeth.TIMESTAMP, FieldValue.serverTimestamp());
        coMeth.getDb().collection(POSTS + "/" + postId + "/" + CoMeth.LIKES_COL)
                .document(currentUserId)
                .set(likesMap);
    }

    private void unlikePost(String postId) {
        coMeth.getDb()
                .collection(POSTS + "/" + postId + "/" + CoMeth.LIKES_COL)
                .document(currentUserId)
                .delete();
    }

    private void savePost(final @NonNull PostViewHolder holder, String postId) {
        Map<String, Object> savesMap = new HashMap<>();
        savesMap.put(CoMeth.TIMESTAMP, FieldValue.serverTimestamp());
        //save new post
        coMeth.getDb()
                .collection(POSTS + "/" + postId + "/" + SAVES)
                .document(currentUserId)
                .set(savesMap);
        this.currentUserId = coMeth.getUid();
        coMeth.getDb()
                .collection(USERS + "/" + this.currentUserId + "/" + SUBSCRIPTIONS)
                .document(SAVED_POSTS_DOC).collection(SAVED_POSTS)
                .document(postId)
                .set(savesMap);
        showSaveSnack(context.getString(R.string.added_to_saved_text), holder);
    }

    private void unSavePost(String postId) {
        coMeth.getDb().collection(POSTS + "/" + postId + "/" + SAVES)
                .document(currentUserId)
                .delete();
        coMeth.getDb().collection(USERS + "/" + currentUserId + "/" + SUBSCRIPTIONS)
                .document(SAVED_POSTS_DOC).collection(SAVED_POSTS)
                .document(postId)
                .delete();
    }

    private void sharePost(final @NonNull PostViewHolder holder, Post post, String description) {
        showProgress(context.getString(R.string.loading_text));
        //create post url
        String postId = post.PostId;
        String postUrl = context.getResources().getString(R.string.fursa_url_post_head) + postId;
        String imageUrl = post.getImage_url();
        shareDynamicLink(postUrl, description, imageUrl, holder);
    }

    private String getDescription(String title, String desc){
        String description = "";
        if (title != null && !title.isEmpty()){
            description = description.concat(title);
            if (desc != null && !desc.isEmpty()){
                return description.concat("\n" + desc);
            } else {
                return "";
            }
        }else {
            if (desc != null && !desc.isEmpty()) {
                return  description.concat(desc);
            }else{
                return "";
            }
        }
    }

    private String getShareDescription(String description){
        if (description != null && !description.isEmpty()){
            if (description.length() >= 150){
                return description.substring(0, 150);
            }else {
                return description;
            }
        }else{
            return context.getResources().getString(R.string.sharing_opp_text);
        }
    }

    private void showProgress(String message) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void shareDynamicLink(String postUrl, final String description,
                                  final String postImageUrl, final PostViewHolder holder) {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(postUrl))
                .setDynamicLinkDomain(context.getString(R.string.dynamic_link_domain))
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(coMeth.minVerCode)
                        .setFallbackUrl(Uri.parse(context.getString(R.string.playstore_url)))
                        .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(getSharedTitle(description))
                                .setDescription(getShareDescription(description))
                                .setImageUrl(Uri.parse(getImageUrl(postImageUrl)))
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            //show share dialog
                            String fullShareMsg = getShareDescription(description) + "\n" + shortLink;
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
                            shareIntent.putExtra(Intent.EXTRA_TEXT, fullShareMsg);
                            coMeth.stopLoading(progressDialog);
                            context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(R.string.share_with_text)));
                        } else {
                            Log.d(TAG, "onComplete: \ncreating short link task failed\n" +
                                    task.getException());
                            coMeth.stopLoading(progressDialog);
                            showSnack(context.getString(R.string.failed_to_share_text), holder);
                        }
                    }
                });
    }

    @NonNull
    private String getSharedTitle(String description) {
        if (getShareDescription(description).contains("\n")){
            return getShareDescription(description).substring(0,
                    getShareDescription(description).indexOf("\n"));
        }else{
            return getShareDescription(description);
        }
    }

    private String getImageUrl(String postImageUrl) {
        if (postImageUrl != null) {
            return postImageUrl;
        } else {
            return context.getString(R.string.app_icon_url);
        }
    }

    private void openPost(String postId) {
        if (className.equals(CLASS_NAME_VIEW_POST)){
            ((ViewPostActivity)context).finish();
            Intent openPostIntent = new Intent(context, ViewPostActivity.class);
            openPostIntent.putExtra(POST_ID, postId);
            context.startActivity(openPostIntent);
        }else {
            Intent openPostIntent = new Intent(context, ViewPostActivity.class);
            openPostIntent.putExtra(POST_ID, postId);
            context.startActivity(openPostIntent);
        }
    }

    private void showSnack(String message, final @NonNull PostViewHolder holder) {
        Snackbar.make(holder.mView.findViewById(R.id.postLayout),
                message, Snackbar.LENGTH_LONG).show();
    }

    private void showSaveSnack(String message, final @NonNull PostViewHolder holder) {

        //check current class
        switch (className) {
            case USER_POST_ACTIVITY:
                Snackbar.make(holder.mView.findViewById(R.id.postLayout),
                        message, Snackbar.LENGTH_LONG)
                        .setAction(context.getString(R.string.see_list_text),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent goToSavedIntent = new Intent(context, UserPostsActivity.class);
                                        goToSavedIntent.putExtra(DESTINATION, SAVED_VAL);
                                        ((UserPostsActivity)context).finish();
                                        context.startActivity(goToSavedIntent);
                                    }
                                })
                        .setActionTextColor(context.getResources().getColor(R.color.secondaryLightColor))
                        .show();

            default:
                Snackbar.make(holder.mView.findViewById(R.id.postLayout),
                        message, Snackbar.LENGTH_LONG)
                        .setAction(context.getString(R.string.see_list_text),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent goToSavedIntent = new Intent(context, UserPostsActivity.class);
                                        goToSavedIntent.putExtra(DESTINATION, SAVED_VAL);
                                        context.startActivity(goToSavedIntent);
                                    }
                                })
                        .setActionTextColor(context.getResources().getColor(R.color.secondaryLightColor))
                        .show();
                break;
        }
    }

    private void goToLogin(String message) {
        Intent goToLoginIntent = new Intent(context, LoginActivity.class);
        goToLoginIntent.putExtra("message", message);
        context.startActivity(goToLoginIntent);
    }

    class PostViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private TextView postUsernameTextView, sponsoredTextView;
        private ImageView postUserImageView;
        private TextView postLikesCount, postCommentCount, postLocationTextView,
                postDateTextView, descTextView, activityTextView;
        private ImageView postSaveButton, postImageView, postLikeButton;
        private CardView postCardView;
        private LinearLayout activityLayout, likeLayout, saveLayout, commentLayout,
                shareLayout, topLayout, actionsLayout;
        private Button followButton;

        PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            postUsernameTextView = mView.findViewById(R.id.postUsernameTextView);
            postUserImageView = mView.findViewById(R.id.postUserImageImageView);
            postLocationTextView = mView.findViewById(R.id.postLocationTextView);
            postLikeButton = mView.findViewById(R.id.postLikeImageView);
            postLikesCount = mView.findViewById(R.id.postLikeCountText);
            likeLayout = mView.findViewById(R.id.postLikeLayout);
            postSaveButton = mView.findViewById(R.id.postSaveImageView);
            saveLayout = mView.findViewById(R.id.postSaveLayout);
            shareLayout = mView.findViewById(R.id.postShareLayout);
            postImageView = mView.findViewById(R.id.postImageView);
            postCommentCount = mView.findViewById(R.id.postCommentCountText);
            commentLayout = mView.findViewById(R.id.postCommentLayout);
            postLocationTextView = mView.findViewById(R.id.postLocationTextView);
            postLocationTextView = mView.findViewById(R.id.postLocationTextView);
            descTextView = mView.findViewById(R.id.postDescTextView);
            postDateTextView = mView.findViewById(R.id.postDateTextView);
            activityTextView = mView.findViewById(R.id.postActivityTextView);
            activityLayout = mView.findViewById(R.id.postActivityLayout);
            postCardView = mView.findViewById(R.id.postCardView);
            followButton = mView.findViewById(R.id.postFollowButton);
            topLayout = mView.findViewById(R.id.postTopLayout);
            actionsLayout = mView.findViewById(R.id.postActionsLayout);
            sponsoredTextView = mView.findViewById(R.id.postSponsoredTextView);
        }
        //retrieve the image
        void setPostImage(final String imageDownloadUrl, final String thumbDownloadUrl) {
            if (imageDownloadUrl != null && thumbDownloadUrl != null) {
                postImageView.setVisibility(View.VISIBLE);
                coMeth.setImageWithTransition(R.color.colorWhite, imageDownloadUrl,
                        thumbDownloadUrl, postImageView, glide);
            } else {
                postImageView.setVisibility(View.GONE);
            }
        }

        void setPostLocation(ArrayList<String> locationArray) {
            if (locationArray != null) {
                String locationString = "";
                for (int i = 0; i < locationArray.size(); i++) {
                    locationString = locationString.concat(locationArray.get(i) + "\n");
                }
                postLocationTextView.setText(locationString.trim());
            } else {
                postLocationTextView.setVisibility(View.GONE);
            }
        }

        public void build(int position, String className, int postType) {
            //check class name
            switch (className){
                case CLASS_NAME_VIEW_POST:
                   handlePostInViewPost(post);
                   break;
               default:
                   User user = usersList.get(position);
                   handleNormalPost(post, user, postType);
            }
        }

        private void handleNormalPost(Post post, User user, int postType) {

            String title = post.getTitle();
            String desc = post.getDesc();
            String description = getDescription(title, desc);
            setDescription(this, title, desc);
            ArrayList<String> locationArray = post.getLocation();
            setPostLocation(locationArray);
            currentUserId = coMeth.getUid();
            String imageUrl = post.getImage_url();
            String thumbUrl = post.getThumb_url();
            setPostImage(imageUrl, thumbUrl);
            handlePostActivity(this, post);
            postUsernameTextView.setText(user.getName());
            String userImageUrl = user.getImage();
            setUserImage(this, user, userImageUrl);
            String postUserId = post.getUser_id();
            handleFollowButton(this, postUserId);
            handlePostDate(this);

            final String postId = post.PostId;
            CollectionReference likesRef = coMeth.getDb().collection(
                    POSTS + "/" + postId + "/" + LIKES_COL);
            handleLikesCount(this, likesRef);
            if (coMeth.isConnected(context) && coMeth.isLoggedIn()) {
                handlePostLikes(this, postId, likesRef);
                handlePostSaves(this, postId);
            } else {
                handlePostActionsConnectionException(this);
                handlePostActionsLoginException(this);
            }
            handleSharePost(post, this, description);
            handlePostCommentsCount(this, postId);
            commentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openComments(postId);
                }
            });
            postCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPost(postId);
                }
            });

            //handle sponsored
            if (postType == POST_TYPE_SPONSORED){
                sponsoredTextView.setVisibility(View.VISIBLE);
            }else{
                sponsoredTextView.setVisibility(View.GONE);
            }

            new UpdatePostActivityTask().execute();
        }

        private void handlePostInViewPost(Post post) {
            setPostImage(post.getImage_url(), post.thumb_url);
            postImageView.setVisibility(View.VISIBLE);
            final String postId = post.PostId;
            postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPost(postId);
                }
            });
            postCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPost(postId);
                }
            });
            String title = post.getTitle();
            String desc = post.getDesc();
            setDescription(this, title, desc);
            //hide views
            actionsLayout.setVisibility(View.GONE);
            topLayout.setVisibility(View.GONE);
            postDateTextView.setVisibility(View.GONE);
        }
    }

    class AdViewHolder extends RecyclerView.ViewHolder {

        private AdView adView;

        AdViewHolder(View itemView) {
            super(itemView);
            adView = itemView.findViewById(R.id.adView);

        }

        void build(){
//                    .addTestDevice("CD3E657857E4EEBE754743B250DCAB5E")
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    public static class UpdatePostActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            updateFeedViews(post);
            return null;
        }
    }
}
