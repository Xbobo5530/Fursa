package com.nyayozangu.labs.fursa;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "Sean";

    // TODO: 4/4/18 add twitter sign in
    // TODO: 4/4/18 check phone number sign in


    private EditText regEmailField;
    private EditText regPasswordField;
    private EditText regConfirmPasswordField;
    private Button regButton;
    private Button regLoginButton;
    private FloatingActionButton closeRegisterButton;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();

        //initialize elements
        regEmailField = findViewById(R.id.regEmail);
        regPasswordField = findViewById(R.id.regPasswordEditText);
        regConfirmPasswordField = findViewById(R.id.regConfirmPasswordEditText);
        regButton = findViewById(R.id.regButton);
        regLoginButton = findViewById(R.id.regLoginButton);
        closeRegisterButton = findViewById(R.id.reg_close_button);

        closeRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMain();
            }
        });

        regLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to login page
                goToLogin();

            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = regEmailField.getText().toString();
                String password = regPasswordField.getText().toString();
                String confirmPassword = regConfirmPasswordField.getText().toString();

                //check if fields are empty
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {
                    //check for confirm password
                    if (password.equals(confirmPassword)) {

                        // show progress
                        showProgress("Loading...");

                        //create new user
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //check registration status
                                if (task.isSuccessful()) {
                                    //registration successful
                                    //go to account setup
                                    startActivity(new Intent(RegisterActivity.this, AccountActivity.class));
                                    finish();
                                } else {
                                    //registration failed
                                    String errorMessage = task.getException().getMessage();

                                    Snackbar.make(findViewById(R.id.register_activity_layout),
                                            "Error: " + errorMessage, Snackbar.LENGTH_SHORT).show();

                                    /*Toast.makeText(RegisterActivity.this, "Error " + errorMessage, Toast.LENGTH_LONG).show();*/
                                }
                            }
                        });

                        progressDialog.dismiss();
                        /*//hide progress bar
                        regProgress.setVisibility(View.GONE);*/


                    } else {
                        //password and confirm pass are a mismatch
                        Toast.makeText(RegisterActivity.this, "Confirmed password does not match", Toast.LENGTH_LONG).show();
                    }
                } else if (TextUtils.isEmpty(email)) {

                    Snackbar.make(findViewById(R.id.register_activity_layout),
                            "Enter your email to sign up", Snackbar.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(password)) {
                    Snackbar.make(findViewById(R.id.register_activity_layout),
                            "Enter your password to sign up", Snackbar.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(confirmPassword)) {
                    Snackbar.make(findViewById(R.id.register_activity_layout),
                            "Confirm your password to sign up", Snackbar.LENGTH_SHORT).show();
                } else {
                    //all fields are empty
                    Snackbar.make(findViewById(R.id.register_activity_layout),
                            "Enter your email and password to sign up", Snackbar.LENGTH_SHORT).show();

                    /*Toast.makeText(RegisterActivity.this, "Please fill in all the fields!!", Toast.LENGTH_LONG).show();*/
                }
            }
        });

    }

    private void goToLogin() {
        //go to log in page
        Intent goToLoginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(goToLoginIntent);
        finish();
    }

    private void goToMain() {
        Intent goToMainIntent = new Intent(this, MainActivity.class);
        startActivity(goToMainIntent);
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
