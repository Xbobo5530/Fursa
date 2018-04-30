package com.nyayozangu.labs.fursa.commonmethods;

import android.content.Context;
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


}
