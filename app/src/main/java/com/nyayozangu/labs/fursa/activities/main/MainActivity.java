package com.nyayozangu.labs.fursa.activities.main;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nyayozangu.labs.fursa.BuildConfig;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.fragments.AlertFragment;
import com.nyayozangu.labs.fursa.activities.main.fragments.CategoriesFragment;
import com.nyayozangu.labs.fursa.activities.main.fragments.HomeFragment;
import com.nyayozangu.labs.fursa.activities.main.fragments.SavedFragment;
import com.nyayozangu.labs.fursa.activities.main.fragments.TermsFragment;
import com.nyayozangu.labs.fursa.activities.posts.CreatePostActivity;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.activities.settings.SettingsActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.UserPageActivity;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    private static final String FACEBOOK_COM = "facebook.com";

    // TODO: 6/23/18 handle notifications
    // TODO: 6/23/18 handle general notoifications and user notifications

    //common methods
    private CoMeth coMeth = new CoMeth();

    //users
    public FloatingActionButton newPostFab;
    public BottomNavigationView mainBottomNav;
    private TextView titleBarTextView;

    private DrawerLayout mainDrawerLayout;

    //instances of fragments
    private HomeFragment homeFragment;
    private CategoriesFragment categoriesFragment;
    private SavedFragment savedFragment;
    private AlertFragment alertFragment;
    private TermsFragment termsFragment;

    private boolean doubleBackToExitPressedOnce = false;

//    private CircleImageView userProfileImage;
//    private TextView searchBar;

//    private SearchView mainSearchView;
//    private ConstraintLayout searchLayout;

    private List<String> lastSearches;
    private ProgressDialog progressDialog;

    public String hasAcceptedTermsStatus;

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
        termsFragment = new TermsFragment();

        //initiate elements
//        mainSearchView = findViewById(R.id.mainSearchView);
//        progressBar = findViewById(R.id.mainProgressBar);
//        ImageButton searchButton = findViewById(R.id.searchButton);
//        searchLayout = findViewById(R.id.mainSearchConsLayout);
//        userProfileImage = findViewById(R.id.currentUserImageView);
        newPostFab = findViewById(R.id.newPostFab);
        mainBottomNav = findViewById(R.id.mainBottomNav);
//        TextView titleBar = findViewById(R.id.fursaTitleTextView);
        mainDrawerLayout = findViewById(R.id.drawer_layout);

        //handle toolbar
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle(getResources().getString(R.string.app_name));
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainDrawerLayout.openDrawer(Gravity.START);
            }
        });

        hasAcceptedTermsStatus = getResources().getString(R.string.false_value);

        //get the sent intent
        handleIntent();

        //check if already accepted terms
        checkHasAcceptedTerms();

        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
//                        menuItem.setChecked(true);

                        Log.d(TAG, "onNavigationItemSelected: selected item is " + menuItem);


                        switch (menuItem.getItemId()) {
                            case R.id.nav_share:
                                Log.d(TAG, "onNavigationItemSelected: " +
                                        "share nave item selected");
                                Toast.makeText(MainActivity.this, "share nav item selected", Toast.LENGTH_SHORT).show();
                            default:
                                Log.d(TAG, "onNavigationItemSelected: " +
                                        "nav menu item select on default");
                                Toast.makeText(MainActivity.this, "at default", Toast.LENGTH_SHORT).show();

                        }

                        // close drawer when item is tapped
                        mainDrawerLayout.closeDrawers();
                        return true;

                    }
                });

        if (!coMeth.isConnected()) {
            //notify user is not connected
            showSnack(getString(R.string.failed_to_connect_text));
        }

        //set onclick Listener for when the navigation items are selected
        mainBottomNav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
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
        handleNavViewHeader(navigationView);

        //hadnlde new posts fab
        newPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (coMeth.isConnected() && coMeth.isLoggedIn()) {
                    //check is user has verified email
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    boolean emailVerified = user.isEmailVerified();
                    if (emailVerified
                            || user.getProviders().contains(FACEBOOK_COM)
                            || user.getProviders().contains("twitter.com")
                            || user.getProviders().contains("google.com")) {
                        //start the new post activity
                        goToCreatePost();
                    } else {
                        showVerEmailDialog();
                    }
                } else {
                    if (!coMeth.isConnected()) showConnectionErrorMessage();
                    if (!coMeth.isLoggedIn()) goToLogin(getString(R.string.login_to_post_text));
                }
            }
        });

        //handle search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        mainSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        mainSearchView.setSearchableInfo(searchManager.getSearchableInfo(
