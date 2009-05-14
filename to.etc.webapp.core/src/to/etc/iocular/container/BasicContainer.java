package to.etc.iocular.container;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import to.etc.iocular.BindingScope;
import to.etc.iocular.Container;
import to.etc.iocular.def.ComponentDef;
import to.etc.iocular.def.ComponentRef;
import to.etc.iocular.def.ContainerDefinition;
import to.etc.util.IndentWriter;

/**
 * <p>This is a default implementation of an IOC container. While it
 * exists it caches all instance objects. This object is threadsafe.</p>
 * 
 * <p>If an object really needs to be created we do this in two
 * phases. The first phase creates a "build plan" for the object. The build plan
 * considers all possible ways to create the given object, and will select the most
 * optimal plan. The second phase will then execute the plan to actually create the
 * object.</p>
 * 
 * <p>The separation into plan and create phase is needed because an object can be
 * created across many ways (for instance because it has multiple constructors). We cannot
 * try each method in turn because trying would force us to create objects that might not
 * be needed after all when a given way to instantiate is not possible. We prevent this
 * by first determining a plan which is guaranteed to work.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 27, 2007
 */
public class BasicContainer implements Container {
	/** True when this container is in 'started' state. */
	private boolean				m_started;

	/** If this is a child container this refers to it's parent. It is null for a root container. */
	private BasicContainer		m_parent;

	/** The definition of this container. */
	private ContainerDefinition	m_def;

	/** The stack of containers that are parents of this one, indexed by ComponentRef.index. Used to quickly get a specific parent. */
	private BasicContainer[]	m_stack;

	private Map<ComponentDef, StaticComponentRef>	m_staticMap = new HashMap<ComponentDef, StaticComponentRef>();

	/** Map of all instantiated objects of a given definition */
	private Map<ComponentDef, ContainerObjectRef>	m_singletonMap = new HashMap<ComponentDef, ContainerObjectRef>();

	public BasicContainer(ContainerDefinition def, Container parent) {
		if(def == null)
			throw new IllegalStateException("The definition for a container cannot be null of course");
		if(parent == null && def.getParentDefinition() != null)
			throw new IllegalStateException("This container's definition states that this container REQUIRES a parent container '"+def.getParentDefinition().getName()+"'");
		if(parent != null && def.getParentDefinition() == null)
			throw new IllegalStateException("This container's definition states that it has NO parent container, but one was passed anyway?");
		m_def = def;
		m_parent = (BasicContainer)parent;

		//-- Create the container stack
		m_stack = new BasicContainer[ def.getContainerIndex() +1 ];
		m_stack[def.getContainerIndex()] = this;
		BasicContainer	c = (BasicContainer) parent;

		while(c != null) {
			m_stack[c.m_def.getContainerIndex()] = c;
			c = c.m_parent;
		}
	}

	public synchronized void start() {
		if(m_started)
			throw new IllegalStateException(this+": container has already been started!!");
		m_started = true;
		
	}
	public synchronized void destroy() {
		if(m_started) {
			// TODO Destroy all created objects
			
			m_started = false;
		}
	}

	public String	getIdent() {
		return m_def.getName();
	}

	/**
	 * Gets a Ref for a given definition. If the ref does not yet
	 * exists it gets created atomically.
	 *
	 * @param cd
	 * @return
	 */
	private synchronized ContainerObjectRef	getRef(ComponentDef cd) {
		if(! m_started)
			throw new IllegalStateException(this+": the container has been destroyed, or it has not yet been started.");
		ContainerObjectRef	ref = m_singletonMap.get(cd);
		if(ref == null) {
			ref = new ContainerObjectRef(cd);
			m_singletonMap.put(cd, ref);
		}
		return ref;
	}

	public <T> T findObject(Class<T> theClass) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T findObject(String name, Class<T> theClass) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getObject(Class<T> theClass) throws Exception {
		ComponentRef	ref = m_def.findComponentReference(theClass);
		if(ref == null)
			throw new IocContainerException(this, "Can't create object of type="+theClass.getName()+": definition is not found");
		return (T) retrieve(ref);
	}

