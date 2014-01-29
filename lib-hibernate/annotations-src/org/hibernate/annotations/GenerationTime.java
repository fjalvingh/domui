//$Id: GenerationTime.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.annotations;

/**
 * When should the generation occurs
 *
 * @author Emmanuel Bernard
 */
public enum GenerationTime {
	NEVER,
	INSERT,
	ALWAYS
}
