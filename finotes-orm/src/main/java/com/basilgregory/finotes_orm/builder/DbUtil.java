package com.basilgregory.finotes_orm.builder;

import com.basilgregory.finotes_orm.annotations.Table;
import com.basilgregory.finotes_orm.constants.DB;

import java.lang.reflect.Method;

/**
 * Created by donpeter on 8/29/17.
 */

public class DbUtil {
    public static String getTableName(Entity entity){
        return getTableName((Class<Entity>)entity.getClass());
    }
    public static String getTableName(Class<Entity> cls){
        return (cls.getAnnotation(Table.class) == null || cls.getAnnotation(Table.class).name().isEmpty()) ?
                cls.getSimpleName().toLowerCase() :
                cls.getAnnotation(Table.class).name();
    }

    static String getColumnName(Method method){
        if (method == null) return null;
        if (method.getName().toLowerCase().contains("set")){
            String columnName  = method.getName().toLowerCase().replace("set","");
            return columnName.equals("id") ? DB.PRIMARY_KEY_ID : columnName;
        }
        return null;
    }


    static Object invokeGetter(String fieldName, Entity entity) throws Exception{
        Method[] allMethods = entity.getClass().getDeclaredMethods();
        for (Method method:allMethods) {
            if (method.getName().toLowerCase().contains("get") &&
                    method.getName().toLowerCase().contains(fieldName.toLowerCase())){
                return method.invoke(entity);
            }
        }
        return null;
    }

}
