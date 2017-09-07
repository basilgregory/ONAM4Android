package com.basilgregory.onam.annotations;

import com.basilgregory.onam.builder.FetchType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by donpeter on 8/29/17.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne {
    short fetchType() default FetchType.LAZY;
}
