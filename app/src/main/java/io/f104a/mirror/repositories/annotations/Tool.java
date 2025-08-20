package io.f104a.mirror.repositories.annotations;

import io.f104a.mirror.tools.ITool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tool annotation.
 * This annotation should be used to collect Mirror tools implementations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Tool{
    Class<?> requires() default ITool.class;
    int order() default 0;
}
