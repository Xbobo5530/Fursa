package com.nyayozangu.labs.fursa.activities;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.HashMap;
import java.util.Map;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.ACTION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.IMAGE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MESSAGE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NAME;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NOTIFY;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TIMESTAMP;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    // TODO: 6/16/18 post gradle update
    // TODO: 6/16/18 try to use firebase Ui again
    // TODO: 6/16/18 try to user teh job scheduler again

    private static final String TAG = "Sean";
    private static final String CONSUMER_KEY = "D99HM3hZBaOVTKW6tfUeAtAcQ";
    private static final String CONSUMER_SECRET = "dxQNNNK4q4W32Bpu6RHRI2CNrAiFVqokAJ9t2d9cSCwQxdnY0d";
    private static final int RC_SIGN_IN = 0;
    GoogleSignInClient mGoogleSignInClient;
    private CoMeth coMeth = new CoMeth();
    private ProgressDialog progressDialog;
    private TextView loginAlertTextView;
    private SignInButton googleSignInButton;
    private TwitterLoginButton twitterLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeTwitter();
        setContentView(R.layout.activity_login);
        loginAlertTextView = findViewById(R.id.loginAlertTextView);
        Toolbar toolbar = findViewById(R.id.loginToolbar);
        googleSignInButton = findViewById(R.id.google_sign_in_button);
        twitterLoginButton = findViewById(R.id.twitter_login_button);
        handleToolbar(toolbar);
        handleViewsVisibility();
        handleIntent();
        configGoogleSignIn();
        configTwitterSignIn();
    }

    @Override
    protected void onStart() {
        super.onStart();
        handleViewsVisibility();
    }

    private void handleViewsVisibility() {
        if (!coMeth.isConnected(this)) {
            googleSignInButton.setVisibility(View.GONE);
            twitterLoginButton.setVisibility(View.GONE);
            loginAlertTextView.setText(getString(R.string.failed_to_connect_text));
        } else {
            googleSignInButton.setVisibility(View.VISIBLE);
            twitterLoginButton.setVisibility(View.VISIBLE);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null &&
                intent.getStringExtra(MESSAGE) != null) {
            String alertMessage = intent.getStringExtra(MESSAGE);
            loginAlertTextView.setText(alertMessage);
            loginAlertTextView.setVisibility(View.VISIBLE);
        }
    }

    private void handleToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.login_text));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void configTwitterSignIn() {
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
               showSnack(getResources().getString(R.string.error_text) + ": " + exception.getMessage());
            }
        });
    }

    private void initializeTwitter() {
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(CONSUMER_KEY , CONSUMER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    private void configGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.login_activity_layout),
                message, Snackbar.LENGTH_LONG).show();
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
                if (account != null)
                    firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.e(TAG, "Google Sign In failed.");
                Snackbar.make(findViewById(R.id.login_activity_layout),
                        "Google Sign In failed.", Snackbar.LENGTH_SHORT).show();
            }

        }

        // Pass the activity result to the Twitter login button.
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    //handle result for twitter sign in
    private void handleTwitterSession(final TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);
        final AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        //show progress
        showProgress(getResources().getString(R.string.loading_text));
        coMeth.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            final String uid = task.getResult().getUser().getUid();

                            coMeth.getDb().collection(USERS).document(uid).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists()){
                                                coMeth.stopLoading(progressDialog);
                                                finish();
                                            }else{
                                                String name = session.getUserName();
                                                addUserToDb(uid, name, null);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "onFailure: failed to check user " +
                                                    e.getMessage(), e );
                                            showSnack(getResources().getString(R.string.error_text)
                                                    + ": " + e.getMessage());
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.login_activity_layout),
                                    "Authentication Failed.", Snackbar.LENGTH_SHORT).show();

                        }

                    }
                });
    }

    /*//go to accounts page
    private void goToAccSettings() {
        startActivity(new Intent(LoginActivity.this, AccountActivity.class));
        finish();
    }*/

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {

        //show progress
        showProgress(getResources().getString(R.string.loading_text));
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        coMeth.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Log.d(TAG, "signInWithCredential:success");
                            final String uid = task.getResult().getUser().getUid();
                            //check if user exists
                            checkIfUserExists(uid, acct);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //stop loading
                            coMeth.stopLoading(progressDialog);
                            Snackbar.make(findViewById(R.id.login_activity_layout),
                                    "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkIfUserExists(final String uid, final GoogleSignInAccount acct) {
        coMeth.getDb().collection(USERS).document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    coMeth.stopLoading(progressDialog);
                    finish();
                } else{
                    String name = acct.getDisplayName();
                    Uri photoUrl = acct.getPhotoUrl();
                    if (name == null){
                        String email = acct.getEmail();
                        assert email != null; // TODO: 7/25/18 recheck email nullable
                        name = email.substring(email.indexOf("@") - 1);
                        checkPhoto(name, photoUrl, uid);
                    }else{
                        checkPhoto(name, photoUrl, uid);
                    }
                }
            }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "onFailure: failed to check user\n" +
                                e.getMessage());
                        showSnack(getResources().getString(R.string.error_text) +
                                ": " + e.getMessage());
                    }
                });
    }

    private void checkPhoto(String name, Uri photoUrl, String uid) {
        if (photoUrl != null) {
            addUserToDb(uid, name, photoUrl.toString());
        }else{
            addUserToDb(uid, name, null);
        }
    }

    private void addUserToDb(@NonNull String uid, @NonNull final String name, String imageUrl) {
        Map<String, Object> newUserMap = new HashMap<>();
        newUserMap.put(NAME, name);
        if (imageUrl != null) {
            newUserMap.put(IMAGE, imageUrl);
        }
        newUserMap.put(TIMESTAMP, FieldValue.serverTimestamp());
        coMeth.getDb().collection(USERS).document(uid)
            .set(newUserMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    String welcomeMessage = getResources().getString(R.string.welcome_text) + " " + name;
                    intent.putExtra(ACTION, NOTIFY);
                    intent.putExtra(MESSAGE, welcomeMessage);
                    coMeth.stopLoading(progressDialog);
                    startActivity(intent);
                    finish();
                }
            });
    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
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
}
