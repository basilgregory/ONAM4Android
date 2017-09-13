package com.basilgregory.onamsample.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.basilgregory.onamsample.R;
import com.basilgregory.onamsample.entities.Post;

import java.util.List;

/**
 * Created by donpeter on 9/12/17.
 */

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {


    private List<Post> posts;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView postTitle,postDescription,postOwnerName;

        public ViewHolder(View view) {
            super(view);
            postTitle = (TextView) view.findViewById(R.id.postTitle);
            postDescription = (TextView) view.findViewById(R.id.postDescription);
            postOwnerName = (TextView) view.findViewById(R.id.postOwnerName);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostsAdapter(List<Post> posts) {
        this.posts = posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.postTitle.setText(posts.get(position).getTitle());
        holder.postDescription.setText(posts.get(position).getPost());
        if (posts.get(position).getUser() == null) return;
        holder.postOwnerName.setText(posts.get(position).getUser().getName());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (posts == null) return 0;
        return posts.size();
    }


}
