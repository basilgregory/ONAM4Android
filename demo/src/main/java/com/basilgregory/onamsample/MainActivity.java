package com.basilgregory.onamsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.basilgregory.onam.android.Entity;
import com.basilgregory.onam.annotations.DB;
import com.basilgregory.onamsample.entities.Comment;
import com.basilgregory.onamsample.entities.Post;
import com.basilgregory.onamsample.entities.User;

@DB(name = "blog_db", tables = {Post.class,Comment.class,User.class}, version = 1)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_posts);
        Entity.init(this,this);

        User user = new User();
        user.setName("DON PETER");
        user.setBio("Developer");

        Post post = new Post();
        post.setTitle("First POST");
        post.setPost("this is my first ever post used");
        post.setCreatedAt(System.currentTimeMillis());
        post.setUser(user);

        //Adding a comment
        Comment comment = new Comment();
        comment.setComment("This is the first comment");
        comment.setCreatedAt(System.currentTimeMillis());
        comment.setPost(post);
        comment.setUser(user);

        comment.save();

        User savedUser = User.find(User.class,1);
        savedUser.getPosts();
        savedUser.getComments().get(0).getPost();
    }
}