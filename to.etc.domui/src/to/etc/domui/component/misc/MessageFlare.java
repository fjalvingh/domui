package to.etc.domui.component.misc;

import java.util.*;

import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;

public class MessageFlare extends Flare {
	private List<NodeBase>	m_content = new ArrayList<NodeBase>();

	private MsgType m_type;

	public MessageFlare() {
		this(MsgType.ERROR);
	}

	public MessageFlare(MsgType type) {
		m_type = type;
	}

	private void renderType() {
		switch(m_type){
			case ERROR:
				setCssClass("ui-flare ui-flare-error");
				break;
			case INFO:
				setCssClass("ui-flare ui-flare-info");
				break;
			case WARNING:
				setCssClass("ui-flare ui-flare-warning");
				break;
			default:
				throw new IllegalArgumentException("Unknown msg type:" + m_type);
		}
		Img img = null;
		switch(m_type){
			case ERROR:
				img = new Img("THEME/flare-important.png");
				break;
			case INFO:
				img = new Img("THEME/info.png");
				break;
			case WARNING:
				img = new Img("THEME/warning.png");
				break;
			default:
				throw new IllegalStateException("Unknown msg type:" + m_type);
		}
		img.setAlign(ImgAlign.LEFT);
		add(img);
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		renderType();
		for(NodeBase nb: m_content) {
			add(nb);
		}
	}

	public void addMessage(String message) {
		Div	dmsg = new Div();
		m_content.add(dmsg);
		dmsg.add(message);
	}

	public void addMessage(String message, MsgType type) {
		addMessage(message);
		setType(type);
	}

	public void setType(MsgType type) {
		if(m_type != type) {
			m_type = type;
			if(isBuilt()) {
				forceRebuild();
			}
		}
	}

	public static void display(NodeContainer parent, String message) {
		MessageFlare mf = get(parent, MessageFlare.class);
		mf.addMessage(message);
	}

	public static void display(NodeContainer parent, MsgType type, String message) {
		MessageFlare mf = get(parent, MessageFlare.class);
		mf.setType(type);
		mf.addMessage(message);
	}

	public static void display(NodeContainer parent, UIMessage message) {
		display(parent, message.getType(), message.getMessage());
	}
}
