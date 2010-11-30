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
import java.util.*;

/**
 * <p>A container definition contains the <quote>compiled</quote> form of the
 * configuration of a single container in a container tree. The container
 * definition can be <i>inherited</i> from another definition; in that case
 * the definition of the "base" container is <i>extended</i> by the definitions
 * of this container. The whole definition still pertains to a single container
 * though.</p>
 * <p>The most common form of inter-definition dependency is the "parent container"
 * or "parent" link. This defines this configuration to be for a container that
 * has another container as a parent. The parent-child relationship is used when
 * for instance the parent and child containers have different scope and lifecycle
 * rules.</p>
 * <p>The container definition gets completed and checked when configuration has
 * completed. At that time the builders will check all of the data pertaining to
 * the containers and will create <b>build plans</i> for all of the defined artifacts.
 * If, during this process, it is determined that any object cannot be built using the
 * definitions in the configuration then an error will be thrown and the container
 * definition will not be created.</p>
 *
 * @author jal
 * Created on Apr 3, 2007
 */
final public class ContainerDefinition {
	/** A name of this container. It should be unique within a container tree. */
	private final String m_name;

	/** If this definition is for a container that has a parent container this refers to the parent's definition. */
	private final ContainerDefinition m_parentContainerDefinition;

	/** The index of this container within the container stack; where the topmost parent has index 0, and each child has a one-highet index. */
	private final int m_containerIndex;

	/** If this definition extends another definition this refers to the "base" definition. */
	//	private ContainerDefinition		m_originalBaseDefinition;

	private Map<String, ComponentRef> m_namedMap = new HashMap<String, ComponentRef>();

	private Map<Class< ? >, ComponentRef> m_declaredMap = new HashMap<Class< ? >, ComponentRef>();

	private Map<Class< ? >, ComponentRef> m_actualMap = new HashMap<Class< ? >, ComponentRef>();

	public ContainerDefinition(final String name, final ContainerDefinition base, final ContainerDefinition parent, final Map<String, ComponentRef> namedMap,
		final Map<Class< ? >, ComponentRef> declaredMap, final Map<Class< ? >, ComponentRef> actualMap, final int index) {
		m_name = name;
		m_parentContainerDefinition = parent;
		//		m_originalBaseDefinition = base;
		m_namedMap = namedMap;
		m_declaredMap = declaredMap;
		m_actualMap = actualMap;
		m_containerIndex = index;
	}

	public ContainerDefinition getParentDefinition() {
		return m_parentContainerDefinition;
	}

	public String getName() {
		return m_name;
	}

	public int getContainerIndex() {
		return m_containerIndex;
	}

	public ComponentRef findComponentReference(final String name) {
		return m_namedMap.get(name);
	}

	public ComponentRef findComponentReference(final Class< ? > cls) {
		ComponentRef r = m_declaredMap.get(cls);
		if(r != null)
			return r;
		return m_actualMap.get(cls);
	}

	/**
	 * Local workhorse to decode the parameters and get the appropriate object
	 * to fulfill the specified reference.
	 *
	 * @param ptype
	 * @param annar
	 * @param def
	 * @return
	 */
	ComponentRef findDefinedReference(final Class< ? > ptype, final Annotation[] annar, final MethodParameterSpec def) {
		//-- Try to find a ComponentBuilder that is able to provide this thingy.
		/*
		 * TODO First try to find something using the provided parameters and annotations.
		 */

		//-- No parameters provided- try to locate using the type of the type using the defined type table
		ComponentRef ref = m_declaredMap.get(ptype);
		if(ref != null)
			return ref;

		//		//-- Find in my base, if possible,
		//		if(m_baseDefinition != null) {
		//			ref = m_baseDefinition.findDefinedReference(ptype, annar, def);
		//			if(ref != null)
		//				return ref;
		//		}

		//-- Try my parent.
		if(m_parentContainerDefinition != null) {
			ref = m_parentContainerDefinition.findDefinedReference(ptype, annar, def);
			if(ref != null)
				return ref;
		}
		return null; // Not found!
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
	ComponentRef findInferredReference(final Class< ? > ptype, final Annotation[] annar, final MethodParameterSpec def) {
		//-- Try to find a ComponentBuilder that is able to provide this thingy.
		/*
		 * TODO First try to find something using the provided parameters and annotations.
		 */
		ComponentRef ref = m_actualMap.get(ptype);
		if(ref != null)
			return ref;

		//		//-- Find in my base, if possible,
		//		if(m_baseDefinition != null) {
		//			ref = m_baseDefinition.findInferredReference(ptype, annar, def);
		//			if(ref != null)
		//				return ref;
		//		}

		//-- Try my parent.
		if(m_parentContainerDefinition != null) {
			ref = m_parentContainerDefinition.findInferredReference(ptype, annar, def);
			if(ref != null)
				return ref;
		}
		return null; // Not found!
	}
}
