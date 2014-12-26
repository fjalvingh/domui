package to.etc.domui.jsmodel;

import to.etc.domui.component.meta.*;

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

	final private Map<PropertyMetaModel<?>, Object> m_propertyMap = new HashMap<>();

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

	<T> void updateValue(PropertyMetaModel<T> property, @Nullable T value) {
		m_propertyMap.put(property, value);
	}
}
