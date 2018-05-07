package com.nyayozangu.labs.fursa.activities.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class AdminActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "Sean";
    //common methods
    private CoMeth coMeth = new CoMeth();
    private android.support.v7.widget.Toolbar toolbar;
    private Button reportPostsButton, reportCommentsButton;
    private ListView reportedItemsList;

    private ArrayList<DocumentChange> reportedCommentsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        //initiate items
        toolbar = findViewById(R.id.settingsToolbar);
        reportPostsButton = findViewById(R.id.reportedPostsButton);
        reportCommentsButton = findViewById(R.id.reportedCommentsButton);
        reportedItemsList = findViewById(R.id.reportedItemsListView);

        reportedCommentsList = new ArrayList<DocumentChange>();

        //handle toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //handle clicks
        reportPostsButton.setOnClickListener(this);
        reportCommentsButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.reportedPostsButton:
                showReportedPostsFeed();
                break;
            case R.id.reportedCommentsButton:

                showReportedCommentsFeed();
                break;
            default:
                Log.d(TAG, "onClick: at default");


        }

    }

    private void showReportedCommentsFeed() {

        // TODO: 5/7/18 show reported comments feed
        Log.d(TAG, "showReportedCommentsFeed: ");
        //access db
        coMeth.getDb()
                .collection("Flags")
                .document("comments")
                .collection("Comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                //check if an item is added
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String commentId = doc.getDocument().getId();
                                    //add document to reported comments array
                                    reportedCommentsList.add(doc);

                                }

                            }
                        } else {

                            //no reported comments found
                            // TODO: 5/7/18 handle no reported comments

                        }
                    }
                });

    }

    private void showReportedPostsFeed() {

        // TODO: 5/7/18 show reported posts feed

    }
}
