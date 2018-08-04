package com.nyayozangu.labs.fursa.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.jsibbold.zoomage.ZoomageView;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.helpers.CoMeth;


public class ViewImageActivity extends AppCompatActivity {
    private ZoomageView mContentView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        mContentView = findViewById(R.id.fullscreen_content);
        handleIntent();
    }

    private void handleIntent() {
        if (getIntent() != null) {
            String downloadImageUrl = getIntent().getStringExtra(getResources().getString(R.string.view_image_intent_name));
            setImage(downloadImageUrl);
        }
    }

    private void setImage(String downloadUrl) {
        CoMeth coMeth = new CoMeth();
        coMeth.setImageWithTransition(R.drawable.appiconshadow, downloadUrl, mContentView, this);
    }
}
