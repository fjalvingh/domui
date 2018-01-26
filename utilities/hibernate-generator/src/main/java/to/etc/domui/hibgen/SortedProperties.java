package to.etc.domui.hibgen;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-9-17.
 */
public class SortedProperties extends Properties {
	private static final long serialVersionUID = 4112278634029123840L;

	@Override
	public synchronized Enumeration<Object> keys() {
		Comparator<Object> byCaseInsensitiveString = Comparator.comparing(Object::toString, String.CASE_INSENSITIVE_ORDER);
		Supplier<TreeSet<Object>> supplier = () -> new TreeSet<>(byCaseInsensitiveString);

		TreeSet<Object> sortedSet = super.keySet().stream()
			.collect(Collectors.toCollection(supplier));
		return Collections.enumeration(sortedSet);
	}
}
