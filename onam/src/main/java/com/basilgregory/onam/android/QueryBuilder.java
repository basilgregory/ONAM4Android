package com.basilgregory.onam.android;

import android.content.ContentValues;

import com.basilgregory.onam.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static com.basilgregory.onam.android.DbUtil.invokeGetter;

/**
 * Created by donpeter on 8/29/17.
 */

public class QueryBuilder {

    static String findById(Class<Entity> cls,long id){
        return selectQuery(DbUtil.getTableName(cls), DB.PRIMARY_KEY_ID+" = "+id,null,null,0,1);
    }

    static String findByUniqueProperty(Class<Entity> cls,String columnName,Object value){
        value = FieldType.isStringType(value) ? "'"+value+"'" : value;
        return selectQuery(DbUtil.getTableName(cls), columnName+" = "+value,null,null,0,1);
    }


    static String findByProperty(String tableName,String columnName,Object value,Integer startIndex,Integer pageSize){
        value = FieldType.isStringType(value) ? "'"+value+"'" : value;
        return selectQuery(tableName, columnName+" = "+value,null,null,startIndex,pageSize);
    }

    static String findByProperty(Class<Entity> cls,String columnName,Object value,Integer startIndex,Integer pageSize){
        return findByProperty(DbUtil.getTableName(cls),columnName,value,startIndex,pageSize);
    }

    static String findByROWId(Class<Entity> cls,long rowID){
        String rawQuery =  "SELECT * FROM :table WHERE ROWID = :id Limit 1";
        return rawQuery
                .replaceFirst(":table",DbUtil.getTableName(cls))
                .replaceFirst(":id",String.valueOf(rowID));
    }

    static String findAll(Class<Entity> cls,String whereClause,Integer startIndex,Integer pageSize){
        return selectQuery(DbUtil.getTableName(cls),whereClause,null,null,startIndex,pageSize);
    }

    static String findAll(Class<Entity> cls,String whereClause,String orderByColumn,boolean descending,Integer startIndex,Integer pageSize){
        StringBuffer orderByCondition = new StringBuffer(orderByColumn)
                .append(" ").append(descending ? "DESC" : "ASC");
        return selectQuery(DbUtil.getTableName(cls),whereClause,null,orderByCondition.toString(),startIndex,pageSize);
    }


    static String findColumnById(Class<Entity> cls,long id,String columnName){
        String rawQuery =  "SELECT :columnName FROM :table WHERE "+DB.PRIMARY_KEY_ID+" = :id Limit 1";
        return rawQuery
                .replace(":columnName",columnName)
                .replaceFirst(":table",DbUtil.getTableName(cls))
                .replaceFirst(":id",String.valueOf(id));
    }

    static String selectQuery(String tableName,String whereClause,String groupBy,String orderBy,Integer startIndex, Integer pageSize){
        if (tableName == null) return null;
        whereClause = whereClause == null? "" : " where "+ whereClause;
        groupBy = groupBy == null? "" : " group by "+ groupBy;
        orderBy = orderBy == null? "" : " order by "+orderBy;
        StringBuffer selectQuery = new StringBuffer("select * from ").append(tableName)
                .append(whereClause).append(groupBy).append(orderBy);

        if ((startIndex != null) && (pageSize != null))
            selectQuery.append(" LIMIT ").append(startIndex).append(",").append(pageSize);
        selectQuery.append(";");
        return selectQuery.toString();
    }


    static ContentValues insertContentValues(Entity entity){
        ContentValues contentValues = new ContentValues();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field: fields) {
            try {
                if (Modifier.isTransient(field.getModifiers())) continue; //Transient field are already omitted from database.
                if (field.getType().getAnnotation(Table.class) != null) {
                    Object relatedEntity = DbUtil.invokeGetter(field,entity);
                    if (relatedEntity == null) continue;
                    contentValues.put(DbUtil.getColumnName(field), ((Entity) relatedEntity).getId());
                }else FieldType.addValues(contentValues,field, invokeGetter(field,entity));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return contentValues;
    }

    static ContentValues insertConflictContentValues(Entity entity,Entity currentEntityInDB){
        ContentValues contentValues = new ContentValues();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field: fields) {
            try {
                if (Modifier.isTransient(field.getModifiers())) continue; //Transient field are already omitted from database.
                if (field.getType().getAnnotation(Table.class) != null) {
                    Object relatedEntity = DbUtil.invokeGetter(field,entity);
                    if (relatedEntity == null) continue;
                    contentValues.put(DbUtil.getColumnName(field), ((Entity) relatedEntity).getId());
                }else{
                    Object newReturnValue = invokeGetter(field,entity);
                    Object currentReturnValue = invokeGetter(field,currentEntityInDB);
                    if (newReturnValue != null &&  !newReturnValue.equals(currentReturnValue)){
                        try {
                            FieldType.addValues(contentValues,field,newReturnValue);
                        } catch (Exception e) {
                            L.vi("Possibly a list in QueryBuilder "+e.getLocalizedMessage());

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return contentValues;
    }
}