//                new ComponentName(this, SearchableActivity.class)));
//        mainSearchView.setQueryHint(getResources().getString(R.string.search_hint));
    }

    private void showConnectionErrorMessage() {
        AlertDialog.Builder noNetBuilder = new AlertDialog.Builder(MainActivity.this);
        noNetBuilder.setTitle("Connection Error")
                .setIcon(R.drawable.ic_action_red_alert)
                .setMessage("Failed to connect to the internet" +
                        "\nCheck your connection and try again")
                .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void handleNavViewHeader(NavigationView navigationView) {
        Log.d(TAG, "handleNavViewHeader: ");
        ConstraintLayout navHeader = (ConstraintLayout) navigationView.getHeaderView(0);
        final ImageView userProfileImage = navHeader.findViewById(R.id.navHeaderUserImageView);
        final TextView usernameField = navHeader.findViewById(R.id.navHeaderUsernameTextView);

        if (coMeth.isLoggedIn()) {
            //set user data
            String userId = coMeth.getUid();
            coMeth.getDb()
                    .collection(CoMeth.USERS).document(userId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //task is successful
                            //convert user to object
                            if (documentSnapshot.exists()) {
                                Users user = documentSnapshot.toObject(Users.class);
                                assert user != null;
                                setUserData(user, userProfileImage, usernameField);
                            } else {
                                //user does not exist
                                Log.d(TAG, "onSuccess: user does not exist");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: failed to load user image\n" +
                                    e.getMessage());
                            userProfileImage.setImageDrawable(
                                    getResources().getDrawable(R.drawable.ic_action_person_placeholder));
                        }
                    });

            //set click listener
            navHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToUserPage();
                }
            });
        } else {
            userProfileImage.setImageDrawable(
                    getResources().getDrawable(R.drawable.appiconshadow));
            //set login text
            usernameField.setText(getResources().getString(R.string.login_text));
            //if user is not logged in
            //clicking the nav header opens the login page
            navHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToLogin();
                }
            });
        }
    }

    /**
     * go to login page without message
     */
    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    /**
     * set user data
     *
     * @param user             user data
     * @param usernameField    view TextView for username
     * @param userProfileImage String user image download url
     */
    private void setUserData(Users user, ImageView userProfileImage, TextView usernameField) {
        if (user.getImage() != null) {
            String userImageDownloadUri = user.getImage();
            String username = user.getName();
            //set image
            coMeth.setImage(R.drawable.ic_action_person_placeholder,
                    userImageDownloadUri,
                    userProfileImage);
            //set username
            usernameField.setText(username);
        } else {
            userProfileImage
                    .setImageDrawable(
                            getResources().getDrawable(R.drawable
                                    .ic_action_person_placeholder));
        }
    }

    private void checkHasAcceptedTerms() {

        //default value for has accepted terms
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String hasAcceptedTerms = sharedPref.
                getString(getResources().getString(R.string.has_accepted_terms),
                        hasAcceptedTermsStatus);
        Log.d(TAG, "onCreate: \nhasAcceptedTerms: " + hasAcceptedTerms);

        if (hasAcceptedTerms.equals(getResources().getString(R.string.false_value))) {
            handleTerms();
        }
    }

    private void handleTerms() {
        Log.d(TAG, "handleTerms: ");
        android.app.AlertDialog.Builder termBuilder = new
                android.app.AlertDialog.Builder(MainActivity.this);
        termBuilder.setTitle("Terms and Conditions")
                .setIcon(getResources().getDrawable(R.drawable.ic_action_book))
                .setMessage(R.string.by_proceedinng_u_accept_terms_text)
                .setPositiveButton(R.string.agree_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.has_accepted_terms),
                                getString(R.string.true_value));
                        editor.apply();

                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar, menu);
        return true;
    }

    private void goToTerms() {
        setFragment(termsFragment);
        //hide the hide bottom nav
        //hide create post fab
        mainBottomNav.setVisibility(View.GONE);
        newPostFab.setVisibility(View.GONE);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: ");
        super.onNewIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        if (getIntent() != null) {
            Intent intent = getIntent();
            if (intent.getStringExtra(getResources().getString(R.string.ACTION_NAME)) != null) {
                switch (intent.getStringExtra(getResources().getString(R.string.ACTION_NAME))) {

                    case CoMeth.NOTIFY:

                        //set the homeFragment when home the main activity is loaded
                        mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
                        setFragment(homeFragment);
                        String notifyMessage = intent.getStringExtra(
                                getResources().getString(R.string.MESSAGE_NAME));
                        showSnack(notifyMessage);
                        Log.d(TAG, "notifyMessage is: " + notifyMessage);

                        break;

                    case CoMeth.UPDATE:
                        //set the homeFragment when home the main activity is loaded
                        mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
                        setFragment(homeFragment);
                        showUpdateDialog();
                        Log.d(TAG, "handleIntent: action is update");
                        break;

                    case CoMeth.GOTO:

                        switch (intent.getStringExtra(CoMeth.DESTINATION)) {

                            case CoMeth.SAVED_VAL:
                                mainBottomNav.setSelectedItemId(R.id.bottomNavSavedItem);
                                setFragment(savedFragment);
                                break;
                            case CoMeth.CATEGORIES_VAL:
                                mainBottomNav.setSelectedItemId(R.id.bottomNavCatItem);
                                setFragment(categoriesFragment);
                                break;
                            case CoMeth.TERMS:
                                setFragment(termsFragment);
                                break;
                            default:
                                mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
                                setFragment(homeFragment);
                                Log.d(TAG, "onCreate: at goto default");
                        }
                        break;
                    default:
                        mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
                        Log.d(TAG, "onCreate: at action default" +
                                intent.getStringExtra("action"));
                }
            } else {
                setFragment(homeFragment);
            }
        } else {
            Log.d(TAG, "handleIntent: intent is null");
            setFragment(homeFragment);
        }
    }

    private void showUpdateDialog() {
        Log.d(TAG, "showUpdateDialog: ");
        AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(this);
        String versionName = BuildConfig.VERSION_NAME;
        aboutBuilder.setTitle(getResources().getString(R.string.app_name) + " " + versionName)
                .setIcon(getResources().getDrawable(R.drawable.ic_action_info_grey))
                .setMessage(getResources().getString(R.string.UPDATE_INFO))
                .setPositiveButton(getResources().getString(R.string.close_text),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    private void goToLogin(String message) {
        Intent goToLogin = new Intent(MainActivity.this, LoginActivity.class);
        goToLogin.putExtra(CoMeth.MESSAGE, message);
        startActivity(goToLogin);
    }

    private void goToCreatePost() {
        startActivity(
                new Intent(this, CreatePostActivity.class));
    }

    private void goToSettings() {
        startActivity(
                new Intent(this, SettingsActivity.class));
    }

    private void showVerEmailDialog() {
        android.app.AlertDialog.Builder emailVerBuilder =
                new android.app.AlertDialog.Builder(MainActivity.this);
        emailVerBuilder.setTitle(R.string.email_ver_text)
                .setIcon(R.drawable.ic_action_info_grey)
                .setMessage(R.string.verify_your_email_text)
                .setPositiveButton(R.string.resend_email_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        //send ver email
                        FirebaseUser user = coMeth.getAuth().getCurrentUser();
                        //show progress
                        String sendEmailMessage = getString(R.string.send_email_text);
                        showProgress(sendEmailMessage);
                        sendVerEmail(dialog, user);
                        coMeth.stopLoading(progressDialog);

                    }
                })
                .setNegativeButton(getString(R.string.cancel_text),
                        new DialogInterface.OnClickListener() {
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
                            showVerificationInstructions();

                        }
                    }
                });
    }

    private void showVerificationInstructions() {
        AlertDialog.Builder logoutConfirmEmailBuilder =
                new AlertDialog.Builder(MainActivity.this);
        logoutConfirmEmailBuilder.setTitle(getString(R.string.email_ver_text))
                .setIcon(R.drawable.ic_action_info_grey)
                .setMessage(getString(R.string.verification_email_sent_text) +
                        getString(R.string.login_afer_verification_text))
                .setPositiveButton(getString(R.string.ok_text),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //take user to login screen
                                coMeth.signOut();
                                startActivity(new Intent(
                                        MainActivity.this,
                                        LoginActivity.class));
                                finish();

                            }
                        }).show();
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.mainSnack),
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

        Snackbar.make(findViewById(R.id.mainSnack),
                getString(R.string.confirm_ext_text),
                Snackbar.LENGTH_LONG)
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


    private void goToUserPage() {
        Intent goToUserPageIntent = new Intent(this, UserPageActivity.class);
        goToUserPageIntent.putExtra("userId", coMeth.getUid());
        startActivity(goToUserPageIntent);
    }

}