	public <T> T getObject(String name, Class<T> theClass) throws Exception {
		ComponentRef	ref = m_def.findComponentReference(name);
		if(ref == null)
			throw new IocContainerException(this, "Object with name="+name+" not defined");
		return (T) retrieve(ref);
	}

	/**
	 * Actually get the required thingy. If it is already present in this container (and a singleton)
	 * it will be created atomically; if it is a prototype it will be created without lock.
	 *
	 * @param <T>
	 * @param theClass
	 * @param ref
	 * @return
	 */
	public Object	retrieve(ComponentRef ref) throws Exception {
		return m_stack[ref.getContainerIndex()]._retrieve(ref);
	}

	/**
	 * Retrieve or create an actual instance of an object in *this* container.
	 *
	 * @param <T>
	 * @param theClass
	 * @param ref
	 * @return
	 */
	private Object	_retrieve(ComponentRef cr) throws Exception {
		if(cr.getContainerIndex() != m_def.getContainerIndex())
			throw new IllegalStateException("Internal: Container index does not correspond with this container's index!?");
		handleOneTimeInit(cr.getDefinition());	// Handle any static factory initializations.

		//-- If this is a "prototype" (a class that is always created anew) then just create without lock
		if(cr.getDefinition().getScope() == BindingScope.PROTOTYPE) {
			return createObject(cr.getDefinition());
		}

		//-- Singleton object in this scope. Get a ref, then proceed to create
		ContainerObjectRef	ref = getRef(cr.getDefinition());
		Object	obj = ref.retrieveOrOwn(this);
		if(obj != null)
			return obj;

		//-- We have obtained the right to create this object. So create it;
		try {
			Object theObject = createObject(cr.getDefinition());
			ref.setObject(theObject);
			return theObject;
		} finally {
			ref.releaseOwnership();				// Make sure ownership is released even when erroring
		}
	}

	/**
	 * Unlocked create of an object, using the current container stack.
	 * @param cd
	 * @return
	 */
	private Object	createObject(ComponentDef cd) throws Exception {
//		dump(cd, cd.getBuildPlan());
		try {
			return cd.getBuildPlan().getObject(this);
		} catch(InvocationTargetException itx) {
			if(itx.getCause() instanceof Error)
				throw (Error) itx.getCause();
			else if(itx.getCause() instanceof Exception)
				throw (Exception) itx.getCause();
			throw itx;
		}
	}

	/**
	 * Called to check if a component needs static initialization.
	 * @param cr
	 */
	private void handleOneTimeInit(ComponentDef def) throws Exception {
		BuildPlan	pl	= def.getBuildPlan();
		if(! pl.needsStaticInitialization())
			return;

		//-- Check to see if init has been done, using 2-part locking. First get thingy ref atomically then own it,
		StaticComponentRef	sr;
		synchronized(m_staticMap) {
			sr = m_staticMap.get(def);
			if(sr == null) {
				sr = new StaticComponentRef(def);
				m_staticMap.put(def, sr);
			}
		}

		//-- 2nd part: obtain ownership of this thingy then initialize it.
		if(sr.mustInitialize(this)) {
			boolean success = false;
			try {
				/*
				 * Ok; we own the component but hold no locks. Initialize the thingy.
				 */
				def.getBuildPlan().staticStart(this);
				success = true;
			} finally {
				sr.initCompleted(success);
			}
		}
	}

	static private void dump(ComponentDef cd, BuildPlan pl) {
		StringWriter	sw = new StringWriter(8192);
		IndentWriter	iw	= new IndentWriter(sw);
		try {
			pl.dump(iw);
			sw.close();
			System.out.println("DUMP: Build plan for component "+cd+", defined at "+cd.getDefinitionLocation());
			System.out.println(sw.getBuffer().toString());
		} catch(IOException x) {
			x.printStackTrace();
		}
	}
	@Override
	public String toString() {
		return "Container: "+getIdent();
	}

}
