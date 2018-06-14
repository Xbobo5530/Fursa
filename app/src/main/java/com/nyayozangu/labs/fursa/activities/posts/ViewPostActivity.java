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
import com.nyayozangu.labs.fursa.activities.search.SearchableActivity;
import com.nyayozangu.labs.fursa.activities.settings.AdminActivity;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.notifications.Notify;
import com.nyayozangu.labs.fursa.users.UserPageActivity;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: 5/7/18 reorganize code on click listeners
    // TODO: 5/30/18 handle posts with multiple images
    // TODO: 6/7/18 when deleting a post with an image, delete the image as well
    // TODO: 6/7/18 run an algorythim to delete all post photos that have no posts
    // TODO: 6/14/18 check view post reset issue
    // TODO: 6/14/18 for view reward  notifications use viewsRewardPostId as topic


    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();

    private MenuItem editPost, deletePost;
    private FloatingActionButton viewPostActionsFAB;
    //    private TextView descTextView;
    private ExpandableTextView descTextView;
    private TextView timeTextView, priceTextView, locationTextView,
            titleTextView, eventDateTextView, contactTextView,
            userTextView, catTextView, tagsTextView,
            commentsCountText, likesCountText, viewsCountField, shareText,
            saveText;

    private CircleImageView userImage; //image of user who posted post
    private ConstraintLayout titleLayout, descLayout,
            locationLayout, priceLayout, timeLayout,
            eventDateLayout, contactLayout, userLayout,
            catLayout, tagsLayout, actionsLayout;

    private String postUserId, currentUserId, contactDetails,
            desc, postId, postTitle,
            reportDetailsString,
            postImageUri, postThumbUrl;
    private ImageView postImage, commentsButton, shareButton, saveButton, likeButton, viewsButton;
    private android.support.v7.widget.Toolbar toolbar;
    //progress
    private ProgressDialog progressDialog;
    //save categories to list
    private ArrayList<String> catArray, catKeys, reportedItems, flagsArray, tags;

    private boolean isAdmin = false;
    private int likes = 0;
    private int comments = 0;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem editPost = menu.findItem(R.id.editMenuItem);
        MenuItem deletePost = menu.findItem(R.id.deleteMenuItem);
        MenuItem postStats = menu.findItem(R.id.postStatsMenuItem);
        MenuItem promotePost = menu.findItem(R.id.promoteMenuItem);

        if (coMeth.isConnected()) {
            if (coMeth.isLoggedIn()) {
                currentUserId = new CoMeth().getUid();

                //check if current user is the post user
                //check if user is admin
                if (currentUserId.equals(postUserId)) {
                    editPost.setVisible(true);
                    deletePost.setVisible(true);
                    postStats.setVisible(true);
                    promotePost.setVisible(false); // TODO: 5/31/18 remove on launch
                } /*else if (hasAdminAccess()) {
                    editPost.setVisible(true);
                    deletePost.setVisible(true);
                }*/ else {
                    editPost.setVisible(false);
                    deletePost.setVisible(false);
                    postStats.setVisible(false);
                    promotePost.setVisible(false); // TODO: 5/31/18 remove on launch
                }
            }
        } else {
            editPost.setVisible(false);
            deletePost.setVisible(false);
            postStats.setVisible(false);
            promotePost.setVisible(false); // TODO: 5/31/18 remove on launch
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
        // TODO: 6/1/18 use string resources 
        if (getIntent() != null &&
                getIntent().getStringExtra(getResources().getString(R.string.PERMISSION_NAME)) != null) {
            if (getIntent().getStringExtra(getResources().getString(R.string.PERMISSION_NAME))
                    .equals(getResources().getString(R.string.ADMIN_VAL))) {
                isAdmin = true;
            }
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
            case R.id.postStatsMenuItem:
                showPostStats();
                break;
            case R.id.promoteMenuItem:
                goToPromote();
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

    private void goToPromote() {
        // TODO: 5/31/18 go to promote page
    }

    private void showPostStats() {
        Log.d(TAG, "showPostStats: ");
        AlertDialog.Builder statsBuilder = new AlertDialog.Builder(this);
        statsBuilder.setTitle(R.string.post_stats_text)
                .setIcon(getResources().getDrawable(R.drawable.ic_action_stats))
                .setMessage(
                        getResources().getString(R.string.likes_text) + ": " + likesCountText.getText() + "\n" +
                                getResources().getString(R.string.comments_text) + ": " + commentsCountText.getText() + "\n" +
                                getResources().getString(R.string.views_text) + ": " + viewsCountField.getText()
                )
                .setPositiveButton(getResources().getString(R.string.ok_text),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .show();

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
                                                    showSnack(getResources().getString(
                                                            R.string.something_went_wrong_text));
                                                }
                                                if (!task.getResult().exists()) {
                                                    //post has not been reported
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
                .setIcon(getResources().getDrawable(R.drawable.ic_action_red_alert))
                .setPositiveButton(getResources().getString(R.string.delete_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        showProgress("Deleting...");
                        coMeth.getDb().collection("Posts").document(postId).delete();
                        coMeth.stopLoading(progressDialog, null);
                        Intent delResultIntent =
                                new Intent(ViewPostActivity.this, MainActivity.class);
                        delResultIntent.putExtra(
                                getResources().getString(R.string.ACTION_NAME), getResources().getString(R.string.notify_value_text));
                        delResultIntent.putExtra(getResources().getString(R.string.MESSAGE_NAME), getString(R.string.del_success_text));
                        if (hasAdminAccess()) {
                            //go back to admin page
                            startActivity(new Intent(
                                    ViewPostActivity.this, AdminActivity.class));
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
                getIntent().getStringExtra(getResources().getString(R.string.PERMISSION_NAME)) != null &&
                getIntent().getStringExtra(getResources().getString(R.string.PERMISSION_NAME))
                        .equals(getResources().getString(R.string.ADMIN_VAL)) &&
                isAdmin();
    }

    private void goToEdit() {

        Intent editIntent = new Intent(ViewPostActivity.this, CreatePostActivity.class);
        editIntent.putExtra(getResources().getString(R.string.EDIT_POST_NAME), postId);
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
                if (isTaskRoot()) {
                    // TODO: 6/5/18 test when launching from 3rd party app
                    Log.d(TAG, "onClick: is task toot, going to main");
                    startActivity(new Intent(ViewPostActivity.this, MainActivity.class));
                    finish();
                } else {
                    Log.d(TAG, "onClick: is not task root");
                    finish();
                }
            }
        });

        //initialize items
        actionsLayout = findViewById(R.id.viewPostActionsLayout);
        likeButton = findViewById(R.id.viewPostLikeImageView);
        likesCountText = findViewById(R.id.viewPostLikesCountsTextView);
        commentsButton = findViewById(R.id.viewPostCommentImageView);
        commentsCountText = findViewById(R.id.viewPostCommentTextView);

        saveButton = findViewById(R.id.viewPostSaveImageView);
        saveText = findViewById(R.id.viewPostSaveTextView);

        shareButton = findViewById(R.id.viewPostShareImageView);
        shareText = findViewById(R.id.viewPostShareTextView);

        viewsCountField = findViewById(R.id.viewPostViewsCountTextView);
        viewsButton = findViewById(R.id.viewPostViewsImageView);

        titleTextView = findViewById(R.id.viewPostTitleTextView);
        descTextView = findViewById(R.id.viewPostDescTextView);
        eventDateTextView = findViewById(R.id.viewPostEventDateTextView);
        timeTextView = findViewById(R.id.viewPostTimeTextView);
        priceTextView = findViewById(R.id.viewPostPriceTextView);
        locationTextView = findViewById(R.id.viewPostLocationTextView);
        postImage = findViewById(R.id.viewPostImageView);
        contactTextView = findViewById(R.id.viewPostContactTextView);
        userImage = findViewById(R.id.viewPostUserImageView);

        titleLayout = findViewById(R.id.viewPostTitleLayout);
        descLayout = findViewById(R.id.viewPostDescLayout);
        locationLayout = findViewById(R.id.viewPostLocationLayout);
        priceLayout = findViewById(R.id.viewPostPriceLayout);
        timeLayout = findViewById(R.id.viewPostTimeLayout);
        eventDateLayout = findViewById(R.id.viewPostEventDateLayout);
        contactLayout = findViewById(R.id.viewPostContactLayout);

        tagsTextView = findViewById(R.id.viewPostTagsTextView);
        tagsLayout = findViewById(R.id.viewPostTagsLayout);

        userTextView = findViewById(R.id.viewPostUserTextView);
        userLayout = findViewById(R.id.viewPostUserLayout);

        catLayout = findViewById(R.id.viewPostCatLayout);
        catTextView = findViewById(R.id.viewPostCatTextView);
        catArray = new ArrayList<>();
        catKeys = new ArrayList<>();
        flagsArray = new ArrayList<>();
        reportedItems = new ArrayList<String>();
        reportDetailsString = "";
        tags = new ArrayList<>();


        //handle intent
        handleIntent();

        //showprogress
        showProgress(getResources().getString(R.string.loading_text));

        //get post title on create
        if (coMeth.isConnected()) {
            //handle action clicks
            //handle comments click
            commentsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open comments page
                    openComments();

                }
            });
            commentsCountText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openComments();
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
                                ViewPostActivity.this.likes = likes;
                                //update post likes
                                updatePostLikes(ViewPostActivity.this.likes);
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
                                comments = numberOfComments;
                                updatePostComments(comments);
                            }
                        }
                    });

            //set likes and save status
            if (coMeth.isConnected() && coMeth.isLoggedIn()) {
                //get likes
                //determine likes by current user

                final String currentUserId = coMeth.getUid();

                //handle like button
                coMeth.getDb()
                        .collection("Posts/" + postId + "/Likes")
                        .document(currentUserId)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot,
                                                FirebaseFirestoreException e) {

                                //update the like button real time
                                if (documentSnapshot.exists()) {

                                    Log.d(TAG, "at get likes, updating likes real time");
                                    //user has liked
                                    likeButton.setImageDrawable(
                                            getResources().getDrawable(R.drawable.ic_action_liked));

                                    //handle click like to remove like
                                    likeButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //remove save
                                            unlikePost(currentUserId);
                                        }
                                    });
                                    likesCountText.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            unlikePost(currentUserId);
                                        }
                                    });

                                } else {

                                    //current user has not liked the post
                                    likeButton.setImageDrawable(
                                            getResources().getDrawable(R.drawable.ic_action_like_unclicked));

                                    likeButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //add save
                                            likePost(currentUserId);
                                        }
                                    });
                                    likesCountText.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            likePost(currentUserId);
                                        }
                                    });
                                }
                            }
                        });

                //handle save button
                coMeth.getDb()
                        .collection("Posts/" + postId + "/Saves")
                        .document(currentUserId)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(
                                    DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                //update the save button real time
                                if (documentSnapshot.exists()) {

                                    Log.d(TAG, "at get saves, updating saves realtime");
                                    //user has saved post
                                    saveButton.setImageDrawable(
                                            getResources().getDrawable(R.drawable.ic_action_bookmarked));
                                    //handle save button click
                                    saveButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //remove saved on click
                                            unsavePost(currentUserId);
                                        }
                                    });
                                    saveText.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            unsavePost(currentUserId);
                                        }
                                    });

                                } else {

                                    //user has not liked post
                                    saveButton.setImageDrawable(
                                            getResources().getDrawable(R.drawable.ic_action_bookmark_outline));
                                    //handle save button click
                                    saveButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //save post
                                            savePost(currentUserId);
                                        }
                                    });
                                    saveText.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            savePost(currentUserId);
                                        }
                                    });

                                }
                            }
                        });

            } else {
                if (!coMeth.isConnected()) {
                    //save button
                    saveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            goToMain(getResources().getString(R.string.failed_to_connect_text));
                        }
                    });
                    likeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            goToMain(getResources().getString(R.string.failed_to_connect_text));
                        }
                    });
                }
                if (!coMeth.isLoggedIn()) {
                    saveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            goToLogin(getResources().getString(R.string.login_to_save_text));
                        }
                    });
                    likeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            goToLogin(getResources().getString(R.string.login_to_like));
                        }
                    });
                }
            }

            //set contents
            coMeth.getDb()
                    .collection("Posts")
                    .document(postId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                //post exists
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

                                //handle views
                                updateViews(post);
                                //handle views visibility
                                if (coMeth.isLoggedIn() &&
                                        coMeth.getUid().equals(post.getUser_id())) {
                                    viewsCountField.setVisibility(View.VISIBLE);
                                    viewsButton.setVisibility(View.VISIBLE);
                                    int views = post.getViews();
                                    viewsCountField.setText(String.valueOf(views));

                                } else {
                                    //viewer has no credentials
                                    //hide views
                                    viewsCountField.setVisibility(View.GONE);
                                    viewsButton.setVisibility(View.GONE);
                                }


                                //set tags
                                tags = post.getTags();
                                if (tags != null && !tags.isEmpty()) {
                                    String tagsString = "";
                                    for (int i = 0; i < tags.size(); i++) {
                                        tagsString = tagsString.concat("#" + tags.get(i) + " ");
                                    }
                                    tagsTextView.setText(tagsString.trim());
                                } else {
                                    tagsLayout.setVisibility(View.GONE);
                                }

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
                                    contactLayout.setVisibility(View.GONE);
                                    Log.d(TAG, "onEvent: has no contact details");

                                }

                                //set location
                                ArrayList<String> locationArray = post.getLocation();
                                String locationString = "";

                                if (locationArray != null) {

                                    Log.d(TAG, "onEvent: has location");
                                    Log.d(TAG, "onEvent: location is " + locationArray);

                                    for (int i = 0; i < locationArray.size(); i++) {
                                        locationString = locationString.concat(
                                                locationArray.get(i).toString() + "\n");
                                    }
                                    locationTextView.setText(locationString.trim());
                                } else {
                                    locationLayout.setVisibility(View.GONE);
                                }

                                //set price
                                String price = post.getPrice();
                                if (price != null && !price.isEmpty()) {
                                    priceTextView.setText(price);
                                } else {
                                    priceLayout.setVisibility(View.GONE);
                                }

                                //set event date
                                if (post.getEvent_date() != null) {
                                    long eventDate = post.getEvent_date().getTime();
                                    Log.d(TAG, String.valueOf(eventDate));
                                    String eventDateString = DateFormat.format("EEE, MMM d, 20yy",
                                            new Date(eventDate)).toString();
                                    Log.d(TAG, "onEvent: \neventDateString: " + eventDateString);
                                    eventDateTextView.setText(eventDateString);
                                } else {
                                    eventDateLayout.setVisibility(View.GONE);
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
                                                postImage);
                                    } catch (Exception glideException) {
                                        Log.d(TAG, "onEvent: glide exception " +
                                                glideException.getMessage());
                                        //set placeholder image
                                        postImage.setImageDrawable(
                                                getResources().getDrawable(R.drawable.appiconshadow));
                                    }

                                    postImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            openImage();
                                        }
                                    });

                                    Log.d(TAG, "onEvent: image set");
                                } else {
                                    //post has no image, hide the image view
                                    postImage.setVisibility(View.GONE);
                                }

                                //get user id for the post
                                postUserId = post.getUser_id();
                                //set the post user layout click
                                userLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent goToUserPageIntent = new Intent(
                                                ViewPostActivity.this, UserPageActivity.class);
                                        goToUserPageIntent.putExtra("userId", postUserId);
                                        startActivity(goToUserPageIntent);

                                    }
                                });
                                //check db for user
                                getPostUserDetails();

                                //get categories
                                if (post.getCategories() != null) {

                                    Log.d(TAG, "onEvent: post has cats");
                                    String catString = "";

                                    //post has categories
                                    catKeys = post.getCategories();

                                    ArrayList<String> categories = new ArrayList<>();
                                    for (int i = 0; i < catKeys.size(); i++) {
                                        //go through catKeys and get values
                                        String catValue = coMeth.getCatValue(catKeys.get(i));
                                        categories.add(catValue);
                                    }

                                    Log.d(TAG, "onEvent: categories are " + categories);
                                    catArray.clear();
                                    for (int i = 0; i < categories.size(); i++) {

                                        catString = catString.concat(String.valueOf(categories.get(i)) + "\n");
                                        //update the catArrayList
                                        catArray.add(String.valueOf(categories.get(i)));

                                    }
                                    catTextView.setText(catString.trim());

                                } else {
                                    catLayout.setVisibility(View.GONE);
                                    Log.d(TAG, "onEvent: post has no cats");
                                }

                                coMeth.stopLoading(progressDialog);

                            } else {
                                //post does not exist
                                coMeth.stopLoading(progressDialog);
                                Log.d(TAG, "Error: post does not exist");
                                //save error and notify in main
                                goToMain(getString(R.string.post_not_found_text));
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: failed to get post\n" + e.getMessage());
                            goToMain(getString(R.string.something_went_wrong_text) + ": " + e.getMessage());
                        }
                    });

            //handle clicks

            //views click
            viewsButton.setOnClickListener(this);
            viewsCountField.setOnClickListener(this);
