//$Id: MetaValue.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.annotations;

/**
 * Represent a discriminator value associated to a given entity type
 * @author Emmanuel Bernard
 */
public @interface MetaValue {
	/**
	 * entity type
	 */
	Class targetEntity();

	/**
	 * discriminator value stored in database
	 */
	String value();
}
