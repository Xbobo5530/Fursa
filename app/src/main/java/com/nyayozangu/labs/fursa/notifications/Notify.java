package com.nyayozangu.labs.fursa.notifications;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sean on 4/25/18.
 */

public class Notify extends AsyncTask<Void, Void, Void> {


    private static final String TAG = "Sean";
    private static final String API_KEY = "key=AAAAx83bavk:APA91bHl_bttQCZ9UtkPMnBdz6VIjXj-4BD6S3ZDcUL20153ns6a2Aep0BdU_f0tP5pkeIyEivOyuebqmplIVt1-bhRNtgxQD_SqcmdhBM5DaJg6v0e59gyTvNSkt0RcN9WmgzSTJCtq";
    private String token;

    @Override
    protected Void doInBackground(Void... voids) {

        Log.d(TAG, "doInBackground: notifications");

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

            json.put("to", token);


            JSONObject info = new JSONObject();
            info.put("title", "Fursa");   // Notification title
            info.put("body", "Hello Test notification"); // Notification body

            json.put("notification", info);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(json.toString());
            wr.flush();
            conn.getInputStream();

            Log.d(TAG, "doInBackground: sending notification complete");

        } catch (Exception e) {
            Log.d(TAG, "Notification error: " + e.getMessage());
        }


        return null;
    }
}
