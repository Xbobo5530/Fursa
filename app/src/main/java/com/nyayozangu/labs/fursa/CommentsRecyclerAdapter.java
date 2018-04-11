package com.nyayozangu.labs.fursa;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Sean on 4/9/18.
 */

class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {


    private static final String TAG = "Sean";
    //member variables for storing posts
    public List<Comments> commentsList;

    public Context context;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    //empty constructor for receiving the posts
    public CommentsRecyclerAdapter(List<Comments> commentsList) {

        //store received posts
        this.commentsList = commentsList;
    }


    @NonNull
    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the viewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();


        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        return new CommentsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsRecyclerAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "at onBindViewHolder");

        /*holder.setIsRecyclable(false);*/

        /*//get title from holder
        String titleData = postsList.get(position).getTitle();
        //set the post title
        holder.setTitle(titleData);*/

        // TODO: 4/9/18 set items

        //set comment
        String comment = commentsList.get(position).getComment();
        holder.setComment(comment);

        // TODO: 4/10/18 fix the null on setting time
        /*try {
            long millis = commentsList.get(position).getTimestamp().getTime();
            //convert millis to date time format
            String timeString = DateFormat.format("EEE, MMM d, ''yy - h:mm a", new Date(millis)).toString();
            holder.setTime(timeString);
        } catch (Exception e) {
            Log.d(TAG, "onBindViewHolder: error:" + e.getMessage());
        }*/

        //set image
        // TODO: 4/10/18 set image


    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //initiate view
        private View mView;

        //initiate items
        private CircleImageView userImageView;
        private TextView commentTextView;
        private TextView timeTextView;


        public ViewHolder(View itemView) {
            super(itemView);
            //use mView to populate other methods
            mView = itemView;

            userImageView = mView.findViewById(R.id.commentUserImage);
            commentTextView = mView.findViewById(R.id.commentTextView);
            timeTextView = mView.findViewById(R.id.commentTimeTextView);


        }

        public void setComment(String comment) {

            commentTextView = mView.findViewById(R.id.commentTimeTextView);
            commentTextView.setText(comment);

        }

        public void setImage(String imageUrl) {

            userImageView = mView.findViewById(R.id.commentUserImage);

            //add the placeholder image
            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.ic_action_image_placeholder);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOptions).load(imageUrl).into(userImageView);
        }

        public void setTime(String time) {

            // TODO: 4/4/18 show different time status  like one minute ago ++

            timeTextView = mView.findViewById(R.id.commentTimeTextView);
            timeTextView.setText(time);

        }

    }
}
