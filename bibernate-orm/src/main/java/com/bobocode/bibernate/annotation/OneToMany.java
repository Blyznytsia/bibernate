package com.bobocode.bibernate.annotation;

import com.bobocode.bibernate.enums.FetchType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToMany {

  FetchType fetch() default FetchType.LAZY;
}
