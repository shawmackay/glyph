package org.jini.glyph;

public @interface LocalService {
    boolean administrable() default false;
    String id() default "default";
}
