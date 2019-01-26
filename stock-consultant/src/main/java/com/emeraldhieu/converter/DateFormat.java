package com.emeraldhieu.converter;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface DateFormat {

    public static final String DEFAULT_DATE = "yyyy-MM-dd";

    String value() default DEFAULT_DATE;
}