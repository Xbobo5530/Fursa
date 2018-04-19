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


    private ConstraintLayout actionsLayout;


    private String contactName;
    private String contactPhone;
    private String contactEmail;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        toolbar = findViewById(R.id.viewPostToolbar);
        setSupportActionBar(toolbar);
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
        titleTextView = findViewById(R.id.viewPostTitleTextView);
        descTextView = findViewById(R.id.viewPostDescTextView);
        eventDateTextView = findViewById(R.id.createPostEventDateTextView);
        timeTextView = findViewById(R.id.viewPostTimeTextView);
        priceTextView = findViewById(R.id.viewPostPriceTextView);
        locationTextView = findViewById(R.id.viewPostLocationTextView);
        viewPostImage = findViewById(R.id.viewPostImageView);
        likesTextView = findViewById(R.id.viewPostLikesTextView);
        contactTextView = findViewById(R.id.viewPostContactTextView);


        userImage = findViewById(R.id.viewPostUserImageView);


        viewPostTitleLayout = findViewById(R.id.viewPostTitleLayout);
        viewPostDescLayout = findViewById(R.id.viewPostDescLayout);
        viewPostLocationLayout = findViewById(R.id.viewPostLocationLayout);
        viewPostLikesLayout = findViewById(R.id.viewPostLikesLayout);
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
                        catKeys = (ArrayList) documentSnapshot.get("categories");

                        ArrayList<String> categories = new ArrayList<>();
                        for (int i = 0; i < catKeys.size(); i++) {

                            //go through catKeys and get values
                            String catValue = getCatValue(catKeys.get(i).toString());
                            categories.add(catValue);

                        }


                        Log.d(TAG, "onEvent: categories are " + categories);
                        for (int i = 0; i < categories.size(); i++) {

                            if (i == categories.size() - 1) {

                                //is last cat
                                catString = catString.concat(String.valueOf(categories.get(i)));
                                Log.d(TAG, "onEvent: cat size is last");

                            } else {

                                //is middle item
                                catString = catString.concat(categories.get(i) + ", ");
                                Log.d(TAG, "onEvent: cat is middle");

                            }

                            //update the catArrayList
                            catArray.add(String.valueOf(categories.get(i)));


                        }

                        Log.d(TAG, "onEvent: \ncatString is: " + catString);
                        Log.d(TAG, "onEvent: \ncatArray is: " + catArray);
                        catTextView.setText("Categories:\n" + catString);

                    } else {

                        //post has not categories
                        // hide categories layout
                        viewPostCatLayout.setVisibility(View.GONE);

                    }


                    //set user image and username
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
        /*viewPostContactLayout.setOnClickListener(new View.OnClickListener() {
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
        });*/


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
        placeHolderOptions.placeholder(R.drawable.ic_thumb_person);

        Glide.with(getApplicationContext()).applyDefaultRequestOptions(placeHolderOptions)
                .load(downloadUrl).into(userImage);

        Log.d(TAG, "onEvent: image set");
    }

    // TODO: 4/16/18 think of removing dialog

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
