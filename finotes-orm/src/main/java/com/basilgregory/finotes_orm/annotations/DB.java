package com.basilgregory.finotes_orm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by donpeter on 8/29/17.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface DB {
    String name() default "";
    int version() default 1;
    Class[] tables() default {};

}
