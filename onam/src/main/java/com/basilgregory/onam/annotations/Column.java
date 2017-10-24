package com.basilgregory.onam.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by donpeter on 8/31/17.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    boolean unique() default false;
    String name() default "";

}
