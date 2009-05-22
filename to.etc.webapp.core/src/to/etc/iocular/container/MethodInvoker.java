package to.etc.iocular.container;

import java.io.IOException;
import java.lang.reflect.Method;
import to.etc.iocular.def.ComponentRef;
import to.etc.util.IndentWriter;

final public class MethodInvoker {
	private final Method			m_method;

	private final ComponentRef[]	m_actuals;

	public MethodInvoker(final Method method, final ComponentRef[] actuals) {
		m_method = method;
		m_actuals = actuals;
	}

	public int getScore() {
		return m_method.getParameterTypes().length;
	}

	/**
	 * Actually invoke the method on some thingy.
	 *
	 * @param bc
	 * @return
	 * @throws Exception
	 */
	public Object invoke(final Object thisobject, final BasicContainer bc) throws Exception {
		Object[]	param = new Object[ m_actuals.length ];
		for(int i = m_actuals.length; --i >= 0;) {
			param[i] = bc.retrieve(m_actuals[i]);
		}

		return m_method.invoke(thisobject, param);
	}

	public void dump(final IndentWriter iw) throws IOException {
		iw.print("InstanceConstructor ");
		iw.print(m_method.toGenericString());
		iw.print(" (score ");
		iw.print(Integer.toString(getScore()));
		iw.println(")");
		if(m_actuals.length != 0) {
			iw.println("- Method parameter build plan(s):");
			iw.inc();
			for(int i = 0; i < m_actuals.length; i++) {
				iw.println("argument# "+i);
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
}
