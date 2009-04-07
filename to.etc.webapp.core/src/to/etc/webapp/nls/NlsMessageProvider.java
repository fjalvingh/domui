package to.etc.webapp.nls;

import java.util.*;

/**
 * Something which can provide a message for a given code and locale.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 10, 2006
 */
public interface NlsMessageProvider {
	public String findMessage(Locale loc, String code);
}
