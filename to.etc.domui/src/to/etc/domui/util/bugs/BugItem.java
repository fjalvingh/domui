package to.etc.domui.util.bugs;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * A single message reported through the bugs thing.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2010
 */
final public class BugItem {
	final private Date m_timestamp = new Date();

	@Nonnull
	private String m_message;

	@Nullable
	private Throwable m_exception;

	@Nonnull
	private Exception m_location;

	@Nullable
	private List<NodeBase> m_formattedMsg;

	private int m_number;

	public BugItem(@Nonnull String message) {
		m_message = message;
		initLocation();
	}

	public BugItem(List<NodeBase> msg) {
		m_formattedMsg = msg;
		m_exception = null;
		initLocation();
		StringBuilder sb = new StringBuilder();
		flatten(sb, msg);
		m_message = sb.toString();
	}

	static private void flatten(StringBuilder sb, List<NodeBase> msg) {
		for(NodeBase b : msg) {
			flatten(sb, b);
		}
	}

	private static void flatten(StringBuilder sb, NodeBase b) {
		if(b instanceof TextNode)
			sb.append(((TextNode) b).getText());
		else if(b instanceof NodeContainer) {
			for(NodeBase cb : ((NodeContainer) b))
				flatten(sb, cb);
		}
	}

	public BugItem(@Nonnull String message, @Nullable Throwable exception) {
		m_message = message;
		m_exception = exception;
		initLocation();
	}

	private void initLocation() {
		try {
			throw new Exception("duh");
		} catch(Exception x) {
			m_location = x;
		}
	}

	@Nonnull
	public Date getTimestamp() {
		return m_timestamp;
	}

	@Nonnull
	public String getMessage() {
		return m_message;
	}

	@Nullable
	public Throwable getException() {
		return m_exception;
	}

	@Nonnull
	public Exception getLocation() {
		return m_location;
	}

	public int getNumber() {
		return m_number;
	}

	public void setNumber(int number) {
		m_number = number;
	}

	public List<NodeBase> getFormattedMsg() {
		return m_formattedMsg;
	}
}
