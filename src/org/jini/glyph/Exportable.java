/*
 * Exportable.java
 *
 * Created on 22 August 2006, 12:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jini.glyph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author calum
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Exportable {
    String implementing() default "";

    String id() default "default";
}
