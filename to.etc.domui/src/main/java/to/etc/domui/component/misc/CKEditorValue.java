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
	public void webActionDATALOG(@NonNull RequestContextImpl context) throws Exception {
		String m_data = (String) context.getAttribute("DATALOG");
	}

}