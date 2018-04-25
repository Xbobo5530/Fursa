package com.nyayozangu.labs.fursa.activities.posts;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.categories.ViewCategoryActivity;
import com.nyayozangu.labs.fursa.activities.comments.CommentsActivity;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostActivity extends AppCompatActivity {


    // TODO: 4/21/18 check if is connected before requesting for items

    private static final String TAG = "Sean";


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
    private ArrayList catKeys;


    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    //postId
    private String postId;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (isConnected()) {
            if (isLoggedIn()) {
                currentUserId = mAuth.getCurrentUser().getUid();

                MenuItem editPost = menu.findItem(R.id.editMenuItem);
                MenuItem deletePost = menu.findItem(R.id.deleteMenuItem);

                if (currentUserId.equals(postUserId)) {
                    editPost.setVisible(true);
                    deletePost.setVisible(true);
                } else {
                    editPost.setVisible(false);
                    deletePost.setVisible(false);
                }

            }

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
                Toast.makeText(this, item.getItemId(), Toast.LENGTH_SHORT)
                        .show();
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

    private void deletePost(final String postId) {

        String delConfirmMessage = "Are you sure you want to delete this post?";
        //add alert Dialog
        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(ViewPostActivity.this);
        deleteBuilder.setTitle("Delete Post")
                .setMessage(delConfirmMessage)
                .setIcon(getDrawable(R.drawable.ic_action_alert))
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        showProgress("Deleting...");
                        db.collection("Posts").document(postId).delete();
                        progressDialog.dismiss();
                        Intent delResultIntent = new Intent(ViewPostActivity.this, MainActivity.class);
                        delResultIntent.putExtra("notify", "Post successfully Deleted");
                        startActivity(delResultIntent);
                        finish();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

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
        catKeys = new ArrayList();


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
                if (isConnected()) {

                    //check if user is logged in
                    if (isLoggedIn()) {

                        final String currentUserId = mAuth.getCurrentUser().getUid();

                        db.collection("Posts/" + postId + "/Saves").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                //get data from the saves collections

                                //check if user has already saved the post
                                if (!task.getResult().exists()) {
                                    Map<String, Object> savesMap = new HashMap<>();
                                    savesMap.put("timestamp", FieldValue.serverTimestamp());
                                    //save new post
                                    db.collection("Posts/" + postId + "/Saves").document(currentUserId).set(savesMap);

                                    //notify user that post has been saved

                                    showSnack(R.id.view_post_activity_layout, "Added to saved items");

                                    /*// TODO: 4/19/18 add actions to go view the saved list
                                    Snackbar.make(findViewById(R.id.view_post_activity_layout),
                                            "Added to saved items", Snackbar.LENGTH_LONG)
                                            .setAction("View List", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    //go to saved list
                                                    // TODO: 4/19/18 open the saved it
                                                    FragmentManager manager = getFragmentManager();
                                                    FragmentTransaction transaction = manager.beginTransaction();
                                                    transaction.replace(R.id.container, new SavedFragment());
                                                    transaction.addToBackStack(null);
                                                    transaction.commit();

                                                }
                                            });*/

                                } else {
                                    //delete saved post
                                    db.collection("Posts/" + postId + "/Saves").document(currentUserId).delete();
                                }
                            }
                        });
                    } else {
                        //user is not logged in
                        Log.d(TAG, "user is not logged in");
                        //notify user

                        String message = "Log in to save items";
                        showLoginAlertDialog(message);
                    }
                } else {

                    //user is not connected to the internet
                    //show alert dialog
                    showSnack(R.id.view_post_activity_layout, "Failed to connect to the internet");

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

                if (isConnected()) {

                    if (isLoggedIn()) {

                        final String currentUserId = mAuth.getCurrentUser().getUid();

                        db.collection("Posts/" + postId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                //get data from teh likes collection

                                //check if current user has already liked post
                                if (!task.getResult().exists()) {
                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());

                                    //db.collection("Posts").document(postId).collection("Likes");
                                    //can alternatively ne written
                                    db.collection("Posts/" + postId + "/Likes").document(currentUserId).set(likesMap);

                                } else {
                                    //delete the like
                                    db.collection("Posts/" + postId + "/Likes").document(currentUserId).delete();
                                }
                            }
                        });

                    } else {
                        //user is not logged in
                        Log.d(TAG, "use is not logged in");
                        //notify user

                        String message = "Log in to like items";
                        showLoginAlertDialog(message);

                    }
                } else {

                    //alert user is not connected
                    showSnack(R.id.view_post_activity_layout, "Failed to connect to the internet\nCheck your connection and try again");

                }

                //enable button
                likeButton.setClickable(true);

            }
        });


        //set likes
        db.collection("Posts/" + postId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
        db.collection("Posts/" + postId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
        if (isLoggedIn()) {
            //get likes
            //determine likes by current user

            String currentUserId = mAuth.getCurrentUser().getUid();

            db.collection("Posts/" + postId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    //update the like button real time
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "at get likes, updating likes real time");
                        //user has liked
                        likeButton.setImageDrawable(getDrawable(R.drawable.ic_action_like_accent));
                    } else {
                        //current user has not liked the post
                        likeButton.setImageDrawable(getDrawable(R.drawable.ic_action_like_unclicked));
                    }
                }
            });

            //get saves
            db.collection("Posts/" + postId + "/Saves").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
        db.collection("Posts").document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                    Log.d(TAG, "onEvent: title set");
                    getSupportActionBar().setTitle(title);
                    Log.d(TAG, "onEvent: toolbar title set");


                    //set the description
                    String desc = post.getDesc();
                    descTextView.setText(desc);
                    Log.d(TAG, "onEvent: desc set");

                    //set the contact info
                    ArrayList contactArray = post.getContact_details();
                    if (contactArray != null) {
                        Log.d(TAG, "onEvent: has contact details");

                        String contactString = "";
                        int i = 0;
                        do {

                            //set the first item
                            contactString = contactString.concat(contactArray.get(i).toString() + "\n");
                            i++;

                        } while (i < contactArray.size());

                        //set contactString
                        contactTextView.setText(contactString.trim());
                        Log.d(TAG, "onEvent: contact details set");

                    } else {

                        //hide contact details field
                        viewPostContactLayout.setVisibility(View.GONE);
                        Log.d(TAG, "onEvent: has no contact details");

                    }


                    //set location
                    ArrayList locationArray = post.getLocation();
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
                    String postImageUri = post.getImage_url();
                    String postThumbUrl = post.getThumb_url();

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
                    db.collection("Users").document(postUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            //check if user exists
                            if (documentSnapshot.exists()) {

                                Log.d(TAG, "onEvent: user exists");

                                //user exists
                                if (documentSnapshot.get("thumb") != null) {

                                    //user has thumb
                                    String userThumbDwnUrl = documentSnapshot.get("thumb").toString();
                                    setImage(userThumbDwnUrl);
                                    Log.d(TAG, "onEvent: user thumb set");

                                } else if (documentSnapshot.get("image") != null) {


                                    //use has no thumb but has image
                                    String userImageDwnUrl = documentSnapshot.get("image").toString();
                                    setImage(userImageDwnUrl);
                                    Log.d(TAG, "onEvent: user thumb set");

                                } else {

                                    //user has no image or thumb
                                    userImage.setImageDrawable(getDrawable(R.drawable.ic_action_person_placeholder));
                                    Log.d(TAG, "onEvent: placeholder user image set");

                                }


                                //set username
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

    private boolean isConnected() {

        //check if there's a connection
        Log.d(TAG, "at isConnected");
        Context context = getApplicationContext();
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {

            activeNetwork = cm.getActiveNetworkInfo();

        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean isLoggedIn() {

        //check if user is logged in
        return mAuth.getCurrentUser() != null;

    }

    private String getCatValue(String catValue) {

        /*
            "Featured",
            "Popular",
            "UpComing",
            "Events",
            "Places"
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

        Glide.with(getApplicationContext()).applyDefaultRequestOptions(placeHolderOptions)
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

    private void showSnack(int id, String message) {
        Snackbar.make(findViewById(id),
                message, Snackbar.LENGTH_LONG).show();
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
