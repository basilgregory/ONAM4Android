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

public class JSONParser {
    /**
     * Converts only primitive fields.
     * Convertion restricted to one layer.
     * @param entity
     * @return null if conversion fails
     */
    public static JSONObject toJsonObject(Entity entity){
        if (entity == null) return null;
        Field[] fields = entity.getClass().getDeclaredFields();
        JSONObject jsonObject = new JSONObject();
        for (Field field : fields) {
            try {
                if (field.getType().getAnnotation(Table.class) != null) continue;
                String fieldName = getJsonFieldName(field, DbUtil.getMethod("get",field));
                if (fieldName == null) continue;
                jsonObject.put(fieldName, DbUtil.invokeGetter(field,entity));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public static JSONArray toJsonArray(List<Entity> entities){
        if (entities == null) return null;
        JSONArray jsonArray = new JSONArray();
        for (Entity entity : entities)  jsonArray.put(toJsonObject(entity));
        return jsonArray;
    }



    public static <E extends Entity> E fromJsonObject(JSONObject jsonObject, Class entityClass)
            throws JSONException,InstantiationException,IllegalAccessException {
        Field[] declaredFields = entityClass.getDeclaredFields();
        Entity entity = (Entity) entityClass.newInstance();
        for (Field field : declaredFields){
            Class type = field.getType();
            if (type.getAnnotation(ManyToMany.class) != null ) continue;
            Method getter = DbUtil.getMethod("get",field);
            String fieldName = getJsonFieldName(field, getter);
            if (fieldName == null) continue;
            if (!jsonObject.has(fieldName) || jsonObject.isNull(fieldName)) continue;
            OneToMany oneToMany = getter.getAnnotation(OneToMany.class);
            ManyToMany manyToMany = getter.getAnnotation(ManyToMany.class);
            Class targetEntity = oneToMany == null?
                    manyToMany == null ?
                            null : manyToMany.targetEntity() : oneToMany.targetEntity();
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


    private static String getJsonFieldName(Field field,Method getterMethod){
        if (getterMethod == null) return null;
        Json jsonAnnotation = getterMethod.getAnnotation(Json.class);
        return  (jsonAnnotation == null) ? field.getName() : jsonAnnotation.fieldName();
    }

}
