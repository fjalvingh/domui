package to.etc.domui.component.misc;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;

public class DebugWindow extends Window {
	public DebugWindow() {
		super(false, true, "Debug");
	}

	private Div m_log;

	@Override
	public void createContent() throws Exception {
		Div cont = new Div();
		add(cont);
		cont.setMinWidth("512px");
		cont.setWidth("auto");
		cont.setHeight("250px");
		cont.setOverflow(Overflow.AUTO);
		m_log = new Div();
		cont.add(m_log);
	}

	private void logLine(String line) {
		Div l = new Div();
		l.setText(line);
		m_log.add(l);
	}

	static public void log(NodeBase pageNode, String text) {
		if(!pageNode.isAttached())
			return;
		DebugWindow w = (DebugWindow) pageNode.getPage().getConversation().getAttribute("DebugWindow");
		if(null == w) {
			w = new DebugWindow();
			pageNode.getPage().getBody().add(0, w);
			pageNode.getPage().getConversation().setAttribute("DebugWindow", w);
			try {
				w.build();
			} catch(Exception xx) {
				xx.printStackTrace();
			}
		}

		w.logLine(text);
	}

}
