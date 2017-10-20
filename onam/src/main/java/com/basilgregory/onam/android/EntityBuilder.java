package com.basilgregory.onam.android;

import android.database.Cursor;

import com.basilgregory.onam.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.basilgregory.onam.android.DbUtil.getColumnName;
import static com.basilgregory.onam.android.DbUtil.getMappingForeignColumnNameClass;
import static com.basilgregory.onam.android.DbUtil.getMethod;
import static com.basilgregory.onam.android.Entity.find;

/**
 * Created by donpeter on 8/30/17.
 */

class EntityBuilder {

    static List<Entity> getEntityFromMappingTable(Cursor cursor, Class collectionType){
        if (cursor == null || cursor.getCount() < 1) return null;
        cursor.moveToFirst();
        List<Entity> entities = new ArrayList<>();
        do{
            long foreignKey = cursor.getLong
                    (cursor.getColumnIndex(
                            getMappingForeignColumnNameClass(collectionType)));
            Entity queriedEntity = find(collectionType,foreignKey);
            if (queriedEntity == null) continue;
            entities.add(queriedEntity);
        }while (cursor.moveToNext());
        return entities;
    }

    private static Entity convertToEntity(Class<Entity> cls,Cursor cursor,Field[] fields,Entity entity){
        for (Field field: fields) {
            try {
                if (Modifier.isTransient(field.getModifiers())) continue; //Transient field are already omitted from database.
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
                L.vi("Field ("+field.getName()
                        +") of type ("+field.getType().getSimpleName().toUpperCase()
                        +") is not a match with the database types (possibly a LIST)");
                L.vi(e.getLocalizedMessage());
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
        if (cursor == null || cursor.getCount() < 1) return null;
        cursor.moveToFirst();
        Field[] fields = entity.getClass().getDeclaredFields();
        return convertToEntity((Class<Entity>) entity.getClass(),cursor,fields,entity);
    }

    static Entity convertToEntity(Class<Entity> cls,Cursor cursor){
        if (cursor == null || cursor.getCount() < 1) return null;
        cursor.moveToFirst();
        Field[] fields = cls.getDeclaredFields();
        return convertToEntity(cls,cursor,fields,null);
    }

    static Entity convertToEntity(Class<Entity> cls,Cursor cursor,Field[] fields){
        return convertToEntity(cls,cursor,fields,null);
    }
}
