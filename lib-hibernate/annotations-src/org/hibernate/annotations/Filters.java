//$Id: Filters.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.annotations;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Add multiple @Filters
 *
 * @author Emmanuel Bernard
 * @author Matthew Inger
 * @author Magnus Sandberg
 */
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface Filters {
	Filter[] value();
}
