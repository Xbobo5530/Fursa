package com.nyayozangu.labs.fursa;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Sean on 4/4/18.
 * <p>
 * Adapter class for the Recycler view
 */

public class PostsRecyclerAdapter extends RecyclerView.Adapter<PostsRecyclerAdapter.ViewHolder> {


    //member vatiables for storing posts
    public List<Posts> postsList;

    //empty constructor for receiving the posts
    public PostsRecyclerAdapter(List<Posts> postsList) {

        //store received posts
        this.postsList = postsList;
    }

    @NonNull
    @Override
    public PostsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the viewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsRecyclerAdapter.ViewHolder holder, int position) {

        //set the data after the viewHolder has been bound
        String descData = postsList.get(position).getDesc();
        //set the data to the view holder
        holder.setDesc(descData);


    }

    @Override
    public int getItemCount() {
        //the number of posts
        return postsList.size();
    }

    //implement the viewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {


        //initiate view
        private View mView;

        //initiate elements in the view holder
        private TextView descTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            //use mView to populate other methods
            mView = itemView;
        }

        //retrieves description on post
        public void setDesc(String descText) {

            descTextView = mView.findViewById(R.id.postDescTextView);
            descTextView.setText(descText);
        }
    }
}
