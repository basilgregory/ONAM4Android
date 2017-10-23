package com.basilgregory.onamsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.basilgregory.onam.android.Entity;
import com.basilgregory.onam.annotations.DB;
import com.basilgregory.onamsample.adapter.PostsAdapter;
import com.basilgregory.onamsample.entities.Comment;
import com.basilgregory.onamsample.entities.Post;
import com.basilgregory.onamsample.entities.User;
import com.basilgregory.onamsample.listeners.ClickListener;
import com.basilgregory.onamsample.listeners.RecyclerTouchListener;

import org.json.JSONArray;
import org.json.JSONObject;

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
    Button removePosts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_posts);

        Entity.init(this); //This init should be called in the activity #{onCreate} where you have #{DB} annotation added.
        Entity.log(true,true); //Activate both debug and verbose logs
        registerUser();
        connectViews();

        try {
            convertJSONToEntity(); //Sample function to demo convertion of JSON to Entity.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sample function to display the convertion of JSON to Entity
     * @throws Exception
     */
    private void convertJSONToEntity() throws Exception{
        JSONObject post = new JSONObject();
        post.put("title","some title");
        JSONArray comments = new JSONArray();
        JSONObject comment = new JSONObject();
        comment.put("comment","commnet 1");
        JSONObject comment2 = new JSONObject();
        comment2.put("comment","commnet 2");
        comments.put(comment);
        comments.put(comment2);
        post.put("comments",comments);
        Post post1 = (Post) Entity.fromJSON(post,Post.class);
    }

    /**
     * Function checks for user with primary key 1 in database, and if no user is found will create a John doe.
     * This user will be assigned to all posts and comments made from the app.
     */
    private void registerUser(){
        if (User.find(User.class,1) != null) return;
        User user = new User();
        user.setName("John Doe");
        user.setBio("Developer");
        user.save();

        createUserListToFollow();
    }

    private void createUserListToFollow(){
        User aUser = new User();
        aUser.setName("Follower A");
        aUser.setBio("Developer");
        aUser.save();
        User bUser = new User();
        bUser.setName("Follower B");
        bUser.setBio("Designer");
        bUser.save();
        User cUser = new User();
        cUser.setName("Follower C");
        cUser.setBio("Doctor");
        cUser.save();
        User dUser = new User();
        dUser.setName("Follower D");
        dUser.setBio("Entrepreneur");
        dUser.save();
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

        removePosts = (Button) findViewById(R.id.removePosts);
        removePosts.setOnClickListener(removeAllPosts);

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

    View.OnClickListener removeAllPosts = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Post.truncate(Post.class);
        }
    };

    View.OnClickListener addNewPlan = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(AllPostsActivity.this,AddPostActivity.class));
        }
    };

    private void fetchAllPosts(){
        postList =  Post.findAll(Post.class);

    }


}
