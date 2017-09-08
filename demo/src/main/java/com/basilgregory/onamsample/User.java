package com.basilgregory.onamsample;

import com.basilgregory.onam.annotations.JoinTable;
import com.basilgregory.onam.annotations.ManyToMany;
import com.basilgregory.onam.annotations.OneToMany;
import com.basilgregory.onam.annotations.Table;
import com.basilgregory.onam.android.Entity;

import java.util.List;

/**
 * Created by donpeter on 9/6/17.
 */
@Table
public class User extends Entity {
    private String name;
    private String bio;


    private List<Post> followedPosts;
    private List<Post> posts;
    private List<Comment> comments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @OneToMany(referencedColumnName = "owner_id")
    public List<Post> getPosts() {
        return fetch(this.posts,new Post(){});
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    @OneToMany(referencedColumnName = "creator_id")
    public List<Comment> getComments() {
        return fetch(this.comments,new Comment(){});
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @ManyToMany
    @JoinTable(tableName = "user_followers", targetEntity = Post.class)
    public List<Post> getFollowedPosts() {
        return fetch(this.followedPosts,new Post(){});
    }

    public void setFollowedPosts(List<Post> followedPosts) {
        this.followedPosts = followedPosts;
    }
}
