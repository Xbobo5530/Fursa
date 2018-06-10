package com.nyayozangu.labs.fursa.activities.main;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
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
import com.nyayozangu.labs.fursa.users.Users;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity/* implements CreatePostActivity.AsyncResponse */ {

    private static final String TAG = "Sean";

    //common methods
    private CoMeth coMeth = new CoMeth();

    //users
    public FloatingActionButton createPostButton;
    public BottomNavigationView mainBottomNav;
    private TextView titleBarTextView;

    //instances of fragments
    private HomeFragment homeFragment;
    private CategoriesFragment categoriesFragment;
    private SavedFragment savedFragment;
    private AlertFragment alertFragment;
    private TermsFragment termsFragment;

    private boolean doubleBackToExitPressedOnce = false;

    private CircleImageView userProfileImage;
    private TextView searchBar, fursaTitle;

    private SearchView mainSearchView;
    private ImageView searchButton;
    private ConstraintLayout searchLayout;

    private List<String> lastSearches;
    private ProgressDialog progressDialog;

    public String hasAcceptedTermsStatus;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        Log.d(TAG, "at MainActivity, onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        cleanDB();
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
        mainSearchView = findViewById(R.id.mainSearchView);
        searchButton = findViewById(R.id.searchButton);
        searchLayout = findViewById(R.id.mainSearchConsLayout);
        userProfileImage = findViewById(R.id.currentUserImageView);
        createPostButton = findViewById(R.id.newPostFab);
        mainBottomNav = findViewById(R.id.mainBottomNav);
        fursaTitle = findViewById(R.id.fursaTitleTextView);

        hasAcceptedTermsStatus = getResources().getString(R.string.false_value);

        //get the sent intent
        handleIntent();

        //check if already accepted terms
        checkHasAcceptedTerms();

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

        // TODO: 5/23/18 handle reselect bottom nav items
        //handle bottom nav item re-selected
        /*mainBottomNav.setOnNavigationItemReselectedListener(
                new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.bottomNavHomeItem:

                        *//*
                        * Bundle bundle = new Bundle();
                            bundle.putString("edttext", "From Activity");
                            // set Fragmentclass Arguments
                            Fragmentclass fragobj = new Fragmentclass();
                            fragobj.setArguments(bundle);
                        * *//*
                        Log.d(TAG, "onNavigationItemReselected: ");
                        Bundle bundle = new Bundle();
                        bundle.putString("action", "home_reselect");
                        homeFragment.setArguments(bundle);
                        setFragment(homeFragment);
                        break;
                    case R.id.bottomNavSavedItem:
                        setFragment(savedFragment);
                        break;
                    default:
                        Log.d(TAG, "onNavigationItemReselected: ");
                }
            }
        });*/

        //set the userProfile image
        if (coMeth.isLoggedIn()) {

            //user is logged in
            String userId = new CoMeth().getUid();
            coMeth.getDb()
                    .collection("Users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //task is successful
                            //convert user to object
                            if (documentSnapshot.exists()) {
                                Users user = documentSnapshot.toObject(Users.class);
                                if (user.getImage() != null) {
                                    String userImageDownloadUri = user.getImage();
                                    //set image
                                    coMeth.setImage(R.drawable.ic_action_person_placeholder,
                                            userImageDownloadUri,
                                            userProfileImage);
                                } else {
                                    userProfileImage
                                            .setImageDrawable(
                                                    getResources().getDrawable(R.drawable.ic_action_person_placeholder));
                                }
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
        } else {
            userProfileImage.setImageDrawable(
                    getResources().getDrawable(R.drawable.appiconshadow));
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

                        String message = getString(R.string.login_to_post_text);
                        goToLogin(message);
                    }
                } else {

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
            }
        });


        //handle search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mainSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mainSearchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(this, SearchableActivity.class)));
        mainSearchView.setQueryHint(getResources().getString(R.string.search_hint));
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

    private void goToTerms() {
        setFragment(termsFragment);
        //hide the hide bottom nav
        //hide create post fab
        mainBottomNav.setVisibility(View.GONE);
        createPostButton.setVisibility(View.GONE);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: ");
        super.onNewIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        Log.d(TAG, "handleIntent: at main");
        if (getIntent() != null) {
            Log.d(TAG, "handleIntent: intent is not null");
            Intent intent = getIntent();
            if (intent.getStringExtra(getResources().getString(R.string.ACTION_NAME)) != null) {
                switch (intent.getStringExtra(getResources().getString(R.string.ACTION_NAME))) {

                    case "notify":

                        //set the homeFragment when home the main activity is loaded
                        mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
                        setFragment(homeFragment);
                        String notifyMessage = intent.getStringExtra(
                                getResources().getString(R.string.MESSAGE_NAME));
                        showSnack(notifyMessage);
                        Log.d(TAG, "notifyMessage is: " + notifyMessage);

                        break;

                    case "update":
                        //set the homeFragment when home the main activity is loaded
                        mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
                        setFragment(homeFragment);
                        showUpdateDialog();
                        Log.d(TAG, "handleIntent: action is update");
                        break;

                    case "goto":

                        switch (intent.getStringExtra("destination")) {

                            case "saved":
                                mainBottomNav.setSelectedItemId(R.id.bottomNavSavedItem);
                                setFragment(savedFragment);
                                break;
                            case "categories":
                                mainBottomNav.setSelectedItemId(R.id.bottomNavCatItem);
                                setFragment(categoriesFragment);
                                break;
                            case "terms":
                                setFragment(termsFragment);
                                break;
                            default:
                                //set the homeFragment when home the main activity is loaded
                                mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
                                setFragment(homeFragment);
                                Log.d(TAG, "onCreate: at goto default");
                        }
                        break;
                    default:
                        //set the homeFragment when home the main activity is loaded
                        mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
//                        setFragment(homeFragment);
                        Log.d(TAG, "onCreate: at action default" +
                                intent.getStringExtra("action"));
                }
            } else {
                //set the homeFragment when home the main activity is loaded
//                mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
                setFragment(homeFragment);
            }
        } else {
            //set the homeFragment when home the main activity is loaded
            Log.d(TAG, "handleIntent: intent is null");
//            mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
            setFragment(homeFragment);
        }
    }

    private void showUpdateDialog() {
        Log.d(TAG, "showUpdateDialog: ");
        AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(this);
        Log.d(TAG, "showVersionInfo: aboutBuilder " + aboutBuilder.toString());
        String versionName = BuildConfig.VERSION_NAME;
        Log.d(TAG, "showVersionInfo: versionName " + versionName);
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
        goToLogin.putExtra("message", message);
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
                            AlertDialog.Builder logoutConfirmEmailBuilder =
                                    new AlertDialog.Builder(MainActivity.this);
                            logoutConfirmEmailBuilder.setTitle(getString(R.string.email_ver_text))
                                    .setIcon(R.drawable.ic_action_info_grey)
                                    .setMessage("A verification email has been sent to your email address." +
                                            "\nLogin after verifying your email to create posts.")
                                    .setPositiveButton(getString(R.string.ok_text),
                                            new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            //log use out
                                            //take user to login screen
                                            coMeth.signOut();
                                            startActivity(new Intent(
                                                    MainActivity.this,
                                                    LoginActivity.class));
                                            finish();

                                        }
                                    }).show();

                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        hideSearchView();
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

    private void hideProgress() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

