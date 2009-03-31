package to.etc.domui.util;

/**
 * Renders some kind of presentation (string) for some object T.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 13, 2008
 */
public interface ILabelStringRenderer<T> {
	public String			getLabelFor(T object);
}
