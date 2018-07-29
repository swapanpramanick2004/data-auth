/**
 * 
 */
package com.springpoc.auth.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(PARAMETER)
/**
 * @author swpraman
 *
 */
public @interface Path {

}
