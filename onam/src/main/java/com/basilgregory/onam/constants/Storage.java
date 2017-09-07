package com.basilgregory.onam.constants;

import android.content.Context;
import android.content.SharedPreferences;

import com.basilgregory.onam.builder.DbMetaData;

/**
 * Created by donpeter on 9/1/17.
 */

public class Storage {
    Context context;
    public Storage(Context context){
        this.context = context;
    }

    public void storeCurrentDbMeta(String databaseName,DbMetaData dbMetaData) throws Exception{
        SharedPreferences sharedPref = context.getSharedPreferences(
                "database_meta", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(databaseName, DB.toString(dbMetaData));
        editor.commit();
    }

    public DbMetaData getCurrentDbMeta(String databaseName) throws Exception{
        SharedPreferences sharedPref = context.getSharedPreferences(
                "database_meta", Context.MODE_PRIVATE);
        return DB.toObject(sharedPref.getString(databaseName, null));
    }


}
