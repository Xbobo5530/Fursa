package com.nyayozangu.labs.fursa.activities.settings;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private CoMeth coMeth = new CoMeth();
    private Toolbar toolbar;
    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        toolbar = findViewById(R.id.privacyPolicyToolbar);
        webview = findViewById(R.id.privacyPolicyWebview);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.privacy_policy_text);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (coMeth.isConnected()) {

            webview.loadUrl(getString(R.string.privacy_policy_url));

        } else {

            webview.setVisibility(View.GONE);
            showSnack(getString(R.string.failed_to_connect_text));
        }
    }


    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.privacyPolicyLayout),
                message, Snackbar.LENGTH_SHORT).show();
    }


}
