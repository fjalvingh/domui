package to.etc.domui.dom.html;

import java.lang.instrument.Instrumentation;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 13-11-18.
 */
public class InstrumentationAgent {
	private static volatile Instrumentation globalInstrumentation;

	public static void premain(final String agentArgs, final Instrumentation inst) {
		globalInstrumentation = inst;
	}

	public static long getObjectSize(final Object object) {
		if (globalInstrumentation == null) {
			throw new IllegalStateException("Agent not initialized.");
		}
		return globalInstrumentation.getObjectSize(object);
	}
}
