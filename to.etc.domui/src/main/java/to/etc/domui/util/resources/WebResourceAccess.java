package to.etc.domui.util.resources;

import org.eclipse.jdt.annotation.NonNull;

import java.util.Locale;
import java.util.Set;

/**
 * Access rules for webapp resources that can be addressed by an URL.
 *
 * <p>A servlet container never serves anything below the webapp's WEB-INF and META-INF directories:
 * those contain the application's configuration, its classes and its libraries. DomUI resolves
 * resources itself - see {@link to.etc.domui.server.parts.InternalResourcePart} for the "$name"
 * resource mechanism - and thereby bypasses the container completely, so it has to enforce that same
 * rule itself. Without it an URL like <code>$WEB-INF/web.xml</code> would simply return the content
 * of that file.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9-9-26.
 */
final public class WebResourceAccess {
	/** The webapp directories that must never be reachable from an URL, in lowercase. */
	static private final Set<String> PROTECTED_DIRECTORY_SET = Set.of("web-inf", "meta-inf");

	private WebResourceAccess() {
	}

	/**
	 * Returns T if the specified resource path may not be served: it either addresses one of the
	 * protected webapp directories or it tries to escape from the webapp's root.
	 */
	static public boolean isForbidden(@NonNull String path) {
		for(String segment : path.replace('\\', '/').split("/")) {
			if(segment.equals(".."))
				return true;
			if(PROTECTED_DIRECTORY_SET.contains(normalizeSegment(segment)))
				return true;
		}
		return false;
	}

	/**
	 * Throws {@link SecurityException} if the specified resource path may not be served, see
	 * {@link #isForbidden(String)}.
	 */
	static public void checkAllowed(@NonNull String path) {
		if(isForbidden(path))
			throw new SecurityException("Access to the resource '" + path + "' is not allowed");
	}

	/**
	 * Windows ignores trailing dots and whitespace in file names, so "WEB-INF." and "WEB-INF " address
	 * the very same directory as "WEB-INF" there; remove those before comparing. The lowercasing uses
	 * {@link Locale#ROOT} because a locale sensitive conversion would not map the I in WEB-INF to an i
	 * in, for instance, the Turkish locale.
	 */
	@NonNull
	static private String normalizeSegment(@NonNull String segment) {
		int end = segment.length();
		while(end > 0) {
			char c = segment.charAt(end - 1);
			if(c != '.' && !Character.isWhitespace(c))
				break;
			end--;
		}
		return segment.substring(0, end).toLowerCase(Locale.ROOT);
	}
}
