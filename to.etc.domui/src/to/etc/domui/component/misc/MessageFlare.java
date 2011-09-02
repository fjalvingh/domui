package to.etc.domui.component.misc;

import java.util.*;

import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;

/**
 * Message flare component. Shows message flare on screen.
 * If defined as auto vanish, message disapears automatically, otherwise it disapears on any user mouse move.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 2 Sep 2011
 */
public class MessageFlare extends Flare {
	private List<NodeBase>	m_content = new ArrayList<NodeBase>();

	private MsgType m_type;

	/**
	 * Create message flare of {@link MsgType.ERROR} type.
	 * Message would stay on screen until any user mouse move.
	 * To make it autoVanish or of other type, use {@link MessageFlare#MessageFlare(MsgType, boolean)}
	 */
	public MessageFlare() {
		this(MsgType.ERROR);
	}

	/**
	 * Create message flare. Message would stay on screen until any user mouse move. To make it autoVanish use {@link MessageFlare#MessageFlare(MsgType, boolean)}
	 * @param type Type of message. See {@link MsgType}.
	 */
	public MessageFlare(MsgType type) {
		this(type, false);
	}

	/**
	 * Create message flare.
	 * @param type Type of message. See {@link MsgType}.
	 * @param stay If T, message would stay on screen until any user mouse move. See {@link Flare#setAutoVanish(boolean)}
	 */
	public MessageFlare(MsgType type, boolean autoVanish) {
		m_type = type;
		setAutoVanish(autoVanish);
	}

	private void renderType() {
		switch(m_type){
			case ERROR:
				setCssClass("ui-flare ui-flare-msg ui-flare-error");
				break;
			case INFO:
				setCssClass("ui-flare ui-flare-msg ui-flare-info");
				break;
			case WARNING:
				setCssClass("ui-flare ui-flare-msg ui-flare-warning");
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
		Div msgContent = new Div(); //extra div container to enable text alignment right
		add(msgContent);
		for(NodeBase nb: m_content) {
			msgContent.add(nb);
		}
	}

	/**
	 * Adds message to message flare.
	 * @param message new message
	 */
	public void addMessage(String message) {
		Div	dmsg = new Div();
		m_content.add(dmsg);
		dmsg.add(message);
	}

	/**
	 * Adds message to message flare.
	 * @param message new message
	 * @param type flare type
	 */
	public void addMessage(String message, MsgType type) {
		addMessage(message);
		setType(type);
	}

	/**
	 * Set flare type. See {@link MsgType}.
	 * @param type
	 */
	public void setType(MsgType type) {
		if(m_type != type) {
			m_type = type;
			if(isBuilt()) {
				forceRebuild();
			}
		}
	}

	/**
	 * Display flare message.
	 * @param parent
	 * @param message
	 */
	public static void display(NodeContainer parent, String message) {
		MessageFlare mf = get(parent, MessageFlare.class);
		mf.addMessage(message);
	}

	/**
	 * Display flare message.
	 * @param parent
	 * @param type @see {@link MessageFlare#setType(MsgType)}
	 * @param message
	 */
	public static void display(NodeContainer parent, MsgType type, String message) {
		MessageFlare mf = get(parent, MessageFlare.class);
		mf.setType(type);
		mf.addMessage(message);
	}

	/**
	 * Display flare message. Add localized message string and type from specified {@link UIMessage}.
	 * @param parent
	 * @param message
	 */
	public static void display(NodeContainer parent, UIMessage message) {
		display(parent, message.getType(), message.getMessage());
	}
}
