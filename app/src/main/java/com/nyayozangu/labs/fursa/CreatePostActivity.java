package com.nyayozangu.labs.fursa;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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
    private final String[] categories = new String[]{

            "Business",
            "Events",
            "Buying and selling",
            "Education",
            "Jobs",
            "Places",
            "Queries"

    };

    private final String[] catKeys = new String[]{

            "business",
            "events",
            "buysell",
            "education",
            "jobs",
            "places",
            "queries"

    };

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    //for file compression
    Bitmap compressedImageFile;
    private android.support.v7.widget.Toolbar toolbar;
    private ImageView createPostImageView;
    private FloatingActionButton editImageFAB;
    private Button submitButton;
    private ProgressDialog progressDialog;
    private Uri postImageUri;
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
    private View alertView;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;
    //user
    private String currentUserId;
    private Date eventDate;
    private String price;
    private ArrayList<Integer> mSelectedCats;
    private ArrayList<String> catsStringsArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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


        //initiate firebase
        mAuth = FirebaseAuth.getInstance();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
        //initiate storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

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

        eventDateField = findViewById(R.id.createEventDateDescLayout);
        eventDateTextView = findViewById(R.id.createPostEventDateTextView);

        contactField = findViewById(R.id.createPostContactLayout);
        contactTextView = findViewById(R.id.createPostContactTextView);

        priceField = findViewById(R.id.createPostPriceLayout);
        priceTextView = findViewById(R.id.createPostPriceTextView);


        descField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //crate a dialog tha twill have a
                AlertDialog.Builder builder = new AlertDialog.Builder(CreatePostActivity.this);
                builder.setTitle("Post Description")
                        .setIcon(R.drawable.ic_action_descritption);

                final EditText input = new EditText(CreatePostActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                builder.setView(input);
                if (!postDescTextView.getText().toString().isEmpty()) {
                    input.setText(postDescTextView.getText().toString());
                }
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        desc = input.getText().toString().trim();
                        postDescTextView.setText(desc);
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


        //open a dialog for contact details
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        alertView = inflater.inflate(R.layout.contact_alert_dialog_content_layout, null);

        contactField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder contactDialogBuilder = new AlertDialog.Builder(CreatePostActivity.this);
                contactDialogBuilder.setTitle("Contact Details")
                        .setIcon(R.drawable.ic_action_contact)
                        .setView(alertView)
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //user clicks done
                                EditText contactNameField = alertView.findViewById(R.id.contactNameDialogEditText);
                                EditText contactPhoneField = alertView.findViewById(R.id.contactPhoneDialogEditText);
                                EditText contactEmailField = alertView.findViewById(R.id.contactEmailDialogEditText);

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

                                Log.d(TAG, "at positive button clicked: \n contact name: " + contactName +
                                        "\ncontact phone: " + contactPhone + "\ncontact email: " + contactEmail);
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
                if (alertView.getParent() != null) {
                    ((ViewGroup) alertView.getParent()).removeView(alertView);
                }
                contactDialogBuilder.show();


            }
        });


        //set an on click listener for the categories field
        categoriesField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //alert dialog builder
                AlertDialog.Builder catPickerBuilder = new AlertDialog.Builder(CreatePostActivity.this);
                catPickerBuilder.setTitle("Categories")
                        .setIcon(getDrawable(R.drawable.ic_action_cat_light))
                        .setMultiChoiceItems(categories, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                                //when the alert dialog is opened first populate it with items from the mselected list(itema that are already selected)
                                //empty all fields

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
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //when Done is clicked
                                //show the selected cats on the cats text view

                                String catsString = "";

                                for (int i = 0; i < mSelectedCats.size(); i++) {


                                    //check if is last item
                                    if (i == mSelectedCats.size() - 1) {

                                        Log.d(TAG, "onClick: \nat if for loop, which is: " + which);
                                        //this is the last item
                                        //concat a string without comma
                                        catsString = catsString.concat(categories[mSelectedCats.get(i)]);

                                    } else {

                                        //concat a string
                                        catsString = catsString.concat(categories[mSelectedCats.get(i)] + ", ");

                                    }

                                    //update cats string array
                                    if (!catsStringsArray.contains(categories[mSelectedCats.get(i)])) {

                                        //only add items to the array if they are not already there
                                        catsStringsArray.add(catKeys[mSelectedCats.get(i)]);
                                        Log.d(TAG, "onClick: \n catsStringArray is: " + catsStringsArray);

                                    }

                                }

                                catsTextView.setText(catsString.trim());
                            }
                        })

                        //set negative buttton
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //dismiss the dialog
                                dialog.dismiss();

                            }
                        });

                //clear itemts before showing
                mSelectedCats.clear();
                catsStringsArray.clear();
                catsTextView.setText("");
                catsTextView.setHint("Select categories for your post");

                //show the dialog
                catPickerBuilder.show();


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
                            .setIcon(getDrawable(R.drawable.ic_action_alert))
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

                final DatePickerDialog eventDatePickerDialog = new DatePickerDialog(CreatePostActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        eventDate = new Date(year, month, dayOfMonth);
                        Log.d(TAG, "date selected is: " + eventDate.toString());
                        //set selected date to the eventDate textView
                        eventDateTextView.setText(android.text.format.DateFormat.format("EEE, MMM d, yyyy - h:mm a", eventDate).toString());

                    }
                }, YEAR, MONTH, DAY);
                eventDatePickerDialog.show();

                // save date to eventDate
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
                if (isConnected()) {

                    //start submitting
                    //get desc
                    desc = postDescTextView.getText().toString().trim();
                    //get title
                    title = postTitleEditText.getText().toString().trim();


                    //check if description field is empty
                    // TODO: 4/15/18 make it so that posts dont need to have images to be posted, use default image for posts
                    if (!TextUtils.isEmpty(desc) && postImageUri != null && !TextUtils.isEmpty(title)) {
                        //description is not empty and image is not null
                        showProgress("Posting...");

                        //generate randomString name for image based on firebase time stamp
                        final String randomName = UUID.randomUUID().toString();

                        //define path to upload image
                        StorageReference filePath = mStorageRef.child("post_images").child(randomName + ".jpg");

                        //upload the image
                        filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                                //get download url
                                final String downloadUri = task.getResult().getDownloadUrl().toString();

                                //handle results after attempting to upload
                                if (task.isSuccessful()) {
                                    //upload complete
                                    Log.d(TAG, "upload successful");

                                    File newImageFile = new File(postImageUri.getPath());

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
                                    UploadTask uploadTask = mStorageRef.child("post_images/thumbs")
                                            .child(randomName + ".jpg")
                                            .putBytes(thumbData);

                                    //on success listener
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            //get downloadUri for thumbnail
                                            String downloadThumbUri = taskSnapshot.getDownloadUrl().toString();
                                            //on success

                                            //store the user info associated with post
                                            Map<String, Object> postMap = new HashMap<>();
                                            postMap.put("image_url", downloadUri);
                                            postMap.put("thumb_url", downloadThumbUri);
                                            postMap.put("title", title);
                                            postMap.put("desc", desc);
                                            postMap.put("user_id", currentUserId);
                                            try {

                                                postMap.put("timestamp", FieldValue.serverTimestamp());
                                                postMap.put("location_name", postPlace.getName());
                                                postMap.put("location_address", postPlace.getAddress());
                                                postMap.put("location_event_date", eventDate);
                                                postMap.put("contact_name", contactName);
                                                postMap.put("contact_phone", contactPhone);
                                                postMap.put("contact_email", contactEmail);
                                                postMap.put("price", price);
                                                postMap.put("categories", catsStringsArray);
                                                Log.d(TAG, "onSuccess: uploading, catStrinArraty is: " + catsStringsArray);

                                            } catch (NullPointerException e) {
                                                Log.d(TAG, "Error: " + e.getMessage());
                                            }

                                            //upload
                                            db.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    //check the result
                                                    if (task.isSuccessful()) {
                                                        //db update successful
                                                        Log.d(TAG, "Db Update successful");
                                                        //go back to main feed
                                                        startActivity(new Intent(CreatePostActivity.this, MainActivity.class));
                                                        finish();

                                                    } else {
                                                        //upload failed
                                                        String errorMessage = task.getException().getMessage();
                                                        Log.d(TAG, "Db Update failed: " + errorMessage);

                                                        Snackbar.make(findViewById(R.id.createPostActivityLayout),
                                                                "Failed to upload image: " + errorMessage, Snackbar.LENGTH_SHORT).show();

                                                    /*//hide progress bar
                                                    newPostProgressBar.setVisibility(View.INVISIBLE);*/
                                                        progressDialog.dismiss();
                                                    }

                                                }
                                            });

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //on failure
                                            //upload failed
                                            String errorMessage = task.getException().getMessage();
                                            Log.d(TAG, "Db Update failed: " + errorMessage);

                                            showSnack(R.id.createPostActivityLayout, "Failed to upload image: " + errorMessage);
                                                    /*//hide progress bar
                                                    newPostProgressBar.setVisibility(View.INVISIBLE);*/
                                            progressDialog.dismiss();
                                        }
                                    });


                                } else {
                                    //post failed
                                    String errorMessage = task.getException().getMessage();

                                    Log.w(TAG, "signInWithCredential:failure", task.getException());

                                    showSnack(R.id.createPostActivityLayout, "Failed to upload image: " + errorMessage);

                                /*//hide progress bar
                                newPostProgressBar.setVisibility(View.INVISIBLE);*/
                                    progressDialog.dismiss();
                                }
                            }
                        });


                    } else {
                        //desc is empty
                        //upload failed
                        Log.d(TAG, "Fields are empty");

                        showSnack(R.id.createPostActivityLayout, "Enter your post details to proceed");

                    }
                } else {

                    //notify user is not connected and cant post
                    showSnack(R.id.createPostActivityLayout, "You are not connected to the internet\nCheck your connection and try again");

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

                Log.i(TAG, "PlaceName: " + place.getName() +
                        "\nAddress is: " + place.getAddress() +
                        "\ntoString: " + place.toString() +
                        "\nplaceId: " + place.getId() +
                        "\ngetLocale: " + place.getLocale() +
                        "\ngetLatLng: " + place.getLatLng());

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
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();

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

    private void showSnack(int id, String message) {
        Snackbar.make(findViewById(id),
                message, Snackbar.LENGTH_LONG).show();
    }

}
