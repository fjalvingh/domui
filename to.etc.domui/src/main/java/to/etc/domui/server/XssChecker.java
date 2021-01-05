package to.etc.domui.server;

//import org.owasp.esapi.ESAPI;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on ${DATE}.
 */
final public class XssChecker {
	private static final Pattern[] PATTERNLIST = new Pattern[]{
		// Script fragments
		Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
		// src='...'
		Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
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

	private XssChecker() {}


	static public boolean isXss(String text) {
		if(null == text)
			return false;
		//text = ESAPI.encoder().canonicalize(text);
		text = text.replaceAll("\0", "");
		for(Pattern pattern : PATTERNLIST) {
			if(pattern.matcher(text).matches())
				return true;
		}
		return false;
	}

	static public String stripXSS(String value) {
		if (value != null) {
			//value = ESAPI.encoder().canonicalize(value);

			//-- Avoid null characters
			value = value.replaceAll("\0", "");

			//-- Remove all sections that match a pattern
			for (Pattern scriptPattern : PATTERNLIST){
				value = scriptPattern.matcher(value).replaceAll("");
			}
		}
		return value;
	}

}
