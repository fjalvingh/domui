package to.etc.domui.component.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.layout.Window;
import to.etc.domui.dom.css.Overflow;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeBase;

public class DebugWindow extends Window {
	static private final Logger LOG = LoggerFactory.getLogger(DebugWindow.class);

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
				LOG.error("Failed to create DebugWindow", xx);
			}
		}

		w.logLine(text);
	}

}
