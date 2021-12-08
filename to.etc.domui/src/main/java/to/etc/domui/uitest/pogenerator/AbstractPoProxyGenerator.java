package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.dom.html.NodeBase;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
@NonNullByDefault
abstract public class AbstractPoProxyGenerator implements IPoProxyGenerator {
	protected final NodeBase m_node;

	private IPoSelector m_selector;

	public AbstractPoProxyGenerator(NodeBase node) {
		m_node = node;
		m_selector = new PoSelectorTestId(node.getTestID());
	}

	@Override
	public boolean acceptChildren(PoGeneratorContext ctx) throws Exception {
		return true;
	}

	@Override
	public void prepare(PoGeneratorContext context) throws Exception {
	}

	@Override
	public void updateSelector(IPoSelector selector) {
		m_selector = selector;
	}

	public IPoSelector getSelector() {
		return m_selector;
	}
}
