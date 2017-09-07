package com.basilgregory.onam.builder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.basilgregory.onam.annotations.JoinTable;
import com.basilgregory.onam.annotations.ManyToMany;
import com.basilgregory.onam.annotations.OneToMany;
import com.basilgregory.onam.annotations.Table;
import com.basilgregory.onam.constants.ClassCursorPair;
import com.basilgregory.onam.constants.DB;
import com.basilgregory.onam.constants.Storage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.basilgregory.onam.builder.DbUtil.getColumnName;
import static com.basilgregory.onam.builder.DbUtil.getMappingForeignColumnNameClass;
import static com.basilgregory.onam.builder.EntityBuilder.convertToEntity;


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
        if (o.getClass().getAnnotation(com.basilgregory.onam.annotations.DB.class) == null)
            return;
        getInstance(context, o.getClass().getAnnotation(com.basilgregory.onam.annotations.DB.class).name(), o.getClass().getAnnotation(com.basilgregory.onam.annotations.DB.class).version())
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
        Log.d(DBExecutor.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        onCreate(db);
    }

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
        this.storage = new Storage(context);
        try {
            com.basilgregory.onam.annotations.DB dbAnnotation =
                    o.getClass().getAnnotation(com.basilgregory.onam.annotations.DB.class);
            if (dbAnnotation == null) return;
            if (!isThisNewVersionOfDb(context,dbAnnotation)) return;

            ArrayList<Class> curatedTablesList = new ArrayList<>();//Curating list for table creation.
            for (Class table:dbAnnotation.tables()){
                if (tableExists(DbUtil.getTableName(table))) continue;
                curatedTablesList.add(table);
            }
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
            executeNewTableCreation(mappingTableCreateDDL);


            executeTableDrop(DMLBuilder.curateAndDropTables
                    (storage.getCurrentDbMeta(dbName).tableNames,dbAnnotation.tables(),mappingTables));
            executeTableUpdate(DMLBuilder.renameTables(dbAnnotation.tables()));


            List<ClassCursorPair> classCursorPairs = new ArrayList<ClassCursorPair>(dbAnnotation.tables().length);
            for(Class cls:dbAnnotation.tables()) {
                if (cls == null || cls.getAnnotation(Table.class) == null) continue;
                ClassCursorPair classCursorPair = new ClassCursorPair();
                classCursorPair.setaClass(cls);
                classCursorPair.setCursor(getTableInfo(DbUtil.getTableName(cls)));
                classCursorPairs.add(classCursorPair);
            }

            executeTableUpdate(DMLBuilder.addColumns(classCursorPairs));



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
                    getWritableDatabase().execSQL(ddls.get(tableName)); //Here after execution it can be certain that the table creation was successfull.
                    dbMetaData.tableNames.add(tableName);
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

    Entity findRelatedEntity(Entity entity, Method method) {
        String foreignKeyColumn = getColumnName(method);
        Integer foreignKey = findForeignKeyFromEntity((Class<Entity>) entity.getClass(), entity.getId(), foreignKeyColumn);
        if (foreignKey == null || foreignKey < 1) return null;
        Entity relatedEntity = findById((Class<Entity>) method.getReturnType(), foreignKey);
        Method setterMethod = DbUtil.getSetterMethod(method);
        if (setterMethod == null) return relatedEntity;
        try {
            setterMethod.invoke(entity, relatedEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return relatedEntity;
    }

    List<Entity> findRelatedEntities(Entity entity, Object holderClass){
        Method method = holderClass.getClass().getEnclosingMethod();
        OneToMany oneToMany = method.getAnnotation(OneToMany.class);
        ManyToMany manyToMany = method.getAnnotation(ManyToMany.class);
        if (oneToMany != null)
            return findOneToManyRelatedEntities(entity,holderClass,oneToMany,method);
        else if (manyToMany != null)
            return findManyToManyRelatedEntities(entity,holderClass,method);
        return new ArrayList<Entity>();
    }

    private List<Entity> findOneToManyRelatedEntities(Entity entity, Object holderClass
            ,OneToMany oneToMany,Method method ) {
        Class collectionType = holderClass.getClass().getSuperclass();
        List<Entity> entities =  findByProperty(collectionType,
                oneToMany.referencedColumnName(),entity.getId(),null,null);
        Method setterMethod = DbUtil.getSetterMethod(method);
        if (setterMethod == null) return entities;
        try {
            setterMethod.invoke(entity, entities);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entities;

    }

    /**
     * Here the mapping table name has to be provided by the user.
     * the foreignkey column names will be auto generated.
     * @param entity
     * @param holderClass
     * @param method
     * @return
     */
    private List<Entity> findManyToManyRelatedEntities(Entity entity, Object holderClass
            ,Method method ) {
        List<Entity> entities = new ArrayList<>();
        JoinTable joinTable = method.getAnnotation(JoinTable.class);
        if (joinTable == null) return new ArrayList<>();
        Class collectionType = holderClass.getClass().getSuperclass();
        Cursor cursor =  findByProperty(joinTable.tableName(),
                getMappingForeignColumnNameClass(entity.getClass()),entity.getId(),null,null);
        if (cursor == null || cursor.getCount() < 1) return null;
        cursor.moveToFirst();
        do{
            long foreignKey = cursor.getLong
                    (cursor.getColumnIndex(
                            getMappingForeignColumnNameClass(collectionType)));
            Entity queriedEntity = findById(collectionType,foreignKey);
            if (queriedEntity == null) continue;
            entities.add(queriedEntity);
        }while (cursor.moveToNext());
        Method setterMethod = DbUtil.getSetterMethod(method);
        if (setterMethod == null) return entities;
        try {
            setterMethod.invoke(entity, entities);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entities;
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

    Entity findById(Class<Entity> cls, long id) {
        Entity entity = null;
        try {
            entity = convertToEntity(cls, getReadableDatabase().rawQuery
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
            convertToEntity(entity, getReadableDatabase().rawQuery
                    (QueryBuilder.findById((Class<Entity>) entity.getClass(), id), null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findAndSetByReferenceByRowId(Entity entity, long id) {
        try {
            convertToEntity(entity, getReadableDatabase().rawQuery
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
        List<Entity> entities = new ArrayList<Entity>();
        try {
            Cursor cursor = getReadableDatabase().rawQuery
                    (QueryBuilder.findByProperty(cls, columnName, value, startIndex, pageSize), null);
            if (cursor != null && cursor.getCount() > 0 ) {
                cursor.moveToFirst();
                do {
                    try {
                        Entity entity = EntityBuilder.convertToEntity(cls,cursor,cls.getDeclaredFields());
                        fetchAndSetFirstDegreeRelatedObject(entity);
                        entities.add(entity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            insertOrUpdateEntity(entity);
            findAndInsertMappingObject(entity);
            getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
                if (getterResponse == null) continue;
                String tableName = joinTable.tableName();
                Class targetEntity = joinTable.targetEntity();
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
            ContentValues contentValues = QueryBuilder.insertConflictContentValues(entity,
                    findById((Class<Entity>) entity.getClass(), entity.getId()));
            int numberOfRowsUpdated = getWritableDatabase().update
                    (DbUtil.getTableName(entity), contentValues,
                            DB.PRIMARY_KEY_ID + " = ?", new String[]{String.valueOf(entity.getId())});
            if (numberOfRowsUpdated < 1) return;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            findAndSetUsingReferenceById(entity); //if successfull return corresponding Entity.
        }
    }


    private void executeInsert(Entity entity) {
        long rowId = -1;
        try {
            rowId = getWritableDatabase().insert
                    (DbUtil.getTableName(entity), null, QueryBuilder.insertContentValues(entity));
            if (rowId < 0) return; //The row insertion was a failure;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            findAndSetByReferenceByRowId(entity, rowId); //if successfull return corresponding Entity.
        }
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

    boolean doesPropertyExistsForMappingTable(String tableName,
                                              String aColumnName,long aValue,String bColumnName,long bValue) throws Exception{
        String rawQuery =  "SELECT * FROM :table WHERE :aColumn = :aValue AND :bColumn = :bValue";
        rawQuery = rawQuery.replace(":table",tableName);
        rawQuery = rawQuery.replace(":aColumn",aColumnName);
        rawQuery = rawQuery.replace(":aValue",String.valueOf(aValue));
        rawQuery = rawQuery.replace(":bColumn",bColumnName);
        rawQuery = rawQuery.replace(":bValue",String.valueOf(bValue));

        Cursor cursor = getReadableDatabase().rawQuery(rawQuery,null);
        return cursor != null && cursor.getCount() > 0;

    }



    Cursor getTableInfo(String tableName){
        return getReadableDatabase().rawQuery("PRAGMA table_info(" + tableName + ")", null);
    }

    boolean tableExists(String tableName){
        Cursor cursor=  getReadableDatabase().rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?;",
                new String[]{tableName});
        return cursor != null && cursor.getCount() == 1;
    }


}
