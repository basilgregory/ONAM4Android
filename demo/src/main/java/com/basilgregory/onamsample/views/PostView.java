package com.basilgregory.onamsample.views;

import com.basilgregory.onam.annotations.Json;
import com.basilgregory.onamsample.entities.Comment;
import com.basilgregory.onamsample.entities.Post;
import com.basilgregory.onamsample.entities.User;

import java.util.List;

/**
 * Created by donpeter on 10/5/17.
 */

public class PostView {
    private Post post;

    public PostView(Post post) {
        this.post = post;
    }
    @Json(fieldName = "title")
    public String getTitle() {
        return post.getTitle();
    }

    @Json(fieldName = "created_at")
    public long getCreatedAt() {
        return post.getCreatedAt();
    }

    public List<Comment> getComments() {
        return post.getComments();
    }

    public User getUser() {
        return post.getUser();
    }

    public List<User> getFollowers() {
        return post.getFollowers();
    }
}
