package to.etc.util;

/**
 * This interface lets one make an object and initialize the object later on. This
 * is used in the "create with lock quickly, unlock, initialize" pattern for
 * efficient initialization of shared data.
 *
 * Created on Aug 10, 2004
 * @author jal
 */
public interface iMakeAndInit {
	public Object makeObject();

	public void init(Object o) throws Exception;
}
