package com.nyayozangu.labs.fursa;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    // TODO: 4/4/18 add google sign in
    // TODO: 4/4/18 add twitter sign in
    // TODO: 4/4/18 add facebook sign in
    // TODO: 4/4/18 check phone number sign in


    private EditText regEmailField;
    private EditText regPasswordField;
    private EditText regConfirmPasswordField;
    private Button regButton;
    private Button regLoginButton;
    private ProgressBar regProgress;

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
        regProgress = findViewById(R.id.regRrogressBar);

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
                        //create new user

                        //show progress bar
                        regProgress.setVisibility(View.VISIBLE);
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
                                    Toast.makeText(RegisterActivity.this, "Error " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        //hide progress bar
                        regProgress.setVisibility(View.GONE);


                    } else {
                        //password and confirm pass are a mismatch
                        Toast.makeText(RegisterActivity.this, "Confirmed password does not match", Toast.LENGTH_LONG).show();
                    }
                } else {
                    //fields are empty
                    Toast.makeText(RegisterActivity.this, "Please fill in all the fields!!", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onStart() {
        super.onStart();
        goToMain();
    }

    private void goToMain() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent goToMainIntent = new Intent(this, MainActivity.class);
            startActivity(goToMainIntent);
            finish();
        }
    }
}
