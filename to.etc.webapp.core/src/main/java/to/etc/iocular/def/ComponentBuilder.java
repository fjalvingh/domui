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

import to.etc.iocular.*;
import to.etc.iocular.ioccontainer.*;
import to.etc.iocular.util.ClassUtil;
import to.etc.util.*;

/**
 * Thingy which helps with building a component definition. This contains all definition-time data
 * related to a single component object.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 27, 2007
 */
public class ComponentBuilder {
	/** The container definition we're building. */
	private final BasicContainerBuilder m_builder;

	/** The location, as a string, of this definition in the source for the definition. For java-defined code this is a line from the stacktrace. */
	private final String m_definitionLocation;

	/** The basic creation method for this object. This defines the base for how we get the actual instance. A create method must ALWAYS be present. */
	private CreateMethod m_createMethod;

	/** The assigned names for this component. Can be empty. */
	private final List<String> m_nameList = new ArrayList<String>();

	/**
	 * If this class is registered as a "defined" type only that type will be registered in the type table.
	 */
	private final List<Class< ? >> m_definedTypeList = new ArrayList<Class< ? >>();

	private Class< ? > m_baseClass;

	private Class< ? > m_factoryClass;

	/** The alternative instances of methods providing the object using the static factoryClass */
	private List<Method> m_factoryMethodList;

	/** When a factory instance has the method to call this contains the key for the factory instance. */
	private String m_factoryInstance;

	private String m_factoryMethodText;

	private BindingScope m_scope;

	//	private boolean					m_autowire;

	private String m_creationString;

	/**
	 * The actual type created by this definition.
	 */
	private Class< ? > m_actualType;

	private MethodCallBuilder m_currentMethodBuilder;

	private final List<MethodCallBuilder> m_factoryStartList = new ArrayList<MethodCallBuilder>();

	private final List<MethodCallBuilder> m_startList = new ArrayList<MethodCallBuilder>();

	private final List<MethodCallBuilder> m_destroyList = new ArrayList<MethodCallBuilder>();

	ComponentBuilder(final BasicContainerBuilder b, final String loc) {
		m_builder = b;
		m_definitionLocation = loc;
	}

	public String getDefinitionLocation() {
		return m_definitionLocation;
	}

	public BasicContainerBuilder getBuilder() {
		return m_builder;
	}

