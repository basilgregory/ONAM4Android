package com.basilgregory.finotesormsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.basilgregory.finotes_orm.annotations.DB;
import com.basilgregory.finotes_orm.builder.Entity;

@DB(name = "test_db",
        tables = {
                Event.class,
                Issue.class
})
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Entity.init(getApplicationContext(),this);
        Event event = new Event();
        event.setName("DON PETER");
        event.setInteger(000);
        event.save();

//        event.setInteger(200);
//        event.save();


//        List<Event> eventFromDB = (List<Event>) Entity.findByProperty(Event.class,"name","DON PETER",null,null);
        Log.d("TAG","somethign");
    }
}
