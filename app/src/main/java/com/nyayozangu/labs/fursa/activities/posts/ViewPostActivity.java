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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.ViewImageActivity;
import com.nyayozangu.labs.fursa.activities.categories.ViewCategoryActivity;
import com.nyayozangu.labs.fursa.activities.comments.CommentsActivity;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    MenuItem editPost;
    MenuItem deletePost;
    //post image
    String postImageUri;
    String postThumbUrl;
    private ImageView viewPostImage;
    private FloatingActionButton viewPostActionsFAB;
    private TextView descTextView;
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
    //postId
    private String postId;
    //common methods
    private CoMeth coMeth = new CoMeth();
    private ArrayList<String> reportedItems;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem editPost = menu.findItem(R.id.editMenuItem);
        MenuItem deletePost = menu.findItem(R.id.deleteMenuItem);

        if (coMeth.isConnected()) {
            if (coMeth.isLoggedIn()) {
                currentUserId = new CoMeth().getUid();

                if (currentUserId.equals(postUserId)) {
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

                    showLoginAlertDialog(getString(R.string.login_to_report));

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
                    public void onClick(DialogInterface dialog, int which) {

                        if (coMeth.isConnected()) {

                            showProgress("Submitting...");
                            //add post details to db
                            Map<String, Object> reportMap = new HashMap<>();
                            reportMap.put("reporterUserId", coMeth.getUid());
                            reportMap.put("postId", postId);
                            reportMap.put("timestamp", FieldValue.serverTimestamp());
                            reportMap.put("flags", reportedItems);
                            coMeth.getDb()
                                    .collection("Flags")
                                    .document(postId)
                                    .collection("Flags")
                                    .document(coMeth.getUid())
                                    .set(reportMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            progressDialog.dismiss();
                                            if (task.isSuccessful()) {

                                                //alert user
                                                showConfirmReport();

                                            } else {

                                                showSnack(getString(R.string.report_submit_failed_text));
                                                Log.d(TAG, "onComplete: " + task.getException());

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

    private void showConfirmReport() {
        AlertDialog.Builder reportSuccessBuilder = new AlertDialog.Builder(ViewPostActivity.this);
        reportSuccessBuilder.setTitle(getString(R.string.report_text))
                .setIcon(R.drawable.ic_action_red_flag)
                .setMessage("Your report has been submitted for reviews.")
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
                        new CoMeth().getDb().collection("Posts").document(postId).delete();
                        progressDialog.dismiss();
                        Intent delResultIntent = new Intent(ViewPostActivity.this, MainActivity.class);
                        delResultIntent.putExtra("notify", getString(R.string.del_success_text));
                        startActivity(delResultIntent);
                        finish();

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

    private void goToEdit() {

        Intent editIntent = new Intent(ViewPostActivity.this, CreatePostActivity.class);
        editIntent.putExtra("editPost", postId);
        startActivity(editIntent);
        finish();

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
        eventDateTextView = findViewById(R.id.createPostEventDateTextView);
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
        reportedItems = new ArrayList<String>();


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
            goToMain();
        }


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
                //create post url
                String postUrl = getResources().getString(R.string.fursa_url_head) + postId;
                Log.d(TAG, "postUrl is: " + postUrl);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                shareIntent.putExtra(Intent.EXTRA_TEXT, postUrl);
                startActivity(Intent.createChooser(shareIntent, "Share this post with"));

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
                        showLoginAlertDialog(message);
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
                        showLoginAlertDialog(message);

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
                .document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                Log.d(TAG, "at view post query");
                //show progress
                // TODO: 4/22/18 check on editing post showing this progress dialog crashes the app
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
                    String desc = post.getDesc();
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
                        Log.d(TAG, "onEvent: location set");

                    } else {

                        viewPostLocationLayout.setVisibility(View.GONE);
                        Log.d(TAG, "onEvent: has no location");
                    }

                    //set price
                    String price = post.getPrice();
                    if (price != null) {

                        Log.d(TAG, "onEvent: has price");

                        priceTextView.setText(price);

                        Log.d(TAG, "onEvent: price set");
                    } else {

                        viewPostPriceLayout.setVisibility(View.GONE);
                        Log.d(TAG, "onEvent: has no price");

                    }

                    //set event date
                    // TODO: 4/8/18 fix setting date to view post view
                    /*Log.d(TAG,  post.getEvent_date().toString());
                    if (true) {
                        long eventDate = post.getEvent_date().getTime();
                        Log.d(TAG, String.valueOf(eventDate));
                        String eventDateString = DateFormat.format("EEE, MMM d, ''yy - h:mm a", new Date(eventDate)).toString();
                        eventDateTextView.setText(eventDateString);
                    }else{
                        viewPostEventDateLayout.setVisibility(View.GONE);
                    }*/

                    //set the time
                    long millis = post.getTimestamp().getTime();
                    String dateString = DateFormat.format("EEE, MMM d, yyyy - h:mm a", new Date(millis)).toString();
                    String date = "Posted on:\n" + dateString;
                    timeTextView.setText(date);

                    //set post image
                    //add the placeholder image
                    postImageUri = post.getImage_url();
                    postThumbUrl = post.getThumb_url();

                    if (postImageUri != null && postThumbUrl != null) {

                        RequestOptions placeHolderOptions = new RequestOptions();
                        placeHolderOptions.placeholder(R.drawable.ic_action_image_placeholder);
                        Glide.with(getApplicationContext())
                                .applyDefaultRequestOptions(placeHolderOptions)
                                .load(postImageUri)
                                .thumbnail(Glide.with(getApplicationContext()).load(postThumbUrl))
                                .into(viewPostImage);

                        Log.d(TAG, "onEvent: image set");

                    } else {

                        //post has no image, hide the image view
                        viewPostImage.setVisibility(View.GONE);

                    }

                    //get user id for the post
                    postUserId = post.getUser_id();

                    //check db for user
                    coMeth.getDb()
                            .collection("Users")
                            .document(postUserId)
                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            //check if user exists
                            if (documentSnapshot.exists()) {

                                //user exists
                                if (documentSnapshot.get("thumb") != null) {

                                    //user has thumb
                                    String userThumbDwnUrl = documentSnapshot.get("thumb").toString();
                                    coMeth.setImage(R.drawable.ic_action_person_placeholder,
                                            userThumbDwnUrl,
                                            userImage);
                                    Log.d(TAG, "onEvent: user thumb set");

                                } else if (documentSnapshot.get("image") != null) {

                                    //use has no thumb but has image
                                    String userImageDwnUrl = documentSnapshot.get("image").toString();
                                    coMeth.setImage(R.drawable.ic_action_person_placeholder,
                                            userImageDwnUrl,
                                            userImage);
                                    Log.d(TAG, "onEvent: user thumb set");

                                } else {

                                    //user has no image or thumb
                                    userImage.setImageDrawable(getDrawable(R.drawable.ic_action_person_placeholder));
                                    Log.d(TAG, "onEvent: placeholder user image set");

                                }

                                //set name
                                //get user name
                                if (documentSnapshot.get("name") != null) {

                                    String username = documentSnapshot.get("name").toString();
                                    String userNameMessage = getString(R.string.posted_by_text) + "\n" + username;
                                    userTextView.setText(userNameMessage);


                                } else {

                                    //use name is null, hide the user layout
                                    viewPostUserLayout.setVisibility(View.GONE);

                                }

                            } else {

                                //user does not exist
                                userImage.setImageDrawable(getDrawable(R.drawable.ic_action_person_placeholder));

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
                            String catValue = getCatValue(catKeys.get(i).toString());
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


                } else {
                    //post does not exist
                    Log.d(TAG, "Error: post does not exist");
                    //save error and notify in main
                    Intent postNotFountIntent = new Intent(ViewPostActivity.this, MainActivity.class);
                    // TODO: 4/21/18 handle cant find post exception
//                    postNotFountIntent.putExtra("error", "Could not find post");
                    startActivity(postNotFountIntent);
                    finish();
                }
                progressDialog.dismiss();
            }
        });

        //handle clicks
        viewPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open image in full screen
                Intent openImageIntent = new Intent(ViewPostActivity.this, ViewImageActivity.class);
                openImageIntent.putExtra("imageUrl", postImageUri);
                startActivity(openImageIntent);

            }
        });


        //set onclick listener for category layout
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
                                catIntent.putExtra("category", catKeys.get(which).toString());
                                startActivity(catIntent);
                                finish();

                                Log.d(TAG, "onClick: \nuser selected cat is: " + catKeys.get(which));
                            }
                        })
                        .show();

            }
        });


        locationTextView.setOnClickListener(new View.OnClickListener() {
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

    private String getCatValue(String catValue) {

            /*
            "Featured",
            "Popular",
            "UpComing",
            "Events",
            "Places",
            "Business",
            "Buy and sell",
            "Education",
            "Jobs",
            "Queries"*/


        //return value for key
        switch (catValue) {

            case "featured":
                return getString(R.string.cat_featured);

            case "popular":
                return getString(R.string.cat_popular);

            case "upcoming":
                return getString(R.string.cat_upcoming);

            case "events":
                return getString(R.string.cat_events);

            case "places":
                return getString(R.string.cat_places);

            case "business":
                return getString(R.string.cat_business);

            case "buysell":
                return getString(R.string.cat_buysell);

            case "education":
                return getString(R.string.cat_education);

            case "jobs":
                return getString(R.string.cat_jobs);

            case "queries":
                return getString(R.string.cat_queries);

            default:
                Log.d(TAG, "getCatValue: default");
                return "";

        }
    }

    private void setImage(String downloadUrl) {
        RequestOptions placeHolderOptions = new RequestOptions();
        placeHolderOptions.placeholder(R.drawable.ic_action_person_placeholder);

        Glide.with(getApplicationContext())
                .applyDefaultRequestOptions(placeHolderOptions)
                .load(downloadUrl).into(userImage);

        Log.d(TAG, "onEvent: image set");
    }

    private void callContact(String phone) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
        startActivity(callIntent);
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

            int endOfUrlHead = getResources().getString(R.string.fursa_url_head).length();
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

        // TODO: 4/22/18 when editing post, saving a post crashes the app due to showing porogress bar
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

    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        android.support.v7.app.AlertDialog.Builder loginAlertBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        loginAlertBuilder.setTitle("Login")
                .setIcon(getDrawable(R.drawable.ic_action_alert))
                .setMessage("You are not logged in\n" + message)
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send user to login activity
                        goToLogin();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void goToLogin() {

        Intent loginIntent = new Intent(ViewPostActivity.this, LoginActivity.class);
        loginIntent.putExtra("source", "ViewPost");
        loginIntent.putExtra("postId", postId);
        startActivity(loginIntent);

    }

}
