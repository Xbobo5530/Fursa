package com.nyayozangu.labs.fursa.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.ViewCategoryActivity;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.models.Tags;

import java.util.List;

import static com.nyayozangu.labs.fursa.helpers.CoMeth.TAG_VAL;


public class TagsRecyclerAdapter extends
        RecyclerView.Adapter<TagsRecyclerAdapter.ViewHolder> {

    private static final String TAG = "Sean";
    private List<Tags> tagsList;
    public Context context;
    public TagsRecyclerAdapter(List<Tags> tagsList) {
        //store received posts
        this.tagsList = tagsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the viewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tags_tab_list_item, parent, false);
        context = parent.getContext();
        return new TagsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //get tags
        final String title = tagsList.get(position).getTitle();
        int postCount = tagsList.get(position).getPost_count();
        holder.setTitle(title);
        holder.setPostCount(postCount);

        //set click listeners
        holder.tagsItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent tagIntent = new Intent(context, ViewCategoryActivity.class);
                tagIntent.putExtra(TAG_VAL, title);
                context.startActivity(tagIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return tagsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //initiate view
        private View mView;

        private ConstraintLayout tagsItemView;
        private TextView tagTitleView, postCountView;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            tagsItemView = mView.findViewById(R.id.tagsTabItemView);
            tagTitleView = mView.findViewById(R.id.tagTitleTextView);
            postCountView = mView.findViewById(R.id.postCountTextView);
        }

        public void setTitle(String title) {
            tagTitleView.setText(title);
        }

        void setPostCount(int count) {
            String postCountText;
            if (count == 1) {
                postCountText = count + " " + context.getResources().getString(R.string.post_text);
            } else {
                postCountText = count + " " + context.getResources().getString(R.string.posts_text);
            }
            postCountView.setText(postCountText);
        }
    }
}
