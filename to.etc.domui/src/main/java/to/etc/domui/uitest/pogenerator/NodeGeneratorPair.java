package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.dom.html.NodeBase;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-12-21.
 */
@NonNullByDefault
final public class NodeGeneratorPair {
	private final NodeBase m_node;

	private final IPoProxyGenerator m_generator;

	public NodeGeneratorPair(NodeBase node, IPoProxyGenerator generator) {
		m_node = node;
		m_generator = generator;
	}

	public NodeBase getNode() {
		return m_node;
	}

	public IPoProxyGenerator getGenerator() {
		return m_generator;
	}
}
