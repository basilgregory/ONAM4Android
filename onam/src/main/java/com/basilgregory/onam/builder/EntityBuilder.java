package com.basilgregory.onam.builder;

import android.database.Cursor;

import com.basilgregory.onam.annotations.Table;
import com.basilgregory.onam.constants.DB;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.basilgregory.onam.builder.DbUtil.getColumnName;
import static com.basilgregory.onam.builder.DbUtil.getMethod;

/**
 * Created by donpeter on 8/30/17.
 */

public class EntityBuilder {

    private static Entity convertToEntity(Class<Entity> cls,Cursor cursor,Field[] fields,Entity entity){
        for (Field field: fields) {
            try {

                String columnName = getColumnName(field);
                if (columnName == null) continue; //No getter functions found. Getter is mandatory

                String firstParameterType = field.getType().getSimpleName().toUpperCase();
                entity = entity == null ? cls.newInstance() : entity;
                Method setter= getMethod("set", field);
                if (setter == null || setter.getParameterTypes()[0].getAnnotation(Table.class) != null) continue;
                Object valueFromDatabase = FieldType.getValueFromCursor(cursor,
                        firstParameterType,columnName);
                setter.invoke(entity,valueFromDatabase);
            } catch (IllegalStateException e) {
                Logger.InternalLogger.w("Field type is not a match with the database types (possibly a LIST)");
                Logger.InternalLogger.d(e.getLocalizedMessage());
            }catch (Exception e) {
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
            Field[] fields = entity.getClass().getDeclaredFields();
            return convertToEntity((Class<Entity>) entity.getClass(),cursor,fields,entity);
        }
        return null;
    }

    static Entity convertToEntity(Class<Entity> cls,Cursor cursor){
        if (cursor != null && cursor.getCount() > 0 ) {
            cursor.moveToFirst();
            Field[] fields = cls.getDeclaredFields();
            return convertToEntity(cls,cursor,fields,null);
        }
        return null;
    }

    static Entity convertToEntity(Class<Entity> cls,Cursor cursor,Field[] fields){
        return convertToEntity(cls,cursor,fields,null);
    }
}
