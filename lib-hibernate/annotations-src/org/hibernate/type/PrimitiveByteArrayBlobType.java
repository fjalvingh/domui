//$Id: PrimitiveByteArrayBlobType.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.type;

/**
 * Map a byte[] to a Blob
 *
 * @author Emmanuel Bernard
 */
public class PrimitiveByteArrayBlobType extends ByteArrayBlobType {
	public Class getReturnedClass() {
		return byte[].class;
	}

	protected Object wrap(byte[] bytes) {
		return bytes;
	}

	protected byte[] unWrap(Object bytes) {
		return (byte[]) bytes;
	}
}
