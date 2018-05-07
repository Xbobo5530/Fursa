package com.nyayozangu.labs.fursa.activities.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;
import com.nyayozangu.labs.fursa.users.Users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public class AdminActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "Sean";
    //common methods
    private CoMeth coMeth = new CoMeth();
    private Toolbar toolbar;
    private Button reportPostsButton, reportCommentsButton;
    private ListView reportedItemsList;

    private ArrayList<DocumentChange> reportedCommentsList;
    private ArrayList<String> reportedComments;
    private ArrayList<String> reportedCommentsImageUrls;
    private ArrayList<String> reportedCommentsUsernames;
    private String[] reportedCommentsListItems;
    private String[] reportedCommentsImageUrlsListItems;
    private String[] reportedCommentsUsernamesListItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        //initiate items
        toolbar = findViewById(R.id.adminToolbar);
        reportPostsButton = findViewById(R.id.reportedPostsButton);
        reportCommentsButton = findViewById(R.id.reportedCommentsButton);
        reportedItemsList = findViewById(R.id.reportedItemsListView);

        reportedComments = new ArrayList<>();
        reportedCommentsImageUrls = new ArrayList<>();
        reportedCommentsUsernames = new ArrayList<>();

        /*reportedCommentsListItems = new String[]{};
        reportedCommentsImageUrlsListItems = new String[]{};
        reportedCommentsUsernamesListItems = new String[]{};*/

        //handle toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Admin");
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
                                    String comment = doc.getDocument().get("comment").toString();
                                    reportedComments.add(comment);
                                    //get comment user
                                    String postId = doc.getDocument().get("postId").toString();
                                    coMeth.getDb()
                                            .collection("Posts")
                                            .document(postId)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful() && task.getResult().exists()) {

                                                        Posts post = task.getResult().toObject(Posts.class);
                                                        String postUserId = post.getUser_id();

                                                        //get user details
                                                        coMeth.getDb()
                                                                .collection("Users")
                                                                .document(postUserId)
                                                                .get()
                                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                                        if (task.isSuccessful() && task.getResult().exists()) {

                                                                            //convert user to object
                                                                            Users user = task.getResult().toObject(Users.class);
                                                                            //get username
                                                                            String username = user.getName();
                                                                            String userImage = user.getImage();
                                                                            String userThumb = user.getThumb();

                                                                            //add items to arrays
                                                                            reportedCommentsImageUrls.add(userImage);
                                                                            reportedCommentsUsernames.add(username);

                                                                        } else {
                                                                            if (!task.isSuccessful()) {
                                                                                //task failed
                                                                                // TODO: 5/7/18 handle getting user task failed
                                                                            }
                                                                            if (!task.getResult().exists()) {
                                                                                //user does not exist
                                                                                // TODO: 5/7/18 handle user does not exist
                                                                            }
                                                                        }

                                                                    }
                                                                });

                                                    } else {

                                                        if (!task.isSuccessful()) {
                                                            //task failed
                                                            // TODO: 5/7/18 handle task failed
                                                        }
                                                        if (!task.getResult().exists()) {
                                                            //user post does not exist
                                                            // TODO: 5/7/18 handle post does not exist
                                                        }

                                                    }
                                                }
                                            });

                                }

                            }

                            //handle adapter
                            /*catsListItems = catSubsArray.toArray((new String[catSubsArray.size()]));*/
                            reportedCommentsListItems = reportedComments.toArray(new String[reportedComments.size()]);
                            reportedCommentsImageUrlsListItems = reportedCommentsImageUrls.toArray(new String[reportedCommentsImageUrls.size()]);
                            reportedCommentsUsernamesListItems = reportedCommentsUsernames.toArray(new String[reportedCommentsUsernames.size()]);

                            Log.d(TAG, "onComplete: " +
                                    "\nreportedCommentsListItems: " + reportedCommentsListItems +
                                    " length is: " + reportedCommentsListItems.length +
                                    "\nreportedCommentsImageUrlsListItems: " + reportedCommentsImageUrlsListItems +
                                    " length is: " + reportedCommentsImageUrlsListItems.length +
                                    "\nreportedCommentsUsernamesListItems: " + reportedCommentsImageUrlsListItems +
                                    " lenth is: " + reportedCommentsImageUrlsListItems.length);

                            //create a simple adapter
                            List<HashMap<String, String>> aList = new ArrayList<>();

                            for (int i = 0; i < reportedCommentsListItems.length; i++) {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("listView_comment", reportedCommentsListItems[i]);
                                                                                /*hm.put("listView_image_url", reportedCommentsImageUrlsListItems[i]);*/
                                hm.put("listView_username", reportedCommentsUsernamesListItems[i]);
                                aList.add(hm);
                            }

                            String[] from = {"listView_comment",
                                    "listView_username"};
                            int[] to = {R.id.reportedDetailsTextView,
                                    R.id.reportedUsernameTextView};

                            SimpleAdapter reportedCommentsSimpleAdapter = new SimpleAdapter(getBaseContext(),
                                    aList,
                                    R.layout.reported_item_list,
                                    from,
                                    to);
                            ListView catGridView = findViewById(R.id.reportedItemsListView);
                            catGridView.setAdapter(reportedCommentsSimpleAdapter);


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
