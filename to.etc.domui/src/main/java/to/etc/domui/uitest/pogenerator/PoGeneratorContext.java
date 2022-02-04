package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This defines the context for generation, and receives all
 * parts that need to be generated.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
public class PoGeneratorContext {
	static public final RefType WDCONNECTOR = new RefType(IPoProxyGenerator.WEBDRIVERPACKAGE, "WebDriverConnector");

	static public final RefType URLPAGE = new RefType(UrlPage.class.getPackageName(), UrlPage.class.getSimpleName());

	//static public final RefType CLASSURLPAGE = new RefType(Class.class.getPackageName(), Class.class.getSimpleName(), "? extends UrlPage");

	private final UrlPage m_page;

	private final RefType m_pageType;

	/** All classes generated within this context */
	private final List<PoClass> m_classList = new ArrayList<>();

	private final PoClass m_rootClass;

	private final PoClass m_emptyClass;

	private final List<String> m_errorList = new ArrayList<>();

	private int m_counter;

	public PoGeneratorContext(UrlPage page) throws Exception {
		m_page = page;

		//-- Create the root class: the class representing this page.
		String pkg = calculatePageTestclassPackageName(page);
		String name = calculatePageTestclassName(page);

		m_pageType = new RefType(page.getClass());

		RefType baseClass = new RefType(IPoProxyGenerator.PROXYPACKAGE, "AbstractCpPage", m_pageType);
		PoClass urlPage = new PoClass(UrlPage.class.getPackageName(), UrlPage.class.getSimpleName());
		//baseClass.addGenericParameter(new RefType(page.getClass()));

		PoClass clz = new PoClass(pkg, name + "Base", baseClass, Collections.emptyList()).generated();
		m_rootClass = clz;
		m_classList.add(clz);

		//-- Imports
		clz.addImport(WDCONNECTOR);
		//clz.addImport(URLPAGE);

		//-- Constructor
		PoMethod cons = clz.addConstructor();
		cons.addParameter(WDCONNECTOR, "connector");
		cons.append("super(connector, " + page.getClass().getCanonicalName() + ".class);").nl();

		//-- The empty class for extending the PO
		PoClass empty = m_emptyClass = new PoClass(pkg, name, clz.asType(), Collections.emptyList());
		m_classList.add(empty);
		empty.addImport(WDCONNECTOR);
		empty.addImport(URLPAGE);

		cons = empty.addConstructor();
		cons.addParameter(WDCONNECTOR, "connector");
		cons.append("super(connector);").nl();
	}

	/**
	 * Walks all nodes, and creates proxy generators for every recognized thing in the
	 * tree.
	 */
	public List<NodeGeneratorPair> createGenerators(NodeContainer nc) throws Exception {
		List<NodeGeneratorPair> list = new ArrayList<>();
		createGenerators(list, nc);
		return list;
	}

	public String getPageName() {
		return m_page.getClass().getSimpleName();
	}

	/**
	 * Recursively walk all children of a node and detect generateable controls.
	 */
	public void createGenerators(List<NodeGeneratorPair> list, NodeContainer nc) throws Exception {
		for(NodeBase nb : nc) {
			IPoProxyGenerator generator = PoGeneratorRegistry.find(this, nb);
			if(generator != null) {
				GeneratorAccepted acceptance = generator.acceptChildren(this);
				if(acceptance == GeneratorAccepted.Accepted) {		// If it wants to let the generator play with its children
					String testID = nb.getTestID();					// For now do not accept things without a testid
					if(null == testID) {
						error("Component with a null testID not generated: " + nb);
					} else {
						list.add(new NodeGeneratorPair(nb, generator));
					}
				} else if(acceptance == GeneratorAccepted.RefusedScanChildren) {
					//-- This generator does not accept. So- scan the subtree for things.
					if(nb instanceof NodeContainer) {
						//-- Nothing here; walk the children.
						createGenerators(list, (NodeContainer) nb);
					}
				}
			} else {
				if(nb instanceof IControl) {				// This should have a proxy
					error("No factory for component: " + nb);
				}
				if(nb instanceof NodeContainer) {
					//-- Nothing here; walk the children.
					createGenerators(list, (NodeContainer) nb);
				}
			}
		}
	}

	static public String calculatePageTestclassPackageName(UrlPage page) {
		Class<? extends UrlPage> pc = page.getClass();
		String packageName = pc.getPackageName();
		return packageName + ".test";
	}

	static public String calculatePageTestclassName(UrlPage page) {
		Class<? extends UrlPage> pc = page.getClass();
		return "PO" + pc.getSimpleName();							// Prefix with PO to not always have them popup if you enter a normal page class.
	}

	public UrlPage getPage() {
		return m_page;
	}

	public PoClass getRootClass() {
		return m_rootClass;
	}

	public List<PoClass> getClassList() {
		return m_classList;
	}

	public void error(String text) {
		m_errorList.add(text);
	}

	public void error(NodeBase b, String text) {
		m_errorList.add(b + ": " + text);
	}


	public List<String> getErrorList() {
		return m_errorList;
	}

	public PoClass addClass(String packageName, String className, @Nullable RefType baseClass, List<RefType> ifaces) {
		PoClass pc = new PoClass(packageName, className, baseClass, ifaces);
		m_classList.add(pc);
		return pc;
	}

	public PoClass addClass(String className, @Nullable RefType baseClass, List<RefType> ifaces) {
		return addClass(m_rootClass.getPackageName(), className, baseClass, ifaces);
	}

	public PoClass addClass(PoClass clz) {
		m_classList.add(clz);
		return clz;
	}

	public int nextCounter() {
		return ++m_counter;
	}

	static public String clean(String str) {
		StringBuilder sb = new StringBuilder(str.length());
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c == '_' || Character.isLetterOrDigit(c)) {
				sb.append(c);
			} else if(c == '-' || c == ' ') {
				sb.append('_');
			}
		}
		return sb.toString();
	}

	static public String methodName(String baseName) {
		if(Character.isUpperCase(baseName.charAt(0)))
			return baseName;
		return Character.toUpperCase(baseName.charAt(0)) + baseName.substring(1);
	}

	static public String propertyName(String baseName) {
		if(Character.isLowerCase(baseName.charAt(0)))
			return baseName;
		StringBuilder sb = new StringBuilder(baseName.length());
		for(int i = 0; i < baseName.length(); i++) {
			char c = baseName.charAt(i);
			if(Character.isLowerCase(c)) {
				sb.append(baseName.substring(i));
				break;
			}
			sb.append(Character.toLowerCase(c));
		}
		return sb.toString();
	}

	static public String fieldName(String baseName) {
		return "m_" + propertyName(baseName);
	}

	static public String removeUnderscores(String s) {
		StringBuilder sb = new StringBuilder(s.length());
		boolean upcase = false;
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c == '_') {
				upcase = true;
			} else if(upcase) {
				sb.append(Character.toUpperCase(c));
				upcase = false;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	static public String makeNameValid(String in) {
		if(in.length() == 0)
			throw new IllegalStateException("Empty name not supported");
		if(! Character.isJavaIdentifierStart(in.charAt(0))) {
			in = "c" + in;
		}
		return in;
	}

	static public String makeName(String from) {
		return makeNameValid(removeUnderscores(clean(from)));
	}
}
