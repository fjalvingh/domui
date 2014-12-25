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
	private Map<PropertyMetaModel<?>, IRenderType<?>> m_simpleProperties;

	private List<PropertyMetaModel<?>> m_parentProperties;

	private List<PropertyMetaModel<?>> m_childProperties;

	public ClassInfo(Map<PropertyMetaModel<?>, IRenderType<?>> simpleProperties, List<PropertyMetaModel<?>> parentProperties, List<PropertyMetaModel<?>> childProperties) {
		m_simpleProperties = simpleProperties;
		m_parentProperties = parentProperties;
		m_childProperties = childProperties;
	}

	public Map<PropertyMetaModel<?>, IRenderType<?>> getSimpleProperties() {
		return m_simpleProperties;
	}

	public List<PropertyMetaModel<?>> getParentProperties() {
		return m_parentProperties;
	}

	public List<PropertyMetaModel<?>> getChildProperties() {
		return m_childProperties;
	}

	@Nullable
	static public ClassInfo	decode(Class<?> inclz) {
		//-- Must be annotated with @JsClass or we'll ignore it
		JsClass jcl = inclz.getAnnotation(JsClass.class);
		if(null == jcl)
			return null;

		ClassMetaModel cmm = MetaManager.findClassMeta(inclz);
		Map<PropertyMetaModel<?>, IRenderType<?>> simpleProps = new HashMap<>();
		List<PropertyMetaModel<?>> childProps = new ArrayList<>();
		List<PropertyMetaModel<?>> parentProps = new ArrayList<>();

		for(PropertyMetaModel<?> property : cmm.getProperties()) {
			IRenderType<?> renderer = JsModel.findRenderer(property);
			if(null != renderer) {
				simpleProps.put(property, renderer);
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

		return new ClassInfo(simpleProps, parentProps, childProps);
	}

}
