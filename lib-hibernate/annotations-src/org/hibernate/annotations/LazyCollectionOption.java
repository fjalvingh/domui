//$Id: LazyCollectionOption.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.annotations;

/**
 * Lazy options available for a collection
 *
 * @author Emmanuel Bernard
 */
public enum LazyCollectionOption {
	/** eagerly load it */
	FALSE,
	/** load it when the state is requested */
	TRUE,
	/** prefer extra queries over fill collection loading */
	EXTRA
}
