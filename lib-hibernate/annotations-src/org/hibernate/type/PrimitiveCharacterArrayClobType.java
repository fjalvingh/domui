//$Id: PrimitiveCharacterArrayClobType.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.type;


/**
 * Map a char[] to a Clob
 *
 * @author Emmanuel Bernard
 */
public class PrimitiveCharacterArrayClobType extends CharacterArrayClobType {
	public Class returnedClass() {
		return char[].class;
	}
}
