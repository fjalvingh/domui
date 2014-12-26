package to.etc.domui.jsmodel;

import to.etc.domui.component.meta.*;
import to.etc.domui.jsmodel.ClassInfo.*;

import java.util.*;
import java.util.Map.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/26/14.
 */
public class JsInitialRenderer {
	final private JsModel	m_model;

	final private Appendable m_output;

	public JsInitialRenderer(Appendable output, JsModel model) {
		m_output = output;
		m_model = model;
	}

	public void render() throws Exception {
		m_output.append("function() {");

		//-- Create root.
		Object rootObject = m_model.getRootObject();
		ClassInfo ci = m_model.getInfo(rootObject.getClass());
		if(null == ci)
			throw new IllegalStateException("The root Javascript model object is not annotated with "+JsClass.class.getName());

		Set<InstanceInfo> instances = m_model.collectAllInstances(rootObject);
		for(InstanceInfo ii: instances) {
			renderInstanceCreate(ii);
		}

		for(InstanceInfo ii: instances) {
			renderRelations(ii);
		}
		m_output.append("\n}();\n");
		m_model.setReachableSet(instances);
	}
	private void renderRelations(InstanceInfo ii) throws Exception {
		renderChildRelations(ii);
		renderParentRelations(ii);
	}

	private void renderParentRelations(InstanceInfo ii) throws Exception {
		ClassInfo ci = ii.getClassInfo();
		for(PropertyMetaModel<?> pp : ci.getParentProperties()) {
			renderParentProperty(ii, pp);
		}
	}

	private void renderParentProperty(InstanceInfo ii, PropertyMetaModel<?> pp) throws Exception {
		Object value = pp.getValue(ii.getInstance());
		if(null == value)
			return;
		InstanceInfo parii = m_model.findInstance(value);
		if(null == parii) {
			ClassInfo ci = m_model.getInfo(value.getClass());
			if(ci.getIdProperty() != null)
				throw new IllegalStateException("Cannot re-find instance " + MetaManager.identify(value));

			//-- Idless class- create on-the-fly.
			m_output.append("v").append(ii.getId()).append("._").append(pp.getName()).append("= new ").append(ci.getSimpleName()).append("({");
			renderSimpleProperties(ci, value);
			m_output.append("});\n");
			return;
		}

		m_output.append("v").append(ii.getId()).append("._").append(pp.getName()).append("=v").append(parii.getId()).append(";\n");
	}

	private void renderChildRelations(InstanceInfo ii) throws Exception {
		ClassInfo ci = ii.getClassInfo();
		for(PropertyMetaModel<?> cp : ci.getChildProperties()) {
			renderChildList(ii, cp);
		}
	}

	private void renderChildList(InstanceInfo ii, PropertyMetaModel<?> cp) throws Exception {
		Object value = cp.getValue(ii.getInstance());
		if(null == value)
			return;
		if(! (value instanceof List)) {
			throw new IllegalStateException("The child model is not returning List.");
		}
		List<Object> list = (List<Object>) value;

		m_output.append("v").append(ii.getId()).append("._").append(cp.getName()).append(" = [");
		int count = 0;
		for(Object item: list) {
			if(count % 10 == 9)
				m_output.append("\n");
			if(count++ != 0)
				m_output.append(",");

			InstanceInfo chii = m_model.findInstance(item);
			if(null == chii) {
				ClassInfo ci = m_model.getInfo(item.getClass());
				if(ci.getIdProperty() != null)
					throw new IllegalStateException("Cannot find list child instance " + MetaManager.identify(item));
				m_output.append("new ").append(ci.getSimpleName()).append("({");
				renderSimpleProperties(ci, item);
				m_output.append("})");
			} else {
				m_output.append("v").append(chii.getId());
			}

		}
		m_output.append("];\n");
	}

	private void renderInstanceCreate(InstanceInfo ii) throws Exception {
		String name = ii.getClassInfo().getSimpleName();
		m_output.append("var v").append(ii.getId()).append("=new ").append(name).append("({");
		renderSimpleProperties(ii);
		m_output.append("});\n");
	}

