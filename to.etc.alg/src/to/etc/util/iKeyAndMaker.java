package to.etc.util;

/**
 * Generic interface to lookup and if not found create 
 * whatever object. Used to handle the efficient multithreading
 * cacheing pattern below:
 * 
 * <ul>
 *  <li>Lock lookup table</li>
 * 	<li>Find the object by key</li>
 * 	<li>If found unlock and exit with the object</li>
 * 	<li>Call iObjectMaker.makeObject() to create the object we want</li>
 * 	<li>Put the new object in the table</li>
 * 	<li>Unlock and return</li>
 * </ul>
 * 
 * An implementation of this class should also implement equals(Object) 
 * and hashCode().
 * 
 * @author jal
 * Created on May 11, 2004
 */
public interface iKeyAndMaker {
	public Object makeObject() throws Exception;
}
