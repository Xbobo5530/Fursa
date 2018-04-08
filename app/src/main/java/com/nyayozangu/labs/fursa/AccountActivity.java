package com.nyayozangu.labs.fursa;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    private CircleImageView setupImage;

    private Uri mainImageUri = null;
    private EditText userNameField;
    private Button saveSettingsButton;
    private ProgressBar accProgressBar;

    //user
    private String userId;

    //uploading image to Firebase
    private StorageReference mStorageRef;
    private FirebaseAuth mAUth;

    //Firebase database
    private FirebaseFirestore db;

    private boolean imageIsChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        //initiating the Firebase reference
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAUth = FirebaseAuth.getInstance();

        //initiate elements
        android.support.v7.widget.Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        setupImage = findViewById(R.id.setupImageCircleImageView);
        userNameField = findViewById(R.id.accNameEditText);
        saveSettingsButton = findViewById(R.id.accSaveSettingsButton);
        accProgressBar = findViewById(R.id.accProgressBar);

        //user
        try {
            // TODO: 4/6/18 fix the app crash after sgning with google sign in at account setting page
            userId = mAUth.getCurrentUser().getUid();
        } catch (NullPointerException e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();


        //show progress bar while loading
        accProgressBar.setVisibility(View.VISIBLE);
        //disable save button
        saveSettingsButton.setEnabled(false);

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
                        String image = task.getResult().getString("image");

                        //set the data
                        userNameField.setText(name);

                        // TODO: 4/3/18 set image
                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.mipmap.ic_launcher);

                        //loading the string for url to the image view
                        Glide.with(AccountActivity.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(setupImage);

                        //update the imageUri
                        mainImageUri = Uri.parse(image);

                    } else {
                        Toast.makeText(AccountActivity.this, "Data does not exist: ", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "data does not exist");
                    }

                } else {
                    //retrieve data from db unsuccessful
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(AccountActivity.this, "Data retrieve error : " +
                            errorMessage, Toast.LENGTH_LONG).show();

                }
                //hide progress after loading
                accProgressBar.setVisibility(View.INVISIBLE);
                //enable save settings button
                saveSettingsButton.setEnabled(true);
            }
        });


        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check os version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //user is running marshmallow or greater
                    //check user permission
                    if (ContextCompat.checkSelfPermission(AccountActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //permission not yet granted
                        //ask for permission
                        Toast.makeText(AccountActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
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

        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get user details
                final String userName = userNameField.getText().toString();

                //check if userNameField is empty
                if (!TextUtils.isEmpty(userName) && mainImageUri != null) {
                    Log.d(TAG, "userName is: " + userName +
                            "\nimageUri is: " + mainImageUri.toString());


                    // TODO: 4/4/18 before uploading userImage, compress to get userImageThumb
                    //check if data (image) has changed
                    if (imageIsChanged) {
                        //upload the image to firebase
                        userId = mAUth.getCurrentUser().getUid();
                        StorageReference imagePath = mStorageRef.child("profile_images").child(userId + ".jpg");
                        Log.d(TAG, "userId is: " + userId + "imagePath is: " + imagePath);

                        //show progress bar
                        accProgressBar.setVisibility(View.VISIBLE);
                        //start handling data with firebase
                        imagePath.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                //show progress bar after starting
                                Log.d(TAG, "at onComplete");

                                //check if is complete.
                                if (task.isSuccessful()) {

                                    //update the database
                                    updateDb(task, userName);


                                } else {
                                    //upload failed
                                    Log.d(TAG, "upload failed");
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(AccountActivity.this, "Upload Failed: " +
                                            errorMessage, Toast.LENGTH_LONG).show();

                                }

                                //hide progress bar after finishing
                                accProgressBar.setVisibility(View.GONE);

                            }
                        });


                        // TODO: 4/3/18 when userImage is not present use default image
                    } else {
                        //no image selected, no username selected
                        //update username but not the imageUri
                        updateDb(null, userName);
                    }
                } else {
                    //image has not changed, no need to update the umage uri on db

                }
            }
        });
    }

    private void updateDb(@NonNull Task<UploadTask.TaskSnapshot> task, String userName) {
        //upload successful
        Log.d(TAG, "upload successful");

        //declare downladUri
        Uri downloadUri;

        //check if the task is null
        if (task != null) {
            //new image uri
            downloadUri = task.getResult().getDownloadUrl();
        } else {
            //image uri has not changed
            downloadUri = mainImageUri;
        }

        //create map for users
        Map<String, String> usersMap = new HashMap<>();
        usersMap.put("name", userName);
        usersMap.put("image", downloadUri.toString());

        //store data to db
        db.collection("Users").document(userId).set(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    //task successful
                    //go to main activity, go to feed
                    Toast.makeText(AccountActivity.this, "Database update successful", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(AccountActivity.this, MainActivity.class));
                    finish();

                } else {
                    //task failed
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(AccountActivity.this, "Database error: " +
                            errorMessage, Toast.LENGTH_LONG).show();

                }
                //hide progress bar after finishing
                accProgressBar.setVisibility(View.GONE);
            }
        });
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
                mainImageUri = result.getUri();
                //set the setupImage uri to cropped image
                setupImage.setImageURI(mainImageUri);
                //change the imageIsChanged value
                imageIsChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //handle errors
                Exception error = result.getError();
            }
        }
    }
}
