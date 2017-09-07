package com.basilgregory.onam.builder;

/**
 * Created by donpeter on 8/28/17.
 */

import android.content.Context;

import java.io.Serializable;
import java.util.List;

/**
 * This class will act as the base class for all Entity POJOs.
 * You need to extent this class for all Entity classes
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
    public Entity() {
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
                ? DBExecutor.getInstance().findRelatedEntity(this, holderClass.getClass().getEnclosingMethod())
                : relatedEntity);
        return  entity;
    }
    protected Object fetch(Object relatedEntityList, Object holderClass) {
        return ((this.refresh || relatedEntityList == null || ((List<Entity>)relatedEntityList).size() < 1)
                ? DBExecutor.getInstance().findRelatedEntities(this, holderClass)
                : relatedEntityList);
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
     * @param context - Context of your application (getApplicationContext())
     * @param activityObject - Current class object or preferably Acitivty object (ex. Activity.this).
     */
    public static void init(Context context, Object activityObject){
        DBExecutor.init(context,activityObject);

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

    public boolean delete(){
        DBExecutor.getInstance().delete(this);
        return false;
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
    public static <E extends Entity> E findById(Class entityClass,long id){
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
    public static List<?> findByProperty(Class entityClass,String columnName,Object value,Integer startIndex,Integer pageSize){
        return (List<?>)DBExecutor.getInstance().findByProperty(entityClass,columnName,value,startIndex,pageSize);

    }

}
