package to.etc.iocular.def;

/**
 * Defines the basic "instance creation" method as defined by a builder for a component.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 15, 2009
 */
public enum CreateMethod {
	/**
	 * Create the object by doing a 'new' on the object. It will use the most optimal constructor using
	 * a greedy algorithm (i,e, it uses the constructor with the most or most specialized parameters).
	 */
	ASNEW,

	/**
	 * The object is a container parameter; it's value will be set at runtime when the container is constructed.
	 */
	CONTAINER_PARAMETER,

	/**
	 * Created by some factory, by calling a method on a static or known instance.
	 */
	FACTORY_METHOD,


}
