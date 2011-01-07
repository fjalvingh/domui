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
import javax.resource.spi.IllegalStateException;
import javax.script.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.resources.*;
import to.etc.template.*;
import to.etc.util.*;

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

	private ScriptEngineManager m_engineManager;

	public CssFragmentCollector(DomApplication da, String name) {
		if(name.startsWith("/"))
			name = name.substring(1);
		if(name.endsWith("/"))
			name = name.substring(0, name.length() - 1);
		m_app = da;
	}

	private void init() throws Exception {
		if(m_engineManager != null)
			return;

		m_engineManager = new ScriptEngineManager();
		m_compiler = new JSTemplateCompiler();
	}

	ScriptEngineManager getEngineManager() throws Exception {
		init();
		return m_engineManager;
	}

	DomApplication getApp() {
		return m_app;
	}


	public void loadStyleSheet() throws Exception {
		//		loadStyleProperties();
		//		appendFragments();
	}



	/*--------------------------------------------------------------*/
	/*	CODING:	Stylesheet Properties file loader thingy			*/
	/*--------------------------------------------------------------*/
	/**
	 * Load a property file set for colors and style properties, where the
	 * properties are not fragmented.
	 */
	public CssPropertySet getProperties(String dir, String name) throws Exception {
		CssPropertySet ps = new CssPropertySet(this, dir, name, null);
		ps.loadStyleProperties(dir);
		return ps;
	}

	public CssPropertySet getFragmentedProperties(String dir, String rootfile, String suffix) throws Exception {
		CssPropertySet ps = new CssPropertySet(this, dir, rootfile, suffix);
		ps.loadStyleProperties(dir);
		return ps;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Fragment collector.									*/
	/*--------------------------------------------------------------*/
	private JSTemplateCompiler m_compiler;

	/**
	 * The type of fragment expansion/check to do.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jan 7, 2011
	 */
	static public enum Check {
		/** Do not check at all: just concatenate the fragments. */
		NONE

		/** Try to expand every fragment by calling the template expander, but use original source as result, not the expanded template */
		, CHECK

		/** Expand the fragment and use the result in the append operation. */
		, EXPAND
	}


	/**
	 * This code collects "fragments" of files and connects them to form a
	 * full file which is the concatenation of all fragments. The fragments
	 * are identified by a set of "directory names" and a "suffix". The fragments are
	 * either files inside [webapp-dir/directory], i.e. files below WebContent,
	 * or class resources below a "resources" toplevel "package". Files take
	 * precedence over resources with the same name, so every resource can be
	 * easily overridden by a file in the same directory with the exact same name.
	 *
	 * <p>The loadType decides whether the content of each fragment is expanded or not.</p>
	 *
	 * @throws Exception
	 *
	 */
	public void getFragments(StringBuilder target, List<String> directory, String suffix, Check loadType, ResourceDependencyList rdl, Map<String, Object> propertyMap) throws Exception {
		long ts = System.nanoTime();

		//-- Find all possible files/resources, then sort them by their name.
		List<String> reslist = collectFragments(directory, suffix);

		//-- Run the templater on every file.
		int count = 0;
		for(String name : reslist) {
			String full = "$" + name;
			appendFragment(target, full, loadType, rdl, propertyMap);
			count++;
		}
		ts = System.nanoTime() - ts;
		System.out.println("css: loading " + directory + "+: loaded " + count + " fragments took " + StringTool.strNanoTime(ts));
	}

	/**
	 * Load and append the specified concrete fragment.
	 * @param target
	 * @param full
	 * @param loadType
	 * @param rdl
	 * @throws Exception
	 */
	private void appendFragment(StringBuilder target, String full, Check loadType, ResourceDependencyList rdl, Map<String, Object> propertyMap) throws Exception {
		IResourceRef ires = findRef(rdl, full);
		if(null == ires)
			throw new StyleException("The " + full + " file/resource is not found.");
		InputStream is = ires.getInputStream();
		if(null == is)
			throw new StyleException("The " + full + " file/resource is not found.");
		//		System.out.println("css: loading " + full + " as " + ires);

		try {
			//-- 1. Load as a string.
			String source = FileTool.readStreamAsString(is, "utf-8");
			JSTemplate tmpl = m_compiler.compile(new StringReader(source), full);
			StringBuilder sb = target;
			switch(loadType){
				default:
					throw new IllegalStateException("Bad?");
				case NONE:
					target.append(source);
					return;
				case CHECK:
					sb = new StringBuilder();
					//$FALL-THROUGH$
				case EXPAND:
					tmpl.execute(sb, propertyMap);
					return;
			}
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	@Nullable
	protected IResourceRef findRef(ResourceDependencyList rdl, @Nonnull String rurl) throws Exception {
		try {
			IResourceRef ires = m_app.getApplicationResourceByName(rurl); // Get the source file, abort if not found
			rdl.add(ires);
			return ires;
		} catch(ThingyNotFoundException x) {}
		return null;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Collecting an inheritance stack of fragments.		*/
	/*--------------------------------------------------------------*/


	public List<String> collectFragments(List<String> directoryStack, String suffix) throws Exception {
		if(!suffix.startsWith("."))
			suffix = "." + suffix;
		suffix = suffix.toLowerCase();

		//-- Find all possible files/resources, then sort them by their name.
		Map<String, String> frags = collectFragments(directoryStack);
		List<String> names = new ArrayList<String>(frags.keySet());
		Collections.sort(names);

		//-- Create a full list, and filter bad extensions.
		List<String> res = new ArrayList<String>(names.size());
		for(String name : names) {
			if(!name.toLowerCase().endsWith(suffix))
				continue;

			String map = frags.get(name);
			String full = map + "/" + name;
			res.add(full);
		}
		return res;
	}


	/**
	 * Walk the inheritance tree from baseclass to superclass, and collect
	 * all fragments by name; in the process remove all duplicates.
	 * @throws Exception
	 */
	private Map<String, String> collectFragments(List<String> directoryStack) throws Exception {
		Map<String, String> res = new HashMap<String, String>();

		for(String inh : directoryStack)
			collectFragments(res, inh);
		return res;
	}

	/**
	 * Scan the specified name as a directory, and locate all files in first
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
}
