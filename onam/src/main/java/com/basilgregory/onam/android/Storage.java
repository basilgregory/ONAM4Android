package com.basilgregory.onam.android;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by donpeter on 9/1/17.
 */

class Storage {
    Context context;
    Storage(Context context){
        this.context = context;
    }

    void storeCurrentDbMeta(String databaseName,DbMetaData dbMetaData) throws Exception{
        SharedPreferences sharedPref = context.getSharedPreferences(
                "database_meta", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(databaseName, DB.toString(dbMetaData));
        editor.commit();
    }

    DbMetaData getCurrentDbMeta(String databaseName) throws Exception{
        SharedPreferences sharedPref = context.getSharedPreferences(
                "database_meta", Context.MODE_PRIVATE);
        return DB.toObject(sharedPref.getString(databaseName, null));
    }


}
