package com.basilgregory.onamsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import com.basilgregory.onam.android.Entity;
import com.basilgregory.onam.annotations.DB;
import com.basilgregory.onamsample.adapter.PostsAdapter;
import com.basilgregory.onamsample.entities.Comment;
import com.basilgregory.onamsample.entities.Post;
import com.basilgregory.onamsample.entities.User;
import com.basilgregory.onamsample.listeners.ClickListener;
import com.basilgregory.onamsample.listeners.RecyclerTouchListener;

import java.util.List;


/**
 * This is where your database name along with the entity class names is to be provided.
 * Both are mandatory
 * No explicit naming convention needed.
 * Ideally #{DB} should be added to the activity that will be called every time application is opened fresh.
 *
 */
@DB(name = "blog_db",
        tables = {Post.class,Comment.class,User.class
}, version = 1)
public class AllPostsActivity extends AppCompatActivity {

    PostsAdapter postsAdapter;
    RecyclerView posts;
    FloatingActionButton addPlan;
    List<Post> postList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_posts);

        //This init should be called in the activity #{onCreate} where you have #{DB} annotation added.
        Entity.init(this);

        connectViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (postsAdapter == null) return;
        fetchAllPosts();
        postsAdapter.setPosts(postList);
        postsAdapter.notifyDataSetChanged();
    }

    private void connectViews(){

        fetchAllPosts();
        postsAdapter = new PostsAdapter(postList);
        addPlan = (FloatingActionButton) findViewById(R.id.addPlan);
        addPlan.setOnClickListener(addNewPlan);
        posts = (RecyclerView) findViewById(R.id.posts);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        posts.setLayoutManager(mLayoutManager);
        posts.setItemAnimator(new DefaultItemAnimator());
        posts.setAdapter(postsAdapter);
        posts.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), posts, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (postList == null || postList.size() < 1) return;
                startActivity(new Intent(AllPostsActivity.this,PostDetailsActivity.class)
                        .putExtra("post_id",postList.get(position).getId()));

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        postsAdapter.notifyDataSetChanged();
    }


    View.OnClickListener addNewPlan = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(AllPostsActivity.this,AddPostActivity.class));
        }
    };

    private void fetchAllPosts(){
        postList =  Entity.findAll(Post.class,null,null);
    }


}
