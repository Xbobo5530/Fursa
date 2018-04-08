package com.nyayozangu.labs.fursa;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class CreatePostActivity extends AppCompatActivity {

    private static final String TAG = "Sean";

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    //for file compression
    Bitmap compressedImageFile;
    private ImageView createPostImageView;
    private ImageView closeImageButton;
    private FloatingActionButton editImageFAB;
    private Button submitButton;
    private ProgressDialog progressDialog;
    private Uri postImageUri;
    private EditText postTitleField;
    private TextView postDescField;

    private String desc;
    private String title;

    private ConstraintLayout descField;
    private ConstraintLayout categoriesField;
    private ConstraintLayout locationField;
    private ConstraintLayout priceField;

    private TextView locationTextView;
    private Place postPlace = null;


    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;
    //user
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        //initiate firebase
        mAuth = FirebaseAuth.getInstance();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
        //initiate storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        createPostImageView = findViewById(R.id.createPostImageView);
        closeImageButton = findViewById(R.id.createPostCloseImageView);
        submitButton = findViewById(R.id.createPostSubmitButton);
        editImageFAB = findViewById(R.id.createPostEditImageFab);
        postTitleField = findViewById(R.id.createPostTileEditText);
        postDescField = findViewById(R.id.createPostDescTextView);

        descField = findViewById(R.id.createPostDescLayout);
        categoriesField = findViewById(R.id.createPostCategoriesLayout);

        locationField = findViewById(R.id.createPostLocationLayout);
        locationTextView = findViewById(R.id.createPostLocationTextView);


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
                if (!postDescField.getText().toString().isEmpty()) {
                    input.setText(postDescField.getText().toString());
                }
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        desc = input.getText().toString().trim();
                        postDescField.setText(desc);
                    }
                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                builder.show();
                Log.d(TAG, "dialog created");

            }
        });

        locationField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //open the pick location activity
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(CreatePostActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }

            }
        });




        //on submit

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //start submitting
                //get desc
                desc = postDescField.getText().toString().trim();
                //get title
                title = postTitleField.getText().toString().trim();


                //check if description field is empty
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
                                        postMap.put("timestamp", FieldValue.serverTimestamp());
                                        postMap.put("location_name", postPlace.getName());
                                        postMap.put("location_address", postPlace.getAddress());
                                        Log.d(TAG, "locationName" + postPlace.getName());

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
                                    }
                                });


                            } else {
                                //post failed
                                // TODO: 4/4/18 handle error uploading image
                                String errorMessage = task.getException().getMessage();

                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                                Snackbar.make(findViewById(R.id.createPostActivityLayout),
                                        "Failed to upload image: " + errorMessage, Snackbar.LENGTH_SHORT).show();

                                /*//hide progress bar
                                newPostProgressBar.setVisibility(View.INVISIBLE);*/
                                progressDialog.dismiss();
                            }
                        }
                    });


                } else {
                    //desc is empty

                }

            }
        });

        //handle close button
        closeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to main
                goToMain();

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
                // TODO: Handle the error.
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

}
