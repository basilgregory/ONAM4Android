package com.basilgregory.onam.constants;

import android.database.Cursor;

/**
 * Created by donpeter on 9/5/17.
 */

public class ClassCursorPair {
    private Class aClass;
    private Cursor cursor;

    public Class getaClass() {
        return aClass;
    }

    public void setaClass(Class aClass) {
        this.aClass = aClass;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }
}
