package com.nyayozangu.labs.fursa;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    private EditText loginEmailText;
    private EditText loginPasswordText;
    private Button loginButton;
    private Button loginRegistrationButton;
    private ProgressBar loginProgressBar;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "at LoginActivity, at onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initiating Firebase instance
        mAuth = FirebaseAuth.getInstance();

        //initiating elements
        loginEmailText = findViewById(R.id.emailEditText);
        loginPasswordText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        loginRegistrationButton = findViewById(R.id.loginRegisterButton);
        loginProgressBar = findViewById(R.id.loginProgressBar);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sign in an existing user

                String loginEmail = loginEmailText.getText().toString();
                String loginPassword = loginPasswordText.getText().toString();

                if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)) {
                    //login email and password are not empty
                    //show progress bar
                    loginProgressBar.setVisibility(View.VISIBLE);
//                    signInExistingUser(loginEmail, loginPassword);

                    mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //check if login was successful
                            if (task.isSuccessful()) {
                                //login was successful
                                startMain();
                            } else {
                                //login was not successful
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,
                                        "Error: " + errorMessage,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } else {
                    // TODO: 4/1/18 alert user to fill in an email and/or a password
                }
            }
        });
        loginRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //launch register activity
                // TODO: 4/1/18 launch register activity
            }
        });
    }

    //check to see if the user is logged in
    @Override
    public void onStart() {

        Log.d(TAG, "at onStart");

        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);

        if (currentUser != null) {
            //user is logged in
            startMain();

        }
    }

    private void startMain() {
        //send user to Main
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }


}
