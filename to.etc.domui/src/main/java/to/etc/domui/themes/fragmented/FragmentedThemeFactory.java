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
package to.etc.domui.themes.fragmented;

import to.etc.domui.server.*;
import to.etc.domui.themes.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.js.*;
import to.etc.domui.util.resources.*;
import to.etc.util.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

import static to.etc.domui.util.DomUtil.*;

/**
 * <p>This theme factory uses fragments to create a "theme". A DomUI theme consists of the following:
 * <ul>
 *	<li>A style sheet containing all css for all components.</li>
 *	<li>A set of icons that are used by that stylesheet</li>
 	<li>A color definition consisting of icons and .js files that contain stylesheet resources of a given color.</li>
 *	<li>A variant that allows addressing a separate stylesheet for the theme.</li>
 * </ul>
 *
 * All of these together form a "theme instance" (an ITheme).</p>
 *
 * <p>A theme is identified by 4 strings, one for each components: a style name, a iconset name, a color name and a variant name.
 * The full theme is then described as a string: style/iconset/color/variant. An example theme instance is: "domui/domui/orange/default".
 * This four-part string is the <i>theme name</i>. This factory is used to instantiate a theme instance for a specific "theme name".</p>.
 *
 * <p>The first three parts of the theme name come from an application-level global setting: the string set as {@link DomApplication#setDefaultThemeName(String)}.
 * This defines the "global" theme for the application. The last part, the variant, is provided by the page itself: it allows a page to specify a different
 * version for the master stylesheet. The use case for this is migration to different style sheets if an older one has problems; by creating pages that
 * refer to a new sheet we can change the stylesheet without regression risk for existing pages.</p>
 *
 * <p>The different parts of the theme are constructed as follows:</p>
 * <ul>
 *	<li>A 'style name' (s) which identifies a directory with the name 'themes/css-{s}' containing *.frag.css files and some .js files.
 *		The files in here form the master stylesheet by concatenating themselves in alphabetic order.</li>
 *	<li>The variant name is added to the style name except if the variant is "default". So for a variant 'clean' the complete style name
 *		would be domui-clean, and the directory for it's css files would be css-domui-clean.</li>
 * </ul>
 *
 * This class collects all ".frag.css" files in the specified
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
@DefaultNonNull
public class FragmentedThemeFactory {
	static private final IThemeFactory INSTANCE = new IThemeFactory() {
		@Nonnull
		@Override
		public ITheme getTheme(@Nonnull DomApplication da, @Nonnull String themeName) throws Exception {
			FragmentedThemeFactory stf = new FragmentedThemeFactory(da, themeName);
			return stf.createTheme();
		}

		@Nonnull @Override public String getFactoryName() {
			return "fragmented";
		}

		@Nonnull @Override public String getDefaultThemeName() {
			return getFactoryName() + "-domui-orange-domui";
		}
	};

	final private DomApplication m_application;

	final private String m_themeName;

	@Nullable
	private String m_stylesheet;

	final private List<String> m_searchList = new ArrayList<>();

	/*--------------------------------------------------------------*/
	/*	CODING:	Factory code.										*/
	/*--------------------------------------------------------------*/
	static public IThemeFactory getInstance() {
		return INSTANCE;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Instance creation.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Constructor for a factory instance that will generate the ITheme.
	 */
	protected FragmentedThemeFactory(DomApplication da, String themeName) {
		m_application = da;
		m_themeName = themeName;
	}

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

		m_searchList.add("$themes/css-all");					// 20130327 jal the "all" theme dir contains stuff shared over all themes.
		return new FragmentedThemeStore(m_application, m_themeName, nullChecked(m_stylesheet).getBytes("utf-8"), executor(), m_searchList, rd);
	}

	protected void loadStyleInfo() throws Exception {
		//-- Split theme name into css/icons/color
		String[] ar = m_themeName.split("-");
		if(ar.length != 4 && ar.length != 5)
			throw new StyleException("The theme name '" + m_themeName + "' is invalid for "+getClass()+": expecting styleName/icon/color/variant");
		String styleName = ar[1];
		String iconName = ar[2];
		String colorName = ar[3];
		String variant = ar.length == 4 ? DefaultThemeVariant.INSTANCE.getVariantName() : ar[4];

		loadColors(colorName);
		loadIcons(iconName, variant);
		loadStyle(styleName, variant);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Stylesheet (css) theme set loading.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Load all theme style (css) related info. The styleName is a folder name inside themes/; that
	 * folder <b>must</b> contain a "style.props.js" property file and contains multiple xxxx.frag.css
	 * files.
	 * @param styleName
	 * @param variant
	 * @throws Exception
	 */
	protected void loadStyle(String styleName, String variant) throws Exception {
		if(! variant.equals(DefaultThemeVariant.INSTANCE.getVariantName()))
			styleName += "-" + variant;

		loadClear();
		setInheritence("internalInheritStyle");
		internalInheritStyle(styleName); 						// Use that same name to load this set.

		//-- Now load all stylesheet fragments (.frag.css)
		StringBuilder sb = new StringBuilder(65536);
		executor().put("browser", BrowserVersion.parseUserAgent("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)"));
		getFragments(sb, m_inheritanceStack, ".frag.css", Check.CHECK, m_rdl);
		m_stylesheet = sb.toString();
	}

	public void internalInheritStyle(String styleName) throws Exception {
		String dirname = normalizeName("themes/css-" + styleName);

		//-- If already loaded- abort;
		if(m_inheritanceStack.contains(dirname))
			throw new StyleException(m_themeName + ": style set '" + styleName + "' is used before (cyclic loop in styles, or double inheritance)");
		m_inheritanceStack.add(0, dirname);
		m_searchList.add(0, dirname); 							// Style sets are part of the search path

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
	 *
	 * @param variant
	 * @param iconName
	 * @throws Exception
	 */
	protected void loadIcons(String iconName, String variant) throws Exception {
		loadClear();
		setInheritence("internalInheritIcon");

		if(! DefaultThemeVariant.INSTANCE.getVariantName().equals(variant)) {
			String dirname = normalizeName("themes/" + iconName + "-icons-" + variant);
			if(m_inheritanceStack.contains(dirname))
				throw new StyleException(m_themeName + ": icon variant set '" + dirname + "' is used before (cyclic loop in styles, or double inheritance)");
			m_inheritanceStack.add(0, dirname);
			m_searchList.add(0, dirname);
		}
		internalInheritIcon(iconName);						// Use that same name to load this set.

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
		m_searchList.add(0, dirname); 							// Icon sets are part of the search path

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
		internalInheritColor(colorName); 						// Use that same name to load this set.
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
	/*	CODING:	Fragment collector.									*/
	/*--------------------------------------------------------------*/
	/**
	 * The type of fragment expansion/check to do.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jan 7, 2011
	 */

	private enum Check {
		/** Do not check at all: just concatenate the fragments. */
		NONE

		/** Try to expand every fragment by calling the template expander, but use original source as result, not the expanded template */
		, CHECK
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
		System.out.println("css: theme '" + m_themeName + "' loading " + directory + "+: loaded " + count + " fragments took " + StringTool.strNanoTime(ts));
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
			switch(loadType){
				default:
					throw new IllegalStateException("Bad?");
				case NONE:
					target.append(source);
					return;
				case CHECK:
					target.append(source);
					tmpl.execute(new StringBuilder(), executor().newScope());
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

	@Nullable
	protected String getStylesheet() {
		return nullChecked(m_stylesheet);
	}

	protected List<String> getSearchList() {
		return m_searchList;
	}
}
