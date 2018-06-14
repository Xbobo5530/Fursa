package com.nyayozangu.labs.fursa.activities.posts;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.ViewImageActivity;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.notifications.Notify;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class CreatePostActivity extends AppCompatActivity {

    // TODO: 5/30/18 allow posts with multiple images
    // TODO: 5/30/18 share post after submitting
    // TODO: 6/14/18 to share post, send post is ready notification after submitting
    // TODO: 6/14/18 add multiple images upload 

    private static final String TAG = "Sean";

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    //for file compression
    Bitmap compressedImageFile;
    private CoMeth coMeth = new CoMeth();

    private String postId;

    private android.support.v7.widget.Toolbar toolbar;
    private ImageView createPostImageView;
    private FloatingActionButton editImageFAB;
    private Button submitButton;
    private ProgressDialog progressDialog;

    private Uri postImageUri;
    private String downloadThumbUri;
    private String downloadUri;

    private EditText postTitleEditText;
    private TextView postDescTextView;
    //    private ExpandableTextView postDescTextView;
    private TextView contactTextView;
    private TextView eventDateTextView;
    private TextView priceTextView;
    private TextView catsTextView;
    private String desc;
    private String imageText = "", imageLabels = "";
    private String title;
    private ConstraintLayout descField;
    private ConstraintLayout categoriesField;
    private ConstraintLayout locationField;
    private ConstraintLayout priceField;
    private ConstraintLayout eventDateField;
    private ConstraintLayout contactField;
    private TextView locationTextView;
    private Place postPlace = null;
    private View contactDialogView;

    //contact details
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private ArrayList<String> contactDetails;

    //user
    private String currentUserId;
    private Date eventDate;
    private String price;
    private ArrayList<Integer> mSelectedCats;
    private ArrayList<String> catsStringsArray;
    private ArrayList<String> locationArray;
    private Date timestamp;
    private ArrayList<String> cats;
    private ArrayList<String> tags;

    //submit result
    public boolean isSubmitSuccessful;

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "at CreatePostActivity, onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        toolbar = findViewById(R.id.createPostToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.create_post_text));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        createPostImageView = findViewById(R.id.createPostImageView);
        submitButton = findViewById(R.id.createPostSubmitButton);
        editImageFAB = findViewById(R.id.createPostEditImageFab);
        postTitleEditText = findViewById(R.id.createPostTileEditText);
        postDescTextView = findViewById(R.id.createPostDescTextView);

        descField = findViewById(R.id.createPostDescLayout);

        categoriesField = findViewById(R.id.createPostCategoriesLayout);
        catsTextView = findViewById(R.id.createPostCategoriesTextView);
        mSelectedCats = new ArrayList<>();
        catsStringsArray = new ArrayList<>();

        locationField = findViewById(R.id.createPostLocationLayout);
        locationTextView = findViewById(R.id.createPostLocationTextView);
        locationArray = new ArrayList<>();

        eventDateField = findViewById(R.id.createEventDateDescLayout);
        eventDateTextView = findViewById(R.id.createPostEventDateTextView);

        contactField = findViewById(R.id.createPostContactLayout);
        contactTextView = findViewById(R.id.createPostContactTextView);
        contactDetails = new ArrayList<String>();

        priceField = findViewById(R.id.createPostPriceLayout);
        priceTextView = findViewById(R.id.createPostPriceTextView);

        tags = new ArrayList<>();

        if (coMeth.isLoggedIn()) {

            currentUserId = coMeth.getUid();

        } else {

            //user is not logged in
            Intent homeIntent = new Intent(
                    CreatePostActivity.this, MainActivity.class);
            homeIntent.putExtra(getString(R.string.ACTION_NAME), getString(R.string.notify_value_text));
            homeIntent.putExtra(getString(R.string.MESSAGE_NAME), getString(R.string.not_logged_in_text));
            startActivity(homeIntent);
            finish();

        }

        if (getIntent() != null) {
            handleIntent();
        }


        descField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //crate a dialog tha twill have a
                AlertDialog.Builder builder = new AlertDialog.Builder(CreatePostActivity.this);
                builder.setTitle("Post Description")
                        .setIcon(R.drawable.ic_action_descritption);
                //construct the view
                final EditText input = new EditText(CreatePostActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                builder.setView(input);
                if (!postDescTextView.getText().toString().isEmpty()) {
                    input.setText(postDescTextView.getText().toString());
                }
                builder.setPositiveButton(getString(R.string.done_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        desc = input.getText().toString().trim();
                        postDescTextView.setText(desc);
                    }
                })
                        .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setCancelable(false);
                builder.show();
                Log.d(TAG, "dialog created");

            }
        });


        //open a dialog for contact details
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        contactDialogView = inflater.inflate(R.layout.contact_alert_dialog_content_layout, null);

        contactField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder contactDialogBuilder =
                        new AlertDialog.Builder(CreatePostActivity.this);
                contactDialogBuilder.setTitle("Contact Details")
                        .setIcon(R.drawable.ic_action_contact)
                        .setView(contactDialogView)
                        .setPositiveButton(getString(R.string.done_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //user clicks done
                                EditText contactNameField =
                                        contactDialogView.findViewById(R.id.contactNameDialogEditText);
                                EditText contactPhoneField =
                                        contactDialogView.findViewById(R.id.contactPhoneDialogEditText);
                                EditText contactEmailField =
                                        contactDialogView.findViewById(R.id.contactEmailDialogEditText);

                                //get values
                                contactName = contactNameField.getText().toString().trim();
                                contactPhone = contactPhoneField.getText().toString().trim();
                                contactEmail = contactEmailField.getText().toString().trim();

                                processContactDetails(dialog);
                            }
                        })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel_text),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // user clicks cancel
                                        dialog.cancel();
                                    }
                                })
                        .setCancelable(false);

                //check if view already has parent
                if (contactDialogView.getParent() != null) {
                    ((ViewGroup) contactDialogView.getParent()).removeView(contactDialogView);
                }
                contactDialogBuilder.show();
            }
        });


        //categories field on click listener
        categoriesField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //alert dialog builder
                AlertDialog.Builder catPickerBuilder =
                        new AlertDialog.Builder(CreatePostActivity.this);
                catPickerBuilder.setTitle(getString(R.string.categories_text))
                        .setIcon(getResources().getDrawable(R.drawable.ic_action_cat_light))
                        .setMultiChoiceItems(coMeth.categories, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                                // TODO: 5/3/18 set checked items for categories
                                //what happens when an item is checked
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedCats.add(which);
                                } else if (mSelectedCats.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedCats.remove(Integer.valueOf(which));
                                }
                            }
                        })
                        .setPositiveButton(getString(R.string.done_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //when Done is clicked
                                //show the selected cats on the cats text view
                                String catsString = "";

                                for (int i = 0; i < mSelectedCats.size(); i++) {

                                    //concat catString string
                                    catsString = catsString.concat(coMeth.categories[mSelectedCats.get(i)] + "\n");
                                    //add items to catArray
                                    catsStringsArray.add(coMeth.getCatKey(coMeth.categories[mSelectedCats.get(i)]));

                                }

                                // TODO: 5/3/18 check on creating post selecting cats does not
                                catsTextView.setText(catsString.trim());
                            }
                        })

                        //set negative button
                        .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss the dialog
                                dialog.dismiss();
                            }
                        });

                //clear items before showing
                mSelectedCats.clear();
                catsStringsArray.clear();
                catsTextView.setText("");
                catsTextView.setHint("Select categories for your post");
                //show the dialog
                catPickerBuilder.show();

            }
        });


        //location field
        locationField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open the pick location activity
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(CreatePostActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException |
                        GooglePlayServicesNotAvailableException e) {

                    Log.e(TAG, "onClick: " + e.getMessage());
                    AlertDialog.Builder locationErrorBuilder =
                            new AlertDialog.Builder(CreatePostActivity.this);
                    locationErrorBuilder.setTitle("Error")
                            .setIcon(getResources().getDrawable(R.drawable.ic_action_red_alert))
                            .setMessage(R.string.failed_to_load_locations_text)
                            .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //dismiss the dialog
                                    dialog.dismiss();
                                }
                            })
                            //show the dialog
                            .show();
                }
            }
        });


        //for date picker
        eventDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open date picker dialog
                Calendar calendar = Calendar.getInstance();
                int YEAR = calendar.get(Calendar.YEAR);
                int MONTH = calendar.get(Calendar.MONTH);
                int DAY = calendar.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog eventDatePickerDialog =
                        new DatePickerDialog(CreatePostActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                        // save date to eventDate
                                        eventDate = new Date(year, month, dayOfMonth);
                                        // TODO: 5/2/18 check setting event date on edit post fails
                                        Log.d(TAG, "date selected is: " + eventDate.toString());
                                        //set selected date to the eventDate textView
                                        eventDateTextView.setText(android.text.format.DateFormat
                                                .format("EEE, MMM d, 20yy", eventDate).toString());


                                    }
                                }, YEAR, MONTH, DAY);
                eventDatePickerDialog.show();
            }
        });


        //set the price
        priceField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(CreatePostActivity.this);
                builder.setTitle(getString(R.string.price_text))
                        .setIcon(R.drawable.ic_action_payment);

                final EditText input = new EditText(CreatePostActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                builder.setView(input);
                if (!priceTextView.getText().toString().isEmpty()) {
                    input.setText(priceTextView.getText().toString());
                }
                builder.setPositiveButton(
                        getResources().getString(R.string.done_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                price = input.getText().toString().trim();
                                priceTextView.setText(price);
                            }
                        })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                        .setCancelable(false);
                builder.show();
                Log.d(TAG, "dialog created");

            }
        });


        //on submit
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if internet is connected
                if (coMeth.isConnected()) {

                    //start submitting
                    desc = postDescTextView.getText().toString().trim();
                    title = postTitleEditText.getText().toString().trim();

                    //check if description field is empty
                    if (!TextUtils.isEmpty(desc) && !TextUtils.isEmpty(title)) {

                        //show progress
                        showProgress(getString(R.string.submitting));
                        //disable the submit button
                        submitButton.setClickable(false);
                        if (!isEditPost()) {

                            //is new post
                            Log.d(TAG, "onClick: is new post");

                            // today
                            Calendar date = new GregorianCalendar();
                            // reset hour, minutes, seconds and millis
                            date.set(Calendar.HOUR_OF_DAY, 0);
                            date.set(Calendar.MINUTE, 0);
                            date.set(Calendar.SECOND, 0);
                            date.set(Calendar.MILLISECOND, 0);
                            Log.d(TAG, "onClick: date is " + date + "\ndate in millis is: " +
                                    date.getTimeInMillis());

                            //check user post daily quota
                            coMeth.getDb()
                                    .collection("Users/" +
                                            currentUserId +
                                            "/Subscriptions/my_posts/MyPosts")
                                    .whereGreaterThan("timestamp", date.getTime())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            Log.d(TAG, "onSuccess: got documents");
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                //user has posts from today
                                                //user has posted today
                                                int todayPostCount = queryDocumentSnapshots.size();
                                                Log.d(TAG, "onEvent: user has posted " + todayPostCount + " post(s) today");

                                                coMeth.stopLoading(progressDialog);
                                                if (todayPostCount > 10) {
                                                    AlertDialog.Builder quotaAlertBuilder =
                                                            new AlertDialog.Builder(CreatePostActivity.this);
                                                    quotaAlertBuilder.setTitle("Daily post limit reached")
                                                            .setIcon(getResources().getDrawable(R.drawable.ic_action_quota))
                                                            .setMessage("You have reached you daily posting limit. "/* +
                                                                    "\nWould you like pay for more posts?"*/)
                                                            .setPositiveButton(getResources().getString(R.string.ok_text),
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            //go to payments
                                                                            // TODO: 6/8/18 handle opening payment page
                                                                            //// TODO: 6/8/18 replace replacement code
                                                                            dialog.dismiss();
                                                                            goToMain();
                                                                        }
                                                                    })
                                                            .show();
                                                } else {
                                                    //has not reached cap
                                                    Log.d(TAG, "onSuccess: user has not reached cap");
                                                    new SubmitPostTask().execute();
//                                                  submitPost();
                                                    coMeth.stopLoading(progressDialog);
                                                    goToMain(getString(R.string.post_will_be_available_text));

                                                }
                                            } else {
                                                //user has no posts from today
                                                Log.d(TAG, "onSuccess: there are no documents from today");
                                                //has not reached cap
                                                new SubmitPostTask().execute();
//                                                  submitPost();
                                                coMeth.stopLoading(progressDialog);
                                                goToMain(getString(R.string.post_will_be_available_text));
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: failed to get user's posts from today\n" +
                                                    e.getMessage());
                                        }
                                    });
                        } else {

                            //is edit post
                            Log.d(TAG, "onClick: is edit post");
                            //get the sent intent
                            Intent getEditPostIdIntent = getIntent();
                            postId = getEditPostIdIntent.getStringExtra("editPost");
                            Log.d(TAG, "postId is: " + postId);
                            //update post details
                            coMeth.getDb()
                                    .collection("Posts")
                                    .document(postId)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            //check if task is successful
                                            if (task.isSuccessful()) {

                                                if (task.getResult().exists()) {

                                                    Posts post = task.getResult().toObject(Posts.class);
                                                    if (post.getImage_url() != null)
                                                        downloadUri = post.getImage_url();
                                                    if (post.getThumb_url() != null)
                                                        downloadThumbUri = post.getThumb_url();
                                                    if (post.getCategories() != null)
                                                        cats = post.getCategories();
                                                    if (post.getLocation() != null)
                                                        locationArray = post.getLocation();
                                                    if (post.getContact_details() != null)
                                                        contactDetails = post.getContact_details();
                                                    if (post.getEvent_date() != null)
                                                        eventDate = post.getEvent_date();
                                                    if (post.getPrice() != null)
                                                        price = post.getPrice();

                                                    new SubmitPostTask().execute();
//                                                    submitPost();
                                                    coMeth.stopLoading(progressDialog);
                                                    goToMain(getString(R.string.post_will_be_available_text));
                                                } else {
                                                    //post no longer exists
                                                    Log.d(TAG, "onComplete: post does not exist");
                                                    //go to main
//                                                    goToMain(getResources().getString(R.string.post_not_found_text));
                                                }
                                            } else {
                                                //task failed
                                                Log.d(TAG, "onComplete: task failed" + task.getException());
//                                                showSnack(getString(R.string.failed_to_complete_text));
                                            }
                                        }
                                    });
                        }
                        //re-enable submit button
                        submitButton.setClickable(true);
                    } else {
                        //desc is empty
                        //upload failed
                        showSnack(getString(R.string.enter_post_details_text));
                    }
                } else {
                    //notify user is not connected and cant post
                    showSnack(getString(R.string.failed_to_connect_text));

                }
            }
        });


        //clicking the edit image button
        //set onClickListener for imageView
        editImageFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image picker and crop tool
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        /*.setAspectRatio(16, 9)*/
                        .setMinCropResultSize(512, 512)
                        .start(CreatePostActivity.this);
            }
        });

        //set click listener to image view
        createPostImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postImageUri != null) {
                    String imageUrl = postImageUri.toString();
                    Intent openImageIntent = new Intent(
                            CreatePostActivity.this, ViewImageActivity.class);
                    openImageIntent.putExtra(
                            getResources().getString(R.string.view_image_intent_name), imageUrl);
                    startActivity(openImageIntent);
                }
            }
        });
    }

    /**
     * go to main activity
     */
    private void goToMain() {
        startActivity(new Intent(CreatePostActivity.this, MainActivity.class));
        finish();
    }

    /**
     * processed the contact details
     *
     * @param dialog the contact details alert dialog
     */
    private void processContactDetails(DialogInterface dialog) {
        if (contactName.isEmpty() && contactPhone.isEmpty() && contactEmail.isEmpty()) {
            //all fields are empty
            dialog.cancel();

        } else {

            //at least one field is filled
            if (!contactName.isEmpty()) {
                //name is not empty
                if (!contactPhone.isEmpty()) {
                    //name and phone are not empty
                    if (!contactEmail.isEmpty()) {
                        //has name, phone and email
                        String contactDetails = contactName + "\n" +
                                contactPhone + "\n" +
                                contactEmail;
                        contactTextView.setText(contactDetails);
                    } else {
                        //has name and phone
                        String contactDetails = contactName + "\n" + contactPhone;
                        contactTextView.setText(contactDetails);
                    }
                } else {
                    //has name
                    if (!contactEmail.isEmpty()) {
                        //has name and email
                        String contactDetails = contactName + "\n" + contactEmail;
                        contactTextView.setText(contactDetails);
                    } else {
                        //has name
                        contactTextView.setText("");
                    }
                }

            } else {

                //name is empty
                if (!contactPhone.isEmpty()) {

                    //has phone
                    if (!contactEmail.isEmpty()) {
                        //has email and phone
                        String contactDetails = contactPhone + "\n" + contactEmail;
                        contactTextView.setText(contactDetails);
                    } else {
                        //has phone
                        String contactDetails = contactPhone;
                        contactTextView.setText(contactDetails);
                    }
                } else {

                    //name and phone are empty
                    if (!contactEmail.isEmpty()) {
                        //has email
                        String contactDetails = contactEmail;
                        contactTextView.setText(contactDetails);
                    } else {
                        //phone, email and name are all empty
                        //hide contact field
                        contactTextView.setText("");
                    }
                }
            }
        }
    }

    public void submitPost() {

        Log.d(TAG, "submitPost: at submit post");
        if (postImageUri != null) {
            handlePostWithImage();
        } else {
            //post has no image
            //get map
            handlePostWithoutImage();
        }
    }

    private void handlePostWithoutImage() {
        final Map<String, Object> postMap = handleMap(downloadThumbUri, downloadUri);

        if (!isEditPost()) {
            // TODO: 6/7/18 check why use add not set
            //create new post
            coMeth.getDb()
                    .collection("Posts")
                    .add(postMap)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            //check if posting is successful
                            if (task.isSuccessful()) {

                                //update submit post result
//                                isSubmitSuccessful = true;
//                                goToMain();
                                //notify users subscribed to cats
                                notifyNewPostCatsUpdates(catsStringsArray);
                                Log.d(TAG, "onComplete: posted post without image");
                                String currentPostId = task.getResult().getId();
                                FirebaseMessaging.getInstance().subscribeToTopic(currentPostId);
                                //update my posts
                                updateMyPosts(currentPostId);
                                //get postId
                                final String newPostId = task.getResult().getId();
                                Log.d(TAG, "onComplete: new post id " + newPostId);
                                //update tags on DB
                                updateTagsOnDB(newPostId, postMap);
                                updateCatsOnDB(newPostId, postMap);

                            } else {

                                Log.d(TAG, "onComplete: " + task.getException());
                            }
                        }
                    });

