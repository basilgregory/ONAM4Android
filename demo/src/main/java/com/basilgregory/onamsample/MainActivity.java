package com.basilgregory.onamsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.basilgregory.onam.annotations.DB;
import com.basilgregory.onam.builder.Entity;

@DB(name = "blog_db",
        tables = {Post.class,Comment.class,User.class
}, version = 1)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Entity.init(this);

        User user2 = new User();
        user2.setName("Robin Alex");
        user2.setBio("Developer");



//        User user = new User();
//        user.setName("DON PETER");
//        user.setBio("Developer");
//        user.save();
//
        Post post = new Post();
        post.setTitle("Another POST");
        post.setCreated_at(System.currentTimeMillis());
        post.setUser(user2);
        post.save();
//
//        //Adding a comment
        Comment comment = new Comment();
        comment.setComment("Some stupid comment");
        comment.setCreated_at(System.currentTimeMillis());
        comment.setPost(post);
        comment.setUser(user2);
        comment.save();


        Log.d("TAG","somethign");

    }
}
