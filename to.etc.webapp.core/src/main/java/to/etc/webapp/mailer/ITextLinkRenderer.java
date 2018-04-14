package to.etc.webapp.mailer;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Render a textlink instance from some message string. The implementation
 * decides how the link is rendered.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 5, 2010
 */
public interface ITextLinkRenderer {
	/**
	 * Render a link to the specified relative URL (it does not contain app context nor host name).
	 * @param rurl
	 * @param text
	 */
	void appendLink(@NonNull String rurl, @NonNull String text);

	/**
	 * Append verbatim literal text.
	 * @param text
	 */
	void appendText(@NonNull String text);
}
