package to.etc.iocular.def;

import to.etc.iocular.BindingScope;
import to.etc.iocular.container.BuildPlan;

final public class ComponentDef {
	/** All names this component was registered with; the 1st one is the primary. For an unnamed item this is the empty array. */
	private String[]		m_names;

	private Class<?>		m_actualClass;

	/**
	 * All of the classes that this component is <i>defined</i> to have in
	 * the configuration.
	 */
	private Class<?>[]		m_definedTypes;

	private BindingScope	m_scope;

	private String			m_definitionLocation;

	/** The completed build plan for this type. */
	private BuildPlan		m_buildPlan;

	ComponentDef(Class<?> actualclz, String[] names, Class<?>[] deftypes, BindingScope scope, String definitionLocation, BuildPlan plan) {
		m_names = names;
		m_definedTypes = deftypes;
		m_scope = scope;
		m_definitionLocation = definitionLocation;
		m_buildPlan = plan;
		m_actualClass	= actualclz;
	}

	/**
	 * Return the actual class that will be built using this definition.
	 * @return
	 */
	public Class<?>	getActualClass() {
		return m_actualClass;
	}
	
	final public String getDefinitionLocation() {
		return m_definitionLocation;
	}
	public String[]	getNames() {
		return m_names;
	}
	public Class< ? >[] getDefinedTypes() {
		return m_definedTypes;
	}
	public BindingScope getScope() {
		return m_scope;
	}

	@Override
	public String toString() {
		return getIdent()+" defined at "+m_definitionLocation;
	}
	public String	getIdent() {
		if(m_names.length > 0)
			return "component(name="+m_names[0]+")";
		if(m_definedTypes.length > 0)
			return "component(type="+m_definedTypes[0].toString()+")";
		return "component(Unnamed?/Untyped?)";
	}

	/**
	 * Return the precompiled build plan for this component.
	 * @return
	 */
	public BuildPlan	getBuildPlan() {
		return m_buildPlan;
	}
}
