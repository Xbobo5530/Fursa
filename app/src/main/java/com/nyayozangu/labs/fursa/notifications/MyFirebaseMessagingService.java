package com.nyayozangu.labs.fursa.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.categories.ViewCategoryActivity;
import com.nyayozangu.labs.fursa.activities.comments.CommentsActivity;
import com.nyayozangu.labs.fursa.activities.comments.models.Comments;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.ViewPostActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 *
 *
 * Created by Sean on 4/24/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "Sean";
    private static final String CHANNEL_ID = "UPDATES";
    private static final String KEY_TEXT_REPLY = "key_text_reply";
    private String GROUP_KEY_FURSA = "com.nyayozangu.labs.fursa.COMMENTS";

    private CoMeth coMeth = new CoMeth();
    private PendingIntent pendingIntent;
    private Uri defaultSoundUri;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    private String title;
    private String message;
    private String notifType;
    private String extraInfo;
    private String userId;
    private int notifId;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO: 5/23/18 do code review
        Log.d(TAG, "onMessageReceived: ");
        Log.d(TAG, remoteMessage.toString());

        notifId = createNotifId();

        if (remoteMessage.getData().size() > 0) {

            Log.d(TAG, "onMessageReceived: message has data");

            Map<String, String> data = remoteMessage.getData();
            title = data.get("title");
            message = data.get("message");
            if (data.get("notif_type") != null) {
                notifType = data.get("notif_type");
                //all notifs with notif_type must have userIds
                userId = data.get("userId");
            }
            if (data.get("extra") != null) {
                extraInfo = data.get("extra").trim();
            }
            Log.d(TAG, "onMessageReceived: Message Received: \n" +
                    "Title: " + title + "\n" +
                    "Message: " + message + "\n" +
                    "Notification type: " + notifType + "\n" +
                    "extraInfo: " + extraInfo);
            // TODO: 6/15/18 code review
            if (!extraInfo.isEmpty()) {
                if (notifType.equals(CoMeth.NEW_POST_UPDATES) ||
                        (coMeth.isLoggedIn() && !userId.equals(coMeth.getUid()))) {
                    Log.d(TAG, "onMessageReceived: sending notif to 'other' users");
                    sendNotification(title, message, notifType, extraInfo, notifId);
                }
            } else {
                if (userId != null &&
                        !userId.equals(coMeth.getUid())) {
                    sendNotification(title, message, null, null, notifId);
                }
            }
        } else if (remoteMessage.getNotification() != null &&
                remoteMessage.getData().size() == 0) {
            //has no data probably comes from database
            Log.d(TAG, "remoteMessage has no 'Data'");
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();

            Log.d(TAG, "title is: " + title + "\nmessage is: " + message);

            if (title != null && message != null) {
                sendNotification(title, message, null, null, notifId);
            } else {
                title = getResources().getString(R.string.app_name);
                message = getResources().getString(R.string.sharing_opp_text);
                sendNotification(title, message, null, null, notifId);
            }
        } else {
            //other weird cases
            Log.d(TAG, "data is null, notification is null");
            title = getResources().getString(R.string.app_name);
            message = getResources().getString(R.string.sharing_opp_text);
            sendNotification(title, message, null, null, notifId);
        }
    }


    /**
     * sends the notification
     * @param title the title of the notification
     * @param messageBody the message of the notification
     * @param extraInfo the url to open when the notification is opened
     */
    private void sendNotification(final String title,
                                  final String messageBody,
                                  String notifType,
                                  String extraInfo,
                                  final int notifId) {
        Log.d(TAG, "at sendNotification");

        if (notifType != null) {
            switch (notifType) {

                case CoMeth.COMMENT_UPDATES:
                    //handle comment notifications
                    handleCommentNotif(title, extraInfo, notifId);
                    break;

                case CoMeth.CATEGORIES_UPDATES:
                    //handle cat notifications
                    handleCatNotif(title, messageBody, extraInfo, notifId);
                    break;

                case CoMeth.LIKES_UPDATES:
                    //handle likes notifications
                    handleLikesNotif(title, messageBody, extraInfo, notifId);
                    break;

                case CoMeth.NEW_POST_UPDATES:
                    //handle notification for new post
                    handleNewPostNotif(title, messageBody, extraInfo, notifId);
                    break;

                default:

                    Log.d(TAG, "sendNotification: at default");
                    Intent noExtraNotifIntent = new Intent(this, MainActivity.class);
                    noExtraNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(
                            this, notifId /* Request code */, noExtraNotifIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    buildNotif(title, messageBody, null, notifId);

            }
        } else {

            //notif and extra are null
            Intent noExtraNotifIntent = new Intent(this, MainActivity.class);
            noExtraNotifIntent.addFlags(
                    Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(
                    this, notifId /* Request code */, noExtraNotifIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            buildNotif(title, messageBody, null, notifId);
        }

    }

    private void handleNewPostNotif(String title, String messageBody, String extraInfo, int notifId) {
        Log.d(TAG, "handleNewPostNotif: ");
        Intent newPostNotifIntent = new Intent(this, ViewPostActivity.class);
        newPostNotifIntent.putExtra("postId", extraInfo);
        newPostNotifIntent.putExtra("isNewPost", true);
        newPostNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, notifId /* Request code */, newPostNotifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        buildNotif(title, messageBody, null, notifId);
    }

    private void handleLikesNotif(String title, String messageBody, String extraInfo, int notifId) {
        Intent likesNotifIntent = new Intent(this, ViewPostActivity.class);
        likesNotifIntent.putExtra("postId", extraInfo);
        likesNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, notifId /* Request code */, likesNotifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        buildNotif(title, messageBody, null, notifId);
    }

    private void handleCatNotif(String title, String messageBody, String extraInfo, int notifId) {
        Intent catsNotifIntent = new Intent(this, ViewCategoryActivity.class);
        catsNotifIntent.putExtra("category", extraInfo);
        catsNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, notifId /* Request code */, catsNotifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        buildNotif(title, messageBody, null, notifId);
    }

    private void handleCommentNotif(String title, String extraInfo, int notifId) {
        Intent commentsNotifIntent = new Intent(this, CommentsActivity.class);
        commentsNotifIntent.putExtra("postId", extraInfo);
        commentsNotifIntent.addFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
        pendingIntent = PendingIntent.getActivity(this,
                notifId /* Request code */, commentsNotifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final ArrayList<Comments> commentsList = new ArrayList<>();
        //pass comment details
        // TODO: 6/1/18 find better way of receiving the latest comment
        fetchLatestComment(title, extraInfo, notifId, commentsList);
    }

    private void fetchLatestComment(final String title,
                                    String extraInfo,
                                    final int notifId,
                                    final ArrayList<Comments> commentsList) {
        coMeth.getDb()
                .collection("Posts")
                .document(extraInfo)
                .collection("Comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
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
                            getCommentDetails(latestComment, commentUserId, title, notifId);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to fetch latest comment");
                    }
                });

    }

    private void getCommentDetails(final String latestComment,
                                   String commentUserId,
                                   final String title,
                                   final int notifId) {
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

                            buildNotif(title,
                                    username + "\n" + latestComment,
                                    image,
                                    notifId);

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

    private int createNotifId() {
        Log.d(TAG, "createNotifId: ");
        Date date = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(date));
        Log.d(TAG, "createNotifId: \nid is: " + id);
        return id;
    }

    // TODO: 5/6/18 handle images on notifications
    private void buildNotif(String title,
                            String messageBody,
                            String userImageDownloadUrl,
                            int notifId) {
        Log.d(TAG, "buildNotif: ");
        defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // TODO: 6/2/18 show notifications in a group
        /*NotificationCompat.Builder groupBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setGroupSummary(true)
                        .setGroup(GROUP_KEY_FURSA)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                        .setContentIntent(pendingIntent);*/


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setGroup(GROUP_KEY_FURSA)
                .setGroupSummary(true)
                .setColor(getResources().getColor(R.color.colorPrimaryDark))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getResources(), R.mipmap.ic_launcher))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Log.d(TAG, "buildNotif: at Build.VERSION.SDK_INT >= Build.VERSION_CODES.O");
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

            // notificationId is a unique int for each notification that you must define
            notificationManagerCompat.notify(notifId, notificationBuilder.build());

            Log.d(TAG, "buildNotif: notifying");
//                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());


        } else {


            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
//            manager.notify(createNotifId(), groupBuilder.build());
            manager.notify(notifId, notificationBuilder.build());



            /*notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {

                Log.d(TAG, "buildNotif: notifying");
                notificationManager.notify(notifId, notificationBuilder.build());
            }*/

        }
    }

