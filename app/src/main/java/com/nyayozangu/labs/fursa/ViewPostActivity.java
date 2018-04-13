package com.nyayozangu.labs.fursa;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Date;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostActivity extends AppCompatActivity {


    private static final String TAG = "Sean";


    private ImageView viewPostImage;
    private FloatingActionButton viewPostActionsFAB;
    private ImageView closeButton;

    private TextView descTextView;
    private TextView timeTextView;
    private TextView likesTextView;
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
    private ConstraintLayout viewPostLikesLayout;
    private ConstraintLayout viewPostPriceLayout;
    private ConstraintLayout viewPostTimeLayout;
    private ConstraintLayout viewPostEventDateLayout;
    private ConstraintLayout viewPostContactLayout;
    private ConstraintLayout viewPostUserLayout;
    private ConstraintLayout viewPostCatLayout;


    private String contactName;
    private String contactPhone;
    private String contactEmail;


    //progress
    private ProgressDialog progressDialog;


    // TODO: 4/7/18 handle populating data from postId
    // TODO: 4/7/18 if user is not logged in change the comment hint to "login to comment" and send user to login page when comments are clicked

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    //postId
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        final Toolbar toolbar = findViewById(R.id.viewCatToolbar);
        setSupportActionBar(toolbar);

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        //initialize items
        titleTextView = findViewById(R.id.viewPostTitleTextView);
        descTextView = findViewById(R.id.viewPostDescTextView);
        eventDateTextView = findViewById(R.id.createPostEventDateTextView);
        timeTextView = findViewById(R.id.viewPostTimeTextView);
        priceTextView = findViewById(R.id.viewPostPriceTextView);
        locationTextView = findViewById(R.id.viewPostLocationTextView);
        viewPostImage = findViewById(R.id.viewPostImageView);
        likesTextView = findViewById(R.id.viewPostLikesTextView);
        contactTextView = findViewById(R.id.viewPostContactTextView);
        userTextView = findViewById(R.id.viewPostUserTextView);
        catTextView = findViewById(R.id.viewPostCatTextView);

        closeButton = findViewById(R.id.viewPostCloseImageView);
        userImage = findViewById(R.id.viewPostUserImageView);


        viewPostTitleLayout = findViewById(R.id.viewPostTitleLayout);
        viewPostDescLayout = findViewById(R.id.viewPostDescLayout);
        viewPostLocationLayout = findViewById(R.id.viewPostLocationLayout);
        viewPostLikesLayout = findViewById(R.id.viewPostLikesLayout);
        viewPostPriceLayout = findViewById(R.id.viewPostPriceLayout);
        viewPostTimeLayout = findViewById(R.id.viewPostTimeLayout);
        viewPostEventDateLayout = findViewById(R.id.viewPostEventDateLayout);
        viewPostContactLayout = findViewById(R.id.viewPostContactLayout);
        viewPostUserLayout = findViewById(R.id.viewPostUserLayout);
        viewPostCatLayout = findViewById(R.id.viewPostCatLayout);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


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

        db.collection("Posts").document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                    //set the description
                    String desc = post.getDesc();
                    descTextView.setText(desc);

                    //set the contact info
                    contactName = post.getContact_name();
                    contactPhone = post.getContact_phone();
                    contactEmail = post.getContact_email();

                    if (contactName != null) {
                        //name is not empty
                        if (contactPhone != null) {
                            //name and phone not empty
                            if (contactEmail != null) {
                                //phone, email and name are all not empty
                                contactTextView.setText(contactName + "\n" + contactPhone + "\n" + contactEmail);
                            } else {
                                //name and phone are not empty but email is empty
                                contactTextView.setText(contactName + "\n" + contactPhone);
                            }
                        } else {
                            //name is not empty but phone is empty
                            if (contactEmail != null) {
                                //name and email is not empty but phone is empty
                                contactTextView.setText(contactName + "\n" + "\n" + contactEmail);
                            } else {
                                //name is not empty, but email and phone are empty
                                viewPostContactLayout.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        //name is empty
                        if (contactPhone != null) {
                            //name is empty but phone is not empty
                            if (contactEmail != null) {
                                //name is empty but phone and email are not empty
                                contactTextView.setText(contactPhone + "\n" + contactEmail);
                            } else {
                                //name is empty and email are empty but phone is not empty
                                contactTextView.setText(contactPhone);
                            }
                        } else {
                            //name is empty and phone is also empty
                            if (contactEmail != null) {
                                //name and phone are empty but email is not empty
                                contactTextView.setText(contactEmail);
                            } else {
                                //name, phone and email are all empty
                                viewPostContactLayout.setVisibility(View.GONE);
                            }
                        }
                    }


                    //set location
                    String locationName = post.getLocation_name();
                    String locationAddress = post.getLocation_address();
                    if (locationAddress != null && locationAddress != null) {
                        locationTextView.setText(locationName + "\n" + locationAddress);
                    } else {
                        viewPostLocationLayout.setVisibility(View.GONE);
                    }

                    //set price
                    String price = post.getPrice();
                    if (price != null) {
                        priceTextView.setText(price);
                    } else {
                        Log.d(TAG, "onEvent: price is: " + price);
                        viewPostPriceLayout.setVisibility(View.GONE);
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
                    String dateString = DateFormat.format("EEE, MMM d, ''yy - h:mm a", new Date(millis)).toString();
                    timeTextView.setText("Posted on:\n" + dateString);

                    //set post image
                    //add the placeholder image
                    String postImageUri = post.getImage_url();
                    String postThumbUrl = post.getThumb_url();
                    RequestOptions placeHolderOptions = new RequestOptions();
                    placeHolderOptions.placeholder(R.drawable.ic_action_image_placeholder);

                    Glide.with(getApplicationContext())
                            .applyDefaultRequestOptions(placeHolderOptions)
                            .load(postImageUri)
                            .thumbnail(Glide.with(getApplicationContext()).load(postThumbUrl))
                            .into(viewPostImage);


                    //get user id for the post
                    final String userId = post.getUser_id();

                    //check db for user
                    db.collection("Users").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            //check if user exists
                            if (documentSnapshot.exists()) {

                                Log.d(TAG, "onEvent: user exists");

                                //user exists
                                //create object user
                                /*final Posts post = documentSnapshot.toObject(Posts.class).withId(postId);*

                                //set user image
                                //get user thumbDownloadUrl

                                /*String userProfileImageDownloadUrl = documentSnapshot.get("image").toString();*/

                                if (documentSnapshot.get("thumb") != null) {

                                    //user has thumb
                                    String userThumbDwnUrl = documentSnapshot.get("thumb").toString();

                                    Log.d(TAG, "onEvent: user thumb download url is: " + userThumbDwnUrl);

                                    setImage(userThumbDwnUrl);
                                } else if (documentSnapshot.get("image") != null) {


                                    //use has no thumb but has image
                                    String userImageDwnUrl = documentSnapshot.get("image").toString();

                                    setImage(userImageDwnUrl);

                                } else {

                                    //user has no image or thumb
                                    userImage.setImageDrawable(getDrawable(R.drawable.ic_thumb_person));

                                }


                                //set username
                                //get user name
                                if (documentSnapshot.get("name") != null) {

                                    String username = documentSnapshot.get("name").toString();
                                    userTextView.setText(getString(R.string.posted_by_text) + "\n" + username);
                                    Log.d(TAG, "onEvent: username is: " + username);

                                } else {

                                    //use name is null, hide the user layout
                                    viewPostUserLayout.setVisibility(View.GONE);

                                }

                            } else {

                                //user does not exist
                                userImage.setImageDrawable(getDrawable(R.drawable.ic_thumb_person));
                                Log.d(TAG, "onEvent: user does not exist");

                            }


                        }
                    });


                    //get categories
                    if (documentSnapshot.get("categories") != null) {

                        String catString = "";

                        //post has categories
                        ArrayList categories = (ArrayList) documentSnapshot.get("categories");
                        Log.d(TAG, "onEvent: categories are " + categories);
                        for (int i = 0; i < categories.size(); i++) {

                            if (i == categories.size() - 1) {

                                //is last cat
                                catString = catString.concat(String.valueOf(categories.get(i)));

                            } else {

                                //is middle item
                                catString = catString.concat(categories.get(i) + ", ");

                            }

                        }

                        Log.d(TAG, "onEvent: \ncatString is: " + catString);
                        catTextView.setText("Categories:\n" + catString);

                    } else {

                        //post has not categories
                        // hide categories layout
                        viewPostCatLayout.setVisibility(View.GONE);

                    }


                    //set user image and usename
                    //query for users and get user details
                    db.collection("Users").document(post.getUser_id()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            //check if user exists
                            if (documentSnapshot.exists()) {
                                Log.d(TAG, "User exist");
                                //user exists
                                //get ID of user hwo posted
                                String userId = post.getUser_id();
                                Log.d(TAG, "UserId is: " + userId);
                                Users user = documentSnapshot.toObject(Users.class).withId(userId);
                                Log.d(TAG, "user is: " + user.toString());
                                //get user image url
                                String userImageUrl = user.getUserImage();
                                Log.d(TAG, "userImageUrl is: " + userImageUrl);

                                //set user image
                                // TODO: 4/7/18 load user thumb instead of image
                                RequestOptions placeHolderOptions = new RequestOptions();
                                placeHolderOptions.placeholder(R.drawable.ic_thumb_person);
                                Glide.with(getApplicationContext())
                                        .applyDefaultRequestOptions(placeHolderOptions)
                                        .load(userImageUrl)
                                        .into(userImage);

                            } else {
                                //user does not exists
                                Log.d(TAG, "user does not exist");
                            }
                        }
                    });


                } else {
                    //post does not exist
                    Log.d(TAG, "Error: post does not exist");
                    //save error and notify in main
                    Intent postNotFountIntent = new Intent(ViewPostActivity.this, MainActivity.class);
                    postNotFountIntent.putExtra("error", "Could not find post...");
                    startActivity(postNotFountIntent);
                    finish();
                }
                progressDialog.dismiss();
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
                    Log.d(TAG, "post has likes");
                    //set likes to likesTextView
                    if (likes == 1) {
                        likesTextView.setText(likes + " Like");
                    } else {
                        likesTextView.setText(likes + " Likes");
                    }
                } else {
                    //hide the likes view
                    viewPostLikesLayout.setVisibility(View.GONE);
                    Log.d(TAG, "query returned empty");
                }
            }
        });


        //handle contact actions
        viewPostContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder contactActionsDialogBuilder = new AlertDialog.Builder(ViewPostActivity.this);
                if (contactName != null) {
                    contactActionsDialogBuilder.setTitle(contactName);
                } else {
                    contactActionsDialogBuilder.setTitle("Contact");
                }
                // TODO: 4/9/18 handle the empty string bug
                contactActionsDialogBuilder.setIcon(getDrawable(R.drawable.ic_action_contact));
                if (contactPhone != null) {
                    //phone is not null
                    if (contactEmail != null) {
                        //phone and email are available
                        final String[] values = new String[]{
                                contactPhone,
                                contactEmail
                        };
                        contactActionsDialogBuilder.setItems(values, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case 0:
                                        callContact(contactPhone);
                                        break;
                                    case 1:
                                        emailContact(contactEmail);
                                        break;
                                    default:
                                        Log.d(TAG, "at default");
                                        break;
                                }
                            }
                        });
                        contactActionsDialogBuilder.show();
                    }
                } else {
                    //phone is null
                    if (contactEmail != null) {
                        //phone is null but email is available set email
                        emailContact(contactEmail);
                    } else {
                        //phone is null and email is null
                        Log.d(TAG, "email and phone are null");
                    }
                }
            }
        });


        //close post button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go back
                finish();
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

    private void setImage(String downloadUrl) {
        RequestOptions placeHolderOptions = new RequestOptions();
        placeHolderOptions.placeholder(R.drawable.ic_thumb_person);

        Glide.with(getApplicationContext()).applyDefaultRequestOptions(placeHolderOptions)
                .load(downloadUrl).into(userImage);

        Log.d(TAG, "onEvent: image set");
    }

    private void emailContact(String email) {
        // TODO: 4/8/18 fix when sending email email address is not forwarded bug
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, email);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
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
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();

    }
}
