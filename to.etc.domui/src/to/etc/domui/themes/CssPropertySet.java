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

import to.etc.domui.util.js.*;
import to.etc.domui.util.resources.*;
import to.etc.util.*;

/**
 * A completed set of properties for either theme properties,
 * icon properties or colorset properties.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 7, 2011
 */
public class CssPropertySet {
	final private FragmentedThemeFactory m_collector;

	/** The root name of the map containing the styles. This must be a real slashed "directory" name that can be looked up in resources and WebContent files. */
	final private String m_dirname;

	final private String m_name;

	final private String m_fragmentSuffix;

	/** When style a EXTENDS style B etc, this starts with the base class (b) and ends with the topmost one (A). */
	private List<String> m_inheritanceStack = new ArrayList<String>();

	private ResourceDependencyList m_rdl = new ResourceDependencyList();

	CssPropertySet(FragmentedThemeFactory fc, String dirname, String name, String fragments) {
		m_collector = fc;
		m_dirname = dirname;
		m_name = name;
		m_fragmentSuffix = fragments;
	}

	/**
	 * Load a property set including it's base sets in proper inheritance order.
	 *
	 * Load the properties for the current style *and it's base styles*. After this, the style sheet
	 * property files have executed in the proper order, and the context contains the proper properties.
	 * @param start
	 */
	void loadStyleProperties(RhinoExecutor rx, String dirname) throws Exception {
		if(m_inheritanceStack.size() > 0)
			throw new IllegalStateException("Already loaded!");

		//-- Re-create the "inherit" function for the thingy to load.
		rx.put("collector", this);
		rx.eval("function inherit(s) { collector.internalInherit(s); }");
		loadProperties(rx, dirname, m_name); // Start loading all files-by-name

		if(m_fragmentSuffix != null)
			loadFragments(rx);
	}

	/**
	 * Walks the inheritance stack, and loads all fragments present there as properties too.
	 * @param rx
	 * @throws Exception
	 */
	private void loadFragments(RhinoExecutor rx) throws Exception {
		long ts = System.nanoTime();

		//-- Find all possible files/resources, then sort them by their name.
		List<String> reslist = m_collector.collectFragments(m_inheritanceStack, m_fragmentSuffix);

		//-- Load every one of them as a javascript file.
		int count = 0;
		for(String name : reslist) {
			String full = "$" + name;
			loadScript(rx, full);
			count++;
		}
		ts = System.nanoTime() - ts;
		System.out.println("css: loading " + m_dirname + "+: loaded " + count + " fragments took " + StringTool.strNanoTime(ts));
	}

	/**
	 * Load a specific theme's style properties. Core part of inherit('') command.
	 * @param dirname
	 * @throws Exception
	 */
	private void loadProperties(RhinoExecutor rx, String dirname, String filename) throws Exception {
		if(dirname.startsWith("/"))
			dirname = dirname.substring(1);
		if(dirname.endsWith("/"))
			dirname = dirname.substring(0, dirname.length() - 1);
		if(dirname.startsWith("$"))
			dirname = dirname.substring(1);

		//-- If already loaded- abort;
		if(m_inheritanceStack.contains(dirname))
			throw new StyleException(m_name + ": inherited style '" + dirname + "' is used before (cyclic loop in styles, or double inheritance)");
		m_inheritanceStack.add(0, dirname); // Insert BEFORE the others (this is a base class for 'm)

		//-- Load the .props.js file which must exist as either resource or webfile.
		String pname = "$" + dirname + "/" + filename;
		loadScript(rx, pname);
	}

	/**
	 * Load the specified resource as a Javascript thingy.
	 * @param pname
	 * @throws Exception
	 */
	private void loadScript(RhinoExecutor rx, String pname) throws Exception {
		IResourceRef ires = m_collector.findRef(m_rdl, pname);
		if(null == ires)
			throw new StyleException("The " + pname + " file is not found.");
		InputStream is = ires.getInputStream();
		if(null == is)
			throw new StyleException("The " + pname + " file is not found.");
		System.out.println("css: loading " + pname + " as " + ires);
		try {
			//-- Execute Javascript;
			Reader r = new InputStreamReader(is, "utf-8");
			rx.eval(r, pname);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	public List<String> getInheritanceStack() {
		return m_inheritanceStack;
	}

	public ResourceDependencyList getResourceDependencyList() {
		return m_rdl;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Javascript-callable global functions.				*/
	/*--------------------------------------------------------------*/
	//	/**
	//	 * Implements the root-level "inherit" command.
	//	 * @param scheme
	//	 * @throws Exception
	//	 */
	//	public void internalInherit(String scheme) throws Exception {
	//		loadProperties(scheme, m_name);
	//	}

	@Override
	public String toString() {
		return m_dirname + "/" + m_name;
	}
}
