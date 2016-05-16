package to.etc.domui.annotations;

import java.lang.annotation.*;

/**
 * Annotation to mark in code that field or method are bound by name -> using reflexion.
 * It should indicate that changing it's name or removing it would cause broken code somewhere.
 *
 * Created by vmijic on 10.3.16..
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bound {
	String where();
}
