package com.nyayozangu.labs.fursa.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.helpers.Notify;
import com.nyayozangu.labs.fursa.models.User;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;


import static com.nyayozangu.labs.fursa.helpers.CoMeth.DESTINATION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWERS_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWING;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWING_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MESSAGE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MY_POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MY_POSTS_DOC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NEW_FOLLOWERS_UPDATE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SUBSCRIPTIONS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TIMESTAMP;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_POSTS;

public class UserPageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserPageActivity";
    private CoMeth coMeth = new CoMeth();

    private TextView usernameField, userBioField;
    private ImageView userImageView;
    private Button postsButton, followButton, followersButton, followingButton, creditButton;
    private String userId, userImageUrl, currentUserId;
    private ProgressDialog progressDialog;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private CollectionReference followersRef;
    private CollectionReference followingRef;
    private CollectionReference curUserFollowingRef;
    private boolean isFollowing = false;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: setting content");
        setContentView(R.layout.activity_user_page);

        //initiate items
        usernameField = findViewById(R.id.userPageUsernameTextView);
        userBioField = findViewById(R.id.userPageUserBioTextView);
        userImageView = findViewById(R.id.userPageUserImage);
        postsButton = findViewById(R.id.userPagePostsButton);
        followersButton = findViewById(R.id.userPageFollowersButton);
        followingButton = findViewById(R.id.userPageFollowingButton);
        followButton = findViewById(R.id.userPageFollowButton);
        creditButton = findViewById(R.id.userPageCreditCountButton);
        toolbar = findViewById(R.id.userPageToolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.username_post_placeholder_text));
        actionBar.setDisplayHomeAsUpEnabled(true);

        database = coMeth.getDb();

        handleBackBehaviour();

        showProgress(getResources().getString(R.string.loading_text));
        checkConnectivity();
        currentUserId = coMeth.getUid();
        handleIntent();
        followersRef = database.collection(USERS + "/" + userId + "/" + FOLLOWERS);
        followingRef = database.collection(USERS + "/" + userId + "/" + FOLLOWING);
        curUserFollowingRef =
                database.collection(USERS + "/" + currentUserId + "/" + FOLLOWING);

        handleFollowButton();

        postsButton.setOnClickListener(this);
        userImageView.setOnClickListener(this);
        followButton.setOnClickListener(this);
        followingButton.setOnClickListener(this);
        followersButton.setOnClickListener(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem editProfileButton = menu.findItem(R.id.userPageEditProfileMenuItem);

        if (coMeth.isLoggedIn()){
            if (coMeth.getUid().equals(userId)){
                editProfileButton.setVisible(true);
            }else{
                editProfileButton.setVisible(false);
            }
        }else{
            editProfileButton.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_page_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.userPageEditProfileMenuItem:
                goToAccSettings();
                break;
            case R.id.userPageShareMenuItem:
                shareProfile();
                break;
        }
        return true;
    }

    private void checkConnectivity() {
        if (!coMeth.isConnected(this)) {
            coMeth.stopLoading(progressDialog);
            showSnack(getResources().getString(R.string.failed_to_connect_text));
        }
    }

    private void handleIntent() {
        Log.d(TAG, "getPostId: user page");
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(USER_ID) != null) {
                userId = intent.getStringExtra(USER_ID);
                Log.d(TAG, "getPostId: userId is " + userId);
                populatePage();
                handleItemVisibility(userId);
            } else if (intent.getAction() != null) {
                userId = handleDeepLink(getIntent());
                populatePage();
            } else {
                goToMain(getString(R.string.something_went_wrong_text));
            }
        }else{
            goToMain(getResources().getString(R.string.something_went_wrong_text));
        }
    }

    public void handleBackBehaviour(){
        Intent intent = getIntent();
        if (intent != null) {
            String appLinkAction = intent.getAction();
            Uri appLinkData = intent.getData();
            if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToMain();
                    }
                });
            }else{
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        }
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private String handleDeepLink(Intent intent) {

        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();

        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            String profileUrl = String.valueOf(appLinkData);
            int endOfUrlHead = getResources().getString(R.string.fursa_url_profile_head).length();
            userId = profileUrl.substring(endOfUrlHead);
            Log.i(TAG, "incoming user id is " + userId);
        }
        return userId;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        handleIntent();
        handleBackBehaviour();
    }

    private void handleItemVisibility(String userId) {
        if (coMeth.isLoggedIn() && userId.equals(currentUserId)) {
            followButton.setVisibility(View.GONE);
            creditButton.setVisibility(View.VISIBLE);
        } else {
            followButton.setVisibility(View.VISIBLE);
            creditButton.setVisibility(View.GONE);
        }
    }

    //get posts count
    private void handlePostsCount() {
        database.collection(USERS + "/" + userId + "/" + SUBSCRIPTIONS + "/"
                        + MY_POSTS_DOC + "/" + MY_POSTS)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e == null) {
                            assert queryDocumentSnapshots != null;
                            if (!queryDocumentSnapshots.isEmpty()) {
                                //user has posts
                                int postCount = queryDocumentSnapshots.size();
                                if (postCount == 0) {
                                    postsButton.setVisibility(View.GONE);
                                } else {
                                    postsButton.setVisibility(View.VISIBLE);
                                    String postsMessage = String.valueOf(postCount) + "\n" +
                                            getResources().getString(R.string.posts_text);
                                    postsButton.setText(postsMessage);
                                }
                                updateUserPostCount(userId, postCount);
                            } else {
                                //user has no posts
                                Log.d(TAG, "onEvent: user has no posts");
                            }
                        }else{
                            Log.d(TAG, "onEvent: error handling posts count\n" + e.getMessage());
                        }
                    }
                });
    }

    private void updateUserPostCount(String userId, int postCount) {
        Map<String, Object> postCountMap = new HashMap<>();
        postCountMap.put(POSTS_VAL, postCount);
        database.collection(USERS).document(userId).update(postCountMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: posts count updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update posts count");
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleBackBehaviour();
    }

    private void populatePage() {
        Log.d(TAG, "populatePage: ");
        database.collection(USERS).document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                String username = user.getName();
                                actionBar.setTitle(username);
                                usernameField.setText(username);
                                String postsButtonText = "View " + username + "\'s posts";
                                postsButton.setText(postsButtonText);
                                String bio = user.getBio();
                                userBioField.setText(bio);
                                userImageUrl = null;
                                if (user.getThumb() != null) {
                                    userImageUrl = user.getThumb();
                                    setImage(userImageUrl);
                                } else if (user.getImage() != null) {
                                    userImageUrl = user.getImage();
                                    setImage(userImageUrl);
                                } else {
                                    userImageView.setImageDrawable(getResources().getDrawable(
                                            R.drawable.ic_action_person_placeholder));
                                }
                                handlePostsCount();
                                handleFCounts(followersRef, followersButton, FOLLOWERS);
                                handleFCounts(followingRef, followingButton, FOLLOWING);
                                int credit = user.getCredit();
                                String creditInfo = getString(R.string.balance_text) + ": " +
                                        credit + " " + getString(R.string.credit_text);
                                creditButton.setText(creditInfo);
                                coMeth.stopLoading(progressDialog);
                            }
                        } else {
                            coMeth.stopLoading(progressDialog);
                            goToMain(getString(R.string.user_not_found_text));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get user data " + e.getMessage());
                        coMeth.stopLoading(progressDialog);
                        String errorMessage = getResources().getString(R.string.error_text) + ": " + e.getMessage();
                        Snackbar.make(findViewById( R.id.userPageLayout),
                                errorMessage, Snackbar.LENGTH_LONG)
                                .setAction(getResources().getString(R.string.reload_text),
                                        new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showProgress(getResources().getString(R.string.reloading_text));
                                        populatePage();
                                    }
                                })
                                .setActionTextColor(getResources().getColor(R.color.secondaryLightColor))
                                .show();
                    }
                });
    }

    private void handleFCounts(CollectionReference reference, final Button button, final String target) {
        reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e == null){
                    if (queryDocumentSnapshots != null) {
                        if (queryDocumentSnapshots.isEmpty()){
                            button.setVisibility(View.GONE);
                        }else{
                            int followersCount = queryDocumentSnapshots.size();
                            String countMessage;
                            switch (target){
                                case FOLLOWING:
                                     countMessage = String.valueOf(followersCount) + "\n" +
                                            getResources().getString(R.string.following_text);
                                    button.setText(countMessage);
                                    break;
                                case FOLLOWERS:
                                     countMessage = String.valueOf(followersCount) + "\n" +
                                            getResources().getString(R.string.followers_text);
                                    button.setText(countMessage);
                                    break;
                                default:
                                    Log.d(TAG, "onEvent: deault at hanlde F count");
                            }

                            button.setVisibility(View.VISIBLE);
                        }
                    }
                }else{
                    Log.d(TAG, "onEvent: failed to get followers");
                    showSnack(getResources().getString(R.string.error_text) + ": " + e.getMessage());
                }
            }
        });
    }

    /**
     * Open the MainActivity when the user is not found
     * or when loading user data failed
     *
     * @param message the message to pass on to the main activity
     */
    private void goToMain(String message) {
        Intent goToMainIntent = new Intent(this, MainActivity.class);
        goToMainIntent.putExtra(getResources().getString(R.string.ACTION_NAME),
                getResources().getString(R.string.NOTIFY_VAL));
        goToMainIntent.putExtra(getResources().getString(R.string.MESSAGE_NAME), message);
        startActivity(goToMainIntent);
        finish();
    }

    /**
     * set an image to an image view from a provided download url
     *
     * @param userImageUrl the user image download url
     */
    private void setImage(final String userImageUrl) {
        coMeth.setCircleImage(R.drawable.ic_action_person_placeholder,
                userImageUrl, userImageView, this);
        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openImageIntent = new Intent(
                        UserPageActivity.this, ViewImageActivity.class);
                openImageIntent.putExtra(CoMeth.IMAGE_URL, userImageUrl);
                startActivity(openImageIntent);
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.userPagePostsButton:
                if (coMeth.isConnected(this))
                { goToUserPosts(); }
                else { showSnack(getResources().getString(R.string.failed_to_connect_text)); }
                break;
            case R.id.userPageFollowButton:
                if (coMeth.isLoggedIn()){
                    handleFollowAction();
                }else{
                    goToLogin(getString(R.string.login_to_follow));
                }
                break;
            case R.id.userPageFollowersButton:
                goToUsersPage(FOLLOWERS_VAL);
                break;
            case R.id.userPageFollowingButton:
                goToUsersPage(FOLLOWING_VAL);
                break;
            default:
                Log.d(TAG, "onClick: at user page click listener default");
        }
    }

    private void goToUsersPage(String destination) {
        Intent intent = new Intent(this, UsersActivity.class);
        intent.putExtra(DESTINATION, destination);
        intent.putExtra(USER_ID, userId);
        startActivity(intent);
    }

    private void handleFollowAction() {
        if (isFollowing){
            confirmUnFollowPage();
        }else {
            followPage();
        }
    }

    private void confirmUnFollowPage() {
        AlertDialog.Builder unFollowBuilder = new AlertDialog.Builder(this);
        unFollowBuilder.setMessage(R.string.confirm_unfollow_text)
                .setNegativeButton(getResources().getString(R.string.cancel_text),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.unfollow_text),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        unFollowPage();
                    }
                })
                .show();
    }

    private void unFollowPage() {
        followersRef.document(currentUserId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: unfollowed user");
                        removeFollowRef();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: filed to unfollow\n" + e.getMessage());
                        showSnack(getResources().getString(R.string.failed_to_unfollow_text) + ": " +
                                e.getMessage());
                    }
                });
    }

    private void removeFollowRef() {
        curUserFollowingRef.document(userId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: deleted the follow ref on current user");
                        unSubscribeToTopic();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to delete follow ref on cur user\n" +
                                e.getMessage());
                        showSnack(getResources().getString(R.string.error_text) + ": " +
                                e.getMessage());
                    }
                });
    }

    private void unSubscribeToTopic() {
        String topic = userId + FOLLOWERS_VAL;
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
    }

    private void followPage() {
        Map<String, Object> followerMap = new HashMap<>();
        followerMap.put(TIMESTAMP, FieldValue.serverTimestamp());
        followerMap.put(USER_ID_VAL, currentUserId);
        followersRef.document(currentUserId).set(followerMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addFollowRef();
                        Log.d(TAG, "onSuccess: followed added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to followPage page\n"+ e.getMessage());
                        showSnack(getResources().getString(R.string.failed_to_follow_text) + ": " +
                                e.getMessage());
                    }
                });
    }

    private void notifyFollow() {
        //send notification to user page user with current user user name
        String mTopic = userId + NEW_FOLLOWERS_UPDATE;
        new Notify().execute(NEW_FOLLOWERS_UPDATE, mTopic, currentUserId);
    }

    private void addFollowRef() {
        Map<String, Object> followingMap = new HashMap<>();
        followingMap.put(USER_ID_VAL, userId);
        followingMap.put(TIMESTAMP, FieldValue.serverTimestamp());
        curUserFollowingRef.document(userId).set(followingMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: following ref added to current user");
                        subscribeToUser();
                        notifyFollow();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to add reference to current user\n" +
                                e.getMessage() );
                        showSnack(getResources().getString(R.string.error_text) + ": " +
                                e.getMessage());
                    }
                });
    }

    private void subscribeToUser() {
        String topic = userId + FOLLOWERS_VAL;
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }

    private void handleFollowButton(){
        if (coMeth.isLoggedIn()) {
            followersRef.document(currentUserId).addSnapshotListener(this,
                    new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e == null) {
                                if (documentSnapshot != null) {
                                    if (documentSnapshot.exists()) {
                                        followButton.setText(getResources().getString(R.string.following_text));
                                        followButton.setBackground(getResources().getDrawable(
                                                        R.drawable.button_shape));
                                        followButton.setTextColor(getResources().getColor(
                                                R.color.primaryDarkColor));
                                        isFollowing = true;
                                    } else {
                                        followButton.setText(getResources().getString(R.string.follow_text));
                                        followButton.setBackground(
                                                getResources().getDrawable(
                                                        R.drawable.follow_button_shape));
                                        followButton.setTextColor(getResources().getColor(
                                                R.color.primaryTextColor));
                                        isFollowing = false;
                                    }
                                }
                            } else {
                                Log.d(TAG, "onEvent: failed to listen to followers\n" + e.getMessage());
                            }
                        }
                    });
        }
    }

    private void goToLogin(String message) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(MESSAGE, message);
        startActivity(intent);
    }

    /**
     * share a deep link to the profile page
     */
    private void shareProfile() {
        Log.d(TAG, "shareProfile: ");

        //show loading
        showProgress(getResources().getString(R.string.loading_text));
        //create app url
        String profileUrl = getResources().getString(R.string.fursa_url_profile_head) + userId;
        final String fullShareMsg = getBio();

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(profileUrl))
                .setDynamicLinkDomain(getString(R.string.dynamic_link_domain))
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(coMeth.minVerCode)
                        .setFallbackUrl(Uri.parse(getString(R.string.playstore_url)))
                        .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(usernameField.getText().toString())
                                .setDescription(getBio())
                                .setImageUrl(Uri.parse(getProfileImageUrl()))
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                            @Override
                            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                if (task.isSuccessful()) {
                                    Uri shortLink = task.getResult().getShortLink();
                                    Log.d(TAG, "onComplete: short link is: " + shortLink);

                                    //show share dialog
                                    String shareText = fullShareMsg + "\n" + shortLink;
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                                    coMeth.stopLoading(progressDialog);
                                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with_text)));
                                } else {
                                    coMeth.stopLoading(progressDialog);
                                    showSnack(getString(R.string.failed_to_share_text) + " profile");
                                }
                            }
                });
    }

    private String getBio() {
        if (userBioField.getText().toString().isEmpty()) {
            //bio is empty
            return getResources().getString(R.string.checkout_my_page_text);
        } else {
            return getResources().getString(R.string.checkout_my_page_text)
                    + "\n" + userBioField.getText().toString();
        }
    }

    private void goToAccSettings() {
        startActivity(new Intent(this, AccountActivity.class));
        finish();
    }

    private void goToUserPosts() {
        Intent goToUserPostsIntent = new Intent(
                UserPageActivity.this, UserPostsActivity.class);
        goToUserPostsIntent.putExtra(USER_ID, userId);
        goToUserPostsIntent.putExtra(DESTINATION, USER_POSTS);
        goToUserPostsIntent.putExtra(CoMeth.USERNAME, usernameField.getText().toString());
        startActivity(goToUserPostsIntent);
    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.userPageLayout),
                message, Snackbar.LENGTH_LONG).show();
    }

    public String getProfileImageUrl() {
        if (userImageUrl != null) {
            return userImageUrl;
        } else {
            return getResources().getString(R.string.app_icon_url);
        }
    }
}
