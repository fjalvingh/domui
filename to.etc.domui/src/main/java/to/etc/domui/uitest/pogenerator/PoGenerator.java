package to.etc.domui.uitest.pogenerator;

import to.etc.domui.component.layout.MessageLine;
import to.etc.domui.component.misc.ExceptionDialog;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component.misc.MsgBox2.Type;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Pre;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.DeveloperOptions;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
final public class PoGenerator {
	static public void onGeneratePO(UrlPage page) {
		if(!DeveloperOptions.isDeveloperWorkstation())
			return;

		PageObjectGenerator pg;
		String result;
		try {
			pg = new PageObjectGenerator(page);
			result = pg.generateAll(true);
		} catch(Exception x) {
			ExceptionDialog.createIgnore(page, "Failed to generate PO model", x);
			return;
		}

		Div content = new Div();

		for(String s : pg.getContext().getErrorList()) {
			content.add(new MessageLine(MsgType.ERROR, s));
		}

		Pre pre = new Pre();
		content.add(pre);
		pre.add(result);

		MsgBox2.on(page)
			.content(content)
			.type(Type.INFO)
			.title("Test Object Classes")
			.resizable()
			.size(1024, 768)
			;
	}

}
