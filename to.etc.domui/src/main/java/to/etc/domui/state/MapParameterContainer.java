package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.server.BrowserVersion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-10-19.
 */
public class MapParameterContainer implements IBasicParameterContainer {
	private final Map<String, String[]> m_safeParametersMap = new HashMap<>();

	private final Map<String, String[]> m_unsafeParametersMap = new HashMap<>();

	@NonNull
	private String m_urlContextString = "";

	/** The approximate length of this parameters instance when rendered on an URL. */
	private int m_dataLength;

	@NonNull
	private String m_inputPath = "";

	@NonNull
	private BrowserVersion m_browserVersion = BrowserVersion.INSTANCE;

	@Nullable
	private String m_themeName;

	public MapParameterContainer() {
	}

	@Nullable
	@Override
	public String[] getParameterValues(String name) {
		return m_safeParametersMap.get(name);
	}

	@Nullable
	@Override
	public String[] getRawUnsafeParameterValues(String name) {
		return m_unsafeParametersMap.get(name);
	}

	@Override
	public int size() {
		return m_safeParametersMap.size();
	}

	@NonNull
	@Override
	public Set<String> getParameterNames() {
		return new HashSet<>(m_safeParametersMap.keySet());
	}

	@Nullable
	public String[] setParameterValues(@NonNull String name, @Nullable String[] object) {
		String[] old;
		if(object == null) {
			old = m_safeParametersMap.remove(name);
			m_unsafeParametersMap.remove(name);
		} else {
			old = m_safeParametersMap.put(name, object);
			m_unsafeParametersMap.put(name, object);
		}
		decreaseLength(old);
		increaseLength(object);
		return old;
	}

	public void setRawUnsafeParameterValues(@NonNull String name, @Nullable String[] object) {
		if(object == null) {
			m_unsafeParametersMap.remove(name);
		} else {
			m_unsafeParametersMap.put(name, object);
		}
	}

	@Nullable
	@Override
	public String getUrlContextString() {
		return m_urlContextString;
	}

	public void setUrlContextString(@Nullable String urlContextString) {
		m_urlContextString = urlContextString == null ? "" : urlContextString;
	}

	private void decreaseLength(@Nullable Object o) {
		if(o instanceof String) {
			m_dataLength -= ((String) o).length() + 2;
		} else if(o instanceof String[]) {
			for(String s : (String[]) o) {
				decreaseLength(s);
			}
		}
	}

	private void increaseLength(@Nullable Object value) {
		if(null == value)
			return;
		if(value instanceof String) {
			m_dataLength += (((String) value).length() + 2);
		} else if(value instanceof String[]) {
			for(String s : ((String[]) value)) {
				if(s != null) {
					m_dataLength += (s.length() + 2);
				}
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(! (o instanceof MapParameterContainer))
			return false;

		MapParameterContainer that = (MapParameterContainer) o;

		if(m_dataLength != that.m_dataLength)
			return false;
		if(!m_safeParametersMap.equals(that.m_safeParametersMap))
			return false;
		if(!m_urlContextString.equals(that.m_urlContextString))
			return false;
		if(!m_inputPath.equals(that.m_inputPath))
			return false;
		if(!m_browserVersion.equals(that.m_browserVersion))
			return false;
		return m_themeName != null ? m_themeName.equals(that.m_themeName) : that.m_themeName == null;
	}

	@Override
	public int hashCode() {
		int result = m_safeParametersMap.hashCode();
		result = 31 * result + m_urlContextString.hashCode();
		result = 31 * result + m_dataLength;
		result = 31 * result + m_inputPath.hashCode();
		result = 31 * result + m_browserVersion.hashCode();
		result = 31 * result + (m_themeName != null ? m_themeName.hashCode() : 0);
		return result;
	}

	@Override
	public int getDataLength() {
		return m_dataLength;
	}

	@Override
	@NonNull
	public String getInputPath() {
		return m_inputPath;
	}

	public void setInputPath(@NonNull String inputPath) {
		m_inputPath = inputPath;
	}

	@Override
	@NonNull
	public BrowserVersion getBrowserVersion() {
		return m_browserVersion;
	}

	public void setBrowserVersion(@NonNull BrowserVersion browserVersion) {
		m_browserVersion = browserVersion;
	}

	@Override
	@Nullable
	public String getThemeName() {
		return m_themeName;
	}

	public void setThemeName(@Nullable String themeName) {
		m_themeName = themeName;
	}

	public void clear() {
		m_safeParametersMap.clear();
		m_dataLength = 0;
	}
}
