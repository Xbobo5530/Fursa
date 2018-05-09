package com.nyayozangu.labs.fursa.activities.comments.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.comments.models.Comments;
import com.nyayozangu.labs.fursa.activities.settings.LoginActivity;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 *
 * Created by Sean on 4/9/18.
 */

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {


    private static final String TAG = "Sean";
    //member variables for storing posts
    public List<Comments> commentsList;
    public String postId;
    public Context context;
    //common methods
    private CoMeth coMeth = new CoMeth();
    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    //reorts users
    private ArrayList flags;
    private String reportDetails;
    private String reportedItemsString;
    private ProgressDialog progressDialog;


    //empty constructor for receiving the posts
    public CommentsRecyclerAdapter(List<Comments> commentsList, String postId) {

        //store received posts
        this.commentsList = commentsList;
        this.postId = postId;

    }


    @NonNull
    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the viewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        flags = new ArrayList<String>();
        reportedItemsString = "";

        return new CommentsRecyclerAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final CommentsRecyclerAdapter.ViewHolder holder, final int position) {
        Log.d(TAG, "at onBindViewHolder");

        holder.setIsRecyclable(false);

        //set comment
        String comment = commentsList.get(position).getComment();
        Log.d(TAG, "onBindViewHolder: \ncomment is: " + comment);
        holder.setComment(comment);

        //set on click for comment item
        holder.commentItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open report alert dialog
                AlertDialog.Builder reportBuilder = new AlertDialog.Builder(context);
                reportBuilder.setTitle("Comment Options")
                        .setIcon(context.getDrawable(R.drawable.ic_action_comment))
                        .setItems(getCommentReportOptionsList(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (coMeth.isConnected() && coMeth.isLoggedIn()) {
                                    switch (getCommentReportOptionsList()[which]) {

                                        case "Report":
                                            reportComment(holder, commentsList.get(position));
                                            break;
                                        case "Delete":
                                            // TODO: 5/6/18 handle delete comment
//                                            deleteComment();
                                        default:
                                            Log.d(TAG, "onClick: comments select items default");

                                    }

                                } else {

                                    if (!coMeth.isConnected()) {

                                        // TODO: 5/6/18 handle not connect
                                        // show snack

                                    }
                                    if (!coMeth.isLoggedIn()) {

                                        //show login alert
                                        // TODO: 5/6/18 show login alert
                                    }
                                }

                            }
                        })
                        .show();
            }
        });

        //set name
        String userId = commentsList.get(position).getUser_id();
        db.collection("Users").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                //check if user exists
                if (documentSnapshot.exists()) {

                    // TODO: 4/16/18 test when user is not available


                    try {
                        //user exists
                        String username = documentSnapshot.get("name").toString();

                        //set name to name textView
                        holder.setUsername(username);

                        //set user image
                        try {
                            String userImageDownloadUrl = documentSnapshot.get("image").toString();
                            holder.setImage(userImageDownloadUrl);
                        } catch (NullPointerException userImageException) {

                            //user image is null
                            Log.e(TAG, "onEvent: ", userImageException);


                        }

                    } catch (NullPointerException userInfoErrorException) {

                        Log.e(TAG, "onEvent: \nuserInfoErrorException: " +
                                userInfoErrorException.getMessage(), userInfoErrorException);

                    }

                } else {

                    //user does not exist
                    Log.d(TAG, "onEvent: user does not exist");


                }
            }
        });

    }

    private void reportComment(@NonNull final CommentsRecyclerAdapter.ViewHolder holder, final Comments comment) {

        //create report map
        final Map<String, Object> reportMap = new HashMap<>();
        reportMap.put("commentId", comment.CommentId);
        //get users who have already reported comment
        reportMap.put("comment", comment.getComment());
        reportMap.put("commentUserId", comment.getUser_id());
        reportMap.put("postId", postId);
        reportMap.put("commentTimestamp", comment.getTimestamp());
        reportMap.put("timestamp", FieldValue.serverTimestamp());

        final ArrayList<String> reportedItems = new ArrayList<>();

        //show report flags list
        AlertDialog.Builder reportFlagsBuilder = new AlertDialog.Builder(context);
        reportFlagsBuilder.setTitle(context.getString(R.string.report_comment_text))
                .setIcon(context.getDrawable(R.drawable.ic_action_red_flag))
                .setMultiChoiceItems(coMeth.reportList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        //what happens when an item is checked
                        if (isChecked) {

                            // If the user checked the item, add it to the selected items
                            reportedItems.add(coMeth.reportListKey[which]);

                        } else if (reportedItems.contains(coMeth.reportListKey[which])) {

                            // Else, if the item is already in the array, remove it
                            reportedItems.remove(coMeth.reportListKey[which]);

                        }

                    }
                })
                .setPositiveButton(context.getString(R.string.done_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        //show loading
                        showProgress(context.getString(R.string.submitting));
                        //update reported items string
                        for (String item : reportedItems) {
                            reportedItemsString = reportedItemsString.concat(item + "\n");
                        }
                        //get reporters list
                        coMeth.getDb()
                                .collection("Flags/comments/Comments")
                                .document(comment.CommentId)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful() && task.getResult().exists()) {

                                            flags = (ArrayList) task.getResult().get("flags");
                                            //update reporters list
                                            reportDetails = coMeth.getUid() + "\n" + reportedItemsString.trim();
                                            flags.add(reportDetails);
                                            reportMap.put("flags", flags);
                                            submitReport(holder, comment, reportMap);

                                        } else {

                                            if (!task.isSuccessful()) {
                                                dialog.cancel();
                                                showSnack(holder, context.getString(R.string.something_went_wrong_text));
                                                Log.d(TAG, "onComplete: task failed");

                                            }
                                            if (!task.getResult().exists()) {

                                                //update reporters list
                                                reportDetails = coMeth.getUid() + "\n" + reportedItemsString.trim();
                                                flags.add(reportDetails);
                                                reportMap.put("flags", flags);
                                                submitReport(holder, comment, reportMap);
                                            }

                                        }

                                    }
                                });



                    }
                })
                .setNegativeButton(context.getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .show();
    }

    private void submitReport(
            @NonNull final CommentsRecyclerAdapter.ViewHolder holder,
            Comments comment,
            Map<String, Object> reportMap) {

        Log.d(TAG, "submitReport: comment id is " + comment.CommentId);
        coMeth.getDb()
                .collection("Flags")
                .document("comments")
                .collection("Comments")
                .document(comment.CommentId)
                .set(reportMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            //stop loading
                            coMeth.stopLoading(progressDialog);
                            //alert user
                            showReportConfirmationDialog();

                        } else {

                            coMeth.stopLoading(progressDialog);
                            showSnack(holder, "Failed to report comment");
                            Log.d(TAG, "onComplete: failed to report comment " + task.getException());

                        }


                    }
                });
    }

    //show progress
    private void showProgress(String message) {

        Log.d(TAG, "at showProgress\n message is: " + message);
        //construct the dialog box
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.show();

    }

    private void showReportConfirmationDialog() {
        //show an alert dialog to inform user task is successful
        AlertDialog.Builder reportCommentSuccessBuilder = new AlertDialog.Builder(context);
        reportCommentSuccessBuilder.setTitle(context.getString(R.string.report_comment_text))
                .setIcon(context.getDrawable(R.drawable.ic_action_red_flag))
                .setMessage("Report successifully submitted for review")
                .setPositiveButton(context.getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .show();
    }

    //get comment report options list
    private String[] getCommentReportOptionsList() {

        // TODO: 5/6/18 handle admin actions and user actions
        // TODO: 5/6/18 handle delete comment and del comment permissions

        return new String[]{

                context.getString(R.string.report_text)

        };


    }

    private void showSnack(@NonNull CommentsRecyclerAdapter.ViewHolder holder, String message) {
        Snackbar.make(holder.mView.findViewById(R.id.postLayout),
                message, Snackbar.LENGTH_LONG).show();
    }

    private void goToLogin() {
        //take user to the login page
        Log.d(TAG, "at goToLogin");
        context.startActivity(new Intent(context, LoginActivity.class));

    }

    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        android.support.v7.app.AlertDialog.Builder loginAlertBuilder = new android.support.v7.app.AlertDialog.Builder(context);
        loginAlertBuilder.setTitle(context.getString(R.string.login_text))
                .setIcon(context.getDrawable(R.drawable.ic_action_red_alert))
                .setMessage(context.getString(R.string.not_logged_in_text) + message)
                .setPositiveButton(context.getString(R.string.login_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send user to login activity
                        goToLogin();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //initiate view
        private View mView;

        //initiate items
        private ConstraintLayout commentItemView;
        private CircleImageView userImageView;
        private TextView commentTextView;
        private TextView usernameTextView;


        public ViewHolder(View itemView) {
            super(itemView);
            //use mView to populate other methods
            mView = itemView;

            commentItemView = mView.findViewById(R.id.commentsItemLayout);
            userImageView = mView.findViewById(R.id.commentUserImage);
            commentTextView = mView.findViewById(R.id.commentTextView);
            usernameTextView = mView.findViewById(R.id.commentUsernameTextView);

        }

        public void setImage(String imageUrl) {

            userImageView = mView.findViewById(R.id.commentUserImage);

            //add the placeholder image
            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.ic_action_image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOptions).load(imageUrl).into(userImageView);

        }

        public void setUsername(String username) {

            usernameTextView = mView.findViewById(R.id.commentUsernameTextView);
            usernameTextView.setText(username);

        }

        public void setComment(String comment) {

            commentTextView = mView.findViewById(R.id.commentTextView);
            commentTextView.setText(comment);

        }

    }
}
