package com.basilgregory.onam.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by donpeter on 9/26/17.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Json {
    String fieldName();
}
