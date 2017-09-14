package com.basilgregory.onamsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.basilgregory.onamsample.entities.Post;
import com.basilgregory.onamsample.entities.User;

public class AddPostActivity extends AppCompatActivity {
    EditText postTitle,postDescription;
    Button savePost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        connectViews();

    }

    private void connectViews(){
        savePost = (Button) findViewById(R.id.savePost);
        savePost.setOnClickListener(savePostClick);
        postTitle = (EditText) findViewById(R.id.postTitle);
        postDescription = (EditText) findViewById(R.id.postDescription);
    }

    View.OnClickListener savePostClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Post post = new Post();
            post.setTitle(postTitle.getText().toString());
            post.setPost(postDescription.getText().toString());
            User registeredUser = User.find(User.class,1);
            post.setUser(registeredUser);
            post.save();
            finish();
        }
    };
}
