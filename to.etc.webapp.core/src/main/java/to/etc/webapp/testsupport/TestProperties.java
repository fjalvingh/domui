package to.etc.webapp.testsupport;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Properties;

/**
 * Accessor for test-related properties, which "knows" the ways that properties can
 * be gotten: either from a .test.properties file or System.getProperty parameters.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 11, 2014
 */
//@Immutable
final public class TestProperties {
	@NonNull
	final private Properties m_properties;

	final private boolean m_present;

	public TestProperties(@NonNull Properties properties, boolean present) {
		m_properties = properties;
		m_present = present;
	}

	@Nullable
	public String getProperty(@NonNull String key) {
		String value = System.getProperty(key);
		if(null == value)
			value = m_properties.getProperty(key);
		//System.out.println("$$$$ test: property " + key + " value " + value);
		return value;
	}

	@Nullable
	public String getProperty(@NonNull String key, @Nullable String deflt) {
		String res = getProperty(key);
		return res == null ? deflt : res;
	}

	public boolean isPresent() {
		return m_present;
	}
}
