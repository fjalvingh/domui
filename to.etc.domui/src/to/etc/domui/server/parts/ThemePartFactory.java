package to.etc.domui.server.parts;

import java.io.*;

import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.domui.util.resources.*;
import to.etc.util.*;

/**
 * This accepts all urls in the format *.theme.xxx. It generates string resources that
 * depend on the theme map. It reads the original resource as a string and replaces all
 * theme values therein before re-rendering the result to the caller.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 1, 2009
 */
public class ThemePartFactory implements IBufferedPartFactory, IUrlPart {
	public boolean accepts(String rurl) {
		int dot1 = rurl.lastIndexOf('.');
		if(dot1 == -1)
			return false;
		int dot2 = rurl.lastIndexOf('.', dot1 - 1);
		if(dot2 == -1)
			return false;
		return rurl.substring(dot2 + 1, dot1).equals("theme");
	}

	public Object decodeKey(String rurl, IParameterInfo param) throws Exception {
		return rurl;
	}

	public void generate(PartResponse pr, DomApplication da, Object key, ResourceDependencyList rdl) throws Exception {
		if(!da.inDevelopmentMode()) { // Not gotten from WebContent or not in DEBUG mode? Then we may cache!
			pr.setCacheTime(da.getDefaultExpiryTime());
		}
		String src = (String) key;
		String content = da.getThemeReplacedString(rdl, src);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(pr.getOutputStream()));
		pw.append(content);
		pw.close();
		pr.setMime(ServerTools.getExtMimeType(FileTool.getFileExtension(src)));
	}
}
