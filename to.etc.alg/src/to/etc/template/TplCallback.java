package to.etc.template;

/**
 * This interface retrieves the values requested in the templates as objects.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public interface TplCallback {
	public Object getValue(String name);
}
