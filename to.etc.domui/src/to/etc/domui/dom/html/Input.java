package to.etc.domui.dom.html;

import to.etc.domui.dom.errors.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * The "input" tag as a base class. This one only handles classic, non-image inputs.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class Input extends NodeBase implements IHasChangeListener, INodeErrorDelegate {
	private boolean m_disabled;

	private int m_maxLength;

	private boolean m_readOnly;

	private int m_size;

	private String m_rawValue;

	private String m_onKeyPressJS;

	private IValueChanged< ? > m_onValueChanged;

	private ILookupTypingListener< ? > m_onLookupTyping;

	public Input() {
		super("input");
	}

	/**
	 * Returns the input type= string which defaults to 'text' but which can be changed to 'password' by the HiddenText&lt;T&gt; control.
	 * @return
	 */
	public String getInputType() {
		return "text";
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitInput(this);
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		boolean wasro = m_disabled || m_readOnly;
		m_disabled = disabled;
		boolean isro = m_disabled || m_readOnly;
		if(wasro != isro)
			updateRoStyle();
	}

	public boolean isReadOnly() {
		return m_readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		if(m_readOnly == readOnly)
			return;
		boolean wasro = m_disabled || m_readOnly;
		m_readOnly = readOnly;
		boolean isro = m_disabled || m_readOnly;
		if(wasro != isro)
			updateRoStyle();
	}

	private void updateRoStyle() {
		if(m_disabled || m_readOnly)
			addCssClass("ui-ro");
		else
			removeCssClass("ui-ro");
	}

	public int getMaxLength() {
		return m_maxLength;
	}

	public void setMaxLength(int maxLength) {
		if(m_maxLength != maxLength)
			changed();
		m_maxLength = maxLength;
	}

	public int getSize() {
		return m_size;
	}

	public void setSize(int size) {
		if(m_size != size)
			changed();
		m_size = size;
	}

	public String getRawValue() {
		return m_rawValue;
	}

	public void setRawValue(String value) {
		if(DomUtil.isEqual(value, m_rawValue))
			return;
		changed();
		m_rawValue = value;
	}

	public String getOnKeyPressJS() {
		return m_onKeyPressJS;
	}

	public void setOnKeyPressJS(String onKeyPressJS) {
		if(!DomUtil.isEqual(onKeyPressJS, m_onKeyPressJS))
			changed();
		m_onKeyPressJS = onKeyPressJS;
	}

	/**
	 * The input tag accepts a single value.
	 * @see to.etc.domui.dom.html.NodeBase#acceptRequestParameter(java.lang.String[])
	 */
	@Override
	public boolean acceptRequestParameter(String[] values) {
		String prev = m_rawValue;
		if(values == null || values.length != 1)
			m_rawValue = null;
		else
			m_rawValue = values[0];

		//-- For "changed" determination: treat null and empty string in rawValue the same.
		if((prev == null || prev.length() == 0) && (m_rawValue == null || m_rawValue.length() == 0))
			return false; // Both are "empty" meaning null/""
		return !DomUtil.isEqual(prev, m_rawValue); // Changed if not equal
	}

	/**
	 * The input tag handles {@link Constants#ACMD_LOOKUP_TYPING} and {@link Constants#ACMD_LOOKUP_TYPING_DONE} browser commands.
	 * @see to.etc.domui.dom.html.NodeBase#componentHandleWebAction(to.etc.domui.server.RequestContextImpl, java.lang.String)
	 */
	@Override
	public void componentHandleWebAction(RequestContextImpl ctx, String action) throws Exception {
		if(Constants.ACMD_LOOKUP_TYPING.equals(action)) {
			handleLookupTyping(ctx);
		} else if(Constants.ACMD_LOOKUP_TYPING_DONE.equals(action)) {
			handleLookupTypingDone(ctx);
		}
	}

	/**
	 * Called when the action is a TYPING event on some Input thingy. This causes the onTyping handler for
	 * the input to be called. Typing event is triggered after time delay of 500ms after user has stopped typing.
	 *
	 * @param ctx
	 * @param page
	 * @param cid
	 * @throws Exception
	 */
	private void handleLookupTyping(final IRequestContext ctx) throws Exception {
		ILookupTypingListener<NodeBase> tl = (ILookupTypingListener<NodeBase>) getOnLookupTyping();
		if(tl != null) {
			tl.onLookupTyping(this, false);
		}
	}

	/**
	 * Called when the action is a TYPING DONE event on some Input thingy. This causes the onTyping handler for
	 * the input to be called with parame done set to true. Occurs when user press return key on input with registered onTyping listener.
	 *
	 * @param ctx
	 * @param page
	 * @param cid
	 * @throws Exception
	 */
	private void handleLookupTypingDone(final IRequestContext ctx) throws Exception {
		ILookupTypingListener<NodeBase> tl = (ILookupTypingListener<NodeBase>) getOnLookupTyping();
		if(tl != null) {
			tl.onLookupTyping(this, true);
		}
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#setOnValueChanged(to.etc.domui.dom.html.IValueChanged)
	 */
	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	public ILookupTypingListener< ? > getOnLookupTyping() {
		return m_onLookupTyping;
	}

	public void setOnLookupTyping(ILookupTypingListener< ? > onLookupTyping) {
		m_onLookupTyping = onLookupTyping;
	}

}
