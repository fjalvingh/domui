//$Id: NotYetImplementedException.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.cfg;

import org.hibernate.MappingException;

/**
 * Mapping not yet implemented
 *
 * @author Emmanuel Bernard
 */
public class NotYetImplementedException extends MappingException {

	public NotYetImplementedException(String msg, Throwable root) {
		super( msg, root );
	}

	public NotYetImplementedException(Throwable root) {
		super( root );
	}

	public NotYetImplementedException(String s) {
		super( s );
	}

}
