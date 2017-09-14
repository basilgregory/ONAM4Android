package com.basilgregory.onam.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.basilgregory.onam.annotations.AfterCreate;
import com.basilgregory.onam.annotations.AfterUpdate;
import com.basilgregory.onam.annotations.BeforeCreate;
import com.basilgregory.onam.annotations.BeforeUpdate;
import com.basilgregory.onam.annotations.JoinTable;
import com.basilgregory.onam.annotations.ManyToMany;
import com.basilgregory.onam.annotations.OneToMany;
import com.basilgregory.onam.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.basilgregory.onam.android.DbUtil.getColumnName;
import static com.basilgregory.onam.android.DbUtil.getMappingForeignColumnNameClass;


/**
 * Created by donpeter on 8/29/17.
 */

public class DBExecutor extends SQLiteOpenHelper {
    Storage storage;
    String dbName;
    private static DBExecutor instance = null;

    public static DBExecutor getInstance(Context context, String dbName, int version) {

        instance = instance == null ?
                new DBExecutor(context, dbName,
                        version) : instance;
        return instance;
    }


    public static DBExecutor getInstance() {
        return instance;
    }

    public static void init(Context context, Object o) {
        if (o.getClass().getAnnotation(com.basilgregory.onam.annotations.DB.class) == null) return;
        getInstance(context, o.getClass().getAnnotation(com.basilgregory.onam.annotations.DB.class).name(),
                o.getClass().getAnnotation(com.basilgregory.onam.annotations.DB.class).version())
                .createTables(context, o);

    }

