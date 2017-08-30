package com.basilgregory.finotes_orm.builder;

import android.database.Cursor;

import com.basilgregory.finotes_orm.constants.DB;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.basilgregory.finotes_orm.builder.DbUtil.getColumnName;

/**
 * Created by donpeter on 8/30/17.
 */

public class EntityBuilder {
    private static Entity convertToEntity(Class<Entity> cls,Cursor cursor,Method[] methods,Entity entity){
        for (Method method: methods) {
            try {
                String columnName = getColumnName(method);
                if (columnName == null) continue; //No setter functions found.
                String firstParameterType = method.getParameterTypes()[0].getSimpleName().toUpperCase();
                entity = entity == null ? cls.newInstance() : entity;
                method.invoke(entity,FieldType.getValueFromCursor(cursor,
                        firstParameterType,columnName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            entity.setId((Long) FieldType.getValueFromCursor(cursor,
                    FieldType.LONG.realName().toUpperCase(), DB.PRIMARY_KEY_ID));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    static Entity convertToEntity(Entity entity,Cursor cursor){
        if (cursor != null && cursor.getCount() > 0 ) {
            cursor.moveToFirst();
            Method[] methods = entity.getClass().getDeclaredMethods();
            return convertToEntity((Class<Entity>) entity.getClass(),cursor,methods,entity);
        }
        return null;
    }

    static Entity convertToEntity(Class<Entity> cls,Cursor cursor){
        if (cursor != null && cursor.getCount() > 0 ) {
            cursor.moveToFirst();
            Method[] methods = cls.getDeclaredMethods();
            return convertToEntity(cls,cursor,methods,null);
        }
        return null;
    }

    static List<Entity> convertToEntities(Class<Entity> cls,Cursor cursor){
        List<Entity> entities = new ArrayList<Entity>(cursor.getCount());
        if (cursor != null && cursor.getCount() > 0 ) {
            cursor.moveToFirst();
            Method[] methods = cls.getDeclaredMethods();
            do {
                entities.add(convertToEntity(cls,cursor,methods,null));
            }while (cursor.moveToNext());
        }
        return entities;
    }



}
