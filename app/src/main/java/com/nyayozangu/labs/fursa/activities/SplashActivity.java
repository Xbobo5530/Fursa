package com.nyayozangu.labs.fursa.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.nyayozangu.labs.fursa.BuildConfig;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.tutorial.TutorialActivity;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        checkFirstRun();
        finish();
    }

    /**
     * checks if its the first time the app is launched
     */
    private void checkFirstRun() {

        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOES_NOT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOES_NOT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            //start the main activity

            Intent goToMainIntent = new Intent(
                    SplashActivity.this, MainActivity.class);
            startActivity(goToMainIntent);
            finish();
            Log.i("Sean", "At Splash, this is normal");

        } else if (savedVersionCode == DOES_NOT_EXIST) {

            // This is a new install (or the user cleared the shared preferences)
            //start the main activity
            Intent goToTutorialIntent = new Intent(
                    SplashActivity.this, TutorialActivity.class);
            startActivity(goToTutorialIntent);
            finish();
            Log.i("Sean", "At Splash, this is new install / cleared cache");

        } else if (currentVersionCode > savedVersionCode) {

            // This is an upgrade
            //start the main activity
            Intent infoUpdateIntent = new Intent(
                    SplashActivity.this, MainActivity.class);
            infoUpdateIntent.putExtra(
                    getResources().getString(R.string.ACTION_NAME),
                    getResources().getString(R.string.update_value));
            startActivity(infoUpdateIntent);
            finish();
            Log.i("Sean", "At Splash, this is upgrade");
        } else {
            startActivity(new Intent(
                    SplashActivity.this, MainActivity.class));
            finish();
        }
        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

}
