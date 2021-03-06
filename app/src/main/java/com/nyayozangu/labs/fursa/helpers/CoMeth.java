package com.nyayozangu.labs.fursa.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.format.DateFormat;
import android.util.Log;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;


import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nyayozangu.labs.fursa.BuildConfig;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.models.Notifications;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class CoMeth{
    public static final String TAG = "Sean";

    public static final String FACEBOOK_DOT_COM = "facebook.com";
    public static final String GOOGLE_DOT_COM = "google.com";
    public static final String TWITTER_DOT_COM = "twitter.com";

    //collections
    public static final String CATEGORIES = "Categories";
    public static final String TAGS = "Tags";
    public static final String POSTS = "Posts";
    public static final String USERS = "Users";
    public static final String MY_POSTS = "MyPosts";
    public static final String SAVED_POSTS = "SavedPosts";
    public static final String SUBSCRIPTIONS = "Subscriptions";
    public static final String NOTIFICATIONS = "Notifications";

    //documents
    public static final String CATEGORIES_DOC = "categories";
    public static final String MY_POSTS_DOC = "my_posts";
    public static final String SAVED_POSTS_DOC = "saved_posts";

    public static final String CATEGORIES_VAL = "categories";

    public static final String TIMESTAMP = "timestamp";

    //cats values and names
    public static final String EXHIBITIONS = "exhibitions";
    public static final String EVENTS = "events";
    public static final String PLACES = "places";
    public static final String SERVICES = "services";
    public static final String BUSINESS = "business";
    public static final String BUY_AND_SELL = "buysell";
    public static final String EDUCATION = "education";
    public static final String JOBS = "jobs";
    public static final String QUERIES = "queries";
    public static final String DISCUSSIONS = "discussions";
    public static final String ART = "art";
    public static final String APPS = "apps";
    public static final String GROUPS = "groups";


    //intent actions and keys
    public static final String ACTION = "action";
    public static final String GOTO = "goto";
    public static final String NOTIFY = "notify";
    public static final String DESTINATION = "destination";
    public static final String IMAGE_URL = "imageUrl";
    public static final String POST_ID = "postId";
    // TODO: 6/10/18 add all the static fields
    public static final String MESSAGE = "message";
    public static final String NEW_POST_UPDATES = "new_post_updates";
    public static final String COMMENT_UPDATES = "comment_updates";
    public static final String LIKES_UPDATES = "likes_updates";
    public static final String SAVED_POSTS_UPDATES = "saved_posts_updates";
    public static final String CATEGORIES_UPDATES = "categories_updates";
    public static final String LIKES = "LIKES";
    public static final String COMMENTS = "COMMENTS";
    public static final String SAVED = "SAVED";
    public static final String CATS = "CATS";
    public static final String TITLE = "title";
    public static final String DESC = "desc";
    public static final String USER_ID_VAL = "user_id";
    public static final String EXTRA = "extra";
    public static final String ACTIVITY = "activity";
    public static final String STATUS = "status";
    public static final String NOTIF_TYPE = "notif_type";
    public static final String PERMISSION = "permission";
    public static final String ADMIN = "admin";
    public static final String TAG_NAME = "tag";
    public static final String SAVED_VAL = "saved";
    public static final String HOME_FEED_AD_UNIT_ID = "ca-app-pub-6180360542591636/5341130287";
    public static final String CATS_FEED_AD_UNIT_ID = "ca-app-pub-6180360542591636/1267181570";
    public static final String TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String LIKES_COL = "Likes";
    public static final String SAVES = "Saves";
    public static final String VIEWS = "views";
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";
    public static final String ERROR = "error";
    public static final String UPDATE = "update";
    public static final String TERMS = "terms";
    public static final String TERMS_URL = "http://fursa.nyayozangu.com/privacy_policy/";
    public static final String HAS_NOT_ACCEPTED_TERMS = "false";
    public static final String TRUE = "true";
    public static final String COMMENTS_COLL = "Comments";
    public static final String COMMENTS_DOC = "comments";
    public static final String CATEGORY = "category";
    public static final String FLAGS = "Flags";
    public static final String POSTS_DOC = "posts";
    public static final String FLAGS_NAME = "flags";
    public static final String SOURCE = "source";
    public static final String NOTIFICATIONS_VAL = "notifications";
    public static final String SAVED_COL = "Saves";
    public static final String TAGS_VAL = "tags";
    public static final String POSTS_VAL = "posts";
    public static final String CLASS_NAME_VIEW_POST = "viewPostActivity";
    public static final String COMMENT = "comment";
    public static final String POST_ID_VAL = "post_id";
    public static final String TAG_VAL = "tag";
    public static final String FOLLOWERS = "Followers";
    public static final String FOLLOWING = "Following";
    public static final String FOLLOWERS_VAL = "followers";
    public static final String FOLLOWING_VAL = "following";
    public static final String FOLLOWER_POST = "follower_post";
    public static final String NEW_FOLLOWERS_UPDATE = "new_followers_update";
    public static final String FOLLOW = "follow";

    public static final String NEW_FOLLOWER = "new_follower";
    public static final String LIKES_VAL = "likes";
    public static final String EVENT_DATE = "event_date";
    public static final String NAME = "name";
    public static final String IMAGE = "image";
    public static final String USER_POSTS = "user_posts";
    public static final int POST_TYPE_POST = 0;
    public static final int POST_TYPE_AD = 1;
    public static final int POST_TYPE_SPONSORED = 2;
    public static final int NOTIFICATION_STATUS_UNREAD = 0;
    public static final int NOTIFICATION_STATUS_READ = 1;
    public static final String CREDIT = "credit";
    public static final String DURATION = "duration";
    public static final String CREATED_AT = "created_at";
    public static final String EXPIRES_AT = "expires_at";
    public static final String SPONSORED = "Sponsored";
    public static final String PROMOTIONS = "Promotions";
    public static final String COST = "cost";
    public static final String HAS_ACCEPTED_TERMS = "true";

    public String[] getCatTitle(Context context){
        return new String[]{

                context.getResources().getString(R.string.cat_business),
                context.getResources().getString(R.string.cat_jobs),
                context.getResources().getString(R.string.cat_education),
                context.getResources().getString(R.string.cat_art),
                context.getResources().getString(R.string.cat_buysell),
                context.getString(R.string.cat_exhibitions),
                context.getResources().getString(R.string.cat_places),
                context.getResources().getString(R.string.cat_events),
                context.getResources().getString(R.string.cat_services),
                context.getResources().getString(R.string.cat_apps),
                context.getResources().getString(R.string.cat_groups),
                context.getResources().getString(R.string.cat_queries)
        };
    }

    public String[] getReportList (Context context){
        return new String[]{
                context.getString(R.string.spam_text),
                context.getString(R.string.inapropriate_text)
        };
    }

    public final String[] reportListKey = new String[]{
            "spam",
            "inappropriate"
    };

    public int minVerCode = BuildConfig.VERSION_CODE;
    public CoMeth() { } //empty constructor


    public String[] getCategories(Context context){
        return new String[]{

                context.getString(R.string.cat_business),
                context.getString(R.string.cat_exhibitions),
                context.getResources().getString(R.string.cat_art),
                context.getString(R.string.cat_events),
                context.getString(R.string.cat_buysell),
                context.getString(R.string.cat_education),
                context.getString(R.string.cat_jobs),
                context.getString(R.string.cat_services),
                context.getString(R.string.cat_places),
                context.getString(R.string.cat_queries),
                context.getResources().getString(R.string.cat_apps),
                context.getResources().getString(R.string.cat_groups)
        };
    }

    //is logged in
    public boolean isLoggedIn() {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser() != null;
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }

    public FirebaseFirestore getDb() {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        return  firestore;
    }

    public FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public String getUid() {
        return this.getAuth().getUid();
    }

    public void signOut() { this.getAuth().signOut(); }

    public StorageReference getStorageRef() {
        return FirebaseStorage.getInstance().getReference();
    }

    public void setCircleImage(int placeholderDrawable, String imageUrl,
                               ImageView targetImageView, Context context) {
        try {
            RequestOptions placeHolderRequest = new RequestOptions();
            placeHolderRequest.placeholder(placeholderDrawable);
            Glide.with(context)
                    .setDefaultRequestOptions(placeHolderRequest.circleCrop())
                    .load(imageUrl)
                    .into(targetImageView);
        } catch (Exception e) {
            Log.d(TAG, "setImage: error " + e.getMessage());
        }
    }

    public void setImageWithTransition(int placeholderDrawable, String imageUrl,
                                       ImageView targetImageView, Context context) {
        try {

            RequestOptions placeHolderRequest = new RequestOptions();
            placeHolderRequest.placeholder(placeholderDrawable);
            Glide.with(context)
                    .setDefaultRequestOptions(placeHolderRequest)
                    .load(imageUrl)
                    .transition(withCrossFade())
                    .into(targetImageView);
        } catch (Exception e) {
            Log.d(TAG, "setImage: error " + e.getMessage());
        }
    }
    public void setImageWithTransition(int placeholderDrawable, String imageUrl, String thumbUrl,
                                       ImageView mImageView, RequestManager mGlide) {
        try {
            RequestOptions placeHolderRequest = new RequestOptions();
            placeHolderRequest.placeholder(placeholderDrawable);
            mGlide.setDefaultRequestOptions(placeHolderRequest)
                    .load(imageUrl)
                    .transition(withCrossFade())
                    .thumbnail(mGlide.load(thumbUrl))
                    .into(mImageView);
        } catch (Exception e) {
            Log.d(TAG, "setImage: error " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setImage(int placeholderDrawable, String imageUrl, String thumbUrl,
                         ImageView targetImageView, RequestManager glide) {
        try {
            RequestOptions placeHolderRequest = new RequestOptions();
            placeHolderRequest.placeholder(placeholderDrawable);
           glide.applyDefaultRequestOptions(placeHolderRequest)
                    .load(imageUrl)
                    .thumbnail(glide.load(thumbUrl))
                    .into(targetImageView);
        } catch (Exception e) {
            Log.d(TAG, "setImage: failed to set image " + e.getMessage());
        }
    }

    public String getCatKey(String catValue) {

        switch (catValue) {
            // TODO: 6/17/18 replace strings with static vals
            case "Events":
                return EVENTS;
            case "Places":
                return PLACES;
            case "Services":
                return SERVICES;
            case "Business":
                return BUSINESS;
            case "Buy and sell":
                return BUY_AND_SELL;
            case "Education":
                return EDUCATION;
            case "Jobs":
                return JOBS;
            case "Discussions":
                return QUERIES;
            case "Exhibitions":
                return EXHIBITIONS;
            case "Art":
                return ART;
            case "Apps":
                return APPS;
            case "Groups":
                return GROUPS;

            //handle swahili items
            case "Matukio":
                return "events";
            case "Maeneo":
                return "places";
            case "Huduma":
                return "services";
            case "Biashara":
                return "business";
            case "Kununua na kuuza":
                return "buysell";
            case "Elimu":
                return "education";
            case "Nafasi za kazi":
                return "jobs";
            case "Majadiliano":
                return "queries";
            case "Maonyesho ya biashara":
                return "exhibitions";
            case "Sanaa":
                return "art";
            case "Makundi":
                return "groups";
            default:
                return null;
        }

    }

    public String getCatValue(String catValue, Context context) {

            /*
            "Events",
            "Places",
            "Services",
            "Business",
            "Buy and sell",
            "Education",
            "Jobs",
            "Queries"
            "Exhibitions"
            "groups"*/


        //return value for key
        switch (catValue) {

            case "events":
                return context.getString(R.string.cat_events);
            case "places":
                return context.getString(R.string.cat_places);
            case "services":
                return context.getString(R.string.cat_services);
            case "business":
                return context.getString(R.string.cat_business);
            case "buysell":
                return context.getString(R.string.cat_buysell);
            case "education":
                return context.getString(R.string.cat_education);
            case "jobs":
                return context.getString(R.string.cat_jobs);
            case "queries":
                return context.getString(R.string.cat_queries);
            case "exhibitions":
                return context.getString(R.string.cat_exhibitions);
            case "art":
                return context.getString(R.string.cat_art);
            case "apps":
                return context.getString(R.string.cat_apps);
            case "groups":
                return context.getString(R.string.cat_groups);
            default:
                Log.d(TAG, "getCatValue: default");
                return "";

        }
    }

    // TODO: 6/17/18 code review
    public String processPostDate(long millis, Context context) {

        //get current date
        long now = new Date().getTime();
        long timeLapsed = (now - millis) / (1000 * 60);
        //minutes ago
        if (timeLapsed < 1) {
            return context.getString(R.string.min_ago_text);
        } else if (timeLapsed > 1 && timeLapsed < 60) {
            return String.valueOf(timeLapsed) + " " + context.getString(R.string.mins_ago_text);
        } else if (timeLapsed >= 60 && timeLapsed <= 120) {
            return context.getString(R.string.hr_ago_text);
        } else if (timeLapsed / 60 > 1 && timeLapsed / 60 < 24) {
            return timeLapsed / 60 + " " + context.getString(R.string.hrs_ago_text);
        } else if (timeLapsed / (60 * 24) >= 1 && timeLapsed / (60 * 24) < 2) {
            return context.getResources().getString(R.string.yesterday_text);
        } else if (timeLapsed / (60 * 24) >= 2 && timeLapsed / (60 * 24) < 7) {
            return timeLapsed / (60 * 24) + " " + context.getString(R.string.day_ago_text);
        } else if (timeLapsed / (60 * 24 * 7) > 1 && timeLapsed / (60 * 24 * 7) <= 4) {
            return timeLapsed / (60 * 24 * 7) + " " + context.getString(R.string.weeks_ago_text);
        } else if (timeLapsed / (60 * 24 * 7 * 4) > 1 && timeLapsed / (60 * 24 * 7 * 4) <= 12) {
            return timeLapsed / (60 * 24 * 7 * 4) + " " + context.getString(R.string.months_ago_text);
        } else {
            return DateFormat.format("EEE, MMM d, 20yy\nh:mm a", new Date(millis)).toString();
        }
    }

    public void stopLoading(ProgressDialog progressDialog,
                            SwipeRefreshLayout swipeRefreshLayout) {

        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
        if (swipeRefreshLayout != null) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    public void stopLoading(ProgressDialog progressDialog) {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public void stopLoading(SwipeRefreshLayout swipeRefreshLayout) {
        if (swipeRefreshLayout != null) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    public void stopLoading(ProgressBar progressBar) {
        if (progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.GONE);
        }
    }
    public void showProgress(ProgressBar progressBar) {
        if (progressBar.getVisibility() == View.GONE){
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void updateNotificationStatus(Notifications notification,
                                         String userId){
        Map<String, Object> updateNotificationMap = new HashMap<>();
        updateNotificationMap.put(STATUS, 1);
        this.getDb().collection(USERS + "/" + userId + "/" + NOTIFICATIONS)
                .document(notification.getDoc_id()).update(updateNotificationMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: notifications updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update notifications\n" +
                                e.getMessage());
                    }
                });
    }

    public void handlePostsView(Context context, Activity activity, RecyclerView recyclerView) {

        Log.d(TAG, "handlePostsView: ");
        if ((context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            // on a large screen device ...
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                    2, StaggeredGridLayoutManager.VERTICAL));

        } else if ((context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            //on xlarge device
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                    3, StaggeredGridLayoutManager.VERTICAL));
        } else {
            //on small, normal or undefined screen devices
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        }
    }

    public int generateRandomInt(int from, int to) {
//        return new Random().nextInt(bound);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return  ThreadLocalRandom.current().nextInt(from, to + 1);
        }else{
            return new Random().nextInt((to - from) + 1) + from;
        }
    }

    public void updateUserCredit(String userId, final int credit){
        Map<String, Object> creditMap = new HashMap<>();
        creditMap.put(CREDIT, credit);
        this.getDb().collection(USERS).document(userId).update(creditMap).addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: user credit updated\nnew credit is " + credit);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "onFailure: failed to update user credit\n" + e.getMessage(), e);
                    }
                });
    }
}

