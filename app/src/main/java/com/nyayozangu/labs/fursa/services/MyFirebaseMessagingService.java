package com.nyayozangu.labs.fursa.services;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.CommentsActivity;
import com.nyayozangu.labs.fursa.activities.MainActivity;
import com.nyayozangu.labs.fursa.activities.UserPageActivity;
import com.nyayozangu.labs.fursa.activities.ViewCategoryActivity;
import com.nyayozangu.labs.fursa.activities.ViewPostActivity;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Comments;
import com.nyayozangu.labs.fursa.models.Posts;
import com.nyayozangu.labs.fursa.models.Users;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.CATEGORIES_UPDATES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.COMMENTS_COLL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.COMMENT_UPDATES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.ERROR;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.EXTRA;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWER_POST;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.LIKES_UPDATES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NEW_FOLLOWERS_UPDATE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NEW_POST_UPDATES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NOTIFICATIONS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POST_ID;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID;

/**
 *
 *
 * Created by Sean on 4/24/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "Sean";
    private static final String CHANNEL_ID = "UPDATES";
    private static final String KEY_TEXT_REPLY = "key_text_reply";
    private static final String TITLE = "title";
    private static final String MESSAGE = "message";
    private static final String NOTIFICATION_TYPE = "notif_type";
    private static final String IS_NEW_POST = "isNewPost";
    private static final String NOTIFICATION_ID = "notif_id";

    private CoMeth coMeth = new CoMeth();
    private PendingIntent pendingIntent;

    private String notifType;
    private String extraInfo;
    private String errorMessage;
    private String userId;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO: 5/23/18 do code review
        Log.d(TAG, "onMessageReceived: ");
        Log.d(TAG, remoteMessage.toString());

        int notifId = createNotifId();

        String title;
        String message;
        if (remoteMessage.getData().size() > 0) {

            Log.d(TAG, "onMessageReceived: message has data");

            Map<String, String> data = remoteMessage.getData();
            title = data.get(TITLE);
            message = data.get(MESSAGE);
            if (data.get(NOTIFICATION_TYPE) != null) {
                notifType = data.get(NOTIFICATION_TYPE);
                userId = data.get(USER_ID);
            }
            if (data.get(EXTRA) != null) {
                extraInfo = data.get(EXTRA).trim();
            }
            if (data.get(ERROR) != null) {
                errorMessage = data.get(ERROR).trim();
            }
            // TODO: 6/15/18 code review
            if (!extraInfo.isEmpty()) {
                if (notifType.equals(NEW_POST_UPDATES) || notifType.equals(NEW_FOLLOWERS_UPDATE) ||
                        coMeth.isLoggedIn() && !userId.equals(coMeth.getUid())) {
                    sendNotification(title, message, notifType, extraInfo, notifId);
                }
            } else {
                if ((notifType.equals(NEW_POST_UPDATES) && errorMessage != null) ||
                        (userId != null && !userId.equals(coMeth.getUid()))) {
                    sendNotification(title, message, null, null, notifId);
                }
            }
        } else if (remoteMessage.getNotification() != null &&
                remoteMessage.getData().size() == 0 &&
                remoteMessage.getNotification().getTitle() != null &&
                remoteMessage.getNotification().getBody() != null) {
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();
            sendNotification(title, message, null, null, notifId);
        } else {
            //other weird cases
            title = getResources().getString(R.string.app_name);
            message = getResources().getString(R.string.sharing_opp_text);
            sendNotification(title, message, null, null, notifId);
        }
    }


    /**
     * sends the notification
     *
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

                case COMMENT_UPDATES:
                    handleCommentNotif(title, extraInfo, notifId);
                    break;

                case CATEGORIES_UPDATES:
                    //handle cat notifications
                    handleCatNotif(title, messageBody, extraInfo, notifId);
                    break;

                case LIKES_UPDATES:
                    //handle likes notifications
                    handleLikesNotif(title, messageBody, extraInfo, notifId);
                    break;

                case FOLLOWER_POST:
                    //handle notification for new post
                    handleFollowerPostNotif(title, extraInfo, notifId);
                    break;

                case NEW_POST_UPDATES:
                    //handle notification for new post
                    handleNewPostNotif(title, messageBody, extraInfo, notifId);
                    break;
                case NEW_FOLLOWERS_UPDATE:
                    //handle notification for new post
                    handleNewFollowerNotif(title, messageBody, extraInfo, notifId);
                    break;

                default:

                    Intent noExtraNotifIntent = new Intent(this, MainActivity.class);
                    noExtraNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(
                            this, notifId /* Request code */, noExtraNotifIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    buildNotif(title, messageBody, null, notifId);
                    addNotifToDb(title, messageBody, notifType, extraInfo, notifId);

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
            addNotifToDb(title, messageBody, notifType, extraInfo, notifId);
        }
    }

    private void handleFollowerPostNotif(final String title, final String extraInfo, final int notifId) {
        //extra info is new post id
        Intent intent = new Intent(this, ViewPostActivity.class);
        intent.putExtra(POST_ID, extraInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, notifId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        getUserInfo(title, extraInfo, notifId, extraInfo);
    }

    private void getUserInfo(final String title, final String extraInfo, final int notifId, String newPostId) {
        DocumentReference newPostRef = coMeth.getDb().collection(POSTS).document(newPostId);
        newPostRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Posts post = documentSnapshot.toObject(Posts.class);
                    String newPostUserId = Objects.requireNonNull(post).getUser_id();
                    DocumentReference postUserRef = coMeth.getDb().collection(USERS).document(newPostUserId);
                    postUserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                Users user = documentSnapshot.toObject(Users.class);
                                assert user != null;
                                String username = user.getName();
                                String messageBody = username + " " + getString(R.string.has_new_post_text);
                                String userImageUrl = user.getImage();
                                buildNotif(title, messageBody, userImageUrl, notifId);
                                addNotifToDb(title, messageBody, notifType, extraInfo, notifId);
                            }
                        }
                    });
                }else{
                    Log.d(TAG, "onSuccess: user does not exist");
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: error fetching user details\n" + e.getMessage());
                    }
                });
    }

    private void addNotifToDb(String title, String messageBody,
                              String notifType, String extraInfo, int notifId) {
        Log.d(TAG, "addNotifToDb: ");
        if (coMeth.isLoggedIn()) {
            String currentUserId = coMeth.getUid();
            Map<String, Object> notifMap = new HashMap<>();
            notifMap.put(TITLE, title);
            notifMap.put(MESSAGE, messageBody);
            notifMap.put(NOTIFICATION_TYPE, notifType);
            notifMap.put(EXTRA, extraInfo);
            notifMap.put(NOTIFICATION_ID, notifId);
            notifMap.put(CoMeth.TIMESTAMP, FieldValue.serverTimestamp());

            coMeth.getDb().collection(CoMeth.USERS).document(currentUserId)
                    .collection(NOTIFICATIONS).add(notifMap)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "onSuccess: notification added");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: failed to add notification" +
                                    e.getMessage());
                        }
                    });
        }
    }

    private void handleNewPostNotif(String title, String messageBody, String extraInfo, int notifId) {

        Intent newPostNotifIntent = new Intent(this, ViewPostActivity.class);
        newPostNotifIntent.putExtra(POST_ID, extraInfo);
        newPostNotifIntent.putExtra(IS_NEW_POST, true);
        newPostNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, notifId, newPostNotifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        buildNotif(title, messageBody, null, notifId);
        addNotifToDb(title, messageBody, notifType, extraInfo, notifId);
    }

    private void handleNewFollowerNotif(final String title, final String messageBody, final String extraInfo, final int notifId) {

        Intent newFollowerNotifIntent = new Intent(this, UserPageActivity.class);
        newFollowerNotifIntent.putExtra(USER_ID, extraInfo);
        newFollowerNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, notifId, newFollowerNotifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //get follower data
        DocumentReference followerRef = coMeth.getDb().collection(USERS).document(extraInfo);
        followerRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    Users follower = documentSnapshot.toObject(Users.class);
                    assert follower != null;
                    String name = follower.getName();
                    String followerImageUrl = follower.getImage();
                    String newFollowerMessage = name + " " + getResources().getString(R.string.is_now_following_text);
                    buildNotif(title, newFollowerMessage, followerImageUrl, notifId);
                    addNotifToDb(title, newFollowerMessage, notifType, extraInfo, notifId);
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get follower data\n " + e.getMessage());
                        buildNotif(title, messageBody, null, notifId);
                        addNotifToDb(title, messageBody, notifType, extraInfo, notifId);
                    }
                });
    }

    private void handleLikesNotif(String title, String messageBody, String extraInfo, int notifId) {
        Intent likesNotifIntent = new Intent(this, ViewPostActivity.class);
        likesNotifIntent.putExtra(POST_ID, extraInfo);
        likesNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, notifId, likesNotifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        buildNotif(title, messageBody, null, notifId);
        addNotifToDb(title, messageBody, notifType, extraInfo, notifId);
    }

    private void handleCatNotif(String title, String messageBody, String extraInfo, int notifId) {
        Intent catsNotifIntent = new Intent(this, ViewCategoryActivity.class);
        catsNotifIntent.putExtra("category", extraInfo);
        catsNotifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, notifId, catsNotifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        buildNotif(title, messageBody, null, notifId);
        addNotifToDb(title, messageBody, notifType, extraInfo, notifId);
    }

    private void handleCommentNotif(String title, String extraInfo, int notifId) {
        Intent commentsNotifIntent = new Intent(this, CommentsActivity.class);
        commentsNotifIntent.putExtra(POST_ID, extraInfo);
        commentsNotifIntent.addFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
        pendingIntent = PendingIntent.getActivity(this,
                notifId, commentsNotifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final ArrayList<Comments> commentsList = new ArrayList<>();
        //pass comment details
        fetchLatestComment(title, extraInfo, notifId, commentsList);
    }

    private void fetchLatestComment(final String title, String extraInfo, final int notifId,
                                    final ArrayList<Comments> commentsList) {
        CollectionReference commentsRef = coMeth.getDb().collection(POSTS +
                "/" + extraInfo +"/" + COMMENTS_COLL);
        commentsRef.orderBy(CoMeth.TIMESTAMP, Query.Direction.DESCENDING).get()
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

    private void getCommentDetails(final String latestComment, String commentUserId,
                                   final String title, final int notifId) {
        DocumentReference userRef =
        coMeth.getDb().collection(CoMeth.USERS).document(commentUserId);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Users user = documentSnapshot.toObject(Users.class);
                    String username = Objects.requireNonNull(user).getName();
                    String image = user.getImage();
                    buildNotif(title, username + "\n" + latestComment, image, notifId);
                    addNotifToDb(title, username + "\n" + latestComment, notifType, extraInfo, notifId);
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: error getting comments for notifications\n" +
                        e.getMessage());
                    }
                });
    }

    private int createNotifId() {
        Log.d(TAG, "createNotifId: ");
        Date date = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(date));
        return id;
    }

    // TODO: 5/6/18 handle images on notifications
    private void buildNotif(String title,
                            String messageBody,
                            String userImageDownloadUrl,
                            int notifId) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // TODO: 6/2/18 show notifications in a group

        String GROUP_KEY_FURSA = "com.nyayozangu.labs.fursa.COMMENTS";
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
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(notifId, notificationBuilder.build());
            Log.d(TAG, "buildNotif: notifying");
        } else {
            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            manager.notify(notifId, notificationBuilder.build());

        }
    }



    @Override
    public void onDeletedMessages() {

    }
}
