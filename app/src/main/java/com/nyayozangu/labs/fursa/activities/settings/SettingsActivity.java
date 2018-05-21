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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.ViewImageActivity;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private CircleImageView userImage;
    private TextView usernameTextView, userBioTextView;
    private Button logoutButton, editProfileButton, myPostsButton, mySubsButton,
            shareAppButton, feedbackButton, contactUsButton, privacyPolicyButton,
            adminButton;
    private android.support.v7.widget.Toolbar toolbar;
    private ImageView editProfileIcon;

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
        editProfileIcon = findViewById(R.id.settingsEditImageView);

        myPostsButton = findViewById(R.id.settingsPostsButton);
        mySubsButton = findViewById(R.id.settingsSubsButton);

        shareAppButton = findViewById(R.id.settingsShareAppButton);
        feedbackButton = findViewById(R.id.settingsFeedbackButton);
        contactUsButton = findViewById(R.id.settingsContactButton);
        privacyPolicyButton = findViewById(R.id.settingsPolicyButton);

        adminButton = findViewById(R.id.adminButton);

        //handle toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.action_settings));
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
            logoutButton.setText(getString(R.string.login_text));
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //go to log in page
                    goToLogin(getString(R.string.login_text));
                }
            });
        }

        //set user details
        //check is user is logged in
        if (coMeth.isLoggedIn()) {
            //get current user is
            final String userId = coMeth.getUid();
            coMeth.getDb()
                    .collection("Users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                //get object
                                Users user = documentSnapshot.toObject(Users.class);
                                //set name
                                String username = user.getName();
                                usernameTextView.setText(username);
                                //set bio
                                String bio = user.getBio();
                                if (bio != null) {
                                    userBioTextView.setText(bio);
                                } else {
                                    Log.d(TAG, "error: no bio");
                                    userBioTextView.setVisibility(View.GONE);
                                }
                                //set image
                                try {
                                    final String userProfileImageDownloadUrl = user.getImage();

                                    coMeth.setImage(R.drawable.ic_action_person_placeholder,
                                            userProfileImageDownloadUrl,
                                            userImage);

                                    //open profile image
                                    userImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            Intent userImageIntent = new Intent(
                                                    SettingsActivity.this, ViewImageActivity.class);
                                            userImageIntent.putExtra("imageUrl", userProfileImageDownloadUrl);
                                            startActivity(userImageIntent);

                                        }
                                    });

                                } catch (NullPointerException userImageException) {
                                    //user image is null
                                    Log.e(TAG, "onEvent: ", userImageException);
                                }

                            } else {
                                Log.d(TAG, "user does not exist");
                                // TODO: 5/21/18 code review
                                userImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_person_placeholder));
                            }
                        }
                    });
        } else {

            //user is not logged in
            usernameTextView.setVisibility(View.GONE);
            userBioTextView.setText(getString(R.string.not_logged_in_text));
            userImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_person_placeholder));
            editProfileButton.setVisibility(View.INVISIBLE); //hide the edit profile button
            editProfileIcon.setVisibility(View.GONE); //hide the edit profile icon
        }

        //handle other button clicks
        myPostsButton.setOnClickListener(this);
        mySubsButton.setOnClickListener(this);
        shareAppButton.setOnClickListener(this);
        feedbackButton.setOnClickListener(this);
        contactUsButton.setOnClickListener(this);
        privacyPolicyButton.setOnClickListener(this);
        editProfileButton.setOnClickListener(this);

        privacyPolicyButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(SettingsActivity.this, "Checking for admin access",
                        Toast.LENGTH_SHORT).show();
                //handle admin access
                if (coMeth.isConnected() && coMeth.isLoggedIn()) {
                    try {
                        //get user email address
                        FirebaseUser user = coMeth.getAuth().getCurrentUser();
                        String userEmail = user.getEmail();
                        if (userEmail != null) {
                            //get admins
                            checkAdminAcc(userEmail);
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "onCreate: checking admin access failed " + e.getMessage());
                    }
                }
                return true;
            }
        });
    }

    private void checkAdminAcc(String userEmail) {
        coMeth.getDb()
                .collection("Admins")
                .document(userEmail)
                .get()
                .addOnCompleteListener(SettingsActivity.this, new OnCompleteListener<DocumentSnapshot>() {
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
                .setIcon(getResources().getDrawable(R.drawable.ic_action_person_placeholder));

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

    private void goToLogin(String message) {
        Intent goToLoginIntent = new Intent(this, LoginActivity.class);
        goToLoginIntent.putExtra("message", message);
        startActivity(goToLoginIntent);
        finish();
    }

    private void goToPrivacyPolicy() {
        startActivity(new Intent(this, PrivacyPolicyActivity.class));
    }

    private void goToFeedback() {
        startActivity(new Intent(this, FeedbackActivity.class));
    }

    private void goToAccSet() {
        startActivity(new Intent(this, AccountActivity.class));
        finish();
    }

    private void confirmSignOut() {
        AlertDialog.Builder confirmLogoutBuilder = new AlertDialog.Builder(SettingsActivity.this);
        confirmLogoutBuilder.setTitle(getString(R.string.logout_text))
                .setIcon(getResources().getDrawable(R.drawable.ic_action_red_alert))
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
        //open the my posts page
        Intent goToMyPostsIntent = new Intent(SettingsActivity.this, UserPostsActivity.class);
        goToMyPostsIntent.putExtra("userId", coMeth.getUid());
        startActivity(goToMyPostsIntent);
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
                    goToLogin(getString(R.string.login_text));
                }
            });
        }

    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.settingsLayout),
                message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.settingsPostsButton:
                if (coMeth.isLoggedIn()) {
                    goToMyPosts();
                } else {
                    goToLogin(getString(R.string.login_to_view_post_text));
                }
                break;
            case R.id.settingsSubsButton:
                if (coMeth.isLoggedIn()) {
                    goToMySubs();
                } else {
                    goToLogin(getString(R.string.login_to_view_subs_text));
                }
                break;
            case R.id.settingsShareAppButton:
                shareApp();
                break;
            case R.id.settingsFeedbackButton:
                if (coMeth.isLoggedIn()) {
                    goToFeedback();
                } else {
                    goToLogin(getString(R.string.login_to_feedback));
                }
                break;
            case R.id.settingsContactButton:
                sendEmail();
                break;
            case R.id.settingsPolicyButton:
                goToPrivacyPolicy();
                break;
            case R.id.settingsEditProfileButton:
                goToAccSet();
                break;
            default:
                Log.d(TAG, "onClick: settings onclick at default");
        }
    }

    private void shareApp() {
        Log.d(TAG, "Sharing app");
        //create post url
        String appUrl = getResources().getString(R.string.app_download_url);
        String fullShareMsg = "Download the Fursa app " +
                "to view and share experiences and opportunities with friends\n" + appUrl;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, fullShareMsg);
        startActivity(Intent.createChooser(shareIntent, "Share with"));
    }

    private void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", getString(R.string.family_email), null));
        startActivity(Intent.createChooser(emailIntent, "Contact us"));
    }
}