//    @Override
//    public void processFinish(boolean submitSuccessful) {
//        Log.d(TAG, "processFinish: on main");
//        if (submitSuccessful) {
//            showSnack("Your post has been successfully posted");
//        }else{
//            showSnack("Failed to submit post");
//        }
//    }

    /*
    private void cleanDB() {
        Log.d(TAG, "cleanDB: ");
        //get all user posts and check if user exists
        coMeth.getDb()
                .collection("Posts")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    final String postId = doc.getDocument().getId();
                                    final Posts post = doc.getDocument().toObject(Posts.class);
                                    String postUserId = post.getUser_id();
                                    //check if user still exists
                                    coMeth.getDb()
                                            .collection("Users")
                                            .document(postUserId)
                                            .get()
                                            .addOnCompleteListener(
                                                    new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(
                                                                @NonNull Task<DocumentSnapshot> task) {
                                                            if (!task.getResult().exists()) {
                                                                //delete post
                                                                coMeth.getDb()
                                                                        .collection("Posts")
                                                                        .document(postId)
                                                                        .delete();
                                                            }
                                                        }
                                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG,
                                                            "onFailure: failed to get post with error" +
                                                                    "\nerror is: " + e.getMessage());
                                                }
                                            });
                                }
                            }
                        }
                    }
                });

    }
    */

}
