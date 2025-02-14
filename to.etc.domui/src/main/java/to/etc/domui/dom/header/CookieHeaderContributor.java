package to.etc.domui.dom.header;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.IContributorRenderer;
import to.etc.domui.state.UIContext;
import to.etc.domui.util.DomUtil;
import to.etc.util.FileTool;
import to.etc.util.StringTool;
import to.etc.webapp.nls.BundleRef;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This shows a Cookie warning and acceptance button for users of the website, as required by
 * an idiotic EU law made by people that have the brain of a peanut and
 * are in all possible ways so stupid that it boggles the mind.
 * Instead of worthwhile legislation that would actually help with
 * privacy issues we now have the requirement to ask for consent on a
 * technical tool on about 100% of websites. A question that no one in
 * its right mind ever reads, because it is useless.
 *
 * To find more info on this idiot law and its requirements see:
 *
 * https://torquemag.io/2018/08/cookie-law-and-consent/
 * https://www.socalinternetlawyer.com/does-the-eu-cookie-law-apply-to-u-s-websites/
 *
 * To comply with the idiot law include this script and make sure your
 * rules and cookie policy adhere to all the laws of the 30+ EU states.
 * But also provide your users with the option to install:
 * https://chrome.google.com/webstore/detail/i-dont-care-about-cookies/fihnjjcciajhdojfnbdddfaoknhalnja?hl=en
 * https://addons.mozilla.org/en-US/firefox/addon/i-dont-care-about-cookies/
 *
 * and hope that the moron lawmakers that instituted this idiocy on mankind
 * get lose as much of their lifetime as they take from us wasting time
 * on this abomination of a law.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-19.
 */
@NonNullByDefault
public class CookieHeaderContributor extends HeaderContributor {
	static public final String ANALYTICS_COOKIECLASS = "analytics";

	final private BundleRef m_bundle;

	private final String m_gaScript;

	private String m_link;

	private Set<String> m_cookieClasses = new HashSet<>();

	@Nullable
	private String m_googleAnalytics;

	final private String m_script;

	public CookieHeaderContributor(Class<?> anchor, String resourceName, String link) throws Exception {
		m_bundle = BundleRef.create(anchor, resourceName);
		m_link = link;
		m_script = FileTool.readResourceAsString(getClass(), "/misc/cookiejs.js", "utf-8");
		m_gaScript = FileTool.readResourceAsString(getClass(), "/misc/googleAnalytics.js", "utf-8");
	}

	@Override
	public boolean isOfflineCapable() {
		return false;
	}

	@Override
	public void contribute(IContributorRenderer r) throws Exception {
		r.renderLoadJavascript("$js/jquery.ihavecookies.js", false, false);

		//-- If the link is not absolute then make it so
		String link = m_link;
		if(! DomUtil.isAbsoluteURL(link)) {
			link = UIContext.getRequestContext().getRelativePath(link);
		}

		String message = m_bundle.getString("message");

		String types = m_cookieClasses.stream()
			.map(this::createCookieSet)
			.collect(Collectors.joining(","))
			;

		StringBuilder cookieAcceptanceJS = new StringBuilder();

		if(m_googleAnalytics != null) {
			//-- See https://developers.google.com/analytics/devguides/collection/analyticsjs
			r.renderLoadJavascript("https://www.google-analytics.com/analytics.js", true, false);

			//-- Initialize the Analytics code only when analytics have been accepted
			String analyticsJs = m_gaScript.replace("${id}", m_googleAnalytics);

			cookieAcceptanceJS.append("if($.fn.ihavecookies.preference('analytics')) {\n")
				.append(analyticsJs)
				.append("}\n");
			;
		}


		String script = m_script.replace("${msg}", StringTool.strToJavascriptString(message, false))
			.replace("${link}", link)
			.replace("${title}", StringTool.strToJavascriptString(m_bundle.getString("title"), false))
			.replace("${acclabel}", StringTool.strToJavascriptString(m_bundle.getString("acceptButton"), false))
			.replace("${infolabel}", StringTool.strToJavascriptString(m_bundle.getString("moreInformation"), false))
			.replace("${cookieTypes}", "[" + types + "]")
			.replace("${cookieAcceptance}", cookieAcceptanceJS.toString())
		;
		r.o().tag("script");
		r.o().attr("language", "javascript");
		r.o().attr("nonce", r.getPage().getNonce());
		r.o().endtag();
		//r.o().writeRaw("<!--\n"); // Embed JS in comment IMPORTANT: the \n is required!!!
		r.o().writeRaw(script);
		//r.o().writeRaw("\n-->");
		r.o().writeRaw("\n");
		r.o().closetag("script");
	}

	private String createCookieSet(String a) {
		return "{type: " + StringTool.strToJavascriptString(m_bundle.getString(a + ".type"), false)
			+ ", value: " + StringTool.strToJavascriptString(a, false)
			+ ", description: " + StringTool.strToJavascriptString(m_bundle.getString(a + ".desc"), false)
			+ "}"
			;
	}

	public CookieHeaderContributor cookieClass(String key) {
		m_cookieClasses.add(key);
		return this;
	}

	public CookieHeaderContributor googleAnalytics(String gaKey) {
		m_googleAnalytics = gaKey;
		cookieClass(ANALYTICS_COOKIECLASS);
		return this;
	}

	@Override
	public int hashCode() {
		return 13;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		return (obj instanceof CookieHeaderContributor);
	}
}