//    /***
//    private void buildCommentNotif(String title,
//                                   String messageBody,
//                                   String userImageDownloadUrl,
//                                   int notifId) {
//
//        Log.d(TAG, "buildCommentNotif: ");
//
//        String replyLabel = getResources().getString(R.string.reply_label);
//
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
//                    .setLabel(replyLabel)
//                    .build();
//
//
//            PendingIntent replyPendingIntent =
//                    PendingIntent.getBroadcast(getApplicationContext(),
//                            notifId,
//                            getMessageReplyIntent(notifId),
//                            PendingIntent.FLAG_UPDATE_CURRENT);
//
//            // Create the reply action and add the remote input.
//            NotificationCompat.Action action =
//                    new NotificationCompat.Action.Builder(R.drawable.ic_action_send,
//                            getString(R.string.reply_label), replyPendingIntent)
//                            .addRemoteInput(remoteInput)
//                            .build();
//
//            // Build the notification and add the action.
//            Notification newMessageNotification = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
//                    .setSmallIcon(R.drawable.appic)
//                    .setContentTitle(title)
//                    .setContentText(messageBody)
//                    .addAction(action)
//                    .build();
//
//            // Issue the notification.
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//            notificationManager.notify(notifId, newMessageNotification);
//
//        }else {
//
//            defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                    .setSmallIcon(R.drawable.ic_stat_notification)
//                    .setContentTitle(title)
//                    .setContentText(messageBody)
//                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
//                    .setGroup(GROUP_KEY_FURSA)
//                    .setAutoCancel(true)
//                    .setSound(defaultSoundUri)
//                    .setContentIntent(pendingIntent)
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                    .setLargeIcon(BitmapFactory.decodeResource(
//                            getResources(), R.mipmap.ic_launcher))
//                    .setStyle(new NotificationCompat.BigTextStyle()
//                            .bigText(messageBody));
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                Log.d(TAG, "buildNotif: at Build.VERSION.SDK_INT >= Build.VERSION_CODES.O");
//                CharSequence name = getString(R.string.channel_name);
//                String description = getString(R.string.channel_description);
//                int importance = NotificationManager.IMPORTANCE_DEFAULT;
//                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//                channel.setDescription(description);
//                // Register the channel with the system; you can't change the importance
//                // or other notification behaviors after this
//                NotificationManager notificationManager = getSystemService(NotificationManager.class);
//                notificationManager.createNotificationChannel(channel);
//
//                if (notificationManager != null) {
//
//
//                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
//
//                    // notificationId is a unique int for each notification that you must define
//                    notificationManagerCompat.notify(createNotifId() /* ID of notification */, notificationBuilder.build());
//
//                    Log.d(TAG, "buildNotif: notifying");
////                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//                }
//
//            } else {
//
//                notificationManager =
//                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//                if (notificationManager != null) {
//
//                    Log.d(TAG, "buildNotif: notifying");
//                    notificationManager.notify(createNotifId() /* ID of notification */, notificationBuilder.build());
//                }
//
//            }
//        }
//    }

//    private Intent getMessageReplyIntent(int notifId) {
//        Intent getMessageReplyIntent = new Intent(this, )
//    }

    @Override
    public void onDeletedMessages() {

    }
}
