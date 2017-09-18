package com.basilgregory.onam.exceptions;

import com.basilgregory.onam.android.Entity;

import java.lang.reflect.Field;

/**
 * Created by donpeter on 9/14/17.
 */

public abstract class ONAMException extends java.lang.Exception {
    private int code;
    private Entity entity;
    private String query;
    private Field field;

    public ONAMException(String message, int code, Throwable cause, Entity entity) {
        super(message, cause);
        this.code = code;
        this.entity = entity;
    }

    public ONAMException(String message, int code, Entity entity) {
        super(message);
        this.code = code;
        this.entity = entity;
    }

    public ONAMException() {
        super();
    }

    public String toString () {
        StringBuffer sb = new StringBuffer(this.code).append(": ").append(this.getMessage());
        if (entity != null) sb.append("\n Entity").append(entity.getClass().getSimpleName());
        if (query != null) sb.append("\n Query").append(query);
        if (field != null) sb.append("\n Field").append(field.getName());
        sb.append("\n").append(this.getStackTrace());
        return sb.toString();
    }



    public enum E {
        ENTITY_NOT_FOUND,
        FETCH_RETURNED_EMPTY,
        FAILED_TO_SAVE,
        FAILED_TO_UPDATE,
        NO_GETTER_FOUND,
        NO_SETTER_FOUND;

    }
}
