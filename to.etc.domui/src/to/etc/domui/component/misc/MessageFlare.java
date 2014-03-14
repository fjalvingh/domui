package to.etc.domui.component.misc;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.themes.*;

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
				img = new Img(Theme.ICON_MBX_ERROR);
				break;
			case INFO:
				img = new Img(Theme.ICON_MBX_INFO);
				break;
			case WARNING:
				img = new Img(Theme.ICON_MBX_WARNING);
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
	 * Adds message to message flare. If multiple messages are added, the "severest" type will be added.
	 * @param message new message
	 * @param type flare type
	 */
	public void addMessage(@Nonnull String message, @Nonnull MsgType type) {
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
