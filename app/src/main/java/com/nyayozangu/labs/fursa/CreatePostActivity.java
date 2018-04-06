package com.nyayozangu.labs.fursa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

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
    //for file compression
    Bitmap compressedImageFile;
    private ImageView createPostImageView;
    private ImageButton closeImageButton;
    private FloatingActionButton editImageFAB;
    private Button submitButton;
    private ProgressDialog progressDialog;
    private Uri postImageUri;
    private EditText postTitleField;
    private EditText postDescField;
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

        createPostImageView = findViewById(R.id.createPostImageView);
        closeImageButton = findViewById(R.id.closeCreatePostImageButton);
        submitButton = findViewById(R.id.submitNavButton);
        editImageFAB = findViewById(R.id.editImageFAB);
        postTitleField = findViewById(R.id.postTitileEditText);
        postDescField = findViewById(R.id.postDescriptionEditText);


        //initiate firebase
        mAuth = FirebaseAuth.getInstance();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
        //initiate storage refference
        mStorageRef = FirebaseStorage.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();


        //on submit

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //start submitting
                //get desc
                final String desc = postDescField.getText().toString().trim();
                //get title
                final String title = postTitleField.getText().toString().trim();

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
