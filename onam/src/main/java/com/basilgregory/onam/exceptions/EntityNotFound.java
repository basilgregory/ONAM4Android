package com.basilgregory.onam.exceptions;

import com.basilgregory.onam.android.Entity;

/**
 * Created by donpeter on 9/14/17.
 */

public final class EntityNotFound extends ONAMException {

    public EntityNotFound(Entity entity) {
        super(E.ENTITY_NOT_FOUND.name(), E.ENTITY_NOT_FOUND.ordinal(), entity);
    }

    public EntityNotFound(Throwable cause, Entity entity) {
        super(E.ENTITY_NOT_FOUND.name(), E.ENTITY_NOT_FOUND.ordinal(), cause, entity);
    }
}
