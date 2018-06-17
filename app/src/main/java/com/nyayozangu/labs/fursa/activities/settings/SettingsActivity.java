package com.nyayozangu.labs.fursa.activities.settings;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nyayozangu.labs.fursa.BuildConfig;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.UserPageActivity;
import com.nyayozangu.labs.fursa.users.Users;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    //    private ImageView userImage;
    private CircleImageView userImage;
    private TextView usernameTextView, userBioTextView;
    private Button logoutButton, myProfileButton,
            shareAppButton, aboutButton, contactUsButton, privacyPolicyButton,
            adminButton;
    private android.support.v7.widget.Toolbar toolbar;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.settingsToolbar);

        userImage = findViewById(R.id.settingsImageView);
        myProfileButton = findViewById(R.id.settingsMyProfileButton);
        shareAppButton = findViewById(R.id.settingsShareAppButton);
        aboutButton = findViewById(R.id.settingsAboutButton);
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

        //set user details
        //check is user is logged in
        if (coMeth.isLoggedIn()) {
            //get current user is
            String userId = coMeth.getUid();
            //show view profile button
            myProfileButton.setText(getResources().getString(R.string.my_prifile_text));
        } else {
            //user is not logged in
            myProfileButton.setText(getResources().getString(R.string.login_text));
        }

        //handle other button clicks
        userImage.setOnClickListener(this);
        shareAppButton.setOnClickListener(this);
        aboutButton.setOnClickListener(this);
        contactUsButton.setOnClickListener(this);
        privacyPolicyButton.setOnClickListener(this);
        myProfileButton.setOnClickListener(this);

        //set the user image
        handleUserImage();

        //disable access to admin to release version
        /*privacyPolicyButton.setOnLongClickListener(new View.OnLongClickListener() {
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
        });*/
    }

    private void handleUserImage() {
        Log.d(TAG, "handleUserImage: ");
        if (coMeth.isLoggedIn()) {
            //set user profile image
            coMeth.getDb()
                    .collection(CoMeth.USERS).document(coMeth.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                //user exists
                                Users user = documentSnapshot.toObject(Users.class);
                                String userImageUrl = user.getImage();
                                if (userImageUrl != null) {
                                    coMeth.setImage(R.drawable.ic_action_person_placeholder,
                                            userImageUrl, userImage, Glide.with(SettingsActivity.this));
                                } else {
                                    userImage.setImageDrawable(
                                            getResources().getDrawable(
                                                    R.drawable.ic_action_person_placeholder));
                                }
                            } else {
                                Log.d(TAG, "onSuccess: user does not exist");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: failed to get user\n" + e.getMessage());
                        }
                    });
        } else {
            userImage.setImageDrawable(
                    getResources().getDrawable(R.drawable.appiconshadow));
        }
    }

//    private void checkAdminAcc(String userEmail) {
//        coMeth.getDb()
//                .collection("Admins")
//                .document(userEmail)
//                .get()
//                .addOnCompleteListener(SettingsActivity.this, new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
//
//                        if (task.isSuccessful() && task.getResult().exists()) {
//
//                            //user is admin
//                            //show admin button
//                            adminButton.setVisibility(View.VISIBLE);
//                            adminButton.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    getAdminPassword(task);
//                                }
//                            });
//                        }
//                    }
//                });
//    }

//    private void getAdminPassword(@NonNull final Task<DocumentSnapshot> task) {
//        //open dialog to enter password
//        AlertDialog.Builder adminBuilder = new AlertDialog.Builder(SettingsActivity.this);
//        adminBuilder.setTitle("Admin Login")
//                .setIcon(getResources().getDrawable(R.drawable.ic_action_person_placeholder));
//
//        //construct the view
//        final EditText input = new EditText(SettingsActivity.this);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//        input.setLayoutParams(lp);
//
//        adminBuilder.setView(input)
//                .setPositiveButton(getString(R.string.done_text), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String adminPass = input.getText().toString().trim();
//                        //check pass
//                        if (adminPass.equals(task.getResult().get("password"))) {
//
//                            //open admin panel
//                            startActivity(new Intent(SettingsActivity.this, AdminActivity.class));
//
//                        } else {
//
//                            dialog.dismiss();
//                            showSnack(getString(R.string.wrong_password_text));
//
//                        }
//                    }
//                })
//                .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .show();
//    }

    private void goToLogin(String message) {
        Intent goToLoginIntent = new Intent(this, LoginActivity.class);
        goToLoginIntent.putExtra(getResources().getString(R.string.MESSAGE_NAME), message);
        startActivity(goToLoginIntent);
        finish();
    }

    private void goToPrivacyPolicy() {
        Intent goToTerms = new Intent(this, MainActivity.class);
        goToTerms.putExtra(getResources().getString(R.string.ACTION_NAME),
                getResources().getString(R.string.GOTO_VAL));
        goToTerms.putExtra(getResources().getString(R.string.DESTINATION_NAME),
                getResources().getString(R.string.TERMS_VAL));
        startActivity(goToTerms);
    }

    private void goToUserPage() {
        Intent goToUserPageIntent = new Intent(this, UserPageActivity.class);
        goToUserPageIntent.putExtra("userId", coMeth.getUid());
        startActivity(goToUserPageIntent);
        finish();
    }

