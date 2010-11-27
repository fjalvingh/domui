package to.etc.domui.util;

import java.io.*;
import java.util.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.resources.*;

/**
 * This is the default implementation of a ThemeMap factory. It uses the current "default"
 * theme and reads a "style.properties" file from there. That file's content is returned
 * as the map.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 27, 2010
 */
public class DefaultThemeMapFactory implements IThemeMapFactory {
	@Override
	public Map<String, Object> createThemeMap(DomApplication da, ResourceDependencyList rdl) throws Exception {
		String rurl = "$themes/" + da.getDefaultTheme() + "/style.properties";
		IResourceRef ires;
		try {
			ires = da.getApplicationResourceByName(rurl); // Get the source file, abort if not found
			if(null != rdl)
				rdl.add(ires); // We're dependent on it...
		} catch(ThingyNotFoundException x) {
			return new HashMap<String, Object>();
		}

		//-- Read the thingy as a property file.
		Properties p = new Properties();
		InputStream is = ires.getInputStream();
		try {
			p.load(new InputStreamReader(is, "utf-8"));
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}

		//-- Make the output map
		Map<String, Object> map = new HashMap<String, Object>();
		for(Object key : p.keySet()) {
			String k = (String) key;
			String val = p.getProperty(k);
			map.put(k, val);
		}
		return map;
	}
}
