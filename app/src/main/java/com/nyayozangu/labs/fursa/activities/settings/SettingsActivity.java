package com.nyayozangu.labs.fursa.activities.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "Sean";

    // TODO: 4/8/18 handlle contact us
    // TODO: 4/8/18 handle feedback
    // TODO: 4/8/18 handle privacy policy

    private CircleImageView userImage;
    private TextView usernameTextView;
    private TextView userBioTextView;
    private Button logoutButton;
    private Button editProfileButton;

    private Button myPostsButton;
    private Button mySubsButton;

    private Button feedbackButton;
    private Button contactUsButton;
    private Button privacyPolicyButton;

    private android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        userImage = findViewById(R.id.settingsUserCirleImageView);
        usernameTextView = findViewById(R.id.settingsUsernameTextView);
        userBioTextView = findViewById(R.id.settingsUserBioTextView);
        logoutButton = findViewById(R.id.settingsLogoutButton);
        editProfileButton = findViewById(R.id.settingsEditProfileButton);

        myPostsButton = findViewById(R.id.settingsPostsButton);
        mySubsButton = findViewById(R.id.settingsSubsButton);

        feedbackButton = findViewById(R.id.settingsFeedbackButton);
        contactUsButton = findViewById(R.id.settingsContactButton);
        privacyPolicyButton = findViewById(R.id.settingsPolicyButton);


        //handle logout
        if (new CoMeth().isLoggedIn()) {

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CoMeth().signOut();
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


        //check is user is logged in
        if (new CoMeth().isLoggedIn()) {
            //get current user is
            String userId = new CoMeth().getUid();
            new CoMeth().getDb()
                    .collection("Users")
                    .document(userId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    //check if user exists
                    if (documentSnapshot.exists()) {
                        //set name
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
                        try {
                            String userProfileImageDownloadUrl = documentSnapshot.get("image").toString();
                            RequestOptions placeHolderOptions = new RequestOptions();
                            placeHolderOptions.placeholder(R.drawable.ic_action_person_placeholder);

                            Glide.with(getApplicationContext()).applyDefaultRequestOptions(placeHolderOptions)
                                    .load(userProfileImageDownloadUrl).into(userImage);
                        } catch (NullPointerException userImageException) {

                            //user image is null
                            Log.e(TAG, "onEvent: ", userImageException);


                        }

                    } else {
                        Log.d(TAG, "user does now exist");
                        userImage.setImageDrawable(getDrawable(R.drawable.ic_action_person_placeholder));
                    }
                }
            });
        } else {
            //user is not logged in
            usernameTextView.setVisibility(View.GONE);
            userBioTextView.setText("You are currently not logged in \nclick the login button to log in");
            userImage.setImageDrawable(getDrawable(R.drawable.ic_action_person_placeholder));
            editProfileButton.setVisibility(View.INVISIBLE); //hide the edit profile button

        }

        myPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new CoMeth().isLoggedIn()) {

                    //open the users posts page
                    goToMyPosts();

                } else {

                    //user is not logged in
                    String message = getString(R.string.login_to_view_post_text);
                    showLoginAlertDialog(message);

                }
            }
        });

        mySubsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if is logged in
                if (new CoMeth().isLoggedIn()) {

                    //user is logged in
                    goToMySubs();

                } else {

                    //not logged in
                    String message = getString(R.string.login_to_view_subs_text);
                    showLoginAlertDialog(message);

                }

            }
        });

    }

    private void goToMySubs() {

        //open mySubs Page
        startActivity(new Intent(SettingsActivity.this, MySubscriptionsActivity.class));

    }

    private void goToMyPosts() {

        //open the myposts page
        startActivity(new Intent(SettingsActivity.this, MyPostsActivity.class));

    }

    @Override
    protected void onStart() {
        super.onStart();

        //handle logout
        if (new CoMeth().isLoggedIn()) {

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CoMeth().signOut();
                    Log.d(TAG, "user is logged out");
                    goToMain();
                }
            });
        } else {
            //user is signed out
            logoutButton.setText(getString(R.string.login_text));
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


    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(SettingsActivity.this);
        loginAlertBuilder.setTitle("Login")
                .setIcon(getDrawable(R.drawable.ic_action_alert))
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

}
