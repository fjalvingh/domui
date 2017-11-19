/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.iocular.ioccontainer;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import to.etc.iocular.*;
import to.etc.iocular.def.*;
import to.etc.util.*;

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
	private boolean m_started;

	/** If this is a child container this refers to it's parent. It is null for a root container. */
	private final BasicContainer m_parent;

	/** The definition of this container. */
	private final ContainerDefinition m_def;

	/** The stack of containers that are parents of this one, indexed by ComponentRef.index. Used to quickly get a specific parent. */
	private final BasicContainer[] m_stack;

	private final Map<ComponentDef, StaticComponentRef> m_staticMap = new HashMap<ComponentDef, StaticComponentRef>();

	/** Map of all instantiated objects of a given definition */
	private final Map<ComponentDef, ContainerObjectRef> m_singletonMap = new HashMap<ComponentDef, ContainerObjectRef>();

	private enum RefState {
		NEW, OKAY, ALLOCATING, ERROR
	}

	private static class ContainerObjectRef {
		/** If an actual object has been created *and* it is valid this holds that object. */
		Object instance;

		/** If creating the object has caused a problem this holds that problem (wrapped if not a RuntimeException). */
		Exception exception;

		//		Container	allocatingContainer;

		RefState state;

		ContainerObjectRef(final ComponentDef cd) {
			//			m_def = cd;
			state = RefState.NEW;
		}
	}

	/**
	 * Keeps a reference to an object that was allocated and needs to be destroyed when the container is destroyed.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on May 22, 2009
	 */
	final private static class Destructor {
		private final Object m_instance;

		private final BuildPlan m_plan;

		public Destructor(final BuildPlan plan, final Object instance) {
			m_plan = plan;
			m_instance = instance;
		}

		public Object getInstance() {
			return m_instance;
		}

		public BuildPlan getPlan() {
			return m_plan;
		}
	}

	/**
	 * For each object that was created using a build plan which specified destructors, this contains
	 * a reference to the created object and it's build plan so it's destructors can be called at
	 * container close time.
	 */
	private List<Destructor> m_destructorList = Collections.EMPTY_LIST;

	public BasicContainer(final ContainerDefinition def, final Container parent) {
		if(def == null)
			throw new IllegalStateException("The definition for a container cannot be null of course");
		if(parent == null && def.getParentDefinition() != null)
			throw new IllegalStateException("This container's definition states that this container REQUIRES a parent container '" + def.getParentDefinition().getName() + "'");
		if(parent != null && def.getParentDefinition() == null)
			throw new IllegalStateException("This container's definition states that it has NO parent container, but one was passed anyway?");
		m_def = def;
		m_parent = (BasicContainer) parent;

		//-- Create the container stack
		m_stack = new BasicContainer[def.getContainerIndex() + 1];
		m_stack[def.getContainerIndex()] = this;
		BasicContainer c = (BasicContainer) parent;

		while(c != null) {
			m_stack[c.m_def.getContainerIndex()] = c;
			c = c.m_parent;
		}
	}

	@Override
	public synchronized void start() {
		if(m_started)
			throw new IllegalStateException(this + ": container has already been started!!");
		m_started = true;

	}

	@Override
	public void destroy() {
		List<Destructor> dlist;
		synchronized(this) {
			if(!m_started)
				return;
			m_started = false;
			if(m_destructorList.size() == 0)
				return;
			dlist = new ArrayList<Destructor>(m_destructorList); // Copy destructors,
			m_destructorList.clear();
			m_singletonMap.clear();
			m_staticMap.clear();
		}

		for(int i = dlist.size(); --i >= 0;) { // Destroy in reverse order of allocation
			Destructor d = dlist.get(i);
			System.out.println("Destroying: " + d.getInstance());
			d.getPlan().destroy(this, d.getInstance());
		}
	}

	public String getIdent() {
		return m_def.getName();
	}

	/**
	 * Gets a Ref for a given definition. If the ref does not yet
	 * exists it gets created atomically.
	 *
	 * @param cd
	 * @return
	 */
	private synchronized ContainerObjectRef getRef(final ComponentDef cd) {
		if(!m_started)
			throw new IllegalStateException(this + ": the container has been destroyed, or it has not yet been started.");
		ContainerObjectRef ref = m_singletonMap.get(cd);
		if(ref == null) {
			ref = new ContainerObjectRef(cd);
			m_singletonMap.put(cd, ref);
		}
		return ref;
	}

	@Override
	public <T> T findObject(final Class<T> theClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T findObject(final String name, final Class<T> theClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getObject(final Class<T> theClass) throws Exception {
		ComponentRef ref = m_def.findComponentReference(theClass);
		if(ref == null)
			throw new IocContainerException(this, "Can't create object of type=" + theClass.getName() + ": definition is not found");
		return (T) retrieve(ref);
	}

	@Override
	public <T> T getObject(final String name, final Class<T> theClass) throws Exception {
		ComponentRef ref = m_def.findComponentReference(name);
		if(ref == null)
			throw new IocContainerException(this, "Object with name=" + name + " not defined");
		return (T) retrieve(ref);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Container parameter assignment.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Set a container parameter object. The parameter to set is inferred from the object type.
	 * @param instance
	 */
	@Override
	public void setParameter(final Object instance) {
		if(instance == null)
			throw new IocContainerException(this, "You cannot set a parameter to null using this function, you must specify the parameter type or name!");

		//-- Find the type of the parameter being set; use base classes also.
		ComponentRef ref = null;
		Class< ? > current = instance.getClass();
		while(ref == null) {
			ref = m_def.findComponentReference(current); // Can we find a ref for this thingy?
			if(ref != null)
				break;

			//-- Move to super class
			current = current.getSuperclass();
			if(current == Object.class || current == null)
				throw new IocContainerException(this, "Undefined container parameter with type=" + instance.getClass() + " (or base class)");
		}

		assignParameter(ref, instance);
	}

	/**
	 * Set the parameter as identified by it's target class to the specified instance. This instance CAN
	 * be null, in which case null will be set into contructors and/or setters dependent on this parameter.
	 * @param clz
	 * @param instance
	 */
	@Override
	public void setParameter(final Class< ? > theClass, final Object instance) {
		ComponentRef ref = m_def.findComponentReference(theClass);
		if(ref == null)
			throw new IocContainerException(this, "Can't create object of type=" + theClass.getName() + ": definition is not found");
		assignParameter(ref, instance);
	}

	/**
	 * Sets the parameter with the specified name to the instance passed. This instance CAN
	 * be null, in which case null will be set into contructors and/or setters dependent on
	 * this parameter.
	 * @param name
	 * @param instance
	 */
	@Override
	public void setParameter(final String name, final Object instance) {
		ComponentRef ref = m_def.findComponentReference(name);
		if(ref == null)
			throw new IocContainerException(this, "Object with name=" + name + " not defined");
		assignParameter(ref, instance);
	}

	/**
	 * Tries to assign a value to the specified ref. This works for parameter types only. The
	 * instance is allowed to be null in which case the parameter IS set (but has the value null).
	 * @param ref
	 * @param instance
	 */
	private void assignParameter(final ComponentRef cref, final Object instance) {
		//-- Singleton object in this scope. Get a ref, then proceed to create
		ContainerObjectRef ref = getRef(cref.getDefinition());
		synchronized(ref) {
			switch(ref.state){
				default:
					throw new IllegalStateException("Unexpected state " + ref.state);
				case NEW:
					//-- Normal, new thingy. Assign, then be done;
					ref.instance = instance;
					ref.state = RefState.OKAY;
					return;

				case OKAY:
					throw new IocContainerException(this, "Attempt to re-assign the container parameter=" + cref.getDefinition());
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Retrieval and creation primitives.					*/
	/*--------------------------------------------------------------*/
	private synchronized void addDestructor(final BuildPlan plan, final Object inst) {
		if(m_destructorList == Collections.EMPTY_LIST)
			m_destructorList = new ArrayList<Destructor>();
		m_destructorList.add(new Destructor(plan, inst));
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
	public Object retrieve(final ComponentRef ref) throws Exception {
		return m_stack[ref.getContainerIndex()]._retrieve(ref);
	}

	/**
	 * Retrieve or create an actual instance of an object in *this* container. This is a threadsafe
	 * init/retrieve.
	 *
	 * @param <T>
	 * @param theClass
	 * @param ref
	 * @return
	 */
	private Object _retrieve(final ComponentRef cr) throws Exception {
		if(cr.getContainerIndex() != m_def.getContainerIndex())
			throw new IllegalStateException("Internal: Container index does not correspond with this container's index!?");
		handleOneTimeInit(cr.getDefinition()); // Handle any static factory initializations.

		//-- If this is a "prototype" (a class that is always created anew) then just create without lock
		if(cr.getDefinition().getScope() == BindingScope.PROTOTYPE) {
			Object inst = createObject(cr.getDefinition());
			if(cr.getBuildPlan().hasDestructors())
				addDestructor(cr.getBuildPlan(), inst);
			return inst;
		}

		/*
		 * Thread-safe init of singleton in ref. We obtain ownership of the ref, or wait until ownership has been
		 * released. If we have ownership we proceed to initialization.
		 */
		//-- Singleton object in this scope. Get a ref, then proceed to create
		ContainerObjectRef ref = getRef(cr.getDefinition());
		long ets = 0;
		outer : for(;;) {
			synchronized(ref) {
				switch(ref.state){
					default:
						throw new IllegalStateException("Internal: unexpected state " + ref.state);
					case OKAY:
						return ref.instance; // Value was set.
					case ERROR:
						if(ref.exception == null)
							throw new IllegalStateException("Missing exception type for object ref in ERROR state");
						throw ref.exception; // Retrow the init exception.

					case NEW:
						ref.state = RefState.ALLOCATING; // Mark as allocating for next thread
						//						ref.allocatingContainer = this;		// I'm allocating now
						break outer; // Exit synchronisation.
					case ALLOCATING:
						/*
						 * We need to block IF the max time has not been exceeded.
						 */
						long ts = System.currentTimeMillis();
						if(ets == 0) // First time entered?
							ets = ts + 60 * 1000; // Wait max. 1 minute
						else if(ts >= ets)
							throw new IocContainerException(this, "Another thread took too long to initialize an instance of component=" + cr.getDefinition());

						//-- We need to wait....
						ref.wait(10 * 1000);
						break; // And loop again, till timeout or state change.
				}
			}
		}

		/*
		 * We get here ONLY when a new object is to be allocated. We currently 'own' the object reference (it's state is ALLOCATING) and
		 * any other thread using it *will* wait for me to complete. We are outside of the lock to prevent uncontrollable deadlock. This
		 * code asks the definition to create an instance; if the definition defines a parameter it causes a "parameter not set" exception
		 * since parameters cannot be created (only set).
		 */
		Exception thex = null;
		RefState nstate = RefState.ERROR;
		Object theObject = null;
		try {
			theObject = createObject(cr.getDefinition()); // Create a new instance
			nstate = RefState.OKAY; // Ref is assigned, now.

			//-- Link in destructor if needed.
			if(cr.getBuildPlan().hasDestructors())
				addDestructor(cr.getBuildPlan(), theObject);

			return theObject;
		} catch(Exception x) {
			thex = x;
			nstate = RefState.ERROR;
			throw x;
		} finally {
			/*
			 * Always assign result in finally to prevent always-locked objects.
			 */
			synchronized(ref) {
				ref.state = nstate;
				ref.instance = theObject;
				ref.exception = thex;
				ref.notify();
			}
		}
	}

	/**
	 * Unlocked create of an object, using the current container stack.
	 * @param cd
	 * @return
	 */
	private Object createObject(final ComponentDef cd) throws Exception {
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
	private void handleOneTimeInit(final ComponentDef def) throws Exception {
		BuildPlan pl = def.getBuildPlan();
		if(!pl.needsStaticInitialization())
			return;

		//-- Check to see if init has been done, using 2-part locking. First get thingy ref atomically then own it,
		StaticComponentRef sr;
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

	static private void dump(final ComponentDef cd, final BuildPlan pl) {
		StringWriter sw = new StringWriter(8192);
		IndentWriter iw = new IndentWriter(sw);
		try {
			pl.dump(iw);
			sw.close();
			System.out.println("DUMP: Build plan for component " + cd + ", defined at " + cd.getDefinitionLocation());
			System.out.println(sw.getBuffer().toString());
		} catch(IOException x) {
			x.printStackTrace();
		}
	}

	public void dump(final Class< ? > theClass) {
		ComponentRef ref = m_def.findComponentReference(theClass);
		if(ref == null)
			throw new IocContainerException(this, "Can't dump object of type=" + theClass.getName() + ": definition is not found");
		dump(ref.getDefinition(), ref.getBuildPlan());
	}

	@Override
	public String toString() {
		return "Container: " + getIdent();
	}
}
