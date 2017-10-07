package to.etc.domui.sass;

import com.vaadin.sass.internal.handler.SCSSErrorHandler;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;

/**
 * Captures all messages in a string.
 */
final class SassCapturingErrorHandler extends SCSSErrorHandler {
	final private StringBuilder m_sb = new StringBuilder();

	private boolean m_hasError;

	@Override public void warning(CSSParseException e) throws CSSException {
		render("warning", e);
	}

	private void render(String type, CSSParseException e) {
		m_sb.append(e.getURI())
		.append("(")
		.append(e.getLineNumber())
		.append(':')
		.append(e.getColumnNumber())
		.append(") ")
		.append(type)
		.append(": ")
		.append(e.getMessage())
		.append("\n");
	}

	private void render(String type, String message) {
		m_sb.append("ERROR: ").append(message).append("\n");
	}

	@Override public void traverseError(Exception e) {
		super.traverseError(e);
		render("error", e.toString());
		m_hasError = true;
	}

	@Override public void traverseError(String message) {
		super.traverseError(message);
		render("error", message);
		m_hasError = true;
	}

	@Override public void error(CSSParseException e) throws CSSException {
		m_hasError = true;
		render("error", e);
	}

	@Override public void fatalError(CSSParseException e) throws CSSException {
		m_hasError = true;
		render("fatal error", e);
	}

	public boolean hasError() {
		return m_hasError;
	}

	@Override public String toString() {
		return m_sb.toString();
	}
}
