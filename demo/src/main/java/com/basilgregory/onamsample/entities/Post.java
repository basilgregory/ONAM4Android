package com.basilgregory.onamsample.entities;

import com.basilgregory.onam.android.Entity;
import com.basilgregory.onam.annotations.Column;
import com.basilgregory.onam.annotations.Json;
import com.basilgregory.onam.annotations.ManyToMany;
import com.basilgregory.onam.annotations.ManyToOne;
import com.basilgregory.onam.annotations.OneToMany;
import com.basilgregory.onam.annotations.Table;

import java.util.List;

/**
 * Created by donpeter on 9/6/17.
 */
@Table
public class Post extends Entity {
    private String title;
    private String post;

    //If you need to include a property that is not be inserted/updated to database then you have to specify the modifier transient
    private transient String transientPost;


    private long createdAt;

    private List<User> followers;

    private List<Comment> comments;
    private User user;

    @Column(unique = true)
    @Json(fieldName = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Json(fieldName = "post")
    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    @Json(fieldName = "created_at")
    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @OneToMany(referencedColumnName = "post_id", targetEntity = Comment.class)
    public List<Comment> getComments() {
        return fetch(this.comments,new Comment(){});
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @ManyToOne
    public User getUser() {
        return fetch(this.user,new User(){});
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToMany(tableName = "post_followers", targetEntity = User.class)
    public List<User> getFollowers() {
        return fetch(this.followers,new User(){});
    }

    public void setFollowers(List<User> followers) {
        this.followers = followers;
    }

    public String getTransientPost() {
        return transientPost;
    }

    public void setTransientPost(String transientPost) {
        this.transientPost = transientPost;
    }
}
