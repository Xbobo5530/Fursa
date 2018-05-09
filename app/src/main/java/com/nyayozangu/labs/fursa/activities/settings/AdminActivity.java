package com.nyayozangu.labs.fursa.activities.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.comments.CommentsActivity;
import com.nyayozangu.labs.fursa.activities.posts.ViewPostActivity;
import com.nyayozangu.labs.fursa.activities.posts.models.Posts;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

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
    private String[] reportedCommentsListItems;
    private String[] reportedCommentsImageUrlsListItems;
    private String[] reportedCommentsUsernamesListItems;
    private ProgressDialog progressDialog;
    private ArrayList<String> reportedPostsTitle;
    private String[] reportedPostsTitleListItems;
    private ArrayList<String> reportedPostIdArray;
    private ArrayList<String> reportedCommentsPostIdArray;
    private ArrayList<String> reportedCommentsIds;


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
        reportedPostsTitle = new ArrayList<>();
        reportedPostIdArray = new ArrayList<>();
        reportedCommentsPostIdArray = new ArrayList<>();
        reportedCommentsIds = new ArrayList<>();

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

    private void showReportedPostsFeed() {

        Log.d(TAG, "showReportedPostsFeed: ");
        //show loading
        showProgress(getString(R.string.loading_text));
        //clear items
        // TODO: 5/9/18 fix clearing reported posts
//        reportedPostsTitle.clear();
        //get items from db
        coMeth.getDb()
                .collection("Flags/posts/Posts")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(
                            @Nullable QuerySnapshot queryDocumentSnapshots,
                            @Nullable FirebaseFirestoreException e) {

                        //check for reported posts
                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                String postId = doc.getDocument().getId();
                                //add post id to reported postIds
                                reportedPostIdArray.add(postId);
                                //get post details
                                getPostDetails(postId);
                            }

                            //stop loading
                            coMeth.stopLoading(progressDialog);
                            /*catsListItems = catSubsArray.toArray((new String[catSubsArray.size()]));*/
                            reportedPostsTitleListItems = reportedPostsTitle
                                    .toArray(new String[reportedPostsTitle.size()]);
                            Log.d(TAG, "onEvent: reportedPostsTitleListItems" +
                                    reportedPostsTitleListItems);
                            handleAdapter(reportedPostsTitleListItems, "posts");

                        } else {

                            //there are no reported posts
                            coMeth.stopLoading(progressDialog);
                            showSnack(getString(R.string.post_not_found_text));

                        }

                    }
                });

    }

    private void getPostDetails(String postId) {
        coMeth.getDb()
                .collection("Posts")
                .document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {

                            Posts post = documentSnapshot.toObject(Posts.class);
                            String title = post.getTitle();
                            reportedPostsTitle.add(title);
                            Log.d(TAG, "onSuccess: " +
                                    "\ntitle is: " + title +
                                    "\nreportedPostsTitle: " + reportedPostsTitle);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d(TAG, "onFailure: get users task failed " +
                                e.getMessage());

                    }
                });
    }

    private void showReportedCommentsFeed() {

        Log.d(TAG, "showReportedCommentsFeed: ");
        //show loading
        showProgress(getString(R.string.loading_text));
        //clear comments
        reportedComments.clear();
        //access db
        coMeth.getDb()
                .collection("Flags/comments/Comments")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                //check if an item is added
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String comment = doc.getDocument().get("comment").toString();
                                    reportedComments.add(comment);
                                    //extra info
                                    String commentId = doc.getDocument().getId();
                                    reportedCommentsIds.add(commentId);
                                    String postId = doc.getDocument().get("postId").toString();
                                    //add post id to comments post id array
                                    reportedCommentsPostIdArray.add(postId);

                                }
                            }

                            //stop loading
                            coMeth.stopLoading(progressDialog);
                            /*catsListItems = catSubsArray.toArray((new String[catSubsArray.size()]));*/
                            reportedCommentsListItems = reportedComments
                                    .toArray(new String[reportedComments.size()]);
                            //create a simple adapter
                            handleAdapter(reportedCommentsListItems, "comments");

                        } else {

                            //no reported comments found
                            Log.d(TAG, "onEvent: no reported comments found");
                            //stop loading
                            coMeth.stopLoading(progressDialog);
                            showSnack(getString(R.string.post_not_found_text));

                        }
                    }
                });

    }

    private void handleAdapter(String[] reportedListItems, final String source) {

        Log.d(TAG, "handleAdapter: ");
        List<HashMap<String, String>> aList = new ArrayList<>();
        for (int i = 0; i < reportedListItems.length; i++) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("listView_report", reportedListItems[i]);
            hm.put("listView_icon",
                    String.valueOf(getDrawable(R.drawable.ic_action_red_flag)));
            aList.add(hm);
        }
        Log.d(TAG, "handleAdapter: aList is " + aList);

        String[] from = {
                "listView_report",
                "listView_icon"
        };
        int[] to = {
                R.id.reportedDetailsTextView,
                R.id.reportFlagImageView
        };

        SimpleAdapter reportsSimpleAdapter = new SimpleAdapter(
                getBaseContext(),
                aList,
                R.layout.reported_item_list,
                from,
                to);
        ListView reportsListView = findViewById(R.id.reportedItemsListView);
        reportsListView.setAdapter(reportsSimpleAdapter);
        reportsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                showProgress(getString(R.string.loading_text));

                switch (source) {

                    case "posts":
                        Intent openPostIntent = new Intent(AdminActivity.this,
                                ViewPostActivity.class);
                        String postsPostId = reportedPostIdArray.get(position);
                        openPostIntent.putExtra("postId", postsPostId);
                        openPostIntent.putExtra("permission", "admin");
                        startActivity(openPostIntent);
                        coMeth.stopLoading(progressDialog);
                        break;

                    case "comments":
                        Intent openCommentsIntent = new Intent(AdminActivity.this,
                                CommentsActivity.class);
                        String commentsPostId = reportedCommentsPostIdArray.get(position);
                        openCommentsIntent.putExtra("postId", commentsPostId);
                        openCommentsIntent.putExtra("permission", "admin");
                        String commentId = reportedCommentsIds.get(position);
                        openCommentsIntent.putExtra("commentId", commentId);
                        startActivity(openCommentsIntent);
                        coMeth.stopLoading(progressDialog);
                        break;

                    default:
                        coMeth.stopLoading(progressDialog);
                        Log.d(TAG, "onItemClick: at admin handling adapter on default");
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

    //show snack
    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.adminLayout),
                message, Snackbar.LENGTH_LONG)
                .show();
    }
}
