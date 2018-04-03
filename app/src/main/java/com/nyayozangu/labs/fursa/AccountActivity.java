package com.nyayozangu.labs.fursa;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountActivity extends AppCompatActivity {

    private CircleImageView setupImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        android.support.v7.widget.Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        setupImage = findViewById(R.id.setupImageCircleImageView);

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check os version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //user is running marchmallow or greater
                    //check user permission
                    if (ContextCompat.checkSelfPermission(AccountActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //permission not yet granted
                        //ask for permission
                        Toast.makeText(AccountActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(AccountActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);


                    } else {
                        //permission already granted
                        Toast.makeText(AccountActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
