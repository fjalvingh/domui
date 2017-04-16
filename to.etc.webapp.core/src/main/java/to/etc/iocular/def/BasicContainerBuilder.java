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
package to.etc.iocular.def;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import org.slf4j.*;

import to.etc.iocular.*;

/**
 * This allows creation of a container definition from within Java source code. When building
 * is complete a ContainerDefinition is created by calling createDefinition(). At that time all
 * of the data entered in the builder is checked for consistency, build plans are created for
 * all defined components and if all goes well a definition gets returned.</p>
 *
 * @author jal
 * Created on Apr 3, 2007
 */
public class BasicContainerBuilder implements Builder {
	static private final Logger LOG = LoggerFactory.getLogger(BasicContainerBuilder.class);

	/** The created definition from this build. When set the builder has completed. */
	private ContainerDefinition m_myDefinition;

	/** The name of the container that is being created. */
	private final String m_name;

	private int m_containerIndex;

	/** If this def is for a child of a parent container this contains the definition of the parent container. */
	private final ContainerDefinition m_parentDefinition;

	/** If this definition extends another definition this refers to the "base" definition. */
	private final ContainerDefinition m_baseDefinition;

	private final List<ComponentBuilder> m_builderList = new ArrayList<ComponentBuilder>();

	private final Map<String, ComponentBuilder> m_namedComponentMap = new HashMap<String, ComponentBuilder>();

	private final Map<Class< ? >, ComponentBuilder> m_definedTypeMap = new HashMap<Class< ? >, ComponentBuilder>();

	private final Map<Class< ? >, List<ComponentBuilder>> m_availableTypeMap = new HashMap<Class< ? >, List<ComponentBuilder>>();

	/**
	 * Maps interfaces to implementation classes.
	 */
	private final Map<Class< ? >, List<Class< ? >>> m_implementationMap = new HashMap<Class< ? >, List<Class< ? >>>();

	private BasicContainerBuilder(ContainerDefinition parent, final ContainerDefinition base, final String name) {
		m_name = name;
		m_parentDefinition = parent;
		m_baseDefinition = base;
		while(parent != null) {
			m_containerIndex++;
			parent = parent.getParentDefinition();
		}
	}

	/**
	 * Create a "normal" builder for a container that has no parent container
	 * and no base container (does not inherit).
	 *
	 * @param name
	 * @return
	 */
	static public BasicContainerBuilder createBuilder(final String name) {
		return new BasicContainerBuilder(null, null, name);
	}

	/**
	 * Create a builder for a container that is a child container of a parent
	 * container.
	 *
	 * @param parent
	 * @param name
	 * @return
	 */
	static public BasicContainerBuilder createChildBuilder(final ContainerDefinition parent, final String name) {
		if(parent == null)
			throw new IllegalArgumentException("The 'parent' cannot be null for a child container");
		return new BasicContainerBuilder(parent, null, name);
	}

	/**
	 * Create an inherited container.
	 *
	 * @param base
	 * @param name
	 * @return
	 */
	static public BasicContainerBuilder createInheritedBuilder(final ContainerDefinition base, final String name) {
		if(base == null)
			throw new IllegalArgumentException("The 'parent' cannot be null for an inherited container");
		return new BasicContainerBuilder(null, base, name);
	}

	private void check() {
		if(m_myDefinition != null)
			throw new IllegalStateException("The containerDefinition has already been created from this builder. Create a new builder to create a new definition.");
	}

	void addComponentName(final ComponentBuilder cb, final String name) {
		check();
		ComponentBuilder t = m_namedComponentMap.get(name);
		if(t != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Duplicate name '");
			sb.append(name);
			sb.append("' for the object *now* defined at\n");
			sb.append(cb.getDefinitionLocation());
			sb.append("\nThe earlier object was defined at\n");
			sb.append(t.getDefinitionLocation());
			throw new IocConfigurationException(null, sb.toString());
		}
		m_namedComponentMap.put(name, cb);
	}

