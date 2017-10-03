package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.init.MetaInitializer.ClassProviderRef;
import to.etc.domui.component.meta.init.MetaInitializer.PropertyProviderRef;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-10-17.
 */
@DefaultNonNull
final public class MetaInitContext {
	private final Map<Object, ClassMetaModel> m_classMap;

	/** The list of class objects under construction. */
	private Map<Object, ClassInfo> m_constructionMap = new HashMap<>();

	/** The list of classes that need their metadata constructed. */
	private List<ClassInfo> m_todoProviderClassList = new ArrayList<>();

	/** Indicator that something was done during a loop. */
	private boolean m_worked;

	public MetaInitContext(Map<Object, ClassMetaModel> classMap) {
		m_classMap = classMap;
	}

	private static class ClassInfo {
		final private Object m_type;

		private ClassMetaModel m_model;

		private List<PropertyProviderRef> m_propertyProviderList = Collections.emptyList();

		private List<ClassProviderRef> m_classProviderList = Collections.emptyList();

		/** The list of properties still to do for the current property provider */
		@Nullable
		private List<PropertyMetaModel<?>> m_todoPropertyList;

		public ClassInfo(Object type, ClassMetaModel cmm) {
			m_type = type;
			m_model = cmm;
			m_propertyProviderList = MetaInitializer.getPropertyProviderList();
			m_classProviderList = MetaInitializer.getClassProviderList();
		}

		public Object getType() {
			return m_type;
		}

		public List<PropertyProviderRef> getPropertyProviderList() {
			return m_propertyProviderList;
		}

		public List<ClassProviderRef> getClassProviderList() {
			return m_classProviderList;
		}

		public ClassMetaModel getModel() {
			return m_model;
		}

		@Nullable public List<PropertyMetaModel<?>> getTodoPropertyList() {
			return m_todoPropertyList;
		}

		public void setTodoPropertyList(@Nullable List<PropertyMetaModel<?>> todoPropertyList) {
			m_todoPropertyList = todoPropertyList;
		}
	}

	/**
	 * This method is the only one allowed during metamodel initialization. It will
	 * throw {@link ClassModelNotInitializedException} if the class is not yet known.
	 * The method will, however, return incomplete classes (the ones being initialized).
	 *
	 * @param type
	 * @return
	 */
	public ClassMetaModel getModel(Object type) {
		//-- Already known?
		ClassMetaModel cmm = m_classMap.get(type);
		if(null != cmm)
			return cmm;

		//-- Being constructed?
		ClassInfo ci = m_constructionMap.get(type);
		if(null != ci) {
			return ci.getModel();
		}

		//-- We need to add this.
		IClassMetaModelFactory best = MetaInitializer.findModelFactory(type);
		cmm = best.createModel(this, type);
		ci = new ClassInfo(type, cmm);
		m_todoProviderClassList.add(0, ci);
		m_constructionMap.put(type, ci);
		return cmm;
		//throw new ClassModelNotInitializedException(type);
	}

	/**
	 * Main workhorse: keep initializing things until everything is done.
	 */
	void initializationLoop() throws Exception {
		m_worked = false;
		int notWorkCount = 0;
		while(m_todoProviderClassList.size() > 0) {
			handlePendingClasses();
			if(! m_worked) {
				notWorkCount++;
				if(notWorkCount > 1000)
					throw new IllegalStateException("Metadata initialization is stuck during initialization: no work could be done");
			} else {
				notWorkCount = 0;
			}
		}
	}

	private void handlePendingClasses() throws Exception {
		while(m_todoProviderClassList.size() > 0) {
			ClassInfo ci = m_todoProviderClassList.get(0);

			for(;;) {
				ClassProviderRef cpr = null;
				PropertyProviderRef ppr = null;
				if(ci.getClassProviderList().size() > 0) {
					cpr = ci.getClassProviderList().get(0);
				}
				if(ci.getPropertyProviderList().size() > 0) {
					ppr = ci.getPropertyProviderList().get(0);
				}
				if(ppr != null && (cpr == null || ppr.getOrder() <= cpr.getOrder())) {
					//-- We need to execute the property provider.
					handlePropertyProvider(ppr, ci);
				} else if(cpr != null) {
					handleClassProvider(cpr, ci);
				} else if(ppr == null && cpr == null) {
					//-- All actions for the class are done -> move it to finished
					m_todoProviderClassList.remove(ci);			// Nothing to be done anymore
					m_constructionMap.remove(ci.getType());		// No longer under construction.
					m_classMap.put(ci.getType(), ci.getModel());// Store in final classmap
					return;
				} else {
					throw new IllegalStateException("Logic error: no provider selected");
				}
			}
		}
	}

	private void handleClassProvider(ClassProviderRef cpr, ClassInfo ci) throws Exception {
		try {
			cpr.getProvider().provide(this, Objects.requireNonNull(ci.getModel()));
			m_worked = true;
			ci.getClassProviderList().remove(0);
		} catch(ClassModelNotInitializedException cmx) {
			queueClassLast(ci);
		}
	}

	private void handlePropertyProvider(PropertyProviderRef ppr, ClassInfo ci) throws Exception {
		try {
			//-- Do we have properties to-do?
			List<PropertyMetaModel<?>> propList = ci.getTodoPropertyList();
			ClassMetaModel cmm = ci.getModel();
			if(null == propList || propList.size() == 0) {
				propList = new ArrayList<>(cmm.getProperties());
				ci.setTodoPropertyList(propList);
			}

			while(propList.size() > 0) {
				PropertyMetaModel<?> pmm = propList.get(0);
				ppr.getProvider().provide(this, cmm, pmm);
				propList.remove(0);
				m_worked = true;
			}

			//-- because we finished all properties- remove the provider.
			ci.getPropertyProviderList().remove(0);
			m_worked = true;										// For the uncommon case of a class without properties.
		} catch(ClassModelNotInitializedException cmx) {
			queueClassLast(ci);
		}
	}

	private void queueClassLast(ClassInfo ci) {
		m_todoProviderClassList.remove(ci);
		m_todoProviderClassList.add(ci);						// Move to end
	}

}