	/**
	 * Render all primitive properties of a thing as a javascript object format fragment.
	 * @param ii
	 * @throws Exception
	 */
	private void renderSimpleProperties(InstanceInfo ii) throws Exception {
		boolean comma = false;
		ClassInfo ci = ii.getClassInfo();
		for(Entry<PropertyMetaModel<?>, Simple<?>> me : ci.getSimpleProperties().entrySet()) {
			if(comma)
				m_output.append(',');
			renderSimpleProperty(ii, me.getValue());
			comma = true;
		}
	}

	private <T> void renderUnidentifiedProperty(InstanceInfo sourceInstance, PropertyMetaModel<T> property, Object objectValue) throws Exception {
		Map<String, Object> valueMap = sourceInstance.getUnidentifiedPropertyValues(property);

		ClassInfo ci = m_model.getInfo(objectValue.getClass());
		m_output.append("new ").append(ci.getSimpleName()).append("({");
		renderSimpleProperties(objectValue, valueMap, "");
	}

	private boolean renderSimpleProperties(Object instance, Map<String, Object> valueMap, String path) throws Exception {
		ClassMetaModel cmm = MetaManager.findClassMeta(instance.getClass());
		boolean comma = false;
		for(PropertyMetaModel<?> pmm: cmm.getProperties()) {
			comma = renderProperty(instance, pmm, valueMap, path, comma);
		}
		return comma;
	}

	private <T> boolean renderProperty(Object instance, PropertyMetaModel<T> pmm, Map<String, Object> valueMap, String path, boolean comma) throws Exception {
		T value = pmm.getValue(instance);
		if(null == value)
			return comma;

		IRenderType<T> renderer = JsModel.findRenderer(pmm);
		if(null != renderer) {
			if(comma)
				m_output.append(',');
			m_output.append(pmm.getName()).append(":");
			renderer.render(m_output, value);
			valueMap.put(pmm.getName(), value);
			return true;
		}

		if(value instanceof List) {
			//-- Child list properties.


			throw new IllegalStateException("Child list of unidentified not supported");
		}

		//-- Must be a parent.
		Class<?> pcl = value.getClass();
		JsClass jcl = pcl.getAnnotation(JsClass.class);
		if(null == jcl)
			return comma;

		//-- Parent class render
		Map<String, Object> altMap = (Map<String, Object>) valueMap.get(pmm.getName());
		if(null == altMap) {
			altMap = new HashMap<>();
			valueMap.put(pmm.getName(), altMap);
		}

		if(comma) {
			m_output.append(",");
		}
		m_output.append("new ").append(value.getClass().getSimpleName()).append("({");
	 	renderUnidentified(value, path, altMap);
		m_output.append("})");
		return true;
	}

	private <T> boolean renderUnidentified(T instance, String path, Map<String, Object> valueMap) throws Exception{
		ClassMetaModel cmm = MetaManager.findClassMeta(instance.getClass());
		boolean comma = false;
		for(PropertyMetaModel<?> pmm: cmm.getProperties()) {
			comma = renderProperty(instance, pmm, valueMap, path, comma);
		}
		return comma;
	}


	private boolean renderSimpleProperties(ClassInfo ci, Object instance) throws Exception {
		boolean comma = false;
		for(Entry<PropertyMetaModel<?>, Simple<?>> me : ci.getSimpleProperties().entrySet()) {
			if(comma)
				m_output.append(',');
			renderSimpleProperty(instance, me.getValue());
			comma = true;
		}
		return comma;
	}

	private <T> void renderSimpleProperty(Object instance, Simple<T> simple) throws Exception {
		m_output.append(simple.getProperty().getName()).append(":");
		T value = simple.getProperty().getValue(instance);
		if(null == value) {
			m_output.append("null");
			return ;
		}

		simple.getRenderer().render(m_output, value);
	}

	private <T> void renderSimpleProperty(InstanceInfo ii, Simple<T> simple) throws Exception {
		m_output.append(simple.getProperty().getName()).append(":");
		T value = simple.getProperty().getValue(ii.getInstance());
		ii.updateValue(simple.getProperty(), value);
		if(null == value) {
			m_output.append("null");
			return ;
		}

		simple.getRenderer().render(m_output, value);
	}
}