	/**
	 * Defines an interface's implementation class.
	 *
	 * @see to.etc.iocular.Builder#bind(java.lang.Class, java.lang.Class)
	 */
	@Override
	public <T> void bind(final Class<T> intf, final Class<T> impl) {
		if(!intf.isInterface())
			throw new IllegalStateException("The class " + intf + " is not an interface.");
		if(impl.isInterface())
			throw new IllegalStateException("The implementation class " + impl + " cannot be an interface");
		int mod = impl.getModifiers();
		if(Modifier.isAbstract(mod))
			throw new IllegalStateException("The implementation class " + impl + " cannot be abstract");
		if(!Modifier.isPublic(mod))
			throw new IllegalStateException("The implementation class " + impl + " must be public");
		List<Class< ? >> list = m_implementationMap.get(intf);
		if(list == null) {
			list = new ArrayList<Class< ? >>();
			m_implementationMap.put(intf, list);
		}
		list.add(impl);
	}

	private ComponentBuilder makeBuilder() {
		String loc = null;
		try {
			throw new Exception("duh");
		} catch(Exception x) {
			StackTraceElement[] ar = x.getStackTrace();
			if(ar == null || ar.length < 3)
				loc = "(unknown location)";
			else
				loc = ar[2].toString();
		}
		return new ComponentBuilder(this, loc);
	}

	static public String getLocationString(final int stackoffset) {
		try {
			throw new Exception("duh");
		} catch(Exception x) {
			StackTraceElement[] ar = x.getStackTrace();
			if(ar == null || ar.length <= stackoffset)
				return "(unknown location)";
			return ar[stackoffset].toString();
		}
	}

	@Override
	public ComponentBuilder register() {
		check();
		ComponentBuilder c = makeBuilder();
		m_builderList.add(c);
		return c;
	}

	/**
	 * Register an instance of an object. This also registers the class into
	 * the per-class data tables.
	 *
	 * @see to.etc.iocular.Builder#registerInstance(java.lang.Object)
	 */
	public ComponentBuilder registerInstance(final Object inst) {
		check();
		if(inst == null)
			throw new IllegalStateException("Instance cannot be null");
		ComponentBuilder c = makeBuilder();

		return c;
	}

	public ComponentBuilder registerInstance(final String name, final Object inst) {
		if(inst == null)
			throw new IllegalStateException("Instance cannot be null");
		ComponentBuilder c = makeBuilder();

		return c;
	}

	/**
	 * Registers a type as "available" in the available type map.
	 *
	 * @param clz
	 * @param cb
	 */
	private void registerAvailableType(final Class< ? > clz, final ComponentBuilder cb) {
		check();
		List<ComponentBuilder> list = m_availableTypeMap.get(clz);
		if(list == null) {
			list = new ArrayList<ComponentBuilder>();
			m_availableTypeMap.put(clz, list);
		}
		if(!list.contains(cb))
			list.add(cb);
		//		System.out.println("-- added InferredType="+clz+" using "+cb);
	}

	private void registerDefinedType(final Class< ? > type, final ComponentBuilder cb) {
		check();
		if(type == Object.class)
			return;
		ComponentBuilder t = m_definedTypeMap.put(type, cb);
		if(t != null)
			throw new IocConfigurationException(cb, "Duplicate definition of implemented type " + type + "; the other definition was in " + t);
		//		System.out.println("-- added DefinedType="+type+" using "+cb);
	}

	private void registerBaseClasses(Class< ? > clz, final ComponentBuilder cb) {
		for(;;) {
			clz = clz.getSuperclass();
			if(clz == null || clz == Object.class)
				return;
			registerAvailableType(clz, cb);
		}
	}

	private void registerInterfaces(final Class< ? > clz, final ComponentBuilder cb) {
		Class< ? >[] ar = clz.getInterfaces();
		for(Class< ? > clif : ar) {
			registerAvailableType(clif, cb);
		}
	}

	/**
	 * While building, this scavenges the entire builder database to find
	 * the named type and determine it's type.
	 *
	 * @param name
	 * @return
	 */
	Class< ? > calcTypeByName(final Stack<ComponentBuilder> stack, final String name) {
		//-- 1. First try the 'current' container,
		ComponentBuilder cb = m_namedComponentMap.get(name);
		if(cb != null) {
			//-- Got the thingy from this container; get it's type;
			return cb.calculateType(stack);
		}

		//-- 2. Try any parent.
		ComponentRef cd = m_baseDefinition.findComponentReference(name);
		if(cd != null)
			return cd.getDefinition().getActualClass();
		cd = m_parentDefinition.findComponentReference(name);
		if(cd != null)
			return cd.getDefinition().getActualClass();
		return null;
	}