//                return isSubmitSuccessful;
        } else {

            //for edit post
            coMeth.getDb()
                    .collection("Posts")
                    .document(postId)
                    .update(postMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            //check if posting is successful
                            if (task.isSuccessful()) {
                                //update post result
//                                isSubmitSuccessful = true;
//                                goToMain();
                                //notify users subscribed to cats
                                notifyNewPostCatsUpdates(catsStringsArray);
                                Log.d(TAG, "onComplete: posted post without image");
                                //subscribe current user to post comments
                                FirebaseMessaging.getInstance().subscribeToTopic(postId);
                                //update tags on db
                                updateTagsOnDB(postId, postMap);
                                updateCatsOnDB(postId, postMap);
                            } else {
                                //posting failed
                                String errorMessage =
                                        Objects.requireNonNull(task.getException()).getMessage();
//                                    showSnack(getString(R.string.failed_to_post_text));
//                                    isSubmitSuccessful = false;
//                                goToMain(getResources().getString(R.string.failed_to_post_text)
//                                        + ": " + errorMessage);
                            }

                        }
                    });

//                return isSubmitSuccessful;

        }
    }

    private void updateTagsOnDB(final String newPostId, Map<String, Object> postMap) {
        //update tags on db
        //check if post has tags
        if (postMap.get("tags") != null) {
            //cycle through the tags and add them to the tags list
            for (int i = 0; i < tags.size(); i++) {
                final String title = tags.get(i);
                //create tagsMap
                final Map<String, Object> tagsMap = new HashMap<>();
                tagsMap.put("title", title);
                coMeth.getDb()
                        .collection("Tags")
                        .document(title)
                        .update(tagsMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: tags updated");
                                updatePostsOnTags(title, newPostId);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: failed to add new post to Tags collection\n" +
                                        e.getMessage());
                                coMeth.getDb()
                                        .collection("Tags")
                                        .document(title)
                                        .set(tagsMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: created new tag on DB");
                                                updatePostsOnTags(title, newPostId);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: failed to create new tag on DB\n" +
                                                        e.getMessage());
                                            }
                                        });
                            }
                        });

            }
        }
    }

    private void updatePostsOnTags(final String title, final String newPostId) {
        Log.d(TAG, "updatePostsOnTags: ");
        final Map<String, Object> tagsPostMap = new HashMap<>();
        tagsPostMap.put(CoMeth.TIMESTAMP, FieldValue.serverTimestamp());
        coMeth.getDb()
                .collection("Tags/" + title + "/Posts/")
                .document(newPostId)
                .update(tagsPostMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: tags fully updated on DB");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update tags posts on DB\n" +
                                e.getMessage());
                        coMeth.getDb()
                                .collection("Tags/" + title + "/Posts/")
                                .document(newPostId)
                                .set(tagsPostMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: tags fully updated on DB");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: final check, " +
                                                "failed to update posts on tags\n" +
                                                e.getMessage());
                                    }
                                });
                    }
                });
    }

