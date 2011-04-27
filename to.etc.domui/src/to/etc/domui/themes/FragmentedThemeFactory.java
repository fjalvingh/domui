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

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.js.*;
import to.etc.domui.util.resources.*;
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
public class FragmentedThemeFactory implements IThemeFactory {
	static public final FragmentedThemeFactory INSTANCE = new FragmentedThemeFactory();

	private DomApplication m_application;

	private String m_themeName;

	private String m_styleName;

	private String m_iconName;

	private String m_colorName;

	private CssPropertySet m_colorSet;

	private CssPropertySet m_iconSet;

	private CssPropertySet m_styleSet;

	private String m_stylesheet;

	private DomApplication m_app;

	/**
	 * Constructor to create the factory itself.
	 */
	private FragmentedThemeFactory() {
	}

	/**
	 * Constructor for an instance.
	 * @param da
	 * @param themeName
	 */
	private FragmentedThemeFactory(DomApplication da, String themeName) {
		m_application = da;
		m_themeName = themeName;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Factory code.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Create the theme store for the specified theme input string.
	 * @see to.etc.domui.themes.IThemeFactory#getTheme(to.etc.domui.server.DomApplication, java.lang.String)
	 */
	@Override
	public ITheme getTheme(DomApplication da, String themeName) throws Exception {
		FragmentedThemeFactory stf = new FragmentedThemeFactory(da, themeName);
		try {
			return stf.createTheme();
		} finally {
			try {
				stf.close();
			} catch(Exception x) {}
		}
	}

	/**
	 *
	 */
	private void close() {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Instance creation.									*/
	/*--------------------------------------------------------------*/
	@Nullable
	private RhinoExecutor m_executor;

	@Nonnull
	private RhinoExecutor executor() {
		if(null == m_executor)
			m_executor = RhinoExecutorFactory.getInstance().createExecutor();
		return m_executor;
	}


	/**
	 * Instance creation.
	 * @return
	 */
	private ITheme createTheme() throws Exception {
		//-- Split theme name into theme/icons/color
		String[] ar = m_themeName.split("\\/");
		if(ar.length != 3)
			throw new StyleException("The theme name '" + m_themeName + "' is invalid for the factory SimpleThemeFactory: expecting theme/icon/color");
		m_styleName = ar[0];
		m_iconName = ar[1];
		m_colorName = ar[2];

		loadStyleInfo();
		ResourceDependencyList rdl = new ResourceDependencyList();
		rdl.add(m_colorSet.getResourceDependencyList());
		rdl.add(m_iconSet.getResourceDependencyList());
		rdl.add(m_styleSet.getResourceDependencyList());

		loadStylesheetFragments(rdl);
		ResourceDependencies rd = rdl.createDependencies();

		//-- Compile the template;
		//		RhinoTemplateCompiler rtc = new RhinoTemplateCompiler();
		//		RhinoTemplate tmpl = rtc.compile(new StringReader(m_stylesheet), "<theme fragments>");

		List<String> templateList = new ArrayList<String>();
		templateList.addAll(m_iconSet.getInheritanceStack());
		templateList.addAll(m_styleSet.getInheritanceStack());

		return new FragmentedThemeStore(m_application, m_stylesheet.getBytes("utf-8"), executor(), templateList, rd);
	}

	/**
	 * Load all *.frag.css files and construct a proper stylesheet from them.
	 * @throws Exception
	 */
	private void loadStylesheetFragments(IResourceDependencyList rdl) throws Exception {
		StringBuilder sb = new StringBuilder(65536);
		//		ResourceDependencyList rdl = new ResourceDependencyList();
		executor().put("browser", BrowserVersion.parseUserAgent("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)"));

		getFragments(sb, m_styleSet.getInheritanceStack(), ".frag.css", Check.CHECK, rdl);
		m_stylesheet = sb.toString();
	}

	protected void loadStyleInfo() throws Exception {
		loadStyleInfo(m_colorName, m_iconName, m_styleName);
	}

	protected void loadStyleInfo(String colorset, String iconset, String styleset) throws Exception {
		m_colorSet = getProperties("themes", colorset + ".color.js");
		m_iconSet = getFragmentedProperties("icons/" + iconset, "icon.props.js", ".fragprops.js");
		m_styleSet = getProperties("themes/" + styleset, "style.props.js");
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
		ps.loadStyleProperties(executor(), dir);
		return ps;
	}

	public CssPropertySet getFragmentedProperties(String dir, String rootfile, String suffix) throws Exception {
		CssPropertySet ps = new CssPropertySet(this, dir, rootfile, suffix);
		ps.loadStyleProperties(executor(), dir);
		return ps;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Fragment collector.									*/
	/*--------------------------------------------------------------*/

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
	public void getFragments(StringBuilder target, List<String> directory, String suffix, Check loadType, IResourceDependencyList rdl) throws Exception {
		long ts = System.nanoTime();

		//-- Find all possible files/resources, then sort them by their name.
		List<String> reslist = collectFragments(directory, suffix);

		//-- Run the templater on every file.
		int count = 0;
		for(String name : reslist) {
			String full = "$" + name;
			appendFragment(target, full, loadType, rdl);
			count++;
		}
		ts = System.nanoTime() - ts;
		System.out.println("css: loading " + directory + "+: loaded " + count + " fragments took " + StringTool.strNanoTime(ts));
	}

	/**
	 * Load and append the specified concrete fragment.
	 * @param target
	 * @param fullPathName
	 * @param loadType
	 * @param rdl
	 * @throws Exception
	 */
	private void appendFragment(StringBuilder target, String fullPathName, Check loadType, IResourceDependencyList rdl) throws Exception {
		IResourceRef ires = findRef(rdl, fullPathName);
		if(null == ires)
			throw new StyleException("The " + fullPathName + " file/resource is not found.");
		InputStream is = ires.getInputStream();
		if(null == is)
			throw new StyleException("The " + fullPathName + " file/resource is not found.");
		//		System.out.println("css: loading " + full + " as " + ires);

		try {
			//-- 1. Load as a string.
			String source = FileTool.readStreamAsString(is, "utf-8");
			RhinoTemplateCompiler rtc = new RhinoTemplateCompiler();
			RhinoTemplate tmpl = rtc.compile(new StringReader(source), fullPathName);
			StringBuilder sb = target;
			switch(loadType){
				default:
					throw new IllegalStateException("Bad?");
				case NONE:
					target.append(source);
					return;
				case CHECK:
					target.append(source);
					sb = new StringBuilder();
					//$FALL-THROUGH$
				case EXPAND:
					tmpl.execute(sb, executor().newScope());
					return;
			}
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	@Nullable
	protected IResourceRef findRef(@Nonnull IResourceDependencyList rdl, @Nonnull String rurl) throws Exception {
		try {
			IResourceRef ires = m_app.getResource(rurl, rdl); // Get the source file, abort if not found
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
