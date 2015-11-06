package ch.icclab.cyclops.util;

import java.lang.annotation.*;

/**
 * Created by Konstantin on 22.10.2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD })
@Inherited
public @interface Loggable {
}
