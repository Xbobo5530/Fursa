package com.nyayozangu.labs.fursa.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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
import com.nyayozangu.labs.fursa.activities.MainActivity;
import com.nyayozangu.labs.fursa.activities.UserPageActivity;
import com.nyayozangu.labs.fursa.activities.ViewPostActivity;
import com.nyayozangu.labs.fursa.helpers.Notify;
import com.nyayozangu.labs.fursa.models.Posts;
import com.nyayozangu.labs.fursa.activities.LoginActivity;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.ACTION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.COMMENTS_COLL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.DESTINATION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWERS_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWING;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.GOTO;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.LIKES_COL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NEW_FOLLOWERS_UPDATE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POST_ID;
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
import static com.nyayozangu.labs.fursa.helpers.CoMeth.VIEW_POST;

/**
 * Created by Sean on 4/4/18.
 * <p>
 * Adapter class for the Recycler view
 */

public class PostsRecyclerAdapter extends RecyclerView.Adapter<PostsRecyclerAdapter.ViewHolder> {

    // TODO: 6/14/18 add an impressions field for posts viewed on feed

    private static final String TAG = "Sean";
    private static final String VIEW_CAT_ACTIVITY = "ViewCategoryActivity";
    private static final String SAVED_FRAGMENT = "SavedTabFragment";
    private static final String RECENT_FRAGMENT = "RecentFragment";
    private static final String POPULAR_FRAGMENT = "PopularFragment";

    //member variables for storing posts
    public List<Posts> postsList;
    public List<Users> usersList;
    public Context context;
    public Activity activity;
    public RequestManager glide;
    public static Posts post;
    private String currentUserId;
    private String className;
    private ProgressDialog progressDialog;
    private CoMeth coMeth = new CoMeth();

    //empty constructor for receiving the posts
    public PostsRecyclerAdapter(List<Posts> postsList, List<Users> usersList, String className,
                                RequestManager glide, Activity activity) {
        this.postsList = postsList;
        this.usersList = usersList;
        this.className = className;
        this.glide = glide;
        this.activity = activity;
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
//        holder = mHolder;
        loadPostContent(position, holder);
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
        return position;
    }

    static void updateFeedViews(Posts post) {
        Map<String, Object> feedViewMap = new HashMap<>();
        int feedViews = post.getFeed_views() + 1;
        feedViewMap.put("feed_views", feedViews);
        FirebaseFirestore.getInstance().collection(POSTS)
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

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Log.d(TAG, "onViewRecycled: view is recycled clearing images");
        glide.clear(holder.postImageView);
    }

