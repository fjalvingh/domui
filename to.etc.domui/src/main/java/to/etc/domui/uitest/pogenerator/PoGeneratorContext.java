package to.etc.domui.uitest.pogenerator;

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
	private final UrlPage m_page;

	/** All classes generated within this context */
	private final List<PoClass> m_classList = new ArrayList<>();

	private final PoClass m_rootClass;

	private final PoClass m_emptyClass;

	public PoGeneratorContext(UrlPage page) {
		m_page = page;

		//-- Create the root class: the class representing this page.
		String pkg = calculatePageTestclassPackageName(page);
		String name = calculatePageTestclassName(page);
		PoClass clz = new PoClass(pkg, name + "Base", null, Collections.emptyList()).generated();
		m_rootClass = clz;
		m_classList.add(clz);

		//-- The empty class for extending the PO
		m_emptyClass = new PoClass(pkg, name, clz, Collections.emptyList());
		m_classList.add(m_emptyClass);
	}

	/**
	 * Recursively walk all children of a node and detect generateable controls.
	 */
	public void createGenerators(List<IPoProxyGenerator> list, NodeContainer nc) throws Exception {
		for(NodeBase nb : nc) {
			IPoProxyGenerator generator = PoGeneratorRegistry.find(this, nb);
			if(generator != null) {
				list.add(generator);
				generator.acceptChildren();                // If it wants to let the generator play with its children
			} else if(nb instanceof NodeContainer) {
				//-- Nothing here; walk the children.
				createGenerators(list, (NodeContainer) nb);
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

	static public String clean(String str) {
		return str
			.replace(" ", "_")
			.replace("-", "_")
			.replace("=", "")
			.replace("(", "")
			.replace(")", "")
			.replace("*", "")
			.replace(";", "")
			.replace("/", "")
			.replace("\\", "");
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

}
