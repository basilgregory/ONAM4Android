package com.basilgregory.onam.android;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.reflect.Field;


/**
 * Created by donpeter on 8/29/17.
 */

enum FieldType {
    INTEGER("integer"),
    INT("integer"),
    LONG("integer"),
    SHORT("integer"),
    BYTE("integer"),
    BOOLEAN("integer"),
    BIGINTEGER("integer"),
    BIGDECIMAL("real"),
    FLOAT("real"),
    DOUBLE("real"),
    STRING("text"),
    DATE("text"),
    TIME("text"),
    DATETIME("text"),
    BYTE_S("BLOB"),
    OBJECT("text");


    private String dataType;

    private FieldType(String dataType) {
        this.dataType = dataType;
    }

    String getDataType() {
        return this.dataType;
    }

    static FieldType adjustedValueOf(String name) {
        return valueOf(name.replace("[]", "_S"));
    }

    String realName() {
        return name().replace("_S", "[]");
    }


    static void addValues(ContentValues contentValues, Field field, Object returnValue) throws Exception {
        if (returnValue != null) {
            String columnName = DbUtil.getColumnName(field);
            switch (field.getType().getSimpleName().toUpperCase()) {
                case "INT":
                    contentValues.put(columnName,
                            (int) returnValue);
                    break;
                case "INTEGER":
                    contentValues.put(columnName,
                            (int) returnValue);
                    break;
                case "LONG":
                    contentValues.put(columnName,
                            (long) returnValue);
                    break;
                case "SHORT":
                    contentValues.put(columnName,
                            (short) returnValue);
                    break;
                case "BYTE":
                    contentValues.put(columnName,
                            (byte) returnValue);
                    break;
                case "BOOLEAN":
                    contentValues.put(columnName,
                            (boolean) returnValue);
                    break;
                case "BIGINTEGER":
                    contentValues.put(columnName,
                            (long) returnValue);
                    break;
                case "BIGDECIMAL":
                    contentValues.put(columnName,
                            (double) returnValue);
                    break;
                case "FLOAT":
                    contentValues.put(columnName,
                            (float) returnValue);
                    break;
                case "DOUBLE":
                    contentValues.put(columnName,
                            (double) returnValue);
                    break;
                case "BYTE[]":
                    contentValues.put(columnName,
                            (byte[]) returnValue);
                    break;

                default:
                    contentValues.put(columnName,
                            (String) returnValue);
                    break;
            }
        }

    }

    static boolean isStringType(Object o){
        if (o != null) {
            switch (o.getClass().getSimpleName().toUpperCase()) {
                case "DATE":
                    return true;
                case "TIME":
                    return true;
                case "DATETIME":
                    return true;
                case "STRING":
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    static Object getValueFromCursor(Cursor cursor,String parameterType, String columnName) throws Exception {
        switch (parameterType) {
            case "INT":
                return cursor.getInt(cursor.getColumnIndex(columnName));
            case "INTEGER":
                return cursor.getInt(cursor.getColumnIndex(columnName));
            case "LONG":
                return cursor.getLong(cursor.getColumnIndex(columnName));
            case "SHORT":
                return cursor.getShort(cursor.getColumnIndex(columnName));
            case "BYTE":
                return cursor.getInt(cursor.getColumnIndex(columnName));
            case "BOOLEAN":
                return cursor.getInt(cursor.getColumnIndex(columnName)) == 1 ? true : false;
            case "BIGINTEGER":
                return cursor.getLong(cursor.getColumnIndex(columnName));
            case "BIGDECIMAL":
                return cursor.getDouble(cursor.getColumnIndex(columnName));
            case "FLOAT":
                return cursor.getFloat(cursor.getColumnIndex(columnName));
            case "DOUBLE":
                return cursor.getDouble(cursor.getColumnIndex(columnName));
            case "BYTE[]":
                return cursor.getBlob(cursor.getColumnIndex(columnName));
            default:
                return cursor.getString(cursor.getColumnIndex(columnName));
        }

    }
}
