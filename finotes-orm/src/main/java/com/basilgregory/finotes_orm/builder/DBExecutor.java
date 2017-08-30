package com.basilgregory.finotes_orm.builder;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.basilgregory.finotes_orm.constants.DB;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by donpeter on 8/29/17.
 */

public class DBExecutor extends SQLiteOpenHelper {
    private static DBExecutor instance = null;

    public static DBExecutor getInstance(Context context, String dbName, int version){

        instance =  instance == null ?
                new DBExecutor(context,dbName,
                        version) : instance;
        return instance;
    }

    public static DBExecutor getInstance() {
        return instance;
    }

    public static void init (Context context, Object o) {
        if (o.getClass().getAnnotation(com.basilgregory.finotes_orm.annotations.DB.class) == null) return;
        getInstance(context,o.getClass().getAnnotation(com.basilgregory.finotes_orm.annotations.DB.class).name(), o.getClass().getAnnotation(com.basilgregory.finotes_orm.annotations.DB.class).version())
                .createTables(o);

    }

    private DBExecutor(Context context,String dbName,int version) {
        super(context, dbName, null, version);
    }
    @Override
    public void onCreate(SQLiteDatabase database) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(DBExecutor.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        onCreate(db);
    }

    void createTables(Object o){
        ArrayList<String> ddls = DDLBuilder.createTables(o.getClass().getAnnotation(com.basilgregory.finotes_orm.annotations.DB.class).tables());
        try {
            getWritableDatabase().beginTransaction();
            for (String ddl : ddls) {
                try {
                    getWritableDatabase().execSQL(ddl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            getWritableDatabase().endTransaction();
        }
    }

    Entity findById(Class<Entity> cls,long id){
        try {
            return EntityBuilder.convertToEntity(cls,getReadableDatabase().rawQuery
                    (QueryBuilder.findById(cls,id),null));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close();
        }
        return null;
    }

    private Entity findById(Entity entity){
        try {
            return EntityBuilder.convertToEntity(entity,getReadableDatabase().rawQuery
                    (QueryBuilder.findById((Class<Entity>) entity.getClass(),entity.getId()),null));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close();
        }
        return null;
    }

    private Entity findByROWId(Entity entity,long id){
        try {
            return EntityBuilder.convertToEntity(entity,getReadableDatabase().rawQuery
                    (QueryBuilder.findByROWId((Class<Entity>) entity.getClass(),id),null));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close();
        }
        return null;
    }

    Entity findByUniqueProperty(Class<Entity> cls,String columnName,Object value){
        try {
            return EntityBuilder.convertToEntity(cls,getReadableDatabase().rawQuery
                    (QueryBuilder.findByUniqueProperty(cls,columnName,value),null));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close();
        }
        return null;
    }

    List<Entity> findByProperty(Class<Entity> cls, String columnName, Object value,Integer startIndex,Integer pageSize){
        try {
            return EntityBuilder.convertToEntities(cls,getReadableDatabase().rawQuery
                    (QueryBuilder.findByProperty(cls,columnName,value,startIndex,pageSize),null));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close();
        }
        return null;
    }

    void insert(Entity entity){
        long rowId = -1;
        try {
            getWritableDatabase().beginTransaction();
            rowId = getWritableDatabase().insert
                    (DbUtil.getTableName(entity),null,QueryBuilder.insertContentValues(entity));
            if (rowId < 0) return; //The row insertion was a failure;
            getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            getWritableDatabase().endTransaction();
            findByROWId(entity,rowId); //if successfull return corresponding Entity.
            close();
        }
    }

    void update(Entity entity){
        try {
            ContentValues contentValues = QueryBuilder.insertConflictContentValues(entity,
                    findById((Class<Entity>) entity.getClass(),entity.getId()));
            getWritableDatabase().beginTransaction();
            int numberOfRowsUpdated = getWritableDatabase().update
                    (DbUtil.getTableName(entity),contentValues,
                    DB.PRIMARY_KEY_ID = "?",new String[]{String.valueOf(entity.getId())});
            if (numberOfRowsUpdated < 1) return ;
            getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            getWritableDatabase().endTransaction();
            findById(entity); //if successfull return corresponding Entity.
            close();
        }
    }


}
