package com.basilgregory.onam.android;

import com.basilgregory.onam.annotations.ManyToMany;
import com.basilgregory.onam.annotations.OneToMany;
import com.basilgregory.onam.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import static com.basilgregory.onam.android.DbUtil.getTableName;

/**
 * Created by donpeter on 8/28/17.
 */

public class DDLBuilder {

    static String createMappingTables(String tableName,Class fromClass,Class targetClass){
        StringBuffer ddlCreate = new StringBuffer();
        //This pattern "table [] ( " is used in #{DbUtil.extractNameOfTableFromDdl} cross check when making a change.
        ddlCreate.append("create table ").append(tableName).append(" ( ");
        ddlCreate.append(DbUtil.getMappingForeignColumnNameClass(fromClass)).append(" integer,");
        ddlCreate.append(DbUtil.getMappingForeignColumnNameClass(targetClass)).append(" integer");
        ddlCreate.append(");");
        return ddlCreate.toString();

    }


    static HashMap<String,String> createTables(List<Class> curatedClassList){

        HashMap<String,String> ddls = new HashMap<String,String>(curatedClassList.size());

        for(Class cls:curatedClassList) {
            if (cls == null || cls.getAnnotation(Table.class) == null) continue;
            ddls.put(getTableName(cls),createTable(cls));
        }
        return ddls;
    }

    private static String createTable(Class<Entity> cls){
        StringBuffer ddlCreate = new StringBuffer();
        //This pattern "table [] ( " is used in #{DbUtil.extractNameOfTableFromDdl} cross check when making a change.
        ddlCreate.append("create table ").append(getTableName(cls)).append(" ( ").append(DB.PRIMARY_KEY_ID).append(" integer primary key autoincrement, ");
        Field[] fields = cls.getDeclaredFields();
        for (Field field:fields) {
            String fieldType = DbUtil.findType(field);
            if (fieldType != null) ddlCreate.append(field.getName().toLowerCase()).append(" ").append(fieldType);
            else {
                Method getterMethod = DbUtil.getMethod("get",field);
                if (getterMethod.getAnnotation(OneToMany.class) == null &&
                        getterMethod.getAnnotation(ManyToMany.class) == null) DbUtil.generateForeignColumnName(ddlCreate,field);
                else continue;
            }
            ddlCreate.append(", ");
        }
        ddlCreate.replace(ddlCreate.length()-2,ddlCreate.length()-1,"");
        ddlCreate.append(");");
        return ddlCreate.toString();
    }


}
