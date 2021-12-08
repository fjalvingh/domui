package to.etc.domui.uitest.pogenerator;

import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.uitest.pogenerator.PoGeneratorContext.NameMode;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
public class PogButton extends AbstractPoProxyGenerator implements IPoProxyGenerator {
	private final NodeBase m_node;

	public PogButton(NodeBase node) {
		m_node = node;
	}

	@Override
	public void generateCode(PoGeneratorContext context) throws Exception {
		PoClass rc = context.getRootClass();

		String testID = m_node.getTestID();
		String fieldName = context.getNameFromTestID(testID, NameMode.FIELD);

		rc.addField(PROXYPACKAGE, "ButtonPO", fieldName);
	}
}
