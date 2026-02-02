package to.etc.util.commandinterpreter;

import to.etc.telnet.TelnetPrintWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-02-22.
 */
class CommandContext {
	private final CommandHandler m_handler;

	private final Object[] m_paramValues;

	private final boolean[] m_paramValueSet;

	private final String[] m_paramError;

	private final List<String> m_longestMatch = new ArrayList<>();

	public CommandContext(CommandHandler handler) {
		m_handler = handler;
		m_paramValueSet = new boolean[handler.getParamCount()];
		m_paramValues = new Object[handler.getParamCount()];
		m_paramError = new String[handler.getParamCount()];
	}

	public void setParameter(int targetIndex, Object o) {
		m_paramValues[targetIndex] = o;
		m_paramValueSet[targetIndex] = true;
	}

	public void setParameterError(int targetIndex, String message) {
		m_paramError[targetIndex] = message;
	}

	public boolean recognize(String[] words) {
		for(int i = 0; i < m_paramValues.length; i++) {
			m_paramValues[i] = null;
			m_paramError[i] = null;
			m_paramValueSet[i] = false;
		}
		m_longestMatch.clear();
		int wordIndex = 0;
		for(IWordRecognizer wr : m_handler.getRecognizerList()) {
			if(wordIndex >= words.length) {
				return false;
			}

			String word = words[wordIndex];
			if(! wr.recognize(this, word)) {
				return false;
			}
			m_longestMatch.add(word);
			wordIndex++;
		}
		return wordIndex == words.length;
	}

	public List<String> getLongestMatch() {
		return m_longestMatch;
	}

	public boolean hasError() {
		boolean hasError = false;
		for(int i = 1; i < m_paramValues.length; i++) {
			if(m_paramError[i] != null) {
				hasError = true;
			} else if(! m_paramValueSet[i]) {
				m_paramError[i] = "No value";
				hasError = true;
			}
		}
		return hasError;
	}

	public Object[] getParamValues() {
		return m_paramValues;
	}

	public CommandHandler getHandler() {
		return m_handler;
	}

	public String getErrorMessage() {
		for(int i = 1; i < m_paramError.length; i++) {
			String error = m_paramError[i];
			if(null != error) {
				return "parameter " + m_handler.getParamInfo()[i].getName() + " value error: " + error;
			}
		}
		return "Unknown error??";
	}

	public boolean renderHelp(TelnetPrintWriter tpw, String[] help) {
		boolean matched = true;

		int wordIndex = 0;
		for(IWordRecognizer wr : m_handler.getRecognizerList()) {
			if(wordIndex >= help.length) {
				break;
			}

			String word = help[wordIndex];
			if(! wr.recognize(this, word)) {
				return false;
			}
			wordIndex++;
		}

		tpw.println(m_handler.getHelpText());
		return true;
	}
}
