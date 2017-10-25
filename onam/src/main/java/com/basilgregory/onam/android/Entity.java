package com.basilgregory.onam.android;

/**
 * Created by donpeter on 8/28/17.
 */

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * This class will act as the base class for all Entity POJOs.
 * You need to extent this class for all Entity classes
 *
 * Currently supports only ONE database.
 *
 * Sample
 * @Table(name = "events") -- this will be the name that is used to when creating the corresponding table in database.
 * class TableOne extents Entity{
 *
 * }
 *
 * Table annotation may be found at #{{@link com.basilgregory.onam.annotations.Table}}.
 *
 */
public abstract class Entity implements Serializable{
    static final long serialVersionUID = 41L;

    public Entity() {
    }

    //For internal purposes never to be used by developer.
    private boolean returnValueAsItIs = false;

    void setReturnValueAsItIs(boolean returnValueAsItIs) {
        this.returnValueAsItIs = returnValueAsItIs;
    }

    private long id;

    private boolean refresh;

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    /**
     * This will be the primary unique autoincrement field maintained for internal purposes by the ORM.
     * You may use the @{getId} function to access it, but you shall never override this value for any reason.
     *
     * Mandatory function. getter should be implemented for all attibutes (Table columns).
     *
     * You will get the id when you create a new entity by calling #{save} function.
     * @return -- id of the entity will be returned.
     */
    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    public static String PRIMARY_KEY_COLUMN_NAME = DB.PRIMARY_KEY_ID;

    /**
     * This will make a database fetch only when the refresh is set to true (controlled by you) or the entity you are
     * querying is null.
     * If the related entity is initialized by an entity constructor then this will fail.
     *
     * Sample
     *
     * @Table(name = "events")
     * public class Event extends Entity {
     *      private Issue issue; --- correct method.
     * }
     *
     *
     * @Table(name = "events")
     * public class Event extends Entity {
     *      private Issue issue = new Issue(); --- wrong method!! should never be initialized.
     * }
     *
     * @param relatedEntity
     * @param holderClass
     * @return
     */
    protected <E extends Entity> E fetch(Entity relatedEntity, Object holderClass) {
        E entity = (E)((this.refresh || relatedEntity == null)
                ? DBExecutor.getInstance().findRelatedEntityByMapping(this, holderClass.getClass().getEnclosingMethod())
                : relatedEntity);
        return  entity;
    }
    protected <E extends Entity> List<E> fetch(List<E>  relatedEntityList, Object holderClass) {
        return (List<E>) (returnValueAsItIs ? relatedEntityList :
                ((this.refresh || relatedEntityList == null || relatedEntityList.size() < 1
                        ? DBExecutor.getInstance().findRelatedEntitiesByMapping(this, holderClass)
                        : relatedEntityList)));
    }

    /**
     * This function needs to be called in the class where @DB #{{@link com.basilgregory.onam.annotations.DB}}
     * annotation is used, which should contain list of all Entity classes that extends #{{@link Entity}} class.
     *
     * Sample annotation.
     * @DB(name = "database_name",
     *       tables = {
     *           TableOne.class,
     *           TableTwo.class
     *       })
     *
     * Sample #{init} call
     * Entity.init(getApplicationContext(),Activity.this);
     *
     * @param activityObject - Current class object or preferably Acitivty object (ex. Activity.this).
     */
    public static void init(Activity activityObject, Object classWithDBAnnotation){
        DBExecutor.init(activityObject.getApplicationContext(),classWithDBAnnotation);
    }

    public static void init(Activity activityObject){
        DBExecutor.init(activityObject.getApplicationContext(),activityObject);
    }

    public static void log(boolean debug){
        L.getInstance().setDebug(debug);
    }
    public static void log(boolean debug,boolean verbose){
        log(debug);
        L.getInstance().setVerbose(verbose);
    }




    /**
     * The ORM checks for the presence of id. See #{getId}, which is a value above 0( You wont be able to manually set this value ).
     * if the value is present, then update operation is carried out.
     * Update only happens for fields that are set and is different from the one that is already in the database.
     * if id field is not found,new row insertion is carried out with an auto generated id.
     *
     *
     * After save, the Entity object that called #{save} will contain an auto increment, unique primary key
     *      (not in case of update, as it will already have an id value.).
     * You may use this id for #{findById} function call.
     * See #{getId} for more details on how to get id value.
     *
     * During save, getterfunctions will return null. so refrain from calling getter function while the save function is called
     *
     */
    public void save(){
        DBExecutor.getInstance().save(this);
    }

    /**
     * Will delete the current entity on which the function is called.
     * @return true if the row was deleted.
     */
    public boolean delete(){
        return DBExecutor.getInstance().delete(this) == 1;
    }

    public static boolean truncate(Class entityClass){
        return DBExecutor.getInstance().truncate(entityClass);
    }