    private DBExecutor(Context context, String dbName, int version) {
        super(context, dbName, null, version);
        this.dbName = dbName;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


    Entity findRelatedEntityByMapping(Entity entity, Method method) {
        String foreignKeyColumn = getColumnName(method);
        Integer foreignKey = findForeignKeyFromEntity((Class<Entity>) entity.getClass(), entity.getId(), foreignKeyColumn);
        if (foreignKey == null || foreignKey < 1) return null;
        Entity relatedEntity = findById((Class<Entity>) method.getReturnType(), foreignKey);
        DbUtil.invokeSetterForList(entity,DbUtil.getSetterMethod(method),relatedEntity);
        return relatedEntity;
    }

    List<Entity> findRelatedEntitiesByMapping(Entity entity, Object holderClass){
        Method method = holderClass.getClass().getEnclosingMethod();
        OneToMany oneToMany = method.getAnnotation(OneToMany.class);
        ManyToMany manyToMany = method.getAnnotation(ManyToMany.class);
        if (oneToMany != null)
            return findOneToManyRelatedEntities(entity,holderClass,oneToMany,method);
        if (manyToMany != null)
            return findManyToManyRelatedEntities(entity,holderClass,method);
        return null;
    }

    private List<Entity> findOneToManyRelatedEntities(Entity entity, Object holderClass
            ,OneToMany oneToMany,Method method ) {
        Class collectionType = holderClass.getClass().getSuperclass();
        List<Entity> entities = findByProperty(collectionType,
                oneToMany.referencedColumnName(),entity.getId(),null,null);
        DbUtil.invokeSetterForList(entity,DbUtil.getSetterMethod(method),entities);
        return entities;

    }

    /**
     * Mapping table name has to be provided by the user.
     * Foreignkey column names will be auto generated using #{getMappingForeignColumnNameClass}.
     * @param entity
     * @param holderClass
     * @param method
     * @return
     */
    private List<Entity> findManyToManyRelatedEntities(Entity entity, Object holderClass
            ,Method method ) {
        JoinTable joinTable = method.getAnnotation(JoinTable.class);
        if (joinTable == null) return null;
        Cursor cursor =  findByProperty(joinTable.tableName(),
                getMappingForeignColumnNameClass(entity.getClass()),entity.getId(),null,null);
        List<Entity> entities  = EntityBuilder.getEntityFromMappingTable(cursor,holderClass.getClass().getSuperclass());
        DbUtil.invokeSetterForList(entity,DbUtil.getSetterMethod(method),entities);
        return entities;
    }




    Entity findById(Class<Entity> cls, long id) {
        Entity entity = null;
        try {
            entity = EntityBuilder.convertToEntity(cls, getReadableDatabase().rawQuery
                    (QueryBuilder.findById(cls, id), null));
            fetchAndSetFirstDegreeRelatedObject(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return entity;
        }
    }

    private void fetchAndSetFirstDegreeRelatedObject(Entity entity) {
        if (entity == null) return;
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().getAnnotation(Table.class) == null) continue;
            try {
                if (DbUtil.getFetchType(field) != FetchType.EAGER)
                    continue; //Fetching related entity only if fetch type is eager.
                String foreignKeyColumn = getColumnName(field);
                Integer foreignKey = findForeignKeyFromEntity((Class<Entity>) entity.getClass(), entity.getId(), foreignKeyColumn);
                if (foreignKey == null || foreignKey < 1) continue;
                Object relatedObject = DbUtil.invokeGetter(field, entity);
                if (relatedObject != null && relatedObject instanceof Entity)
                    findAndSetUsingReferenceById((Entity) relatedObject, foreignKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * To be called when fetch is needed for entity with id already in the object.
     *
     * @param entity
     */
    private void findAndSetUsingReferenceById(Entity entity) {
        try {
            findAndSetUsingReferenceById(entity, entity.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * May be called when fetch is needed for entity with id passed as attribute.
     *
     * @param entity
     * @param id
     */
    private void findAndSetUsingReferenceById(Entity entity, long id) {
        try {
            EntityBuilder.convertToEntity(entity, getReadableDatabase().rawQuery
                    (QueryBuilder.findById((Class<Entity>) entity.getClass(), id), null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findAndSetByReferenceByRowId(Entity entity, long id) {
        try {
            EntityBuilder.convertToEntity(entity, getReadableDatabase().rawQuery
                    (QueryBuilder.findByROWId((Class<Entity>) entity.getClass(), id), null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Entity findByUniqueProperty(Class<Entity> cls, String columnName, Object value) {
        Entity entity = null;
        try {
            entity =  EntityBuilder.convertToEntity(cls, getReadableDatabase().rawQuery
                    (QueryBuilder.findByUniqueProperty(cls, columnName, value), null));
            fetchAndSetFirstDegreeRelatedObject(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    List<Entity> findByProperty(Class<Entity> cls, String columnName, Object value, Integer startIndex, Integer pageSize) {
        return convertToEntityAndFetchFirstDegreeRelatedEntity(getReadableDatabase().rawQuery
                (QueryBuilder.findByProperty(cls, columnName, value, startIndex, pageSize), null)
                ,cls);
    }

    List<Entity> findAll(Class<Entity> cls,String whereClause, Integer startIndex, Integer pageSize) {
        return convertToEntityAndFetchFirstDegreeRelatedEntity(getReadableDatabase().rawQuery
                (QueryBuilder.findAll(cls, whereClause, startIndex, pageSize), null)
                ,cls);
    }

    List<Entity> findAllWithOrderBy(Class<Entity> cls, String whereClause, String orderByColumn,boolean descending, Integer startIndex, Integer pageSize) {
        return convertToEntityAndFetchFirstDegreeRelatedEntity(getReadableDatabase().rawQuery
                (QueryBuilder.findAll(cls, whereClause,orderByColumn,descending, startIndex, pageSize), null)
                ,cls);
    }

    private List<Entity> convertToEntityAndFetchFirstDegreeRelatedEntity(Cursor cursor,Class<Entity> entityClass){
        List<Entity> entities = new ArrayList<Entity>();
        if (cursor != null && cursor.getCount() > 0 ) {
            cursor.moveToFirst();
            do {
                try {
                    Entity entity = EntityBuilder.convertToEntity(entityClass,cursor,entityClass.getDeclaredFields());
                    fetchAndSetFirstDegreeRelatedObject(entity);
                    entities.add(entity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }while (cursor.moveToNext());
        }
        return entities;
    }

    /**
     * This function will query the mapping table and return the corresponding cursor.
     *
     */
    Cursor findByProperty(String tableName, String columnName, Object value, Integer startIndex, Integer pageSize) {
        try {
            return getReadableDatabase().rawQuery
                    (QueryBuilder.findByProperty(tableName, columnName, value, startIndex, pageSize), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    long delete(Entity entity){
        long numberOfRowsAffected = 0;
        try {
            if (entity.getId() < 1) return numberOfRowsAffected;
            getWritableDatabase().beginTransaction();
            numberOfRowsAffected = getWritableDatabase().delete(DbUtil.getTableName(entity),
                    DB.PRIMARY_KEY_ID + " = ?",new String[] { String.valueOf(entity.getId()) });
            getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getWritableDatabase().endTransaction();
        }
        return numberOfRowsAffected;
    }

    void save(Entity entity) {
        try {
            getWritableDatabase().beginTransaction();
            entity.setReturnValueAsItIs(true); //Makes sure that only entity related values that the user has set will be returned.
            insertOrUpdateEntity(entity);
            findAndInsertMappingObject(entity);
            getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entity.setReturnValueAsItIs(false);
            getWritableDatabase().endTransaction();
        }
    }

    private Entity insertOrUpdateEntity(Entity entity) {
        try {
            lookForRelatedObjects(entity);
            if (entity.getId() > 0) executeUpdate(entity);
            else executeInsert(entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return entity;
        }
    }

    private void findAndInsertMappingObject(Entity masterEntity) throws Exception{
        Method[] methods = masterEntity.getClass().getDeclaredMethods();
        for (Method method: methods) {
            try {
                if (method.getAnnotation(ManyToMany.class) == null) continue;
                JoinTable joinTable = method.getAnnotation(JoinTable.class);
                if (joinTable == null) continue;
                List<Entity> getterResponse = (List<Entity>) method.invoke(masterEntity);
                if (getterResponse == null) continue; //rest will be executed only if the mapping or list of entities returned is not null.
                String tableName = joinTable.tableName();
                Class targetEntity = joinTable.targetEntity();
                removeMapping(tableName,getMappingForeignColumnNameClass(masterEntity.getClass()),masterEntity.getId());
                for (Entity entity:getterResponse){
                    if (entity == null) continue;
                    entity = insertOrUpdateEntity(entity);
                    addMapping(tableName, getMappingForeignColumnNameClass(masterEntity.getClass()),
                            masterEntity.getId(), getMappingForeignColumnNameClass(targetEntity)
                            ,entity.getId());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void lookForRelatedObjects(Entity entity) throws Exception {
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().getAnnotation(Table.class) != null) {
                Object relatedObject = DbUtil.invokeGetter(field, entity);
                if (relatedObject != null && relatedObject instanceof Entity)
                    insertOrUpdateEntity((Entity) relatedObject);
            }
        }
    }


    private void executeUpdate(Entity entity) {
        try {
            AnnotationUtils.executeAnnotationFunction(entity, BeforeUpdate.class);
            ContentValues contentValues = QueryBuilder.insertConflictContentValues(entity,
                    findById((Class<Entity>) entity.getClass(), entity.getId()));
            if (contentValues.size() < 1) return;
            int numberOfRowsUpdated = getWritableDatabase().update
                    (DbUtil.getTableName(entity), contentValues,
                            DB.PRIMARY_KEY_ID + " = ?", new String[]{String.valueOf(entity.getId())});
            if (numberOfRowsUpdated < 1) return;
            findAndSetUsingReferenceById(entity); //if successfull return corresponding Entity.
            AnnotationUtils.executeAnnotationFunction(entity, AfterUpdate.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void executeInsert(Entity entity) {
        try {
            AnnotationUtils.executeAnnotationFunction(entity, BeforeCreate.class);
            long rowId = getWritableDatabase().insert
                    (DbUtil.getTableName(entity), null, QueryBuilder.insertContentValues(entity));
            if (rowId < 0) return; //The row insertion was a failure;
            findAndSetByReferenceByRowId(entity, rowId); //if successfull return corresponding Entity.
            AnnotationUtils.executeAnnotationFunction(entity, AfterCreate.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private int removeMapping(String tableName,String aColumn,long aValue){
        return getWritableDatabase().delete(tableName,
                aColumn + " = ?",new String[] { String.valueOf(aValue) });
    }

    private void addMapping(String tableName,String aColumnName,long aValue,String bColumnName,long bValue){
        try {
            if (doesPropertyExistsForMappingTable(tableName,aColumnName,aValue,bColumnName,bValue)) return;
            ContentValues contentValues = new ContentValues();
            contentValues.put(aColumnName,aValue);
            contentValues.put(bColumnName,bValue);
            getWritableDatabase().insert(tableName,null,contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean doesPropertyExistsForMappingTable(String tableName,
                                              String aColumnName,long aValue,String bColumnName,long bValue) throws Exception{
      Cursor cursor = getReadableDatabase().rawQuery(new StringBuffer("SELECT * FROM ")
              .append(tableName).append(" WHERE ")
              .append(aColumnName)
              .append(String.valueOf(aValue))
              .append(bColumnName)
              .append(String.valueOf(bValue))
              .toString(),null);
        return cursor != null && cursor.getCount() > 0;

    }

    private Cursor getTableInfo(String tableName){
        return getReadableDatabase().rawQuery("PRAGMA table_info(" + tableName + ")", null);
    }



    boolean tableExists(String tableName){
        Cursor cursor=  getReadableDatabase().rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?;",
                new String[]{tableName});
        return cursor != null && cursor.getCount() == 1;
    }

    private Integer findForeignKeyFromEntity(Class<Entity> entityClass, long id, String foreignKeyColumn) {
        Integer foreignKey = -1;
        if (foreignKeyColumn == null) return foreignKey;
        try {
            Cursor cursor = getReadableDatabase().rawQuery
                    (QueryBuilder.findColumnById(entityClass, id, foreignKeyColumn), null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                foreignKey = cursor.getInt(cursor.getColumnIndex(foreignKeyColumn));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return foreignKey;
    }


    // Mark: start of table management.

    private static boolean isThisNewVersionOfDb(Context context, com.basilgregory.onam.annotations.DB dbAnnotation){
        try {
            Storage storage = new Storage(context);
            return dbAnnotation.version() > storage.getCurrentDbMeta(dbAnnotation.name()).version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void setCurrentVersion(Context context,com.basilgregory.onam.annotations.DB dbAnnotation){
        try {
            Storage storage = new Storage(context);
            DbMetaData dbMetaData = storage.getCurrentDbMeta(dbAnnotation.name());
            dbMetaData.version = dbAnnotation.version();
            storage.storeCurrentDbMeta(dbAnnotation.name(),dbMetaData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void createTables(Context context, Object o) {
        try {
            this.storage = new Storage(context);
            com.basilgregory.onam.annotations.DB dbAnnotation =
                    o.getClass().getAnnotation(com.basilgregory.onam.annotations.DB.class);
            if (dbAnnotation == null) return;
            if (!isThisNewVersionOfDb(context,dbAnnotation)) return;

            ArrayList<Class> curatedTablesList = new ArrayList<>();//Curating list for table creation.
            for (Class table:dbAnnotation.tables()){
                if (tableExists(DbUtil.getTableName(table))) continue;
                curatedTablesList.add(table);
            }
            //Creation of fresh tables directly from entities.
            executeNewTableCreation(DDLBuilder.createTables(curatedTablesList));


            //Getting the list of curated mapping tables.
            List<String> mappingTables = new ArrayList<>();
            HashMap<String,String> mappingTableCreateDDL = new HashMap<>();//Curating list for table creation.
            for (Class table:dbAnnotation.tables()){
                Method[] methods = table.getDeclaredMethods();
                for (Method method:methods){
                    if (method.getAnnotation(ManyToMany.class) == null) continue;
                    JoinTable joinTableAnnotation = method.getAnnotation(JoinTable.class);
                    if (joinTableAnnotation == null) continue;
                    String mappingTableName = joinTableAnnotation.tableName();
                    mappingTables.add(mappingTableName);
                    if (tableExists(mappingTableName)) continue;
                    Class targetEntity = joinTableAnnotation.targetEntity();
                    String ddl = DDLBuilder.createMappingTables(mappingTableName,table,targetEntity);
                    mappingTableCreateDDL.put(mappingTableName,ddl);
                }
            }
            //Creation of mapping tables from #{ManyToMany} annotation getters.
            executeNewTableCreation(mappingTableCreateDDL);

            //Droping entities that are no more found in #{DB} annotation.
            executeTableDrop(DMLBuilder.curateAndDropTables
                    (storage.getCurrentDbMeta(dbName).tableNames,dbAnnotation.tables(),mappingTables));

            //executing table renaming from entities.
            executeTableUpdate(DMLBuilder.renameTables(dbAnnotation.tables()));


            HashMap<Class,List<String>> tableCursors = new HashMap<>(dbAnnotation.tables().length);
            for(Class cls:dbAnnotation.tables()) {
                if (cls == null || cls.getAnnotation(Table.class) == null) continue;
                tableCursors.put(cls,DbUtil.getColumnNamesFromCursor(getTableInfo(DbUtil.getTableName(cls))));
            }
            executeTableUpdate(DMLBuilder.addColumns(tableCursors));

            setCurrentVersion(context,dbAnnotation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeTableDrop(HashMap<String,String> dmls){
        if (dmls == null || dmls.size() < 1) return;
        getWritableDatabase().beginTransaction();
        try {
            DbMetaData dbMetaData = storage.getCurrentDbMeta(dbName);
            for (String tableName : dmls.keySet()) {
                try {
                    getWritableDatabase().execSQL(dmls.get(tableName));
                    dbMetaData.tableNames.remove(tableName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            getWritableDatabase().setTransactionSuccessful();
            storage.storeCurrentDbMeta(dbName,dbMetaData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getWritableDatabase().endTransaction();

    }

    private void executeTableUpdate(HashMap<String,String> dmls){
        if (dmls == null || dmls.size() < 1) return;
        getWritableDatabase().beginTransaction();
        for (String tableName : dmls.keySet()) {
            try {
                getWritableDatabase().execSQL(dmls.get(tableName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        getWritableDatabase().setTransactionSuccessful();
        getWritableDatabase().endTransaction();

    }


    private void executeNewTableCreation(HashMap<String,String> ddls){
        if (ddls == null || ddls.size() < 1) return;
        getWritableDatabase().beginTransaction();
        try {
            DbMetaData dbMetaData = storage.getCurrentDbMeta(dbName);
            for (String tableName : ddls.keySet()) {
                try {
                    getWritableDatabase().execSQL(ddls.get(tableName));
                    dbMetaData.tableNames.add(tableName); //Here after execution it can be certain that the table creation was successful.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            getWritableDatabase().setTransactionSuccessful();
            storage.storeCurrentDbMeta(dbName,dbMetaData);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            getWritableDatabase().endTransaction();
        }
    }
}
