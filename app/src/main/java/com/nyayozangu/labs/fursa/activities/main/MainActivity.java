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
import com.nyayozangu.labs.fursa.activities.posts.CreatePostActivity;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.activities.settings.SettingsActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity/* implements CreatePostActivity.AsyncResponse */ {

    // TODO: 5/14/18 add a timer to record when a user has not poened the app for 2 days

    private static final String TAG = "Sean";

    //common methods
    private CoMeth coMeth = new CoMeth();

    //users
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

//        cleanDB();

        /*Log.d(TAG, "onCreate: testing process update info \nprocessed info is: " + processUpdate("Thank you for updating your Fursa app, here is what we have been working on:\\n\n" +
                "New Features:\\n\n" +
                "- Tag your posts by simply adding ‘#’ to words in your post title or post description\\n\n" +
                "- Increased stability\\n\\n\n" +
                "Bug Fixes:\\n\n" +
                "- Faster loading speeds\\n\n" +
                "- Fixed the no pots alert on the Categories Page\n"));*/

        //subscribe to app updates
        FirebaseMessaging.getInstance().subscribeToTopic("UPDATES");
        Log.d(TAG, "user subscribed to topic UPDATES");

        //initiate fragments
        homeFragment = new HomeFragment();
        categoriesFragment = new CategoriesFragment();
        savedFragment = new SavedFragment();
        alertFragment = new AlertFragment();

        //initiate elements
        mainSearchView = findViewById(R.id.mainSearchView);
        searchButton = findViewById(R.id.searchButton);
        searchLayout = findViewById(R.id.mainSearchConsLayout);
        userProfileImage = findViewById(R.id.currentUserImageView);
        createPostButton = findViewById(R.id.newPostFab);
        mainBottomNav = findViewById(R.id.mainBottomNav);
        fursaTitle = findViewById(R.id.fursaTitleTextView);


        //get the sent intent
        handleIntent();

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
                                    userProfileImage
                                            .setImageDrawable(getResources()
                                                    .getDrawable(R.drawable.appiconshadow));
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
            userProfileImage.setImageDrawable(getResources().getDrawable(R.drawable.appiconshadow));
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

    private void handleIntent() {
        Log.d(TAG, "handleIntent: at main");
        if (getIntent() != null) {
            Log.d(TAG, "handleIntent: intent is not null");
            Intent getActionIntent = getIntent();
            if (getActionIntent.getStringExtra("action") != null) {
                switch (getActionIntent.getStringExtra("action")) {

                    case "notify":

                        //set the homeFragment when home the main activity is loaded
                        mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
                        setFragment(homeFragment);
                        String notifyMessage = getActionIntent.getStringExtra("message");
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

                        switch (getActionIntent.getStringExtra("destination")) {

                            case "saved":
                                mainBottomNav.setSelectedItemId(R.id.bottomNavSavedItem);
                                setFragment(savedFragment);
                                break;
                            case "categories":
                                mainBottomNav.setSelectedItemId(R.id.bottomNavCatItem);
                                setFragment(categoriesFragment);
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
                        setFragment(homeFragment);
                        Log.d(TAG, "onCreate: at action default\naction is: " +
                                getActionIntent.getStringExtra("action"));
                }
            }
        } else {
            //set the homeFragment when home the main activity is loaded
            Log.d(TAG, "handleIntent: intent is null");
            mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
            setFragment(homeFragment);
        }
    }

    private void showUpdateDialog() {
        Log.d(TAG, "showUpdateDialog: ");
        String currentVersionCode = String.valueOf(BuildConfig.VERSION_CODE);
        Log.d(TAG, "showUpdateDialog: \ncurrentVersionCode: " + currentVersionCode);
        coMeth.getDb()
                .collection("Updates")
                .document(currentVersionCode)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            handleUpdatesDialog(documentSnapshot);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get update info: " + e.getMessage());
                    }
                });
    }

    private void handleUpdatesDialog(DocumentSnapshot documentSnapshot) {
        Log.d(TAG, "handleUpdatesDialog: ");
        if (documentSnapshot.get("info") != null) {
            if (documentSnapshot.get("info") != null) {
                String updateInfo = documentSnapshot.get("info").toString();
//                String processedUpdateInfo = processUpdate(updateInfo);
                AlertDialog.Builder updatesBuilder = new AlertDialog.Builder(MainActivity.this);
                updatesBuilder.setTitle(getResources().getString(R.string.on_this_update_text))
                        .setIcon(getResources().getDrawable(R.drawable.ic_action_updates))
                        .setMessage(updateInfo)
                        .setPositiveButton(getResources().getString(R.string.ok_text),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .setCancelable(false)
                        .show();
            }
        }
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
