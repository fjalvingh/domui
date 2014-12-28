package to.etc.domui.jsmodel;

import to.etc.domui.component.meta.*;
import to.etc.domui.jsmodel.ClassInfo.*;

import java.util.*;
import java.util.Map.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/26/14.
 */
public class JsInitialRenderer extends JsRenderBase {
	public JsInitialRenderer(Appendable output, JsModel model) {
		super(output, output, model);
	}

	public void render() throws Exception {
		appendSet("function() {");

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
		appendSet("\n}();\n");
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
			appendSet("v").append(ii.getId()).append("._").append(pp.getName()).append("= new ").append(ci.getSimpleName()).append("({");
			renderSimpleProperties(ci, value);
			appendSet("});\n");
			return;
		}

		appendSet("v").append(ii.getId()).append("._").append(pp.getName()).append("=v").append(parii.getId()).append(";\n");
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

		appendSet("v").append(ii.getId()).append("._").append(cp.getName()).append(" = [");
		int count = 0;
		for(Object item: list) {
			if(count % 10 == 9)
				appendSet("\n");
			if(count++ != 0)
				appendSet(",");

			InstanceInfo chii = m_model.findInstance(item);
			if(null == chii) {
				ClassInfo ci = m_model.getInfo(item.getClass());
				if(ci.getIdProperty() != null)
					throw new IllegalStateException("Cannot find list child instance " + MetaManager.identify(item));
				appendSet("new ").append(ci.getSimpleName()).append("({");
				renderSimpleProperties(ci, item);
				appendSet("})");
			} else {
				appendSet("v").append(chii.getId());
			}

		}
		appendSet("];\n");
	}

	private void renderInstanceCreate(InstanceInfo ii) throws Exception {
		String name = ii.getClassInfo().getSimpleName();
		appendSet("var v").append(ii.getId()).append("=new ").append(name).append("({");
		renderSimpleProperties(ii);
		appendSet("});\n");
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
				appendSet(",");
			renderSimpleProperty(ii, me.getValue());
			comma = true;
		}

		for(PropertyMetaModel<?> pmm: ci.getValueProperties()) {
			if(comma) {
				appendSet(",");
			}
			renderValueObject(ii, pmm);
			comma = true;
		}
	}

	private boolean renderSimpleProperties(ClassInfo ci, Object instance) throws Exception {
		boolean comma = false;
		for(Entry<PropertyMetaModel<?>, Simple<?>> me : ci.getSimpleProperties().entrySet()) {
			if(comma)
				appendSet(",");
			renderSimpleProperty(instance, me.getValue());
			comma = true;
		}
		return comma;
	}

	private <T> void renderSimpleProperty(Object instance, Simple<T> simple) throws Exception {
		appendSet(simple.getProperty().getName()).append(":");
		T value = simple.getProperty().getValue(instance);
		if(null == value) {
			appendSet("null");
			return ;
		}

		simple.getRenderer().render(getSetSb(), value);
	}

	private <T> void renderSimpleProperty(InstanceInfo ii, Simple<T> simple) throws Exception {
		appendSet(simple.getProperty().getName()).append(":");
		T value = simple.getProperty().getValue(ii.getInstance());
		ii.updateValue(simple.getProperty(), value);
		if(null == value) {
			appendSet("null");
			return ;
		}

		simple.getRenderer().render(getSetSb(), value);
	}
}
