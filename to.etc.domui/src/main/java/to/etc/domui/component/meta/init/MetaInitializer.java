package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.impl.PathPropertyMetaModel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-10-17.
 */
public final class MetaInitializer {
	static private List<IClassMetaModelFactory> m_modelList = new ArrayList<IClassMetaModelFactory>();

	/**
	 * Map indexed by Class<?> or IMetaClass returning the {@link ClassMetaModel} for that instance.
	 */
	static private Map<Object, ClassMetaModel> m_classMap = new HashMap<>();

	/** While a metamodel is being initialized this keeps track of recursive init's */
	final static private Stack<Object> m_initStack = new Stack<Object>();

	final static private List<Runnable> m_initList = new ArrayList<Runnable>();

	static synchronized public void registerModel(@Nonnull IClassMetaModelFactory model) {
		List<IClassMetaModelFactory> mm = new ArrayList<IClassMetaModelFactory>(m_modelList);
		mm.add(model);
		m_modelList = mm;
	}

	@Nonnull
	static public synchronized List<IClassMetaModelFactory> getList() {
		if(m_modelList.size() == 0)
			registerModel(new DefaultJavaClassMetaModelFactory());
		return m_modelList;
	}

	/**
	 * Clears the cache. In use by reloading class mechanism,
	 * hence only ever called while developing never in
	 * production. <b>INTERNAL USE ONLY, DO NOT USE</b>
	 */
	public synchronized static void internalClear() {
		m_classMap.clear();
	}

	@Nonnull
	static public ClassMetaModel findAndInitialize(@Nonnull Object mc) {
		//-- We need some factory to create it.
		synchronized(MetaManager.class) {
			ClassMetaModel cmm = m_classMap.get(mc);
			if(cmm != null)
				return cmm;

			//-- Phase 1: create the metamodel and it's direct properties.
			checkInitStack(mc, "primary initialization");				// Signal any ordering problems
			IClassMetaModelFactory best = findModelFactory(mc);
			m_initStack.add(mc);
			cmm = best.createModel(m_initList, mc);
			m_classMap.put(mc, cmm);
			m_initStack.remove(mc);

			//-- Phase 2: create the secondary model.
			if(m_initStack.size() == 0 && m_initList.size() > 0) {
				List<Runnable> dl = new ArrayList<Runnable>(m_initList);
				m_initList.clear();
				for(Runnable r : dl) {
					r.run();
				}
			}
			return cmm;
		}
	}

	private static void checkInitStack(Object mc, String msg) {
		if(m_initStack.contains(mc)) {
			m_initStack.add(mc);
			StringBuilder sb = new StringBuilder();
			for(Object o : m_initStack) {
				if(sb.length() > 0)
					sb.append(" -> ");
				sb.append(o.toString());
			}
			m_initStack.clear();

			throw new IllegalStateException("Circular reference in " + msg + ": " + sb.toString());
		}
	}

	/**
	 * We need to find a factory that knows how to deliver this metadata.
	 */
	@Nonnull
	private synchronized static IClassMetaModelFactory findModelFactory(Object theThingy) {
		int bestscore = 0;
		int hitct = 0;
		IClassMetaModelFactory best = null;
		for(IClassMetaModelFactory mmf : getList()) {
			int score = mmf.accepts(theThingy);
			if(score > 0) {
				if(score == bestscore)
					hitct++;
				else if(score > bestscore) {
					bestscore = score;
					best = mmf;
					hitct = 1;
				}
			}
		}

		//-- We MUST have some factory now, or we're in trouble.
		if(best == null)
			throw new IllegalStateException("No IClassModelFactory accepts the type '" + theThingy + "', which is a " + theThingy.getClass());
		if(hitct > 1)
			throw new IllegalStateException("Two IClassModelFactory's accept the type '" + theThingy + "' (which is a " + theThingy.getClass() + ") at score=" + bestscore);
		return best;
	}


	static public PropertyMetaModel< ? > internalCalculateDottedPath(ClassMetaModel cmm, String name) {
		int pos = name.indexOf('.'); 							// Dotted name?
		if(pos == -1)
			return cmm.findSimpleProperty(name); 				// Use normal resolution directly on the class.

		//-- We must create a synthetic property.
		int ix = 0;
		int len = name.length();
		ClassMetaModel ccmm = cmm; 								// Current class meta-model for property reached
		List<PropertyMetaModel< ? >> acl = new ArrayList<PropertyMetaModel< ? >>(10);
		for(;;) {
			String sub = name.substring(ix, pos); 				// Get path component,
			ix = pos + 1;

			PropertyMetaModel< ? > pmm = ccmm.findSimpleProperty(sub); // Find base property,
			if(pmm == null)
				throw new IllegalStateException("Invalid property path '" + name + "' on " + cmm + ": property '" + sub + "' on classMetaModel=" + ccmm + " does not exist");
			acl.add(pmm); // Next access path,
			ccmm = MetaManager.findClassMeta(pmm.getActualType());

			if(ix >= len)
				break;
			pos = name.indexOf('.', ix);
			if(pos == -1)
				pos = len;
		}

		//-- Resolved to target. Return a complex proxy.
		return new PathPropertyMetaModel<Object>(name, acl.toArray(new PropertyMetaModel[acl.size()]));
	}

	/**
	 * Return a list of all classes whose metamodel is known.
	 *
	 * @return
	 */
	public static synchronized List<ClassMetaModel> getAllMetaClasses() {
		return new ArrayList<>(m_classMap.values());
	}
}
