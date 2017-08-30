package com.basilgregory.finotes_orm.builder;

import com.basilgregory.finotes_orm.annotations.OneToOne;
import com.basilgregory.finotes_orm.annotations.Table;
import com.basilgregory.finotes_orm.constants.DB;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.basilgregory.finotes_orm.builder.DbUtil.getTableName;

/**
 * Created by donpeter on 8/28/17.
 */

public class DDLBuilder {



    static ArrayList<String> createTables(Class[] classes){

        StringBuffer ddlCreate = new StringBuffer();
        ArrayList<String> ddls = new ArrayList<String>(classes.length);

        for(Class cls:classes) {
            ddls.add(createTable(cls));
        }

        return ddls;
    }

    private static String createTable(Class<Entity> cls){
        StringBuffer ddlCreate = new StringBuffer();
        ddlCreate.append("create table ").append(DbUtil.getTableName(cls)).append(" ( ").append(DB.PRIMARY_KEY_ID).append(" INTEGER primary key autoincrement, ");
        Field[] fields = cls.getDeclaredFields();
        for (Field field:fields) {
            String fieldType = findType(field);
            if (fieldType != null){
                ddlCreate.append(field.getName()).append(" ").append(fieldType);
            }else{
                generateForiegnKey(ddlCreate,field);
            }
            ddlCreate.append(", ");
        }
        ddlCreate.replace(ddlCreate.length()-2,ddlCreate.length()-1,"");
        ddlCreate.append(");");
        return ddlCreate.toString();
    }

    private static void generateForiegnKey(StringBuffer stringBuffer,Field field){
        Class foriegnClass = field.getType();
        if (foriegnClass.getAnnotation(Table.class) == null) return;
        if (field.getAnnotation(OneToOne.class) != null){
            stringBuffer.append(getTableName(foriegnClass)).append("_id").append(" ").append("INTEGER");
        }

    }

    private static String findType(Field field){

        try {
            return FieldType.adjustedValueOf(field.getType().getSimpleName().toUpperCase()).getDataType();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
