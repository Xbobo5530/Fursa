package com.nyayozangu.labs.fursa;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    // TODO: 4/4/18 handle read later feature
    // TODO: 4/4/18  add a floating search bar with user
    // TODO: 4/4/18 remove action bar
    // TODO: 4/4/18 add bottom navigation
    // TODO: 4/4/18 handle feed, categories and read later fragments

    private static final String TAG = "Sean";
    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    //users
    private String currentUserId;
    private Toolbar mainToolbar;
    private FloatingActionButton mNewPost;
    private BottomNavigationView mainBottomNav;

    //instances of fragments
    private HomeFragment homeFragment;
    private CategoriesFragment categoriesFragment;
    private SavedFragment savedFragment;





    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        Log.d(TAG, "at MainActivity, onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        //initiate fragments
        homeFragment = new HomeFragment();
        categoriesFragment = new CategoriesFragment();
        savedFragment = new SavedFragment();

        //initiate elements
        mainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Main Feed");
        mNewPost = findViewById(R.id.newPostFab);
        mainBottomNav = findViewById(R.id.mainBottomNav);


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
                        setFragment(savedFragment);
                        return true;
                    default:
                        return false;
                }

            }
        });


        mNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start the new post activity
                startActivity(new Intent(MainActivity.this, NewPostActivity.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.actions_logout:
                logout();
                return true;
            case R.id.action_settings:
                //go to account page
                goToAccount();

            default:
                return false;
        }
    }

    private void goToAccount() {
        //go to Account page
        startActivity(new Intent(MainActivity.this, AccountActivity.class));
    }

    private void logout() {
        mAuth.signOut();
        //send user to login page
        Intent goToLoginIntent = new Intent(this, LoginActivity.class);
        startActivity(goToLoginIntent);
    }

    //check to see if the user is logged in
    @Override
    public void onStart() {

        Log.d(TAG, "at onStart");

        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        //check if user is logged in
        if (currentUser == null) {
            //user is not logged in
            Log.d(TAG, "currentUser is: " + currentUser);
            //send to login page
            sendToLogin();
        } else {
            //user is signed in
            //check if user exists in db
            currentUserId = mAuth.getCurrentUser().getUid();

            db.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    //check if user exists
                    if (task.isSuccessful()) {

                        //check is user exist
                        if (!task.getResult().exists()) {
                            //user does not exist
                            Log.d(TAG, "user exists");
                            //send user to login activity
                            sendToLogin();
                        } else {
                            //user exists
                            Log.d(TAG, "user exists");

                        }

                    } else {
                        //task was not successful
                        //handle error
                        String errorMessage = task.getException().getMessage();
                        Log.d(TAG, "failed to get user\n error message is: " + errorMessage);
                        Toast.makeText(MainActivity.this, "Failed to get user detials: " + errorMessage, Toast.LENGTH_LONG).show();
                    }

                }
            });
        }

    }

    private void sendToLogin() {
        Log.d(TAG, "at sendToLogin()");
        //send to sing log in page
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void updateUI(FirebaseUser currentUser) {
        // TODO: 3/31/18 sing in current user
    }


    private void setFragment(Fragment fragment) {

        //begin transaction
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrameContainer, fragment);
        fragmentTransaction.commit();

    }


}
