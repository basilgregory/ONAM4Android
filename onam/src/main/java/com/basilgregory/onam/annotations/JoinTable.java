package com.basilgregory.onam.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by donpeter on 9/7/17.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface JoinTable {
    String tableName() default "";
    Class targetEntity();
}
