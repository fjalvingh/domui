package to.etc.iocular.def;

import java.io.IOException;
import java.lang.reflect.Constructor;
import to.etc.iocular.container.BasicContainer;
import to.etc.iocular.container.BuildPlan;
import to.etc.util.IndentWriter;

/**
 * A build plan to call a constructor.
 *
 * @author jal
 * Created on Mar 28, 2007
 */
final public class ConstructorBuildPlan implements BuildPlan {
	private final int				m_score;
	private final Constructor<?>	m_constructor;
	private final ComponentRef[]	m_actuals;

	public ConstructorBuildPlan(final Constructor<?> constructor, final int score, final ComponentRef[] actuals) {
		m_constructor = constructor;
		m_score = score;
		m_actuals	= actuals;
	}
	public ConstructorBuildPlan(final Constructor< ? > constructor, final int score) {
		this(constructor, score, BuildPlan.EMPTY_PLANS);
	}
	public Object getObject(final BasicContainer bc) throws Exception {
		Object[]	param = new Object[ m_actuals.length ];
		for(int i = m_actuals.length; --i >= 0;) {
			param[i] = bc.retrieve(m_actuals[i]);
		}

		//-- Construct a new instance,
		return m_constructor.newInstance(param);
	}
	public int getScore() {
		return m_score;
	}

	public void dump(final IndentWriter iw) throws IOException {
		iw.print("InstanceConstructor ");
		iw.print(m_constructor.toGenericString());
		iw.print(" (score ");
		iw.print(Integer.toString(m_score));
		iw.println(")");
		if(m_actuals.length != 0) {
			iw.println("- Constructor parameter build plan(s):");
			iw.inc();
			for(int i = 0; i < m_actuals.length; i++) {
				iw.println("constructor parameter# "+i);
				iw.inc();
				if(m_actuals[i] == null)
					iw.println("!?!?!?! null BuildPlan!!??!");
				else
					m_actuals[i].dump(iw);
				iw.dec();
			}
			iw.dec();
		}
	}
	public boolean needsStaticInitialization() {
		return false;
	}
	public void staticStart(final BasicContainer c) throws Exception {
	}
}
