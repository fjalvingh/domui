package to.etc.util.commandinterpreter;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-02-22.
 */
final class CommandHandler {
	final private Method m_method;

	private final String m_pattern;

	final private ParamInfo[] m_paramInfo;

	final private List<IWordRecognizer> m_recognizerList;

	public CommandHandler(Method method, String pattern, ParamInfo[] paramInfo, List<IWordRecognizer> recognizerList) {
		m_method = method;
		m_pattern = pattern;
		m_paramInfo = paramInfo;
		m_recognizerList = recognizerList;
	}

	public Method getMethod() {
		return m_method;
	}

	public ParamInfo[] getParamInfo() {
		return m_paramInfo;
	}

	public List<IWordRecognizer> getRecognizerList() {
		return m_recognizerList;
	}

	public int getParamCount() {
		return m_paramInfo.length;
	}

	public String getHelpText() {
		return m_pattern;
	}
}
