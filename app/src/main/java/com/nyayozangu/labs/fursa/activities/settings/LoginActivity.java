package com.nyayozangu.labs.fursa.activities.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
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

    // TODO: 4/14/18 connect social accs to single user

    private static final String TAG = "Sean";
    private static final int RC_SIGN_IN = 0;
    //for google sign in
    GoogleSignInClient mGoogleSignInClient;
    //for facebook sing in
    CallbackManager mCallbackManager;
    //common methods
    private CoMeth coMeth = new CoMeth();
    private Button loginButton;
    private Button loginRegistrationButton;
    private ProgressDialog progressDialog;
    private Toolbar toolbar;
    private TextView loginAlertTextView;

    //social login buttons
    private SignInButton googleSignInButton;
    private TwitterLoginButton twitterLoginButton;
    private LoginButton facebookLoginButton;

    private View registerView;
    private View loginView;

    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onStart() {
        super.onStart();

        //check connection to show the login buttons
        if (!coMeth.isConnected()) {

            //hide login buttons
            loginButton.setVisibility(View.GONE);
            loginRegistrationButton.setVisibility(View.GONE);
            googleSignInButton.setVisibility(View.GONE);
            facebookLoginButton.setVisibility(View.GONE);
            twitterLoginButton.setVisibility(View.GONE);
            //show connection alert
            loginAlertTextView.setText(getString(R.string.failed_to_connect_text));

        } else {

            //hide login buttons
            loginButton.setVisibility(View.VISIBLE);
            loginRegistrationButton.setVisibility(View.VISIBLE);
            googleSignInButton.setVisibility(View.VISIBLE);
            facebookLoginButton.setVisibility(View.VISIBLE);
            twitterLoginButton.setVisibility(View.VISIBLE);

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("D99HM3hZBaOVTKW6tfUeAtAcQ",
                        "dxQNNNK4q4W32Bpu6RHRI2CNrAiFVqokAJ9t2d9cSCwQxdnY0d"))
                .debug(true)
                .build();
        Twitter.initialize(config);

        setContentView(R.layout.activity_login);

        //initiating elements
        loginButton = findViewById(R.id.loginButton);
        loginRegistrationButton = findViewById(R.id.loginRegisterButton);
        loginAlertTextView = findViewById(R.id.loginAlertTextView);
        toolbar = findViewById(R.id.loginToolbar);
        //social login
        googleSignInButton = findViewById(R.id.google_sign_in_button);
        twitterLoginButton = findViewById(R.id.twitter_login_button);
        facebookLoginButton = findViewById(R.id.facebook_login_button);

        //get the sent intent
        Intent getPostIdIntent = getIntent();
        final String postId = getPostIdIntent.getStringExtra("postId");
        Log.d(TAG, "postId is: " + postId);

        //handle toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.login_text));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //check connection to show the login buttons
        if (!coMeth.isConnected()) {

            //hide login buttons
            loginButton.setVisibility(View.GONE);
            loginRegistrationButton.setVisibility(View.GONE);
            googleSignInButton.setVisibility(View.GONE);
            facebookLoginButton.setVisibility(View.GONE);
            twitterLoginButton.setVisibility(View.GONE);
            //show connection alert
            loginAlertTextView.setText(getString(R.string.failed_to_connect_text));

        } else {

            //show login buttons
            loginButton.setVisibility(View.VISIBLE);
            loginRegistrationButton.setVisibility(View.VISIBLE);
            googleSignInButton.setVisibility(View.VISIBLE);
            facebookLoginButton.setVisibility(View.VISIBLE);
            twitterLoginButton.setVisibility(View.VISIBLE);

        }

        //handle intents
        if (getIntent() != null &&
                getIntent().getStringExtra("message") != null) {

            String alertMessage = getIntent().getStringExtra("message");
            loginAlertTextView.setText(alertMessage);
            loginAlertTextView.setVisibility(View.VISIBLE);

        }

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

                                    //hide keyboard
                                    hideKeyBoard();
                                    //show progress
                                    showProgress(getString(R.string.logging_in_text));

                                    coMeth.getAuth()
                                            .signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            //check if login was successful
                                            if (task.isSuccessful()) {

                                                processLoginIntent();

                                            } else {

                                                //login was not successful
                                                String errorMessage = task.getException().getMessage();
                                                showSnack(R.id.login_activity_layout, "Error: " + errorMessage);

                                            }
                                            //hide progress
                                            coMeth.stopLoading(progressDialog);
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

                                String email = emailField.getText().toString();
                                String password = passwordField.getText().toString();
                                String confirmPassword = confirmPasswordField.getText().toString();

                                //check if fields are empty
                                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {
                                    //check for confirm password
                                    if (password.equals(confirmPassword)) {

                                        //hide keyboard
                                        hideKeyBoard();
                                        // show progress
                                        showProgress("Registering...");

                                        //create new user
                                        coMeth.getAuth()
                                                .createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                //check registration status
                                                if (task.isSuccessful()) {

                                                    //go to account setup
                                                    startActivity(new Intent(LoginActivity.this, AccountActivity.class));
                                                    finish();

                                                } else {

                                                    //registration failed
                                                    String errorMessage = task.getException().getMessage();
                                                    showSnack(R.id.login_activity_layout, "Error: " + errorMessage);

                                                }
                                            }
                                        });

                                        coMeth.stopLoading(progressDialog, null);

                                    } else {

                                        // TODO: 5/11/18 user string resources
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
                        .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
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
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
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

    private void processLoginIntent() {
        //login was successful
        if (getIntent() == null) {
            goToAccSettings();
        } else {

            Intent sourceIntent = getIntent();
            if (sourceIntent.getStringExtra("source") != null) {

                switch (sourceIntent.getStringExtra("source")) {

                    case "comments":

                        Intent commentsIntent = new Intent(LoginActivity.this, AccountActivity.class);
                        commentsIntent.putExtra("postId", sourceIntent.getStringExtra("postId"));
                        startActivity(commentsIntent);
                        finish();
                        break;

                    case "categories":

                        Intent catsIntent = new Intent(LoginActivity.this, AccountActivity.class);
                        catsIntent.putExtra("category", sourceIntent.getStringExtra("category"));
                        startActivity(catsIntent);
                        finish();
                        break;

                    default:
                        goToAccSettings();

                }
            } else {

                goToAccSettings();

            }

        }
    }

    private void showSnack(int id, String message) {
        Snackbar.make(findViewById(id),
                message, Snackbar.LENGTH_LONG).show();
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
        coMeth.getAuth()
                .signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = coMeth.getAuth().getCurrentUser();
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

        coMeth.getAuth()
                .signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
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

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        coMeth.getAuth()
                .signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Log.d(TAG, "signInWithCredential:success");
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Snackbar.make(findViewById(R.id.login_activity_layout),
                "Google Play Services error.", Snackbar.LENGTH_SHORT).show();

    }

    private void hideKeyBoard() {

        Log.d(TAG, "hideKeyBoard: ");
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.d(TAG, "onClick: exception on hiding keyboard " + e.getMessage());
        }

    }
}