//    private void confirmSignOut() {
//        AlertDialog.Builder confirmLogoutBuilder = new AlertDialog.Builder(SettingsActivity.this);
//        confirmLogoutBuilder.setTitle(getString(R.string.logout_text))
//                .setIcon(getResources().getDrawable(R.drawable.ic_action_red_alert))
//                .setMessage(getString(R.string.confirm_lougout_text))
//                .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        dialog.dismiss();
//
//                    }
//                })
//                .setPositiveButton(getString(R.string.logout_text), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        coMeth.signOut();
//                        Log.d(TAG, "user is logged out");
//                        goToMain();
//                    }
//                })
//                .show();
//    }

//    private void goToMain() {
//        startActivity(new Intent(this, MainActivity.class));
//        finish();
//    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.settingsLayout),
                message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.settingsImageView:
                if (coMeth.isLoggedIn()) {
                    goToUserPage();
                } else {
                    goToLogin(getResources().getString(R.string.login_text));
                }
                break;
            case R.id.settingsShareAppButton:
                shareApp();
                break;
            case R.id.settingsAboutButton:
                showVersionInfo();
                break;
            case R.id.settingsContactButton:
                sendEmail();
                break;
            case R.id.settingsPolicyButton:
                goToPrivacyPolicy();
                break;
            case R.id.settingsMyProfileButton:
                if (coMeth.isLoggedIn()) {
                    goToUserPage();
                } else {
                    goToLogin(getResources().getString(R.string.login_text));
                }
                break;
            default:
                Log.d(TAG, "onClick: settings onclick at default");
        }
    }

    private void showVersionInfo() {
        Log.d(TAG, "showVersionInfo: ");
        AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(this);
        Log.d(TAG, "showVersionInfo: aboutBuilder " + aboutBuilder.toString());
        String versionName = BuildConfig.VERSION_NAME;
        Log.d(TAG, "showVersionInfo: versionName " + versionName);
        aboutBuilder.setTitle(getResources().getString(R.string.app_name) + " " + versionName)
                .setIcon(getResources().getDrawable(R.drawable.ic_action_info_grey))
                .setMessage(getResources().getString(R.string.UPDATE_INFO))
                .setPositiveButton(getResources().getString(R.string.rate_text) + " " +
                                getResources().getString(R.string.app_name),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                launchMarket();
                            }
                        })
                .show();
    }

    private void launchMarket() {
        Log.d(TAG, "launchMarket: ");
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            showSnack(getResources().getString(R.string.unable_to_find_playstore_text));
        }
    }

    private void shareApp() {
        Log.d(TAG, "Sharing app");
        //show loading
        showProgress(getResources().getString(R.string.loading_text));
        //create app url
        String appUrl = getResources().getString(R.string.app_download_url);
        final String fullShareMsg = getString(R.string.share_app_mesage_text);

        Task<ShortDynamicLink> shortLinkTask =
                FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse(appUrl))
                        .setDynamicLinkDomain(getString(R.string.dynamic_link_domain))
                        .setSocialMetaTagParameters(
                                new DynamicLink.SocialMetaTagParameters.Builder()
                                        .setTitle(getString(R.string.app_name))
                                        .setDescription(fullShareMsg)
                                        .setImageUrl(Uri.parse(getString(R.string.app_icon_url)))
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
                                    showSnack(getString(R.string.failed_to_share_text));
                                }
                            }
                        });
    }

    private void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", getString(R.string.family_email), null));
        startActivity(Intent.createChooser(emailIntent, "Contact us"));
    }

    private void showProgress(String message) {

        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();

    }
}
