package com.nyayozangu.labs.fursa.activities.posts;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class CreatePostActivity extends AppCompatActivity {

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
    private TextView contactTextView;
    private TextView eventDateTextView;
    private TextView priceTextView;
    private TextView catsTextView;
    private String desc;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "at CreatePostActivity, onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        toolbar = findViewById(R.id.createPostToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Post");
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

        if (coMeth.isLoggedIn()) {

            currentUserId = coMeth.getUid();

        } else {

            //user is not logged in
            Intent homeIntent = new Intent(CreatePostActivity.this, MainActivity.class);
            homeIntent.putExtra("action", "notify");
            homeIntent.putExtra("message", "You are not logged in");
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
                // TODO: 5/4/18 use a cutom view to properly align the desc text
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
                final AlertDialog.Builder contactDialogBuilder = new AlertDialog.Builder(CreatePostActivity.this);
                contactDialogBuilder.setTitle("Contact Details")
                        .setIcon(R.drawable.ic_action_contact)
                        .setView(contactDialogView)
                        .setPositiveButton(getString(R.string.done_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //user clicks done
                                EditText contactNameField = contactDialogView.findViewById(R.id.contactNameDialogEditText);
                                EditText contactPhoneField = contactDialogView.findViewById(R.id.contactPhoneDialogEditText);
                                EditText contactEmailField = contactDialogView.findViewById(R.id.contactEmailDialogEditText);

                                //get values
                                contactName = contactNameField.getText().toString().trim();
                                contactPhone = contactPhoneField.getText().toString().trim();
                                contactEmail = contactEmailField.getText().toString().trim();

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
                                                String contactDetails = contactName + "\n" + contactPhone + "\n" + contactEmail;
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
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
                AlertDialog.Builder catPickerBuilder = new AlertDialog.Builder(CreatePostActivity.this);
                catPickerBuilder.setTitle(getString(R.string.categories_text))
                        .setIcon(getDrawable(R.drawable.ic_action_cat_light))
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

                        //set actions buttons
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

                                // TODO: 5/3/18 chcek on createing pst selecting cats does not
                                catsTextView.setText(catsString.trim());
                            }
                        })

                        //set negative buttton
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

                // TODO: 5/3/18 test clearing items after showing dialog

            }
        });


        //for location
        locationField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open the pick location activity
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(CreatePostActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {

                    Log.e(TAG, "onClick: " + e.getMessage());
                    AlertDialog.Builder locationErrorBuilder = new AlertDialog.Builder(CreatePostActivity.this);
                    locationErrorBuilder.setTitle("Error")
                            .setIcon(getDrawable(R.drawable.ic_action_red_alert))
                            .setMessage("Failed to load locations at this moment\n Please try again later")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
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

                final DatePickerDialog eventDatePickerDialog = new DatePickerDialog(CreatePostActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        // save date to eventDate
                        eventDate = new Date(year, month, dayOfMonth);
                        // TODO: 5/2/18 check setting event date on edit post fails
                        Log.d(TAG, "date selected is: " + eventDate.toString());
                        //set selected date to the eventDate textView
                        eventDateTextView.setText(android.text.format.DateFormat.format("EEE, MMM d, 20yy\nh:mm a", eventDate).toString());


                    }
                }, YEAR, MONTH, DAY);
                eventDatePickerDialog.show();


            }
        });


        //set the price
        priceField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreatePostActivity.this);
                builder.setTitle("Post Description")
                        .setIcon(R.drawable.ic_action_price);

                final EditText input = new EditText(CreatePostActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                builder.setView(input);
                if (!priceTextView.getText().toString().isEmpty()) {
                    input.setText(priceTextView.getText().toString());
                }
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        price = input.getText().toString().trim();
                        priceTextView.setText(price);
                    }
                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
                        //description is not empty and image is not null

                        //check if is new post or edit post
                        if (!isEditPost()) {

                            //is new post
                            Log.d(TAG, "onClick: is new post");
                            submitPost();

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
                                                    /*if (post.getTitle() != null) title = post.getTitle();
                                                    if (post.getDesc() != null) desc = post.getDesc();*/
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

                                                    submitPost();

                                                } else {

                                                    //post no longer exists
                                                    Log.d(TAG, "onComplete: post does not exist");
                                                    coMeth.stopLoading(progressDialog, null);
                                                    //go to main
                                                    Intent postNotFoundIntent = new Intent(CreatePostActivity.this, MainActivity.class);
                                                    postNotFoundIntent.putExtra("action", "notify");
                                                    postNotFoundIntent.putExtra("message", getString(R.string.post_not_found_text));
                                                    startActivity(postNotFoundIntent);
                                                    finish();

                                                }

                                            } else {

                                                //task failed
                                                Log.d(TAG, "onComplete: task failed" + task.getException());
                                                showSnack(getString(R.string.failed_to_complete_text));

                                            }

                                        }
                                    });
                        }

                        //hide loading
                        coMeth.stopLoading(progressDialog, null);
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


    }

    private void submitPost() {

        //check if post has image
        Log.d(TAG, "submitPost: at submit post");
        if (postImageUri != null) {

            //generate randomString name for image based on firebase time stamp
            final String randomName = UUID.randomUUID().toString();
            //define path to upload image
            StorageReference filePath = coMeth.getStorageRef().child("post_images").child(randomName + ".jpg");
            //upload the image
            filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                    //get download url
                    downloadUri = task.getResult().getDownloadUrl().toString();
                    //handle results after attempting to upload
                    if (task.isSuccessful()) {

                        //upload complete
                        Log.d(TAG, "upload successful");
                        File newImageFile = new File(postImageUri.getPath());
                        Log.d(TAG, "onComplete: newImageFile is" + newImageFile);

                        try {
                            compressedImageFile = new Compressor(CreatePostActivity.this)
                                    .setMaxWidth(100)
                                    .setMaxHeight(100)
                                    .setQuality(5)
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
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                                    coMeth.getDb()
                                            .collection("Posts")
                                            .add(postMap)
                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {

                                                    //check the result
                                                    if (task.isSuccessful()) {

                                                        //db update successful
                                                        Log.d(TAG, "Db Update successful");
                                                        goToMain();
                                                        //notify users subscribed to cats
                                                        notifyNewPostCatsUpdates(catsStringsArray);
                                                        Log.d(TAG, "onComplete: about to upload " +
                                                                "\ncategproes are: " + catsStringsArray);


                                                    } else {

                                                        //upload failed
                                                        coMeth.stopLoading(progressDialog, null);
                                                        String errorMessage = task.getException().getMessage();
                                                        Log.d(TAG, "Db Update failed: " + errorMessage);
                                                        showSnack(getString(R.string.failed_to_upload_image_text));

                                                    }
                                                    coMeth.stopLoading(progressDialog, null);

                                                }
                                            });

                                } else {

                                    //is edit post
                                    coMeth.getDb()
                                            .collection("Posts")
                                            .document(postId)
                                            .set(postMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    //check the result
                                                    if (task.isSuccessful()) {

                                                        //db update successful
                                                        Log.d(TAG, "Db Update successful");
                                                        goToMain();
                                                        //notify users subscribed to cats
                                                        notifyNewPostCatsUpdates(catsStringsArray);
                                                        Log.d(TAG, "onComplete: about to upload \ncategproes are: " + catsStringsArray);

                                                    } else {

                                                        //upload failed
                                                        Log.d(TAG, "Db Update failed: " + task.getException());
                                                        showSnack(getString(R.string.failed_to_upload_image_text));

                                                    }
                                                    coMeth.stopLoading(progressDialog, null);

                                                }
                                            });

                                }


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //upload failed
                                Log.d(TAG, "Db Update failed: " + task.getException());
                                showSnack(getString(R.string.failed_to_upload_image_text));
                                coMeth.stopLoading(progressDialog, null);
                            }
                        });


                    } else {

                        //post failed
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        showSnack(getString(R.string.failed_to_upload_image_text));
                    }

                    coMeth.stopLoading(progressDialog, null);

                }
            });

        } else {


            //post has no image
            //get map
            Map<String, Object> postMap = handleMap(downloadThumbUri, downloadUri);

            if (!isEditPost()) {
                coMeth.getDb()
                        .collection("Posts")
                        .add(postMap)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                //check if postig is successful
                                if (task.isSuccessful()) {

                                    goToMain();
                                    //notify users subscribed to cats
                                    notifyNewPostCatsUpdates(catsStringsArray);
                                    Log.d(TAG, "onComplete: posted post without image");

                                } else {

                                    //posting failed
                                    Log.d(TAG, "onComplete: " + task.getException());
                                    showSnack(getString(R.string.failed_to_post_text));

                                }

                            }
                        });
            } else {

                //for edit post
                coMeth.getDb()
                        .collection("Posts")
                        .document(postId)
                        .set(postMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                //check if posting is successful
                                if (task.isSuccessful()) {

                                    goToMain();
                                    //notify users subscribed to cats
                                    notifyNewPostCatsUpdates(catsStringsArray);
                                    Log.d(TAG, "onComplete: posted post without image");

                                } else {

                                    //posting failed
                                    String errorMessage = task.getException().getMessage();
                                    showSnack(getString(R.string.failed_to_post_text));

                                }

                            }
                        });

            }

        }
    }

    private void notifyNewPostCatsUpdates(ArrayList<String> catsStringsArray) {

        //send notifications to all users subscribed to cats in catStringArray
        for (int i = 0; i < catsStringsArray.size(); i++) {

            String notifType = "categories_updates";
            new Notify().execute(notifType, catsStringsArray.get(i));

        }

    }

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
        postMap.put("title", title);
        postMap.put("desc", desc);
        postMap.put("user_id", currentUserId);

        //get the current time
        postMap.put("timestamp", FieldValue.serverTimestamp());
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

            //loc array has content
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

            Log.d(TAG, "handleMap: catsStringArray has content\n" + catsStringsArray);
            postMap.put("categories", catsStringsArray);

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

        showProgress("Loading...");
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
                    /*//update the  original timestamp
                    timestamp = (Date) task.getResult().get("timestamp");*/
                    //nullable
                    //set image
                    if (post.getImage_url() != null) {
                        String imageUrl = task.getResult().get("image_url").toString();
                        String thumbUrl = task.getResult().get("thumb_url").toString();


                        RequestOptions placeHolderOptions = new RequestOptions();
                        placeHolderOptions.placeholder(R.drawable.ic_action_image_placeholder);
                        Glide.with(getApplicationContext())
                                .applyDefaultRequestOptions(placeHolderOptions)
                                .load(imageUrl)
                                .thumbnail(Glide.with(getApplicationContext()).load(thumbUrl))
                                .into(createPostImageView);

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

                            catsString = catsString.concat(coMeth.getCatValue(catsArray.get(i).toString()) + "\n");

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
                        String eventDateString = DateFormat.format("EEE, MMM d, 20yy\nh:mm a", new Date(eventDate)).toString();
                        Log.d(TAG, "onEvent: \nebentDateString: " + eventDateString);
                        eventDateTextView.setText(eventDateString);
                    }

                    //set price
                    if (post.getPrice() != null) {

                        String price = task.getResult().get("price").toString();
                        priceTextView.setText(price);

                    }

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

                coMeth.stopLoading(progressDialog, null);

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


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //handle errors
                Exception error = result.getError();
            }
        }

        //for google places
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(CreatePostActivity.this, data);
                String postLocation = place.getName().toString();
                //set the edit text to the location text
                locationTextView.setText(postLocation + "\n" + place.getAddress());
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


    private void goToMain() {

        //go to main feed
        startActivity(new Intent(CreatePostActivity.this, MainActivity.class));
        finish();

    }

    private void showProgress(String message) {

        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(CreatePostActivity.this);
        progressDialog.setMessage(message);
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
                .setIcon(getDrawable(R.drawable.ic_action_red_alert))
                .setMessage("You are not logged in\n" + message)
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send user to login activity
                        coMeth.goToLogin();
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
}
