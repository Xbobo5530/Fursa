package com.nyayozangu.labs.fursa.commonmethods;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Sean on 4/29/18.
 * commonly used methods throughout the app
 */

public class CoMeth {

    private static final String TAG = "Sean";
    public final String[] categories = new String[]{

            "Business",
            "Events",
            "Buying and selling",
            "Education",
            "Jobs",
            "Places",
            "Queries"

    };

    //public methods
    public final String[] catKeys = new String[]{

            "business",
            "events",
            "buysell",
            "education",
            "jobs",
            "places",
            "queries"

    };
    public final String[] reportList = new String[]{

            "Spam",
            "Inappropriate content"

    };

    public final String[] reportListKey = new String[]{

            "spam",
            "inappropriate"

    };

    public CoMeth() {
    } //empty constructor

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

    /*public void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog.setMessage(message);
        progressDialog.show();
    }*/

    public String getUid() {
        return this.getAuth().getUid();
    }

    public void signOut() {

        this.getAuth().signOut();

    }

    public StorageReference getStorageRef() {

        return FirebaseStorage.getInstance().getReference();

    }

    public void setImage(int placeholderDrawable, String imageUrl, ImageView targetImageView) {
        RequestOptions placeHolderRequest = new RequestOptions();
        placeHolderRequest.placeholder(placeholderDrawable);
        //loading the string for url to the image view
        Glide.with(getApplicationContext())
                .setDefaultRequestOptions(placeHolderRequest)
                .load(imageUrl)
                .into(targetImageView);
    }


    public void showLoginAlertDialog(String message) {
        //Prompt user to log in
        android.support.v7.app.AlertDialog.Builder loginAlertBuilder = new android.support.v7.app.AlertDialog.Builder(getApplicationContext());
        loginAlertBuilder.setTitle("Login")
                .setIcon(getApplicationContext().getDrawable(R.drawable.ic_action_alert))
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

    public void goToLogin() {

        getApplicationContext().startActivity(new Intent(getApplicationContext(), LoginActivity.class));

    }


    public String getCatKey(String catValue) {

        switch (catValue) {

            case "Featured":
                return "featured";
            case "Popular":
                return "popular";
            case "Up Coming":
                return "upcoming";
            case "Events":
                return "events";
            case "Places":
                return "places";
            case "Business":
                return "business";
            case "Buy and sell":
                return "buysell";
            case "Education":
                return "education";
            case "Jobs":
                return "jobs";
            case "Queries":
                return "queries";
            default:
                return null;
        }

    }


}