    private void loadPostContent(final int position, @NonNull final ViewHolder holder) {

        post = postsList.get(position);

        if (className.equals(VIEW_POST)){
            holder.setPostImage(post.getImage_url(), post.thumb_url);
            holder.postImageView.setVisibility(View.VISIBLE);
            holder.postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPost(post.PostId);
                }
            });
            if (post.getTitle() != null) {
                holder.titleTextView.setText(post.getTitle());
                holder.titleTextView.setVisibility(View.VISIBLE);
            }else{
                holder.titleTextView.setVisibility(View.GONE);
            }
            // TODO: 7/24/18 remove post title

            //hide views
            holder.actionsLayout.setVisibility(View.GONE);
            holder.topLayout.setVisibility(View.GONE);
            holder.descTextView.setVisibility(View.GONE);
            holder.adCard.setVisibility(View.GONE);
            holder.postDateTextView.setVisibility(View.GONE);

        }else {
            Random random = new Random();
            int randNum = random.nextInt((((position + 10) - position) + 1)) + position;
            if (position == randNum && (className.equals(RECENT_FRAGMENT) ||
                            (className.equals(POPULAR_FRAGMENT) ||
                                    className.equals(VIEW_CAT_ACTIVITY)))) {
                holder.setAd();
            } else {
                holder.adCard.setVisibility(View.GONE);
            }

            final Users user = usersList.get(position);
            String title = post.getTitle();
            String desc = post.getDesc();
            holder.setPostBasicData(title, desc);
            ArrayList locationArray = post.getLocation();
            holder.setPostLocation(locationArray);
            currentUserId = coMeth.getUid();
            String postUserId = post.getUser_id();
            new UpdatePostActivityTask().execute();
            final String imageUrl = post.getImage_url();
            String thumbUrl = post.getThumb_url();
            holder.setPostImage(imageUrl, thumbUrl);
            final String postId = post.PostId;
            String userName = user.getName();
            String userImageDownloadUri = user.getImage();
            handlePostActivity(holder, post);
            holder.postUsernameTextView.setText(userName);
            coMeth.setCircleImage(R.drawable.ic_action_person_placeholder, userImageDownloadUri,
                    holder.postUserImageView);
            holder.postUserImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String postUserId = user.UserId;
                    openUserPage(postUserId);
                }
            });
            handleFollowButton(holder, postUserId);

            //handle date for posts
            if (post.getTimestamp() != null) {
                long millis = post.getTimestamp().getTime();
                String dateString = coMeth.processPostDate(millis);
                holder.postDateTextView.setText(dateString);
            }else{
                holder.postDateTextView.setVisibility(View.GONE);
            }

            CollectionReference likesRef = coMeth.getDb().collection(
                    POSTS + "/" + postId + "/" + LIKES_COL);
            //get likes count
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


            //determine likes by current user
            if (coMeth.isConnected() && coMeth.isLoggedIn()) {
                //check post likes
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

                //check post saves
                coMeth.getDb().collection(POSTS + "/" + postId + "/" + SAVED_COL)
                        .document(currentUserId)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot,
                                                FirebaseFirestoreException e) {
                                if (e == null) {
                                    if (documentSnapshot.exists()) {
                                        holder.postSaveButton.setImageDrawable(
                                                context.getResources().getDrawable(R.drawable.ic_action_bookmarked));
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

            } else {
                if (!coMeth.isConnected()) {
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

            //share post
            holder.shareLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharePost(holder, postsList.get(position));
                }
            });

            //count comments
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

            //comment icon click action
            holder.commentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openComments(postId);
                }
            });

            holder.postCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPost(postId);
                }
            });
        }
    }

    private void handleFollowButton(final @NonNull ViewHolder holder, final String postUserId) {
        final Button mFollowButton = holder.followButton;
        if (coMeth.isLoggedIn()){
            if (postUserId.equals(currentUserId)){
                mFollowButton.setVisibility(View.GONE);
            }else{
                checkUserStatus(holder, postUserId, mFollowButton);
            }
        }else{
            holder.followButton.setVisibility(View.VISIBLE);
        }
        if (!coMeth.isConnected()){
            holder.followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSnack(context.getResources().getString(R.string.failed_to_connect_text), holder);
                }
            });
        }
    }

    private void checkUserStatus(@NonNull final ViewHolder holder, final String postUserId, final Button mFollowButton) {
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

    private void followUser(final @NonNull ViewHolder holder, final String postUserId) {
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
                        Log.d(TAG, "onSuccess: followed added");
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

    private void addFollowRef(final @NonNull ViewHolder holder, final String postUserId) {
        Map<String, Object> followingMap = new HashMap<>();
        followingMap.put(USER_ID_VAL, postUserId);
        followingMap.put(TIMESTAMP, FieldValue.serverTimestamp());
        CollectionReference curUserFollowingRef =
                coMeth.getDb().collection(USERS + "/" + currentUserId + "/" + FOLLOWING);
        curUserFollowingRef.document(postUserId).set(followingMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: following ref added to current user");
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


    private void handlePostActivity(final @NonNull ViewHolder holder, Posts post) {
        if (coMeth.isLoggedIn() && coMeth.getUid().equals(post.getUser_id())){
            holder.activityLayout.setVisibility(View.VISIBLE);
            int activityCount = post.getActivity();
            holder.activityTextView.setText(String.valueOf(activityCount));
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

    private void savePost(final @NonNull ViewHolder holder, String postId) {
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

    private void sharePost(final @NonNull ViewHolder holder, Posts post) {
        showProgress(context.getString(R.string.loading_text));
        //create post url
        String postId = post.PostId;
        String postUrl = context.getResources().getString(R.string.fursa_url_post_head) + postId;
        String title = post.getTitle();
        String desc = post.getDesc();
        String imageUrl = post.getImage_url();
        Log.d(TAG, "sharePost: title is: " + title);
        shareDynamicLink(postUrl, title, desc, imageUrl, holder);
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
    private void shareDynamicLink(String postUrl, final String postTitle, final String postDesc,
                                  final String postImageUrl, final ViewHolder holder) {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
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
                            Log.d(TAG, "onComplete: short link is: " + shortLink);

                            //show share dialog
                            String fullShareMsg = postTitle + "\n" + shortLink;
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                                    context.getResources().getString(R.string.app_name));
                            shareIntent.putExtra(Intent.EXTRA_TEXT, fullShareMsg);
                            coMeth.stopLoading(progressDialog);
                            context.startActivity(Intent.createChooser(shareIntent,
                                    context.getResources().getString(R.string.share_with_text)));
                        } else {
                            Log.d(TAG, "onComplete: \ncreating short link task failed\n" +
                                    task.getException());
                            coMeth.stopLoading(progressDialog);
                            showSnack(context.getString(R.string.failed_to_share_text), holder);
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
        if (className.equals(VIEW_POST)){
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

    private void showSnack(String message, final @NonNull ViewHolder holder) {
        Snackbar.make(holder.mView.findViewById(R.id.postLayout),
                message, Snackbar.LENGTH_LONG).show();
    }

    private void showSaveSnack(String message, final @NonNull ViewHolder holder) {

        //check current class
        switch (className) {
            case RECENT_FRAGMENT:
            case POPULAR_FRAGMENT:

                Snackbar.make(activity.findViewById(R.id.mainSnack),
                        message, Snackbar.LENGTH_SHORT)
                        .show();
                break;
            case SAVED_FRAGMENT:
                //do nothing
                break;
            default:
                Snackbar.make(holder.mView.findViewById(R.id.postLayout),
                        message, Snackbar.LENGTH_LONG)
                        .setAction(context.getString(R.string.see_list_text),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent goToSavedIntent = new Intent(context, MainActivity.class);
                                        goToSavedIntent.putExtra(ACTION, GOTO);
                                        goToSavedIntent.putExtra(DESTINATION, SAVED_VAL);
                                        context.startActivity(goToSavedIntent);
                                    }
                                })
                        .setActionTextColor(context.getResources().getColor(R.color.secondaryLightColor))
                        .show();
                break;
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
    class ViewHolder extends RecyclerView.ViewHolder {

        //initiate view
        View mView;

        //initiate elements in the view holder
        private TextView postUsernameTextView;
//        private CircleImageView postUserImageCircleView;
        private ImageView postUserImageView;
        private TextView postLikesCount, postCommentCount,
                postLocationTextView, postDateTextView, descTextView, titleTextView,
                postSaveText, postShareText, activityTextView;
        private ImageView postSaveButton, postSharePostButton, postCommentButton,
                postImageView, postLikeButton;
        private CardView adCard, postCardView;
        private LinearLayout activityLayout, likeLayout, saveLayout, commentLayout,
                shareLayout, topLayout, actionsLayout;
        private Button followButton;

        ViewHolder(View itemView) {
            super(itemView);
            //use mView to populate other methods
            mView = itemView;

            postUsernameTextView = mView.findViewById(R.id.postUsernameTextView);
            postUserImageView = mView.findViewById(R.id.postUserImageImageView);
            postLocationTextView = mView.findViewById(R.id.postLocationTextView);
            postLikeButton = mView.findViewById(R.id.postLikeImageView);
            postLikesCount = mView.findViewById(R.id.postLikeCountText);
            likeLayout = mView.findViewById(R.id.postLikeLayout);
            postSaveButton = mView.findViewById(R.id.postSaveImageView);
            postSaveText = mView.findViewById(R.id.postSaveTextTextView);
            saveLayout = mView.findViewById(R.id.postSaveLayout);
            postSharePostButton = mView.findViewById(R.id.postShareImageView);
            shareLayout = mView.findViewById(R.id.postShareLayout);
            postImageView = mView.findViewById(R.id.postImageView);
            postCommentButton = mView.findViewById(R.id.postCommentImageView);
            postCommentCount = mView.findViewById(R.id.postCommentCountText);
            commentLayout = mView.findViewById(R.id.postCommentLayout);
            postLocationTextView = mView.findViewById(R.id.postLocationTextView);
            postLocationTextView = mView.findViewById(R.id.postLocationTextView);
            postSaveText = mView.findViewById(R.id.postSaveTextTextView);
            postShareText = mView.findViewById(R.id.postShareTextTextView);
            titleTextView = mView.findViewById(R.id.postTitleTextView);
            descTextView = mView.findViewById(R.id.postDescTextView);
            postDateTextView = mView.findViewById(R.id.postDateTextView);
            activityTextView = mView.findViewById(R.id.postActivityTextView);
            activityLayout = mView.findViewById(R.id.postActivityLayout);
            postCardView = mView.findViewById(R.id.postCardView);
            followButton = mView.findViewById(R.id.postFollowButton);
            topLayout = mView.findViewById(R.id.postTopLayout);
            actionsLayout = mView.findViewById(R.id.postActionsLayout);

            adCard = mView.findViewById(R.id.adCard);
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

        void setPostLocation(ArrayList locationArray) {
            if (locationArray != null) {
                String locationString = "";
                for (int i = 0; i < locationArray.size(); i++) {
                    locationString = locationString.concat(locationArray.get(i).toString() + "\n");
                }
                postLocationTextView.setText(locationString.trim());
            } else {
                postLocationTextView.setVisibility(View.GONE);
            }
        }

        //set ad
        void setAd() {
            Log.d(TAG, "setAd: ");
            AdView adView = mView.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
//                    .addTestDevice("CD3E657857E4EEBE754743B250DCAB5E")
                    .build();
            adView.loadAd(adRequest);
            adCard.setVisibility(View.VISIBLE);
        }

        void setPostBasicData(String title, String desc) {
            if (title != null){
                titleTextView.setVisibility(View.VISIBLE);
                titleTextView.setText(title);
            }else{
                titleTextView.setVisibility(View.GONE);
            }
            if (desc != null){
                descTextView.setVisibility(View.VISIBLE);
                descTextView.setText(desc);
            }else{
                descTextView.setVisibility(View.GONE);
            }
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
