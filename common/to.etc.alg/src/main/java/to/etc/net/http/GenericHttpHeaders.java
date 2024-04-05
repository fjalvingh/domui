package to.etc.net.http;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a header set.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-03-22.
 */
@NonNullByDefault
final public class GenericHttpHeaders {
	private final Map<String, List<String>> m_headerMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	public GenericHttpHeaders() {}

	public GenericHttpHeaders(Map<String, List<String>> map) {
		map.forEach((name, list) -> {
			m_headerMap.put(name, new ArrayList<>(list));
		});
	}

	@Nullable
	public String firstValue(String name) {
		List<String> list = m_headerMap.get(name);
		if(null != list && !list.isEmpty())
			return list.get(0);
		return null;
	}

	public List<String> allValues(String name) {
		List<String> strings = m_headerMap.get(name);
		return strings == null ? Collections.emptyList() : strings;
	}

	public Map<String, List<String>> map() {
		Map<String, List<String>> m = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		m_headerMap.forEach((name, list) -> {
			m.put(name, new ArrayList<>(list));
		});

		return m;
	}

	public void put(String name, String value) {
		m_headerMap.computeIfAbsent(name, a -> new ArrayList<>()).add(value);
	}
}
