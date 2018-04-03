package com.nyayozangu.labs.fursa;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "Sean";
    private FirebaseAuth mAuth;

    private Toolbar mainToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "at MainActivity, onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);


        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        mainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Main Feed");


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
            default:
                return false;
        }
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

        if (currentUser == null) {

            Log.d(TAG, "currentUser is: " + currentUser);

            //user is not logged in
            //send to sing log in page
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

    }

    private void updateUI(FirebaseUser currentUser) {
        // TODO: 3/31/18 sing in current user
    }


    public void createAccount(String userName, String password) {

    }


}
