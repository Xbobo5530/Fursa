package com.nyayozangu.labs.fursa.activities.settings;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private EditText nameField, phoneField, emailField, descField;
    private Button submitButton;
    private Toolbar toolbar;
    private String name, phone, email, desc;
    private ArrayList<String> feedbackContactArray;
    private ProgressDialog progressDialog;

    @Override
    protected void onStart() {

        if (!coMeth.isLoggedIn()) {

            showLoginAlertDialog("Log in to submit a feedback");

        }
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        toolbar = findViewById(R.id.feedbackToolbar);
        nameField = findViewById(R.id.feedbackNameEditText);
        phoneField = findViewById(R.id.feedbackPhoneEditText);
        emailField = findViewById(R.id.feedbackEmailEditText);
        descField = findViewById(R.id.fedbackDescEditText);
        submitButton = findViewById(R.id.feedbackSubmitButton);

        feedbackContactArray = new ArrayList<String>();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.feedback_title_text);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (!coMeth.isLoggedIn()) {

            showLoginAlertDialog("Log in to submit a feedback");

        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //chcek if is connected
                if (coMeth.isConnected()) {

                    desc = descField.getText().toString();
                    //check that desc is not empty
                    if (!desc.isEmpty()) {

                        showProgress(getString(R.string.submitting));
                        //get fields content
                        name = nameField.getText().toString();
                        phone = phoneField.getText().toString();
                        email = emailField.getText().toString();

                        //getname
                        if (!name.isEmpty()) {

                            //has name
                            if (!phone.isEmpty()) {

                                //has name, phone
                                if (!email.isEmpty()) {

                                    //has name, phone, email
                                    feedbackContactArray.add(name);
                                    feedbackContactArray.add(phone);
                                    feedbackContactArray.add(email);

                                } else {

                                    //has name, phone
                                    feedbackContactArray.add(name);
                                    feedbackContactArray.add(phone);

                                }

                            } else {

                                //has name
                                if (!email.isEmpty()) {

                                    //has name, email
                                    feedbackContactArray.add(name);
                                    feedbackContactArray.add(email);

                                } else {

                                    //has name
                                    feedbackContactArray.add(name);

                                }

                            }

                        } else {

                            if (!phone.isEmpty()) {

                                //has phone
                                if (!email.isEmpty()) {

                                    //has phone, email
                                    feedbackContactArray.add(phone);
                                    feedbackContactArray.add(email);

                                } else {

                                    //has phone
                                    feedbackContactArray.add(phone);

                                }

                            } else {

                                if (!email.isEmpty()) {

                                    //has email
                                    feedbackContactArray.add(email);

                                }

                            }

                        }

                        String userId = coMeth.getUid();

                        Map<String, Object> feedackMap = new HashMap<>();
                        feedackMap.put("user_id", userId);
                        feedackMap.put("timestamp", FieldValue.serverTimestamp());
                        feedackMap.put("desc", desc);
                        if (!feedbackContactArray.isEmpty())
                            feedackMap.put("contact_details", feedbackContactArray);

                        coMeth.getDb()
                                .collection("Users/" + userId + "/Feedback")
                                .add(feedackMap)
                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {

                                        //check if is complete
                                        if (task.isSuccessful()) {

                                            progressDialog.dismiss();
                                            AlertDialog.Builder feedbackBuilder = new AlertDialog.Builder(FeedbackActivity.this);
                                            feedbackBuilder.setTitle(getString(R.string.feedback_title_text))
                                                    .setIcon(R.drawable.ic_action_feedback)
                                                    .setMessage(getString(R.string.thanks_feedback_text))
                                                    .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            goToSettings();

                                                        }
                                                    })
                                                    .setCancelable(false)
                                                    .show();


                                        } else {

                                            //task failed
                                            Log.d(TAG, "onComplete: task failed" + task.getException());
                                            progressDialog.dismiss();
                                            showSnack(getString(R.string.failed_to_submit_text));

                                        }

                                    }
                                });


                    } else {

                        //desc cant be empty
                        showSnack("Enter a feedback description");

                    }

                } else {

                    //device not connected
                    showSnack(getString(R.string.failed_to_connect_text));
                }

            }
        });


    }

    private void goToSettings() {
        startActivity(new Intent(FeedbackActivity.this, SettingsActivity.class));
    }

    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(FeedbackActivity.this);
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

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.feedbackLayout),
                message, Snackbar.LENGTH_SHORT).show();
    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(FeedbackActivity.this);
        progressDialog.setMessage(message);
        progressDialog.show();

    }

}
