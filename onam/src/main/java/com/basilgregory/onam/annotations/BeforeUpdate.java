package com.basilgregory.onam.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by donpeter on 9/14/17.
 */

/**
 * You may add this annotation to any number of functions in your Entity class.
 * All the @{@link BeforeUpdate} annotated functions will be executed by ONAM before update to the database.
 * Changes made by this function are persistent.
 * Incase of multiple functions having @{@link BeforeUpdate} annotation, the order of execution will be random
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeUpdate {
}
