package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.themes.Theme;

import java.util.ArrayList;
import java.util.List;

/**
 * Message flare component. Shows message flare on screen.
 * If defined as auto vanish, message disappears automatically, otherwise it disappears on any user mouse move.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 2 Sep 2011
 */
public class MessageFlare extends Flare {
	private List<NodeBase>	m_content = new ArrayList<NodeBase>();

	private MsgType m_type;

	/**
	 * Create message flare of ERROR type.
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
	 * @param autoVanish If T, message would stay on screen until any user mouse move. See {@link Flare#setAutoVanish(boolean)}
	 */
	public MessageFlare(MsgType type, boolean autoVanish) {
		m_type = type;
		setAutoVanish(autoVanish);
	}

	@Override
	public void createContent() throws Exception {
		//Div content = new Div("ui-flare-content");
		//add(content);
		//
		renderType(this);
		Div msgContent = new Div("ui-flare-txt");
		add(msgContent);
		for(NodeBase nb: m_content) {
			msgContent.add(nb);
		}
	}

	private void renderType(Div content) {
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
		IIconRef img = null;
		switch(m_type){
			case ERROR:
				img = Theme.ICON_MBX_ERROR;
				break;
			case INFO:
				img = Theme.ICON_MBX_INFO;
				break;
			case WARNING:
				img = Theme.ICON_MBX_WARNING;
				break;
			default:
				throw new IllegalStateException("Unknown msg type:" + m_type);
		}
		Div d = new Div("ui-flare-img");
		content.add(d);
		NodeBase icon = img.createNode();
		d.add(icon);
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
	 * Adds message to message flare. If multiple messages are added, the "severest" type will be added.
	 * @param message new message
	 * @param type flare type
	 */
	public void addMessage(@NonNull String message, @NonNull MsgType type) {
		addMessage(message);
		if(m_type == null || type.getOrder() > m_type.getOrder()) // Set highest severity.
			setType(type);
	}

	/**
	 * Add a {@link UIMessage} to the flare. If multiple messages are added, the "severest" type will be added.
	 * @param uim
	 */
	public void addMessage(UIMessage uim) {
		addMessage(uim.getMessage(), uim.getType());
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
	 * @return created {@link MessageFlare} instance.
	 */
	public static MessageFlare display(NodeContainer parent, String message) {
		MessageFlare mf = get(parent, MessageFlare.class);
		mf.addMessage(message);
		return mf;
	}

	/**
	 * Display flare message.
	 * @param parent
	 * @param type @see {@link MessageFlare#setType(MsgType)}
	 * @param message
	 * @return created {@link MessageFlare} instance.
	 */
	public static MessageFlare display(NodeContainer parent, MsgType type, String message) {
		MessageFlare mf = get(parent, MessageFlare.class);
		mf.setType(type);
		mf.addMessage(message);
		return mf;
	}

	/**
	 * Display flare message. Add localized message string and type from specified {@link UIMessage}.
	 * @param parent
	 * @param message
	 * @return created {@link MessageFlare} instance.
	 */
	public static MessageFlare display(NodeContainer parent, UIMessage message) {
		return display(parent, message.getType(), message.getMessage());
	}
}
