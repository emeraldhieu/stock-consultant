package com.emeraldhieu.converter;// imports

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ FIELD, PARAMETER })
public @interface DateTimeFormat {
 
  public static final String DEFAULT_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ";
 
  String value() default DEFAULT_DATE_TIME;
}