package to.etc.iocular.def;

import to.etc.iocular.container.BuildPlan;
import to.etc.util.IndentWriter;

/**
 * A reference to some component in a given container.
 *
 * @author jal
 * Created on Apr 9, 2007
 */
final public class ComponentRef {
	private ComponentDef		m_def;

	private int					m_containerIndex;

	public ComponentRef(ComponentDef def, int containerIndex) {
		m_def = def;
		m_containerIndex = containerIndex;
	}
	public ComponentDef getDefinition() {
		return m_def;
	}
	public int getContainerIndex() {
		return m_containerIndex;
	}
	public void	dump(IndentWriter iw) {
		
	}

	public BuildPlan	getBuildPlan() {
		return m_def.getBuildPlan();
	}
}
