package to.etc.domui.jsmodel;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.jsmodel.ClassInfo.Simple;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/28/14.
 */
class JsRenderBase {
	protected final Appendable m_refSb;

	protected final Appendable m_setSb;

	protected final JsModel m_model;

	public JsRenderBase(Appendable refSb, Appendable setSb, JsModel model) {
		m_refSb = refSb;
		m_setSb = setSb;
		m_model = model;
	}

	protected void renderValueObject(InstanceInfo ii, PropertyMetaModel<?> property) throws Exception {
		Map<String, Object> valueMap = ii.getPropertyMap();
		renderValueObject(ii.getInstance(), valueMap, property);
	}

	private <T> void renderValueObject(Object instance, Map<String, Object> valueMap, PropertyMetaModel<T> property) throws Exception {
		m_setSb.append(property.getName()).append(":");

		T value = property.getValue(instance);
		if(null == value) {
			m_setSb.append("null");
			return;
		}
		Map<String, Object> propertyValueMap = (Map<String, Object>) valueMap.get(property.getName());
		if(null == propertyValueMap) {
			propertyValueMap = new HashMap<>();
			valueMap.put(property.getName(), propertyValueMap);
		}

		//-- Walk all properties of this object, and check if they changed value.
		ClassInfo valueCi = m_model.getInfo(value.getClass());
		if(null == valueCi)
			throw new IllegalStateException("No clss info for " + value.getClass());
		if(valueCi.getParentProperties().size() > 0)
			throw new IllegalStateException("Value object "+valueCi+" has identifyable parent properties which is not supported");
		if(valueCi.getChildProperties().size() > 0)
			throw new IllegalStateException("Value object "+valueCi+" has child list values which is not supported");

		m_setSb.append("new ").append(value.getClass().getSimpleName()).append("({");
		boolean done = false;
		for(Simple<?> simple: valueCi.getSimpleProperties().values()) {
			if(done)
				m_setSb.append(",");
			done = true;
			renderSimple(value, simple, propertyValueMap);
		}
		for(PropertyMetaModel<?> pmm: valueCi.getValueProperties()) {
			if(! done)
				m_setSb.append(',');
			done = true;

			renderValueObject(value, propertyValueMap, pmm);
		}
		m_setSb.append("})\n");
	}

	protected <I, V> void renderSimple(I instance, Simple<V> simple, Map<String, Object> valueMap) throws Exception {
		V value = simple.getProperty().getValue(instance);
		valueMap.put(simple.getProperty().getName(), value);

		m_setSb.append(simple.getProperty().getName()).append(":");
		if(null == value) {
			m_setSb.append("null");
			return;
		}
		simple.getRenderer().render(m_setSb, value);
	}

	protected Appendable appendRef(String what) throws Exception {
		m_refSb.append(what);
		return m_refSb;
	}

	protected Appendable appendSet(String what) throws Exception {
		m_setSb.append(what);
		return m_setSb;
	}

	public Appendable getRefSb() {
		return m_refSb;
	}

	public Appendable getSetSb() {
		return m_setSb;
	}
}
