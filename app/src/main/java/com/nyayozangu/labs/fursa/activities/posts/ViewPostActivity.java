package com.nyayozangu.labs.fursa.activities.posts;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.ViewImageActivity;
import com.nyayozangu.labs.fursa.activities.categories.ViewCategoryActivity;
import com.nyayozangu.labs.fursa.activities.comments.CommentsActivity;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.settings.AdminActivity;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.UserPageActivity;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostActivity extends AppCompatActivity {

    // TODO: 5/7/18 reorganize code on click listeners

    private static final String TAG = "Sean";
    MenuItem editPost;
    MenuItem deletePost;
    //post image
    String postImageUri;
    String postThumbUrl;
    private ImageView viewPostImage;
    private FloatingActionButton viewPostActionsFAB;
    //    private TextView descTextView;
    private ExpandableTextView descTextView;
    private TextView timeTextView;
    private TextView priceTextView;
    private TextView locationTextView;
    private TextView titleTextView;
    private TextView eventDateTextView;
    private TextView contactTextView;
    private TextView userTextView;
    private TextView catTextView;
    private CircleImageView userImage; //image of user who posted post
    private ConstraintLayout viewPostTitleLayout;
    private ConstraintLayout viewPostDescLayout;
    private ConstraintLayout viewPostLocationLayout;
    private ConstraintLayout viewPostPriceLayout;
    private ConstraintLayout viewPostTimeLayout;
    private ConstraintLayout viewPostEventDateLayout;
    private ConstraintLayout viewPostContactLayout;
    private ConstraintLayout viewPostUserLayout;
    private ConstraintLayout viewPostCatLayout;
    private String postUserId;
    private String currentUserId;
    private ConstraintLayout actionsLayout;
    private ImageView likeButton;
    private TextView likesCountText;
    private ImageView commentsButton;
    private TextView commentsCountText;
    private ImageView saveButton;
    private ImageView shareButton;
    private String contactDetails;
    private android.support.v7.widget.Toolbar toolbar;
    //progress
    private ProgressDialog progressDialog;
    //save categories to list
    private ArrayList<String> catArray;
    private ArrayList<String> catKeys;
    //post content
    private String desc;
    private String postId;
    private String postTitle;
    //common methods
    private CoMeth coMeth = new CoMeth();
    private ArrayList<String> reportedItems;
    private ArrayList flagsArray;
    private String reportDetailsString;

    private boolean isAdmin = false;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem editPost = menu.findItem(R.id.editMenuItem);
        MenuItem deletePost = menu.findItem(R.id.deleteMenuItem);

        if (coMeth.isConnected()) {
            if (coMeth.isLoggedIn()) {
                currentUserId = new CoMeth().getUid();

                //check if current user is the post user
                //check if user is admin
                if (currentUserId.equals(postUserId)) {
                    editPost.setVisible(true);
                    deletePost.setVisible(true);
                } else if (hasAdminAccess()) {
                    editPost.setVisible(true);
                    deletePost.setVisible(true);
                } else {
                    editPost.setVisible(false);
                    deletePost.setVisible(false);
                }
            }

        } else {

            editPost.setVisible(false);
            deletePost.setVisible(false);

        }

        return true;
    }

    /**
     * Check if current user is an admin
     *
     * @return boolean
     * true if current use if admin
     * false if current user is not admin
     */
    private boolean isAdmin() {

        //get current user
        FirebaseUser user = coMeth.getAuth().getCurrentUser();
        if (user.getEmail() != null) {

            String userEmail = user.getEmail();
            //check if user email is in admin
            coMeth.getDb()
                    .collection("Admins")
                    .document(userEmail)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //check if user exists
                            //user is admin
                            isAdmin = documentSnapshot.exists();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: failed to get admins");
                        }
                    });

        }
        return isAdmin;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_post_toolbar_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.reportMenuItem:

                if (coMeth.isLoggedIn()) {
                    showReportDialog();
                } else {
                    goToLogin(getString(R.string.login_to_report));
                }
                break;

            case R.id.editMenuItem:
                //open edit post
                goToEdit();
                break;

            case R.id.deleteMenuItem:
                //handle delete post
                deletePost(postId);
                break;

            default:
                break;
        }

        return true;
    }

    private void showReportDialog() {

        final AlertDialog.Builder reportBuilder = new AlertDialog.Builder(ViewPostActivity.this);
        reportBuilder.setTitle(getString(R.string.report_text))
                .setIcon(R.drawable.ic_action_red_flag)
                .setMultiChoiceItems(coMeth.reportList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        //what happens when an item is checked
                        if (isChecked) {

                            // If the user checked the item, add it to the selected items
                            reportedItems.add(coMeth.reportListKey[which]);

                        } else if (reportedItems.contains(coMeth.reportListKey[which])) {

                            // Else, if the item is already in the array, remove it
                            reportedItems.remove(coMeth.reportListKey[which]);

                        }

                    }
                })
                .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .setPositiveButton(getString(R.string.done_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        if (coMeth.isConnected()) {

                            showProgress(getString(R.string.submitting));

                            //create report details string
                            for (String item : reportedItems) {
                                reportDetailsString = reportDetailsString.concat(item + "\n");
                            }
                            //add post details to db
                            final Map<String, Object> reportMap = new HashMap<>();
                            reportMap.put("postId", postId);
                            reportMap.put("timestamp", FieldValue.serverTimestamp());

                            //get existing flags
                            coMeth.getDb()
                                    .collection("Flags/posts/Posts")
                                    .document(postId)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if (task.isSuccessful() && task.getResult().exists()) {

                                                //get existing flags
                                                flagsArray = (ArrayList) task.getResult().get("flags");
                                                //update the flags
                                                flagsArray.add(coMeth.getUid() + "\n" + reportDetailsString);
                                                reportMap.put("flags", flagsArray);
                                                submitReport(reportMap);

                                            } else {

                                                if (!task.isSuccessful()) {
                                                    // TODO: 5/7/18 handle task failed
                                                }
                                                if (!task.getResult().exists()) {
                                                    //post has not been reported
                                                    // TODO: 5/7/18 handle task not reported
                                                    flagsArray.add(coMeth.getUid() + "\n" + reportDetailsString);
                                                    reportMap.put("flags", flagsArray);
                                                    submitReport(reportMap);
                                                }

                                            }

                                        }
                                    });

                        } else {

                            //alert user is not connected
                            dialog.dismiss();
                            showSnack(getString(R.string.failed_to_connect_text));

                        }

                    }
                })
                .setCancelable(false)
                .show();

    }

    private void submitReport(Map<String, Object> reportMap) {

        coMeth.getDb()
                .collection("Flags/posts/Posts")
                .document(postId)
                .set(reportMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            coMeth.stopLoading(progressDialog);
                            //alert user
                            showConfirmReport();

                        } else {

                            coMeth.stopLoading(progressDialog);
                            showSnack(getString(R.string.report_submit_failed_text));
                            Log.d(TAG, "onComplete: " + task.getException());

                        }

                    }

                });

    }

    private void showConfirmReport() {
        AlertDialog.Builder reportSuccessBuilder = new AlertDialog.Builder(ViewPostActivity.this);
        reportSuccessBuilder.setTitle(getString(R.string.report_text))
                .setIcon(R.drawable.ic_action_red_flag)
                .setMessage(R.string.report_submitted_text)
                .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                }).show();
    }

    private void deletePost(final String postId) {

        String confirmDelMessage = getString(R.string.confirm_del_text);
        //add alert Dialog
        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(ViewPostActivity.this);
        deleteBuilder.setTitle(R.string.del_post_text)
                .setMessage(confirmDelMessage)
                .setIcon(getDrawable(R.drawable.ic_action_red_alert))
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        showProgress("Deleting...");
                        coMeth.getDb().collection("Posts").document(postId).delete();
                        coMeth.stopLoading(progressDialog, null);
                        Intent delResultIntent = new Intent(ViewPostActivity.this, MainActivity.class);
                        delResultIntent.putExtra("action", "notify");
                        delResultIntent.putExtra("message", getString(R.string.del_success_text));
                        if (hasAdminAccess()) {
                            //go back to admin page
                            startActivity(new Intent(ViewPostActivity.this, AdminActivity.class));
                            finish();

                        } else {
                            startActivity(delResultIntent);
                            finish();
                        }

                    }
                })
                .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .show();


    }

    private boolean hasAdminAccess() {
        return getIntent() != null &&
                getIntent().getStringExtra("permission") != null &&
                getIntent().getStringExtra("permission").equals("admin") &&
                isAdmin();
    }

    private void goToEdit() {

        Intent editIntent = new Intent(ViewPostActivity.this, CreatePostActivity.class);
        editIntent.putExtra("editPost", postId);
        startActivity(editIntent);
        finish();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        toolbar = findViewById(R.id.viewPostToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //initialize items
        actionsLayout = findViewById(R.id.viewPostActionsLayout);
        likeButton = findViewById(R.id.viewPostLikeImageView);
        likesCountText = findViewById(R.id.viewPostLikesCountsTextView);
        commentsButton = findViewById(R.id.viewPostCommentImageView);
        commentsCountText = findViewById(R.id.viewPostCommentTextView);
        saveButton = findViewById(R.id.viewPostSaveImageView);
        shareButton = findViewById(R.id.viewPostShareImageView);

        titleTextView = findViewById(R.id.viewPostTitleTextView);
        descTextView = findViewById(R.id.viewPostDescTextView);
        eventDateTextView = findViewById(R.id.viewPostEventDateTextView);
        timeTextView = findViewById(R.id.viewPostTimeTextView);
        priceTextView = findViewById(R.id.viewPostPriceTextView);
        locationTextView = findViewById(R.id.viewPostLocationTextView);
        viewPostImage = findViewById(R.id.viewPostImageView);
        contactTextView = findViewById(R.id.viewPostContactTextView);
        userImage = findViewById(R.id.viewPostUserImageView);

        viewPostTitleLayout = findViewById(R.id.viewPostTitleLayout);
        viewPostDescLayout = findViewById(R.id.viewPostDescLayout);
        viewPostLocationLayout = findViewById(R.id.viewPostLocationLayout);
        viewPostPriceLayout = findViewById(R.id.viewPostPriceLayout);
        viewPostTimeLayout = findViewById(R.id.viewPostTimeLayout);
        viewPostEventDateLayout = findViewById(R.id.viewPostEventDateLayout);
        viewPostContactLayout = findViewById(R.id.viewPostContactLayout);

        userTextView = findViewById(R.id.viewPostUserTextView);
        viewPostUserLayout = findViewById(R.id.viewPostUserLayout);

        viewPostCatLayout = findViewById(R.id.viewPostCatLayout);
        catTextView = findViewById(R.id.viewPostCatTextView);
        catArray = new ArrayList<>();
        catKeys = new ArrayList<>();
        flagsArray = new ArrayList();
        reportedItems = new ArrayList<String>();
        reportDetailsString = "";

        //handle intent
        handleIntent();

        //get post title on create
        postTitle = getPostTitle(postId);

        //handle action clicks
        //handle comments click
        commentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open comments page
                Intent commentsIntent = new Intent(ViewPostActivity.this, CommentsActivity.class);
                commentsIntent.putExtra("postId", postId);
                startActivity(commentsIntent);

            }
        });

        //handle share click
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "Sharing post");
                //get post url
                showProgress(getString(R.string.loading_text));
                String postUrl = getResources().getString(R.string.fursa_url_post_head) + postId;
                shareDynamicLink(postUrl);
            }
        });

        //handle save click
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //disable button
                saveButton.setClickable(false);

                //check if user is connected to the internet
                if (coMeth.isConnected()) {

                    //check if user is logged in
                    if (coMeth.isLoggedIn()) {

                        final String currentUserId = coMeth.getUid();

                        coMeth.getDb()
                                .collection("Posts/" + postId + "/Saves")
                                .document(currentUserId).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (!task.getResult().exists()) {

                                            Map<String, Object> savesMap = new HashMap<>();
                                            savesMap.put("timestamp", FieldValue.serverTimestamp());
                                            //save new post
                                            coMeth.getDb().collection("Posts/" + postId + "/Saves").document(currentUserId).set(savesMap);
                                            //notify user that post has been saved
                                            showSaveSnack(getString(R.string.added_to_saved_text));

                                        } else {

                                            //delete saved post
                                            coMeth.getDb().collection("Posts/" + postId + "/Saves")
                                                    .document(currentUserId)
                                                    .delete();
                                        }
                                    }
                                });
                    } else {
                        //user is not logged in
                        Log.d(TAG, "user is not logged in");
                        //notify user

                        String message = getString(R.string.login_to_save_text);
                        goToLogin(message);
                    }
                } else {

                    //user is not connected to the internet
                    showSnack(getString(R.string.failed_to_connect_text));

                }

                //enable button
                saveButton.setClickable(true);

            }
        });

        //handle like click
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //disable button
                likeButton.setClickable(false);

                if (coMeth.isConnected()) {

                    if (coMeth.isLoggedIn()) {

                        final String currentUserId = coMeth.getUid();

                        coMeth.getDb()
                                .collection("Posts/" + postId + "/Likes")
                                .document(currentUserId)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        //get data from teh likes collection
                                        // TODO: 5/6/18 check if the internet actually works
                                        //check if current user has already liked post
                                        if (!task.getResult().exists()) {

                                            Map<String, Object> likesMap = new HashMap<>();
                                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                                            //can alternatively ne written
                                            coMeth.getDb().collection("Posts/" + postId + "/Likes").document(currentUserId).set(likesMap);

                                        } else {
                                            //delete the like
                                            coMeth.getDb().collection("Posts/" + postId + "/Likes").document(currentUserId).delete();
                                        }
                                    }
                                });

                    } else {

                        //user is not logged in
                        Log.d(TAG, "use is not logged in");
                        //notify user
                        String message = getString(R.string.login_to_like);
                        goToLogin(message);

                    }
                } else {

                    //alert user is not connected
                    showSnack(getString(R.string.failed_to_connect_text));

                }

                //enable button
                likeButton.setClickable(true);

            }
        });


        //set likes
        coMeth.getDb()
                .collection("Posts/" + postId + "/Likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        //check if exits
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //post has likes
                            int likes = queryDocumentSnapshots.getDocuments().size();
                            Log.d(TAG, "post has" + likes + " likes");
                            //set likes to likesTextView
                            likesCountText.setText(Integer.toString(likes));
                        }
                    }
                });

        //set comments
        coMeth.getDb()
                .collection("Posts/" + postId + "/Comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                        Log.d(TAG, "at onEvent, when likes change");
                        if (!queryDocumentSnapshots.isEmpty()) {

                            //post has likes
                            int numberOfComments = queryDocumentSnapshots.size();
                            commentsCountText.setText(Integer.toString(numberOfComments));
                        }
                    }
                });


        //set like button
        if (coMeth.isLoggedIn()) {
            //get likes
            //determine likes by current user

            String currentUserId = coMeth.getUid();

            coMeth.getDb()
                    .collection("Posts/" + postId + "/Likes")
                    .document(currentUserId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            //update the like button real time
                            if (documentSnapshot.exists()) {

                                Log.d(TAG, "at get likes, updating likes real time");
                                //user has liked
                                likeButton.setImageDrawable(getDrawable(R.drawable.ic_action_liked));

                            } else {

                                //current user has not liked the post
                                likeButton.setImageDrawable(getDrawable(R.drawable.ic_action_like_unclicked));
                            }
                        }
                    });

            //set saves
            coMeth.getDb()
                    .collection("Posts/" + postId + "/Saves")
                    .document(currentUserId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            //update the save button real time
                            if (documentSnapshot.exists()) {

                                Log.d(TAG, "at get saves, updating saves realtime");
                                //user has saved post
                                saveButton.setImageDrawable(getDrawable(R.drawable.ic_action_bookmarked));

                            } else {

                                //user has not liked post
                                saveButton.setImageDrawable(getDrawable(R.drawable.ic_action_bookmark_outline));

                            }
                        }
                    });

        }

        //set contents
        coMeth.getDb()
                .collection("Posts")
                .document(postId).addSnapshotListener(ViewPostActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                Log.d(TAG, "at view post query");
                //show progress
                showProgress("Loading...");

                //check if post exists
                if (documentSnapshot.exists()) {

                    Log.d(TAG, "Post  exist");
                    final Posts post = documentSnapshot.toObject(Posts.class).withId(postId);
                    //set items
                    //set title
                    String title = post.getTitle();
                    titleTextView.setText(title);
                    getSupportActionBar().setTitle(title);

                    //set the description
                    desc = post.getDesc();
                    descTextView.setText(desc);
                    Log.d(TAG, "onEvent: desc set");

                    //set the contact info
                    ArrayList<String> contactArray = post.getContact_details();
                    if (contactArray != null) {

                        Log.d(TAG, "onEvent: has contact details");
                        String contactString = "";
                        for (int i = 0; i < contactArray.size(); i++) {

                            //set the first item
                            contactString = contactString.concat(contactArray.get(i).toString() + "\n");

                        }
                        //set contactString
                        contactTextView.setText(contactString.trim());
                        Log.d(TAG, "onEvent: contact details set");

                    } else {

                        //hide contact details field
                        viewPostContactLayout.setVisibility(View.GONE);
                        Log.d(TAG, "onEvent: has no contact details");

                    }


                    //set location
                    ArrayList<String> locationArray = post.getLocation();
                    String locationString = "";

                    if (locationArray != null) {

                        Log.d(TAG, "onEvent: has location");
                        Log.d(TAG, "onEvent: location is " + locationArray);

                        for (int i = 0; i < locationArray.size(); i++) {

                            locationString = locationString.concat(locationArray.get(i).toString() + "\n");

                        }

                        locationTextView.setText(locationString.trim());

                    } else {

                        viewPostLocationLayout.setVisibility(View.GONE);
                    }

                    //set price
                    String price = post.getPrice();
                    if (price != null) {

                        priceTextView.setText(price);

                    } else {

                        viewPostPriceLayout.setVisibility(View.GONE);

                    }

                    //set event date
                    if (post.getEvent_date() != null) {
                        long eventDate = post.getEvent_date().getTime();
                        Log.d(TAG, String.valueOf(eventDate));
                        String eventDateString = DateFormat.format("EEE, MMM d, 20yy", new Date(eventDate)).toString();
                        Log.d(TAG, "onEvent: \nebentDateString: " + eventDateString);
                        eventDateTextView.setText(eventDateString);
                    }else{
                        viewPostEventDateLayout.setVisibility(View.GONE);
                    }

                    //set the time
                    if (post.getTimestamp() != null) {
                        long millis = post.getTimestamp().getTime();
                        String dateString = coMeth.processPostDate(millis);
                        String date = getString(R.string.posted_text) + ":\n" + dateString;
                        timeTextView.setText(date);
                    }

                    //set post image
                    //add the placeholder image
                    postImageUri = post.getImage_url();
                    postThumbUrl = post.getThumb_url();

                    if (postImageUri != null && postThumbUrl != null) {

                        try {
                            coMeth.setImage(R.drawable.appiconshadow,
                                    postImageUri,
                                    postThumbUrl,
                                    viewPostImage);
                        } catch (Exception glideException) {
                            Log.d(TAG, "onEvent: glide exception " +
                                    glideException.getMessage());
                            //set placeholder image
                            viewPostImage.setImageDrawable(getDrawable(R.drawable.appiconshadow));
                        }
                        Log.d(TAG, "onEvent: image set");
                    } else {
                        //post has no image, hide the image view
                        viewPostImage.setVisibility(View.GONE);
                    }

                    //get user id for the post
                    postUserId = post.getUser_id();
                    //set the post user layout click
                    viewPostUserLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent goToUserPageIntent = new Intent(
                                    ViewPostActivity.this, UserPageActivity.class);
                            goToUserPageIntent.putExtra("userId", postUserId);
                            startActivity(goToUserPageIntent);

                        }
                    });
                    //check db for user
                    coMeth.getDb()
                            .collection("Users")
                            .document(postUserId)
                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                    //check if user exists
                                    if (documentSnapshot.exists()) {

                                        //make user object
                                        Users user = documentSnapshot.toObject(Users.class);

                                        //user exists
                                        if (user.getThumb() != null) {
                                            //user has thumb
                                            String userThumbDwnUrl = user.getThumb();
                                            coMeth.setImage(R.drawable.ic_action_person_placeholder,
                                                    userThumbDwnUrl,
                                                    userImage);
                                            Log.d(TAG, "onEvent: user thumb set");

                                        } else if (user.getImage() != null) {
                                            //use has no thumb but has image
                                            String userImageDwnUrl = user.getImage();
                                            coMeth.setImage(R.drawable.ic_action_person_placeholder,
                                                    userImageDwnUrl,
                                                    userImage);
                                            Log.d(TAG, "onEvent: user thumb set");

                                        } else {
                                            //user has no image or thumb
                                            userImage.setImageDrawable(
                                                    getDrawable(R.drawable.ic_action_person_placeholder));
                                            Log.d(TAG, "onEvent: placeholder user image set");
                                        }
                                        //set name
                                        //get user name
                                        if (user.getName() != null) {
                                            String username = user.getName();
                                            String userNameMessage = getString(R.string.posted_by_text) + "\n" + username;
                                            userTextView.setText(userNameMessage);

                                        } else {
                                            //use name is null, hide the user layout
                                            viewPostUserLayout.setVisibility(View.GONE);

                                        }
                                    } else {
                                        //user does not exist
                                        userImage.setImageDrawable(
                                                getDrawable(R.drawable.ic_action_person_placeholder));

                                    }


                                }
                            });


                    //get categories
                    if (post.getCategories() != null) {

                        Log.d(TAG, "onEvent: post has cats");
                        String catString = "";

                        //post has categories
                        catKeys = post.getCategories();

                        ArrayList<String> categories = new ArrayList<>();
                        for (int i = 0; i < catKeys.size(); i++) {

                            //go through catKeys and get values
                            String catValue = coMeth.getCatValue(catKeys.get(i).toString());
                            categories.add(catValue);

                        }

                        Log.d(TAG, "onEvent: categories are " + categories);
                        for (int i = 0; i < categories.size(); i++) {

                            //is last cat
                            catString = catString.concat(String.valueOf(categories.get(i)) + "\n");
                            //update the catArrayList
                            catArray.add(String.valueOf(categories.get(i)));

                        }

                        catTextView.setText(catString.trim());

                    } else {

                        //post has not categories
                        // hide categories layout
                        viewPostCatLayout.setVisibility(View.GONE);
                        Log.d(TAG, "onEvent: post has no cats");

                    }

                    coMeth.stopLoading(progressDialog);


                } else {
                    //post does not exist
                    coMeth.stopLoading(progressDialog);
                    Log.d(TAG, "Error: post does not exist");
                    //save error and notify in main
                    Intent postNotFountIntent = new Intent(ViewPostActivity.this, MainActivity.class);
                    postNotFountIntent.putExtra("action", "notify");
                    postNotFountIntent.putExtra("message", "Could not find post");
                    startActivity(postNotFountIntent);
                    finish();
                }

                coMeth.stopLoading(progressDialog);

            }
        });

        //handle clicks
        //post image click
        viewPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open image in full screen
                Intent openImageIntent = new Intent(ViewPostActivity.this, ViewImageActivity.class);
                openImageIntent.putExtra("imageUrl", postImageUri);
                startActivity(openImageIntent);

            }
        });

        //category layout click
        viewPostCatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open alert dialog
                AlertDialog.Builder catDialogBuilder = new AlertDialog.Builder(ViewPostActivity.this);
                catDialogBuilder.setTitle("Categories")
                        .setIcon(getDrawable(R.drawable.ic_action_categories))
                        .setItems(catArray.toArray(new String[0]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //set click actions
                                //open view cat activity
                                Intent catIntent = new Intent(ViewPostActivity.this, ViewCategoryActivity.class);
                                catIntent.putExtra("category", catKeys.get(which));
                                startActivity(catIntent);
                                finish();

                                Log.d(TAG, "onClick: \nuser selected cat is: " + catKeys.get(which));
                            }
                        })
                        .show();

            }
        });


        //location layout click
        viewPostLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //launch google maps and serch for location
                String location = locationTextView.getText().toString();
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });

    }

    private void shareDynamicLink(String postUrl) {
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(postUrl))
                .setDynamicLinkDomain(getString(R.string.dynamic_link_domain))
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(9)
                        .setFallbackUrl(Uri.parse(getString(R.string.playstore_url)))
                        .build())
                // TODO: 5/18/18 hanlde opeinig links on ios
                /*.setIosParameters(new DynamicLink.IosParameters.Builder(FirebaseApp.getInstance().getApplicationContext().getPackageName())
                        .setCustomScheme(getString(R.string.playstore_url))
                        .setFallbackUrl(Uri.parse(getString(R.string.playstore_url)))
                        .build())*/
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(getString(R.string.app_name))
                                .setDescription(getString(R.string.sharing_opp_text))
                                .setImageUrl(Uri.parse(getString(R.string.app_icon_url)))
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Log.d(TAG, "onComplete: short link is: " + shortLink);

                            //show share dialog
                            String postTitle = getPostTitle(postId);
                            String fullShareMsg = getString(R.string.app_name) + ":\n" +
                                    postTitle + "\n" +
                                    shortLink;
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                            shareIntent.putExtra(Intent.EXTRA_TEXT, fullShareMsg);
                            coMeth.stopLoading(progressDialog);
                            startActivity(Intent.createChooser(shareIntent, "Share with"));
                        } else {
                            Log.d(TAG, "onComplete: \ncreating short link task failed\n" +
                                    task.getException());
                            coMeth.stopLoading(progressDialog);
                            showSnack(getString(R.string.failed_to_share_text));
                        }
                    }
                });
    }

    private void handleIntent() {
        if (getIntent() != null) {
            if (getIntent().hasExtra("postId")) {
                //get the sent intent
                Intent getPostIdIntent = getIntent();
                postId = getPostIdIntent.getStringExtra("postId");
                Log.d(TAG, "postId is: " + postId);
            } else {
                postId = handleDeepLinks(getIntent());
            }
        } else {
            if (hasAdminAccess()) {
                startActivity(new Intent(ViewPostActivity.this, AdminActivity.class));
                finish();
            } else {
                goToMain();
            }
        }
    }

    //retrieve the post title
    private String getPostTitle(String postId) {

        //get data from db
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        //check if exists
                        if (documentSnapshot.exists()) {

                            //post exists
                            Posts post = documentSnapshot.toObject(Posts.class);
                            postTitle = post.getTitle();
                            Log.d(TAG, "onSuccess: post title is " + postTitle);

                        } else {

                            //post does not exist

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get post");
                    }
                });
        Log.d(TAG, "getPostTitle: post title is " + postTitle);
        return postTitle;

    }

    private void goToMain() {
        startActivity(new Intent(ViewPostActivity.this, MainActivity.class));
        finish();
    }

    private String handleDeepLinks(Intent intent) {
        // ATTENTION: This was auto-generated to handle app links.
        Log.i(TAG, "at handleDeepLinkIntent");

        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();

        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            String postUrl = String.valueOf(appLinkData);

            int endOfUrlHead = getResources().getString(R.string.fursa_url_post_head).length();
            postId = postUrl.substring(endOfUrlHead);

            Log.i(TAG, "incomingUrl is " + postId);

        }

        return postId;

    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(ViewPostActivity.this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.view_post_activity_layout),
                message, Snackbar.LENGTH_LONG).show();
    }

    private void showSaveSnack(String message) {
        Snackbar.make(findViewById(R.id.view_post_activity_layout),
                message, Snackbar.LENGTH_LONG)
                .setAction(R.string.see_list_text, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent goToSavedIntent = new Intent(ViewPostActivity.this, MainActivity.class);
                        goToSavedIntent.putExtra("action", "goto");
                        goToSavedIntent.putExtra("destination", "saved");
                        startActivity(goToSavedIntent);
                        finish();

                    }
                })
                .show();
    }

    private void goToLogin(String message) {

        Intent loginIntent = new Intent(ViewPostActivity.this, LoginActivity.class);
        loginIntent.putExtra("message", message);
        startActivity(loginIntent);

    }

}
