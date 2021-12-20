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

	public AbstractPoProxyGenerator(NodeBase node) {
		m_node = node;
	}

	@Override
	public GeneratorAccepted acceptChildren(PoGeneratorContext ctx) throws Exception {
		return GeneratorAccepted.Accepted;
	}

	@Override
	public void prepare(PoGeneratorContext context) throws Exception {
	}
}
