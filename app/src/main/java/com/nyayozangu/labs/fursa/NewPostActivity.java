package com.nyayozangu.labs.fursa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    private static final int MAX_LENGTH = 100;
    //compressing images
    Bitmap compressedImageFile;
    private Toolbar newPostToolBar;
    private ImageView newPostImage;
    private EditText newPostDescField;
    private Button submitNewPostButon;
    private ProgressBar newPostProgressBar;
    private Uri postImageUri;
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;
    //user
    private String currentUserId;

    /**
     * generates a random string
     *
     * @returns String a random String
     */


    public static String randomString() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        //initiate firebase
        mAuth = FirebaseAuth.getInstance();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
        //intiate storage refference
        mStorageRef = FirebaseStorage.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        //initiate elements
        newPostImage = findViewById(R.id.newPostImageView);
        newPostDescField = findViewById(R.id.newPostDescriptionEditText);
        submitNewPostButon = findViewById(R.id.submitNewPostButton);
        newPostProgressBar = findViewById(R.id.newPostProgressBar);

        newPostToolBar = findViewById(R.id.newPostToolBar);
        setSupportActionBar(newPostToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("New Post");

        //get user data from db


        //set onClickListener for imageView
        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show imagepcker and crop tool
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setMinCropResultSize(512, 512)
                        .start(NewPostActivity.this);
            }
        });


        //set onClickListener for submit button
        submitNewPostButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //start submitting
                final String desc = newPostDescField.getText().toString();

                //check if description field is empty
                if (!TextUtils.isEmpty(desc) && postImageUri != null) {
                    //description is not empty and image is not null
                    newPostProgressBar.setVisibility(View.VISIBLE);

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
                                    compressedImageFile = new Compressor(NewPostActivity.this)
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
                                                    startActivity(new Intent(NewPostActivity.this, MainActivity.class));
                                                    finish();

                                                } else {
                                                    //upload failed
                                                    String errorMessage = task.getException().getMessage();
                                                    Log.d(TAG, "Db Update failed: " + errorMessage);
                                                    //hide progress bar
                                                    newPostProgressBar.setVisibility(View.INVISIBLE);
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
                                Toast.makeText(NewPostActivity.this, "Failed to upload image: " + errorMessage, Toast.LENGTH_SHORT).show();

                                //hide progress bar
                                newPostProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });


                } else {
                    //desc is empty

                }

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //image is selected
                //get image uri
                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //handle errors
                Exception error = result.getError();
            }
        }
    }
}
