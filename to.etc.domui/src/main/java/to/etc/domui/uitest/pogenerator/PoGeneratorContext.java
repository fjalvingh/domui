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

	public PoGeneratorContext(UrlPage page) {
		m_page = page;

		//-- Create the root class: the class representing this page.
		String pkg = calculatePageTestclassPackageName(page);
		String name = calculatePageTestclassName(page);
		PoClass clz = new PoClass(pkg, name, null, Collections.emptyList());
		m_rootClass = clz;
		m_classList.add(clz);
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
		return pc.getSimpleName() + "PO";
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
}
