package com.basilgregory.onamsample;

import com.basilgregory.onam.annotations.Column;
import com.basilgregory.onam.annotations.ManyToOne;
import com.basilgregory.onam.annotations.Table;
import com.basilgregory.onam.android.Entity;

/**
 * Created by donpeter on 9/6/17.
 */
@Table
public class Comment extends Entity {
    private String comment;
    private long created_at;

    private Post post;
    private User user;


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    @ManyToOne
    @Column(name = "post_id")
    public Post getPost() {
        return fetch(this.post,new Post(){});
    }

    public void setPost(Post post) {
        this.post = post;
    }

    @ManyToOne
    @Column(name = "creator_id")
    public User getUser() {
        return fetch(this.user,new User(){});
    }

    public void setUser(User user) {
        this.user = user;
    }
}
