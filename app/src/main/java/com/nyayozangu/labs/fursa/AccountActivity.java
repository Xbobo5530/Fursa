package com.nyayozangu.labs.fursa;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
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

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    Bitmap compressedImageFile;
    private CircleImageView setupImage;
    private Uri userImageUri = null;
    private Uri userThumbUri = null;
    private EditText userNameField;
    private EditText userBioField;
    private Button saveButton;
    private FloatingActionButton editImageFab;
    //user
    private String userId;

    //uploading image to Firebase
    private StorageReference mStorageRef;
    private FirebaseAuth mAUth;

    //Firebase database
    private FirebaseFirestore db;

    private boolean imageIsChanged = false;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        //initiating the Firebase reference
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAUth = FirebaseAuth.getInstance();

        //initiate elements

        setupImage = findViewById(R.id.setupImageCircleImageView);
        userNameField = findViewById(R.id.accNameEditText);
        saveButton = findViewById(R.id.accSaveButton);
        editImageFab = findViewById(R.id.accEditFab);
        userBioField = findViewById(R.id.accSettingAboutEditText);

        //user
        userId = mAUth.getCurrentUser().getUid();

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        //show progress
        showProgress("Loading...");

        //disable save button
        saveButton.setEnabled(false);

        //retrieve data if any
        db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    //check if data exists
                    if (task.getResult().exists()) {
                        //data exists
                        Log.d(TAG, "data exists");

                        //retrieve data
                        String name = task.getResult().getString("name");
                        String bio = task.getResult().getString("bio");
                        String image = task.getResult().getString("image");

                        //set the data
                        userNameField.setText(name);
                        userBioField.setText(bio);

                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.ic_thumb_person);

                        //loading the string for url to the image view
                        Glide.with(getApplicationContext()).setDefaultRequestOptions(placeHolderRequest).load(image).into(setupImage);

                        //update the imageUri
                        userImageUri = Uri.parse(image);

                    } else {
                        Log.d(TAG, "data does not exist");
                    }

                } else {
                    //retrieve data from db unsuccessful
                    String errorMessage = task.getException().getMessage();
                    Snackbar.make(findViewById(R.id.account_layout),
                            "Data retrieve error : " + errorMessage, Snackbar.LENGTH_SHORT).show();
                }
                //hide progress
                progressDialog.dismiss();
                saveButton.setEnabled(true);
            }
        });


        editImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check os version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //user is running marshmallow or greater
                    //check user permission
                    if (ContextCompat.checkSelfPermission(AccountActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //permission not yet granted
                        //ask for permission
                        Snackbar.make(findViewById(R.id.account_layout),
                                "Permission denied", Snackbar.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(AccountActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);


                    } else {
                        //permission already granted
                        pickImage();

                    }
                } else {
                    //os version below M, permissions are handled at installation
                    pickImage();
                }

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get user details
                final String userName = userNameField.getText().toString();
                final String userBio = userBioField.getText().toString();

                //check if userNameField is empty
                if (!TextUtils.isEmpty(userName) && userImageUri != null) {
                    Log.d(TAG, "userName is: " + userName +
                            "\nuserBio is: " + userBio +
                            "\nimageUri is: " + userImageUri.toString());

                    //generate randomString name for image based on firebase time stamp
                    final String randomName = UUID.randomUUID().toString();

                    //check if data (image) has changed
                    if (imageIsChanged) {

                        //upload the image to firebase
                        userId = mAUth.getCurrentUser().getUid();
                        StorageReference imagePath = mStorageRef.child("profile_images").child(userId + ".jpg");

                        Log.d(TAG, "userId is: " + userId + "imagePath is: " + imagePath);

                        //show progress bar
                        showProgress("Loading...");
                        //start handling data with firebase
                        imagePath.putFile(userImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                                //show progress bar after starting
                                Log.d(TAG, "at onComplete");

                                //check if is complete.
                                if (task.isSuccessful()) {
                                    /*//update the database
                                    updateDb(task, null,userName, userBio);*/

                                    File newImageFile = new File(userImageUri.getPath());

                                    try {
                                        compressedImageFile = new Compressor(AccountActivity.this)
                                                .setMaxWidth(100)
                                                .setMaxHeight(100)
                                                .setQuality(2)
                                                .compressToBitmap(newImageFile);
                                    } catch (IOException e) {

                                        e.printStackTrace();

                                    }

                                    //handle Bitmap
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] thumbData = baos.toByteArray();

                                    //uploading the thumbnail
                                    UploadTask uploadTask = mStorageRef.child("profile_images/thumbs")
                                            .child(randomName + ".jpg")
                                            .putBytes(thumbData);
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            //update dB with new thumb in task snapshot
                                            updateDb(task, taskSnapshot, userName, userBio);

                                        }


                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //upload failed
                                            String errorMessage = task.getException().getMessage();
                                            Log.d(TAG, "Db Update failed: " + errorMessage);

                                            Snackbar.make(findViewById(R.id.account_layout),
                                                    "Failed to upload image: " + errorMessage, Snackbar.LENGTH_SHORT).show();

                                            //hide progress bar
                                            progressDialog.dismiss();

                                        }
                                    });



                                } else {
                                    //upload failed
                                    Log.d(TAG, "upload failed");
                                    String errorMessage = task.getException().getMessage();

                                    Snackbar.make(findViewById(R.id.account_layout),
                                            "Upload failed: " + errorMessage, Snackbar.LENGTH_LONG).show();

                                }

                                progressDialog.dismiss();

                            }
                        });


                    } else {
                        //no image selected, no username selected
                        //update username but not the imageUri
                        updateDb(null, null, userName, userBio);
                    }
                } else {
                    //field are empty
                    // TODO: 4/9/18 make the user image optional
                    Snackbar.make(findViewById(R.id.account_layout),
                            "Enter a username and an image to continue...", Snackbar.LENGTH_LONG).show();

                }
            }
        });
    }

    private void updateDb(@NonNull Task<UploadTask.TaskSnapshot> task, UploadTask.TaskSnapshot uploadTaskSnapshot, String userName, String userBio) {
        //upload successful
        Log.d(TAG, "upload successful");

        //declare downloadUri
        Uri downloadUri;
        Uri downloadThumbUri;


        // TODO: 4/13/18 check crash bug during login without changin anything
        //check if the task is null
        if (uploadTaskSnapshot != null) {
            //new image uri
            downloadUri = task.getResult().getDownloadUrl();
            downloadThumbUri = uploadTaskSnapshot.getDownloadUrl();
        } else {
            //image uri has not changed
            downloadUri = userImageUri;
            downloadThumbUri = userThumbUri;
        }

        //create map for users
        try {
            Map<String, String> usersMap = new HashMap<>();
            usersMap.put("name", userName);
            usersMap.put("bio", userBio);
            usersMap.put("image", downloadUri.toString());
            usersMap.put("thumb", downloadThumbUri.toString());


            //store data to db
            db.collection("Users").document(userId).set(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        //task successful
                        //go to main activity, go to feed
                        Log.d(TAG, "Database update successful");
                        goToMain();

                    } else {
                        //task failed
                        String errorMessage = task.getException().getMessage();
                        Snackbar.make(findViewById(R.id.account_layout),
                                "Database error: " + errorMessage, Snackbar.LENGTH_SHORT).show();

                    }
                    //hide progress  after finishing
                    progressDialog.dismiss();
                }
            });

        } catch (NullPointerException dbUpdateNull) {

            Log.e(TAG, "updateDb: ", dbUpdateNull);
            finish();

        }
    }

    private void goToMain() {
        startActivity(new Intent(AccountActivity.this, MainActivity.class));
        finish();
    }

    private void pickImage() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(AccountActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                //store the cropped image uri
                userImageUri = result.getUri();
                //set the setupImage uri to cropped image
                setupImage.setImageURI(userImageUri);
                //change the imageIsChanged value
                imageIsChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //handle errors
                Exception error = result.getError();
            }
        }
    }


    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(AccountActivity.this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }
}
