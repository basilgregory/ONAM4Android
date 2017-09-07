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
        Entity.init(getApplicationContext(),this);

        User user = User.findById(User.class,1);
//        user.getPosts();
//        user.getComments().get(0).getPost();



//        User user = new User();
//        user.setName("DON PETER");
//        user.setBio("Developer");
//        user.save();
//
//        Post post = new Post();
//        post.setTitle("First POST");
//        post.setPost("this is my first ever post used");
//        post.setCreated_at(System.currentTimeMillis());
//        post.setUser(user);
//        post.save();
//
//        //Adding a comment
//        Comment comment = new Comment();
//        comment.setComment("This is the first comment");
//        comment.setCreated_at(System.currentTimeMillis());
//        comment.setPost(post);
//        comment.setUser(user);
//        comment.save();


        Log.d("TAG","somethign");

    }
}
