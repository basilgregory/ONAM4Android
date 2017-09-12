package com.basilgregory.onam.android;

import com.basilgregory.onam.annotations.Column;
import com.basilgregory.onam.annotations.ManyToMany;
import com.basilgregory.onam.annotations.OneToMany;
import com.basilgregory.onam.annotations.OneToOne;
import com.basilgregory.onam.annotations.Table;

import java.lang.reflect.Field;
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

    static String getColumnName(Field field){
        return getColumnName(getMethod("get", field));
    }

    static short getFetchType(Field field){
        return getFetchType(getMethod("get", field));
    }

    static short getFetchType(Method method) {
        try {

            if (method == null) return -1;
            if (method.getAnnotation(OneToOne.class) != null) return method.getAnnotation(OneToOne.class).fetchType();
            if(method.getAnnotation(OneToMany.class) != null) return method.getAnnotation(OneToMany.class).fetchType();
            if (method.getAnnotation(ManyToMany.class) != null ) return method.getAnnotation(ManyToMany.class).fetchType();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;

    }

    /**
     * Steps taken
     * Will check for annotation for column name
     * else will trim  "get" from method name, check if method returns a related entity
     * if yes
     *      will return methodname.tolowercase + "_id"
     * else
     *      will return _id if the method is "getId"
     * @param method
     * @return
     */
    static String getColumnName(Method method) {
        try {
            if (method == null) return null;
            if (method.getAnnotation(Column.class) != null && !method.getAnnotation(Column.class).name().isEmpty()){
                return method.getAnnotation(Column.class).name();
            } else if (method.getName().toLowerCase().contains("get")){
                String columnName  = method.getName().toLowerCase().replace("get","");
                columnName = (method.getReturnType().getAnnotation(Table.class) != null) ? columnName + "_id" : columnName;
                return columnName.equals("id") ? DB.PRIMARY_KEY_ID : columnName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    static  Method getMethod (String prefix, Field field) {
        if (field == null) return null;
        String name = prefix + field.getName();
        try {
            switch (prefix.toLowerCase()) {
                case "set":
                    return findSetterMethod(name,field.getDeclaringClass(), field.getType());
                case "get" :
                    return findGetterMethod(name,field.getDeclaringClass());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Method findGetterMethod(String expectedMethodName,Class aClass){
        Method[] methods = aClass.getDeclaredMethods();
        for (Method method:methods){
            if (method.getName().toLowerCase().equals(expectedMethodName.toLowerCase())) return method;
        }
        return null;
    }

    private static Method findSetterMethod(String expectedMethodName,Class aClass,Class fieldType){
        Method[] methods = aClass.getDeclaredMethods();
        for (Method method:methods){
            if (!method.getName().toLowerCase().equals(expectedMethodName.toLowerCase())) continue;
            Class[] parameters = method.getParameterTypes();
            if (parameters.length < 1) continue;
            if (parameters[0] == fieldType) return method;
        }
        return null;
    }



    static  Method getSetterMethod (Method getter) {
        try {
            return getter.getDeclaringClass().getMethod(getter.getName().replaceFirst("get", "set"), getter.getReturnType());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param field
     * @param entity
     * @return -- returns NULL if the setter method with the single parameter does not exists, else returns response from executing setter method if any
     */
    static Object invokeGetter(Field field, Entity entity){
        try {
            Method method = getMethod("get",field);
            if (method == null) return null;
            return method.invoke(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Setter method will be executed with a single parameter.
     * @param entity
     * @param setterMethod
     * @param parameter -- parameter for setter method. Only one parameter expected.
     * @return -- returns NULL if the setter method with the single parameter does not exists, else returns response from executing setter method if any
     */
    static Object invokeSetterForList(Entity entity,Method setterMethod, Object parameter) {
        try {
            if (setterMethod == null) return null;
            return setterMethod.invoke(entity,parameter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * As foreign key is NOT an internally maintained key it will be provided by the developer using @column annotation.
     * @param stringBuffer
     * @param field
     */
    static void generateForeignColumnName(StringBuffer stringBuffer, Field field){
        Class foriegnClass = field.getType();
        if (foriegnClass.getAnnotation(Table.class) == null) return;
        stringBuffer.append(DbUtil.getColumnName(field)).append(" ").append("integer");
    }

    /**
     * To be used only for mapping class.
     * @param aClass
     * @return
     */
    static String getMappingForeignColumnNameClass(Class aClass){
        return aClass.getSimpleName().toLowerCase()+"_id";
    }

    /**
     * Field type only for attribute and not for related entity.
     * @param field
     * @return - Attribute class name in String format, null if the field is a related Entity.
     */
    static String findType(Field field){
        try {
            if (field.getType().getAnnotation(Table.class) == null) //Field is not a related Entity but is an attribute.
                return FieldType.adjustedValueOf(field.getType().getSimpleName().toUpperCase()).getDataType();
        } catch (IllegalArgumentException e) {
            Logger.InternalLogger.w("Field type is not a match with the database types (possibly a LIST) " +
                    "-- DbUtil.findType("+field.getName()+")");
            Logger.InternalLogger.d(e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}