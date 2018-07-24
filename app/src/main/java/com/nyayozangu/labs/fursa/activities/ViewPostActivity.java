package com.nyayozangu.labs.fursa.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.adapters.PostsRecyclerAdapter;
import com.nyayozangu.labs.fursa.models.Posts;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.helpers.Notify;
import com.nyayozangu.labs.fursa.models.Users;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.COMMENTS_COLL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.COMMENTS_DOC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FLAGS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FLAGS_NAME;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.IMAGE_URL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.LIKES_COL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MY_POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MY_POSTS_DOC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS_DOC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POST_ID;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SAVES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SOURCE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SUBSCRIPTIONS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TAG_NAME;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TIMESTAMP;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.VIEW_POST;

public class ViewPostActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: 5/30/18 handle posts with multiple images
    // TODO: 6/7/18 when deleting a post with an image, delete the image as well
    // TODO: 6/7/18 run an algorythim to delete all post photos that have no posts
    // TODO: 6/14/18 for view reward notifications use viewsRewardPostId as topic

    // TODO: 7/24/18 remove title

    private static final String TAG = "Sean";
    private static final String IS_NEW_POST = "isNewPost";
    private CoMeth coMeth = new CoMeth();
    private android.support.v7.widget.Toolbar toolbar;
    private ExpandableTextView descTextView;
    private TextView titleTextView, mToTextView;
    private String postUserId, currentUserId, postId, reportDetailsString, postImageUri;
    private ImageView postImage, userImage;
    private ProgressDialog progressDialog;
    private ArrayList<String> catArray, catKeys, reportedItems, flagsArray, tags;
    private Button mLikeButton, mSaveButton,  mCommentsButton, mShareButton, mActivityButton,
            mContactButton, mLocationButton, mEventDateButton, mEventEndDateButton, mCatsButton,
            mPriceButton, mTagsButton, mTimeButton, mUserButton;
    private int likes, comments;
    private Posts post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        mLikeButton = findViewById(R.id.viewPostLikeButton);
        mSaveButton = findViewById(R.id.viewPostSaveButton);
        mCommentsButton = findViewById(R.id.viewPostCommentsButton);
        mShareButton = findViewById(R.id.viewPostShareButton);
        mActivityButton = findViewById(R.id.viewPostActivityButton);

        mContactButton = findViewById(R.id.viewPostContactButton);
        mLocationButton = findViewById(R.id.viewPostLocationButton);
        mEventDateButton = findViewById(R.id.viewPostEventDateButton);
        mEventEndDateButton = findViewById(R.id.viewPostEventEndDateButton);
        mToTextView = findViewById(R.id.viewPostToButton);
        mPriceButton = findViewById(R.id.viewPostPriceButton);
        mTagsButton = findViewById(R.id.viewPostTagsButton);
        mTimeButton = findViewById(R.id.viewPostTimeButton);
        mCatsButton = findViewById(R.id.viewPostCatButton);

        titleTextView = findViewById(R.id.viewPostTitleTextView);
        descTextView = findViewById(R.id.viewPostDescTextView);
        postImage = findViewById(R.id.viewPostImageView);
        userImage = findViewById(R.id.viewPostUserImageView);
        mUserButton = findViewById(R.id.viewPostUserButton);

        catArray = new ArrayList<>();
        catKeys = new ArrayList<>();
        flagsArray = new ArrayList<>();
        reportedItems = new ArrayList<>();
        reportDetailsString = "";
        tags = new ArrayList<>();

        handleToolbar();
        handleIntent();
        showProgress(getResources().getString(R.string.loading_text));
        checkConnectivity();
        handleBackBehaviour();
        handleRelatedPosts();

        mTagsButton.setOnClickListener(this);
        mActivityButton.setOnClickListener(this);
        mLocationButton.setOnClickListener(this);
        mCatsButton.setOnClickListener(this);
        postImage.setOnClickListener(this);
        mTagsButton.setOnClickListener(this);
        mUserButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);
    }

    private void handleRelatedPosts() {
        final List<Posts> postsList = new ArrayList<>();
        List<Users> usersList = new ArrayList<>();
        final RecyclerView mRecyclerView = findViewById(R.id.relatedPostsRecyclerView);
        final PostsRecyclerAdapter mAdapter = new PostsRecyclerAdapter(postsList, usersList, VIEW_POST,
                Glide.with(this), this);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
        final ProgressBar mProgressBar = findViewById(R.id.relatedPostsProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        coMeth.getDb().collection(POSTS).limit(2).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e == null) {
                    assert queryDocumentSnapshots != null;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        postsList.clear();
                        for (DocumentChange document : queryDocumentSnapshots.getDocumentChanges()){
                            if (document.getType() == DocumentChange.Type.ADDED){
                                String relatedPostId = document.getDocument().getId();
                                Posts post = document.getDocument().toObject(Posts.class).withId(relatedPostId)
                                        .withId(relatedPostId);
                                if (!relatedPostId.equals(postId) && post.getImage_url() != null){
                                    postsList.add(post);
                                    mAdapter.notifyDataSetChanged();
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    }else{
                        mProgressBar.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.GONE);
                    }
                }else{
                    Log.w(CoMeth.TAG, "failed to load related posts: " + e.getMessage());
                    mProgressBar.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void checkConnectivity() {
        if (!coMeth.isConnected()) {
            coMeth.stopLoading(progressDialog);
            showSnack(getResources().getString(R.string.failed_to_connect_text));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.viewPostActivityButton:
                showPostStats();
                break;
            case R.id.viewPostLocationButton:
                openMap();
                break;
            case R.id.viewPostCatButton:
                showCats();
                break;
            case R.id.viewPostShareButton:
                sharePost();
                break;
            case R.id.viewPostImageView:
                openImage();
                break;
            case R.id.viewPostTagsButton:
                openTags();
                break;
            case R.id.viewPostUserButton:
                openPostUserPage();
                break;
            default:
                Log.d(TAG, "onClick: on details click view post activity");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent();
        handleRelatedPosts();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem editPost = menu.findItem(R.id.editMenuItem);
        MenuItem deletePost = menu.findItem(R.id.deleteMenuItem);
        MenuItem postStats = menu.findItem(R.id.postStatsMenuItem);
        MenuItem promotePost = menu.findItem(R.id.promoteMenuItem);

        if (coMeth.isConnected()) {
            if (coMeth.isLoggedIn()) {
                currentUserId = new CoMeth().getUid();

                //check if current user is the post user
                if (currentUserId.equals(postUserId)) {
                    editPost.setVisible(true);
                    deletePost.setVisible(true);
                    postStats.setVisible(true);
                    promotePost.setVisible(false); // TODO: 5/31/18 remove on launch
                } else {
                    editPost.setVisible(false);
                    deletePost.setVisible(false);
                    postStats.setVisible(false);
                    promotePost.setVisible(false); // TODO: 5/31/18 remove on launch
                }
            }
        } else {
            editPost.setVisible(false);
            deletePost.setVisible(false);
            postStats.setVisible(false);
            promotePost.setVisible(false); // TODO: 5/31/18 remove on launch
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_post_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reportMenuItem:
                if (coMeth.isLoggedIn()) {
                    showReportDialog();
                } else {
                    goToLogin(getString(R.string.login_to_report));
                }
                break;
            case R.id.postStatsMenuItem:
                showPostStats();
                break;
            case R.id.promoteMenuItem:
                goToPromote();
                break;
            case R.id.editMenuItem:
                goToEdit();
                break;
            case R.id.deleteMenuItem:
                deletePost(postId);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleBackBehaviour();
    }

    private void handleToolbar() {
        toolbar = findViewById(R.id.viewPostToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handleBackBehaviour();
    }

    private void openPostUserPage() {
        postUserId = post.getUser_id();
        Intent goToUserPageIntent = new Intent(
                ViewPostActivity.this, UserPageActivity.class);
        goToUserPageIntent.putExtra(USER_ID, postUserId);
        startActivity(goToUserPageIntent);
    }

    private void goToPromote() {
        // TODO: 5/31/18 go to promote page
    }

    private void showPostStats() {
        AlertDialog.Builder statsBuilder = new AlertDialog.Builder(this);
        statsBuilder.setTitle(R.string.post_stats_text)
                .setIcon(getResources().getDrawable(R.drawable.ic_action_activity))
                .setMessage(
                        getResources().getString(R.string.likes_text) + ": " + post.getLikes() + "\n" +
                                getResources().getString(R.string.comments_text) + ": " + post.getComments() + "\n" +
                                getResources().getString(R.string.views_text) + ": " + post.getViews() + "\n" +
                                getString(R.string.feed_views_text) + ": " + post.getFeed_views()
                )
                .setPositiveButton(getResources().getString(R.string.ok_text),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    private void showReportDialog() {
        final AlertDialog.Builder reportBuilder = new AlertDialog.Builder(ViewPostActivity.this);
        reportBuilder.setTitle(getString(R.string.report_text))
                .setIcon(R.drawable.ic_action_red_flag)
                .setMultiChoiceItems(coMeth.reportList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if (isChecked) {
                            reportedItems.add(coMeth.reportListKey[which]);
                        } else if (reportedItems.contains(coMeth.reportListKey[which])) {
                            reportedItems.remove(coMeth.reportListKey[which]);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .setPositiveButton(getString(R.string.done_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        if (coMeth.isConnected()) {
                            showProgress(getString(R.string.submitting));
                            //create report details string
                            for (String item : reportedItems) {
                                reportDetailsString = reportDetailsString.concat(item + "\n");
                            }
                            //add post details to db
                            final Map<String, Object> reportMap = new HashMap<>();
                            reportMap.put(POST_ID, postId);
                            reportMap.put(TIMESTAMP, FieldValue.serverTimestamp());
                            getExistingFlags(reportMap);
                        } else {
                            dialog.dismiss();
                            showSnack(getString(R.string.failed_to_connect_text));
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void getExistingFlags(final Map<String, Object> reportMap) {
        coMeth.getDb()
                .collection(FLAGS + "/" + POSTS_DOC + "/" + POSTS).document(postId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful() && task.getResult().exists()) {

                            //get existing flags
                            flagsArray = (ArrayList<String>) task.getResult().get(FLAGS_NAME);
                            //update the flags
                            flagsArray.add(coMeth.getUid() + "\n" + reportDetailsString);
                            reportMap.put(FLAGS_NAME, flagsArray);
                            submitReport(reportMap);
                        } else {
                            if (!task.isSuccessful()) {
                                showSnack(getResources().getString(
                                        R.string.something_went_wrong_text));
                            }
                            if (!task.getResult().exists()) {
                                flagsArray.add(coMeth.getUid() + "\n" + reportDetailsString);
                                reportMap.put(FLAGS_NAME , flagsArray);
                                submitReport(reportMap);
                            }
                        }
                    }
                });
    }

    /**
     * submit the report submitted by user on a spam/ innapriproate post
     *
     * @param reportMap Map: the map with the report data
     * */
    private void submitReport(Map<String, Object> reportMap) {

        coMeth.getDb()
                .collection(FLAGS + "/" + POSTS_DOC + "/" + POSTS).document(postId)
                .set(reportMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            coMeth.stopLoading(progressDialog);
                            showConfirmReport();
                        } else {
                            coMeth.stopLoading(progressDialog);
                            showSnack(getString(R.string.report_submit_failed_text));
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }

                });
    }

    private void showConfirmReport() {
        AlertDialog.Builder reportSuccessBuilder =
                new AlertDialog.Builder(ViewPostActivity.this);
        reportSuccessBuilder.setTitle(getString(R.string.report_text))
                .setIcon(R.drawable.ic_action_red_flag)
                .setMessage(R.string.report_submitted_text)
                .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void deletePost(final String postId) {
        String confirmDelMessage = getString(R.string.confirm_del_text);
        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(ViewPostActivity.this);
        deleteBuilder.setTitle(R.string.del_post_text)
                .setMessage(confirmDelMessage)
                .setIcon(getResources().getDrawable(R.drawable.ic_action_red_alert))
                .setPositiveButton(getResources().getString(R.string.delete_text),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        showProgress(getResources().getString(R.string.deleting_text));
                        deleteDocReference(postId);
                    }
                })
                .setNegativeButton(getString(R.string.cancel_text),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void deleteDocReference(final String postId) {
        coMeth.getDb().collection(POSTS).document(postId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //delete post reference on user subs
                        coMeth.getDb().collection(USERS + "/" + currentUserId + "/" +
                                SUBSCRIPTIONS + "/" + MY_POSTS_DOC + "/" + MY_POSTS)
                                .document(postId)
                                .delete()
                                .addOnSuccessListener(
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //successfully deleted doc and doc ref
                                                Log.d(TAG, "onSuccess: deleted doc ref");
                                            }
                                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: failed to " +
                                                "delete doc ref on user subs\n"
                                                + e.getMessage());
                                    }
                                });

                        Intent delResultIntent =
                                new Intent(ViewPostActivity.this, MainActivity.class);
                        delResultIntent.putExtra(CoMeth.ACTION, CoMeth.NOTIFY);
                        delResultIntent.putExtra(CoMeth.MESSAGE,
                                getString(R.string.del_success_text));
                        startActivity(delResultIntent);
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to delete post\n" + e.getMessage());
                        coMeth.stopLoading(progressDialog);
                        showSnack(getResources().getString(R.string.failed_to_delete_post)
                                + ": " + e.getMessage());
                    }
                });
    }

    private void goToEdit() {
        Intent editIntent = new Intent(ViewPostActivity.this, CreatePostActivity.class);
        editIntent.putExtra(getResources().getString(R.string.EDIT_POST_NAME), postId);
        startActivity(editIntent);
        finish();
    }

    private void populateFields() {
        handlePostActionButtons();
        setPostDetails();
    }

    private void handlePostActionButtons() {

        mCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComments();
            }
        });
        setLikesCount();
        setCommentsCount();
        if (coMeth.isConnected() && coMeth.isLoggedIn()) {
            final String currentUserId = coMeth.getUid();
            handleLikeButton(currentUserId);
            handleSaveButton(currentUserId);
        } else {
            handleButtonClickExceptions();
        }
    }

    private void setPostDetails() {
        coMeth.getDb().collection(POSTS).document(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //post exists
                            Log.d(TAG, "Post  exist");
                            post = Objects.requireNonNull(
                                    documentSnapshot.toObject(Posts.class)).withId(postId);

                            setTitle();
                            setDesc();
                            updateViews();
                            handleActivityButtonVisibility();
                            setTags();
                            setContactInfo();
                            setLocation();
                            setPrice();
                            setEventDate();
                            setTime();
                            setPostImage();
                            handlePostUserField();
                            setCategories();

                            coMeth.stopLoading(progressDialog);

                        } else {
                            coMeth.stopLoading(progressDialog);
                            goToMain(getString(R.string.post_not_found_text));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to set post details\n" +
                                e.getMessage());
                        onErrorPromptReload(e);
                    }
                });
    }

    private void onErrorPromptReload(@NonNull Exception e) {
        String errorMessage = getResources().getString(R.string.error_text) +
                e.getMessage();
        Snackbar.make(findViewById(R.id.view_post_activity_layout),
                errorMessage, Snackbar.LENGTH_LONG)
                .setAction(getResources().getString(R.string.reload_text),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showProgress(getResources()
                                        .getString(R.string.reloading_text));
                                populateFields();
                            }
                        })
                .setActionTextColor(getResources().getColor(R.color.secondaryLightColor))
                .show();
    }

    private void setCategories() {
        if (post.getCategories() != null) {

            Log.d(TAG, "onEvent: post has cats");
            String catString = "";
            catKeys = post.getCategories();
            ArrayList<String> categories = new ArrayList<>();
            for (int i = 0; i < catKeys.size(); i++) {
                //go through catKeys and get values
                String catValue = coMeth.getCatValue(catKeys.get(i));
                categories.add(catValue);
            }
            catArray.clear();
            for (int i = 0; i < categories.size(); i++) {
                catString = catString.concat(String.valueOf(categories.get(i)) + "\n");
                //update the catArrayList
                catArray.add(String.valueOf(categories.get(i)));
            }
            mCatsButton.setText(catString.trim());

        } else {
            mCatsButton.setVisibility(View.GONE);
            Log.d(TAG, "onEvent: post has no cats");
        }
    }

    private void handlePostUserField() {
        postUserId = post.getUser_id();
        //check db for user
        coMeth.getDb()
                .collection(USERS).document(postUserId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //user exists
                            Users user = documentSnapshot.toObject(Users.class);
                            //user exists
                            assert user != null;
                            if (user.getThumb() != null) {
                                //user has thumb
                                String userThumbDwnUrl = user.getThumb();
                                try {

                                    coMeth.setCircleImage(R.drawable.ic_action_person_placeholder,
                                            userThumbDwnUrl, userImage);
                                } catch (Exception e) {
                                    Log.d(TAG, "onSuccess: failed to load post image\n" +
                                            e.getMessage());
                                }

                            } else if (user.getImage() != null) {
                                //use has no thumb but has image
                                String userImageDwnUrl = user.getImage();
                                try {
                                    coMeth.setCircleImage(R.drawable.ic_action_person_placeholder,
                                            userImageDwnUrl, userImage);
                                } catch (Exception e) {
                                    Log.d(TAG, "onSuccess: failed to set post image\n" +
                                            e.getMessage());
                                }

                            } else {
                                //user has no image or thumb
                                userImage.setImageDrawable(getResources().getDrawable(
                                                R.drawable.ic_action_person_placeholder));
                                Log.d(TAG, "onEvent: placeholder user image set");
                            }
                            if (user.getName() != null) {
                                String username = user.getName();
                                String userNameMessage = getString(R.string.posted_by_text) + "\n" + username;
                                mUserButton.setText(userNameMessage);
                            } else {
                                mUserButton.setVisibility(View.GONE);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get post details from db\n" +
                                e.getMessage());
                    }
                });
    }

    private void setPostImage() {
        postImageUri = post.getImage_url();
        String postThumbUrl = post.getThumb_url();
        if (postImageUri != null && postThumbUrl != null) {
            //if post has image show the image view
            postImage.setVisibility(View.VISIBLE);
            coMeth.setImage(R.drawable.appiconshadow, postImageUri, postThumbUrl,
                    postImage, Glide.with(this));
            postImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openImage();
                }
            });
        } else {
            postImage.setVisibility(View.GONE);
        }
    }

    private void setTime() {
        if (post.getTimestamp() != null) {
            long millis = post.getTimestamp().getTime();
            String dateString = coMeth.processPostDate(millis);
            String date = getString(R.string.posted_text) + ":\n" + dateString;
            mTimeButton.setText(date);
        }
    }

    private void setEventDate() {
        if (post.getEvent_date() != null) {
            long eventDate = post.getEvent_date().getTime();
            String eventDateString = DateFormat.format("EEE, MMM d, 20yy",
                    new Date(eventDate)).toString();
            mEventDateButton.setText(eventDateString);
        } else {
            mEventDateButton.setVisibility(View.GONE);
        }
        if (post.getEvent_end_date() != null){
            long eventEndDate = post.getEvent_end_date().getTime();
            String eventDateString = DateFormat.format("EEE, MMM d, 20yy",
                    new Date(eventEndDate)).toString();
            mEventEndDateButton.setText(eventDateString);
            mEventEndDateButton.setVisibility(View.VISIBLE);
            mToTextView.setVisibility(View.VISIBLE);
        }else{
            mEventEndDateButton.setVisibility(View.GONE);
            mToTextView.setVisibility(View.GONE);
        }
    }

    private void setPrice() {
        String price = post.getPrice();
        if (price != null && !price.isEmpty()) {
            mPriceButton.setText(price);
        } else {
            mPriceButton.setVisibility(View.GONE);
        }
    }

    private void setLocation() {
        ArrayList<String> locationArray = post.getLocation();
        String locationString = "";

        if (locationArray != null && locationArray.size() > 0) {
            for (int i = 0; i < locationArray.size(); i++) {
                locationString = locationString.concat(locationArray.get(i) + "\n");
            }
            if (!locationString.trim().isEmpty()) {
                mLocationButton.setText(locationString.trim());
            }else{
                mLocationButton.setVisibility(View.GONE);
            }
        } else {
            mLocationButton.setVisibility(View.GONE);
        }
    }

    private void setContactInfo() {
        ArrayList<String> contactArray = post.getContact_details();
        if (contactArray != null) {
            String contactString = "";
            for (int i = 0; i < contactArray.size(); i++) {
                //set the first item
                contactString = contactString.concat(contactArray.get(i) + "\n");
            }
            mContactButton.setText(contactString.trim());
        } else {
            //hide contact details field
            mContactButton.setVisibility(View.GONE);
        }
    }

    private void setTags() {
        tags = post.getTags();
        if (tags != null && !tags.isEmpty()) {
            String tagsString = "";
            for (int i = 0; i < tags.size(); i++) {
                tagsString = tagsString.concat("#" + tags.get(i) + " ");
            }
            mTagsButton.setText(tagsString.trim());
        } else {
            mTagsButton.setVisibility(View.GONE);
        }
    }

    private void handleActivityButtonVisibility() {
        if (coMeth.isLoggedIn() &&
                coMeth.getUid().equals(post.getUser_id())) {
            mActivityButton.setVisibility(View.VISIBLE);
        } else {
            //viewer has no credentials
            //hide activity button
            mActivityButton.setVisibility(View.GONE);
        }
    }

    private void setTitle() {
        String title = post.getTitle();
        if (title != null){
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText(title);
            Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        }else{
            titleTextView.setVisibility(View.GONE);
        }
    }

    private void setDesc() {
        String desc = post.getDesc();
        if (desc != null){
            descTextView.setVisibility(View.VISIBLE);
            descTextView.setText(desc);
        }else{
            descTextView.setVisibility(View.GONE);
        }
    }

    private void handleButtonClickExceptions() {
        if (!coMeth.isConnected()) {
            //save button
            mShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToMain(getResources().getString(R.string.failed_to_connect_text));
                }
            });
            mLikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToMain(getResources().getString(R.string.failed_to_connect_text));
                }
            });
        }
        if (!coMeth.isLoggedIn()) {
            mSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToLogin(getResources().getString(R.string.login_to_save_text));
                }
            });
            mLikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToLogin(getResources().getString(R.string.login_to_like));
                }
            });
        }
    }

    private void handleSaveButton(final String currentUserId) {
        coMeth.getDb()
                .collection(POSTS + "/" + postId + "/" + SAVES)
                .document(currentUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(
                            DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e == null) {
                            //update the save button real time
                            if (documentSnapshot.exists()) {
                                mSaveButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        unsavePost(currentUserId);
                                    }
                                });
                            } else {
                                mSaveButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        savePost(currentUserId);
                                    }
                                });
                            }
                        }else{
                            Log.d(TAG, "onEvent: error on handling save button " +
                                    e.getMessage());
                        }
                    }
                });
    }

    private void handleLikeButton(final String currentUserId) {
        coMeth.getDb().collection(POSTS + "/" + postId + "/" + LIKES_COL)
                .document(currentUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot,
                                        FirebaseFirestoreException e) {

                        if (e == null) {
                            //update the like button real time
                            if (documentSnapshot.exists()) {
                                mLikeButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        unlikePost(currentUserId);
                                    }
                                });
                            } else {
                                mLikeButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        likePost(currentUserId);
                                    }
                                });
                            }
                        }else{
                            Log.d(TAG, "onEvent: error in handling likes " + e.getMessage());
                        }
                    }
                });
    }

    private void setCommentsCount() {
        coMeth.getDb()
                .collection(POSTS + "/" + postId + "/" + COMMENTS_COLL)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots,
                                        FirebaseFirestoreException e) {
                        if (e == null) {
                            if (!queryDocumentSnapshots.isEmpty()) {

                                int numberOfComments = queryDocumentSnapshots.size();
                                mCommentsButton.setText(String.valueOf(numberOfComments));
                                comments = numberOfComments;
                                updatePostComments(comments);
                            }
                        }else{
                            Log.d(TAG, "onEvent: error on setting comments count " +
                                    e.getMessage());
                        }
                    }
                });
    }

    private void setLikesCount() {
        coMeth.getDb()
                .collection(POSTS + "/" + postId + "/" + LIKES_COL)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots,
                                        FirebaseFirestoreException e) {
                        if (e == null) {
                            //check if exits
                            if (!queryDocumentSnapshots.isEmpty()) {
                                int likes = queryDocumentSnapshots.getDocuments().size();
                                mLikeButton.setText(String.valueOf(likes));
                                ViewPostActivity.this.likes = likes;
                                //update post likes
                                updatePostLikes(ViewPostActivity.this.likes);
                            }
                        }else{
                            Log.d(TAG, "onEvent: error on setting likes count " +
                                    e.getMessage());
                        }
                    }
                });
    }

    private void openTags() {
        AlertDialog.Builder tagsBuilder =
                new AlertDialog.Builder(ViewPostActivity.this);
        tagsBuilder.setTitle(getString(R.string.tags_text))
                .setIcon(getResources().getDrawable(R.drawable.ic_action_tags))
                .setItems(tags.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent searchTagIntent = new Intent(
                                ViewPostActivity.this, SearchableActivity.class);
                        searchTagIntent.putExtra(TAG_NAME, tags.get(which));
                        startActivity(searchTagIntent);
                    }
                })
                .show();
    }

    private void openImage() {
        Intent openImageIntent = new Intent(
                ViewPostActivity.this, ViewImageActivity.class);
        openImageIntent.putExtra(IMAGE_URL, postImageUri);
        startActivity(openImageIntent);
    }

    /**
     * update the number of comments on a post
     * @param comments the number of comments in post
     */
    private void updatePostComments(int comments) {
        Map<String, Object> commentsMap = new HashMap<>();
        commentsMap.put(COMMENTS_DOC, comments);
        coMeth.getDb()
                .collection(POSTS).document(postId).update(commentsMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: post comments updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update post comments\n" +
                                e.getMessage());
                    }
                });
    }

    /**
     * update the number of likes of post
     *
     * @param likes the number of likes of post
     */
    private void updatePostLikes(int likes) {
        //create a Map
        Map<String, Object> likesMap = new HashMap<>();
        likesMap.put("likes", likes);
        coMeth.getDb().collection(POSTS).document(postId).update(likesMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: likes updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update post likes\n" +
                                e.getMessage());
                    }
                });
    }

    /**
     * save current post to current user
     * @param currentUserId the postId of post to be saved
     * */
    private void savePost(String currentUserId) {
        Map<String, Object> savesMap = new HashMap<>();
        savesMap.put("timestamp", FieldValue.serverTimestamp());
        coMeth.getDb().collection(POSTS + "/" + postId + "/" + SAVES)
                .document(currentUserId)
                .set(savesMap);
        showSaveSnack(getString(R.string.added_to_saved_text));
    }

    private void unsavePost(String currentUserId) {
        coMeth.getDb().collection(POSTS + "/" + postId + "/" + SAVES)
                .document(currentUserId)
                .delete();
    }

    private void likePost(String currentUserId) {
        Map<String, Object> likesMap = new HashMap<>();
        likesMap.put("timestamp", FieldValue.serverTimestamp());
        coMeth.getDb()
                .collection(POSTS + "/" + postId + "/" + LIKES_COL)
                .document(currentUserId).set(likesMap);

        //notify subscribers
        String notifType = "likes_updates";
        new Notify().execute(notifType, postId);
        Log.d(TAG, "onComplete: notification sent");
    }

    private void unlikePost(String currentUserId) {
        coMeth.getDb()
                .collection(POSTS +"/" + postId + "/" + LIKES_COL)
                .document(currentUserId).delete();
    }

    private void openComments() {
        Intent commentsIntent = new Intent(
                ViewPostActivity.this, CommentsActivity.class);
        commentsIntent.putExtra(POST_ID, postId);
        commentsIntent.putExtra(SOURCE, VIEW_POST);
        startActivity(commentsIntent);
    }

    private void shareDynamicLink(String postUrl) {
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(postUrl))
                .setDynamicLinkDomain(getString(R.string.dynamic_link_domain))
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(coMeth.minVerCode)
                        .setFallbackUrl(Uri.parse(getString(R.string.playstore_url)))
                        .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(titleTextView.getText().toString())
                                .setDescription(descTextView.getText().toString())
                                .setImageUrl(Uri.parse(getPostImageUrl()))
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            Log.d(TAG, "onComplete: short link is: " + shortLink);

                            //show share dialog
                            String postTitle = titleTextView.getText().toString();
                            String fullShareMsg = postTitle + "\n" +
                                    shortLink;
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                            shareIntent.putExtra(Intent.EXTRA_TEXT, fullShareMsg);
                            coMeth.stopLoading(progressDialog);
                            startActivity(Intent.createChooser(shareIntent,
                                    getString(R.string.share_with_text)));
                        } else {
                            Log.d(TAG, "onComplete: " +
                                    "\ncreating short link task failed\n" +
                                    task.getException());
                            coMeth.stopLoading(progressDialog);
                            showSnack(getString(R.string.failed_to_share_text));
                        }
                    }
                });
    }

    /**
     * Checks if the post has an image
     *
     * @return String image download url
     * if post has image returns post image url
     * else returns the default app icon download url
     */
    private String getPostImageUrl() {
        if (postImageUri != null) {
            return postImageUri;
        } else {
            return getString(R.string.app_icon_url);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(POST_ID)) {
                if (intent.getBooleanExtra(IS_NEW_POST, false)) {
                    postId = intent.getStringExtra(POST_ID);
                    showShareNewPostDialog();
                } else {
                    postId = intent.getStringExtra(POST_ID);
                    Log.d(TAG, "postId is: " + postId);
                }
            } else {
                postId = handleDeepLinks(intent);
            }
        } else {
            goToMain(getString(R.string.something_went_wrong_text));
        }
        populateFields();

    }

    private void showShareNewPostDialog() {
        AlertDialog.Builder newPostBuilder = new AlertDialog.Builder(this);
        newPostBuilder.setTitle(getResources().getString(R.string.post_ready_text))
                .setMessage(getResources().getString(R.string.new_post_message_text))
                .setIcon(getResources().getDrawable(R.drawable.ic_action_new_post_notif))
                .setPositiveButton(getResources().getString(R.string.share_text),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sharePost();
                            }
                        })
                .show();
    }

    private void updateViews() {
        int views = post.getViews();
        if (!coMeth.isLoggedIn() || (coMeth.isLoggedIn() && !coMeth.getUid().equals(post.getUser_id()))) {
            //update view
            views = post.getViews() + 1;
        }
        //update activity
        final int activity = views + post.getLikes() + post.getComments() + post.getFeed_views();
        Map<String, Object> viewsMap = new HashMap<>();
        viewsMap.put(CoMeth.VIEWS, views);
        viewsMap.put(CoMeth.ACTIVITY, activity);
        coMeth.getDb().collection(POSTS).document(postId).update(viewsMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mActivityButton.setText(String.valueOf(activity));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update views\n" + e.getMessage());
                    }
                });

    }

    private void goToMain(String message) {
        Intent goToMainIntent = new Intent(ViewPostActivity.this, MainActivity.class);
        goToMainIntent.putExtra(getString(R.string.ACTION_NAME), getString(R.string.notify_value_text));
        goToMainIntent.putExtra(getString(R.string.MESSAGE_NAME), message);
        startActivity(goToMainIntent);
        finish();
    }

    private String handleDeepLinks(Intent intent) {

        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();

        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            String postUrl = String.valueOf(appLinkData);
            int endOfUrlHead = getResources().getString(R.string.fursa_url_post_head).length();
            postId = postUrl.substring(endOfUrlHead);
            Log.i(TAG, "incomingUrl is " + postId);
        }
        return postId;
    }

    public void handleBackBehaviour(){
        Intent intent = getIntent();
        if (intent != null){
            String appLinkAction = intent.getAction();
            Uri appLinkData = intent.getData();
            if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToMain();
                    }
                });
            }else{
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        }
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showProgress(String message) {
        progressDialog = new ProgressDialog(ViewPostActivity.this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.view_post_activity_layout),
                message, Snackbar.LENGTH_LONG).show();
    }

    private void showSaveSnack(String message) {
        Snackbar.make(findViewById(R.id.view_post_activity_layout),
                message, Snackbar.LENGTH_LONG)
                .setAction(R.string.see_list_text, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent goToSavedIntent = new Intent(
                                ViewPostActivity.this, MainActivity.class);
                        goToSavedIntent.putExtra(getString(R.string.ACTION_NAME),
                                getString(R.string.GOTO_VAL));
                        goToSavedIntent.putExtra(getString(R.string.DESTINATION_NAME),
                                getString(R.string.saved_value_text));
                        startActivity(goToSavedIntent);
                        finish();
                    }
                })
                .show();
    }

    private void goToLogin(String message) {
        Intent loginIntent = new Intent(ViewPostActivity.this, LoginActivity.class);
        loginIntent.putExtra(getString(R.string.MESSAGE_NAME), message);
        startActivity(loginIntent);

    }

    private void sharePost() {
        Log.d(TAG, "Sharing post");
        //get post url
        showProgress(getString(R.string.loading_text));
        String postUrl = getResources().getString(R.string.fursa_url_post_head) + postId;
        shareDynamicLink(postUrl);
    }

    private void openMap() {
        //launch google maps and search for location
        String location = mLocationButton.getText().toString();
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void showCats() {
        //open alert dialog
        AlertDialog.Builder catDialogBuilder = new AlertDialog.Builder(ViewPostActivity.this);
        catDialogBuilder.setTitle(getResources().getString(R.string.categories_text))
                .setIcon(getResources().getDrawable(R.drawable.ic_action_categories))
                .setItems(catArray.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //open view cat activity
                        Intent catIntent = new Intent(
                                ViewPostActivity.this, ViewCategoryActivity.class);
                        catIntent.putExtra("category", catKeys.get(which));
                        startActivity(catIntent);
                        finish();

                        Log.d(TAG, "onClick: \nuser selected cat is: " + catKeys.get(which));
                    }
                })
                .show();
    }
}
