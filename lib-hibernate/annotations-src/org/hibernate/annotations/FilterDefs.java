//$Id: FilterDefs.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.annotations;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Array of filter definitions
 *
 * @author Matthew Inger
 * @author Emmanuel Bernard
 */
@Target({PACKAGE, TYPE})
@Retention(RUNTIME)
public @interface FilterDefs {
	FilterDef[] value();
}
