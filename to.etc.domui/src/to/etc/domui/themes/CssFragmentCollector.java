/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.themes;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.script.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.resources.*;
import to.etc.template.*;

/**
 * Experimental - This class collects all ".frag.css" files in the specified
 * "directory", while allowing them to be "overridden" in other parts of the
 * structure. The resulting set of .frag.css files is then run through the
 * template compiler (one by one) to create the final result. This result
 * should be the "actual" css file to use.
 *
 * <h1>Style inheritance</h1>
 * <p>A style can 'inherit' another style. This is done in each style's ".jsprops" file
 * where the file must start with an "inherit('xxxx')" statement.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 5, 2011
 */
public class CssFragmentCollector {
	final private DomApplication m_app;

	/** The root name of the map containing the styles. This must be a real slashed "directory" name that can be looked up in resources and WebContent files. */
	final private String m_name;

	/** When style a EXTENDS style B etc, this starts with the base class (b) and ends with the topmost one (A). */
	private List<String> m_inheritanceStack = new ArrayList<String>();

	private ResourceDependencyList m_rdl = new ResourceDependencyList();

	private ScriptEngineManager m_engineManager;

	private ScriptEngine m_engine;

	private Bindings m_rootBindings;

	private Bindings m_bindings;

	private Map<String, Object> m_propertyMap;

	public CssFragmentCollector(DomApplication da, String name) {
		if(name.startsWith("/"))
			name = name.substring(1);
		if(name.endsWith("/"))
			name = name.substring(0, name.length() - 1);
		m_app = da;
		m_name = name;
	}

	private void init() throws Exception {
		if(m_engineManager != null)
			return;

		m_engineManager = new ScriptEngineManager();
		m_engine = m_engineManager.getEngineByName("js");
		m_rootBindings = m_engine.getBindings(ScriptContext.GLOBAL_SCOPE);
		m_rootBindings.put("collector", this);
		m_engine.eval("function inherit(s) { collector.internalInherit(s); }");
		m_compiler = new JSTemplateCompiler();
		m_bindings = m_engine.createBindings();
	}

	public void loadStyleSheet() throws Exception {
		loadStyleProperties();
		appendFragments();
	}


	/**
	 * Load the properties for the current style *and it's base styles*. After this, the style sheet
	 * property files have executed in the proper order, and the context contains the proper properties.
	 */
	public void loadStyleProperties() throws Exception {
		loadProperties(m_name);

		//-- Ok: the binding now contains stuff to add/replace to the map
		m_propertyMap = new HashMap<String, Object>();
		for(Map.Entry<String, Object> e : m_bindings.entrySet()) {
			String name = e.getKey();
			if("context".equals(name))
				continue;
			Object v = e.getValue();
			if(v != null) {
				String cn = v.getClass().getName();
				if(cn.startsWith("sun."))
					continue;
			}
			System.out.println("prop: " + name + " = " + v);
			m_propertyMap.put(name, v);
		}
	}

	/**
	 * Load a specific theme's style properties. Core part of inherit('') command.
	 * @param name
	 * @throws Exception
	 */
	private void loadProperties(String name) throws Exception {
		init();
		if(name.startsWith("/"))
			name = name.substring(1);
		if(name.endsWith("/"))
			name = name.substring(0, name.length() - 1);
		if(name.startsWith("$"))
			name = name.substring(1);

		//-- If already loaded- abort;
		if(m_inheritanceStack.contains(name))
			throw new StyleException(m_name + ": inherited style '" + name + "' is used before (cyclic loop in styles, or double inheritance)");
		m_inheritanceStack.add(0, name); // Insert BEFORE the others (this is a base class for 'm)

		//-- Load the .props.js file which must exist as either resource or webfile.
		String pname = "$" + name + "/style.props.js";
		IResourceRef ires = findRef(pname);
		if(null == ires)
			throw new StyleException("The " + pname + " file is not found.");
		InputStream is = ires.getInputStream();
		if(null == is)
			throw new StyleException("The " + pname + " file is not found.");
		System.out.println("css: loading " + pname + " as " + ires);
		try {
			//-- Execute Javascript;
			Reader r = new InputStreamReader(is, "utf-8");
			m_engine.eval(r, m_bindings);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	protected IResourceRef findRef(@Nonnull String rurl) throws Exception {
		try {
			IResourceRef ires = m_app.getApplicationResourceByName(rurl); // Get the source file, abort if not found
			m_rdl.add(ires);
			return ires;
		} catch(ThingyNotFoundException x) {}
		return null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	*.frag.css collection, over the inherited model.	*/
	/*--------------------------------------------------------------*/
	private StringBuilder m_sb = new StringBuilder(128000);

	private JSTemplateCompiler m_compiler;

	/**
	 * @throws Exception
	 *
	 */
	private void appendFragments() throws Exception {
		Map<String, String> frags = collectFragments();
		List<String> names = new ArrayList<String>(frags.keySet());
		Collections.sort(names);

		//-- Run the templater on every file.
		for(String name : names) {
			if(!name.endsWith(".frag.css"))
				continue;

			String map = frags.get(name);
			String full = "$" + map + "/" + name;
			appendFragment(full);
		}
	}

	private void appendFragment(String full) throws Exception {
		IResourceRef ires = findRef(full);
		if(null == ires)
			throw new StyleException("The " + full + " file/resource is not found.");
		InputStream is = ires.getInputStream();
		if(null == is)
			throw new StyleException("The " + full + " file/resource is not found.");
		System.out.println("css: loading " + full + " as " + ires);

		try {
			Reader r = new InputStreamReader(is, "utf-8");
			JSTemplate tmpl = m_compiler.compile(r, full);
			tmpl.execute(m_sb, m_propertyMap);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Walk the inheritance tree from baseclass to superclass, and collect
	 * all fragments by name; in the process remove all duplicates.
	 * @throws Exception
	 */
	private Map<String, String> collectFragments() throws Exception {
		Map<String, String> res = new HashMap<String, String>();

		for(String inh : m_inheritanceStack)
			collectFragments(res, inh);
		return res;
	}

	/**
	 * Scan the specified name as a directory, and locate all *.frag.css files in first
	 * the classpath, then the webapp's files directory.
	 * @param inh
	 */
	private void collectFragments(Map<String, String> nameSet, String inh) {
		String pkgres = "resources/" + inh;
		List<String>	kns = ClasspathInventory.getInstance().getPackageInventory(pkgres);
		for(String s: kns)
			nameSet.put(s, inh);

		//-- Scan webapp path
		File wad = m_app.getAppFile(inh);
		if(wad != null && wad.isDirectory()) {
			for(File far : wad.listFiles()) {
				if(far.isFile())
					nameSet.put(far.getName(), inh);
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Javascript-callable global functions.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Implements the root-level "inherit" command.
	 * @param scheme
	 * @throws Exception
	 */
	public void internalInherit(String scheme) throws Exception {
		loadProperties(scheme);
	}
}
