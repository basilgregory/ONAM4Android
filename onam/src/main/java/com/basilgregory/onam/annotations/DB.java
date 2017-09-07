package com.basilgregory.onam.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by donpeter on 8/29/17.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface DB {
    String name();
    int version();
    Class[] tables();

}
