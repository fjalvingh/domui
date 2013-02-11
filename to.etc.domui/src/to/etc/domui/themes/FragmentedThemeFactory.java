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
	static private final FragmentedThemeFactory INSTANCE = new FragmentedThemeFactory();

	private DomApplication m_application;

	private String m_themeName;

	private String m_stylesheet;

	private List<String> m_searchList = new ArrayList<String>();

	/**
	 * Constructor to create the factory itself.
	 */
	protected FragmentedThemeFactory() {
	}

	/**
	 * Constructor for an instance.
	 * @param da
	 * @param themeName
	 */
	protected FragmentedThemeFactory(DomApplication da, String themeName) {
		m_application = da;
		m_themeName = themeName;
	}

	static public FragmentedThemeFactory getInstance() {
		return INSTANCE;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Factory code.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Create the theme store for the specified theme input string.
	 * @see to.etc.domui.themes.IThemeFactory#getTheme(to.etc.domui.server.DomApplication, java.lang.String)
	 */
	@Override
	public @Nonnull ITheme getTheme(@Nonnull DomApplication da, @Nonnull String themeName) throws Exception {
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
	protected void close() {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Instance creation.									*/
	/*--------------------------------------------------------------*/
	@Nullable
	private RhinoExecutor m_executor;

	/**
	 * Get the shared executor and scope. If it does not yet exist it is created.
	 * @return
	 */
	@Nonnull
	protected RhinoExecutor executor() throws Exception {
		RhinoExecutor executor = m_executor;
		if(null == executor) {
			executor = m_executor = RhinoExecutorFactory.getInstance().createExecutor();
			executor.eval(Object.class, "icon = new Object();", "internal");
			executor.put("themeName", m_themeName);
			executor.put("themePath", "$THEME/" + m_themeName + "/");
			m_application.augmentThemeMap(executor);
		}
		return executor;
	}

	/**
	 * Instance creation.
	 * @return
	 */
	protected ITheme createTheme() throws Exception {
		loadStyleInfo();
		ResourceDependencies rd = m_rdl.createDependencies();
		return new FragmentedThemeStore(m_application, m_stylesheet.getBytes("utf-8"), executor(), m_searchList, rd);
	}

	protected void loadStyleInfo() throws Exception {
		//-- Split theme name into theme/icons/color
		String[] ar = m_themeName.split("\\/");
		if(ar.length != 3)
			throw new StyleException("The theme name '" + m_themeName + "' is invalid for "+getClass()+": expecting theme/icon/color");
		String styleName = ar[0];
		String iconName = ar[1];
		String colorName = ar[2];

		loadColors(colorName);
		loadIcons(iconName);
		loadStyle(styleName);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Stylesheet (css) theme set loading.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Load all theme style (css) related info. The styleName is a folder name inside themes/; that
	 * folder <b>must</b> contain a "style.props.js" property file and contains multiple xxxx.frag.css
	 * files.
	 * @param styleName
	 * @throws Exception
	 */
	protected void loadStyle(String styleName) throws Exception {
		loadClear();
		setInheritence("internalInheritStyle");
		internalInheritStyle(styleName); // Use that same name to load this set.

		//-- Now load all stylesheet fragments (.frag.css)
		StringBuilder sb = new StringBuilder(65536);
		executor().put("browser", BrowserVersion.parseUserAgent("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)"));
		getFragments(sb, m_inheritanceStack, ".frag.css", Check.CHECK, m_rdl);
		m_stylesheet = sb.toString();
	}

	public void internalInheritStyle(String styleName) throws Exception {
		String dirname = normalizeName("themes/" + styleName);

		//-- If already loaded- abort;
		if(m_inheritanceStack.contains(dirname))
			throw new StyleException(m_themeName + ": style set '" + styleName + "' is used before (cyclic loop in styles, or double inheritance)");
		m_inheritanceStack.add(0, dirname);
		m_searchList.add(0, dirname); // Style sets are part of the search path

		//-- The style.props.js file is mandatory at least;
		loadScript("$" + dirname + "/style.props.js");

	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Icon set loading.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Load all icon related info. Each icon set is in a separate folder inside
	 * the "themes" structure; each folder is required to hold icon.props.js and
	 * optional icon resources. Each icon set that is "inherited" becomes part of
	 * the search path, meaning that theme resources will first be located in a
	 * specific icon path before being found in the theme itself.
	 *
	 * <p>Icon sets consist of a set of property file fragments too.</p>
	 *
	 * @param iconName
	 * @throws Exception
	 */
	protected void loadIcons(String iconName) throws Exception {
		loadClear();
		setInheritence("internalInheritIcon");
		internalInheritIcon(iconName); // Use that same name to load this set.

		//-- An icon set can have fragmented properties too - so load those.
		loadFragments("iconset:" + iconName, ".props.js", "icon.props.js");
	}

	/**
	 * Load the icon set specified. The iconName is a simple name; this code will
	 * convert it to a directory name.
	 * @param iconName
	 * @throws Exception
	 */
	public void internalInheritIcon(String iconName) throws Exception {
		String dirname = normalizeName("themes/" + iconName + "-icons");
		//-- If already loaded- abort;
		if(m_inheritanceStack.contains(dirname))
			throw new StyleException(m_themeName + ": icon set '" + iconName + "' is used before (cyclic loop in styles, or double inheritance)");
		m_inheritanceStack.add(0, dirname);
		m_searchList.add(0, dirname); // Icon sets are part of the search path

		//-- The icon.props.js file is mandatory at least;
		loadScript("$" + dirname + "/icon.props.js");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Color set loading.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Load everything related to colors. Results in the current {@link IScriptScope} being
	 * filled with all color properties.
	 *
	 * @param colorName
	 * @throws Exception
	 */
	protected void loadColors(String colorName) throws Exception {
		loadClear();
		setInheritence("internalInheritColor");
		internalInheritColor(colorName); // Use that same name to load this set.
	}

	/**
	 * Load color properties. For a color [x] this loads $themes/[x].color.js. A color set can inherit
	 * from another color set, by
	 * @param colorName
	 */
	public void internalInheritColor(String colorName) throws Exception {
		String fullname = normalizeName("themes/" + colorName + ".color.js").replace("domui.", "orange."); //jsavic 20121107: reported workaround - temporary

		//-- If already loaded- abort;
		if(m_inheritanceStack.contains(fullname))
			throw new StyleException(m_themeName + ": color set '" + colorName + "' is used before (cyclic loop in styles, or double inheritance)");
		m_inheritanceStack.add(0, fullname);
		loadScript("$" + fullname);

		m_searchList.add(0, normalizeName("themes/" + colorName + "-colors"));
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Loading sets of properties and fragments.			*/
	/*--------------------------------------------------------------*/
	/** During load of a set, this defines the inheritance stack of the loaded item. When style a EXTENDS style B etc, this starts with the base class (b) and ends with the topmost one (A). */
	private List<String> m_inheritanceStack = new ArrayList<String>();

	/** During load of a set this collects all dependencies. */
	private ResourceDependencyList m_rdl = new ResourceDependencyList();

	protected ResourceDependencyList getCurrentDependencies() {
		return m_rdl;
	}

	/**
	 * Initialize for loading a new set. Clears all information of the previously loaded set.
	 */
	protected void loadClear() {
		m_inheritanceStack.clear();
	}

	/**
	 * Defines the implementation of the "inherit" method which causes inheritance to work. The
	 * method name must be an accessible method in this class accepting a single String as
	 * parameter.
	 * @param methodname
	 */
	private void setInheritence(String methodname) throws Exception {
		executor().put("collector", this);
		executor().eval(Object.class, "function inherit(s) { collector." + methodname + "(s); }", "internal");
	}

	/**
	 * Load a property set from the given directory name.
	 *
	 * Load a specific theme's style properties. Core part of inherit('') command.
	 * @param dirname
	 * @throws Exception
	 */
	private void loadProperties(String dirname, String filename) throws Exception {
		if(dirname.startsWith("/"))
			dirname = dirname.substring(1);
		if(dirname.endsWith("/"))
			dirname = dirname.substring(0, dirname.length() - 1);
		if(dirname.startsWith("$"))
			dirname = dirname.substring(1);

		//-- If already loaded- abort;
		if(m_inheritanceStack.contains(dirname))
			throw new StyleException(m_themeName + ": inherited set '" + dirname + "' is used before (cyclic loop in styles, or double inheritance)");
		m_inheritanceStack.add(0, dirname); // Insert BEFORE the others (this is a base class for 'm)

		//-- Load the .props.js file which must exist as either resource or webfile.
		String pname = "$" + dirname + "/" + filename;
		loadScript(pname);
	}

	/**
	 * Load the specified resource as a Javascript thingy.
	 * @param pname
	 * @throws Exception
	 */
	private void loadScript(String pname) throws Exception {
		IResourceRef ires = findRef(m_rdl, pname);
		if(null == ires)
			throw new StyleException("The " + pname + " file is not found.");
		InputStream is = ires.getInputStream();
		if(null == is)
			throw new StyleException("The " + pname + " file is not found.");
		System.out.println("css: loading " + pname + " as " + ires);
		try {
			//-- Execute Javascript;
			Reader r = new InputStreamReader(is, "utf-8");
			executor().eval(Object.class, r, pname);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Walks the inheritance stack, and loads all fragments present there as properties too.
	 * @param rx
	 * @throws Exception
	 */
	private void loadFragments(String setname, String fragmentSuffix, String ignoreName) throws Exception {
		long ts = System.nanoTime();

		//-- Find all possible files/resources, then sort them by their name.
		List<String> reslist = collectFragments(m_inheritanceStack, fragmentSuffix);

		//-- Load every one of them as a javascript file.
		int count = 0;
		for(String name : reslist) {
			if(ignoreName != null && name.endsWith("/" + ignoreName))
				continue;
			String full = "$" + name;
			loadScript(full);
			count++;
		}
		ts = System.nanoTime() - ts;
		System.out.println("css: loading " + setname + "+: loaded " + count + " fragments took " + StringTool.strNanoTime(ts));
	}

	private String normalizeName(String name) {
		if(name.startsWith("/"))
			name = name.substring(1);
		if(name.endsWith("/"))
			name = name.substring(0, name.length() - 1);
		if(name.startsWith("$"))
			name = name.substring(1);
		return name;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Stylesheet Properties file loader thingy			*/
	/*--------------------------------------------------------------*/
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
	private void getFragments(StringBuilder target, List<String> directory, String suffix, Check loadType, IResourceDependencyList rdl) throws Exception {
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
			IResourceRef ires = m_application.getResource(rurl, rdl); // Get the source file, abort if not found
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
		File wad = m_application.getAppFile(inh);
		if(wad != null && wad.isDirectory()) {
			for(File far : wad.listFiles()) {
				if(far.isFile())
					nameSet.put(far.getName(), inh);
			}
		}
	}

	protected String getThemeName() {
		return m_themeName;
	}

	protected DomApplication getApplication() {
		return m_application;
	}

	protected String getStylesheet() {
		return m_stylesheet;
	}

	protected List<String> getSearchList() {
		return m_searchList;
	}
}
