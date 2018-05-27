package com.nyayozangu.labs.fursa.activities.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private Toolbar toolbar;
    private WebView webview;
    private Button acceptTermsButton;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        toolbar = findViewById(R.id.privacyPolicyToolbar);
        webview = findViewById(R.id.privacyPolicyWebview);
        acceptTermsButton = findViewById(R.id.privacyPolicyAcceptButton);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.privacy_policy_text);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        showProgress(getResources().getString(R.string.loading_text));

        if (coMeth.isConnected()) {

            webview.loadUrl(getString(R.string.privacy_policy_url));
            if (webview.getProgress() > 50) {
                coMeth.stopLoading(progressDialog);
            }

        } else {

            webview.setVisibility(View.GONE);
            showSnack(getString(R.string.failed_to_connect_text));
            coMeth.stopLoading(progressDialog);
        }

        if (getIntent() != null) {
            handleIntent();
        }

        acceptTermsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sharedPref =
                        PrivacyPolicyActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.has_accepted_terms),
                        getString(R.string.true_text));
                editor.apply();

                startActivity(new Intent(
                        PrivacyPolicyActivity.this, MainActivity.class));
            }
        });
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent.getStringExtra("source") != null) {
            String source = intent.getStringExtra("source");
            if (source.equals("tut")) {
                acceptTermsButton.setVisibility(View.VISIBLE);
                //hide the back button
                getSupportActionBar().hide();
            }
        }
    }

    private void showProgress(String message) {
        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.privacyPolicyLayout),
                message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent() != null) {
            handleIntent();
        } else {
            acceptTermsButton.setVisibility(View.GONE);
            //hide the back button
            getSupportActionBar().show();
        }
    }
}
