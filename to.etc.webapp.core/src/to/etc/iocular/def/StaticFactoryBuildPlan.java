package to.etc.iocular.def;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import to.etc.iocular.container.BasicContainer;
import to.etc.iocular.container.BuildPlan;
import to.etc.iocular.container.MethodInvoker;
import to.etc.util.IndentWriter;

public class StaticFactoryBuildPlan implements BuildPlan {
	/**
	 * The static factory method to invoke.
	 */
	private Method		m_method;

	/**
	 * The build plans for the method's arguments.
	 */
	private ComponentRef[]	m_argumentList;

	private MethodInvoker[]	m_startList;

	private int	m_score;

	StaticFactoryBuildPlan(Method m, int score, ComponentRef[] args, List<MethodInvoker> startlist) {
		m_method = m;
		m_argumentList = args;
		m_score = score;

		if(startlist != null && startlist.size() > 0)
			m_startList = startlist.toArray(new MethodInvoker[startlist.size()]);
	}

	public int getScore() {
		return m_score;
	}

	/**
	 * Execute the plan to *get* the object from 
	 * @see to.etc.iocular.container.BuildPlan#getObject()
	 */
	public Object getObject(BasicContainer bc) throws Exception {
		Object[]	param = new Object[ m_argumentList.length ];
		for(int i = m_argumentList.length; --i >= 0;) {
			param[i] = bc.retrieve(m_argumentList[i]);
		}
		return m_method.invoke(null, param);
	}

	public void dump(IndentWriter iw) throws IOException {
		iw.print("Staticfactory method ");
		iw.println(m_method.toGenericString());
		if(m_argumentList.length != 0) {
			iw.println("- Constructor parameter build plan(s):");
			iw.inc();
			for(int i = 0; i < m_argumentList.length; i++) {
				iw.println("argument# "+i);
				iw.inc();
				m_argumentList[i].dump(iw);
				iw.dec();
			}
			iw.dec();
		}		
	}

	public boolean needsStaticInitialization() {
		return m_startList != null;
	}

	/**
	 * Called as a single-time per container init only if this factory has static initializers.
	 *
	 * @see to.etc.iocular.container.BuildPlan#staticStart(to.etc.iocular.container.BasicContainer)
	 */
	public void staticStart(BasicContainer c) throws Exception {
		for(MethodInvoker miv : m_startList) {
			miv.invoke(null, c);
		}
	}
}
