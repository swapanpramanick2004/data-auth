/**
 * 
 */
package com.springpoc.auth.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
/**
 * @author swpraman
 *
 */
public @interface Authorize {

    String value() default "";
}
