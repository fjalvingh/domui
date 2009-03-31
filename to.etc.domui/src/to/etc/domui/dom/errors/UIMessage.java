package to.etc.domui.dom.errors;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.nls.NlsContext;

/**
 * A single error message for a component. The message consists of a message code and optional
 * parameters, and usually hard-refers to the component where the error occured. Global errors
 * exist also; these have a null component and must be shown in whatever error container 
 * exists.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 11, 2008
 */
public class UIMessage {
	/** The error message code for the error that has occured. This exists always and is a lookup into the error NLS messages. */
	private String			m_code;

	private MsgType			m_type;

	/** For errors that have parameters - these are the parameters. This is null if no parameters are present. */
	private Object[]		m_parameters;

	/**
	 * The Node that this error pertains to. It is usually a Component-type node.
	 */
	private NodeBase		m_errorNode;

	public UIMessage(NodeBase errorNode, MsgType type, String code, Object[] parameters) {
		m_errorNode = errorNode;
		m_code = code;
		m_parameters = parameters;
		m_type = type;
	}
	public String getCode() {
		return m_code;
	}
	public Object[] getParameters() {
		return m_parameters;
	}
	public NodeBase getErrorNode() {
		return m_errorNode;
	}
	public MsgType getType() {
		return m_type;
	}
	
	/**
	 * FIXME Must return localized and replaced message.
	 * @return
	 */
	public String	getMessage() {
		return NlsContext.getGlobalMessage(m_code, m_parameters);
	}
}
