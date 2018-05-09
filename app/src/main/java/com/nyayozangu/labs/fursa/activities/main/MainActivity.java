package com.nyayozangu.labs.fursa.activities.main;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.fragments.AlertFragment;
import com.nyayozangu.labs.fursa.activities.main.fragments.CategoriesFragment;
import com.nyayozangu.labs.fursa.activities.main.fragments.HomeFragment;
import com.nyayozangu.labs.fursa.activities.main.fragments.SavedFragment;
import com.nyayozangu.labs.fursa.activities.posts.CreatePostActivity;
import com.nyayozangu.labs.fursa.activities.settings.AccountActivity;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.activities.settings.SettingsActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    // TODO: 4/6/18 add bottom navigation fade in feature

    private static final String TAG = "Sean";

    //common methods
    private CoMeth coMeth = new CoMeth();

    //users
    private String currentUserId;
    private FloatingActionButton createPostButton;
    private BottomNavigationView mainBottomNav;
    private TextView titleBarTextView;

    //instances of fragments
    private HomeFragment homeFragment;
    private CategoriesFragment categoriesFragment;
    private SavedFragment savedFragment;
    private AlertFragment alertFragment;
    private boolean doubleBackToExitPressedOnce = false;

    private CircleImageView userProfileImage;
    private TextView searchBar, fursaTitle;

    private SearchView mainSearchView;
    private ImageView searchButton;
    private ConstraintLayout searchLayout;

    private List<String> lastSearches;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        Log.d(TAG, "at MainActivity, onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //subscribe to app updates
        FirebaseMessaging.getInstance().subscribeToTopic("UPDATES");
        Log.d(TAG, "user subscribed to topic UPDATES");

        //initiate fragments
        homeFragment = new HomeFragment();
        categoriesFragment = new CategoriesFragment();
        savedFragment = new SavedFragment();
        alertFragment = new AlertFragment();

        userProfileImage = findViewById(R.id.currentUserImageView);

        //initiate elements
        mainSearchView = findViewById(R.id.mainSearchView);
        searchButton = findViewById(R.id.searchButton);
        searchLayout = findViewById(R.id.mainSearchConsLayout);

        createPostButton = findViewById(R.id.newPostFab);
        mainBottomNav = findViewById(R.id.mainBottomNav);

        fursaTitle = findViewById(R.id.fursaTitleTextView);

        //search
        //search icon is clicked
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: search icon is clicked");
                openSearch();

            }
        });
        //fursa title is clicked
        fursaTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: fursa title is clicked");
                openSearch();

            }
        });

        if (!coMeth.isConnected()) {

            //notify user is not connected
            showSnack(getString(R.string.failed_to_connect_text));

        }

        //set the homeFragment when home the main activity is loaded
        setFragment(homeFragment);

        //set onclick Listener for when the navigation items are selected
        mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.bottomNavHomeItem:

                        setFragment(homeFragment);
                        return true;

                    case R.id.bottomNavCatItem:

                        setFragment(categoriesFragment);
                        return true;

                    case R.id.bottomNavSavedItem:

                        if (coMeth.isLoggedIn()) {

                            setFragment(savedFragment);

                        } else {

                            setFragment(alertFragment);

                        }

                        return true;

                    default:

                        return false;

                }

            }
        });

        //set the userProfile image
        if (coMeth.isLoggedIn()) {

            //user is logged in
            String userId = new CoMeth().getUid();
            coMeth.getDb()
                    .collection("Users")
                    .document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            //check if successful
                            if (task.isSuccessful()) {
                                //task is successful
                                try {

                                    String userImageDownloadUri = task.getResult().get("image").toString();
                                    //set image
                                    coMeth.setImage(R.drawable.ic_action_person_placeholder,
                                            userImageDownloadUri,
                                            userProfileImage);

                                } catch (NullPointerException imageNotFoundException) {

                                    //user image not found
                                    userProfileImage.setImageDrawable(getDrawable(R.drawable.appiconshadow));
                                    Log.d(TAG, "onComplete: user has no profile image");

                                }
                            } else {

                                //task unsuccessful handle errors
                                String errorMessage = task.getException().getMessage();
                                Log.d(TAG, "Error: " + errorMessage);
                            }
                        }
                    });
        } else {

            userProfileImage.setImageDrawable(getDrawable(R.drawable.appiconshadow));

        }


        //hide search layout
        searchLayout.setVisibility(View.GONE);

        //set click listener to image view
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSettings();
            }
        });


        createPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (coMeth.isConnected()) {

                    //only allow the user to post if user is signed in
                    if (coMeth.isLoggedIn()) {

                        //check is user has verified email
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        boolean emailVerified = user.isEmailVerified();
                        if (emailVerified
                                || user.getProviders().contains("facebook.com")
                                || user.getProviders().contains("twitter.com")
                                || user.getProviders().contains("google.com")) {
                            //start the new post activity
                            goToCreatePost();
                        } else {

                            //user has not verified email
                            //alert user is not verified
                            showVerEmailDialog();

                        }
                    } else {

                        String message = "Log in to post items";
                        //user is not logged in show dialog
                        showLoginAlertDialog(message);
                    }
                } else {

                    AlertDialog.Builder noNetBuilder = new AlertDialog.Builder(MainActivity.this);
                    noNetBuilder.setTitle("Connection Error")
                            .setIcon(R.drawable.ic_action_red_alert)
                            .setMessage("Failed to connect to the internet\nCheck your connection and try again")
                            .setPositiveButton("On", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();

                                }
                            })
                            .show();

                }
            }
        });


        //handle search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mainSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mainSearchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchableActivity.class)));
        mainSearchView.setQueryHint(getResources().getString(R.string.search_hint));


        //get the sent intent
        if (getIntent() != null) {

            Intent getActionIntent = getIntent();
            if (getActionIntent.getStringExtra("action") != null) {
                switch (getActionIntent.getStringExtra("action")) {

                    case "notify":

                        // TODO: 5/1/18 check on deleting post notify message comes as error post not found
                        String notifyMessage = getActionIntent.getStringExtra("message");
                        showSnack(notifyMessage);
                        Log.d(TAG, "notifyMessage is: " + notifyMessage);
                        break;

                    case "goto":

                        switch (getActionIntent.getStringExtra("destination")) {

                            case "saved":

                                mainBottomNav.setSelectedItemId(R.id.bottomNavSavedItem);
                                break;

                            default:

                                Log.d(TAG, "onCreate: at default");

                        }

                    default:
                        Log.d(TAG, "onCreate: at default");

                }
            }




        }

    }

    private void goToCreatePost() {
        startActivity(new Intent(this, CreatePostActivity.class));
    }

    private void goToSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void showVerEmailDialog() {
        android.app.AlertDialog.Builder emailVerBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
        emailVerBuilder.setTitle(R.string.email_ver_text)
                .setIcon(R.drawable.ic_action_info_grey)
                .setMessage("You have to verify your email address to create a post.")
                .setPositiveButton("Resend Email", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        //send ver email
                        FirebaseUser user = coMeth.getAuth().getCurrentUser();
                        //show progress
                        String sendEmailMessage = getString(R.string.send_email_text);
                        showProgress(sendEmailMessage);
                        sendVerEmail(dialog, user);
                        //hide progress
                        hideProgress();

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

    private void sendVerEmail(final DialogInterface dialog, FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "Email sent.");
                            //inform user email is sent
                            //close the
                            dialog.dismiss();
                            AlertDialog.Builder logoutConfirmEmailBuilder = new AlertDialog.Builder(MainActivity.this);
                            logoutConfirmEmailBuilder.setTitle(getString(R.string.email_ver_text))
                                    .setIcon(R.drawable.ic_action_info_grey)
                                    .setMessage("A verification email has been sent to your email address.\nLogin after verifying your email to create posts.")
                                    .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            //log use out
                                            //take user to login screen
                                            coMeth.signOut();
                                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                            finish();

                                        }
                                    }).show();

                        }
                    }
                });
    }

    private void openSearch() {

        //show the search view
        showSearchView();

    }

    private void showSearchView() {
        //Load animation
        Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);
        searchLayout.startAnimation(slide_down);
        searchLayout.setVisibility(View.VISIBLE);

        //editing the text color and hints of the searchView
        int textViewId = mainSearchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView textView = mainSearchView.findViewById(textViewId);

        mainSearchView.setSubmitButtonEnabled(true);
        mainSearchView.setIconifiedByDefault(true);
        mainSearchView.setFocusable(true);
        mainSearchView.setIconified(false);
        mainSearchView.requestFocusFromTouch();
        mainSearchView.setQueryHint(getString(R.string.search_view_query_hint_text));

        mainSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                //hide search layout
                hideSearchView();

                return false;
            }
        });

    }

    /**
     * hides the searchView
     */
    private void hideSearchView() {
        if (searchLayout.getVisibility() == View.VISIBLE) {
            //load animation
            Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_up);
            searchLayout.startAnimation(slide_up);
            mainSearchView.setQuery(String.valueOf(""), false);
            searchLayout.setVisibility(View.GONE);
        }
    }

    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(MainActivity.this);
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

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }


    private void goToAccount() {
        //go to Account page
        startActivity(new Intent(MainActivity.this, AccountActivity.class));
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.main_activity_layout),
                message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        doubleBackToExit();
    }

    private void setFragment(Fragment fragment) {

        //begin transaction
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrameContainer, fragment);
        fragmentTransaction.commit();

    }

    public void doubleBackToExit() {

        if (doubleBackToExitPressedOnce) {
            Log.d(TAG, "pressed once");
            //back button is pressed for the first time
            super.onBackPressed();
            /*return;*/
        }
        //change the back button pressed once true
        doubleBackToExitPressedOnce = true;
        promptExit();
        //create a delay to listen to the second time back is ressed
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //when 2 seconds pass reset the number of counts back is pressed
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void promptExit() {

        Snackbar.make(findViewById(R.id.main_activity_layout), getString(R.string.confirm_ext_text), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.exit_text), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                })
                .show();
    }

    private void showProgress(String message) {

        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();

    }

    private void hideProgress() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


}
