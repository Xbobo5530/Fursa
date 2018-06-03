package com.nyayozangu.labs.fursa.commonmethods;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;

import java.util.Date;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

interface CheckConnectionInterface {
    void checkConnection(boolean result);
}

/**
 * Created by Sean on 4/29/18.
 * commonly used methods throughout the app
 */

public class CoMeth {

    private static final String TAG = "Sean";
    public final String[] catTitle = new String[]{

            getApplicationContext().getResources().getString(R.string.cat_popular), getApplicationContext().getString(R.string.cat_exhibitions),
            getApplicationContext().getResources().getString(R.string.cat_jobs),
            getApplicationContext().getResources().getString(R.string.cat_buysell), getApplicationContext().getResources().getString(R.string.cat_business),
            getApplicationContext().getResources().getString(R.string.cat_upcoming), getApplicationContext().getResources().getString(R.string.cat_events),
            getApplicationContext().getResources().getString(R.string.cat_places), getApplicationContext().getResources().getString(R.string.cat_services),
            getApplicationContext().getResources().getString(R.string.cat_education), getApplicationContext().getResources().getString(R.string.cat_queries)


    };
    public final String[] categories = new String[]{

            getApplicationContext().getString(R.string.cat_business),
            getApplicationContext().getString(R.string.cat_exhibitions),
            getApplicationContext().getString(R.string.cat_events),
            getApplicationContext().getString(R.string.cat_buysell),
            getApplicationContext().getString(R.string.cat_education),
            getApplicationContext().getString(R.string.cat_jobs),
            getApplicationContext().getString(R.string.cat_services),
            getApplicationContext().getString(R.string.cat_places),
            getApplicationContext().getString(R.string.cat_queries)

    };
    //public methods
    public final String[] catKeys = new String[]{

            "business",
            "exhibitions",
            "events",
            "buysell",
            "education",
            "jobs",
            "services",
            "places",
            "queries"

    };
    public final String[] reportList = new String[]{

            getApplicationContext().getString(R.string.spam_text),
            getApplicationContext().getString(R.string.inapropriate_text)

    };
    public final String[] reportListKey = new String[]{

            "spam",
            "inappropriate"

    };
    private boolean hasInternet;
    public int minVerCode = 14;

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

    /**
     * Checks for the connection status
     * if the device has a connection
     * and the connection is active
     *
     * @return boolean true if device is connected and has internet
     * false if device can not connet to the internet
     */
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


        // TODO: 5/9/18 check if connection has internet

