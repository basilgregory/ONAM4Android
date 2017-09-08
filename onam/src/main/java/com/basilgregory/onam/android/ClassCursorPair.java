package com.basilgregory.onam.android;

import android.database.Cursor;

/**
 * Created by donpeter on 9/5/17.
 */

public class ClassCursorPair {
    private Class aClass;
    private Cursor cursor;

    Class getaClass() {
        return aClass;
    }

    void setaClass(Class aClass) {
        this.aClass = aClass;
    }

    Cursor getCursor() {
        return cursor;
    }

    void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }
}
