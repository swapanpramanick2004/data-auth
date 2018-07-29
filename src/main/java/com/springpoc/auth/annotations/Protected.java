/**
 * 
 */
package com.springpoc.auth.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

import com.springpoc.auth.DefaultProtetcor;

@Retention(RUNTIME)
@Target(METHOD)
/**
 * @author swpraman
 *
 */
public @interface Protected {
    
    @AliasFor("forRoles")
    String[] value() default {};
    @AliasFor("value")
    String[] forRoles() default {};
    Class<?> by() default DefaultProtetcor.class;
}
