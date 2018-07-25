package com.nyayozangu.labs.fursa.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nyayozangu.labs.fursa.BuildConfig;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.fragments.AlertFragment;
import com.nyayozangu.labs.fursa.fragments.CategoriesFragment;
import com.nyayozangu.labs.fursa.fragments.HomeFragment;
import com.nyayozangu.labs.fursa.fragments.NotificationsFragment;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Categories;
import com.nyayozangu.labs.fursa.models.Users;

import java.util.ArrayList;
import java.util.Objects;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.ACTION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.CATEGORIES_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.DESTINATION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FACEBOOK_DOT_COM;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.GOOGLE_DOT_COM;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.GOTO;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MESSAGE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NEW_FOLLOWERS_UPDATE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NOTIFICATIONS_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NOTIFY;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SAVED_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TWITTER_DOT_COM;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.UPDATE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String TAG = "Sean";

    private static final String UPDATES = "UPDATES";
    private static final String TAB = "tab";
    private CoMeth  coMeth = new CoMeth();

    public FloatingActionButton getNewPostFab() {
        return newPostFab;
    }

    public FloatingActionButton newPostFab;
    public BottomNavigationView mainBottomNav;
    private DrawerLayout mainDrawerLayout;
    private ActionBarDrawerToggle toggle;

    private HomeFragment homeFragment;
    private CategoriesFragment categoriesFragment;
    private AlertFragment alertFragment;
    private NotificationsFragment notificationsFragment;

    private boolean doubleBackToExitPressedOnce = false;
    private ProgressDialog progressDialog;

    public ActionBar actionbar;
    public String hasAcceptedTermsStatus, username;

    private ImageView userProfileImage;
    private TextView usernameField;
    private Users user;

    private ArrayList<String> catSubsArray;
    private String[] catsListItems;


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //subscribe to app updates
        handleSubscriptions();

        homeFragment = new HomeFragment();
        categoriesFragment = new CategoriesFragment();
        alertFragment = new AlertFragment();
        notificationsFragment = new NotificationsFragment();

        newPostFab = findViewById(R.id.newPostFab);
        mainBottomNav = findViewById(R.id.mainBottomNav);
        mainDrawerLayout = findViewById(R.id.drawer_layout);

        catSubsArray = new ArrayList<>();

        //handle toolbar
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        actionbar = getSupportActionBar();
        actionbar.setTitle(getResources().getString(R.string.app_name));
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainDrawerLayout.openDrawer(Gravity.START);
            }
        });

        //handle nav drawer
        final NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this,mainDrawerLayout, R.string.open_text, R.string.close_text);
        mainDrawerLayout.addDrawerListener(toggle);
        if (user != null){
            setUserData(user, userProfileImage, usernameField);
        }else{
            handleNavViewHeader(navigationView);
        }

        //check terms status
        hasAcceptedTermsStatus = CoMeth.FALSE;
        checkHasAcceptedTerms();

        handleIntent();
        handleDrawerListeners(navigationView);


        handleBottomNavBar();
        newPostFab.setOnClickListener(this);
        if (!coMeth.isConnected(this)) {
            showSnack(getResources().getString(R.string.failed_to_connect_text));
        }
    }

    private void handleSubscriptions() {
        FirebaseMessaging.getInstance().subscribeToTopic(UPDATES);
        if (coMeth.isLoggedIn()) {
            String followerTopic = coMeth.getUid() + NEW_FOLLOWERS_UPDATE;
            FirebaseMessaging.getInstance().subscribeToTopic(followerTopic);
        }
    }

    private void handleDrawerListeners(final NavigationView navigationView) {
        Menu navMenu = navigationView.getMenu();
        final MenuItem loginButton = navMenu.findItem(R.id.nav_login);
        final MenuItem myProfileButton = navMenu.findItem(R.id.nav_my_profile);
        final MenuItem myPostsButton = navMenu.findItem(R.id.nav_my_posts);
        final MenuItem mySavesButton = navMenu.findItem(R.id.nav_my_saves);
        final MenuItem mySubsButton = navMenu.findItem(R.id.nav_my_subs);
        mainDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                updateDrawerButtonsStatus(navigationView,
                        loginButton, myProfileButton, myPostsButton, mySavesButton, mySubsButton);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                updateDrawerButtonsStatus(navigationView,
                        loginButton, myProfileButton, myPostsButton, mySavesButton, mySubsButton);

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                updateDrawerButtonsStatus(navigationView,
                        loginButton, myProfileButton, myPostsButton, mySavesButton, mySubsButton);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void updateDrawerButtonsStatus(NavigationView navigationView,
                                           MenuItem loginButton,
                                           MenuItem myPostsButton,
                                           MenuItem myProfileButton,
                                           MenuItem mySavesButton,
                                           MenuItem mySubsButton) {
        if (coMeth.isLoggedIn()){

            if (user != null){
                setUserData(user, userProfileImage, usernameField);
            }else{
                handleNavViewHeader(navigationView);
            }

            loginButton.setTitle(getResources().getString(R.string.logout_text));
            loginButton.setIcon(getResources().getDrawable(R.drawable.ic_action_logout));
        }else{
            handleNavViewHeader(navigationView);
            loginButton.setTitle(getResources().getString(R.string.login_text));
            loginButton.setIcon(getResources().getDrawable(R.drawable.ic_action_person_placeholder));
            myProfileButton.setVisible(false);
            myPostsButton.setVisible(false);
            mySavesButton.setVisible(false);
            mySubsButton.setVisible(false);
        }
    }

    private void handleBottomNavBar() {
        mainBottomNav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.bottomNavHomeItem:
                                setFragment(homeFragment);
                                actionbar.setTitle(getResources().getString(R.string.app_name));
                                return true;
                            case R.id.bottomNavCatItem:
                                setFragment(categoriesFragment);
                                actionbar.setTitle(getResources().getString(R.string.categories_text));
                                return true;
                            case R.id.bottomNavNotificationsItem:
                                if (coMeth.isLoggedIn()) {
                                    setFragment(notificationsFragment);
                                    actionbar.setTitle(getResources().getString(R.string.notifications_text));
                                }else{
                                    setFragment(alertFragment);
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });
    }

    private void handleNavViewHeader(NavigationView navigationView) {
        ConstraintLayout navHeader = (ConstraintLayout) navigationView.getHeaderView(0);
        userProfileImage = navHeader.findViewById(R.id.navHeaderUserImageView);
        usernameField = navHeader.findViewById(R.id.navHeaderUsernameTextView);
        usernameField.setVisibility(View.VISIBLE);
        if (coMeth.isLoggedIn()) {
            //set user data
            String userId = coMeth.getUid();
            coMeth.getDb().collection(USERS).document(userId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                user = documentSnapshot.toObject(Users.class);
                                assert user != null;
                                setUserData(user, userProfileImage, usernameField);
                            } else {
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
                    mainDrawerLayout.closeDrawers();
                }
            });
        } else {
            userProfileImage.setImageDrawable(
                    getResources().getDrawable(R.drawable.appiconshadow));
            usernameField.setVisibility(View.GONE);

            navHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToLogin();
                    mainDrawerLayout.closeDrawers();
                }
            });
        }
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    /**
     * set user data
     *
     * @param user  user data
     * @param usernameField view TextView for username
     * @param userProfileImage String user image download url
     */
    private void setUserData(Users user, ImageView userProfileImage, TextView usernameField) {
        userProfileImage.setVisibility(View.VISIBLE);
        if (user.getImage() != null) {
            String userImageDownloadUri = user.getImage();
            username = user.getName();
            //set image
            coMeth.setImageWithTransition(R.drawable.appiconshadow,
                    userImageDownloadUri,
                    userProfileImage, this);
            //set username
            usernameField.setText(username);
        } else {
            userProfileImage.setImageDrawable(
                    getResources().getDrawable(R.drawable.ic_action_person_placeholder));
        }
    }

    private void checkHasAcceptedTerms() {
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
        android.app.AlertDialog.Builder termBuilder = new
                android.app.AlertDialog.Builder(MainActivity.this);
        termBuilder.setTitle(R.string.terms_n_cond_text)
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
                .setNegativeButton(getResources().getString(R.string.view_terms_text),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewTermsAndConditions();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar, menu);

        //handle search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchMainToolbarMenuItem).getActionView();
        searchView.setSearchableInfo(Objects.requireNonNull(searchManager).getSearchableInfo(getComponentName()));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(this, SearchableActivity.class)));
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        return true;
    }

    private void viewTermsAndConditions() {
        String url = CoMeth.TERMS_URL;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        if (getIntent() != null) {
            Intent intent = getIntent();
            if (intent.getStringExtra(ACTION) != null) {
                switch (intent.getStringExtra(ACTION)) {

                    case NOTIFY:
                        mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
                        setFragment(homeFragment);
                        String notifyMessage = intent.getStringExtra(MESSAGE);
                        showSnack(notifyMessage);
                        break;
                    case UPDATE:
                        mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
                        setFragment(homeFragment);
                        showUpdateDialog();
                        Log.d(TAG, "handleIntent: action is update");
                        break;
                    case GOTO:
                        switch (intent.getStringExtra(DESTINATION)) {

                            case SAVED_VAL:
                                goToMySavedPosts();
                                break;
                            case CATEGORIES_VAL:
                                mainBottomNav.setSelectedItemId(R.id.bottomNavCatItem);
                                setFragment(categoriesFragment);
                                break;
                            case NOTIFICATIONS_VAL:
                                mainBottomNav.setSelectedItemId(R.id.bottomNavNotificationsItem);
                                setFragment(notificationsFragment);
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
                                intent.getStringExtra(ACTION));
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

    public void showSnack(String message) {
        Snackbar.make(findViewById(R.id.mainSnack), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        doubleBackToExit();
    }

    private void setFragment(Fragment fragment) {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mainFrameContainer, fragment);
        fragmentTransaction.commit();
    }

    public void doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
        }
        doubleBackToExitPressedOnce = true;
        promptExit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void goToUserPage() {
        Intent goToUserPageIntent = new Intent(this, UserPageActivity.class);
        goToUserPageIntent.putExtra(CoMeth.USER_ID, coMeth.getUid());
        startActivity(goToUserPageIntent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_login:
                if (coMeth.isLoggedIn()) {
                    confirmSignOut();
                }else {
                    goToLogin();
                }
                break;
            case R.id.nav_my_profile:
                goToUserPage();
                break;
            case R.id.nav_my_posts:
                goToMyPosts();
                break;
            case R.id.nav_my_saves:
                goToMySavedPosts();
                break;
            case R.id.nav_my_subs:
                openMySubs();
                break;
            case R.id.nav_share:
                shareApp();
                break;
            case R.id.nav_contact:
                sendEmail();
                break;
            case R.id.nav_terms:
                viewTermsAndConditions();
                break;
            case R.id.nav_about:
                showVersionInfo();
                break;
            default:
                Log.d(TAG, "onNavigationItemSelected: at about ");
        }
        mainDrawerLayout.closeDrawers();
        return true;
    }

    private void openMySubs() {
        showProgress(getResources().getString(R.string.loading_text));
        //fetch categories
        coMeth.getDb()
                .collection(USERS + "/" +
                        coMeth.getUid() + "/" +
                        CoMeth.SUBSCRIPTIONS + "/" +
                        CoMeth.CATEGORIES_DOC + "/" +
                        CoMeth.CATEGORIES)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots,
                                        FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //user has cats
                            showCatsDialog(queryDocumentSnapshots);
                        } else {
                            showNoSubsDialog();
                        }
                    }
                });
        coMeth.stopLoading(progressDialog);
    }

    private void showCatsDialog(QuerySnapshot queryDocumentSnapshots) {
        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
            if (doc.getType() == DocumentChange.Type.ADDED) {
                Categories cat = doc.getDocument().toObject(Categories.class);
                //add cat to list
                catSubsArray.add(cat.getValue());
            }
        }
        catsListItems = catSubsArray.toArray((new String[catSubsArray.size()]));
        //stop loading
        coMeth.stopLoading(progressDialog);
        //open an an alert dialog for the sub'd cats
        AlertDialog.Builder catsSubBuilder =
                new AlertDialog.Builder(
                        MainActivity.this);
        catsSubBuilder.setTitle(getString(R.string.categories_text))
                .setIcon(R.drawable.ic_action_categories)
                .setItems(catsListItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //open the view category activity
                        openCat(coMeth.getCatKey(catsListItems[which]));
                    }
                })
                .show();
        //empty the catSubsArray
        catSubsArray.clear();
    }

    private void showNoSubsDialog() {
        AlertDialog.Builder noSubsBuilder = new AlertDialog.Builder(MainActivity.this);
        noSubsBuilder.setTitle(getResources().getString(R.string.categories_text))
                .setIcon(getResources().getDrawable(R.drawable.ic_action_categories))
                .setMessage(getString(R.string.not_subd_to_cats_text) + "\n" +
                        getString(R.string.view_cats_qn_text))
                .setPositiveButton(getString(R.string.view_cats_text),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mainBottomNav.setSelectedItemId(R.id.bottomNavCatItem);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel_text),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }


    private void openCat(String catKey) {
        Intent openCatIntent = new Intent(this, ViewCategoryActivity.class);
        openCatIntent.putExtra(CoMeth.CATEGORY, catKey);
        startActivity(openCatIntent);
        finish();
    }

    private void goToMySavedPosts() {
        mainBottomNav.setSelectedItemId(R.id.bottomNavHomeItem);
        HomeFragment homeFragment = new HomeFragment();
            Bundle args = new Bundle();
            args.putInt(TAB, 2);
            homeFragment.setArguments(args);
            setFragment(homeFragment);

    }

    private void goToMyPosts() {
        Intent goToUserPostsIntent = new Intent(
                MainActivity.this, UserPostsActivity.class);
        goToUserPostsIntent.putExtra(CoMeth.USER_ID, coMeth.getUid());
        goToUserPostsIntent.putExtra(CoMeth.USERNAME, username);
        startActivity(goToUserPostsIntent);
    }

    private void confirmSignOut() {
        android.app.AlertDialog.Builder confirmLogoutBuilder =
                new android.app.AlertDialog.Builder(this);
        confirmLogoutBuilder.setTitle(getString(R.string.logout_text))
                .setIcon(getResources().getDrawable(R.drawable.ic_action_red_alert))
                .setMessage(getString(R.string.confirm_lougout_text))
                .setNegativeButton(getString(R.string.cancel_text),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton(getString(R.string.logout_text),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                signOut();
                                showSnack(getResources().getString(R.string.logged_out_text));
                            }
                        })
                .show();
    }

    private void signOut() {
        //check if user is google user
        FirebaseUser user = coMeth.getAuth().getCurrentUser();
        if (user != null && user.getProviders() != null && user.getProviders().contains(GOOGLE_DOT_COM)) {
            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            // TODO: 6/17/18 this is not working; signing out google users from google acc
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            mGoogleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: signed out from google");
                    coMeth.signOut();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: failed to sign out from google\n" +
                                    e.getMessage());
                            String errorMessage = getString(R.string.failed_to_signout_text) + ": " + e.getMessage();
                            showSnack(errorMessage);
                        }
                    });
        } else {
            //sign out regular non-google user
            coMeth.signOut();
        }
    }

    private void shareApp() {
        Log.d(TAG, "Sharing app");
        showProgress(getResources().getString(R.string.loading_text));
        String appUrl = getResources().getString(R.string.app_download_url);
        final String fullShareMsg = getString(R.string.share_app_mesage_text);

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
                        .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                            @Override
                            public void onSuccess(ShortDynamicLink shortDynamicLink) {
                                Uri shortLink = shortDynamicLink.getShortLink();
                                showShareDialog(shortLink, fullShareMsg);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                coMeth.stopLoading(progressDialog);
                                String errorMessage = getString(R.string.failed_to_share_text) + ": " +
                                        e.getMessage();
                                Log.e(TAG, "onFailure: " + errorMessage, e);
                                showSnack(errorMessage);
                            }
                        });
    }

    private void showShareDialog(Uri shortLink, String fullShareMsg) {
        String shareText = fullShareMsg + "\n" + shortLink;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                getResources().getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        coMeth.stopLoading(progressDialog);
        startActivity(Intent.createChooser(
                shareIntent, getString(R.string.share_with_text)));
    }

    private void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", getString(R.string.family_email), null));
        startActivity(Intent.createChooser(emailIntent, "Contact us"));
    }

    private void showVersionInfo() {
        AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(this);
        String versionName = BuildConfig.VERSION_NAME;
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
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            String message = getResources().getString(R.string.unable_to_find_playstore_text);
            showSnack(message);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.newPostFab:
                handleNewPostFab();
                break;
            default:
                Log.d(TAG, "onClick: main at default");
        }
    }

    public void handleNewPostFab(){
        if (coMeth.isConnected(this) && coMeth.isLoggedIn()) {
            goToCreatePost();
        } else {
            if (!coMeth.isConnected(this)) showSnack(getResources().getString(R.string.failed_to_connect_text));
            if (!coMeth.isLoggedIn()) goToLogin(getString(R.string.login_to_post_text));
        }
    }
}