	int getContainerIndex() {
		return m_containerIndex;
	}

	public String getName() {
		return m_name;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	"Compile" the build code into a completed def		*/
	/*--------------------------------------------------------------*/
	/**
	 * Walk the completed data and build a ContainerDefinition.
	 * @see to.etc.iocular.Builder#createDefinition()
	 */
	@Override
	public ContainerDefinition createDefinition() {
		if(m_myDefinition != null)
			return m_myDefinition;

		//-- Calculate all types for all components here,
		Stack<ComponentBuilder> stack = new Stack<ComponentBuilder>();
		for(ComponentBuilder cb : m_builderList) {
			stack.clear();
			cb.calculateType(stack);
		}

		//-- Register all types
		for(ComponentBuilder cb : m_builderList) {
			List<Class< ? >> defl = cb.getDefinedTypes();
			if(defl.size() > 0) {
				//-- Register these types in the defined type table;
				for(Class< ? > cl : defl)
					registerDefinedType(cl, cb);
			} else {
				//-- If the defined type list is empty register the actual type as the defined type;
				registerDefinedType(cb.getActualClass(), cb);
				cb.getDefinedTypes().add(cb.getActualClass());

				//-- In addition, register all of the implemented interfaces and base classes as available types;
				registerInterfaces(cb.getActualClass(), cb);
				registerBaseClasses(cb.getActualClass(), cb);
			}
		}

		/*
		 * At this point all names and all available types are known. We now are able to
		 * *find* all components that are available, so we can start to construct build
		 * plans.
		 */
		Map<Class< ? >, ComponentRef> defmap = new HashMap<Class< ? >, ComponentRef>(); // Defined type map
		Map<Class< ? >, ComponentRef> actmap = new HashMap<Class< ? >, ComponentRef>(); // Inferred type map
		Map<String, ComponentRef> namedmap = new HashMap<String, ComponentRef>(); // Named thingy map.
		for(ComponentBuilder cb : m_builderList) {
			stack.clear();
			ComponentRef ref = cb.calculateComponentRef(stack);

			//-- Add names and the refs;
			for(String name : cb.getNameList()) {
				namedmap.put(name, ref);
			}

			//-- Add all defined classes
			for(Class< ? > cl : cb.getDefinedTypes()) {
				defmap.put(cl, ref);
			}
		}

		//-- Add all inferred types provided they are unique
		for(Class< ? > cl : m_availableTypeMap.keySet()) {
			List<ComponentBuilder> list = m_availableTypeMap.get(cl); // Get the list of thingies in here,
			if(list.size() == 1) {
				//-- Exactly one provider of this type: add as available (inferred) type
				actmap.put(cl, list.get(0).calculateComponentRef(null));
			}
		}

		//-- And last but not least: create the definition.
		m_myDefinition = new ContainerDefinition(m_name, m_baseDefinition, m_parentDefinition, namedmap, defmap, actmap, m_containerIndex);
		return m_myDefinition;
	}

	ComponentRef findReferenceFor(final ISelfDef self, final Stack<ComponentBuilder> stack, final Class< ? > ptype, final Annotation[] annar, final MethodParameterSpec def) {
		ComponentRef ref = internalFindReferenceFor(self, stack, ptype, annar, def); // Basic lookup;
		if(ref == null)
			return null;

		if(ptype != null) {
			//-- Make sure the returned type can be assigned to the parameter type passed,
			if(!ptype.isAssignableFrom(ref.getDefinition().getActualClass())) // Do not allow
				return null;

		}
		return ref;
	}

	/**
	 * Recursive entrypoint to get a reference. This first tries to get a reference using the
	 * <i>defined</i> classes and components in the current set, the base and finally the parent. If
	 * this fails it tries to retrieve a reference using an inferred definition in the same order.
	 * @param self
	 *
	 * @param stack
	 * @param ptype
	 * @param annar
	 * @param def
	 * @return
	 */
	ComponentRef internalFindReferenceFor(final ISelfDef self, final Stack<ComponentBuilder> stack, final Class< ? > ptype, final Annotation[] annar, final MethodParameterSpec def) {
		if(def != null && def.isSelf()) {
			//-- Self reference. Create a synthetic reference which does contain a proper type.
			if(self == null)
				throw new IllegalStateException("Internal: self reference requested but 'self' is not known...");
			return new ComponentRef(self); // SELF reference,
		}

		//-- 1. Try to find the thingy in here,
		ComponentRef ref = _findDefinedReference(stack, ptype, annar, def);
		if(ref != null)
			return ref;

		//-- Ask my base;
		if(m_baseDefinition != null) {
			ref = m_baseDefinition.findDefinedReference(ptype, annar, def);
			if(ref != null)
				return ref;
		}

		//-- Ask my parent. If succesful this will return a reference to an object in a higher container.
		if(m_parentDefinition != null) {
			ref = m_parentDefinition.findDefinedReference(ptype, annar, def);
			if(ref != null)
				return ref;
		}

		//-- Try inferred types.
		ref = _findInferredReference(stack, ptype, annar, def);
		if(ref != null)
			return ref;

		//-- Ask my base;
		if(m_baseDefinition != null) {
			ref = m_baseDefinition.findInferredReference(ptype, annar, def);
			if(ref != null)
				return ref;
		}

		//-- Ask my parent. If succesful this will return a reference to an object in a higher container.
		if(m_parentDefinition != null) {
			ref = m_parentDefinition.findInferredReference(ptype, annar, def);
			if(ref != null)
				return ref;
		}
		return null; // Not found.
	}


	/**
	 * Local workhorse to decode the parameters and get the appropriate object
	 * to fulfill the specified reference.
	 *
	 * @param stack
	 * @param ptype
	 * @param annar
	 * @param def
	 * @return
	 */
	private ComponentRef _findDefinedReference(final Stack<ComponentBuilder> stack, final Class< ? > ptype, final Annotation[] annar, final MethodParameterSpec def) {
		//-- Try to find a ComponentBuilder that is able to provide this thingy.
		/*
		 * TODO First try to find something using the provided parameters and annotations.
		 */

		//-- No parameters provided- try to locate using the type of the type using the defined type table
		ComponentBuilder cb = m_definedTypeMap.get(ptype); // Find anything that can provide this
		if(cb != null) {
			//-- Recurse into this-comnponent's ref thinger. This will cause a circular reference exception if things refer back
			return cb.calculateComponentRef(stack);
		}
		return null;
	}

	/**
	 * Tries to find a value using the inferred types of the defined components in the definition that
	 * is currently being built.
	 *
	 * @param stack
	 * @param ptype
	 * @param annar
	 * @param def
	 * @return
	 */
	private ComponentRef _findInferredReference(final Stack<ComponentBuilder> stack, final Class< ? > ptype, final Annotation[] annar, final MethodParameterSpec def) {
		//-- Try to find a ComponentBuilder that is able to provide this thingy.
		/*
		 * TODO First try to find something using the provided parameters and annotations.
		 */


		//-- No parameters provided- try to locate using the type of the type using the defined type table
		List<ComponentBuilder> list = m_availableTypeMap.get(ptype);
		if(list == null)
			return null;
		else if(list.size() != 1) {
			LOG.info("Multiple inferred factories for parameter type=" + ptype);
			return null;
		}
		ComponentBuilder cb = list.get(0);
		//-- Recurse into this-comnponent's ref thinger. This will cause a circular reference exception if things refer back
		return cb.calculateComponentRef(stack);
	}

	/**
	 * Return a reference for a property setter.
	 * FIXME URGENT Needs proper implementation using the data in the property def, like component name, type etc.
	 * @param stack
	 * @param pd
	 * @return
	 */
	public ComponentRef findReferenceFor(final Stack<ComponentBuilder> stack, final ComponentPropertyDef pd) {
		return findReferenceFor(null, stack, pd.getInfo().getGetter().getReturnType(), null, null);
	}
}
