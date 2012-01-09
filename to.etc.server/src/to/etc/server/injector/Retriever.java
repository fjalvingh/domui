package to.etc.server.injector;

/**
 * Retrieves some value from a source instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 11, 2006
 */
public interface Retriever extends ObjectReleaser {
	/** Marker instance indicating that no value was provided. Needed to distinguish the valid case of a parameter being null. */
	static public final Object	NO_VALUE	= "$noval$";

	public String getDisplayName();

	public Object retrieveValue(Object source) throws Exception;

	public Class getType();
}
