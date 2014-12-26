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

	void update(Map<PropertyMetaModel<?>, Simple<?>> simpleProperties, List<PropertyMetaModel<?>> parentProperties, List<PropertyMetaModel<?>> childProperties) {
		m_simpleProperties = simpleProperties;
		m_parentProperties = parentProperties;
		m_childProperties = childProperties;
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

	@Nullable
	public PropertyMetaModel<String> getIdProperty() {
		return m_idProperty;
	}

	@Nullable
	static public ClassInfo	decode(Class<?> inclz) {
		//-- Must be annotated with @JsClass or we'll ignore it
		JsClass jcl = inclz.getAnnotation(JsClass.class);
		if(null == jcl)
			return null;

		ClassMetaModel cmm = MetaManager.findClassMeta(inclz);
		Map<PropertyMetaModel<?>, Simple<?>> simpleProps = new HashMap<>();
		List<PropertyMetaModel<?>> childProps = new ArrayList<>();
		List<PropertyMetaModel<?>> parentProps = new ArrayList<>();
		PropertyMetaModel<String> idProperty = null;

		for(PropertyMetaModel<?> property : cmm.getProperties()) {
			if(property.getName().equals("id")) {
				if(property.getActualType() != String.class)
					throw new IllegalStateException("The class "+inclz.getName()+"'s id property must be of type String");
				idProperty = (PropertyMetaModel<String>) property;
			}

			IRenderType<?> renderer = JsModel.findRenderer(property);
			if(null != renderer) {
				simpleProps.put(property, new Simple(property, renderer));
			} else if(List.class.isAssignableFrom(property.getActualType())) {		// Collection -> child?
				Class<?> collectionType = MetaManager.findCollectionType(property.getGenericActualType());
				if(null != collectionType) {
					jcl = collectionType.getAnnotation(JsClass.class);
					if(null != jcl) {
						childProps.add(property);
					}
				}
			} else {
				jcl = property.getActualType().getAnnotation(JsClass.class);
				if(null != jcl) {
					parentProps.add(property);
				}
			}
		}
		//if(null == idProperty)
		//	throw new IllegalStateException("The class "+inclz.getName()+" does not have an id property");
		return new ClassInfo(inclz.getSimpleName(), simpleProps, parentProps, childProps, idProperty);
	}

	public String getSimpleName() {
		return m_simpleName;
	}

}
