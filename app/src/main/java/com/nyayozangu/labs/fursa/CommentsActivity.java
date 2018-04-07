package com.nyayozangu.labs.fursa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {


    private static final String TAG = "Sean";
    private ImageView sendButton;
    private EditText chatField;
    private CircleImageView currentUserImage;

    private RecyclerView commentsRecyclerView;

    //progress
    private ProgressDialog progressDialog;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);


        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();


        sendButton = findViewById(R.id.commentsSendBottonImageView);
        chatField = findViewById(R.id.commentsChatEditText);
        currentUserImage = findViewById(R.id.commentsCurrentUserImageView);

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);

        //get the sent intent
        Intent getPostIdIntent = getIntent();
        postId = getPostIdIntent.getStringExtra("postId");
        Log.d(TAG, "postId is: " + postId);

        //inform user to login to comment
        if (mAuth.getCurrentUser() == null) {
            currentUserImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_person));
            chatField.setHint("Log in to post a comment, click button to login");
            chatField.setClickable(false);
            sendButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_login));
            //clicking send to go to login with postId intent
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "login button has clicked");
                    Intent openPostIntent = new Intent(getApplicationContext(), CommentsActivity.class);
                    openPostIntent.putExtra("postId", postId);
                    startActivity(openPostIntent);
                }

            });
        }


    }
}
