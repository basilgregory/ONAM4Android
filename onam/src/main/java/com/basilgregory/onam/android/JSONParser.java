package com.basilgregory.onam.android;

import com.basilgregory.onam.annotations.Json;
import com.basilgregory.onam.annotations.ManyToMany;
import com.basilgregory.onam.annotations.OneToMany;
import com.basilgregory.onam.annotations.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by donpeter on 9/26/17.
 */

class JSONParser {

    /**
     * All @JSON methods
     * For delegate mandatory provide @JSON fieldname.
     * @JSON fieldname.isempty = fieldname.
     *
     */

    /**
     * Converts only primitive fields.
     * Convertion restricted to one layer.
     * @param entity
     * @return null if conversion fails
     */
    public static JSONObject toJsonObject(Object entity){
        if (entity == null) return null;
        JSONObject jsonObject = new JSONObject();
        Method[] methods = entity.getClass().getDeclaredMethods();
        for (Method method : methods) {
            try {
                if (!DbUtil.isGetter(method) ||
                        method.getAnnotation(Json.class) == null ||
                        method.getReturnType().getAnnotation(Table.class) != null ||
                        method.getReturnType().isAssignableFrom(List.class)) continue;
                String fieldName = getJsonFieldName( method );
                if (fieldName == null) continue;
                jsonObject.put(fieldName, DbUtil.invokeGetter(method,entity));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public static JSONArray toJsonArray(List<Entity> objects){
        return toJsonArrayGeneric(objects);
    }

    private static JSONArray toJsonArrayGeneric(Object objectsReceived){
        List<Object> objects = (List<Object>) objectsReceived;
        if (objects == null) return null;
        JSONArray jsonArray = new JSONArray();
        for (Object object : objects) {
            JSONObject jsonObject = toJsonObject(object);
            if (jsonObject == null || jsonObject.length() < 1) continue;
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }


    public static <E extends Entity> E fromJsonObject(JSONObject jsonObject, Class entityClass)
            throws JSONException,InstantiationException,IllegalAccessException {
        Field[] declaredFields = entityClass.getDeclaredFields();
        Entity entity = (Entity) entityClass.newInstance();
        for (Field field : declaredFields){
            if (field.getType().getAnnotation(ManyToMany.class) != null ) continue;
            Method getter = DbUtil.getMethod("get",field);
            String fieldName = getJsonFieldName( getter);
            if (fieldName == null) continue;
            if (!jsonObject.has(fieldName) || jsonObject.isNull(fieldName)) continue;
            OneToMany oneToMany = getter.getAnnotation(OneToMany.class);
            Class targetEntity = DbUtil.getListParameterType(getter);
            if (oneToMany != null && oneToMany.targetEntity() != Object.class &&
                    targetEntity == null) targetEntity = oneToMany.targetEntity();
            DbUtil.invokeSetter(entity,DbUtil.getMethod("set",field),
                    jsonObject.get(fieldName) instanceof JSONArray && targetEntity != null ?
                    fromJsonArray(jsonObject.getJSONArray(fieldName),targetEntity) :
                            jsonObject.get(fieldName) instanceof JSONObject ?
                                    fromJsonObject(jsonObject.getJSONObject(fieldName), field.getType()) :
                                        jsonObject.get(fieldName));

        }
        return (E) entity;
    }

    public static <E extends Entity> List<E> fromJsonArray(JSONArray jsonArray,Class entity) throws JSONException,InstantiationException,IllegalAccessException{
        List<Entity> entities = new ArrayList<>();
        for (int i = 0; i < jsonArray.length() ;i++)
            if (jsonArray.get(i) instanceof JSONObject) entities.add(fromJsonObject(jsonArray.getJSONObject(i), entity));
        return (List<E>) entities;
    }



    private static String getJsonFieldName(Method getterMethod){
        if (getterMethod == null) return null;
        Json jsonAnnotation = getterMethod.getAnnotation(Json.class);
        String fieldName = getterMethod.getName().toLowerCase().trim().replaceFirst("get","");
        return  (jsonAnnotation == null) ?  fieldName : jsonAnnotation.fieldName();
    }

}
