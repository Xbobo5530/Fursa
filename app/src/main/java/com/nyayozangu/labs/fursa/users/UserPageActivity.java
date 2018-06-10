package com.nyayozangu.labs.fursa.users;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.ViewImageActivity;
import com.nyayozangu.labs.fursa.activities.categories.ViewCategoryActivity;
import com.nyayozangu.labs.fursa.activities.categories.models.Categories;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.settings.AccountActivity;
import com.nyayozangu.labs.fursa.activities.settings.UserPostsActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserPageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Sean";
    private TextView usernameField, userBioField, postsCountField, catsCountField;
    private CircleImageView userImageView;
    private Button userPostsButton, editProfileButton, catSubsButton, logoutButton, shareProfileButton;
    private ImageView editProfileIcon, catSubsIcon, postsIcon, shareProfileIcon;
    private ConstraintLayout logoutCard;
    private CardView logoutCardView;
    private String userId, userImageUrl;
    private CoMeth coMeth = new CoMeth();
    private ProgressDialog progressDialog;
    private String[] catsListItems;
    private ArrayList<String> catSubsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        //initiate items
        usernameField = findViewById(R.id.userPageUsernameTextView);
        userBioField = findViewById(R.id.userPageUserBioTextView);
        userImageView = findViewById(R.id.userPageUserImage);

        editProfileButton = findViewById(R.id.userPageEditProfileButton);
        editProfileIcon = findViewById(R.id.userPageEditProfileImageView);

        catSubsButton = findViewById(R.id.userPageCatsButton);
        catSubsIcon = findViewById(R.id.userPageCatsImageView);
        catsCountField = findViewById(R.id.userPageCatsCountTextView);

        logoutCard = findViewById(R.id.userPageLogoutCard);
        logoutCardView = findViewById(R.id.userPageLogoutCardView);
        logoutButton = findViewById(R.id.userPageLogoutButton);

        userPostsButton = findViewById(R.id.userPagePostsButton);
        postsIcon = findViewById(R.id.userPageUserPostsImageView);
        postsCountField = findViewById(R.id.userPagePostsCountTextView);

        shareProfileButton = findViewById(R.id.userPageShareProfileButton);
        shareProfileIcon = findViewById(R.id.userPageShareImageView);

        Toolbar toolbar = findViewById(R.id.userPageToolbar);

        catSubsArray = new ArrayList<>();

        if (coMeth.isConnected()) {
            //handle intent
            if (getIntent() != null) {
                handleIntent();
            } else {
                //close the page if the intent is null
                goToMain(getResources().getString(R.string.something_went_wrong_text));
            }
        } else {
            //device is not connected
            showSnack(getResources().getString(R.string.failed_to_connect_text));
        }

        //handle toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.username_post_placeholder_text));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 6/3/18 check if stack has content
                finish();
            }
        });


        //handle clicks
        userPostsButton.setOnClickListener(this);
        userImageView.setOnClickListener(this);
        editProfileButton.setOnClickListener(this);
        editProfileIcon.setOnClickListener(this);
        catSubsButton.setOnClickListener(this);
        catSubsIcon.setOnClickListener(this);
        logoutCard.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        shareProfileIcon.setOnClickListener(this);
        shareProfileButton.setOnClickListener(this);

    }

    private void handleIntent() {
        //get userId
        if (getIntent().getStringExtra("userId") != null) {
            userId = getIntent().getStringExtra("userId");
            //set page data
            populatePage();
            //handle button visibility
            handleItemVisibility(userId);
            coMeth.stopLoading(progressDialog);
        } else if (getIntent().getAction() != null) {
            //handle get intent
            userId = handleDeepLink(getIntent());
            //set page data
            populatePage();
        } else {
            goToMain(getString(R.string.something_went_wrong_text));
        }

        /*Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();*/
    }

    private String handleDeepLink(Intent intent) {

        // handle app links
        Log.i(TAG, "at handleDeepLinkIntent");
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
    }

    /**
     * handle items to show when current is viewing page
     */
    private void handleItemVisibility(String userId) {

        //check if user is logged in
        if (coMeth != null && userId.equals(coMeth.getUid())) {

            //user is logged and is current user
            //show edit
            editProfileButton.setVisibility(View.VISIBLE);
            editProfileIcon.setVisibility(View.VISIBLE);
            //show subs
            catSubsIcon.setVisibility(View.VISIBLE);
            catSubsButton.setVisibility(View.VISIBLE);
            //show logout
            logoutCardView.setVisibility(View.VISIBLE);
            //setuser posts button to my posts
            userPostsButton.setText(getResources().getString(R.string.my_posts_text));

        } else {

            //use is not logged in or is not current user
            //hide edit
            editProfileButton.setVisibility(View.GONE);
            editProfileIcon.setVisibility(View.GONE);
            ///hide subs
            catSubsIcon.setVisibility(View.GONE);
            catSubsButton.setVisibility(View.GONE);
            //hide logout
            logoutCardView.setVisibility(View.GONE);
        }
    }

    //get posts count
    private void handlePostsCount(final String userId) {
        Log.d(TAG, "handlePostsCount: ");
        coMeth.getDb()
                .collection("Users/" + userId + "/Subscriptions/my_posts/MyPosts")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //user has posts
                            int postCount = queryDocumentSnapshots.size();
                            if (postCount == 0) {
                                userPostsButton.setVisibility(View.GONE);
                                postsIcon.setVisibility(View.GONE);
                                postsCountField.setVisibility(View.GONE);
                            } else {

                                userPostsButton.setVisibility(View.VISIBLE);
                                postsIcon.setVisibility(View.VISIBLE);
                                postsCountField.setVisibility(View.VISIBLE);

                                postsCountField.setText(String.valueOf(postCount));
                            }
                            updateUserPostCount(userId, postCount);
                        } else {
                            //user has no posts
                            Log.d(TAG, "onEvent: user has no posts");
                        }
                    }
                });
    }

    private void updateUserPostCount(String userId, int postCount) {
        Map<String, Object> postCountMap = new HashMap<>();
        postCountMap.put("posts", postCount);
        coMeth.getDb()
                .collection("Users")
                .document(userId)
                .update(postCountMap)
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

    //get subs count
    private void handleCatsCount(final String userId) {
        Log.d(TAG, "handleCatsCount: ");
        coMeth.getDb()
                .collection("Users/" + userId + "/Subscriptions/categories/Categories")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //user has cats
                            int catsCount = queryDocumentSnapshots.size();
                            if (catsCount == 0) {
                                catsCountField.setVisibility(View.GONE);
                                catSubsButton.setVisibility(View.GONE);
                                catSubsIcon.setVisibility(View.GONE);
                            } else {

                                catsCountField.setVisibility(View.VISIBLE);
                                catSubsButton.setVisibility(View.VISIBLE);
                                catSubsIcon.setVisibility(View.VISIBLE);

                                catsCountField.setText(String.valueOf(catsCount));
                            }
                            udpateUserCatsCount(userId, catsCount);

                        } else {
                            //user has no cats
                            Log.d(TAG, "onEvent: user has no cats");
                        }
                    }
                });
    }

    private void udpateUserCatsCount(String userId, int catsCount) {
        //create catsCountMap
        Map<String, Object> catsCountMap = new HashMap<>();
        catsCountMap.put("categories", catsCount);
        //update the user profile
        coMeth.getDb()
                .collection("Users")
                .document(userId)
                .update(catsCountMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: update successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update catsCount\n" +
                                e.getMessage());
                    }
                });
    }

    private void populatePage() {

        //show progress
        showProgress(getResources().getString(R.string.loading_text));
        coMeth.getDb()
                .collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //convert doc to User object
                            Users user = documentSnapshot.toObject(Users.class);
                            //set username
                            String username = user.getName();
                            //set toolbar title
                            getSupportActionBar().setTitle(username);
                            //set username field
                            usernameField.setText(username);
                            //set username to posts button
                            String postsButtonText = "View " + username + "\'s posts";
                            userPostsButton.setText(postsButtonText);
                            //get bio
                            String bio = user.getBio();
                            userBioField.setText(bio);
                            //get user thumb
                            userImageUrl = null;
                            if (user.getThumb() != null) {
                                userImageUrl = user.getThumb();
                                setImage(userImageUrl);
                            } else if (user.getImage() != null) {
                                userImageUrl = user.getImage();
                                setImage(userImageUrl);
                            } else {
                                //no user image
                                userImageView.setImageDrawable(
                                        getResources().getDrawable(R.drawable.appiconshadow));
                            }
                            //set post count
                            handlePostsCount(userId);
                            //set categories
                            handleCatsCount(userId);
                            coMeth.stopLoading(progressDialog);

                        } else {
                            //user does not exist
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
                        goToMain(getString(R.string.something_went_wrong_text));
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
        try {
            coMeth.setImage(R.drawable.ic_action_person_placeholder,
                    userImageUrl,
                    userImageView);
        } catch (Exception settingImageException) {
            Log.d(TAG, "onSuccess: setting image exception " +
                    settingImageException.getMessage());
        }
        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openImageIntent = new Intent(
                        UserPageActivity.this, ViewImageActivity.class);
                openImageIntent.putExtra("imageUrl", userImageUrl);
                startActivity(openImageIntent);
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.userPagePostsButton:
                if (coMeth.isConnected()) {
                    goToUserPosts();
                } else {
                    showSnack(getResources().getString(R.string.failed_to_connect_text));
                }
                break;
            case R.id.userPageLogoutCard:
                coMeth.signOut();
                goToMain(getString(R.string.logged_out_text));
                break;
            case R.id.userPageLogoutButton:
                coMeth.signOut();
                goToMain(getString(R.string.logged_out_text));
                break;
            case R.id.userPageEditProfileButton:
                if (coMeth.isConnected()) {
                    goToAccSettings();
                } else {
                    showSnack(getResources().getString(R.string.failed_to_connect_text));
                }
                break;
            case R.id.userPageEditProfileImageView:
                if (coMeth.isConnected()) {
                    goToAccSettings();
                } else {
                    showSnack(getResources().getString(R.string.failed_to_connect_text));
                }
                break;
            case R.id.userPageCatsButton:
                if (coMeth.isConnected()) {
                    showCatsDialog();
                } else {
                    showSnack(getResources().getString(R.string.failed_to_connect_text));
                }
                break;
            case R.id.userPageCatsImageView:
                if (coMeth.isConnected()) {
                    showCatsDialog();
                } else {
                    showSnack(getResources().getString(R.string.failed_to_connect_text));
                }
                break;
            case R.id.userPageShareProfileButton:
                if (coMeth.isConnected()) {
                    shareProfile();
                } else {
                    if (coMeth.isConnected()) {
                        showCatsDialog();
                    } else {
                        showSnack(getResources().getString(R.string.failed_to_connect_text));
                    }
                    break;
                }
                break;
            default:
                Log.d(TAG, "onClick: at user page click listener default");
        }
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

        Task<ShortDynamicLink> shortLinkTask =
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
                                    Uri flowchartLink = task.getResult().getPreviewLink();
                                    Log.d(TAG, "onComplete: short link is: " + shortLink);

                                    //show share dialog
                                    String shareText = fullShareMsg + "\n" + shortLink;
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                                            getResources().getString(R.string.app_name));
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                                    coMeth.stopLoading(progressDialog);
                                    startActivity(Intent.createChooser(
                                            shareIntent, getString(R.string.share_with_text)));
                                } else {
                                    Log.d(TAG, "onComplete: " +
                                            "\ncreating short link task failed\n" +
                                            task.getException());
                                    coMeth.stopLoading(progressDialog);
                                    showSnack(getString(R.string.failed_to_share_text) + " profile");
                                }
                            }
                        });


    }

    /**
     * get the user bio
     * @return bio: String the bio of the user
     */
    private String getBio() {
        if (userBioField.getText().toString().isEmpty()) {
            //bio is empty
            return getResources().getString(R.string.checkout_my_page_text);
        } else {
            return getResources().getString(R.string.checkout_my_page_text)
                    + "\n" + userBioField.getText().toString();
        }
    }

    private void showCatsDialog() {
        Log.d(TAG, "showCatsDialog: ");
        //show progress
        showProgress(getResources().getString(R.string.loading_text));
        //fetch categories
        coMeth.getDb()
                .collection("Users/" + userId + "/Subscriptions/categories/Categories")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots,
                                        FirebaseFirestoreException e) {

                        //check if query is empty
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //user has cats
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Categories cat = doc.getDocument().toObject(Categories.class);
                                    //add cat to list
                                    catSubsArray.add(cat.getValue());
                                }
                            }


                            Log.d(TAG, "onEvent: \ncatSubArray contains: " + catSubsArray);
                            Log.d(TAG, "onClick: \ncatsListItems: " + catsListItems);
                            catsListItems = catSubsArray.toArray((new String[catSubsArray.size()]));

                            //stop loading
                            coMeth.stopLoading(progressDialog);
                            //open an an alert dialog for the sub'd cats
                            android.support.v7.app.AlertDialog.Builder catsSubBuilder =
                                    new android.support.v7.app.AlertDialog.Builder(
                                            UserPageActivity.this);
                            catsSubBuilder.setTitle(getString(R.string.categories_text))
                                    .setIcon(R.drawable.ic_action_categories)
                                    .setItems(catsListItems, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //open the view category activity
                                            openCat(coMeth.getCatKey(catsListItems[which]));
                                        }
                                    })
                                    .show();
                            //empty the catSubsArray
                            catSubsArray.clear();
                        } else {
                            //user has not subd cats
                            //go to cats
                            Intent goToCatsIntent = new Intent(
                                    UserPageActivity.this, MainActivity.class);
                            goToCatsIntent.putExtra(getResources().getString(R.string.ACTION_NAME),
                                    getResources().getString(R.string.GOTO_VAL));
                            goToCatsIntent.putExtra(getResources().getString(R.string.DESTINATION_NAME),
                                    getResources().getString(R.string.CATEGORIES_VAL));

                            coMeth.stopLoading(progressDialog);
                            startActivity(goToCatsIntent);
                            finish();
                        }
                    }
                });
    }

    private void goToAccSettings() {
        startActivity(new Intent(this, AccountActivity.class));
        finish();
    }

    private void goToUserPosts() {
        Intent goToUserPostsIntent = new Intent(
                UserPageActivity.this, UserPostsActivity.class);
        goToUserPostsIntent.putExtra("userId", userId);
        startActivity(goToUserPostsIntent);
    }

    private void showProgress(String message) {

        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();

    }

    private void openCat(String catKey) {
        Intent openCatIntent = new Intent(this, ViewCategoryActivity.class);
        openCatIntent.putExtra("category", catKey);
        startActivity(openCatIntent);
        finish();
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.settingsLayout),
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
