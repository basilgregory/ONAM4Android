package com.basilgregory.finotes_orm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by donpeter on 8/28/17.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String name() default "";
}