        /*CheckInternetTask task = new CheckInternetTask(new CheckConnectionInterface() {
            @Override
            public void checkConnection(boolean result) {
                //check connection status
                Log.d(TAG, "checkConnection: \nresult is: " + result);
                hasInternet = result;
                Log.d(TAG, "checkConnection: has internet: " + hasInternet);
            }
        });
        task.execute();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Log.d(TAG, "isConnected: " +
                "\nisConnected: " + isConnected +
                "\nhasInternet: " + hasInternet);*/


        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected /*&& hasInternet*/;

    }

    /**
     * A method to get the Firestore database
     *
     * @return a FirebaseFirestore instance
     */
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

    public StorageReference getStorageRef() {
        return FirebaseStorage.getInstance().getReference();
    }

    public void setImage(int placeholderDrawable, String imageUrl, ImageView targetImageView) {
        Log.d(TAG, "setImage: no thumb");
        RequestOptions placeHolderRequest = new RequestOptions();
        placeHolderRequest.placeholder(placeholderDrawable);
        //loading the string for url to the image view
        Glide.with(getApplicationContext())
                .setDefaultRequestOptions(placeHolderRequest)
                .load(imageUrl)
                .into(targetImageView);
    }

    public void setImage(int placeholderDrawable, String imageUrl, String thumbUrl, ImageView targetImageView) {
        Log.d(TAG, "setImage: with thumb");
        RequestOptions placeHolderOptions = new RequestOptions();
        placeHolderOptions.placeholder(R.drawable.appiconshadow);
        Glide.with(getApplicationContext())
                .applyDefaultRequestOptions(placeHolderOptions)
                .load(imageUrl)
                .thumbnail(Glide.with(getApplicationContext()).load(thumbUrl))
                .into(targetImageView);
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
            case "Services":
                return "services";
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
            case "Exhibitions":
                return "exhibitions";

            //handle swahili items
            case "Spesheli":
                return "featured";
            case "Mchapisho maarufu":
                return "popular";
            case "Zifuatazo":
                return "upcoming";
            case "Matukio":
                return "events";
            case "Maeneo":
                return "places";
            case "Huduma":
                return "services";
            case "Biashara":
                return "business";
            case "Kununua na kuuza":
                return "buysell";
            case "Elimu":
                return "education";
            case "Nafasi za kazi":
                return "jobs";
            case "Maswali na majibu":
                return "queries";
            case "Maonyesho ya biashara":
                return "exhibitions";
            default:
                return null;
        }

    }

    public String getCatValue(String catValue) {

            /*
            "Featured",
            "Popular",
            "UpComing",
            "Events",
            "Places",
            "Services",
            "Business",
            "Buy and sell",
            "Education",
            "Jobs",
            "Queries"
            "Exhibitions"*/


        //return value for key
        switch (catValue) {

            case "featured":
                return getApplicationContext().getString(R.string.cat_featured);

            case "popular":
                return getApplicationContext().getString(R.string.cat_popular);

            case "upcoming":
                return getApplicationContext().getString(R.string.cat_upcoming);

            case "events":
                return getApplicationContext().getString(R.string.cat_events);

            case "places":
                return getApplicationContext().getString(R.string.cat_places);

            case "services":
                return getApplicationContext().getString(R.string.cat_services);

            case "business":
                return getApplicationContext().getString(R.string.cat_business);

            case "buysell":
                return getApplicationContext().getString(R.string.cat_buysell);

            case "education":
                return getApplicationContext().getString(R.string.cat_education);

            case "jobs":
                return getApplicationContext().getString(R.string.cat_jobs);

            case "queries":
                return getApplicationContext().getString(R.string.cat_queries);

            case "exhibitions":
                return getApplicationContext().getString(R.string.cat_exhibitions);

            default:
                Log.d(TAG, "getCatValue: default");
                return "";

        }
    }

    public String processPostDate(long millis) {

        //get current date
        long now = new Date().getTime();
        long timeLapsed = (now - millis) / (1000 * 60);
        //minutes ago
        if (timeLapsed < 1) {

            //when less than a min has passed
            return getApplicationContext().getString(R.string.min_ago_text);

        } else if (timeLapsed > 1 && timeLapsed < 60) {

            //when more than a min but less than an hour has lapsed
            return String.valueOf(timeLapsed) + " " + getApplicationContext().getString(R.string.mins_ago_text);

        } else if (timeLapsed >= 60 && timeLapsed <= 120) {

            //when more than an hour, less than 2 has lapsed
            return getApplicationContext().getString(R.string.hr_ago_text);

        } else if (timeLapsed / 60 > 1 && timeLapsed / 60 < 24) {

            //when more than n hour, less than a day has lapsed
            return timeLapsed / 60 + " " + getApplicationContext().getString(R.string.hrs_ago_text);

        } else if (timeLapsed / (60 * 24) >= 1 && timeLapsed / (60 * 24) < 2) {

            //when a day has lapsed, less than 2 days
            return getApplicationContext().getResources().getString(R.string.yesterday_text);

        } else if (timeLapsed / (60 * 24) >= 2 && timeLapsed / (60 * 24) < 7) {

            //when more than 2 days, less than a week has lapsed
            return timeLapsed / (60 * 24) + " " + getApplicationContext().getString(R.string.day_ago_text);

        } else if (timeLapsed / (60 * 24 * 7) > 1 && timeLapsed / (60 * 24 * 7) <= 4) {

            //when more than a week, less than a month have lapsed
            return timeLapsed / (60 * 24 * 7) + " " + getApplicationContext().getString(R.string.weeks_ago_text);

        } else if (timeLapsed / (60 * 24 * 7 * 4) > 1 && timeLapsed / (60 * 24 * 7 * 4) <= 12) {

            //when more than a month, less than a year has lapsed
            return timeLapsed / (60 * 24 * 7 * 4) + " " + getApplicationContext().getString(R.string.months_ago_text);

        } else {

            //when more than a year has lapsed
            return DateFormat.format("EEE, MMM d, 20yy\nh:mm a", new Date(millis)).toString();

        }
    }

    public void stopLoading(ProgressDialog progressDialog,
                            SwipeRefreshLayout swipeRefreshLayout) {

        Log.d(TAG, "stopLoading: stopping");
        if (progressDialog != null) {

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

        }
        if (swipeRefreshLayout != null) {

            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }

        }

    }

    public void stopLoading(ProgressDialog progressDialog) {

        Log.d(TAG, "stopLoading: stopping");
        if (progressDialog != null) {

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public void stopLoading(SwipeRefreshLayout swipeRefreshLayout) {

        if (swipeRefreshLayout != null) {

            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }

        }

    }

    public void onResultStopLoading(List<Posts> postList,
                                    ProgressDialog progressDialog,
                                    SwipeRefreshLayout swipeRefreshLayout) {

        Log.d(TAG, "onResultStopLoading: ");
        if (postList.size() > 0) {
            this.stopLoading(progressDialog, swipeRefreshLayout);
        }
    }

    public void onResultStopLoading(List<Posts> postList,
                                    ProgressDialog progressDialog) {

        Log.d(TAG, "onResultStopLoading: ");
        if (postList.size() > 0) {
            this.stopLoading(progressDialog);
        }
    }

    /**
     * checks if device screen is small, normal, large or large and sets posts
     *
     * @param activity     the activity the posts are shown in
     * @param context      the context the posts are displayed
     * @param recyclerView the recycler view to alter
     */
    public void handlePostsView(Context context, Activity activity, RecyclerView recyclerView) {

        Log.d(TAG, "handlePostsView: ");
        if ((context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            // on a large screen device ...
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        } else if ((context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            //on xlarge device
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));


        } else {
            //on small, normal or undefined screen devices
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        }
    }

    public void showProgress(String message, ProgressDialog progressDialog, Context context) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

//    public String[] generatePostTags(String textToProcess) {
//        return textToProcess.split("\\W+");
//    }


}

/*class CheckInternetTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "Sean";
    private CheckConnectionInterface mListener;
    private HttpURLConnection urlc = null;

    public CheckInternetTask(CheckConnectionInterface mListener) {
        this.mListener = mListener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        Log.d(TAG, "doInBackground: ");
        try {
            urlc = (HttpURLConnection) (new URL("http://google.com").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            Log.d(TAG, "doInBackground: \nurlc.getResponseCode() == 200 " + (urlc.getResponseCode() == 200));
            Log.d(TAG, "doInBackground: used the return inside");
            return urlc.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "doInBackground: used the return outside");
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Log.d(TAG, "onPostExecute: ");
        if (mListener != null)
            mListener.checkConnection(result);
    }
}*/

