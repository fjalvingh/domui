package to.etc.domui.injector;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.IPageParameters;
import to.etc.util.PropertyInfo;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-03-23.
 */
final public class SimpleListPropertyInjector extends PropertyInjector {
	final private String m_name;

	final private boolean m_mandatory;

	private final Supplier<Collection<Object>> m_collectionSupplier;

	private final Class<?> m_valueType;

	public SimpleListPropertyInjector(@NonNull PropertyInfo info, String name, boolean mandatory, Supplier<Collection<Object>> collectionSupplier, Class<?> valueType) {
		super(info);
		m_name = name;
		m_mandatory = mandatory;
		m_collectionSupplier = collectionSupplier;
		m_valueType = valueType;
	}

	/**
	 * Effects the actual injection of an URL parameter to a value.
	 */
	@Override
	public void inject(@NonNull final UrlPage page, final @NonNull IPageParameters papa, Map<String, Object> attributeMap) throws Exception {
		//-- 1. Get the URL parameter's value.
		String[] paramArray = papa.getStringArray(m_name, null);
		if(paramArray == null || paramArray.length == 0) {
			if(m_mandatory)
				throw new IllegalArgumentException("The page " + page.getClass() + " REQUIRES the URL parameter " + m_name);
			return;
		}

		//-- Create the correct output type.
		Collection<Object> collection = m_collectionSupplier.get();

		//-- Loop: get all values
		for(String s : paramArray) {
			if(s != null) {
				Object value = convertParameterValue(page, s);
				if(null != value) {
					collection.add(value);
				}
			}
		}

		//-- Insert the value.
		try {
			getPropertySetter().invoke(page, collection);
		} catch(Exception x) {
			throw new RuntimeException("Cannot SET the collection value for URL parameter=" + m_name + " of page="
				+ page.getClass() + ": " + x, x);
		}
	}

	@Nullable
	private Object convertParameterValue(UrlPage page, String s) {
		try {
			return ConverterRegistry.convertURLStringToValue(m_valueType, s);
		} catch(Exception x) {
			throw new RuntimeException("Cannot convert the string '" + s + "' to type=" + m_valueType + ", for URL parameter=" + m_name + " of page=" + page.getClass() + ": " + x, x);
		}
	}
}
