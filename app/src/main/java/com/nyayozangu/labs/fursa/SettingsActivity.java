package com.nyayozangu.labs.fursa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "Sean";

    // TODO: 4/8/18 set user profile image
    // TODO: 4/8/18 handle logout action
    // TODO: 4/8/18 handle edit profile button
    // TODO: 4/8/18 handle my posts
    // TODO: 4/8/18 handle mysubscriptions
    // TODO: 4/8/18 handlle contact us
    // TODO: 4/8/18 handle feedback
    // TODO: 4/8/18 handle privacy policy


    private CircleImageView userImage;
    private TextView usernameTextView;
    private TextView userBioTextView;
    private Button logoutButton;
    private Button editProfileButton;

    private Button myPostsButton;
    private Button mySubscription;

    private Button feedbackButton;
    private Button contactUsButton;
    private Button privacyPolicyButton;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        userImage = findViewById(R.id.settingsUserCirleImageView);
        usernameTextView = findViewById(R.id.settingsUsernameTextView);
        userBioTextView = findViewById(R.id.settingsUserBioTextView);
        logoutButton = findViewById(R.id.settingsLogoutButton);
        editProfileButton = findViewById(R.id.settingsEditProfileButton);

        myPostsButton = findViewById(R.id.settingsPostsButton);
        mySubscription = findViewById(R.id.settingsLogoutButton);

        feedbackButton = findViewById(R.id.settingsFeedbackButton);
        contactUsButton = findViewById(R.id.settingsContactButton);
        privacyPolicyButton = findViewById(R.id.settingsPolicyButton);


        //handle logout
        if (mAuth.getCurrentUser() != null) {

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    Log.d(TAG, "user is logged out");
                    goToMain();
                }
            });
        } else {
            //user is signed out
            logoutButton.setText("Login");
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //go to log in page
                    goToLogin();
                }
            });
        }

        //edit profile
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAccSet();
            }
        });


        //set image
        if (isLoggedIn()) {
            //get current user is
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("Users").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    //check if user exists
                    if (documentSnapshot.exists()) {
                        //set username
                        String username = documentSnapshot.get("name").toString();
                        usernameTextView.setText(username);
                        //set bio
                        try {
                            String bio = documentSnapshot.get("bio").toString();
                            userBioTextView.setText(bio);
                        } catch (NullPointerException error) {
                            Log.d(TAG, "error: no bio");
                            userBioTextView.setVisibility(View.GONE);
                        }

                        //set image
                        String userProfileImageDownloadUrl = documentSnapshot.get("image").toString();
                        RequestOptions placeHolderOptions = new RequestOptions();
                        placeHolderOptions.placeholder(R.drawable.ic_thumb_person);

                        Glide.with(SettingsActivity.this).applyDefaultRequestOptions(placeHolderOptions)
                                .load(userProfileImageDownloadUrl).into(userImage);

                    } else {
                        Log.d(TAG, "user does now exist");
                        userImage.setImageDrawable(getDrawable(R.drawable.ic_thumb_person));
                    }
                }
            });
        } else {
            //user is not logged in
            usernameTextView.setVisibility(View.GONE);
            userBioTextView.setText("You are currently not logged in \nclick the login button to log in");
            userImage.setImageDrawable(getDrawable(R.drawable.ic_action_alert));

        }

    }

    private boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        //handle logout
        if (mAuth.getCurrentUser() != null) {

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    Log.d(TAG, "user is logged out");
                    goToMain();
                }
            });
        } else {
            //user is signed out
            logoutButton.setText("Login");
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //go to log in page
                    goToLogin();
                }
            });
        }

    }

    private void goToLogin() {
        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
        finish();
    }

    //go to account settings
    private void goToAccSet() {
        startActivity(new Intent(SettingsActivity.this, AccountActivity.class));
        finish();
    }

    private void goToMain() {
        //go to main page
        //alert user that he is now logged out
        Intent logoutIntent = new Intent(SettingsActivity.this, MainActivity.class);
        logoutIntent.putExtra("error", "You are now logged out...");
        startActivity(logoutIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        finish();
    }
}
