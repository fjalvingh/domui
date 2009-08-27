package to.etc.util;

/**
 * @author jal
 * Created on Jan 22, 2005
 */
public interface ConfigSource {
	public String getOption(String key) throws Exception;

	public Object getSourceObject();

	public ConfigSource getSubSource(String key) throws Exception;
}
