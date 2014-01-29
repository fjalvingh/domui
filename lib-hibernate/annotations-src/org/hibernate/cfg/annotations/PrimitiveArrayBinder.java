//$Id: PrimitiveArrayBinder.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.cfg.annotations;

import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.PrimitiveArray;

/**
 * @author Emmanuel Bernard
 */
public class PrimitiveArrayBinder extends ArrayBinder {
	@Override
	protected Collection createCollection(PersistentClass persistentClass) {
		return new PrimitiveArray( persistentClass );
	}
}
