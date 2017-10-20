package com.basilgregory.onamsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.basilgregory.onam.android.Entity;
import com.basilgregory.onamsample.adapter.CommentsAdapter;
import com.basilgregory.onamsample.entities.Comment;
import com.basilgregory.onamsample.entities.Post;
import com.basilgregory.onamsample.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by donpeter on 9/13/17.
 */

public class PostDetailsActivity extends AppCompatActivity {
    CommentsAdapter commentsAdapter;
    TextView postTitle,postDescription,userName,userBio;
    RecyclerView comments;
    Post post;
    FloatingActionButton addComment;
    Button removePost;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        Intent intent = getIntent();
        connectViews();
        long postId = intent.getLongExtra("post_id",-1);
        if (postId == -1) finish();
        else{
            connectAdapter(postId);
            updateViews();
            addFollowersToPost();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (commentsAdapter == null || post == null) return;
        commentsAdapter.setComments(getAllCommentsForPost(post.getId()));
        commentsAdapter.notifyDataSetChanged();
    }

    private void updateViews(){
        if (post == null) return;
        postTitle.setText(post.getTitle());
        postDescription.setText(post.getPost());
        if (post.getUser() == null) return;
        userName.setText(post.getUser().getName());
        userBio.setText(post.getUser().getBio());
    }

    private void connectAdapter(long postId){
        commentsAdapter = new CommentsAdapter(getAllCommentsForPost(postId));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        comments.setLayoutManager(mLayoutManager);
        comments.setItemAnimator(new DefaultItemAnimator());
        comments.setAdapter(commentsAdapter);
        commentsAdapter.notifyDataSetChanged();
    }

    private void connectViews(){
        addComment = (FloatingActionButton) findViewById(R.id.addComment);
        addComment.setOnClickListener(newCommentClick);
        postTitle = (TextView) findViewById(R.id.postTitle);
        removePost = (Button) findViewById(R.id.removePost);
        removePost.setOnClickListener(removePostClick);

        postDescription = (TextView) findViewById(R.id.postDescription);
        userName = (TextView) findViewById(R.id.userName);
        userBio = (TextView) findViewById(R.id.userBio);

        comments = (RecyclerView) findViewById(R.id.comments);
    }

    private Post findPost(long postId){
        return Post.find(Post.class,postId);
    }

    View.OnClickListener newCommentClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (post == null) return;
            startActivity(new Intent(PostDetailsActivity.this,AddCommentActivity.class)
                    .putExtra("post_id",post.getId()));

        }
    };
    View.OnClickListener removePostClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (post == null) return;
            post.delete();
            finish();
        }
    };


    private void addFollowersToPost(){
        if (post == null || post.getFollowers() != null) return;
        List<User> propsedFollowers = User.findAll(User.class, Entity.PRIMARY_KEY_COLUMN_NAME+" != 1");
        post.setFollowers(propsedFollowers);
        post.save();
    }


    private List<Comment> getAllCommentsForPost(long postId){
        List<Comment> comments = new ArrayList<>();
        post = findPost(postId);
        post.getFollowers();
        if (post == null) return comments;
        return post.getComments();
    }
}
