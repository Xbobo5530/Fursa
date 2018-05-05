package com.nyayozangu.labs.fursa.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.categories.ViewCategoryActivity;
import com.nyayozangu.labs.fursa.activities.comments.CommentsActivity;
import com.nyayozangu.labs.fursa.activities.comments.models.Comments;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;

/**
 *
 *
 * Created by Sean on 4/24/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private PendingIntent pendingIntent;
    private Uri defaultSoundUri;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "onMessageReceived: ");
        Log.d(TAG, remoteMessage.toString());

        if (remoteMessage.getData().size() > 0) {

            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            String message = data.get("message");
            String notifType = data.get("notif_type");
            String extraInfo = data.get("extra").trim();
            Log.d(TAG, "onMessageReceived: Message Received: \n" +
                    "Title: " + title + "\n" +
                    "Message: " + message + "\n" +
                    "Notification type: " + notifType + "\n" +
                    "extraInfo: " + extraInfo);

            if (!extraInfo.isEmpty()) {
                sendNotification(title, message, notifType, extraInfo);
            } else {
                sendNotification(title, message, null, null);
            }
        } else if (remoteMessage.getNotification() != null) {
            //has no data probably comes from database
            Log.d(TAG, "remoteMessage has no 'Data'");
            String title = remoteMessage.getNotification().getTitle();
            String message = remoteMessage.getNotification().getBody();

            Log.d(TAG, "title is: " + title + "\nmessage is: " + message);

            if (title != null && message != null) {
                sendNotification(title, message, null, null);
            } else {
                title = "Nyayo Zangu Store";
                message = "Sharing experiences and opportunities";
                sendNotification(title, message, null, null);
            }
        } else {
            //other weird cases
            Log.d(TAG, "data is null, notification is null");
            String title = "Nyayo Zangu Store";
            String message = "Sharing experiences and opportunities";
            sendNotification(title, message, null, null);
        }
    }


    /**
     * sends the notification
     *
     * @param title       the title of the notification
     * @param messageBody the message of the notification
     * @param extraInfo   the url to open when the notification is opened
     */
    private void sendNotification(final String title, final String messageBody, String notifType, String extraInfo) {
        Log.d(TAG, "at sendNotification");

        if (notifType != null) {
            switch (notifType) {

                case "comment_updates":

                    Intent commentsNotifIntent = new Intent(this, CommentsActivity.class);
                    commentsNotifIntent.putExtra("postId", extraInfo);
                    commentsNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, commentsNotifIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    final ArrayList<Comments> commentsList = new ArrayList<>();

                    //pass comment details
                    coMeth.getDb()
                            .collection("Posts")
                            .document(extraInfo)
                            .collection("Comments")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                                    if (!queryDocumentSnapshots.isEmpty()) {

                                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                                Comments comment = doc.getDocument().toObject(Comments.class);
                                                commentsList.add(comment);

                                            }

                                        }

                                        //get the comments list and get the latest comment
                                        final String latestComment = commentsList.get(0).getComment();
                                        Log.d(TAG, "onEvent: latest comment is " + latestComment);
                                        String commentUserId = commentsList.get(0).getUser_id();

                                        coMeth.getDb()
                                                .collection("Users")
                                                .document(commentUserId)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                        if (task.isSuccessful() && task.getResult().exists()) {

                                                            //convert user to object
                                                            Users user = task.getResult().toObject(Users.class);
                                                            String username = user.getName();
                                                            String image = user.getImage();

                                                            buildNotif(title, username + "\n" + latestComment, image);

                                                        } else {
                                                            if (!task.isSuccessful()) {
                                                                //getting user task failed
                                                                Log.d(TAG, "onComplete: getting user task failed");

                                                            }
                                                            if (!task.getResult().exists()) {
                                                                //comment does not exist
                                                                Log.d(TAG, "onComplete: comment does not exist");

                                                            }
                                                        }
                                                    }
                                                });


                                    }

                                }
                            });


                    break;

                case "categories_updates":

                    Intent catsNotifIntent = new Intent(this, ViewCategoryActivity.class);
                    catsNotifIntent.putExtra("category", extraInfo);
                    catsNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, catsNotifIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    buildNotif(title, messageBody, null);
                    break;

                default:

                    Log.d(TAG, "sendNotification: at default");
                    Intent noExtraNotifIntent = new Intent(this, MainActivity.class);
                    noExtraNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, noExtraNotifIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    buildNotif(title, messageBody, null);

            }
        } else {

            //notif and extra are null

            Intent noExtraNotifIntent = new Intent(this, MainActivity.class);
            noExtraNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, noExtraNotifIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            buildNotif(title, messageBody, null);

        }


    }

    // TODO: 5/6/18 handle images on notifications
    private void buildNotif(String title, String messageBody, String userImageDownloadUrl) {
        defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setColor(getResources().getColor(R.color.colorPrimaryDark))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getResources(), R.drawable.appic))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(messageBody));

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NotificationChannel.DEFAULT_CHANNEL_ID,
                    getString(R.string.default_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }

    @Override
    public void onDeletedMessages() {

    }
}
