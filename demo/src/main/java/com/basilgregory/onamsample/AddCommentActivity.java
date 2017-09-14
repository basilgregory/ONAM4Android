package com.basilgregory.onamsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.basilgregory.onamsample.entities.Comment;
import com.basilgregory.onamsample.entities.Post;
import com.basilgregory.onamsample.entities.User;

public class AddCommentActivity extends AppCompatActivity {
    EditText comment;
    Button saveComment;
    Post post;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);
        Intent intent = getIntent();
        long postId = intent.getLongExtra("post_id",-1);
        findPost(postId);
        connectViews();

    }

    private void connectViews(){
        saveComment = (Button) findViewById(R.id.saveComment);
        saveComment.setOnClickListener(savePostClick);
        comment = (EditText) findViewById(R.id.comment);
    }

    private void findPost(long postId){
        post = Post.find(Post.class,postId);
    }


    View.OnClickListener savePostClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Comment commentObject = new Comment();
            commentObject.setComment(comment.getText().toString());
            if (post != null) commentObject.setPost(post);
            User registeredUser = User.find(User.class,1);
            commentObject.setUser(registeredUser);
            commentObject.save();
            finish();
        }
    };
}
