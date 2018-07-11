package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.misc.MsgBox2.Type;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Pre;
import to.etc.domui.trouble.UIMsgException;
import to.etc.domui.trouble.ValidationException;
import to.etc.util.StringTool;
import to.etc.webapp.query.QConcurrentUpdateException;

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
	static public void create(NodeContainer container, @NonNull String message, @NonNull Exception x) throws Exception {
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
		Pre pre = new Pre();
		pre.add(message + "\n" + x.toString() + "\n\n" + sb);

		MsgBox2.on(container)
			.title("An error has occurred")
			.type(Type.ERROR)
			.content(pre)
			//.text(message + "\n" + x.toString() + "\n\n" + sb)
			.modal()
			.size(700, 500)
		;

			//.message(container, Type.ERROR, message + "\n" + x.toString() + "\n\n" + sb);
	}
}
