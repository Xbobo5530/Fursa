package com.nyayozangu.labs.fursa.notifications;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

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

    // TODO: 5/19/18 handle strings with string resources
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

            //comment updates
            switch (strings[0]) {

                case CoMeth.COMMENT_UPDATES:

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

                case CoMeth.LIKES_UPDATES:

                    postId = strings[1];
                    //notification type is likes updates
                    topic = topic.concat(postId);
                    json.put("to", topic);
                    passNotifDetails(conn, json, "LIKES", postId);
                    Log.d(TAG, "doInBackground: " +
                            "\nnotifType is: LIKES" +
                            "\ntopic is: " + topic +
                            "\npostId is: " + postId);
                    break;

                case CoMeth.SAVED_POSTS_UPDATES:

                    postId = strings[1];
                    topic = topic.concat(postId);
                    json.put("to", topic);
                    passNotifDetails(conn, json, "SAVED", postId);
                    Log.d(TAG, "doInBackground: \nnotifType is: SAVED");
                    break;

                case CoMeth.CATEGORIES_UPDATES:

                    String catKey = strings[1];
                    topic = topic.concat(catKey);
                    json.put("to", topic);
                    passNotifDetails(conn, json, "CATS", catKey);
                    break;

                case CoMeth.NEW_POST_UPDATES:
                    Log.d(TAG, "doInBackground: notify got new post updates");
                    String postReadyTopic = strings[1];
                    String newPostId = strings[2];
                    Log.d(TAG, "doInBackground: new post id is: " + newPostId +
                            "\npost ready topic is " + postReadyTopic);
                    topic = topic.concat(postReadyTopic);
                    json.put("to", topic);
                    passNotifDetails(conn, json, CoMeth.NEW_POST_UPDATES, newPostId);
                    break;

                default:
                    Log.d(TAG, "doInBackground: in notify at default");
            }

        } catch (Exception e) {
            Log.d(TAG, "Notification error: " + e.getMessage());
        }

        return null;
    }

    private void passNotifDetails(HttpURLConnection conn,
                                  JSONObject json,
                                  String notifType,
                                  String extraInfo) throws JSONException, IOException {
        JSONObject info = new JSONObject();
        //pass the currentUserIt
        CoMeth coMeth = new CoMeth();
        if (coMeth.isConnected() && coMeth.isLoggedIn()) {
            info.put("userId", coMeth.getUid());
        }

        //create diff conditions for diff types of notifications
        switch (notifType) {
            case CoMeth.COMMENTS:
                info.put(CoMeth.TITLE, "New Comments");
                info.put(CoMeth.MESSAGE, "Check out new comments on the posts you follow");
                info.put(CoMeth.NOTIF_TYPE, "comment_updates");
                info.put(CoMeth.EXTRA, extraInfo);
                Log.d(TAG, "passNotifDetails: \nextraInfo is:" + extraInfo);
                break;

            case CoMeth.LIKES:
                info.put(CoMeth.TITLE, "New Likes");
                info.put(CoMeth.MESSAGE, "Check out new Likes on the posts you follow");
                info.put(CoMeth.NOTIF_TYPE, "likes_updates");
                info.put(CoMeth.EXTRA, extraInfo);

                Log.d(TAG, "passNotifDetails: \nextraInfo is:" + extraInfo);
                break;

            case CoMeth.SAVED:
                info.put(CoMeth.TITLE, "Saved Post Updates");
                info.put(CoMeth.MESSAGE, "Check out new updates on your saved posts");
                info.put(CoMeth.NOTIF_TYPE, "saved_posts_updates");
                info.put(CoMeth.EXTRA, extraInfo);
                break;

            case CoMeth.CATS:
                info.put(CoMeth.TITLE, "New Posts on Categories you follow");
                info.put(CoMeth.MESSAGE, "Check out new posts on the categories you follow");
                info.put(CoMeth.NOTIF_TYPE, "categories_updates");
                info.put(CoMeth.EXTRA, extraInfo);
                break;

            case CoMeth.NEW_POST_UPDATES:
                info.put(CoMeth.TITLE, "Your post is ready");
                info.put(CoMeth.MESSAGE, "You can now view, edit or share your post with everyone");
                info.put(CoMeth.NOTIF_TYPE, CoMeth.NEW_POST_UPDATES);
                info.put(CoMeth.EXTRA, extraInfo);
                break;

            default:
                Log.d(TAG, "passNotifDetails: at default");
                info.put(CoMeth.TITLE, "Sharing experiences and opportunities");
                info.put("message", "See what everyone has been sharing aon Fursa");
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
