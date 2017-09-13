package com.basilgregory.onamsample.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.basilgregory.onamsample.R;
import com.basilgregory.onamsample.entities.Comment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by donpeter on 9/13/17.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {


    private List<Comment> comments;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView comment,commentedAt,commentOwnerName;

        public ViewHolder(View view) {
            super(view);
            comment = (TextView) view.findViewById(R.id.comment);
            commentedAt = (TextView) view.findViewById(R.id.commentedAt);
            commentOwnerName = (TextView) view.findViewById(R.id.commentOwnerName);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CommentsAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.comment.setText(comments.get(position).getComment());
        Calendar calendar  = Calendar.getInstance();
        calendar.setTimeInMillis(comments.get(position).getCreated_at());
        SimpleDateFormat format = new SimpleDateFormat("MMM, dd hh:mm a");
        String formatedDate = format.format(calendar.getTime());
        holder.commentedAt.setText(formatedDate);
        if (comments.get(position).getUser() == null) return;
        holder.commentOwnerName.setText(comments.get(position).getUser().getName());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (comments == null) return 0;
        return comments.size();
    }
}
