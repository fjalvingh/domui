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

import to.etc.iocular.*;
import to.etc.iocular.ioccontainer.*;

final public class ComponentDef implements ISelfDef {
	/** All names this component was registered with; the 1st one is the primary. For an unnamed item this is the empty array. */
	private final String[] m_names;

	private final Class< ? > m_actualClass;

	/**
	 * All of the classes that this component is <i>defined</i> to have in
	 * the configuration.
	 */
	private final Class< ? >[] m_definedTypes;

	private final BindingScope m_scope;

	private final String m_definitionLocation;

	/** The completed build plan for this type. */
	private BuildPlan m_buildPlan;

	ComponentDef(final Class< ? > actualclz, final String[] names, final Class< ? >[] deftypes, final BindingScope scope, final String definitionLocation) {
		m_names = names;
		m_definedTypes = deftypes;
		m_scope = scope;
		m_definitionLocation = definitionLocation;
		m_actualClass = actualclz;
	}

	void setPlan(final BuildPlan plan) {
		m_buildPlan = plan;
	}

	/**
	 * Return the actual class that will be built using this definition.
	 * @return
	 */
	@Override
	public Class< ? > getActualClass() {
		return m_actualClass;
	}

	@Override
	final public String getDefinitionLocation() {
		return m_definitionLocation;
	}

	@Override
	public String[] getNames() {
		return m_names;
	}

	@Override
	public Class< ? >[] getDefinedTypes() {
		return m_definedTypes;
	}

	@Override
	public BindingScope getScope() {
		return m_scope;
	}

	@Override
	public String toString() {
		return getIdent() + " defined at " + m_definitionLocation;
	}

	@Override
	public String getIdent() {
		if(m_names.length > 0)
			return "component(name=" + m_names[0] + ")";
		if(m_definedTypes.length > 0)
			return "component(type=" + m_definedTypes[0].toString() + ")";
		return "component(Unnamed?/Untyped?)";
	}

	/**
	 * Return the precompiled build plan for this component.
	 * @return
	 */
	public BuildPlan getBuildPlan() {
		if(m_buildPlan == null)
			throw new IllegalStateException("Internal: attempt to get build plan for an object currently being built!?");
		return m_buildPlan;
	}
}
