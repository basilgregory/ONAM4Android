package com.basilgregory.onamsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.basilgregory.onam.android.Entity;
import com.basilgregory.onam.annotations.DB;


@DB(name = "blog_db",
        tables = {Post.class,Comment.class,User.class
}, version = 1)
public class MainActivity extends AppCompatActivity {

    RecyclerView posts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Entity.init(this);

    }


}
