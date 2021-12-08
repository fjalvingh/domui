package to.etc.domui.uitest.pogenerator;

import to.etc.domui.dom.html.UrlPage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
public class PageObjectGenerator {
	private final PoGeneratorContext m_context;

	public PageObjectGenerator(UrlPage page) {
		m_context = new PoGeneratorContext(page);
	}

	/**
	 * Walks all nodes, and creates proxy generators for every recognized thing in the
	 * tree.
	 */
	public List<IPoProxyGenerator> createGenerators() throws Exception {
		List<IPoProxyGenerator> list = new ArrayList<>();
		m_context.createGenerators(list, m_context.getPage());
		return list;
	}

	public String generateAll() throws Exception {
		List<IPoProxyGenerator> generators = createGenerators();
		for(IPoProxyGenerator generator : generators) {
			generator.prepare(m_context);
		}
		for(IPoProxyGenerator generator : generators) {
			generator.generateCode(m_context);
		}

		PoClassWriter cw = new PoClassWriter();
		List<PoClass> classList = m_context.getClassList();
		for(PoClass poClass : classList) {
			poClass.visit(cw);
		}

		return cw.getResult();
	}
}
