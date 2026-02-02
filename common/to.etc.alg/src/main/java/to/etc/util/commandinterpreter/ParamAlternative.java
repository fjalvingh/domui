package to.etc.util.commandinterpreter;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-02-22.
 */
final class ParamAlternative {
	private final Pattern m_pattern;

	private final String m_value;

	public ParamAlternative(Pattern pattern, String value) {
		m_pattern = pattern;
		m_value = value;
	}

	public Pattern getPattern() {
		return m_pattern;
	}

	public String getValue() {
		return m_value;
	}
}
