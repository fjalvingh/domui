package to.etc.domui.themes;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.script.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.resources.*;

public class SimpleThemer implements IThemer {
	final private String m_styleName;

	public SimpleThemer(String styleName) {
		m_styleName = styleName;
	}

	@Override
	public ITheme loadTheme(DomApplication da) throws Exception {
		ResourceDependencyList rdl = new ResourceDependencyList();
		Map<String, Object> map = createThemeMap(da, rdl);
		return new SimpleTheme(m_styleName, map, rdl.createDependencies());
	}

	public Map<String, Object> createThemeMap(DomApplication da, ResourceDependencyList rdl) throws Exception {
		Map<String, Object> map = readProperties(da, rdl); // Read properties.
		String rurl = "$themes/" + m_styleName + "/style.jsproperties";
		IResourceRef ires = findRef(da, rurl, rdl);
		if(null == ires)
			return map;
		InputStream is = ires.getInputStream();
		if(is == null)
			return map;

		ScriptEngine se;
		try {
			//-- Try to load the script as a Javascript file and execute it.
			ScriptEngineManager m = new ScriptEngineManager();
			se = m.getEngineByName("js");
			Bindings b = se.createBindings();

			//-- Add all current map values
			for(Map.Entry<String, Object> e : map.entrySet()) {
				b.put(e.getKey(), e.getValue());
			}

			//-- Add helper classes FIXME TODO

			//-- Execute Javascript;
			Reader r = new InputStreamReader(is, "utf-8");
			se.eval(r);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}

		//-- Ok: the binding now contains stuff to add/replace to the map
		for(Map.Entry<String, Object> e : se.getBindings(ScriptContext.ENGINE_SCOPE).entrySet()) {
			String name = e.getKey();
			if("context".equals(name))
				continue;
			Object v = e.getValue();
			if(v != null) {
				String cn = v.getClass().getName();
				if(cn.startsWith("sun."))
					continue;
			}
		}
		return map;
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "OS_OPEN_STREAM", justification = "Stream is closed closing wrapped instance.")
	public Map<String, Object> readProperties(DomApplication da, ResourceDependencyList rdl) throws Exception {
		String rurl = "$themes/" + m_styleName + "/style.properties";
		IResourceRef ires = findRef(da, rurl, rdl);
		if(null == ires)
			return new HashMap<String, Object>();

		//-- Read the thingy as a property file.
		Properties p = new Properties();
		InputStream is = ires.getInputStream();
		try {
			p.load(new InputStreamReader(is, "utf-8")); // wrapped "is" is closed, no need to close reader.
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

	protected IResourceRef findRef(@Nonnull DomApplication da, @Nonnull String rurl, @Nullable ResourceDependencyList rdl) throws Exception {

		try {
			IResourceRef ires = da.getApplicationResourceByName(rurl); // Get the source file, abort if not found
			if(null != rdl)
				rdl.add(ires);
			return ires;
		} catch(ThingyNotFoundException x) {}
		return null;
	}

}
