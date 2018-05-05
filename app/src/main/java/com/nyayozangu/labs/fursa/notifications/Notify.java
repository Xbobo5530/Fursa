package com.nyayozangu.labs.fursa.notifications;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * send notifications
 * Created by Sean on 4/25/18.
 */

public class Notify extends AsyncTask<String, String, Void> {

    private static final String TAG = "Sean";
    private static final String API_KEY = "key=AAAAx83bavk:APA91bHl_bttQCZ9UtkPMnBdz6VIjXj-4BD6S3ZDcUL20153ns6a2Aep0BdU_f0tP5pkeIyEivOyuebqmplIVt1-bhRNtgxQD_SqcmdhBM5DaJg6v0e59gyTvNSkt0RcN9WmgzSTJCtq";
    private String token;

    @Override
    protected Void doInBackground(String... strings) {

        Log.d(TAG, "doInBackground: notifications");

        //initialize contents
        String topic = "/topics/";

        token = FirebaseInstanceId.getInstance().getToken();

        try {

            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject json = new JSONObject();

            // TODO: 4/25/18 specify topics to send notifications to
            //comment updates
            switch (strings[0]) {

                case "comment_updates":

                    String postId = strings[1];
                    //notification type is comment updates
                    topic = topic.concat(postId);
                    json.put("to", topic);
                    passNotifDetails(conn, json, "COMMENTS", postId);
                    Log.d(TAG, "doInBackground: " +
                            "\nnotifType is: COMMENTS" +
                            "\ntopic is: " + topic +
                            "\npostId is: " + postId);
                    break;

                case "saved_posts_updates":

                    postId = strings[1];
                    topic = topic.concat(postId);
                    json.put("to", topic);
                    passNotifDetails(conn, json, "SAVED", postId);
                    Log.d(TAG, "doInBackground: \nnotifType is: SAVED");
                    break;

                case "categories_updates":

                    String catKey = strings[1];
                    topic = topic.concat(catKey);
                    json.put("to", topic);
                    passNotifDetails(conn, json, "CATS", catKey);
                    break;

                default:

                    Log.d(TAG, "doInBackground: at default");

            }

        } catch (Exception e) {
            Log.d(TAG, "Notification error: " + e.getMessage());
        }


        return null;
    }

    private void passNotifDetails(HttpURLConnection conn, JSONObject json, String notifType, String extraInfo) throws JSONException, IOException {
        JSONObject info = new JSONObject();

        //create diff conditions for diff types of notifications


        switch (notifType) {
            case "COMMENTS":
                info.put("title", "New Comments");   // Notification title
                info.put("message", "Check out new comments on the posts you follow"); // Notification body
                info.put("notif_type", "comment_updates");
                info.put("extra", extraInfo);

                Log.d(TAG, "passNotifDetails: \nextraInfo is:" + extraInfo);
                break;
            case "SAVED":
                info.put("title", "Saved Post Updates");   // Notification title
                info.put("message", "Check out new updates on your saved posts"); // Notification body
                info.put("notif_type", "saved_posts_updates");
                info.put("extra", extraInfo);
                break;
            case "CATS":
                info.put("title", "New Posts on Categories you follow");   // Notification title
                info.put("message", "Check out new posts on the categories you follow"); // Notification body
                info.put("notif_type", "categories_updates");
                info.put("extra", extraInfo);
                break;
            default:
                Log.d(TAG, "passNotifDetails: at default");
                info.put("title", "Sharing experiences and opportunities");   // Notification title
                info.put("message", "See what everyone has been sharing aon Fursa"); // Notification body
                break;

        }

        json.put("data", info);

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(json.toString());
        wr.flush();
        conn.getInputStream();


        Log.d(TAG, "doInBackground: sending notification complete");
    }
}
