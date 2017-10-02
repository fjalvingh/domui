package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.init.MetaInitializer.ClassProviderRef;
import to.etc.domui.component.meta.init.MetaInitializer.PropertyProviderRef;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-10-17.
 */
@DefaultNonNull
final public class MetaInitContext {
	/** The list of class objects under construction. */
	private Map<Object, ClassInfo> m_constructionMap = new HashMap<>();

	/** The list of classes that need their metadata constructed due to an exception while calling providers. */
	private List<ClassInfo> m_pendingClassList = new ArrayList<>();

	/** Class Providers that need to run */
	private List<ClassInfo> m_todoProviderClassList = new ArrayList<>();

	/** Indicator that something was done during a loop. */
	private boolean m_worked;

	private static class ClassInfo {
		final private Object m_type;

		@Nullable
		private ClassMetaModel m_model;

		private List<PropertyProviderRef> m_propertyProviderList = Collections.emptyList();

		private List<ClassProviderRef> m_classProviderList = Collections.emptyList();

		public ClassInfo(Object type) {
			m_type = type;
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

		@Nullable public ClassMetaModel getModel() {
			return m_model;
		}

		public void setModel(@Nullable ClassMetaModel model) {
			m_model = model;
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
		ClassInfo ci = m_constructionMap.get(type);
		if(null != ci) {
			ClassMetaModel model = ci.getModel();
			if(null != model) {
				return model;
			}
			throw new ClassModelNotInitializedException(type);
		}
		return MetaInitializer.getModel(this, type);
	}

	public void addPendingClass(Object mc) {
		ClassInfo ci = m_constructionMap.get(mc);
		if(ci != null) {
			return;
		}
		ci = new ClassInfo(mc);
		m_pendingClassList.add(ci);
		m_constructionMap.put(mc, ci);
	}

	/**
	 * Main workhorse: keep initializing things until everything is done.
	 */
	void initializationLoop() {
		for(;;) {
			m_worked = false;

			while(m_pendingClassList.size() > 0) {
				ClassInfo ci = m_pendingClassList.remove(0);
				createClassMetaData(ci);
			}




			if(m_pendingClassList.size() == 0 && m_todoProviderClassList.size() == 0) {
				return;
			}

			if(! m_worked)
				throw new IllegalStateException("Metadata initialization is stuck during initialization: no work could be done");
		}
	}

	/**
	 * Create the initial metadata for a class using whatever factory accepts it. This
	 * step is not allowed to fail.
	 *
	 * @param ci
	 */
	private void createClassMetaData(ClassInfo ci) {
		IClassMetaModelFactory best = MetaInitializer.findModelFactory(ci.getType());
		ClassMetaModel cmm = best.createModel(this, ci.getType());
		ci.setModel(cmm);
		m_worked = true;

		m_todoProviderClassList.add(ci);
	}
}
