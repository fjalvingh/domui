package to.etc.domui.jsmodel;

import to.etc.domui.component.meta.*;

import javax.annotation.*;
import java.util.*;

/**
 * Contains rendering data on a Javascript model class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/25/14.
 */
class ClassInfo {
	private String m_simpleName;

	private List<PropertyMetaModel<?>> m_valueProperties;

	static class Simple<T> {
		private final PropertyMetaModel<T> m_property;

		private final IRenderType<T> m_renderer;

		public Simple(PropertyMetaModel<T> property, IRenderType<T> renderer) {
			m_property = property;
			m_renderer = renderer;
		}

		public PropertyMetaModel<T> getProperty() {
			return m_property;
		}

		public IRenderType<T> getRenderer() {
			return m_renderer;
		}
	}

	private Map<PropertyMetaModel<?>, Simple<?>> m_simpleProperties;

	private List<PropertyMetaModel<?>> m_parentProperties;

	private List<PropertyMetaModel<?>> m_childProperties;

	@Nullable
	final private PropertyMetaModel<String> m_idProperty;

	public ClassInfo(String name, @Nullable PropertyMetaModel<String> idProp) {
		m_simpleName = name;
		m_idProperty = idProp;
	}

	void update(Map<PropertyMetaModel<?>, Simple<?>> simpleProperties, List<PropertyMetaModel<?>> parentProperties, List<PropertyMetaModel<?>> childProperties, List<PropertyMetaModel<?>> valueProps) {
		m_simpleProperties = simpleProperties;
		m_parentProperties = parentProperties;
		m_childProperties = childProperties;
		m_valueProperties = valueProps;
	}

	public Map<PropertyMetaModel<?>, Simple<?>> getSimpleProperties() {
		return m_simpleProperties;
	}

	public List<PropertyMetaModel<?>> getParentProperties() {
		return m_parentProperties;
	}

	public List<PropertyMetaModel<?>> getChildProperties() {
		return m_childProperties;
	}

	public List<PropertyMetaModel<?>> getValueProperties() {
		return m_valueProperties;
	}

	@Nullable
	public PropertyMetaModel<String> getIdProperty() {
		return m_idProperty;
	}

	public String getSimpleName() {
		return m_simpleName;
	}

}
