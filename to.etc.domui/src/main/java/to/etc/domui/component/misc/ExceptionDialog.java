package to.etc.domui.component.misc;

import to.etc.domui.component.misc.MsgBox.Type;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.trouble.UIMsgException;
import to.etc.domui.trouble.ValidationException;
import to.etc.util.StringTool;
import to.etc.webapp.query.QConcurrentUpdateException;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-1-18.
 */
final public class ExceptionDialog {
	private ExceptionDialog() {
	}

	/**
	 * Show an exception as an error dialog.
	 */
	static public void create(NodeContainer container, @Nonnull String message, @Nonnull Exception x) throws Exception {
		if(x instanceof ValidationException)
			return;
		if(x instanceof QConcurrentUpdateException) {
			MsgBox.error(container, "Concurrent update: " + x);
			return;
		}
		if(x instanceof UIMsgException) {
			UIMsgException msgExc = (UIMsgException) x;
			MsgBox.message(container, msgExc);
			return;
		}

		x.printStackTrace();
		StringBuilder sb = new StringBuilder();
		StringTool.strStacktrace(sb, x);
		MsgBox.message(container, Type.ERROR, message + "\n" + x.toString() + "\n\n" + sb);
	}
}
