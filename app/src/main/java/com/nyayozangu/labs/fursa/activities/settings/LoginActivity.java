package com.nyayozangu.labs.fursa.activities.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    // TODO: 4/5/18 when user has types email and pass, then clicks register, send typed details to register page
    // TODO: 4/14/18 connect social accs to single user


    private static final String TAG = "Sean";
    private static final int RC_SIGN_IN = 0;
    //for google sign in
    GoogleSignInClient mGoogleSignInClient;
    //for facebook sing in
    CallbackManager mCallbackManager;

    private Button loginButton;
    private Button loginRegistrationButton;
    private ImageButton closeLoginButton;

    private ProgressDialog progressDialog;

    //social login buttons
    private SignInButton googleSignInButton;
    private TwitterLoginButton twitterLoginButton;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private View registerView;
    private View loginView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "at LoginActivity, at onCreate");

        super.onCreate(savedInstanceState);


        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("D99HM3hZBaOVTKW6tfUeAtAcQ",
                        "dxQNNNK4q4W32Bpu6RHRI2CNrAiFVqokAJ9t2d9cSCwQxdnY0d"))
                .debug(true)
                .build();
        Twitter.initialize(config);

        setContentView(R.layout.activity_login);

        //Initiating Firebase instance
        mAuth = FirebaseAuth.getInstance();

        //initiating elements
        loginButton = findViewById(R.id.loginButton);
        loginRegistrationButton = findViewById(R.id.loginRegisterButton);
        closeLoginButton = findViewById(R.id.login_close_button);

        //social login
        googleSignInButton = findViewById(R.id.google_sign_in_button);
        twitterLoginButton = findViewById(R.id.twitter_login_button);


        //get the sent intent
        Intent getPostIdIntent = getIntent();
        String postId = getPostIdIntent.getStringExtra("postId");
        Log.d(TAG, "postId is: " + postId);
        // TODO: 4/7/18 when user comes to login form comments, return user to comments after loging in
        // TODO: 4/9/18 setup intent extra receivers for source page and post ids, to return the user to a specific post/ page after login


        closeLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //user closes the login page ad goes back to home page
                goToMain();

            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: login button is clicked");
                //sign in an existing user
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                loginView = inflater.inflate(R.layout.login_alert_dialog_content, null);

                //show login in with email dialog
                AlertDialog.Builder loginBuilder = new AlertDialog.Builder(LoginActivity.this);
                loginBuilder.setTitle("Login with Email")
                        .setIcon(getDrawable(R.drawable.ic_action_contact_email))
                        .setView(loginView)
                        .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        })
                        .setPositiveButton(getString(R.string.login_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                EditText emailField = loginView.findViewById(R.id.loginDialogEmailEditText);
                                EditText passwordField = loginView.findViewById(R.id.loginDialogPasswordEditText);
                                Log.d(TAG, "onClick: items are initialized");
                                String email = emailField.getText().toString().trim();
                                String password = passwordField.getText().toString().trim();
                                Log.d(TAG, "onClick: \nemail is: " + email +
                                        "\npassword is: " + password);

                                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

                                    //show progress
                                    showProgress("Loading...");

                                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            //check if login was successful
                                            if (task.isSuccessful()) {
                                                //login was successful

                                                startMain();
                                            } else {
                                                //login was not successful
                                                String errorMessage = task.getException().getMessage();

                                                showSnack(R.id.login_activity_layout, "Error: " + errorMessage);

                                            }
                                            //hide progress
                                            progressDialog.dismiss();
                                        }
                                    });

                                } else if (TextUtils.isEmpty(email)) {

                                    showSnack(R.id.login_activity_layout, "Enter your email address");

                                } else if (TextUtils.isEmpty(password)) {

                                    showSnack(R.id.login_activity_layout, "Enter your login password");

                                } else {

                                    showSnack(R.id.login_activity_layout, "Enter your login details to login");

                                }

                            }
                        })
                        .show();

            }
        });
        loginRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: login button clicked");
                //open dialog
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                registerView = inflater.inflate(R.layout.register_alert_dialog_content, null);

                AlertDialog.Builder registerDialog = new AlertDialog.Builder(LoginActivity.this);
                registerDialog.setTitle("Register with Email")
                        .setIcon(getDrawable(R.drawable.ic_action_email))
                        .setView(registerView)
                        .setPositiveButton("Register", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //get text
                                EditText emailField = registerView.findViewById(R.id.regEmailEditText);
                                EditText passwordField = registerView.findViewById(R.id.regPassEditText);
                                EditText confirmPasswordField = registerView.findViewById(R.id.regConfirmPassEditText);
                                Log.d(TAG, "onClick: initialized login dialog items");

                                String email = emailField.getText().toString();
                                String password = passwordField.getText().toString();
                                String confirmPassword = confirmPasswordField.getText().toString();

                                //check if fields are empty
                                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {
                                    //check for confirm password
                                    if (password.equals(confirmPassword)) {

                                        // show progress
                                        showProgress("Registering...");

                                        //create new user
                                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                //check registration status
                                                if (task.isSuccessful()) {

                                                    //registration successful

                                                    // TODO: 4/27/18 show an alert dialog to inform user that email verification was sent
                                                    AlertDialog.Builder emailVerBuilder = new AlertDialog.Builder(LoginActivity.this);
                                                    emailVerBuilder.setTitle(R.string.email_ver_text)
                                                            .setIcon(R.drawable.ic_action_info_grey)
                                                            .setMessage("A verification email has been sent to your email address")
                                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {

                                                                    //send ver email
                                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                                    user.sendEmailVerification()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Log.d(TAG, "Email sent.");
                                                                                    }
                                                                                }
                                                                            });

                                                                    //go to account setup
                                                                    startActivity(new Intent(LoginActivity.this, AccountActivity.class));
                                                                    finish();


                                                                }
                                                            })
                                                            .show();

                                                } else {

                                                    //registration failed
                                                    String errorMessage = task.getException().getMessage();
                                                    showSnack(R.id.login_activity_layout, "Error: " + errorMessage);

                                                }
                                            }
                                        });

                                        progressDialog.dismiss();

                                    } else {

                                        //password and confirm pass are a mismatch
                                        showSnack(R.id.login_activity_layout, "Confirmed password does not match");

                                    }
                                } else if (TextUtils.isEmpty(email)) {

                                    showSnack(R.id.login_activity_layout, "Enter your email to sign up");

                                } else if (TextUtils.isEmpty(password)) {

                                    showSnack(R.id.login_activity_layout, "Enter your password to sign up");

                                } else if (TextUtils.isEmpty(confirmPassword)) {

                                    showSnack(R.id.login_activity_layout, "Confirm your password to sign up");

                                } else {

                                    //all fields are empty
                                    showSnack(R.id.login_activity_layout, "Enter your email and password to sign up");

                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        })
                        .show();

            }
        });


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //on user clicks sign in with google
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sign with google
                signIn();

            }
        });


        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });


        //Twitter login


        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                //login failed
            }
        });


    }

    private void showSnack(int id, String message) {
        Snackbar.make(findViewById(id),
                message, Snackbar.LENGTH_LONG).show();
    }

    //check to see if the user is logged in
    @Override
    public void onStart() {

        Log.d(TAG, "at onStart");

        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

    }

    private void startMain() {
        //send user to Main
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }


    //sign in with google
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    //for google sign in result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.e(TAG, "Google Sign In failed.");
                Snackbar.make(findViewById(R.id.login_activity_layout),
                        "Google Sign In failed.", Snackbar.LENGTH_SHORT).show();
            }

        }

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the Twitter login button.
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);

    }


    //handle result for facebook sign in
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            goToAccSettings();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.login_activity_layout),
                                    "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    //handle result for twitter sign in
    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //take user to acc settings after
                            goToAccSettings();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.login_activity_layout),
                                    "Authentication Failed.", Snackbar.LENGTH_SHORT).show();

                        }

                    }
                });
    }

    //go to accounts page
    private void goToAccSettings() {
        startActivity(new Intent(LoginActivity.this, AccountActivity.class));
        finish();
    }

    //take user to main activity
    private void goToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = mAuth.getCurrentUser();
                            /*Toast.makeText(LoginActivity.this, "Sign in success", Toast.LENGTH_SHORT).show();*/
                            Snackbar.make(findViewById(R.id.login_activity_layout),
                                    "Sign in success", Snackbar.LENGTH_SHORT).show();

                            //go to acc settings
                            goToAccSettings();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.login_activity_layout),
                                    "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }


    private void handleFirebaseAuthResult(AuthResult authResult) {
        if (authResult != null) {
            // Welcome the user
            FirebaseUser user = authResult.getUser();
            Snackbar.make(findViewById(R.id.login_activity_layout),
                    "Welcome " + user.getEmail(), Snackbar.LENGTH_SHORT).show();

            // Go back to the main activity
            startActivity(new Intent(this, MainActivity.class));
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Snackbar.make(findViewById(R.id.login_activity_layout),
                "Google Play Services error.", Snackbar.LENGTH_SHORT).show();

    }
}