//    private void shareNewPost(String postId, final String postTitle, String postDesc, String postImage) {
//        String imageUrl;
//        String postUrl = getResources().getString(R.string.fursa_url_post_head) + postId;
//        if (postImage != null) {
//            imageUrl = postImage;
//        } else {
//            imageUrl = getResources().getString(R.string.app_icon_url);
//        }
//
//        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
//                .setLink(Uri.parse(postUrl))
//                .setDynamicLinkDomain(getString(R.string.dynamic_link_domain))
//                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
//                        .setMinimumVersion(coMeth.minVerCode)
//                        .setFallbackUrl(Uri.parse(getString(R.string.playstore_url)))
//                        .build())
//                .setSocialMetaTagParameters(
//                        new DynamicLink.SocialMetaTagParameters.Builder()
//                                .setTitle(postTitle)
//                                .setDescription(postDesc)
//                                .setImageUrl(Uri.parse(imageUrl))
//                                .build())
//                .buildShortDynamicLink()
//                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
//                    @Override
//                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
//                        if (task.isSuccessful()) {
//                            Uri shortLink = task.getResult().getShortLink();
//                            Uri flowchartLink = task.getResult().getPreviewLink();
//                            Log.d(TAG, "onComplete: short link is: " + shortLink);
//
//                            //show share dialog
//                            String fullShareMsg = postTitle + "\n" +
//                                    shortLink;
//                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                            shareIntent.setType("text/plain");
//                            shareIntent.putExtra(Intent.EXTRA_SUBJECT,
//                                    getResources().getString(R.string.app_name));
//                            shareIntent.putExtra(Intent.EXTRA_TEXT, fullShareMsg);
//                            coMeth.stopLoading(progressDialog);
//                            startActivity(Intent.createChooser(shareIntent,
//                                    getString(R.string.share_with_text)));
//                        } else {
//                            Log.d(TAG, "onComplete: " +
//                                    "\ncreating short link task failed\n" +
//                                    task.getException());
//                            coMeth.stopLoading(progressDialog);
//                            showSnack(getString(R.string.failed_to_share_text));
//                        }
//                    }
//                });
//    }


    private void handlePostWithImage() {
        Log.d(TAG, "handlePostWithImage: ");
        //generate randomString name for image based on firebase timestamp
        final String randomName = UUID.randomUUID().toString();
        //define path to upload image
        StorageReference filePath =
                coMeth.getStorageRef().child("post_images").child(randomName + ".jpg");
        //upload the image
        filePath.putFile(postImageUri).addOnCompleteListener(
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        //get download url
                        downloadUri = task.getResult().getDownloadUrl().toString();
                        //handle results after attempting to upload
                        if (task.isSuccessful()) {
                            //upload complete
                            Log.d(TAG, "upload successful");
                            File newImageFile = new File(postImageUri.getPath());

                            try {
                                compressedImageFile = new Compressor(CreatePostActivity.this)
                                        .setMaxWidth(100)
                                        .setMaxHeight(100)
                                        .setQuality(3)
                                        .compressToBitmap(newImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //handle Bitmap
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] thumbData = baos.toByteArray();
                            //uploading the thumbnail
                            UploadTask uploadTask = coMeth.getStorageRef().child("post_images/thumbs")
                                    .child(randomName + ".jpg")
                                    .putBytes(thumbData);

                            //on success listener
                            uploadTask.addOnSuccessListener(
                                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            //get downloadUri for thumbnail
                                            downloadThumbUri = taskSnapshot.getDownloadUrl().toString();
                                            //on success
                                            Map<String, Object> postMap = handleMap(downloadThumbUri, downloadUri);
                                            //upload
                                            //check if its update or new post
                                            if (!isEditPost()) {
                                                //is new post
                                                createNewPost(postMap);
                                            } else {
                                                //is edit post
                                                updatePostDetails(postMap);
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //upload failed
                                    Log.d(TAG, "Db Update failed: " + e.getMessage());
//                                showSnack(getString(R.string.failed_to_upload_image_text));
//                                isSubmitSuccessful = false;
//                            goToMain(getResources().getString(R.string.failed_to_post_text) +
//                                    ": " + e.getMessage());
                                }
                            });

                        } else {

                            //post failed
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                        showSnack(getString(R.string.failed_to_upload_image_text));
//                        isSubmitSuccessful = false;
//                    goToMain(getResources().getString(R.string.failed_to_post_text)
//                            + ": " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

    /**
     * Update the post details when editing a post
     *
     * @param postMap a Map with all the post details
     */
    private void updatePostDetails(final Map<String, Object> postMap) {
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .update(postMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        //check the result
                        if (task.isSuccessful()) {

                            //db update successful
                            Log.d(TAG, "Db Update successful");
//                                                        goToMain();
                            //update post submit status
                            isSubmitSuccessful = true;
                            //notify users subscribed to cats
                            notifyNewPostCatsUpdates(catsStringsArray);
                            Log.d(TAG, "onComplete: about to upload " +
                                    "\ncategories are: " + catsStringsArray);
                            //subscribe current user to post comments
                            FirebaseMessaging.getInstance().subscribeToTopic(postId);
                            //update tags on DB
                            updateTagsOnDB(postId, postMap);
                            updateCatsOnDB(postId, postMap);

                        } else {

                            //upload failed
                            Log.d(TAG, "Db Update failed: " +
                                    Objects.requireNonNull(task.getException()).getMessage());
//                          showSnack(getString(R.string.failed_to_upload_image_text));
                            //update submit status
//                          isSubmitSuccessful = false;
//                            goToMain(getResources().getString(R.string.failed_to_post_text) +
//                                    ": " + task.getException().getMessage());

                        }
                    }
                });
    }

    /**
     * create a new post
     *
     * @param postMap a Map containing the details of the post
     */
    private void createNewPost(final Map<String, Object> postMap) {
        coMeth.getDb()
                .collection("Posts")
                .add(postMap)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {

                        //check the result
                        if (task.isSuccessful()) {

                            Log.d(TAG, "onComplete: submit successful");
//                                                        goToMain();
//                                                        isSubmitSuccessful = true;
                            //notify users subscribed to cats
                            notifyNewPostCatsUpdates(catsStringsArray);
                            Log.d(TAG, "onComplete: about to upload " +
                                    "\ncategories are: " + catsStringsArray);
                            //subscribe current user to post comments
                            String currentPostId = task.getResult().getId();
                            FirebaseMessaging.getInstance().subscribeToTopic(currentPostId);
                            //update users posts list
                            updateMyPosts(currentPostId);
                            String newPostId = task.getResult().getId();
                            updateTagsOnDB(newPostId, postMap);
                            updateCatsOnDB(newPostId, postMap);

                        } else {

                            String errorMessage =
                                    Objects.requireNonNull(task.getException()).getMessage();
                            Log.d(TAG, "Db Update failed: " + errorMessage);
//                          showSnack(getString(R.string.failed_to_upload_image_text));
                            //set post submit status
//                          isSubmitSuccessful = false;
//                            goToMain(getResources().getString(R.string.failed_to_post_text) +
//                                    ": " +  errorMessage);
                        }
                    }
                });
    }

    private void updateCatsOnDB(final String newPostId, Map<String, Object> postMap) {
        Log.d(TAG, "updateCatsOnDB: ");
        //check if post has cats
        if (postMap.get(CoMeth.CATEGORIES_VAL) != null) {
            //cycle through the cats
            for (final String cat : catsStringsArray) {
                //create a map
                final Map<String, Object> catMap = new HashMap<>();
                catMap.put("title", cat);
                coMeth.getDb()
                        .collection(CoMeth.CATEGORIES)
                        .document(cat)
                        .update(catMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: cats on db have been updated");
                                updatePostsOnCats(cat, newPostId);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: failed to update cats on db\n" +
                                        e.getMessage());
                                //try to create new cat
                                coMeth.getDb()
                                        .collection(CoMeth.CATEGORIES)
                                        .document(cat)
                                        .set(catMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: cats on db have been updated");
                                                updatePostsOnCats(cat, newPostId);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: failed to set new cat on db\n" +
                                                        e.getMessage());
                                            }
                                        });
                            }
                        });
            }
        }
    }

    private void updatePostsOnCats(final String cat, final String newPostId) {
        Log.d(TAG, "updatePostsOnCats: ");
        //create map
        final Map<String, Object> catsPostMap = new HashMap<>();
        catsPostMap.put(CoMeth.TIMESTAMP, FieldValue.serverTimestamp());
        coMeth.getDb()
                .collection(CoMeth.CATEGORIES + "/" + cat + "/" + CoMeth.POSTS)
                .document(newPostId)
                .update(catsPostMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: updated the post on cat on db");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update post on cat on db\n" +
                                e.getMessage());
                        //attempt to set map
                        coMeth.getDb()
                                .collection(CoMeth.CATEGORIES + "/" + cat + "/" + CoMeth.POSTS)
                                .document(newPostId)
                                .set(catsPostMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: updated the post on cat on db");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: failed to set post to cat on db\n"
                                                + e.getMessage());
                                    }
                                });
                    }
                });
    }

    private void goToMain(String message) {
        Intent goToMainIntent = new Intent(
                CreatePostActivity.this, MainActivity.class);
        goToMainIntent.putExtra(getResources().getString(R.string.ACTION_NAME),
                getResources().getString(R.string.notify_value_text));
        goToMainIntent.putExtra(getResources().getString(R.string.MESSAGE_NAME),
                message);
        goToMainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(goToMainIntent);
        finish();
    }

    /**
     * adds the current post to the list of current user's posts
     * @param currentPostId the user Id of the current user creating a post
     * */
    private void updateMyPosts(String currentPostId) {
        //create map with timestamp
        Map<String, Object> myPostMap = new HashMap<>();
        myPostMap.put("timestamp", FieldValue.serverTimestamp());
        //update current user's subscriptions
        coMeth.getDb()
                .collection("Users/" + currentUserId + "/Subscriptions/")
                .document("my_posts")
                .collection("MyPosts")
                .document(currentPostId)
                .set(myPostMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: myPosts has been updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update my posts\n" + e);
                    }
                });
    }

    /**
     * sends notification to all the users subscribed to the post categories
     * @param catsStringsArray an ArrayList<String> containing  the post categories
     * */
    private void notifyNewPostCatsUpdates(ArrayList<String> catsStringsArray) {
        //send notifications to all users subscribed to cats in catStringArray
        for (int i = 0; i < catsStringsArray.size(); i++) {

            String notifType = "categories_updates";
            Log.d(TAG, "notifyNewPostCatsUpdates: cat is " + catsStringsArray.get(i));
            new Notify().execute(notifType, catsStringsArray.get(i));
        }
    }

    /**
     * creates a map of the post detials
     * @param downloadThumbUri the download url to the post thumbnail
     * @param downloadUri the download url to the post image
     * @return Map a Map containing all the post details
     * */
    @NonNull
    private Map<String, Object> handleMap(String downloadThumbUri, String downloadUri) {

        //store the user info associated with post
        Map<String, Object> postMap = new HashMap<>();
        if (downloadUri != null) {
            postMap.put("image_url", downloadUri);
        }
        if (downloadThumbUri != null) {
            postMap.put("thumb_url", downloadThumbUri);
        }
        if (!imageLabels.isEmpty()) {
            postMap.put("image_labels", imageLabels);
        }
        if (!imageText.isEmpty()) {
            postMap.put("image_text", imageText);
        }

        postMap.put("title", title);
        postMap.put("desc", desc);
        postMap.put("user_id", currentUserId);

        //get the current time
        if (!isEditPost()) {
            postMap.put("timestamp", FieldValue.serverTimestamp());
        }
        //handle contact details
        if (contactDetails != null) {
            processContactDetails();
        }
        //handle location
        if (postPlace != null) {
            //set up an array for location
            locationArray.add(postPlace.getName().toString());
            locationArray.add(postPlace.getAddress().toString());
        }
        //location
        if (locationArray != null && locationArray.size() > 0) {
            postMap.put("location", locationArray);
        }
        //event date
        if (eventDate != null) {
            postMap.put("event_date", eventDate);
        }
        //contact details
        if (contactDetails != null && contactDetails.size() > 0) {
            postMap.put("contact_details", contactDetails);
        }
        //price
        if (price != null) {
            postMap.put("price", price);
        }
        //categories
        if (!catsStringsArray.isEmpty()) {
            postMap.put(CoMeth.CATEGORIES_VAL, catsStringsArray);
        }

        // TODO: 6/1/18 account for then # is in word (not begging or end)
        //create tags
        String titleDesc = (title + " " + desc).replaceAll("\\s+", " ");
        if (titleDesc.contains("#")) {
            Log.d(TAG, "handleMap: has #");
            int hashPos = titleDesc.indexOf("#");
            while (hashPos < titleDesc.length() &&
                    titleDesc.indexOf("#", hashPos) != -1 &&
                    hashPos != -1) {
                if (titleDesc.indexOf(" ", hashPos) != -1) {
                    String tag = titleDesc.substring(hashPos + 1, titleDesc.indexOf(" ", hashPos));
                    tags.add(tag);
                    Log.d(TAG, "handleMap: tag is " + tag);
                } else {
                    String tag = titleDesc.substring(hashPos + 1);
                    tags.add(tag);
                    Log.d(TAG, "handleMap: \nis last word tag is " + tag);
                }
                hashPos = titleDesc.indexOf("#", hashPos + 1);
            }
            Log.d(TAG, "handleMap: tags are " + tags);
            postMap.put("tags", tags);
        }
        return postMap;
    }

    private void processContactDetails() {


        if (contactName != null) {
            if (contactPhone != null) {
                if (contactEmail != null) {
                    contactDetails.add(contactName);
                    contactDetails.add(contactPhone);
                    contactDetails.add(contactEmail);
                } else {
                    contactDetails.add(contactName);
                    contactDetails.add(contactPhone);
                }
            } else {
                if (contactEmail != null) {
                    contactDetails.add(contactName);
                    contactDetails.add(contactEmail);
                } else {
                    contactDetails.add(contactName);
                }
            }
        } else {
            if (contactPhone != null) {
                if (contactEmail != null) {
                    contactDetails.add(contactPhone);
                    contactDetails.add(contactEmail);
                } else {
                    contactDetails.add(contactPhone);
                }
            } else {
                if (contactEmail != null) {
                    contactDetails.add(contactEmail);
                } else {
                    contactDetails = null;
                }
            }
        }
    }

    private boolean isEditPost() {
        return getIntent().hasExtra("editPost");
    }

    private void handleIntent() {

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {

            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }

        }

        if (isEditPost()) {
            //get the sent intent
            Intent getEditPostIdIntent = getIntent();
            String postId = getEditPostIdIntent.getStringExtra("editPost");
            Log.d(TAG, "postId is: " + postId);
            //populate data
            populateEditPostData(postId);

        }
    }

    void handleSendText(Intent intent) {

        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {

            //set shared text to desc field
            postDescTextView.setText(sharedText);
            Log.d(TAG, "handleSendText: shared text is " + sharedText);

        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {

            //check if user is logged in
            if (coMeth.isLoggedIn()) {
                // Update image Uri
                postImageUri = imageUri;
                Log.d(TAG, "handleSendImage: image uri is " + imageUri);
                //open the image cropper activity and pass in the uri
                CropImage.activity(postImageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        /*.setAspectRatio(16, 9)*/
                        .setMinCropResultSize(512, 512)
                        .start(CreatePostActivity.this);

            } else {

                //user is not logged in
                showLoginAlertDialog(getString(R.string.login_to_create_post));

            }

        }
    }


    private void populateEditPostData(String postId) {

        showProgress(getString(R.string.loading_text));
        //access db to set items
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                //check if result is successful
                if (task.isSuccessful() && task.getResult().exists()) {

                    //set items
                    Posts post = task.getResult().toObject(Posts.class);
                    //non null
                    //set title
                    String title = post.getTitle();
                    postTitleEditText.setText(title);
                    //set desc
                    String desc = task.getResult().get("desc").toString();
                    postDescTextView.setText(desc);

                    //nullable
                    //set image
                    if (post.getImage_url() != null) {
                        String imageUrl = task.getResult().get("image_url").toString();
                        String thumbUrl = task.getResult().get("thumb_url").toString();
                        try {
                            coMeth.setImage(R.drawable.appiconshadow, imageUrl, thumbUrl, createPostImageView);
                        } catch (Exception e) {
                            Log.d(TAG, "onComplete: failed to set create image");
                        }
                        //update the imageUrl
                        downloadUri = imageUrl;
                        downloadThumbUri = thumbUrl;
                    }

                    //set categories
                    if (post.getCategories() != null) {

                        ArrayList catsArray = (ArrayList) task.getResult().get("categories");
                        Log.d(TAG, "onComplete: \n catsArray on edit is: " + catsArray);
                        String catsString = "";
                        for (int i = 0; i < catsArray.size(); i++) {
                            catsString = catsString.concat(
                                    coMeth.getCatValue(catsArray.get(i).toString()) + "\n");
                        }

                        //set cat string
                        catsTextView.setText(catsString.trim());
                        Log.d(TAG, "onComplete: \n catString is: " + catsString);

                    }

                    //set contact details
                    if (post.getContact_details() != null) {

                        ArrayList contactArray = (ArrayList) task.getResult().get("contact_details");
                        String contactString = "";
                        for (int i = 0; i < contactArray.size(); i++) {

                            contactString = contactString.concat(contactArray.get(i).toString() + "\n");

                        }

                        contactTextView.setText(contactString.trim());

                    }

                    //set location
                    if (post.getLocation() != null) {

                        ArrayList locationArray = (ArrayList) task.getResult().get("location");
                        String locationString = "";

                        for (int i = 0; i < locationArray.size(); i++) {

                            locationString = locationString.concat(locationArray.get(i).toString() + "\n");

                        }

                        locationTextView.setText(locationString.trim());

                    }

                    //set event date
                    if (post.getEvent_date() != null) {
                        long eventDate = post.getEvent_date().getTime();
                        Log.d(TAG, String.valueOf(eventDate));
                        String eventDateString = DateFormat.format("EEE, MMM d, 20yy", new Date(eventDate)).toString();
                        Log.d(TAG, "onEvent: \nebentDateString: " + eventDateString);
                        eventDateTextView.setText(eventDateString);
                    }
                    //set price
                    if (post.getPrice() != null) {

                        String price = task.getResult().get("price").toString();
                        priceTextView.setText(price);

                    }
                    coMeth.stopLoading(progressDialog, null);

                } else {

                    //post does not exist
                    Log.d(TAG, "onComplete: post does not exist" + task.getException());
                    //go to main with error message
                    Intent postNotFountIntent = new Intent(CreatePostActivity.this, MainActivity.class);
                    postNotFountIntent.putExtra("action", "notify");
                    postNotFountIntent.putExtra("message", getString(R.string.post_not_found_text));
                    startActivity(postNotFountIntent);
                    finish();

                }

            }
        });

    }


    //when the image is selected by user
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //for selecting post image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //image is selected
                //get image uri
                postImageUri = result.getUri();
                createPostImageView.setImageURI(postImageUri);
                //process image tags
                processMLImage(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //handle errors
                Exception error = result.getError();
            }
        }

        //for google places (location)
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(CreatePostActivity.this, data);
//                String palaceId =  place.getId();
                //set the edit text to the location text
                String placeText = place.getName() + "\n" + place.getAddress();
                locationTextView.setText(placeText);
                //save the location
                postPlace = place;


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error when the creating post process is incomplete. the on desctroy error
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    /**
     * extract data from
     *
     * @param postImageUri image Uri
     */
    private void processMLImage(Uri postImageUri) {
        Log.d(TAG, "processMLImage: ");
        FirebaseVisionImage image = null; //initiate firebase vision image
        try {
            image = FirebaseVisionImage.fromFilePath(CreatePostActivity.this, postImageUri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "processMLImage: e" + e.getMessage());
        }

        //initiate text detector
        FirebaseVisionTextDetector textDetector = FirebaseVision.getInstance()
                .getVisionTextDetector();
        if (image != null) {
            Task<FirebaseVisionText> result =
                    textDetector.detectInImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    // Task completed successfully
                                    for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
                                        Rect boundingBox = block.getBoundingBox();
                                        Point[] cornerPoints = block.getCornerPoints();
                                        String text = block.getText();
                                        /*String newstr = "Word#$#$% Word λ1234ä, ñ, ж".replaceAll("\\P{L}+", "");*/
                                        String cleanText = text.replaceAll("\\P{L}+", " ");
                                        imageText = imageText.concat(cleanText);
                                        Log.d(TAG, "onSuccess: \n text is: " + text +
                                                "\nclean text is: " + cleanText);
                                        // TODO: 5/24/18 continue text handling
                                        /*for (FirebaseVisionText.Line line: block.getLines()) {
                                            // ...
                                            for (FirebaseVisionText.Element element: line.getElements()) {
                                                // ...
                                            }*/
                                    }

                                }
                            }).addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    Log.d(TAG, "onFailure: e: " + e.getMessage());
                                }
                            });
        }

        //initiate image labeling
        FirebaseVisionLabelDetector labelDetector = FirebaseVision.getInstance()
                .getVisionLabelDetector();
        FirebaseVisionLabelDetectorOptions options =
                new FirebaseVisionLabelDetectorOptions.Builder()
                        .setConfidenceThreshold(0.8f)
                        .build();
        if (image != null) {

            Task<List<FirebaseVisionLabel>> result =
                    labelDetector.detectInImage(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<FirebaseVisionLabel>>() {
                                        @Override
                                        public void onSuccess(List<FirebaseVisionLabel> labels) {
                                            // Task completed successfully
                                            for (FirebaseVisionLabel label : labels) {
                                                String labelText = label.getLabel();
                                                imageLabels = imageLabels.concat(labelText + " ");
                                                String entityId = label.getEntityId();
                                                float confidence = label.getConfidence();
                                                Log.d(TAG, "onSuccess: \nlabel is: " + labelText);
                                            }

                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                            Log.d(TAG, "onFailure: " +
                                                    "\nfailed to get labels off image: "
                                                    + e.getMessage());
                                        }
                                    });

        }
    }


    /*private void goToMain() {

        //go to main feed
        startActivity(new Intent(CreatePostActivity.this, MainActivity.class));

    }*/

    private void showProgress(String message) {

        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(CreatePostActivity.this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();

    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.createPostActivityLayout),
                message, Snackbar.LENGTH_LONG).show();
    }

    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(CreatePostActivity.this);
        loginAlertBuilder.setTitle("Login")
                .setIcon(getResources().getDrawable(R.drawable.ic_action_red_alert))
                .setMessage("You are not logged in\n" + message)
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send user to login activity
                        goToLogin();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        dialog.cancel();
                    }
                })
                .show();
    }
    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    //submit post in background
    public class SubmitPostTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            submitPost();
            return null;
        }
    }
}