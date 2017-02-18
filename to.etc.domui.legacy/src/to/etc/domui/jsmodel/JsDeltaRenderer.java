package to.etc.domui.jsmodel;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.jsmodel.ClassInfo.Simple;
import to.etc.domui.util.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/26/14.
 */
public class JsDeltaRenderer extends JsRenderBase {
	private final Appendable m_destination;

	private final String m_modelRoot;

	private final Set<InstanceInfo> m_refRenderedSet = new HashSet<>();

	public JsDeltaRenderer(Appendable output, JsModel jsModel, String modelRoot) {
		super(new StringBuilder(), new StringBuilder(), jsModel);
		m_destination = output;
		m_modelRoot = modelRoot;
	}

	public void render() throws Exception {
		//-- 1. Collect current set of instances.
		Set<InstanceInfo> currentSet = m_model.collectAllInstances(m_model.getRootObject());// Find everything reachable now
		Set<InstanceInfo> oldSet = m_model.getReachableSet();

		//-- Split into items added, removed and retained.
		Set<InstanceInfo> addedSet = new HashSet<>(currentSet);			// Added = current - old
		addedSet.removeAll(oldSet);

		Set<InstanceInfo> removedSet = new HashSet<>(oldSet);
		removedSet.removeAll(currentSet);								// Removed = old - current

		Set<InstanceInfo> retainedSet = new HashSet<>(currentSet);
		retainedSet.retainAll(oldSet);


		//-- Now: first render changed properties for all retained things.
		for(InstanceInfo ii: retainedSet) {
			renderPropertyDeltas(ii);
		}
		m_destination.append("function() {\n");
		m_destination.append((StringBuilder) getRefSb());
		m_destination.append((StringBuilder) getSetSb());
		m_destination.append("\n}();\n");

	}

	private String renderRef(InstanceInfo ii) throws Exception {
		if(m_refRenderedSet.add(ii)) {
			m_refSb.append("var v").append(ii.getId()).append("=").append(m_modelRoot).append(".byId('").append(ii.getId()).append("');\n");
		}
		return "v"+ii.getId();
	}

	private void renderPropertyDeltas(InstanceInfo ii) throws Exception {
		ClassInfo ci = ii.getClassInfo();
		boolean done = false;
		for(Simple<?> simple: ci.getSimpleProperties().values()) {
			done = renderPropertyDelta(simple, ii, done);
		}
		for(PropertyMetaModel<?> property: ci.getValueProperties()) {
			if(hasValueObjectChanged(ii, property)) {
				if(! done) {
					String var = renderRef(ii);
					m_setSb.append(var).append(".updateProperties({");
				} else {
					m_setSb.append(",");
				}
				done = true;
				renderValueObject(ii, property);
			}
		}

		for(PropertyMetaModel<?> pm: ci.getParentProperties()) {
			done = renderSimpleParent(pm, ii, done);
		}

		if(done) {
			m_setSb.append("});\n");
		}
	}

	/**
	 * Returns T if the value object contained in the specified property of the identified object has changed. This
	 * recursively walks the entire value object for data.
	 *
	 * @param ii
	 * @param property
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	private <T> boolean hasValueObjectChanged(InstanceInfo ii, PropertyMetaModel<T> property) throws Exception {
		Map<String, Object> valueMap = ii.getPropertyMap();
		return hasValueObjectChanged(ii.getInstance(), valueMap, property);
	}

	private <T> boolean hasValueObjectChanged(Object instance, Map<String, Object> valueMap, PropertyMetaModel<T> property) throws Exception {
		Map<String, Object> propertyValueMap = (Map<String, Object>) valueMap.get(property.getName());
		T value = property.getValue(instance);
		if(null == value) {
			return propertyValueMap == null || propertyValueMap.size() != 0;	// If there are values stored then the object BECAME null -> so changed
		}

		//-- Walk all properties of this object, and check if they changed value.
		ClassInfo valueCi = m_model.getInfo(value.getClass());
		if(valueCi == null)
			throw new IllegalStateException("No class info for " + value.getClass());
		if(valueCi.getParentProperties().size() > 0)
			throw new IllegalStateException("Value object "+valueCi+" has identifyable parent properties which is not supported");
		if(valueCi.getChildProperties().size() > 0)
			throw new IllegalStateException("Value object "+valueCi+" has child list values which is not supported");

		for(Simple<?> simple: valueCi.getSimpleProperties().values()) {
			if(hasPropertyChanged(value, propertyValueMap, simple))
				return true;
		}

		for(PropertyMetaModel<?> pmm: valueCi.getValueProperties()) {
			if(hasValueObjectChanged(value, propertyValueMap, pmm))
				return true;
		}

		return false;
	}

	/**
	 * Compares the value of the simple property with the old value in the map, and returns true if they differ.
	 * @param instance
	 * @param valueMap
	 * @param simple
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	private <T> boolean hasPropertyChanged(Object instance, Map<String, Object> valueMap, Simple<T> simple) throws Exception {
		Object value = simple.getProperty().getValue(instance);
		Object oldValue = valueMap.get(simple.getProperty().getName());
		return ! DomUtil.isEqual(value, oldValue);
	}

	/**
	 *
	 * @param pm
	 * @param ii
	 * @param done
	 * @param <T>
	 * @return
	 */
	private <T> boolean renderSimpleParent(PropertyMetaModel<T> pm, InstanceInfo ii, boolean done) throws Exception {
		Class<T> actualType = pm.getActualType();
		ClassInfo parentCi = m_model.getInfo(actualType);
		if(null == parentCi)
			throw new IllegalStateException("Cannot get class info for " + pm);

		T value = pm.getValue(ii.getInstance());
		if(parentCi.getIdProperty() != null) {
			if(! ii.updateValue(pm, value)) {
				//-- Value change? Render a lookup to the new object.



			}


		}
		return false;
	}

	private <T> boolean renderPropertyDelta(Simple<T> simple, InstanceInfo ii, boolean inited) throws Exception {
		T value = simple.getProperty().getValue(ii.getInstance());
		if(ii.updateValue(simple.getProperty(), value)) {			// Update- if not changed however exit
			return inited;
		}

		if(! inited) {
			String var = renderRef(ii);
			m_setSb.append(var).append(".updateProperties({");
		} else {
			m_setSb.append(",");
		}
		m_setSb.append(simple.getProperty().getName()).append(":");
		if(value == null) {
			m_setSb.append("null");
		} else {
			simple.getRenderer().render(m_setSb, value);
		}
		return true;
	}
}
