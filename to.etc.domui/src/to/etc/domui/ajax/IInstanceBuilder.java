package to.etc.domui.ajax;

/**
 * Factory which creates fully initialized object instances from a class/interface and
 * a set of parameters.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 14, 2009
 */
public interface IInstanceBuilder {
	<T> T createInstance(Class<T> theclass, Object... parameters);
}
