package to.etc.domui.server.parts;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.themes.IThemeVariant;
import to.etc.domui.util.Constants;
import to.etc.util.StringTool;

/**
 * Takes the dark/light preference the browser reports and makes it this session's theme
 * variant, then sends the browser back to the page it came from.
 *
 * <p>The preference lives in the browser, so the server can only learn it from a script; and
 * the theme is a server side stylesheet, so the page has to be rendered again once it is
 * known. {@link to.etc.domui.dom.HtmlFullRenderer} writes that script as the first thing in
 * the head of a page whose session never chose a variant: it asks
 * <code>matchMedia("(prefers-color-scheme: dark)")</code> and, when the answer differs from
 * what is being rendered, replaces the location with this part before anything is painted.</p>
 *
 * <p>This happens once per browser: the answer is stored in the session and in a cookie by
 * {@link RequestContextImpl#setThemeVariant(IThemeVariant)}, and the script is not written
 * again for a session that has a stored choice. That is what keeps a browser which refuses
 * the cookie from bouncing between the two pages forever - the session alone is enough to
 * end it.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
final public class ColorSchemePart implements IUnbufferedPartFactory {
	/** The webapp relative URL this part listens on. */
	static public final String URL = "$colorscheme";

	/** The scheme the browser prefers: "light" or "dark". */
	static public final String PARAM_SCHEME = "scheme";

	/** Where to go afterwards, as a path relative to the application URL. */
	static public final String PARAM_TARGET = "target";

	static public final IUrlMatcher MATCHER = parameters -> URL.equals(parameters.getInputPath());

	@Override
	public void generate(@NonNull DomApplication app, @NonNull String rurl, @NonNull RequestContextImpl ctx) throws Exception {
		String scheme = ctx.getPageParameters().getString(PARAM_SCHEME, "light");
		IThemeVariant variant = app.getThemeVariantForColorScheme(null == scheme ? "light" : scheme);
		if(null != variant) {
			ctx.setThemeVariant(variant);
		}

		//-- The target came from us, but it travelled through the browser: allow nothing that could leave this application.
		String target = ctx.getPageParameters().getString(PARAM_TARGET, "");
		if(null == target || !isInsideApplication(target)) {
			target = "";
		}
		ctx.getRequestResponse().setNoCache();
		ctx.getRequestResponse().redirect(ctx.getRequestResponse().getApplicationURL() + target);
	}

	/**
	 * T when the target is a path below the application URL and nothing else: it may not start
	 * with a slash or a backslash, which would make it host relative, and its path may not carry
	 * a scheme, which would make it absolute. Both would turn this part into an open redirect.
	 */
	static private boolean isInsideApplication(@NonNull String target) {
		if(target.startsWith("/") || target.startsWith("\\"))
			return false;
		int qix = target.indexOf('?');
		String path = qix < 0 ? target : target.substring(0, qix);
		return path.indexOf(':') < 0 && path.indexOf('\\') < 0;
	}

	/**
	 * The URL to send a browser preferring the scheme passed to, returning to the page the
	 * request being rendered asked for.
	 */
	static public String getRedirectURL(@NonNull IRequestContext ctx, @NonNull String scheme) {
		StringBuilder sb = new StringBuilder();
		sb.append(URL).append('?').append(PARAM_SCHEME).append('=').append(scheme);
		sb.append('&').append(PARAM_TARGET).append('=');
		StringTool.encodeURLEncoded(sb, getReturnPath(ctx));
		return sb.toString();
	}

	/**
	 * The webapp relative path plus query string of the current request: what the browser asked
	 * for, minus the conversation id. Dropping that is what makes the page be built again
	 * instead of being taken from the conversation - it was built for the variant we are leaving,
	 * and a component that looked at the variant while building (the dark/light switch itself,
	 * for one) would come back showing the wrong thing.
	 */
	static private String getReturnPath(@NonNull IRequestContext ctx) {
		String uri = ctx.getRequestResponse().getRequestURI();
		String context = ctx.getRequestResponse().getWebappContext();

		//-- getRequestURI() starts with a slash and includes the context; the application URL already has both.
		StringBuilder sb = new StringBuilder(uri.substring(1 + context.length()));
		String query = ctx.getRequestResponse().getQueryString();
		if(null != query) {
			boolean first = true;
			for(String part : query.split("&")) {
				if(part.isEmpty() || part.startsWith(Constants.PARAM_CONVERSATION_ID + "="))
					continue;
				sb.append(first ? '?' : '&');
				first = false;
				sb.append(part);
			}
		}
		return sb.toString();
	}
}
