package com.nyayozangu.labs.fursa.users;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.ViewImageActivity;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.settings.UserPostsActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserPageActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: 5/30/18 add suer posts
    // TODO: 5/30/18 delete user posts page
    // TODO: 5/30/18 add curr user subs
    // TODO: 5/30/18 delte my subs activity

    private static final String TAG = "Sean";
    private TextView usernameField, userBioField, userPostsCountField;
    private CircleImageView userImageView;
    private Button userPostsButton;
    private Toolbar toolbar;
    private String userId;
    private CoMeth coMeth = new CoMeth();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        //initiate items
        usernameField = findViewById(R.id.userPageUsernameTextView);
        userBioField = findViewById(R.id.userPageUserBioTextView);
        userPostsCountField = findViewById(R.id.userPagePostsCountTextView);
        userImageView = findViewById(R.id.userPageUserImage);
        userPostsButton = findViewById(R.id.userPagePostsButton);
        toolbar = findViewById(R.id.userPageToolbar);

        //handle tooolbar
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
        //handle intent
        if (getIntent() != null) {
            handleIntent();
        } else {
            //close the page if the intent is null
            finish();
        }

    }

    private void handleIntent() {
        //get userId
        if (getIntent().getStringExtra("userId") != null) {
            userId = getIntent().getStringExtra("userId");
            //set page data
            populatePage();
            coMeth.stopLoading(progressDialog);
        } else {
            goToMainOnException(getString(R.string.something_went_wrong_text));
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
                                goToMainOnException(getString(R.string.user_not_found_text));
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: failed to get user data " + e.getMessage());
                            goToMainOnException(getString(R.string.something_went_wrong_text));
                        }
                    });

        } catch (Exception e) {
            Log.d(TAG, "populatePage: exception caught" + e.getMessage());
            goToMainOnException(getString(R.string.something_went_wrong_text));
        }

    }

    /**
     * Open the MainActivity when the user is not found
     * or when loading user data failed
     *
     * @param message the message to pass on to the main activity
     */
    private void goToMainOnException(String message) {
        Intent goToMainIntent = new Intent(
                UserPageActivity.this, MainActivity.class);
        goToMainIntent.putExtra("action", "notify");
        goToMainIntent.putExtra("message",
                message);
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
            default:
                Log.d(TAG, "onClick: at user page click listener default");

        }

    }

    private void goToUserPosts() {
        Intent goToUserPostsIntent = new Intent(
                UserPageActivity.this, UserPostsActivity.class);
        goToUserPostsIntent.putExtra("userId", userId);
        startActivity(goToUserPostsIntent);
    }
}
