package com.basilgregory.onam.builder;

import android.database.Cursor;

import com.basilgregory.onam.annotations.ManyToMany;
import com.basilgregory.onam.annotations.OneToMany;
import com.basilgregory.onam.annotations.Table;
import com.basilgregory.onam.constants.ClassCursorPair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by donpeter on 9/1/17.
 */

public class DMLBuilder {
    static HashMap<String,String> curateAndDropTables(List<String> tableNamesAlreadyInDb,Class[] annotatedTablesList,
                                                      List<String> mappingTableNames){
        ArrayList<String> tablesListedForRemoval = new ArrayList<>();
        for (String tableName:tableNamesAlreadyInDb){
            boolean tableFoundInList = false;
            for (String mappingTable: mappingTableNames) {
                if (mappingTable.equals(tableName)) tableFoundInList = true;
            }
            for (Class table:annotatedTablesList){
                if (DbUtil.getTableName(table).equals(tableName)) tableFoundInList = true;
            }
            if (!tableFoundInList) tablesListedForRemoval.add(tableName);
        }
        return dropTables(tablesListedForRemoval);
    }

    private static HashMap<String,String> dropTables(List<String> tableNames){
        HashMap<String,String> dmls = new HashMap<String,String>(tableNames.size());
        for(String tableName:tableNames) {
            dmls.put(tableName,dropTable(tableName));
        }
        return dmls;
    }

    private static String dropTable(String tableName){
        return new StringBuffer("DROP TABLE IF EXISTS ").append(tableName).toString();
    }

    static HashMap<String,String> renameTables(Class[] classes){
        HashMap<String,String> dmls = new HashMap<String,String>(classes.length);
        for(Class cls:classes) {
            if (cls == null) continue;
            Table table = (Table)cls.getAnnotation(Table.class);
            if (table == null) continue;
            String tableName = DbUtil.getTableName(cls);
            if (table.oldName().isEmpty()) continue;
            dmls.put(tableName,renameTable(tableName, table.oldName()));
        }

        return dmls;

    }

    static HashMap<String,String> addColumns(List<ClassCursorPair> classCursorPairs){
        HashMap<String,String> dmls = new HashMap<String,String>(classCursorPairs.size());
        for(ClassCursorPair classCursorPair:classCursorPairs) {
            Class cls = classCursorPair.getaClass();
            if (cls == null || cls.getAnnotation(Table.class) == null) continue;
            String tableName = DbUtil.getTableName(cls);
            for (String addColumnDml: addColumns(classCursorPair.getCursor(),cls)) {
                dmls.put(tableName,addColumnDml);
            }
        }
        return dmls;
    }

    private static String renameTable(String tableName , String oldName){
        return new StringBuffer("ALTER TABLE ").append(oldName).append(" RENAME TO ").append(tableName)
                .append(";").toString();
    }

    private static List<String> addColumns(Cursor existingTable, Class newTableClass){
        List<String> addColumnsDml = new ArrayList<String>();
        if (existingTable == null || newTableClass == null) return addColumnsDml;
        for (Field field:newTableClass.getDeclaredFields()) {
            boolean fieldAlreadyInDb = false;
            existingTable.moveToFirst();
            do{
                try {
                    String expectedColumnNameForField = DbUtil.getColumnName(field);
                    if (expectedColumnNameForField == null) continue;
                    if (!expectedColumnNameForField.toLowerCase().equals(
                            existingTable.getString(existingTable.getColumnIndex("name")).toLowerCase())) continue;
                    fieldAlreadyInDb = true;
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }while (existingTable.moveToNext());
            if (fieldAlreadyInDb) continue;
            StringBuffer addColumnDml = new StringBuffer("ALTER TABLE ")
                    .append(DbUtil.getTableName(newTableClass)).append(" ADD COLUMN ");
            String fieldType = DbUtil.findType(field);
            if (fieldType != null) addColumnDml.append(field.getName().toLowerCase()).append(" ").append(fieldType);
            else {
                Method getterMethod = DbUtil.getMethod("get",field);
                if (getterMethod.getAnnotation(OneToMany.class) == null &&
                        getterMethod.getAnnotation(ManyToMany.class) == null) DbUtil.generateForeignColumnName(addColumnDml,field);
                else continue;
            }
            addColumnDml.append(";");

            addColumnsDml.add(addColumnDml.toString());
        }
        return addColumnsDml;
    }
}
