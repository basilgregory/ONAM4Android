package com.basilgregory.onamsample.entities;

import com.basilgregory.onam.annotations.AfterCreate;
import com.basilgregory.onam.annotations.BeforeCreate;
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
    private long createdAt;

    private Post post;
    private User user;


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
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

    /**
     * You may create any number of @BeforeCreate functions and will be executed before actual insertion to the database
     * The order of execution of @BeforeCreate functions (incase of more than one) will be random.
     */
    @BeforeCreate
    public void settingTimeStamps(){
        createdAt = System.currentTimeMillis();
    }

    /**
     * You may create any number of #{{@link AfterCreate}} functions and will be executed before actual insertion to the database
     * The order of execution of #{{@link AfterCreate}} functions (incase of more than one) will be random.
     */
    @AfterCreate
    public void afterCreate(){
        this.comment = "Comment: "+this.comment;
    }
}