	MethodCallBuilder getCurrentMethodBuilder() {
		return m_currentMethodBuilder;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Builder construction methods.						*/
	/*--------------------------------------------------------------*/
	//	private void checkCreation() {
	//		if(m_createMethod != null)
	//			throw new IocConfigurationException(this.m_builder, getDefinitionLocation(), "Component already created by "+m_creationString);
	//	}

	private void setCreateMethod(final CreateMethod m, final String detailed) {
		if(m_createMethod != null)
			throw new IocConfigurationException(this.m_builder, getDefinitionLocation(), "Component already created by " + m_creationString);
		m_createMethod = m;
		m_creationString = detailed;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Creator methods.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Create the specified class using it's constructor, followed by setter injection where
	 * needed. This defines a base creation method and so it forbids the other creation methods.
	 */
	public ComponentBuilder type(final Class< ? > clz) {
		setCreateMethod(CreateMethod.ASNEW, "Creating a new class instance using <<new>>");

		//-- Check to see if the class is acceptable
		int mod = clz.getModifiers();
		if(Modifier.isAbstract(mod))
			throw new IocConfigurationException(this, this + ": the class " + clz + " is abstract");
		if(!Modifier.isPublic(mod))
			throw new IocConfigurationException(this, this + ": the class " + clz + " is not public");
		if(clz.isInterface())
			throw new IocConfigurationException(this, this + ": the class " + clz + " is an interface");

		Constructor< ? >[] car = clz.getConstructors();
		if(car == null || car.length == 0) // Cannot construct
			throw new IocConfigurationException(this, this + ": the class " + clz + " has no public constructors");

		//-- Define a type plan, and register it; This is a constructor-defined type...
		m_actualType = clz;
		m_baseClass = clz;
		m_definedTypeList.add(clz);
		return this;
	}

	/**
	 * Create the specified class by getting a parameter that is set, in runtime, when the
	 * container is constructed. This defines the type of the parameter; it can be augmented
	 * by a name. The parameter is assumed to be present at runtime. If it is not set the
	 * runtime code will abort as soon as the parameter is needed to fulfil a build plan. If
	 * a parameter is really unknown at runtime this can be prevented by explicitly assigning
	 * null to the container's parameter.
	 *
	 * @param ptype
	 * @return
	 */
	public ComponentBuilder parameter(final Class< ? > ptype) {
		setCreateMethod(CreateMethod.CONTAINER_PARAMETER, "Passed as a parameter by the container's builder");
		m_actualType = ptype;
		m_definedTypeList.add(ptype);
		return this;
	}

	/**
	 * Create a build plan for a parameter-based object. The object is passed as a parameter at
	 * container runtime.
	 *
	 * @param stack
	 * @return
	 */
	private BuildPlan createParameterBuildPlan(final Stack<ComponentBuilder> stack) {
		return new BuildPlanForContainerParameter(m_actualType, m_nameList);
	}

	/**
	 * <p>A basic object builder defining an object to be returned from a
	 * static factory method on a class. The method passed must be
	 * resolvable to a static method on the class passed, and it's
	 * parameters must be fillable from the container. After this call
	 * we'll have a "method" current so the calls to set method parameters
	 * work and will define parameters for this method.</p>
	 *
	 * <p>This method defines creation so it throws up if another creation method
	 * is already defined.</p>
	 *
	 * @param clz
	 * @param method
	 * @return
	 */
	public ComponentBuilder factory(final Class< ? > clz, final String method) {
		setCreateMethod(CreateMethod.FACTORY_METHOD, "calling factory method " + method + " on class " + clz);
		m_factoryMethodList = findMethodInFactory(clz, method, true);
		m_factoryClass = clz;
		m_actualType = m_factoryMethodList.get(0).getReturnType();
		return this;
	}

	private List<Method> findMethodInFactory(final Class< ? > clz, final String method, final boolean mbstatic) {
		Method[] mar = ClassUtil.findMethod(clz, method);
		if(mar.length == 0)
			throw new IocConfigurationException(this, "Method " + method + " is not defined in class '" + clz + "'");
		List<Method> thelist = new ArrayList<Method>();
		Class< ? > rtype = null;
		for(int i = mar.length; --i >= 0;) {
			Method m = mar[i];

			int mod = m.getModifiers();
			if(mbstatic && !Modifier.isStatic(mod))
				continue;
			if(!Modifier.isPublic(mod))
				continue;
			Class< ? > c = m.getReturnType();
			if(c == Void.TYPE)
				continue;
			if(c.isPrimitive())
				continue;
			if(rtype == null)
				rtype = c;
			else if(rtype != c)
				throw new IocConfigurationException(this, "The " + mar.length + " different overloads of the method " + method + " return different types");
			thelist.add(m);
		}
		if(rtype == null)
			throw new IocConfigurationException(this, "None of the " + mar.length + " versions of the method " + method + " is usable as a" + (mbstatic ? "static " : "")
				+ " public factory method returning an object");
		return thelist;
	}

	/**
	 * A basic object builder defining an object to be returned from a container object identified
	 * by a name, by calling a method on that object.
	 *
	 * @param id
	 * @param method
	 * @return
	 */
	public ComponentBuilder factory(final String id, final String method) {
		setCreateMethod(CreateMethod.FACTORY_METHOD, "calling factory method " + method + " on object reference " + id);
		m_factoryInstance = id;
		m_factoryMethodText = method;
		return this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Modifiers and specifiers.							*/
	/*--------------------------------------------------------------*/
	/**
	 * When called this adds a name for the component. A single component can have more than
	 * one name, but the name must be unique within the container it gets stored in.
	 * @param name
	 */
	public ComponentBuilder name(final String name) {
		m_builder.addComponentName(this, name); // Register another name with the configuration; throws up when duplicate
		m_nameList.add(name);
		return this;
	}

	/**
	 * Defines the scope for this object. This defaults to "SINGLETON"
	 * @param scope
	 * @return
	 */
	public ComponentBuilder scope(final BindingScope scope) {
		m_scope = scope;
		return this;
	}

	/**
	 * Define a "close" or "discard" method <i>on another class</i> for a given object. When added, this adds a method on some
	 * class which gets called when the object is being destroyed at container destroy time. Objects are
	 * destroyed in the reverse order of creation. This pertains to the actual object (the result of
	 * a getObject call). The method being called must have that object or one of it's base classes as
	 * parameter.
	 * The method passed <b>must</b> be a static method on the specified class currently.
	 *
	 * @param wh
	 * @param what
	 * @return
	 */
	public ComponentBuilder destroy(final Class< ? > wh, final String methodName) {
		MethodCallBuilder mcb = new MethodCallBuilder(this, wh, methodName);
		m_currentMethodBuilder = mcb;

		//-- At least one of the parameters *must* be the object we've just constructed.
		mcb.setParameterSelf(0); // Parameter 0 must be me.
		m_destroyList.add(mcb);
		return this;
	}

	/**
	 * Define a "close" or "discard" method on the <i>instance</i> that was created. The method must
	 * be a non-static method without arguments.
	 *
	 * @param methodName
	 * @return
	 */
	public ComponentBuilder destroy(final String methodName) {
		MethodCallBuilder mcb = new MethodCallBuilder(this, m_actualType, methodName);
		m_currentMethodBuilder = mcb;

		//-- At least one of the parameters *must* be the object we've just constructed.
		mcb.setThisIsSelf();
		//		mcb.setParameterSelf(0);						// Parameter 0 must be me.
		m_destroyList.add(mcb);
		return this;
	}

	/**
	 * Define an explicit type for this class. This overrides the "actual" type as found by the
	 * creation method. Typical use is to define that a creation method should be seen as
	 * returning the specified interface, not the actual object type. If the created object
	 * implements multiple interfaces you can call this method multiple times, for each
	 * interface supported.
	 * When used the actual class returned by the creation method <i>must</i> implement this or
	 * have this as a base class.
	 *
	 * @param clz
	 * @return
	 */
	public ComponentBuilder implement(final Class< ? > clz) {
		m_definedTypeList.add(clz);
		return this;
	}

	/**
	 * Only used for static factories, this allows you to call a static method on the
	 * container class itself to get it to initialize.
	 *
	 * @param methodName
	 * @param arguments
	 * @return
	 */
	public ComponentBuilder factoryStart(final String methodName, final Class< ? >... arguments) {
		//-- Make sure we're a static factory thingy.
		if(m_factoryClass == null)
			throw new IocConfigurationException(this, "factoryStart() can only be used for static factory classes.");
		MethodCallBuilder mcb = new MethodCallBuilder(this, m_factoryClass, methodName, arguments, true);
		m_currentMethodBuilder = mcb;
		m_factoryStartList.add(mcb);
		return this;
	}

	/**
	 * Only used for static factories, this allows you to call a static method on whatever
	 * static class to get it to initialize.
	 *
	 * @param methodName
	 * @param arguments
	 * @return
	 */
	public ComponentBuilder factoryStart(final Class< ? > clz, final String methodName, final Class< ? >... arguments) {
		//-- Make sure we're a static factory thingy.
		if(m_factoryClass == null)
			throw new IocConfigurationException(this, "factoryStart() can only be used for static factory classes.");
		MethodCallBuilder mcb = new MethodCallBuilder(this, clz, methodName, arguments, true);
		m_currentMethodBuilder = mcb;
		m_factoryStartList.add(mcb);
		return this;
	}


	/**
	 * Add a start method to an object being retrieved.
	 *
	 * @param methodName
	 * @param arguments
	 * @return
	 */
	public ComponentBuilder start(final String methodName, final Class< ? >... arguments) {
		//-- Make sure we're a static factory thingy.
		if(m_factoryClass == null)
			throw new IocConfigurationException(this, "factoryStart() can only be used for static factory classes.");
		MethodCallBuilder mcb = new MethodCallBuilder(this, m_factoryClass, methodName, arguments, false);
		m_currentMethodBuilder = mcb;
		m_startList.add(mcb);
		return this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Property wiring definition.							*/
	/*--------------------------------------------------------------*/
	/** The mode to use for all properties not explicitly mentioned. */
	private ComponentPropertyMode m_propertyMode = ComponentPropertyMode.NONE;

	/** All properties that were explicitly named with a configuration. */
	private final Map<String, ComponentPropertyDef> m_propertyDefMap = new HashMap<String, ComponentPropertyDef>();

	/**
	 * This defines that <b>all</b> properties on the instance must be set; it aborts if it cannot
	 * find a proper instance for a given property. This does skip all properties that refer to dumb
	 * classes like all primitives and wrappers and String.
	 *
	 * @return
	 */
	public ComponentBuilder setAllProperties() {
		if(m_propertyMode != ComponentPropertyMode.NONE)
			throw new IocConfigurationException(this, "Property configuration mode is already set to " + m_propertyMode);
		m_propertyMode = ComponentPropertyMode.ALL;
		return this;
	}

	/**
	 * This sets all properties on this components for which a value can be determined. It skips properties
	 * with silly types like all primitives and wrappers and String. Properties for which no bound can be
	 * found in the container set are not set (they are explicitly not set to null).
	 * @return
	 */
	public ComponentBuilder setKnownProperties() {
		if(m_propertyMode != ComponentPropertyMode.NONE)
			throw new IocConfigurationException(this, "Property configuration mode is already set to " + m_propertyMode);
		m_propertyMode = ComponentPropertyMode.KNOWN;
		return this;
	}

	/**
	 * Make a property setter definition. Abort if such a definition is already known.
	 * @param name
	 * @return
	 */
	private ComponentPropertyDef uniquePropertyDef(final String name) {
		ComponentPropertyDef pd = m_propertyDefMap.get(name);
		if(pd != null)
			throw new IocConfigurationException(this, "An initialization for the property '" + name + "' has already been set.");
		pd = new ComponentPropertyDef(this, name); // This leaves all other thingies empty, denoting a default init.
		m_propertyDefMap.put(name, pd);
		return pd;
	}

	/**
	 * Define a set of properties on this component that must be set using default
	 * wiring for the properties. Each property is set by retrieving it's instance
	 * from the container set. This works for uniquely-typed properties only.
	 *
	 * @param names
	 * @return
	 */
	public ComponentBuilder setProperties(final String... names) {
		for(String name : names) {
			uniquePropertyDef(name).setRequired(true);
		}
		return this;
	}

	/**
	 * Inject the specified property with the component with the given name. This requires that another
	 * component has a name and a type compatibe with the specified type.
	 *
	 * @param name
	 * @param componentId
	 * @return
	 */
	public ComponentBuilder setProperty(final String name, final String componentId) {
		ComponentPropertyDef pd = uniquePropertyDef(name);
		pd.setSourceName(componentId);
		pd.setRequired(true);
		return this;
	}

	/**
	 * Inject the specified property with the component registered with the specified class. This is usually the
	 * same as using setProperties() using only the property name, since the property's type should be
	 * compatible with the component's type.
	 *
	 * @param name
	 * @param componentClass
	 * @return
	 */
	public ComponentBuilder setProperty(final String name, final Class< ? > componentClass) {
		ComponentPropertyDef pd = uniquePropertyDef(name);
		pd.setSourceClass(componentClass);
		pd.setRequired(true);
		return this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Data getters.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the list of registered names for this object.
	 * @return
	 */
	List<String> getNameList() {
		return m_nameList;
	}

	public String getIdent() {
		if(m_nameList.size() > 0)
			return "component(name=" + m_nameList.get(0) + ")";
		if(m_definedTypeList.size() > 0)
			return "component(type=" + m_definedTypeList.get(0).toString() + ")";
		return "component(Unnamed/untyped)";
	}

	@Override
	public String toString() {
		if(m_nameList.size() > 0)
			return "component(name=" + m_nameList.get(0) + ") defined at " + m_definitionLocation;
		if(m_definedTypeList.size() > 0)
			return "component(type=" + m_definedTypeList.get(0).toString() + ") defined at " + m_definitionLocation;
		return "component(Unnamed/untyped) defined at " + m_definitionLocation;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Type registration.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Called early when the build is complete, this registers all of the
	 * types provided by this component. For "easy" objects like class and
	 * static factories this merely defines the created type and all
	 * implemented interfaces and such. For factories that use some kind
	 * of container reference with a method this determines the type of
	 * object returned by decoding the reference and the method.
	 */
	//	void registerTypes() {
	//		if(m_definedTypeList.size() > 0) {
	//			//-- Register as "
	//
	//		}
	//	}


	/**
	 * Calculates the actual type for this definition. This can get
	 * called recursively when ref factories use other ref factories, in which
	 * case a circular ref can occur. We test for this using the call stack.
	 *
	 * @param stack
	 * @return
	 */
	Class< ? > calculateType(final Stack<ComponentBuilder> stack) {
		if(m_actualType != null)
			return m_actualType;

		//-- Type as-of-yet undefined. Handle late-typing possibilities
		if(m_factoryInstance != null) {
			if(m_factoryMethodText == null)
				throw new IocConfigurationException(this, "Missing method specification on a 'ref' factory");

			//-- We must be able to at least get a 'type' for the ID, from whatever container.
			if(stack.contains(this)) {
				//-- Recursive definition....
				throw new IocConfigurationException(this, "Circular reference to " + this);
			}
			stack.push(this);
			Class< ? > clz = m_builder.calcTypeByName(stack, m_factoryInstance);
			if(this != stack.pop())
				throw new IllegalStateException("Stack inbalance!?");
			if(clz == null)
				throw new IocConfigurationException(this, "The component with id='" + m_factoryInstance + "' is not known");

			//-- The 'factory' type is known; now retrieve the method;
			m_factoryMethodList = findMethodInFactory(clz, m_factoryMethodText, false);
			m_actualType = m_factoryMethodList.get(0).getReturnType();
			return m_actualType;
		} else
			throw new IocConfigurationException(this, "Can't determine the 'type' of this component: no factory defined.");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Builder get info methods.							*/
	/*--------------------------------------------------------------*/
	private ComponentRef m_ref;

	ComponentRef calculateComponentRef(final Stack<ComponentBuilder> stack) {
		if(m_ref != null)
			return m_ref;

		//-- We need to build this. Are we not already building (circular reference)?
		if(stack.contains(this))
			throw new IocCircularException(this, stack, "Circular reference");
		stack.push(this);

		//-- Create a partially-created DEF which can be used as a definition for 'this'.
		ComponentDef def = new ComponentDef(getActualClass(), getNameList().toArray(new String[getNameList().size()]), getDefinedTypes().toArray(new Class< ? >[getDefinedTypes().size()]), getScope(),
			getDefinitionLocation());

		BuildPlan plan = createBuildPlan(def, stack);
		if(this != stack.pop())
			throw new IllegalStateException("Stack mismatch!?");
		def.setPlan(plan);

		//-- Create the def for this object
		m_ref = new ComponentRef(def, getBuilder().getContainerIndex());
		return m_ref;
	}

	/**
	 * Create a build plan in two steps:
	 * <ol>
	 * 	<li>Create the object in some way using one of the supported creation methods</li>
	 * 	<li>Add any extra methods to call at start and destroy time.</li>
	 * </ol>
	 * @param self
	 * @param stack
	 * @return
	 */
	private BuildPlan createBuildPlan(final ISelfDef self, final Stack<ComponentBuilder> stack) {
		BuildPlan bp = createCreationBuildPlan(self, stack); // Create the actual object,

		if(bp instanceof AbstractBuildPlan) {
			//-- Handle setter logic
			AbstractBuildPlan abp = (AbstractBuildPlan) bp;

			List<PropertyInjector> ijlist = calculateSetterInjectors(stack);
			abp.setInjectorList(ijlist);

			if(m_destroyList.size() > 0)
				abp.setDestroyList(createCallArray(self, stack, m_destroyList));
		} else
			throw new IllegalStateException("Unexpected build plan");

		return bp;
	}

	/**
	 *
	 * @param self
	 * @param stack
	 * @return
	 */
	private BuildPlan createCreationBuildPlan(final ISelfDef self, final Stack<ComponentBuilder> stack) {
		switch(m_createMethod){
			default:
				throw new IocConfigurationException(this, "Internal: unknown CreationMethod " + m_createMethod);
			case ASNEW:
				return createConstructorPlan(stack);
			case FACTORY_METHOD:
				return createStaticFactoryBuildPlan(self, stack);
			case CONTAINER_PARAMETER:
				return createParameterBuildPlan(stack);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Constructor-based build plan						*/
	/*--------------------------------------------------------------*/
	/**
	 * Create a build plan for a normal constructed class.
	 * @param self
	 */
	private BuildPlan createConstructorPlan(final Stack<ComponentBuilder> stack) {
		Constructor< ? >[] car = m_baseClass.getConstructors();
		if(car == null || car.length == 0) // Cannot construct
			throw new IocConfigurationException(this, "The class " + m_baseClass + " has no public constructors");
		List<BuildPlanForConstructor> list = new ArrayList<BuildPlanForConstructor>();
		List<FailedAlternative> aflist = new ArrayList<FailedAlternative>();
		for(Constructor< ? > c : car) {
			if(!Modifier.isPublic(c.getModifiers())) {
				aflist.add(new FailedAlternative("The constructor " + c + " is not public"));
				continue;
			}
			BuildPlanForConstructor cbp = calcConstructorPlan(stack, c, aflist);
			if(cbp != null)
				list.add(cbp);
		}
		if(list.size() == 0)
			throw new BuildPlanFailedException(this, "None of the constructors was usable", aflist);

		//-- Find the plan with the highest score.
		BuildPlanForConstructor best = list.get(0);
		for(int i = 1; i < list.size(); i++) {
			BuildPlanForConstructor bp = list.get(i);
			if(bp.getScore() > best.getScore())
				best = bp;
		}
		return best;
	}

	MethodParameterSpec[] getParameters() {
		return null;
	}

	/**
	 * Try to create a build plan for creating the object using the constructor passed.
	 *
	 * @param cont
	 * @param stack
	 * @param c
	 * @param aflist
	 * @return
	 */
	private BuildPlanForConstructor calcConstructorPlan(final Stack<ComponentBuilder> stack, final Constructor< ? > c, final List<FailedAlternative> aflist) {
		Class< ? >[] fpar = c.getParameterTypes(); // Formals.
		Annotation[][] fpanar = c.getParameterAnnotations();
		if(fpar == null || fpar.length == 0) {
			return new BuildPlanForConstructor(c, 0); // Always works but with score=0
		}

		//-- Walk all parameters and make build plans for them until failure..
		try {
			MethodParameterSpec[] paref = getParameters();
			List<ComponentRef> actuals = calculateParameters(stack, fpar, fpanar, paref);

			//-- All constructor arguments were provided- return a build plan,
			return new BuildPlanForConstructor(c, fpar.length, actuals.toArray(new ComponentRef[actuals.size()]));
		} catch(IocUnresolvedParameterException x) {
			//-- This constructor has failed.
			FailedAlternative fa = new FailedAlternative("The constructor " + c + " is unusable: " + x.getMessage());
			aflist.add(fa);
			return null;
		}
	}

	private List<ComponentRef> calculateParameters(final Stack<ComponentBuilder> stack, final Class< ? >[] fpar, final Annotation[][] fpann, final MethodParameterSpec[] defar) {
		List<ComponentRef> actuals = new ArrayList<ComponentRef>();
		for(int i = 0; i < fpar.length; i++) {
			Class< ? > fp = fpar[i];
			MethodParameterSpec def = null;
			if(defar != null && i < defar.length)
				def = defar[i];
			ComponentRef cr = m_builder.findReferenceFor(null, stack, fp, fpann[i], def);
			if(cr == null) {
				//-- Cannot use this- the parameter passed cannot be filled in.
				throw new IocUnresolvedParameterException("Parameter " + i + " (a " + fp + ") cannot be resolved");
			}
			actuals.add(cr);
		}
		return actuals;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Factory-based build plan.							*/
	/*--------------------------------------------------------------*/

	private BuildPlan createStaticFactoryBuildPlan(final ISelfDef self, final Stack<ComponentBuilder> stack) {
		//-- Walk all factoryStart methods that are defined and create the methodlist from them
		List<MethodInvoker> startlist = createCallList(self, stack, m_factoryStartList);

		//-- Walk all possible factory methods, scoring them,
		List<BuildPlanForStaticFactory> list = new ArrayList<BuildPlanForStaticFactory>();
		List<FailedAlternative> aflist = new ArrayList<FailedAlternative>();
		for(Method m : m_factoryMethodList) {
			BuildPlanForStaticFactory cbp = calcStaticFactoryPlan(stack, m, aflist, startlist);
			if(cbp != null)
				list.add(cbp);
		}
		if(list.size() == 0)
			throw new BuildPlanFailedException(this, "None of the factory methods was usable", aflist);

		//-- Find the plan with the highest score.
		BuildPlanForStaticFactory best = list.get(0);
		for(int i = 1; i < list.size(); i++) {
			BuildPlanForStaticFactory bp = list.get(i);
			if(bp.getScore() > best.getScore())
				best = bp;
		}
		return best;
	}

	private BuildPlanForStaticFactory calcStaticFactoryPlan(final Stack<ComponentBuilder> stack, final Method c, final List<FailedAlternative> aflist, final List<MethodInvoker> startlist) {
		Class< ? >[] fpar = c.getParameterTypes(); // Formals.
		Annotation[][] fpanar = c.getParameterAnnotations();
		if(fpar == null || fpar.length == 0) {
			return new BuildPlanForStaticFactory(c, 0, BuildPlan.EMPTY_PLANS, startlist); // Always works but with score=0
		}

		//-- Walk all parameters and make build plans for them until failure..
		try {
			MethodParameterSpec[] paref = getParameters();
			List<ComponentRef> actuals = calculateParameters(stack, fpar, fpanar, paref);

			//-- All constructor arguments were provided- return a build plan,
			return new BuildPlanForStaticFactory(c, fpar.length, actuals.toArray(new ComponentRef[actuals.size()]), startlist);
		} catch(IocUnresolvedParameterException x) {
			//-- This constructor has failed.
			FailedAlternative fa = new FailedAlternative("The static factory method " + c + " is unusable: " + x.getMessage());
			aflist.add(fa);
			return null;
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Call invoker build plan calculator.					*/
	/*--------------------------------------------------------------*/

	private List<MethodInvoker> createCallList(final ISelfDef self, final Stack<ComponentBuilder> stack, final List<MethodCallBuilder> list) {
		List<MethodInvoker> res = new ArrayList<MethodInvoker>(list.size());
		for(MethodCallBuilder mcb : list)
			res.add(mcb.createInvoker(self, stack));
		return res;
	}

	private MethodInvoker[] createCallArray(final ISelfDef self, final Stack<ComponentBuilder> stack, final List<MethodCallBuilder> source) {
		MethodInvoker[] ar = new MethodInvoker[source.size()];
		for(int i = 0; i < ar.length; i++)
			ar[i] = source.get(i).createInvoker(self, stack);
		return ar;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Property injector calculation.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Once the build plan for the base object is known this will check what properties
	 * are to be set using container data. This takes the property defs and creates injectors (setters)
	 * for each property that is to be named.
	 *
	 * @return
	 */
	private List<PropertyInjector> calculateSetterInjectors(final Stack<ComponentBuilder> stack) {
		if(m_propertyMode == ComponentPropertyMode.NONE && m_propertyDefMap.size() == 0) // No properties to set -> exit immediately.
			return Collections.EMPTY_LIST;

		/*
		 * We define an ultimate set of all properties that can or must be injected, and we ensure
		 * that all explicitly named properties actually exist and are settable in the instance.
		 */
		Map<String, ComponentPropertyDef> fullmap = new HashMap<String, ComponentPropertyDef>(m_propertyDefMap); // The full set of thingies to do
		List<PropertyInfo> proplist = to.etc.util.ClassUtil.getProperties(m_actualType); // All class properties.
		Set<String> doneset = new HashSet<String>(m_propertyDefMap.keySet()); // Dup all explicitly set names
		for(PropertyInfo pi : proplist) {
			//-- Is this property named explicitly?
			ComponentPropertyDef pd = m_propertyDefMap.get(pi.getName());
			if(pd != null) {
				//-- Explicitly named property.
				doneset.remove(pi.getName()); // Has been found.
				if(pi.getSetter() == null)
					throw new IocConfigurationException(this, "The property '" + pi.getName() + "' is defined to be set but it is read-only (it has no applicable setter method)");
			} else if(m_propertyMode != ComponentPropertyMode.NONE && pi.getSetter() != null) {
				//-- Not set explicitly; we must set it because of the mode... So define a def for this property
				pd = new ComponentPropertyDef(this, pi.getName());
				pd.setRequired(m_propertyMode == ComponentPropertyMode.ALL); // Is required in ALL mode
				fullmap.put(pi.getName(), pd);
			}
			if(pd != null)
				pd.setInfo(pi); // Save info on property
		}

		//-- All that's left in doneset are properties that are undefined on this class, so die.
		if(doneset.size() > 0)
			throw new IocConfigurationException(this, "Unknown property/properties '" + doneset + "' on class " + m_actualType.getName());
		if(fullmap.size() == 0)
			return Collections.EMPTY_LIST;

		/*
		 * We have a full set of properies to provide injectors for: go, girl.
		 */
		List<PropertyInjector> res = new ArrayList<PropertyInjector>(fullmap.size());
		for(ComponentPropertyDef pd : fullmap.values()) {
			PropertyInjector pij = calculateInjector(stack, pd);
			if(pij == null) {
				if(pd.isRequired())
					throw new IocConfigurationException(this, "The property '" + pd.getPropertyName() + "' cannot be injected");
			} else
				res.add(pij);
		}

		return res;
	}

	/**
	 * Tries to calculate an injector for the specified property. Returns null if no injector can be found.
	 *
	 * @param stack
	 * @param pd
	 * @return
	 */
	private PropertyInjector calculateInjector(final Stack<ComponentBuilder> stack, final ComponentPropertyDef pd) {
		ComponentRef cr = m_builder.findReferenceFor(stack, pd);
		if(cr == null)
			return null;
		return new PropertyInjector(cr, pd.getInfo().getSetter());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Accessors.											*/
	/*--------------------------------------------------------------*/

	Class< ? > getActualClass() {
		if(m_actualType == null)
			throw new IllegalStateException("calculateType has not yet been called");
		return m_actualType;
	}

	List<Class< ? >> getDefinedTypes() {
		return m_definedTypeList;
	}

	public BindingScope getScope() {
		return m_scope;
	}
}
