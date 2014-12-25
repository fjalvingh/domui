package to.etc.domui.jsmodel;

import to.etc.domui.component.meta.*;
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
	final private Map<String, InstanceData> m_idMap = new HashMap<>();

	final private String m_modelRoot;

	final private Appendable m_output;

	private final Object m_rootObject;

	static private Map<Class<?>, IRenderType<?>> m_simpleTypeRendererMap = new HashMap<>();

	private Map<Class<?>, ClassInfo> m_classInfoMap = new HashMap<>();

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

		m_output.append(m_modelRoot).append(".modelReset();");

		//-- Create root.
		ClassInfo ci = getInfo(m_rootObject.getClass());
		if(null == ci)
			throw new IllegalStateException("The root Javascript model object is not annotated with "+JsClass.class.getName());

		String name = m_rootObject.getClass().getSimpleName();
		m_output.append(m_modelRoot).append(" = new ").append(name).append("({");
		ClassMetaModel cmm = MetaManager.findClassMeta(m_rootObject.getClass());
		renderSimpleProperties(ci, m_rootObject);
		m_output.append("});\n");

		//-- Now render all dependents that root at the model.
		renderRelations(cmm, m_rootObject);
		m_output.append(m_modelRoot).append(".modelComplete();");
	}

	private void renderRelations(ClassMetaModel cmm, Object instance) throws Exception {
		boolean comma = false;
		for(PropertyMetaModel<?> property: cmm.getProperties()) {
			if(renderIfSimpleProperty(instance, property, comma)) {
				comma = true;
			}
		}
	}

	private void renderObject(Object modelRoot) throws Exception {
		ClassMetaModel cmm = MetaManager.findClassMeta(modelRoot.getClass());
		PropertyMetaModel<String> idProp = (PropertyMetaModel<String>) cmm.getProperty("id");				// Must have an ID property of type String
		String id = idProp.getValue(modelRoot);
		if(id == null)
			throw new IllegalStateException("The object instance "+modelRoot+" has a null ID");
		InstanceData instanceData = m_idMap.get(id);
		if(null != instanceData)
			throw new IllegalStateException("Duplicate instance with ID="+id);

		//-- We will render a 'createXxxxx' call denoting the simple properties in there.


		boolean comma = false;
		for(PropertyMetaModel<?> property: cmm.getProperties()) {
			if(renderIfSimpleProperty(modelRoot, property, comma)) {
				comma = true;
			}
		}
	}

	/**
	 * Render all primitive properties of a thing as a javascript object format fragment.
	 * @param ci
	 * @param of
	 * @throws Exception
	 */
	private void renderSimpleProperties(ClassInfo ci, Object of) throws Exception {
		boolean comma = false;
		for(Entry<PropertyMetaModel<?>, IRenderType<?>> me : ci.getSimpleProperties().entrySet()) {
			if(comma)
				m_output.append(',');
			renderSimpleProperty(of, me.getKey(), dummy(me));
			comma = true;
		}
	}

	private <T> Map.Entry<PropertyMetaModel<T>, IRenderType<T>> dummy(Map.Entry<PropertyMetaModel<?>, IRenderType<?>> me) {
	}

	private <T> void renderSimpleProperty(Object of, PropertyMetaModel<T> property, IRenderType<T> renderer) throws Exception {
		m_output.append(me.getKey().getName()).append(":");
		T value = me.getKey().getValue(of);
		if(null == value) {
			m_output.append("null");
			return ;
		}

		rt.render(m_output, value);
	}

	private <T> boolean renderIfSimpleProperty(Object modelRoot, PropertyMetaModel<T> property, boolean comma) throws Exception {
		IRenderType<T> rt = (IRenderType<T>) m_simpleTypeRendererMap.get(property.getActualType());
		if(null == rt)
			return false;

		if(comma) {
			m_output.append(',');
		}
		return true;
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
