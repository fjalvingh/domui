//$Id: Filter.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.annotations.common.reflection;

/**
 * Filter properties
 *
 * @author Emmanuel Bernard
 */
public interface Filter {
	boolean returnStatic();

	boolean returnTransient();
}
