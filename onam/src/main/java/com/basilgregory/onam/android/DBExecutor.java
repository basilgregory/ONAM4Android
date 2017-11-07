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
import com.basilgregory.onam.annotations.ManyToMany;
import com.basilgregory.onam.annotations.OneToMany;
import com.basilgregory.onam.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.basilgregory.onam.android.DbUtil.getColumnName;
import static com.basilgregory.onam.android.DbUtil.getMappingForeignColumnNameClass;


/**
 * Created by donpeter on 8/29/17.
 */

class DBExecutor extends SQLiteOpenHelper {


    //region Boiler plate code
    Storage storage;
    String dbName;
    private static DBExecutor instance = null;
    private static Map<String,DBExecutor> instances = new HashMap<String,DBExecutor>();
    public static DBExecutor getInstance(Context context, String dbName, int version) {
        if (instances.containsKey(dbName)) instance = instances.get(dbName);
        instance = instance == null ?
                new DBExecutor(context, dbName,
                        version) : instance;
        instances.put(dbName,instance);
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
    //endregion

    //Interface methods used by Entity.java

    Entity findRelatedEntityByMapping(Entity entity, Method method) {
        L.v("executing method "+method.getName()+" in entity "+entity.getClass().getSimpleName());
        String foreignKeyColumn = getColumnName(method);
        L.v("fetching foreign key from column "+foreignKeyColumn+" in entity "+entity.getClass().getSimpleName());
        Integer foreignKey = findForeignKeyFromEntity((Class<Entity>) entity.getClass(), entity.getId(), foreignKeyColumn);
        if (foreignKey == null || foreignKey < 1) return null;
        Entity relatedEntity = findById((Class<Entity>) method.getReturnType(), foreignKey);
        L.v("fetching related entity "+method.getReturnType().getSimpleName() + "with id "+foreignKey);
        DbUtil.invokeSetter(entity,DbUtil.getSetterMethod(method),relatedEntity);
        return relatedEntity;
    }


    List<Entity> findRelatedEntitiesByMapping(Entity entity, Object holderClass){
        Method method = holderClass.getClass().getEnclosingMethod();
        L.v("executing method "+method.getName()+" in entity "+entity.getClass().getSimpleName());
        OneToMany oneToMany = method.getAnnotation(OneToMany.class);
        ManyToMany manyToMany = method.getAnnotation(ManyToMany.class);
        if (oneToMany != null)
            return findOneToManyRelatedEntities(entity,holderClass,oneToMany,method);
        if (manyToMany != null)
            return findManyToManyRelatedEntities(manyToMany,entity,holderClass,method);
        L.w("No required mapping found (missed @OneToMany/@ManyToMany mapping annotation ?)");
        return null;
    }


    Entity findById(Class<Entity> cls, long id) {
        Entity entity = null;
        try {
            entity = EntityBuilder.convertToEntity(cls, getReadableDatabase().rawQuery
                    (QueryBuilder.findById(cls, id), null));
            fetchAndSetFirstDegreeRelatedObject(entity);
        } catch (Exception e) {
            L.w("fetching entity "+cls.getSimpleName() + "with id "+id+" failed");
            L.e(e);
        }finally {
            return entity;
        }
    }

    Entity findByUniqueProperty(Class<Entity> cls, String columnName, Object value) {
        Entity entity = null;
        try {
            entity =  EntityBuilder.convertToEntity(cls, getReadableDatabase().rawQuery
                    (QueryBuilder.findByUniqueProperty(cls, columnName, value), null));
            fetchAndSetFirstDegreeRelatedObject(entity);
        } catch (Exception e) {
            L.w("fetching entity "+cls.getSimpleName() + "for unique property "+columnName
                    +" with value "+value+" failed");
            L.e(e);
        }
        return entity;
    }

    List<Entity> findByProperty(Class<Entity> cls, String columnName, Object value, Integer startIndex, Integer pageSize) {
        return convertToEntityAndFetchFirstDegreeRelatedEntity(getReadableDatabase().rawQuery
                        (QueryBuilder.findByProperty(cls, columnName, value, startIndex, pageSize), null)
                ,cls);
    }

    List<Entity> findByProperty(Class<Entity> cls,String columnName,Object value,String orderByColumn, boolean descending, Integer startIndex,Integer pageSize){
        return convertToEntityAndFetchFirstDegreeRelatedEntity(getReadableDatabase().rawQuery
                        (QueryBuilder.findByProperty(cls, columnName, value, orderByColumn, descending, startIndex, pageSize), null)
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

    // Private methods

    private List<Entity> findOneToManyRelatedEntities(Entity entity, Object holderClass
            ,OneToMany oneToMany,Method method ) {
        Class collectionType = holderClass.getClass().getSuperclass();
        List<Entity> entities = findByProperty(collectionType,
                DbUtil.getReferencedColumnName(oneToMany,entity.getClass()),entity.getId(),null,null);
        DbUtil.invokeSetter(entity,DbUtil.getSetterMethod(method),entities);
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
    private List<Entity> findManyToManyRelatedEntities(ManyToMany manyToMany,Entity entity, Object holderClass
            , Method method ) {
        Cursor cursor =  findByProperty(manyToMany.tableName(),
                getMappingForeignColumnNameClass(entity.getClass()),entity.getId(),null,null);
        List<Entity> entities  = EntityBuilder.getEntityFromMappingTable(cursor,holderClass.getClass().getSuperclass());
        DbUtil.invokeSetter(entity,DbUtil.getSetterMethod(method),entities);
        return entities;
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
                L.v("fetching foreign key from column "+foreignKeyColumn+" in entity "+entity.getClass().getSimpleName()
                        +" for field "+field.getName());
                Integer foreignKey = findForeignKeyFromEntity((Class<Entity>) entity.getClass(), entity.getId(), foreignKeyColumn);
                L.v("fetching related entity "+entity.getClass().getSimpleName() + "with id "+foreignKey);
                if (foreignKey == null || foreignKey < 1) continue;
                Object relatedObject = DbUtil.invokeGetter(field, entity);
                if (relatedObject != null && relatedObject instanceof Entity)
                    findAndSetUsingReferenceById((Entity) relatedObject, foreignKey);
            } catch (Exception e) {
                L.w("Error while fetching related entity "+entity.getClass().getSimpleName()+" for field "+
                        field.getName());
                L.e(e);
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
            L.vi("About to remove entity "+entity.getClass().getSimpleName()+" with id "+entity.getId());
            if (entity.getId() < 1) {
                L.v("Cant remove entity "+entity.getClass().getSimpleName()+" with id "+entity.getId());
                L.v("Entity id has to be greater than 1");
                return numberOfRowsAffected;
            }
            L.d("Removing entity "+entity.getClass().getSimpleName()+" with id "+entity.getId());
            getWritableDatabase().beginTransaction();
            numberOfRowsAffected = getWritableDatabase().delete(DbUtil.getTableName(entity),
                    DB.PRIMARY_KEY_ID + " = ?",new String[] { String.valueOf(entity.getId()) });
            getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            L.w("Error while removing entity "+entity.getClass().getSimpleName()+" with id "+entity.getId());
            L.e(e);
        } finally {
            getWritableDatabase().endTransaction();
        }
        return numberOfRowsAffected;
    }

    void save(Entity entity) {
        try {
            L.vi("About to save entity "+entity.getClass().getSimpleName()+" with id "+entity.getId());
            getWritableDatabase().beginTransaction();
            entity.setReturnValueAsItIs(true); //Makes sure that only entity related values that the user has set will be returned.
            insertOrUpdateEntity(entity);
            findAndInsertMappingObject(entity);
            getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            L.w("Error while saving entity "+entity.getClass().getSimpleName());
            L.e(e);
        } finally {
            entity.setReturnValueAsItIs(false);
            getWritableDatabase().endTransaction();
        }
    }

    private Entity insertOrUpdateEntity(Entity entity) {
        try {
            lookForRelatedObjects(entity);
            if (entity.getId() > 0) executeUpdate(entity, null);
            else executeInsert(entity, null);
        } catch (Exception e) {
            L.w("Error while saving entity "+entity.getClass().getSimpleName());
            L.e(e);
        } finally {
            return entity;
        }
    }

    /**
     * To be used only for many mapping.
     * This will not look for related entities.
     * @param entity  Entity to be saved
     * @param parentEntity Parent Entity incase of many mapping.
     * @return
     */
    private Entity insertOrUpdateEntity(Entity entity,Entity parentEntity) {
        try {
            if (entity.getId() > 0) executeUpdate(entity, parentEntity);
            else executeInsert(entity, parentEntity);
        } catch (Exception e) {
            L.w("Error while saving entity "+entity.getClass().getSimpleName());
            L.e(e);
        } finally {
            return entity;
        }
    }

    private void findAndInsertMappingObject(Entity masterEntity) throws Exception{
        Method[] methods = masterEntity.getClass().getDeclaredMethods();
        for (Method method: methods) {
            try {
                ManyToMany manyToMany = method.getAnnotation(ManyToMany.class);
                if (manyToMany == null) continue;
                List<Entity> getterResponse = (List<Entity>) method.invoke(masterEntity);
                if (getterResponse == null) continue; //rest will be executed only if the mapping or list of entities returned is not null.
                String tableName = manyToMany.tableName();

                Class targetEntity = DbUtil.getListParameterType(method);
                if (manyToMany != null && manyToMany.targetEntity() != Object.class &&
                        targetEntity == null) targetEntity = manyToMany.targetEntity();

                removeMapping(tableName,getMappingForeignColumnNameClass(masterEntity.getClass()),masterEntity.getId());
                for (Entity entity:getterResponse){
                    if (entity == null) continue;
                    entity = insertOrUpdateEntity(entity);
                    addMapping(tableName, getMappingForeignColumnNameClass(masterEntity.getClass()),
                            masterEntity.getId(), getMappingForeignColumnNameClass(targetEntity)
                            ,entity.getId());
                }
            } catch (IllegalAccessException e) {
                L.w("Error while saving mapping entity "+masterEntity.getClass().getSimpleName()
                        +" with method "+method.getName());
                L.e(e);
            } catch (IllegalArgumentException e) {
                L.w("Error while saving mapping entity "+masterEntity.getClass().getSimpleName()
                        +" with method "+method.getName());
                L.e(e);
            } catch (Exception e) {
                L.w("Error while saving mapping entity "+masterEntity.getClass().getSimpleName()
                        +" with method "+method.getName());
                L.e(e);
            }
        }
    }

    private void lookForRelatedObjects(Entity entity) throws Exception {
        L.vi("Looking for related entities for "+entity.getClass().getSimpleName());
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            Method getterMethod = DbUtil.getMethod("get",field);
            if (getterMethod == null) continue;
            if (field.getType().getAnnotation(Table.class) == null &&
                    getterMethod.getAnnotation(OneToMany.class) == null ) continue;
            if (getterMethod.getAnnotation(OneToMany.class) != null ){ //Returns a list.
                if (entity.getId() < 1) executeInsert(entity, null); // Parent entity id required for many mapping.
                List<Entity> relatedEntities = (List<Entity>) DbUtil.invokeGetter(field, entity);
                if (relatedEntities == null) continue;
                for (Entity relatedEntity: relatedEntities )
                    if (relatedEntity != null) insertOrUpdateEntity(relatedEntity, entity);
            }else{
                Object relatedObject = DbUtil.invokeGetter(field, entity);
                if (relatedObject == null) continue;
                if (relatedObject != null && relatedObject instanceof Entity)
                    insertOrUpdateEntity((Entity) relatedObject);
            }
        }
    }


    private void executeUpdate(Entity entity, Entity parentEntity) {
        try {
            AnnotationUtils.executeAnnotationFunction(entity, BeforeUpdate.class);
            ContentValues contentValues = QueryBuilder.insertConflictContentValues(entity,
                    findById((Class<Entity>) entity.getClass(), entity.getId()), parentEntity);
            if (contentValues.size() < 1) return;
            L.d("Update operation "+DbUtil.getTableName(entity));
            L.d("Update entity row with id "+String.valueOf(entity.getId()));
            L.d("Values "+contentValues.toString());
            int numberOfRowsUpdated = getWritableDatabase().update
                    (DbUtil.getTableName(entity), contentValues,
                            DB.PRIMARY_KEY_ID + " = ?", new String[]{String.valueOf(entity.getId())});
            L.d("Number of rows affected "+numberOfRowsUpdated);
            if (numberOfRowsUpdated < 1) return;
            findAndSetUsingReferenceById(entity); //if successfull return corresponding Entity.
            AnnotationUtils.executeAnnotationFunction(entity, AfterUpdate.class);
        } catch (Exception e) {
            L.w("Error while entity update "+entity.getClass().getSimpleName());
            L.e(e);
        }
    }


    private void executeInsert(Entity entity, Entity parentEntity) {
        try {
            AnnotationUtils.executeAnnotationFunction(entity, BeforeCreate.class);
            ContentValues contentValues = QueryBuilder.insertContentValues(entity, parentEntity);
            L.d("Insert operation "+DbUtil.getTableName(entity));
            L.d("Values "+contentValues.toString());
            long rowId = getWritableDatabase().insert
                    (DbUtil.getTableName(entity), null, contentValues);
            L.d("RowId created "+rowId);
            if (rowId < 0) return; //The row insertion was a failure;
            findAndSetByReferenceByRowId(entity, rowId); //if successfull return corresponding Entity.
            AnnotationUtils.executeAnnotationFunction(entity, AfterCreate.class);
        } catch (Exception e) {
            L.w("Error while insertion entity "+entity.getClass().getSimpleName());
            L.e(e);
        }
    }



    private int removeMapping(String tableName,String aColumn,long aValue){
        L.v("Flushing mapping in table "+tableName+" for column "+aColumn+" with value "+aValue);
        return getWritableDatabase().delete(tableName,
                aColumn + " = ?",new String[] { String.valueOf(aValue) });
    }

    private void addMapping(String tableName,String aColumnName,long aValue,String bColumnName,long bValue){
        try {
            if (doesPropertyExistsForMappingTable(tableName,aColumnName,aValue,bColumnName,bValue)) return;
            ContentValues contentValues = new ContentValues();
            contentValues.put(aColumnName,aValue);
            contentValues.put(bColumnName,bValue);
            L.v("Adding mapping row in table "+tableName+" for column "+aColumnName+" with value "+aValue+
                        " and for column "+bColumnName+" with value "+bValue);
            getWritableDatabase().insert(tableName,null,contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean doesPropertyExistsForMappingTable(String tableName,
                                              String aColumnName,long aValue,String bColumnName,long bValue) throws Exception{
      Cursor cursor = getReadableDatabase().rawQuery(QueryBuilder.queryMappingTable(tableName,
                aColumnName, aValue, bColumnName, bValue),null);
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
    //region Database management
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
            L.d("New version/fresh database found");

            ArrayList<Class> curatedTablesList = new ArrayList<>();//Curating list for table creation.
            for (Class table:dbAnnotation.tables()){
                if (tableExists(DbUtil.getTableName(table))) continue;
                curatedTablesList.add(table);
            }
            //Creation of fresh tables directly from entities.
            executeNewTableCreation(DDLBuilder.createTables(curatedTablesList), false);


            //Getting the list of curated mapping tables.
            List<String> mappingTables = new ArrayList<>();
            HashMap<String,String> mappingTableCreateDDL = new HashMap<>();//Curating list for table creation.
            for (Class table:dbAnnotation.tables()){
                Method[] methods = table.getDeclaredMethods();
                for (Method method:methods){
                    ManyToMany manyToMany = method.getAnnotation(ManyToMany.class);
                    if ( manyToMany == null) continue;
                    String mappingTableName = manyToMany.tableName();
                    mappingTables.add(mappingTableName);
                    if (tableExists(mappingTableName)) continue;

                    Class targetEntity = DbUtil.getListParameterType(method);
                    if (manyToMany != null && manyToMany.targetEntity() != Object.class &&
                            targetEntity == null) targetEntity = manyToMany.targetEntity();

                    mappingTableCreateDDL.put(mappingTableName,
                            DDLBuilder.createMappingTables(mappingTableName,table,targetEntity));
                }
            }
            //Creation of mapping tables from #{ManyToMany} annotation getters.
            executeNewTableCreation(mappingTableCreateDDL, true);

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
            L.w("Error in database management");
            L.e(e);
        }
    }

    private void executeTableDrop(HashMap<String,String> dmls){
        if (dmls == null || dmls.size() < 1) return;
        getWritableDatabase().beginTransaction();
        try {
            DbMetaData dbMetaData = storage.getCurrentDbMeta(dbName);
            for (String tableName : dmls.keySet()) {
                try {
                    L.d("SQL : "+dmls.get(tableName));
                    getWritableDatabase().execSQL(dmls.get(tableName));
                    dbMetaData.tableNames.remove(tableName);
                } catch (Exception e) {
                    L.w("Error while table drop");
                    L.e(e);
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
                L.d("SQL : "+dmls.get(tableName));
                getWritableDatabase().execSQL(dmls.get(tableName));
            } catch (Exception e) {
                L.w("Error while table update");
                L.e(e);
            }
        }
        getWritableDatabase().setTransactionSuccessful();
        getWritableDatabase().endTransaction();

    }

    /**
     * Function that takes ddls and executes them in order they are received.
     * If ddls contain mapping tables then the entire list should be for mapping tables
     * and other ddls should be called separately.
     * @param ddls -- the list of ddl commands
     * @param mapping -- specifies whether the ddls are for mapping tabe or not
     */
    private void executeNewTableCreation(HashMap<String,String> ddls, boolean mapping){
        if (ddls == null || ddls.size() < 1) return;
        getWritableDatabase().beginTransaction();
        try {
            DbMetaData dbMetaData = storage.getCurrentDbMeta(dbName);
            for (String tableName : ddls.keySet()) {
                try {
                    L.d("SQL : "+ddls.get(tableName));
                    getWritableDatabase().execSQL(ddls.get(tableName));
                    if (mapping) dbMetaData.mappingTableNames.add(tableName);
                    dbMetaData.tableNames.add(tableName); //Here after execution it can be certain that the table creation was successful.
                } catch (Exception e) {
                    L.e("SQL : table create ddl failed for "+ddls.get(tableName));
                    L.e(e);
                }
            }
            getWritableDatabase().setTransactionSuccessful();
            storage.storeCurrentDbMeta(dbName,dbMetaData);
        } catch (Exception e) {
            L.w("Error while table creation");
            L.e(e);
        }finally {
            getWritableDatabase().endTransaction();
        }
    }

    boolean truncate(Class<Entity> entity){
        if (entity == null) {
            L.w("SQL : Entity passed is null -- truncate failed");
            return false;
        }
        String tableName = DbUtil.getTableName(entity);
        try {
            L.v("SQL : Truncating table "+tableName);
            getWritableDatabase().beginTransaction();
            getWritableDatabase().execSQL("DELETE FROM "+ tableName);
            getWritableDatabase().setTransactionSuccessful();
            L.v("SQL : Truncating table "+tableName+" complete");
            return true;
        } catch (Exception e) {
            L.e("SQL : table truncate ddl failed for "+tableName);
            L.e(e);
            return false;
        }finally {
            getWritableDatabase().endTransaction();
        }

    }

    //endregion
}
