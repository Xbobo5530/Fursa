package com.nyayozangu.labs.fursa.commonmethods;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Sean on 4/29/18.
 * commonly used methods throughout the app
 */

public class CoMeth {

    private static final String TAG = "Sean";

    public CoMeth() {
    } //empty constructor

    //public methods

    //is logged in
    public boolean isLoggedIn() {
        //firebase auth
        FirebaseAuth mAuth;
        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        //determine if user is logged in
        return mAuth.getCurrentUser() != null;
    }


    //is connected
    public boolean isConnected() {

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

    public FirebaseFirestore getDb() {

        // Access a Cloud Firestore instance from your Activity
        return FirebaseFirestore.getInstance();

    }


    public FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public String getUid() {
        return this.getAuth().getUid();
    }

    public void signOut() {

        this.getAuth().signOut();

    }

    /*public void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog.setMessage(message);
        progressDialog.show();
    }*/
}
