package com.nyayozangu.labs.fursa.activities.tags.adapter;

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
import com.nyayozangu.labs.fursa.activities.main.SearchableActivity;
import com.nyayozangu.labs.fursa.activities.tags.Tags;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.util.List;


public class TagsRecyclerAdapter extends
        RecyclerView.Adapter<TagsRecyclerAdapter.ViewHolder> {

    private static final String TAG = "Sean";
    public List<Tags> tagsList;
    public Context context;
    //common methods
    private CoMeth coMeth = new CoMeth();

    public TagsRecyclerAdapter(List<Tags> tagsList) {

        //store received posts
        this.tagsList = tagsList;
//        this.tagId = tagId;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");

        //inflate the viewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tags_tab_list_item, parent, false);
        context = parent.getContext();

        return new TagsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        Log.d(TAG, "onBindViewHolder: tags recycler adapter");
        //get tags
        final String title = tagsList.get(position).getTitle();
        int postCount = tagsList.get(position).getPost_count();
        holder.setTitle(title);
        holder.setPostCount(postCount);

        //set click listeners
        holder.tagsItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent goSearchTagIntent = new Intent(context, SearchableActivity.class);
                goSearchTagIntent.putExtra(context.getResources().getString(R.string.TAG_NAME), title);
                context.startActivity(goSearchTagIntent);
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

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            tagsItemView = mView.findViewById(R.id.tagsTabItemView);
            tagTitleView = mView.findViewById(R.id.tagTitleTextView);
            postCountView = mView.findViewById(R.id.postCountTextView);
        }

        public void setTitle(String title) {
            tagTitleView.setText(title);
        }

        public void setPostCount(int count) {
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
