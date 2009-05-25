package to.etc.iocular.def;

import java.io.*;

import to.etc.iocular.container.BuildPlan;
import to.etc.util.IndentWriter;

/**
 * A reference to some component in a given container.
 *
 * @author jal
 * Created on Apr 9, 2007
 */
final public class ComponentRef {
	private final ComponentDef	m_def;

	private final int			m_containerIndex;

	public ComponentRef(final ComponentDef def, final int containerIndex) {
		m_def = def;
		m_containerIndex = containerIndex;
	}
	public ComponentDef getDefinition() {
		return m_def;
	}
	public boolean	isSelf() {
		return m_def == null;
	}
	public int getContainerIndex() {
		return m_containerIndex;
	}
	public void	dump(final IndentWriter iw) throws IOException {
		if(m_def == null) {
			iw.println("REF:self");
			return;
		}
		iw.println("REF:container["+m_containerIndex+"] component "+m_def.getIdent()+" build plan:");
		iw.inc();
		getDefinition().getBuildPlan().dump(iw);
		iw.dec();
	}

	public BuildPlan	getBuildPlan() {
		return m_def.getBuildPlan();
	}
	@Override
	public String toString() {
		if(isSelf())
			return "ref:self";
		return "ref:container["+m_containerIndex+"] component "+m_def.getIdent();
	}
}
