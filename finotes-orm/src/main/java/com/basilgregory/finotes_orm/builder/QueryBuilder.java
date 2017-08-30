package com.basilgregory.finotes_orm.builder;

import android.content.ContentValues;

import com.basilgregory.finotes_orm.constants.DB;

import java.lang.reflect.Field;

import static com.basilgregory.finotes_orm.builder.DbUtil.invokeGetter;

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

    static String findByProperty(Class<Entity> cls,String columnName,Object value,Integer startIndex,Integer pageSize){
        value = FieldType.isStringType(value) ? "'"+value+"'" : value;
        return selectQuery(DbUtil.getTableName(cls), columnName+" = "+value,null,null,startIndex,pageSize);
    }

    static String findByROWId(Class<Entity> cls,long rowID){
        String rawQuery =  "SELECT * FROM :table WHERE ROWID = :id Limit 1";
        return rawQuery
                .replaceFirst(":table",DbUtil.getTableName(cls))
                .replaceFirst(":id",String.valueOf(rowID));
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
                FieldType.addValues(contentValues,field, invokeGetter(field.getName(),entity));
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
                Object newReturnValue = invokeGetter(field.getName(),entity);
                Object currentReturnValue = invokeGetter(field.getName(),currentEntityInDB);
                if (newReturnValue != null &&  !newReturnValue.equals(currentReturnValue)){
                    FieldType.addValues(contentValues,field,newReturnValue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return contentValues;
    }
}
