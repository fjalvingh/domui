//$Id: CacheConcurrencyStrategy.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.annotations;

/**
 * Cache concurrency strategy
 *
 * @author Emmanuel Bernard
 */
public enum CacheConcurrencyStrategy {
	NONE,
	READ_ONLY,
	NONSTRICT_READ_WRITE,
	READ_WRITE,
	TRANSACTIONAL
}
