package to.etc.domui.themes;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.script.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.resources.*;

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
	}

	/**
	 * Load the properties for the current style *and it's base styles*. After this, the style sheet
	 * property files have executed in the proper order, and the context contains the proper properties.
	 */
	public void loadStyleProperties() throws Exception {
		loadProperties(m_name);

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
			executeWithGlobals(r);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	private void	executeWithGlobals(Reader r) throws Exception {
		m_engine.eval(r);
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
	/**
	 * Walk the inheritance tree from baseclass to superclass, and collect
	 * all fragments by name; in the process remove all duplicates.
	 * @throws Exception
	 */
	private void collectFragments() throws Exception {
		for(String inh : m_inheritanceStack)
			collectFragments(inh);
	}

	/**
	 * Scan the specified name as a directory, and locate all *.frag.css files in first
	 * the classpath, then the webapp's files directory.
	 * @param inh
	 */

	private void collectFragments(String inh) {
		Package p = Package.getPackage("resources." + inh.replace('/', '.'));
		if(p != null) {

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
