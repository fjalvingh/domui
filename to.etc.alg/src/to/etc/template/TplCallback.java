package to.etc.template;

/**
 * This interface retrieves the values requested in the templates as objects.
 * VERY OLD - DO NOT USE
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@Deprecated
public interface TplCallback {
	public Object getValue(String name);
}