//            postImage.setOnClickListener(this);
            tagsLayout.setOnClickListener(this);
            locationLayout.setOnClickListener(this);
            shareButton.setOnClickListener(this);
            shareText.setOnClickListener(this);
            catLayout.setOnClickListener(this);


            // TODO: 5/28/18 implement swipe image to close activity
        /*//handle swipe image to close
        postImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    finish();
                    return true;
                }
                return false;
            }
        });*/

        } else {
            //device is not connected
//            goToMain(getResources().getString(R.string.failed_to_connect_text));
            showSnack(getResources().getString(R.string.failed_to_connect_text));
        }
    }

    private void getPostUserDetails() {
        coMeth.getDb()
                .collection("Users")
                .document(postUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //user exists
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
                                        getResources().getDrawable(
                                                R.drawable.ic_action_person_placeholder));
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
                                userLayout.setVisibility(View.GONE);

                            }
                        } else {
                            //user does not exits
                            //delete post
                            coMeth.getDb()
                                    .collection("Posts")
                                    .document(postId)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: post with no user deleted");
                                            goToMain(getResources().getString(R.string.post_not_found_text));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: failed to delete post with no user");
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get post details from db\n" + e.getMessage());
//                                                goToMain(getResources().getString(R.string.something_went_wrong_text) +": "
//                                                        + e.getMessage() );
                    }
                });
    }

    private void openTags() {
        Log.d(TAG, "onClick: tags are: " + tags);
        AlertDialog.Builder tagsBuilder =
                new AlertDialog.Builder(ViewPostActivity.this);
        tagsBuilder.setTitle(getString(R.string.tags_text))
                .setIcon(getResources().getDrawable(R.drawable.ic_action_tags))
                .setItems(tags.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent searchTagIntent = new Intent(
                                ViewPostActivity.this, SearchableActivity.class);
                        searchTagIntent.putExtra("tag", tags.get(which));
                        startActivity(searchTagIntent);
                    }
                })
                .show();
    }

    private void openImage() {
        Log.d(TAG, "openImage: ");
        //open image in full screen
        Intent openImageIntent = new Intent(
                ViewPostActivity.this, ViewImageActivity.class);
        openImageIntent.putExtra("imageUrl", postImageUri);
        startActivity(openImageIntent);
    }

    /**
     * update the number of comments on a post
     * @param comments the number of comments in post
     */
    private void updatePostComments(int comments) {
        Log.d(TAG, "updatePostComments: ");
        //create a comments map
        Map<String, Object> commentsMap = new HashMap<>();
        commentsMap.put("comments", comments);
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .update(commentsMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: post comments updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update post comments\n" +
                                e.getMessage());
                    }
                });
    }

    /**
     * update the number of likes of post
     *
     * @param likes the number of likes of post
     */
    private void updatePostLikes(int likes) {
        Log.d(TAG, "updatePostLikes: ");
        //create a Map
        Map<String, Object> likesMap = new HashMap<>();
        likesMap.put("likes", likes);
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .update(likesMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: likes updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update post likes\n" +
                                e.getMessage());
                    }
                });
    }

    /**
     * save current post to current user
     * @param currentUserId the postId of post to be saved
     * */
    private void savePost(String currentUserId) {
        Map<String, Object> savesMap = new HashMap<>();
        savesMap.put("timestamp", FieldValue.serverTimestamp());
        coMeth.getDb().collection("Posts/" + postId + "/Saves")
                .document(currentUserId)
                .set(savesMap);
        //notify user that post has been saved
        showSaveSnack(getString(R.string.added_to_saved_text));
    }

    private void unsavePost(String currentUserId) {
        coMeth.getDb().collection("Posts/" + postId + "/Saves")
                .document(currentUserId)
                .delete();
    }

    private void likePost(String currentUserId) {
        Map<String, Object> likesMap = new HashMap<>();
        likesMap.put("timestamp", FieldValue.serverTimestamp());
        //can alternatively ne written
        coMeth.getDb()
                .collection("Posts/" + postId + "/Likes")
                .document(currentUserId).set(likesMap);

        //notify subscribers
        String notifType = "likes_updates";
        new Notify().execute(notifType, postId);
        Log.d(TAG, "onComplete: notification sent");
    }

    private void unlikePost(String currentUserId) {
        coMeth.getDb()
                .collection("Posts/" + postId + "/Likes")
                .document(currentUserId).delete();
    }

    private void openComments() {
        Intent commentsIntent = new Intent(
                ViewPostActivity.this, CommentsActivity.class);
        commentsIntent.putExtra("postId", postId);
        startActivity(commentsIntent);
    }

    private void shareDynamicLink(String postUrl) {
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(postUrl))
                .setDynamicLinkDomain(getString(R.string.dynamic_link_domain))
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(coMeth.minVerCode)
                        .setFallbackUrl(Uri.parse(getString(R.string.playstore_url)))
                        .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(titleTextView.getText().toString())
                                .setDescription(descTextView.getText().toString())
                                .setImageUrl(Uri.parse(getPostImageUrl()))
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
                            String postTitle = titleTextView.getText().toString();
                            String fullShareMsg = postTitle + "\n" +
                                    shortLink;
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                                    getResources().getString(R.string.app_name));
                            shareIntent.putExtra(Intent.EXTRA_TEXT, fullShareMsg);
                            coMeth.stopLoading(progressDialog);
                            startActivity(Intent.createChooser(shareIntent,
                                    getString(R.string.share_with_text)));
                        } else {
                            Log.d(TAG, "onComplete: " +
                                    "\ncreating short link task failed\n" +
                                    task.getException());
                            coMeth.stopLoading(progressDialog);
                            showSnack(getString(R.string.failed_to_share_text));
                        }
                    }
                });
    }

    /**
     * Checks if the post has an image
     *
     * @return String image download url
     * if post has image returns post image url
     * else returns the default app icon download url
     */
    private String getPostImageUrl() {
        if (postImageUri != null) {
            return postImageUri;
        } else {
            return getString(R.string.app_icon_url);
        }
    }

    private void handleIntent() {
        if (coMeth.isConnected()) {
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
                /*if (hasAdminAccess()) {
                    startActivity(new Intent(
                            ViewPostActivity.this, AdminActivity.class));
                    finish();
                } else {
                    goToMain(getString(R.string.something_went_wrong_text));
                }*/
                goToMain(getString(R.string.something_went_wrong_text));
            }
        } else {
            showSnack(getResources().getString(R.string.failed_to_connect_text));
        }
    }



    /**
     * update the number of times a post is viewed
     * @param post the Posts post that is being viewed
     */
    private void updateViews(Posts post) {
        Log.d(TAG, "updateViews: ");

        if (!coMeth.isLoggedIn() ||
                (coMeth.isLoggedIn() && !coMeth.getUid().equals(post.getUser_id()))) {

            //update view
            int views = post.getViews() + 1;
            //update views
            Map<String, Object> viewsMap = new HashMap<>();
            viewsMap.put("views", views);
            coMeth.getDb()
                    .collection("Posts")
                    .document(postId)
                    .update(viewsMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: views updated");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: failed to update views\n" + e.getMessage());
                        }
                    });
        }
    }

    private void goToMain(String message) {
        Intent goToMainIntent = new Intent(ViewPostActivity.this, MainActivity.class);
        goToMainIntent.putExtra(getString(R.string.ACTION_NAME),
                getString(R.string.notify_value_text));
        goToMainIntent.putExtra(getString(R.string.MESSAGE_NAME), message);
        startActivity(goToMainIntent);
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

                        Intent goToSavedIntent = new Intent(
                                ViewPostActivity.this, MainActivity.class);
                        goToSavedIntent.putExtra(getString(R.string.ACTION_NAME),
                                getString(R.string.GOTO_VAL));
                        goToSavedIntent.putExtra(getString(R.string.DESTINATION_NAME),
                                getString(R.string.saved_value_text));
                        startActivity(goToSavedIntent);
                        finish();
                    }
                })
                .show();
    }

    private void goToLogin(String message) {
        Intent loginIntent = new Intent(ViewPostActivity.this, LoginActivity.class);
        loginIntent.putExtra(getString(R.string.MESSAGE_NAME), message);
        startActivity(loginIntent);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.viewPostViewsCountTextView:
                showPostStats();
                break;
            case R.id.viewPostViewsImageView:
                showPostStats();
                break;
            case R.id.viewPostLocationLayout:
                openMap();
                break;
            case R.id.viewPostCatLayout:
                showCats();
                break;
            case R.id.viewPostShareImageView:
                sharePost();
                break;
            case R.id.postShareTextTextView:
                sharePost();
                break;
            case R.id.viewPostImageView:
                openImage();
                break;
            case R.id.viewPostTagsLayout:
                openTags();
                break;
            default:
                Log.d(TAG, "onClick: on details click view post activity");

        }
    }

    private void sharePost() {
        Log.d(TAG, "Sharing post");
        //get post url
        showProgress(getString(R.string.loading_text));
        String postUrl = getResources().getString(R.string.fursa_url_post_head) + postId;
        shareDynamicLink(postUrl);
    }

    private void openMap() {
        //launch google maps and search for location
        String location = locationTextView.getText().toString();
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void showCats() {
        //open alert dialog
        AlertDialog.Builder catDialogBuilder = new AlertDialog.Builder(ViewPostActivity.this);
        catDialogBuilder.setTitle(getResources().getString(R.string.categories_text))
                .setIcon(getResources().getDrawable(R.drawable.ic_action_categories))
                .setItems(catArray.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //open view cat activity
                        Intent catIntent = new Intent(
                                ViewPostActivity.this, ViewCategoryActivity.class);
                        catIntent.putExtra("category", catKeys.get(which));
                        startActivity(catIntent);
                        finish();

                        Log.d(TAG, "onClick: \nuser selected cat is: " + catKeys.get(which));
                    }
                })
                .show();
    }
}
