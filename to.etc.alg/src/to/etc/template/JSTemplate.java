package to.etc.template;

import java.util.*;

import javax.script.*;

/**
 * A single template which can be generated.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 26, 2010
 */
public class JSTemplate {
	final private String					m_source;

	final private ScriptEngine				m_engine;

	final private CompiledScript			m_code;

	final private List<JSLocationMapping>	m_locMap;

	public JSTemplate(String source, ScriptEngine engine, CompiledScript code, List<JSLocationMapping> locMap) {
		m_source = source;
		m_engine = engine;
		m_code = code;
		m_locMap = locMap;
	}

	final public String getSource() {
		return m_source;
	}

	/**
	 * Execute this template.
	 * @param tc
	 * @param assignments
	 */
	public void execute(IJSTemplateContext tc, Object... assignments) {
		//-- Bind values
		Bindings xb = m_engine.createBindings();
		for(int i = 0; i < assignments.length; i += 2) {
			String name = (String) assignments[i];
			Object val = assignments[i + 1];
			xb.put(name, val);
		}
		xb.put("out", tc);

		try {
			m_code.eval(xb);
		} catch(ScriptException sx) {
			int[] res = JSTemplateCompiler.remapLocation(m_locMap, sx.getLineNumber(), sx.getColumnNumber());
			throw new JSTemplateError(sx.getMessage(), m_source, res[0], res[1]);
		}
	}

	/**
	 * Execute this template, and leave the result in the specified appendable.
	 * @param a
	 * @param assignments
	 */
	public void execute(final Appendable a, Object... assignments) {
		IJSTemplateContext tc = new IJSTemplateContext() {
			@Override
			public void writeValue(Object v) throws Exception {
				if(v == null)
					return;
				a.append(v.toString());
			}

			@Override
			public void write(String text) throws Exception {
				a.append(text);
			}
		};
		execute(tc, assignments);
	}
}
