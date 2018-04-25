package com.nyayozangu.labs.fursa.activities.main;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    // TODO: 4/6/18 add bottom navigation fade in feature
    // TODO: 4/7/18 add back twice to exit

    private static final String TAG = "Sean";
    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    //users
    private String currentUserId;
    private FloatingActionButton mNewPost;
    private BottomNavigationView mainBottomNav;
    private TextView titleBarTextView;

    //instances of fragments
    private HomeFragment homeFragment;
    private CategoriesFragment categoriesFragment;
    private SavedFragment savedFragment;
    private AlertFragment alertFragment;
    private boolean doubleBackToExitPressedOnce = false;

    private CircleImageView userProfileImage;
    private TextView searchBar;

    private SearchView mainSearchView;
    private ImageView searchButton;
    private ConstraintLayout searchLayout;

    private List<String> lastSearches;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        Log.d(TAG, "at MainActivity, onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //subscribe to app updates
        FirebaseMessaging.getInstance().subscribeToTopic("UPDATES");
        Log.d(TAG, "user subscribed to topic UPDATES");

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

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

        mNewPost = findViewById(R.id.newPostFab);
        mainBottomNav = findViewById(R.id.mainBottomNav);


        //search
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openSearch();

            }
        });

        if (!isConnected()) {

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
                    case R.id.bottomNavSavedIted:
                        if (isLoggedIn()) {
                            setFragment(savedFragment);

                        } else {
                            setFragment(alertFragment);
                            Snackbar.make(findViewById(R.id.main_activity_layout),
                                    "Log in to view saved items", Snackbar.LENGTH_SHORT)
                                    .setAction("Login", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            goToLogin();
                                        }
                                    }).show();
                        }
                        return true;
                    default:
                        return false;
                }

            }
        });

        //set the userProfile image
        // TODO: 4/9/18 make having a profile image optional
        if (mAuth.getCurrentUser() != null) {
            //user is logged in
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    //check if successful
                    if (task.isSuccessful()) {
                        //task is successful
                        try {
                            String userImageDownloadUri = task.getResult().get("image").toString();

                            RequestOptions placeHolderOptions = new RequestOptions();
                            placeHolderOptions.placeholder(R.drawable.ic_action_person_placeholder);

                            Glide.with(MainActivity.this).applyDefaultRequestOptions(placeHolderOptions).load(userImageDownloadUri).into(userProfileImage);

                        } catch (NullPointerException imageNotFoundException) {

                            //user image not found
                            userProfileImage.setImageDrawable(getDrawable(R.drawable.ic_action_person_placeholder));
                            Log.d(TAG, "onComplete: user has no profile image");

                        }
                    } else {
                        //task unsuccessful handle errors
                        String errorMessage = task.getException().getMessage();
                        Log.d(TAG, "Error: " + errorMessage);
                    }
                }
            });
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


        mNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //only allow the user to post if user is signed in
                if (isLoggedIn()) {
                    //start the new post activity
                    goToNewPost();
                } else {
                    String message = "Log in to post items";

                    //user is not logged in show dialog
                    showLoginAlertDialog(message);
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
            Log.d(TAG, "getIntent is not null");
            Intent getPostIdIntent = getIntent();

            if (getPostIdIntent.getStringExtra("notify") != null) {

                String notifyMessage = getPostIdIntent.getStringExtra("notify");
                Snackbar.make(findViewById(R.id.main_activity_layout),
                        notifyMessage, Snackbar.LENGTH_LONG)
                        .show();
                Log.d(TAG, "notifyMessage is: " + notifyMessage);

            } else if (getPostIdIntent.getStringExtra("error") != null) {

                String errorMessage = getPostIdIntent.getStringExtra("error");

                if (errorMessage.equals(getString(R.string.post_not_found_text))) {

                    Snackbar.make(findViewById(R.id.main_activity_layout),
                            errorMessage, Snackbar.LENGTH_LONG)
                            .show();
                    Log.d(TAG, "errorMessage is: " + errorMessage);

                } else if (errorMessage.equals(getString(R.string.not_logged_in_text))) {

                    Snackbar.make(findViewById(R.id.main_activity_layout),
                            errorMessage, Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.login_text), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //go to log in
                                    goToLogin();

                                }
                            })
                            .show();
                    Log.d(TAG, "errorMessage is: " + errorMessage);

                }

            }

        }

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
        /*textView.setTextColor(Color.WHITE);*/
        /*textView.setHintTextColor(getResources().getColor(R.color.colorWhiteTransparent));*/

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

    private void goToSettings() {
        //to to settings page
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    //go to new post page
    private void goToNewPost() {
        startActivity(new Intent(MainActivity.this, CreatePostActivity.class));
    }

    //go to login page
    private void goToLogin() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }


    private void goToAccount() {
        //go to Account page
        startActivity(new Intent(MainActivity.this, AccountActivity.class));
    }

    private void logout() {
        mAuth.signOut();
        //send alert user is signed out
        Log.d(TAG, "user has signed out");
        String logoutMessage = "You are now signed out";
        showSnack(logoutMessage);
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.main_activity_layout),
                message, Snackbar.LENGTH_SHORT).show();
    }

    //check to see if the user is logged in
    @Override
    public void onStart() {

        Log.d(TAG, "at onStart");

        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.


        //check if user is logged in
        if (!isLoggedIn()) {
            //user is not logged in
            Log.d(TAG, "user not logged in");
        } else {
            //user is signed in
            Log.d(TAG, "user not logged in");
            //check if user exists in db
            currentUserId = mAuth.getCurrentUser().getUid();

            db.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    //check if user exists
                    if (task.isSuccessful()) {
                        Log.d(TAG, "task is successful");
                        //check is user exist
                        if (!task.getResult().exists()) {
                            //user does not exist
                            Log.d(TAG, "user exists");
                            //send user to login activity
                        } else {
                            //user exists
                            Log.d(TAG, "user exists");

                        }

                    } else {
                        //task was not successful
                        Log.d(TAG, "task not successful");
                        //handle error
                        String errorMessage = task.getException().getMessage();
                        Log.d(TAG, "failed to get user\n error message is: " + errorMessage);
                        Snackbar.make(findViewById(R.id.main_activity_layout),
                                "Failed to get user details: " + errorMessage, Snackbar.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        doubleBackToExit();
    }

    private void sendToLogin() {
        Log.d(TAG, "at sendToLogin()");
        //send to sing log in page
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }


    private void setFragment(Fragment fragment) {

        //begin transaction
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrameContainer, fragment);
        fragmentTransaction.commit();

    }

    private boolean isLoggedIn() {
        //determine if user is logged in
        return mAuth.getCurrentUser() != null;
    }


    /**
     * handles back backButton press when there's no history and/or user is at homescreen
     */
    public void doubleBackToExit() {
        Log.d(TAG, "at doubleBackToExit");

        if (doubleBackToExitPressedOnce) {
            Log.d(TAG, "pressed once");
            //back button is pressed for the first time
            super.onBackPressed();
            return;
        }
        //change the back button pressed once true
        this.doubleBackToExitPressedOnce = true;
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
        Snackbar.make(findViewById(R.id.main_activity_layout), "Are you you want to exit?", Snackbar.LENGTH_LONG)
                .setAction("Exit", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
    }

    private boolean isConnected() {

        //check if there's a connection
        Log.d(TAG, "at isConnected");
        Context context = getApplicationContext();
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {

            activeNetwork = cm.getActiveNetworkInfo();

        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }

}
