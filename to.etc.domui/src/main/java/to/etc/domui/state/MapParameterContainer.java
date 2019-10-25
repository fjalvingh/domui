package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-10-19.
 */
public class MapParameterContainer implements IBasicParameterContainer {
	private final Map<String, Object> m_map = new HashMap<>();

	@NonNull
	private String m_urlContextString = "";

	/** The approximate length of this parameters instance when rendered on an URL. */
	private int m_dataLength;

	@Nullable
	@Override
	public Object getObject(String name) {
		return m_map.get(name);
	}

	@Override
	public int size() {
		return m_map.size();
	}

	@NonNull
	@Override
	public Set<String> getParameterNames() {
		return new HashSet<>(m_map.keySet());
	}

	@Nullable
	public Object setObject(@NonNull String name, @Nullable Object object) {
		Object old;
		if(object == null)
			old = m_map.remove(name);
		else
			old = m_map.put(name, object);
		decreaseLength(old);
		increaseLength(object);
		return old;
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
				m_dataLength += (s.length() + 2);
			}
		}
	}

	@Override
	public int getDataLength() {
		return m_dataLength;
	}

}
