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



}
