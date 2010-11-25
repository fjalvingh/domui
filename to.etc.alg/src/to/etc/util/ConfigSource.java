package to.etc.util;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 22, 2005
 */
@Deprecated
public interface ConfigSource {
	public String getOption(String key) throws Exception;

	public Object getSourceObject();

	public ConfigSource getSubSource(String key) throws Exception;
}
