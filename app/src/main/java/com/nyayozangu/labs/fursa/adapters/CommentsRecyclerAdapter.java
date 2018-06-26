package com.nyayozangu.labs.fursa.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Comments;
import com.nyayozangu.labs.fursa.models.Users;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 *
 * Created by Sean on 4/9/18.
 */

public class CommentsRecyclerAdapter extends
        RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {


    private static final String TAG = "Sean";
    //member variables for storing posts
    public List<Comments> commentsList;
    public String postId;
    public Context context;
    //common methods
    private CoMeth coMeth = new CoMeth();
    //firebase auth


    //reorts users
    private ArrayList flags;
    private String reportDetails;
    private String reportedItemsString;
    private ProgressDialog progressDialog;
    private String[] reportOptionsList;
    private boolean isAdmin = false;
    private int lastPosition = -1;


    //empty constructor for receiving the posts
    public CommentsRecyclerAdapter(List<Comments> commentsList, String postId) {

        //store received posts
        this.commentsList = commentsList;
        this.postId = postId;

    }


    @NonNull
    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                 int viewType) {

        //inflate the viewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();

        flags = new ArrayList<String>();
        reportedItemsString = "";

        return new CommentsRecyclerAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final CommentsRecyclerAdapter.ViewHolder holder,
                                 final int position) {
        Log.d(TAG, "at onBindViewHolder");

        //set comment
        String comment = commentsList.get(position).getComment();
        final String commentId = commentsList.get(position).CommentId;
        Log.d(TAG, "onBindViewHolder: \ncomment is: " + comment);
        holder.setComment(comment);

        //set on click for comment item
//        holder.commentItemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (coMeth.isConnected() && coMeth.isLoggedIn()) {
//
//                    // TODO: 5/25/18 test report post options dialog fix
//                    showProgress(context.getResources().getString(R.string.loading_text));
//                    //open report alert dialog
//                    final String[] reportOptionsList = new String[]{
//                            context.getResources().getString(R.string.report_text)
//                    };
//                    /*getCommentReportOptionsList();*/
//
//                    AlertDialog.Builder reportBuilder = new AlertDialog.Builder(context);
//                    reportBuilder.setTitle("Comment Options")
//                            .setIcon(context.getResources().getDrawable(R.drawable.ic_action_comment))
//                            .setItems(reportOptionsList, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    switch (reportOptionsList[which]) {
//
//                                        case "Report":
//                                            reportComment(holder, commentsList.get(position));
//                                            break;
////                                        case "Delete":
////                                            deleteComment(holder, postId, commentId);
////                                            break;
//                                        default:
//                                            Log.d(TAG, "onClick: comments select items default");
//                                    }
//                                }
//                            });
//                    coMeth.stopLoading(progressDialog);
//                    reportBuilder.show();
//                } else {
//                    if (!coMeth.isConnected()) {
//                        showSnack(holder, context.getString(R.string.failed_to_connect_text));
//                    }
//                    if (!coMeth.isLoggedIn()) {
//                        Intent goToLoginIntent = new Intent(context, LoginActivity.class);
//                        goToLoginIntent.putExtra("message",
//                                context.getString(R.string.login_to_report));
//                        context.startActivity(goToLoginIntent);
//                    }
//                }
//            }
//        });

        //set name
        String userId = commentsList.get(position).getUser_id();
        coMeth.getDb()
                .collection("Users").document(userId).addSnapshotListener(
                new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                //check if user exists
                if (documentSnapshot.exists()) {

                    try {
                        //user exists
                        Users user = documentSnapshot.toObject(Users.class);
                        String username = user.getName();
                        //set name to name textView
                        holder.setUsername(username);
                        //set user image
                        try {
                            String userImageDownloadUrl = user.getImage();
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

        //set animation
//        setAnimation(holder.itemView, position);

    }

//    private void setAnimation(View viewToAnimate, int position) {
//        // If the bound view wasn't previously displayed on screen, it's animated
//        if (position > lastPosition) {
////            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
//            Animation animation = AnimationUtils.loadAnimation(context, R.anim.abc_fade_in);
//            viewToAnimate.startAnimation(animation);
//            lastPosition = position;
//        }
//    }

//    private void deleteComment(@NonNull final CommentsRecyclerAdapter.ViewHolder holder,
//                               final String postId,
//                               final String commentId) {
//        coMeth.getDb()
//                .collection("Posts/" + postId + "/Comments/")
//                .document(commentId)
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot.exists()) {
//                            coMeth.getDb()
//                                    .collection("Posts/" + postId + "/Comments/")
//                                    .document(commentId)
//                                    .delete();
//                            showSnack(holder, context.getString(R.string.del_success_text));
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        showSnack(holder, context.getString(R.string.something_went_wrong_text));
//                    }
//                });
//    }

//    private void reportComment(@NonNull final CommentsRecyclerAdapter.ViewHolder holder,
//                               final Comments comment) {
//
//        //create report map
//        final Map<String, Object> reportMap = new HashMap<>();
//        reportMap.put("commentId", comment.CommentId);
//        //get users who have already reported comment
//        reportMap.put("comment", comment.getComment());
//        reportMap.put("commentUserId", comment.getUser_id());
//        reportMap.put("postId", postId);
//        reportMap.put("commentTimestamp", comment.getTimestamp());
//        reportMap.put("timestamp", FieldValue.serverTimestamp());
//
//        final ArrayList<String> reportedItems = new ArrayList<>();
//
//        //show report flags list
//        AlertDialog.Builder reportFlagsBuilder = new AlertDialog.Builder(context);
//        reportFlagsBuilder.setTitle(context.getString(R.string.report_comment_text))
//                .setIcon(context.getResources().getDrawable(R.drawable.ic_action_red_flag))
//                .setMultiChoiceItems(coMeth.reportList, null,
//                        new DialogInterface.OnMultiChoiceClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//
//                        //what happens when an item is checked
//                        if (isChecked) {
//
//                            // If the user checked the item, add it to the selected items
//                            reportedItems.add(coMeth.reportListKey[which]);
//
//                        } else if (reportedItems.contains(coMeth.reportListKey[which])) {
//
//                            // Else, if the item is already in the array, remove it
//                            reportedItems.remove(coMeth.reportListKey[which]);
//
//                        }
//
//                    }
//                })
//                .setPositiveButton(context.getString(R.string.done_text), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(final DialogInterface dialog, int which) {
//
//                        //show loading
//                        showProgress(context.getString(R.string.submitting));
//                        //update reported items string
//                        for (String item : reportedItems) {
//                            reportedItemsString = reportedItemsString.concat(item + "\n");
//                        }
//                        //get reporters list
//                        getReportersList(dialog, comment, reportMap, holder);
//
//
//                    }
//                })
//                .setNegativeButton(context.getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        dialog.dismiss();
//
//                    }
//                })
//                .show();
//    }

    // TODO: 6/3/18 clean reports
//    private void getReportersList(final DialogInterface dialog,
//                                  final Comments comment,
//                                  final Map<String, Object> reportMap,
//                                  @NonNull final ViewHolder holder) {
//        coMeth.getDb()
//                .collection("Flags/comments/Comments")
//                .document(comment.CommentId)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                        if (task.isSuccessful() && task.getResult().exists()) {
//
//                            flags = (ArrayList) task.getResult().get("flags");
//                            //update reporters list
//                            reportDetails = coMeth.getUid() + "\n" + reportedItemsString.trim();
//                            flags.add(reportDetails);
//                            reportMap.put("flags", flags);
//                            submitReport(holder, comment, reportMap);
//
//                        } else {
//
//                            if (!task.isSuccessful()) {
//                                dialog.cancel();
//                                showSnack(holder,
//                                        context.getString(R.string.something_went_wrong_text));
//                                Log.d(TAG, "onComplete: task failed");
//
//                            }
//                            if (!task.getResult().exists()) {
//
//                                //update reporters list
//                                reportDetails = coMeth.getUid() + "\n" + reportedItemsString.trim();
//                                flags.add(reportDetails);
//                                reportMap.put("flags", flags);
//                                submitReport(holder, comment, reportMap);
//                            }
//
//                        }
//
//                    }
//                });
//    }

//    private void submitReport(
//            @NonNull final CommentsRecyclerAdapter.ViewHolder holder,
//            Comments comment,
//            Map<String, Object> reportMap) {
//
//        Log.d(TAG, "submitReport: comment id is " + comment.CommentId);
//        coMeth.getDb()
//                .collection("Flags")
//                .document("comments")
//                .collection("Comments")
//                .document(comment.CommentId)
//                .set(reportMap)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//
//                        if (task.isSuccessful()) {
//
//                            //stop loading
//                            coMeth.stopLoading(progressDialog);
//                            //alert user
//                            showReportConfirmationDialog();
//
//                        } else {
//
//                            coMeth.stopLoading(progressDialog);
//                            showSnack(holder, "Failed to report comment");
//                            Log.d(TAG, "onComplete: failed to report comment " +
//                                    task.getException());
//                        }
//                    }
//                });
//    }

    //show progress
//    private void showProgress(String message) {
//
//        Log.d(TAG, "at showProgress\n message is: " + message);
//        //construct the dialog box
//        progressDialog = new ProgressDialog(context);
//        progressDialog.setMessage(message);
//        progressDialog.show();
//
//    }

//    private void showReportConfirmationDialog() {
//        //show an alert dialog to inform user task is successful
//        AlertDialog.Builder reportCommentSuccessBuilder = new AlertDialog.Builder(context);
//        reportCommentSuccessBuilder.setTitle(context.getString(R.string.report_comment_text))
//                .setIcon(context.getResources().getDrawable(R.drawable.ic_action_red_flag))
//                .setMessage("Report successifully submitted for review")
//                .setPositiveButton(context.getString(R.string.ok_text), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        dialog.dismiss();
//
//                    }
//                })
//                .show();
//    }

    //get comment report options list
//    private String[] getCommentReportOptionsList() {
//
//        //get postUserId
//        coMeth.getDb()
//                .collection("Posts")
//                .document(postId)
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot.exists()) {
//                            Posts posts = documentSnapshot.toObject(Posts.class);
//                            String postUserId = posts.getUser_id();
//                            //generate report options
//                            if (postUserId.equals(coMeth.getUid())) {
//                                reportOptionsList = new String[]{
//                                        context.getString(R.string.report_text),
//                                        context.getString(R.string.delete_text)
//                                };
//                            } else {
//                                //normal user
//                                reportOptionsList = new String[]{
//                                        context.getString(R.string.report_text)
//                                };
//                            }
//                        } else {
//                            //post does not exist
//                            Log.d(TAG, "onSuccess: post does not exist");
//                            reportOptionsList = new String[]{
//                                    context.getString(R.string.report_text)
//                            };
//                        }
//                        coMeth.stopLoading(progressDialog);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "onFailure: getting post failed");
//                        reportOptionsList = new String[]{
//                                context.getString(R.string.report_text)
//                        };
//                    }
//                });
//        Log.d(TAG, "getCommentReportOptionsList: \nreport option list is: " +
//                reportOptionsList);
//        return reportOptionsList;
//    }

    /**
     * checks if current user is admin
     *
     * @return boolean true if user is admin
     * false if user is not admin
     */

//    private void showSnack(@NonNull CommentsRecyclerAdapter.ViewHolder holder, String message) {
//        Snackbar.make(holder.mView.findViewById(R.id.postLayout),
//                message, Snackbar.LENGTH_LONG).show();
//    }

//    private void goToLogin() {
//        //take user to the login page
//        Log.d(TAG, "at goToLogin");
//        context.startActivity(new Intent(context, LoginActivity.class));
//    }

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
        private TextView commentTextView, usernameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            //use mView to populate other methods
            mView = itemView;
            commentItemView = mView.findViewById(R.id.commentsItemLayout);
            userImageView = mView.findViewById(R.id.commentUserImage);
            commentTextView = mView.findViewById(R.id.commentTextView);
            usernameTextView = mView.findViewById(R.id.commentUsernameTextView);
        }

        // TODO: 6/4/18 hunt for error "you can not start a desctroyed activity"
        public void setImage(String imageUrl) {

            userImageView = mView.findViewById(R.id.commentUserImage);
            //add the placeholder image
            try {
                RequestOptions placeHolderOptions = new RequestOptions();
                placeHolderOptions.placeholder(R.drawable.ic_action_person_placeholder);
                Glide.with(context)
                        .applyDefaultRequestOptions(placeHolderOptions)
                        .load(imageUrl)
                        .into(userImageView);
            } catch (Exception e) {
                Log.d(TAG, "setImage: error " + e.getMessage());
            }
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
