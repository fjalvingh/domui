package to.etc.domui.server;

//import org.owasp.esapi.ESAPI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on ${DATE}.
 */
final public class XssChecker {
	/** When set this allows full URLs to be present in 'src=' tags that are marked as F_ALLOWLOCALSRC */
	private boolean m_allowFullUrlForSrc;

	private static final Pattern[] PATTERNLIST = new Pattern[]{
		// Script fragments
		Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
		// lonely script tags
		Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
		Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		// eval(...)
		Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		// expression(...)
		Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		// javascript:...
		Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
		// vbscript:...
		Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
		// onload(...)=...
		Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
	};

	private static final Pattern[] SRCLIST = new Pattern[]{
		// src='...'
		Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
	};

	static public final int F_ALLOWLOCALSRC = 0x01;

	static public final String SUFFIX_ALLOWLOCALSRC = "_als";

	public XssChecker() {
	}


	public boolean isXss(String text) {
		if(null == text)
			return false;
		//text = ESAPI.encoder().canonicalize(text);
		text = text.replaceAll("\0", "");

		for(Pattern pattern : SRCLIST) {
			if(pattern.matcher(text).matches())
				return true;
		}
		for(Pattern pattern : PATTERNLIST) {
			if(pattern.matcher(text).matches())
				return true;
		}
		return false;
	}

	public String stripXSS(String value) {
		return stripXSS(value, 0);
	}

	/**
	 * This allows the parameter name to have some special suffixes that indicate some
	 * values normally stripped are now allowed (like the src= value in an img tag).
	 */
	public String stripXSS(String parameterName, String value) {
		if(parameterName.endsWith(SUFFIX_ALLOWLOCALSRC)) {
			return stripXSS(value, F_ALLOWLOCALSRC);
		}

		return stripXSS(value, 0);
	}

	public String stripXSS(String value, int flags) {
		if(value != null) {
			//value = ESAPI.encoder().canonicalize(value);

			//-- Avoid null characters
			value = value.replaceAll("\0", "");

			//-- Remove all sections that match a pattern
			for(Pattern scriptPattern : PATTERNLIST) {
				value = scriptPattern.matcher(value).replaceAll("");
			}

			if((flags & F_ALLOWLOCALSRC) == 1) {
				//-- Used for html editors that can contain img tags, this checks src= but allows local URLs (not containing http: or https:)
				for(Pattern scriptPattern : SRCLIST) {
					Matcher matcher = scriptPattern.matcher(value);
					if(matcher.matches()) {
						//-- If we allow full URLs in these then we're done, otherwise make sure the url is relative.
						if(! m_allowFullUrlForSrc) {
							String mv = matcher.group().toLowerCase();
							if(mv.contains("http:") || mv.contains("https:")) {
								value = scriptPattern.matcher(value).replaceAll("");
							}
						}
					}
				}
			} else {
				for(Pattern scriptPattern : SRCLIST) {
					value = scriptPattern.matcher(value).replaceAll("");
				}
			}
		}
		return value;
	}

	/**
	 * DANGEROUS! When set this allows full URLs to be present in 'src=' tags that are marked as F_ALLOWLOCALSRC.
	 * */
	public boolean isAllowFullUrlForSrc() {
		return m_allowFullUrlForSrc;
	}

	public void setAllowFullUrlForSrc(boolean allowFullUrlForSrc) {
		m_allowFullUrlForSrc = allowFullUrlForSrc;
	}
}
