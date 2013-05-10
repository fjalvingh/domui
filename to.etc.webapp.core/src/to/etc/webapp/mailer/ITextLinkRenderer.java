package to.etc.webapp.mailer;

import javax.annotation.*;

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
	void appendLink(@Nonnull String rurl, @Nonnull String text);

	/**
	 * Append verbatim literal text.
	 * @param text
	 */
	void appendText(@Nonnull String text);
}
