package to.etc.domui.jsmodel;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * Cached data for a given Javascript model object.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/24/14.
 */
public class InstanceInfo {
	private final ClassInfo m_classInfo;

	private final Object m_instance;

	final private String m_id;

	final private Map<String, Object> m_propertyMap = new HashMap<>();

	public InstanceInfo(ClassInfo classInfo, Object instance, String id) {
		m_classInfo = classInfo;
		m_instance = instance;
		m_id = id;
	}

	public ClassInfo getClassInfo() {
		return m_classInfo;
	}

	public Object getInstance() {
		return m_instance;
	}

	public String getId() {
		return m_id;
	}

	<T> boolean updateValue(PropertyMetaModel<T> property, @Nullable Object value) {
		Object oldValue = m_propertyMap.put(property.getName(), value);
		return DomUtil.isEqual(value, oldValue);
	}

	public Map<String, Object> getPropertyMap() {
		return m_propertyMap;
	}

	///**
	// * Get property value map for an "unidentified" property.
	// * @param property
	// * @return
	// */
	//public <T> Map<String, Object> getUnidentifiedPropertyValues(PropertyMetaModel<T> property) {
	//	Map<String, Object> valMap = (Map<String, Object>) m_propertyMap.get(property);
	//	if(null == valMap) {
	//		valMap = new HashMap<>();
	//		m_propertyMap.put(property, valMap);
	//	}
	//	return valMap;
	//}
}
