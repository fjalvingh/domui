package to.etc.domui.dom.html;

import java.lang.instrument.Instrumentation;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 13-11-18.
 */
public class InstrumentationAgent {
	@SuppressWarnings("squid:S3077") //-- Junk analysis. This is set by the premain method, and is not modified after that.
	private static volatile Instrumentation m_globalInstrumentation;


	private InstrumentationAgent() {
		//--
	}

	@SuppressWarnings("squid:S1172") //-- Junk analysis. The method signature is defined by the JVM, and the parameters are used by the JVM.
	public static void premain(final String agentArgs, final Instrumentation inst) {
		m_globalInstrumentation = inst;
	}

	public static long getObjectSize(final Object object) {
		if (m_globalInstrumentation == null) {
			throw new IllegalStateException("Agent not initialized.");
		}
		return m_globalInstrumentation.getObjectSize(object);
	}
}
