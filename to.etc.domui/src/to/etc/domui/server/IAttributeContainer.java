package to.etc.domui.server;

/**
 * Define an attribute container.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 19, 2009
 */
public interface IAttributeContainer {
	void		setAttribute(String name, Object value);
	Object		getAttribute(String name);
}
