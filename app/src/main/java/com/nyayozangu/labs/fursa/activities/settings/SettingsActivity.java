package com.nyayozangu.labs.fursa.activities.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.ViewImageActivity;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
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

    private Button adminButton;

    private android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.settingsToolbar);

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

        adminButton = findViewById(R.id.adminButton);

        //handle toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //handle logout
        if (coMeth.isLoggedIn()) {

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d(TAG, "onClick: at onclick login");
                    //confirm sign out
                    confirmSignOut();
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
                    finish();
                }
            });
        }

        //edit profile
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                goToAccSet();
                finish();

            }
        });


        //check is user is logged in
        if (coMeth.isLoggedIn()) {
            //get current user is
            final String userId = coMeth.getUid();
            coMeth.getDb()
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
                                    final String userProfileImageDownloadUrl = documentSnapshot.get("image").toString();

                                    coMeth.setImage(R.drawable.ic_action_person_placeholder,
                                            userProfileImageDownloadUrl,
                                            userImage);

                                    //open profile image
                                    userImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            Intent userImageIntent = new Intent(SettingsActivity.this, ViewImageActivity.class);
                                            userImageIntent.putExtra("imageUrl", userProfileImageDownloadUrl);
                                            startActivity(userImageIntent);

                                        }
                                    });

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

        //handle my posts
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

        //handle my subscriptions
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

        //handle feedback
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                goToFeedback();

            }
        });

        //handle contact us
        contactUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", getString(R.string.family_email), null));
                startActivity(Intent.createChooser(emailIntent, "Contact us"));

            }
        });

        //handle privacy policy
        privacyPolicyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                goToPrivacyPolicy();

            }
        });

        //handle admin access
        // TODO: 5/6/18 handle admin access
        if (coMeth.isConnected() && coMeth.isLoggedIn()) {

            //get user email address
            FirebaseUser user = coMeth.getAuth().getCurrentUser();
            assert user != null;
            String userEmail = user.getEmail();
            if (userEmail != null) {
                //get admins
                checkAdminAcc(userEmail);
            }


        }

    }

    private void checkAdminAcc(String userEmail) {
        coMeth.getDb()
                .collection("Admins")
                .document(userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<DocumentSnapshot> task) {

                        if (task.isSuccessful() && task.getResult().exists()) {

                            //user is admin
                            //show admin button
                            adminButton.setVisibility(View.VISIBLE);
                            adminButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getAdminPassword(task);


                                }
                            });

                        }

                    }
                });
    }

    private void getAdminPassword(@NonNull final Task<DocumentSnapshot> task) {
        //open dialog to enter password
        AlertDialog.Builder adminBuilder = new AlertDialog.Builder(SettingsActivity.this);
        adminBuilder.setTitle("Admin Login")
                .setIcon(getDrawable(R.drawable.ic_action_person_placeholder));

        //construct the view
        final EditText input = new EditText(SettingsActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        adminBuilder.setView(input)
                .setPositiveButton(getString(R.string.done_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String adminPass = input.getText().toString().trim();
                        //check pass
                        if (adminPass.equals(task.getResult().get("password"))) {

                            //open admin panel
                            startActivity(new Intent(SettingsActivity.this, AdminActivity.class));

                        } else {

                            dialog.dismiss();
                            showSnack(getString(R.string.wrong_password_text));

                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void goToPrivacyPolicy() {
        startActivity(new Intent(this, PrivacyPolicyActivity.class));
    }

    private void goToFeedback() {
        startActivity(new Intent(this, FeedbackActivity.class));
    }

    private void goToAccSet() {
        startActivity(new Intent(this, AccountActivity.class));
    }

    private void confirmSignOut() {
        AlertDialog.Builder confirmLogoutBuilder = new AlertDialog.Builder(SettingsActivity.this);
        confirmLogoutBuilder.setTitle(getString(R.string.logout_text))
                .setIcon(getDrawable(R.drawable.ic_action_red_alert))
                .setMessage(getString(R.string.confirm_lougout_text))
                .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .setPositiveButton(getString(R.string.logout_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        coMeth.signOut();
                        Log.d(TAG, "user is logged out");
                        goToMain();


                    }
                })
                .show();
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
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
        if (coMeth.isLoggedIn()) {

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    confirmSignOut();

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
                    finish();
                }
            });
        }

    }


    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(SettingsActivity.this);
        loginAlertBuilder.setTitle("Login")
                .setIcon(getDrawable(R.drawable.ic_action_red_alert))
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

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.settingsLayout),
                message, Snackbar.LENGTH_LONG).show();
    }

}
