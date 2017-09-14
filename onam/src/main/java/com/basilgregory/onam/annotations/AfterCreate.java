package com.basilgregory.onam.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by donpeter on 9/14/17.
 */

/**
 * You may add this annotation to any number of functions in your Entity class.
 * All the @{@link AfterCreate} annotated functions will be executed by ONAM after insertion to the database.
 * Changes made by this function are not persistent.
 * Incase of multiple functions having @{@link AfterCreate} annotation, the order of execution will be random
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterCreate {
}