    /**
     *
     * @param entityClass - This will be your Entity class.
     *
     *
     * Sample Code:
     * Suppose you have a Entity named 'User'.
     * User user = (User) Entity.findById(User.class,12);
     * You need to cast the return Object to approriate Entity that you have created.
     * The returned 'user' object will have all the column values from the row with id '12'
     *
     *
     * @param id - This will be the primary key that is maintained by ORM.
     *           See #{getId} for more details on how to get id value.
     * @return Entity - You need to cast this to the appropriate entity subclass.
     */
    public static <E extends Entity> E find(Class entityClass,long id){
        return (E) DBExecutor.getInstance().findById(entityClass,id);

    }

    /**
     *
     * @param columnName - implicit column name will be the Entity class attribute.
     * @param value
     * @return - returns a single Entity object as the property is unique.
     */
    public static <E extends Entity> E  findByUniqueProperty(Class entityClass,String columnName,Object value){
        return (E) DBExecutor.getInstance().findByUniqueProperty(entityClass,columnName,value);
    }

    /**
     *
     * @param entityClass
     * @param columnName - implicit column name will be the Entity class attribute.
     * @param value
     * @param startIndex - null hack
     * @param pageSize - null hack
     * @return
     */
    public static <E extends Entity> List<E> findByProperty(Class entityClass,String columnName,Object value,Integer startIndex,Integer pageSize){
        return (List<E>)DBExecutor.getInstance().findByProperty(entityClass,columnName,value,startIndex,pageSize);
    }

    public static <E extends Entity> List<E> findByProperty(Class entityClass,String columnName,Object value){
        return (List<E>)DBExecutor.getInstance().findByProperty(entityClass,columnName,value,null,null);
    }

    public static <E extends Entity> List<E> findByProperty(Class entityClass,String columnName,Object value,Integer limit){
        return (List<E>)DBExecutor.getInstance().findByProperty(entityClass,columnName,value,null,limit);
    }

    public static <E extends Entity> List<E> findAll(Class entityClass, Integer startIndex,Integer pageSize){
        return (List<E>) DBExecutor.getInstance().findAll(entityClass,null,startIndex,pageSize);
    }

    public static <E extends Entity> List<E> findAll(Class entityClass){
        return (List<E>) DBExecutor.getInstance().findAll(entityClass,null,null,null);
    }

    public static <E extends Entity> List<E> findAll(Class entityClass, Integer limit){
        return (List<E>) DBExecutor.getInstance().findAll(entityClass,null,null,limit);
    }

    public static <E extends Entity> List<E> findAll(Class entityClass,String whereClause,Integer startIndex,Integer pageSize){
        return (List<E>) DBExecutor.getInstance().findAll(entityClass,whereClause,startIndex,pageSize);
    }

    public static <E extends Entity> List<E> findAll(Class entityClass,String whereClause, Integer limit){
        return (List<E>) DBExecutor.getInstance().findAll(entityClass,whereClause,null,limit);
    }
    public static <E extends Entity> List<E> findAll(Class entityClass,String whereClause){
        return (List<E>) DBExecutor.getInstance().findAll(entityClass,whereClause,null,null);
    }

    public static <E extends Entity> List<E> findAll(Class entityClass,String orderByColumn,boolean descending,Integer startIndex,Integer pageSize){
        return (List<E>) DBExecutor.getInstance().findAllWithOrderBy(entityClass,null,orderByColumn,descending,startIndex,pageSize);
    }

    public static <E extends Entity> List<E> findAll(Class entityClass,String orderByColumn,boolean descending){
        return (List<E>) DBExecutor.getInstance().findAllWithOrderBy(entityClass,null,orderByColumn,descending,null,null);
    }

    public static <E extends Entity> List<E> findAll(Class entityClass,String orderByColumn,boolean descending,Integer limit){
        return (List<E>) DBExecutor.getInstance().findAllWithOrderBy(entityClass,null,orderByColumn,descending,null,limit);
    }

    public static <E extends Entity> List<E> findAll(Class entityClass,String whereClause, String orderByColumn,boolean descending,Integer startIndex,Integer pageSize){
        return (List<E>) DBExecutor.getInstance().findAllWithOrderBy(entityClass,whereClause,orderByColumn,descending,startIndex,pageSize);
    }
    public static <E extends Entity> List<E> findAll(Class entityClass,String whereClause, String orderByColumn,boolean descending){
        return (List<E>) DBExecutor.getInstance().findAllWithOrderBy(entityClass,whereClause,orderByColumn,descending,null,null);
    }
    public static <E extends Entity> List<E> findAll(Class entityClass,String whereClause, String orderByColumn,boolean descending,Integer limit){
        return (List<E>) DBExecutor.getInstance().findAllWithOrderBy(entityClass,whereClause,orderByColumn,descending,null,limit);
    }

    public JSONObject toJSON(){
        return toJSON(this);
    }

    public static JSONObject toJSON(Object object){
        return JSONParser.toJsonObject(object);
    }

    public static JSONArray toJSONArray(List<? extends Entity> objects){
        return JSONParser.toJsonArray(objects);
    }

    public static Entity fromJSON(JSONObject json,Class rootEntity){
        try {
            return JSONParser.fromJsonObject(json,rootEntity);
        } catch (JSONException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Entity> fromJSON(JSONArray jsonArray,Class rootEntity){
        try {
            return JSONParser.fromJsonArray(jsonArray,rootEntity);
        } catch (JSONException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
