package to.etc.domui.component.misc;

import java.util.*;

import to.etc.domui.dom.html.*;

public class MessageFlare extends Flare {
	private List<NodeBase>	m_content = new ArrayList<NodeBase>();

	public MessageFlare() {
		setCssClass("ui-flare ui-flare-msg");
	}

	@Override
	public void createContent() throws Exception {
		Img	img = new Img("THEME/flare-important.png");
		img.setAlign(ImgAlign.LEFT);
		add(img);
		for(NodeBase nb: m_content) {
			add(nb);
		}
	}

	public void addMessage(String message) {
		Div	dmsg = new Div();
		m_content.add(dmsg);
		dmsg.setCssClass("ui-flr-msg");
		dmsg.add(message);
	}

	public static void display(NodeContainer parent, String message) {
		MessageFlare mf = get(parent, MessageFlare.class);
		mf.addMessage(message);
	}
}
