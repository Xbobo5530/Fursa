package com.nyayozangu.labs.fursa.users;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import de.hdodenhof.circleimageview.CircleImageView;


public class UserPageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Sean";
    private TextView usernameField, userBioField;
    private CircleImageView userImageView;
    private Button userPostsButton, editProfileButton, catSubsButton, logoutButton;
    private ImageView editProfileIcon, catSubsIcon;
    private ConstraintLayout logoutCard;
    private String userId;
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

        logoutCard = findViewById(R.id.userPageLogoutCard);
        logoutButton = findViewById(R.id.userPageLogoutButton);

        userPostsButton = findViewById(R.id.userPagePostsButton);
        Toolbar toolbar = findViewById(R.id.userPageToolbar);

        catSubsArray = new ArrayList<>();

        //handle intent
        if (getIntent() != null) {
            handleIntent();
        } else {
            //close the page if the intent is null
            goToMain(getResources().getString(R.string.something_went_wrong_text));
        }

        //handle toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.username_post_placeholder_text));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        } else {
            goToMain(getString(R.string.something_went_wrong_text));
        }
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
            logoutCard.setVisibility(View.VISIBLE);
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
            logoutCard.setVisibility(View.GONE);
        }

    }

    private void populatePage() {
        try {
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
                                String userImageUrl = null;
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
                            } else {
                                //user does not exist
                                goToMain(getString(R.string.user_not_found_text));
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: failed to get user data " + e.getMessage());
                            goToMain(getString(R.string.something_went_wrong_text));
                        }
                    });

        } catch (Exception e) {
            Log.d(TAG, "populatePage: exception caught" + e.getMessage());
            goToMain(getString(R.string.something_went_wrong_text));
        }

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
                goToUserPosts();
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
                goToAccSettings();
                break;
            case R.id.userPageEditProfileImageView:
                goToAccSettings();
                break;
            case R.id.userPageCatsButton:
                showCatsDialog();
                break;
            case R.id.userPageCatsImageView:
                showCatsDialog();
                break;
            default:
                Log.d(TAG, "onClick: at user page click listener default");

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
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

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
}
