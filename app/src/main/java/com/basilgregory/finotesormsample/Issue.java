package com.basilgregory.finotesormsample;

import com.basilgregory.finotes_orm.annotations.OneToOne;
import com.basilgregory.finotes_orm.builder.Entity;
import com.basilgregory.finotes_orm.annotations.Table;

/**
 * Created by donpeter on 8/29/17.
 */
@Table
public class Issue extends Entity {
    private User user;

    @OneToOne
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

