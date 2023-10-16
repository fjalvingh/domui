package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.Div;
import to.etc.domui.server.RequestContextImpl;

/**
 * @package
 * @copyright 2023 Bloomville
 * @author <a href="mailto:marc.mol@bloomville.nl">Marc Mol</a>
 * @license Commercial
 */
public class CKEditorValue extends Div {

	private String m_data;

	public CKEditorValue(String data) {
		m_data = data;
	}

	public String getData() {
		return m_data;
	}

	public void webActionDATALOG(@NonNull RequestContextImpl context) throws Exception {
		m_data = context.getPageParameters().getString("input");
	}

}