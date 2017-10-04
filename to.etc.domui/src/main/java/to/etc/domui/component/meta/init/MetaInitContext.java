package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.init.MetaInitializer.ClassProviderRef;
import to.etc.domui.component.meta.init.MetaInitializer.PropertyProviderRef;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private List<ClassAction> m_todoProviderClassList = new ArrayList<>();

	/** Indicator that something was done during a loop. */
	private boolean m_worked;

	private boolean m_markSortRequired;

	public MetaInitContext(Map<Object, ClassMetaModel> classMap) {
		m_classMap = classMap;
	}

	private final static class ClassAction {
		private final ClassInfo m_classInfo;

		private final int m_order;

		@Nullable
		private final IPropertyMetaProvider<?, ?> m_propertyProvider;

		@Nullable
		private final IClassMetaProvider<?> m_classProvider;

		public ClassAction(ClassInfo ci, int order, IPropertyMetaProvider<?, ?> propertyProvider) {
			m_classInfo = ci;
			m_order = order;
			m_propertyProvider = propertyProvider;
			m_classProvider = null;
		}

		public ClassAction(ClassInfo ci, int order, IClassMetaProvider<?> classProvider) {
			m_classInfo = ci;
			m_order = order;
			m_classProvider = classProvider;
			m_propertyProvider = null;
		}

		public int getOrder() {
			return m_order;
		}

		public ClassInfo getClassInfo() {
			return m_classInfo;
		}

		@Nullable public IPropertyMetaProvider<?, ?> getPropertyProvider() {
			return m_propertyProvider;
		}

		@Nullable public IClassMetaProvider<?> getClassProvider() {
			return m_classProvider;
		}
	}

	private static class ClassInfo {
		final private Object m_type;

		private ClassMetaModel m_model;

		private int m_pendingActionCount;

		/** The list of properties still to do for the current property provider */
		@Nullable
		private List<PropertyMetaModel<?>> m_todoPropertyList;

		/** When set the current property provider that needs to continue. */
		@Nullable
		private IPropertyMetaProvider<?, ?> m_propertyProvider;

		public ClassInfo(Object type, ClassMetaModel cmm) {
			m_type = type;
			m_model = cmm;
		}

		public Object getType() {
			return m_type;
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

		@Nullable public IPropertyMetaProvider<?, ?> getPropertyProvider() {
			return m_propertyProvider;
		}

		public void setPropertyProvider(@Nullable IPropertyMetaProvider<?, ?> propertyProvider) {
			m_propertyProvider = propertyProvider;
		}

		public void setPendingActionCount(int pendingActionCount) {
			m_pendingActionCount = pendingActionCount;
		}

		boolean decrementActionCount() {
			return --m_pendingActionCount <= 0;
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

		//-- Add all class actions
		ClassInfo ci2 = ci;						// The java "architects" are morons with their final idiocy.
		List<ClassProviderRef> classProviderList = MetaInitializer.getClassProviderList();
		classProviderList.forEach(cp -> m_todoProviderClassList.add(new ClassAction(ci2, cp.getOrder(), cp.getProvider())));

		//-- And the same for all property actions
		List<PropertyProviderRef> propertyProviderList = MetaInitializer.getPropertyProviderList();
		propertyProviderList.forEach(pp -> m_todoProviderClassList.add(new ClassAction(ci2, pp.getOrder(), pp.getProviderSupplier().get())));

		ci.setPendingActionCount(propertyProviderList.size() + classProviderList.size());
		m_constructionMap.put(type, ci);
		m_markSortRequired = true;
		return cmm;
	}

	/**
	 * Main workhorse: keep initializing things until everything is done.
	 */
	void initializationLoop() throws Exception {
		m_worked = false;
		int notWorkCount = 0;
		while(m_todoProviderClassList.size() > 0) {
			int currentCount = m_todoProviderClassList.size();
			handlePendingAction();
			if(! m_worked && m_todoProviderClassList.size() == currentCount) {
				notWorkCount++;
				if(notWorkCount > 1000)
					throw new IllegalStateException("Metadata initialization is stuck during initialization: no work could be done");
			} else {
				notWorkCount = 0;
			}
		}
	}

	private void handlePendingAction() throws Exception {
		if(m_markSortRequired) {
			m_markSortRequired = false;
			m_todoProviderClassList.sort(Comparator.comparingInt(ClassAction::getOrder));	// Ascending order
		}
		ClassAction action = m_todoProviderClassList.get(0);
		ClassInfo ci = action.getClassInfo();
		IClassMetaProvider<?> cp = action.getClassProvider();
		IPropertyMetaProvider<?, ?> pp = action.getPropertyProvider();
		boolean actionCompleted = false;
		if(null != cp) {
			actionCompleted = handleClassProvider((IClassMetaProvider<ClassMetaModel>) cp, ci);
		} else if(null != pp) {
			actionCompleted = handlePropertyProvider((IPropertyMetaProvider<ClassMetaModel, PropertyMetaModel<?>>) pp, ci);
		} else
			throw new IllegalStateException("Both class and property providers are empty");

		if(actionCompleted) {
			m_todoProviderClassList.remove(action);
			if(ci.decrementActionCount()) {
				//-- All actions completed -> register class.
				m_todoProviderClassList.remove(ci);			// Nothing to be done anymore
				m_constructionMap.remove(ci.getType());		// No longer under construction.
				m_classMap.put(ci.getType(), ci.getModel());// Store in final classmap
			}
		}
	}

	private boolean handleClassProvider(IClassMetaProvider<ClassMetaModel> provider, ClassInfo ci) throws Exception {
		try {
			if(provider.getModelClass().isAssignableFrom(ci.getModel().getClass())) {
				provider.provide(this, ci.getModel());
			}
			m_worked = true;
			return true;
		} catch(ClassModelNotInitializedException cmx) {
			return false;
		}
	}

	/**
	 * Handle a property provider and call it for all properties of the class.
	 */
	private boolean handlePropertyProvider(IPropertyMetaProvider<ClassMetaModel, PropertyMetaModel<?>> newProvider, ClassInfo ci) throws Exception {
		try {
			//-- Do we have properties to-do?
			List<PropertyMetaModel<?>> propList = ci.getTodoPropertyList();
			IPropertyMetaProvider<ClassMetaModel, PropertyMetaModel<?>> currentProvider = (IPropertyMetaProvider<ClassMetaModel, PropertyMetaModel<?>>) ci.getPropertyProvider();
			ClassMetaModel cmm = ci.getModel();
			if(null == propList || propList.size() == 0 || currentProvider == null) {
				//-- We're at the start of a new list.
				currentProvider = newProvider;
				if(! currentProvider.getClassModelClass().isAssignableFrom(ci.getModel().getClass())) {
					//-- We cannot use this-> skip
					m_worked = true;
					return true;
				}

				propList = new ArrayList<>(cmm.getProperties());
				ci.setTodoPropertyList(propList);
				ci.setPropertyProvider(currentProvider);				// And store it for continuations
				currentProvider.beforeProperties(this, ci.getModel());
			} else if(newProvider != currentProvider)
				throw new IllegalStateException("Continuation for property provider with new provider requested!?");

			while(propList.size() > 0) {
				PropertyMetaModel<?> pmm = propList.get(0);
				currentProvider.provide(this, cmm, pmm);
				propList.remove(0);
				m_worked = true;
			}

			//-- because we finished all properties- remove the provider.
			ci.setPropertyProvider(null);
			ci.setTodoPropertyList(null);
			currentProvider.afterPropertiesDone(this, ci.getModel());
			m_worked = true;										// For the uncommon case of a class without properties.
			return true;
		} catch(ClassModelNotInitializedException cmx) {
			return false;
		}
	}
}
