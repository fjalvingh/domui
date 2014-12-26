package to.etc.domui.jsmodel;

import to.etc.domui.component.meta.*;
import to.etc.domui.jsmodel.ClassInfo.*;
import to.etc.util.*;

import javax.annotation.*;
import java.util.*;
import java.util.Map.*;

/**
 * This handles the rendering and deltaing of a Javascript model.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/23/14.
 */
public class JsModel {
	final private Map<String, InstanceInfo> m_idMap = new HashMap<>();

	final private Map<Object, InstanceInfo> m_instanceMap = new HashMap<>();

	final private String m_modelRoot;

	final private Appendable m_output;

	private final Object m_rootObject;

	static private Map<Class<?>, IRenderType<?>> m_simpleTypeRendererMap = new HashMap<>();

	private Map<Class<?>, ClassInfo> m_classInfoMap = new HashMap<>();

	private List<InstanceInfo> m_pendingRelationRender = new ArrayList<>();

	public JsModel(String modelRoot, Appendable output, Object rootObject) {
		m_modelRoot = modelRoot;
		m_output = output;
		m_rootObject = rootObject;
	}

	/**
	 * Do an initial render of the structure. This prepares the delta-source too.
	 * @throws Exception
	 */
	public void render() throws Exception {
		m_idMap.clear();

		m_output.append("function() {");

		m_output.append(m_modelRoot).append(".modelReset();");

		//-- Create root.
		ClassInfo ci = getInfo(m_rootObject.getClass());
		if(null == ci)
			throw new IllegalStateException("The root Javascript model object is not annotated with "+JsClass.class.getName());
		//InstanceInfo ii = createInstanceInfo(ci, m_rootObject);
		//
		//String name = m_rootObject.getClass().getSimpleName();
		//m_output.append("var root=new ").append(name).append("({");
		//renderSimpleProperties(ii);
		//m_output.append("});\n");

		collectAllInstances(m_rootObject);
		for(InstanceInfo ii: m_instanceMap.values()) {
			renderInstanceCreate(ii);
		}

		for(InstanceInfo ii: m_instanceMap.values()) {
			renderRelations(ii);
		}
		m_output.append("\n}();\n");
	}

	private void collectAllInstances(Object instance) throws Exception {
		InstanceInfo ii = m_instanceMap.get(instance);
		if(ii != null)
			return;                                // Already collected

		ClassInfo ci = getInfo(instance.getClass());
		if(ci.getIdProperty() == null)
			return;

		ii = createInstanceInfo(ci, instance);

		for(PropertyMetaModel<?> pp : ii.getClassInfo().getParentProperties()) {
			Object val = pp.getValue(instance);
			if(null != val)
				collectAllInstances(val);
		}

		for(PropertyMetaModel<?> cp : ii.getClassInfo().getChildProperties()) {
			Object val = cp.getValue(instance);
			if(val != null) {
				if(! (val instanceof List))
					throw new IllegalStateException("Instance "+MetaManager.identify(instance)+" property "+cp.getName()+" does not return List");
				List<Object> list = (List<Object>) val;
				for(Object li: list) {
					collectAllInstances(li);
				}
			}
		}
	}

	private InstanceInfo createInstanceInfo(Object instance) throws Exception {
		ClassInfo ci = getInfo(instance.getClass());
		return createInstanceInfo(ci, instance);
	}

	private InstanceInfo createInstanceInfo(ClassInfo ci, Object instance) throws Exception {
		PropertyMetaModel<String> idProperty = ci.getIdProperty();
		if(null == idProperty)
			throw new IllegalStateException("Attempt to create instance ref for idless instance "+MetaManager.identify(instance));
		String id = idProperty.getValue(instance);
		if(null == id)
			throw new IllegalStateException("The instance "+MetaManager.identify(instance)+" has a null id property");

		InstanceInfo ii = new InstanceInfo(ci, instance, id);
		m_idMap.put(id, ii);
		m_instanceMap.put(instance, ii);
		return ii;
	}

	private InstanceInfo findInstance(Object instance) {
		InstanceInfo ii = m_instanceMap.get(instance);
		return ii;
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
		InstanceInfo parii = findInstance(value);
		if(null == parii) {
			ClassInfo ci = getInfo(value.getClass());
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

			InstanceInfo chii = findInstance(item);
			if(null == chii) {
				ClassInfo ci = getInfo(item.getClass());
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

	private void renderSimpleProperties(ClassInfo ci, Object instance) throws Exception {
		boolean comma = false;
		for(Entry<PropertyMetaModel<?>, Simple<?>> me : ci.getSimpleProperties().entrySet()) {
			if(comma)
				m_output.append(',');
			renderSimpleProperty(instance, me.getValue());
			comma = true;
		}
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

	static private Class<?>[] SIMPLETYPEAR = {Byte.class, Short.class, Character.class, Integer.class, Long.class, Boolean.class, Double.class, Float.class};

	static private final Set<Class<?>> SIMPLETYPESET = new HashSet<>(Arrays.asList(SIMPLETYPEAR));


	static private <T> void registerRender(IRenderType<T> renderer, Class<?>... clz) {
		for(Class<?> c: clz)
			m_simpleTypeRendererMap.put(c, renderer);
	}

	static public <T> IRenderType<T> findRenderer(PropertyMetaModel<T> property) {
		IRenderType<T> rt = (IRenderType<T>) m_simpleTypeRendererMap.get(property.getActualType());
		return rt;
	}

	static {
		registerRender(new IRenderType<Object>() {
			@Override public void render(Appendable a, Object value) throws Exception {
				a.append(String.valueOf(value));
			}
		}, Byte.class, byte.class, Short.class, short.class, Character.class, char.class, Boolean.class, boolean.class, Integer.class, int.class, Long.class, long.class, Double.class, double.class);

		registerRender(new IRenderType<String>() {
			@Override public void render(Appendable a, String value) throws Exception {
				StringTool.strToJavascriptString(a, value, true);
			}
		}, String.class);

	}

	@Nullable
	private ClassInfo getInfo(Class<?> clzin) {
		ClassInfo ci = m_classInfoMap.get(clzin);
		if(null != ci)
			return ci;
		if(m_classInfoMap.containsKey(clzin))
			return null;

		//-- Create it
		ci = ClassInfo.decode(clzin);
		m_classInfoMap.put(clzin, ci);
		return ci;
	}


}
