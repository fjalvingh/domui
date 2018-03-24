package to.etc.domui.jsmodel;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.jsmodel.ClassInfo.Simple;
import to.etc.util.StringTool;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This handles the rendering and deltaing of a Javascript model.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/23/14.
 */
public class JsModel {
	final private Map<String, InstanceInfo> m_idMap = new HashMap<>();

	final private Map<Object, InstanceInfo> m_instanceMap = new HashMap<>();

	private final Object m_rootObject;

	static private Map<Class<?>, IRenderType<?>> m_simpleTypeRendererMap = new HashMap<>();

	private Map<Class<?>, ClassInfo> m_classInfoMap = new HashMap<>();

	@Nullable
	private Set<InstanceInfo> m_reachableSet;

	public JsModel(Object rootObject) {
		m_rootObject = rootObject;
	}


	@Nullable public Set<InstanceInfo> getReachableSet() {
		return m_reachableSet;
	}

	public void setReachableSet(@Nullable Set<InstanceInfo> reachableSet) {
		m_reachableSet = reachableSet;
	}

	/**
	 * Do an initial render of the structure. This prepares the delta-source too.
	 * @throws Exception
	 */
	public void renderFull(Appendable output) throws Exception {
		JsInitialRenderer r = new JsInitialRenderer(output, this);
		r.render();
	}

	public void renderDelta(Appendable output) throws Exception {
		JsDeltaRenderer r = new JsDeltaRenderer(output, this, "vroot");
		r.render();
	}

	Set<InstanceInfo> collectAllInstances(Object root) throws Exception {
		Map<Object, InstanceInfo> newMap = new HashMap<>();
		collectAllInstances(newMap, root);
		return new HashSet<>(newMap.values());
	}

	private void collectAllInstances(Map<Object, InstanceInfo> list, Object instance) throws Exception {
		if(list.containsKey(instance))
			return;

		InstanceInfo ii = m_instanceMap.get(instance);
		if(ii == null) {
			ClassInfo ci = getInfo(instance.getClass());
			if(null == ci)
				throw new IllegalStateException("Cannot get class info for " + instance);


			if(ci.getIdProperty() == null)
				return;

			ii = createInstanceInfo(ci, instance);
		}
		list.put(instance, ii);

		for(PropertyMetaModel<?> pp : ii.getClassInfo().getParentProperties()) {
			Object val = pp.getValue(instance);
			if(null != val)
				collectAllInstances(list, val);
		}

		for(PropertyMetaModel<?> cp : ii.getClassInfo().getChildProperties()) {
			Object val = cp.getValue(instance);
			if(val != null) {
				if(! (val instanceof List))
					throw new IllegalStateException("Instance "+MetaManager.identify(instance)+" property "+cp.getName()+" does not return List");
				List<Object> chlist = (List<Object>) val;
				for(Object li: chlist) {
					collectAllInstances(list, li);
				}
			}
		}
	}

	private InstanceInfo createInstanceInfo(Object instance) throws Exception {
		ClassInfo ci = getInfo(instance.getClass());
		if(null == ci)
			throw new IllegalStateException("Cannot get class info for " + instance);
		return createInstanceInfo(ci, instance);
	}

	private InstanceInfo createInstanceInfo(ClassInfo ci, Object instance) throws Exception {
		if(null == ci)
			throw new IllegalStateException("Null classinfo?");
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

	InstanceInfo findInstance(Object instance) {
		InstanceInfo ii = m_instanceMap.get(instance);
		return ii;
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
	ClassInfo getInfo(Class<?> clzin) {
		ClassInfo ci = m_classInfoMap.get(clzin);
		if(null != ci)
			return ci;
		if(m_classInfoMap.containsKey(clzin))
			return null;

		//-- Must be annotated with @JsClass or we'll ignore it
		JsClass jcl = clzin.getAnnotation(JsClass.class);
		if(null == jcl) {
			m_classInfoMap.put(clzin, null);
			return null;
		}

		ClassMetaModel cmm = MetaManager.findClassMeta(clzin);
		PropertyMetaModel<String> idProp = (PropertyMetaModel<String>) cmm.findProperty("id");
		ci = new ClassInfo(clzin.getSimpleName(), idProp);
		m_classInfoMap.put(clzin, ci);

		Map<PropertyMetaModel<?>, Simple<?>> simpleProps = new HashMap<>();
		List<PropertyMetaModel<?>> childProps = new ArrayList<>();
		List<PropertyMetaModel<?>> parentProps = new ArrayList<>();
		List<PropertyMetaModel<?>> valueProps = new ArrayList<>();

		for(PropertyMetaModel<?> property : cmm.getProperties()) {
			Simple< ? > simple = createSimple(property);
			if(null != simple) {
				simpleProps.put(property, simple);
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
					ClassInfo dadci = getInfo(property.getActualType());
					if(dadci != null) {
						if(dadci.getIdProperty() == null)
							valueProps.add(property);
						else
							parentProps.add(property);
					}
				}
			}
		}
		ci.update(simpleProps, parentProps, childProps, valueProps);
		return ci;
	}

	@Nullable
	private <T> Simple<T> createSimple(PropertyMetaModel<T> pmm) {
		IRenderType<T> renderer = JsModel.findRenderer(pmm);
		if(null != renderer)
			return new Simple<T>(pmm, renderer);
		return null;
	}

	public Object getRootObject() {
		return m_rootObject;
	}
}
