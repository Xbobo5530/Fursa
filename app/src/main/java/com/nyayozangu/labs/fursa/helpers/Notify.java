package com.nyayozangu.labs.fursa.helpers;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.CATEGORIES_UPDATES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.CATS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.COMMENTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.COMMENT_UPDATES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.ERROR;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.EXTRA;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWER_POST;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.LIKES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.LIKES_UPDATES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MESSAGE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NEW_FOLLOWERS_UPDATE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NEW_POST_UPDATES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NOTIF_TYPE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SAVED;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SAVED_POSTS_UPDATES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TITLE;

/**
 * send notifications
 * Created by Sean on 4/25/18.
 */

public class Notify extends AsyncTask<String, String, Void> {

    private static final String TAG = "Sean";
    private static final String API_KEY = "key=AAAAx83bavk:APA91bHl_bttQCZ9UtkPMnBdz6VIjXj-4BD6S3ZDcUL20153ns6a2Aep0BdU_f0tP5pkeIyEivOyuebqmplIVt1-bhRNtgxQD_SqcmdhBM5DaJg6v0e59gyTvNSkt0RcN9WmgzSTJCtq";

    // TODO: 5/19/18 handle strings with string resources
    @Override
    protected Void doInBackground(String... strings) {

        Log.d(TAG, "doInBackground: notifications");

        //initialize contents
        String topic = "/topics/";

        String token = FirebaseInstanceId.getInstance().getToken();

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

                case COMMENT_UPDATES:

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

                case LIKES_UPDATES:

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

                case SAVED_POSTS_UPDATES:

                    postId = strings[1];
                    topic = topic.concat(postId);
                    json.put("to", topic);
                    passNotifDetails(conn, json, "SAVED", postId);
                    Log.d(TAG, "doInBackground: \nnotifType is: SAVED");
                    break;

                case CATEGORIES_UPDATES:

                    String catKey = strings[1];
                    topic = topic.concat(catKey);
                    json.put("to", topic);
                    passNotifDetails(conn, json, "CATS", catKey);
                    break;

                case NEW_POST_UPDATES:
                    Log.d(TAG, "doInBackground: notify got new post updates");

                    String postReadyTopic = strings[1];
                    if (strings[2].equals(CoMeth.SUCCESS)) {
                        String newPostId = strings[3];
                        Log.d(TAG, "doInBackground: new post id is: " + newPostId +
                                "\npost ready topic is " + postReadyTopic);
                        topic = topic.concat(postReadyTopic);
                        json.put("to", topic);
                        passNotifDetails(conn, json, NEW_POST_UPDATES, newPostId);
                    } else {
                        //failed to post
                        String errorMessage = strings[2];
                        topic = topic.concat(postReadyTopic);
                        json.put("to", topic);

                        JSONObject info = new JSONObject();
                        info.put(TITLE, "Sorry, Something went wrong");
                        info.put(MESSAGE, "Could not submit your post: "
                                + errorMessage);
                        info.put(NOTIF_TYPE, NEW_POST_UPDATES);
                        info.put(ERROR, errorMessage);

                        json.put("data", info);

                        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                        wr.write(json.toString());
                        wr.flush();
                        conn.getInputStream();
                    }
                    break;

                case FOLLOWER_POST:
                    String followerPostTopic = strings[1];
                    String newPostId = strings[2];
                    topic = topic.concat(followerPostTopic);
                    json.put("to", topic);
                    passNotifDetails(conn, json, FOLLOWER_POST, newPostId);
                    break;

                case NEW_FOLLOWERS_UPDATE:
                    String followerUserId = strings[2];
                    String mTopic = strings[1];
                    topic = topic.concat(mTopic);
                    json.put("to", topic);
                    passNotifDetails(conn, json, NEW_FOLLOWERS_UPDATE, followerUserId);
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
        if (coMeth.isLoggedIn()) {
            info.put("userId", coMeth.getUid());
        }

        //create diff conditions for diff types of notifications
        switch (notifType) {
            case COMMENTS:
                info.put(TITLE, "New Comments");
                info.put(MESSAGE, "Check out new comments on the posts you follow");
                info.put(NOTIF_TYPE, COMMENT_UPDATES);
                info.put(EXTRA, extraInfo);
                Log.d(TAG, "passNotifDetails: \nextraInfo is:" + extraInfo);
                break;

            case LIKES:
                info.put(TITLE, "New Likes");
                info.put(MESSAGE, "Check out new Likes on the posts you follow");
                info.put(NOTIF_TYPE, LIKES_UPDATES);
                info.put(EXTRA, extraInfo);

                Log.d(TAG, "passNotifDetails: \nextraInfo is:" + extraInfo);
                break;

            case SAVED:
                info.put(TITLE, "Saved Post Updates");
                info.put(MESSAGE, "Check out new updates on your saved posts");
                info.put(NOTIF_TYPE, SAVED_POSTS_UPDATES);
                info.put(EXTRA, extraInfo);
                break;

            case CATS:
                info.put(TITLE, "New Post on Category you follow");
                info.put(MESSAGE, "Check out new posts on the categories you follow");
                info.put(NOTIF_TYPE, CATEGORIES_UPDATES);
                info.put(EXTRA, extraInfo);
                break;

            case FOLLOWER_POST:
                info.put(TITLE, "New Post people you are following");
                info.put(MESSAGE, "Someone you are following has published a new post");
                info.put(NOTIF_TYPE, FOLLOWER_POST);
                info.put(EXTRA, extraInfo);
                break;

            case NEW_POST_UPDATES:
                info.put(TITLE, "Your post is ready");
                info.put(MESSAGE, "You can now view, edit or share your post with everyone");
                info.put(NOTIF_TYPE, NEW_POST_UPDATES);
                info.put(EXTRA, extraInfo);
                break;

            case NEW_FOLLOWERS_UPDATE:
                info.put(TITLE, "You have a new follower");
                info.put(MESSAGE, "Someone new is now following your page");
                info.put(NOTIF_TYPE, NEW_FOLLOWERS_UPDATE);
                info.put(EXTRA, extraInfo);
                break;

            default:
                Log.d(TAG, "passNotifDetails: at default");
                info.put(TITLE, "Sharing experiences and opportunities");
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
