//$Id: Persister.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.annotations;

import java.lang.annotation.*;

/**
 * Specify a custom persister.
 *
 * @author Shawn Clowater
 */
@java.lang.annotation.Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention( RetentionPolicy.RUNTIME )
public @interface Persister {
	/** Custom persister */
	Class impl();
}
