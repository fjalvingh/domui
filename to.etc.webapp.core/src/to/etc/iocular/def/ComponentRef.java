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

	ComponentRef(final ComponentDef def, final int containerIndex) {
		m_def = def;
		m_containerIndex = containerIndex;
	}
	ComponentRef(final ComponentDef def) {
		m_def = def;
		m_containerIndex = -1;
	}
	public ComponentDef getDefinition() {
		return m_def;
	}
	public boolean	isSelf() {
		return m_containerIndex < 0;
	}
	public int getContainerIndex() {
		return m_containerIndex;
	}
	public void	dump(final IndentWriter iw) throws IOException {
		if(isSelf()) {
			iw.println("REF:self["+m_def.getIdent()+"]");
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
			return "ref:self["+m_def.getIdent()+"]";
		return "ref:container["+m_containerIndex+"] component "+m_def.getIdent();
	}
}
