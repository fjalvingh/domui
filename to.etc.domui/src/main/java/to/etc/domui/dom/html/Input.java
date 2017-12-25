/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.dom.html;

import to.etc.domui.component.input.Text;
import to.etc.domui.dom.errors.INodeErrorDelegate;
import to.etc.domui.parts.MarkerImagePart;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.util.Constants;
import to.etc.domui.util.DomUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The "input" tag as a base class. This one only handles classic, non-image inputs.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class Input extends NodeBase implements INativeChangeListener, IHasChangeListener, INodeErrorDelegate, IHtmlInput, IForTarget {
	private boolean m_disabled;

	private int m_maxLength;

	private boolean m_readOnly;

	private int m_size;

	private String m_rawValue;

	private String m_onKeyPressJS;

	private IValueChanged< ? > m_onValueChanged;

	private ILookupTypingListener< ? > m_onLookupTyping;

	@Nullable
	private String m_disabledBecause;

	@Nullable
	private String m_placeHolder;

	private String m_emptyMarker;

	private String m_type = "text";

	public Input() {
		super("input");
	}

	/**
	 * Returns the input type= string which defaults to 'text'.
	 */
	public String getInputType() {
		return m_type;
	}

	public void setInputType(String type) {
		if(Objects.equals(type, m_type)) {
			return;
		}
		m_type = type;
		changed();
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitInput(this);
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		boolean wasro = m_disabled || m_readOnly;
		m_disabled = disabled;
		boolean isro = m_disabled || m_readOnly;
		if(wasro != isro)
			updateRoStyle();
		if(! disabled)
			setOverrideTitle(null);
	}

	@Nullable
	public String getDisabledBecause() {
		return m_disabledBecause;
	}

	public void setDisabledBecause(@Nullable String msg) {
		if(Objects.equals(msg, m_disabledBecause)) {
			return;
		}
		m_disabledBecause = msg;
		setOverrideTitle(msg);
		setDisabled(msg != null);
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

	/**
	 * Define a Javascript method to be called for the onkeypress event.
	 * @param onKeyPressJS
	 */
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
	public boolean acceptRequestParameter(@Nonnull String[] values) {
		if(isDisabled()) {
			return false;
		}
		String prev = m_rawValue;
		if(values == null || values.length != 1)
			m_rawValue = null;
		else
			m_rawValue = values[0];

		//-- For "changed" determination: treat null and empty string in rawValue the same.
		if((prev == null || prev.length() == 0) && (m_rawValue == null || m_rawValue.length() == 0))
			return false; 													// Both are "empty" meaning null/""
		return !DomUtil.isEqual(prev, m_rawValue);							// Changed if not equal
	}

	/**
	 * The input tag handles {@link Constants#ACMD_LOOKUP_TYPING} and {@link Constants#ACMD_LOOKUP_TYPING_DONE} browser commands.
	 * @see to.etc.domui.dom.html.NodeBase#componentHandleWebAction(to.etc.domui.server.RequestContextImpl, java.lang.String)
	 */
	@Override
	public void componentHandleWebAction(@Nonnull RequestContextImpl ctx, @Nonnull String action) throws Exception {
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

	/**
	 * Sets the placeholder attribute.
	 * @return
	 */
	@Nullable public String getPlaceHolder() {
		return m_placeHolder;
	}

	public void setPlaceHolder(@Nullable String placeHolder) {
		if(Objects.equals(placeHolder, m_placeHolder))
			return;
		m_placeHolder = placeHolder;
		changed();
	}

	@Nullable @Override public NodeBase getForTarget() {
		return this;
	}

	/**
	 * This sets a marker image to be used as the background image for an empty text box. It should contain the URL to a fully-constructed
	 * background image. To create such an image from an icon plus text use one of the setMarkerXxx methods. This method should be used
	 * only for manually-constructed images.
	 * @param emptyMarker
	 */
	public void setMarkerImage(String emptyMarker) {
		if(DomUtil.isBlank(emptyMarker)) {
			setSpecialAttribute("marker", null);
		} else {
			setSpecialAttribute("marker", emptyMarker);
		}
		m_emptyMarker = emptyMarker;
	}

	/**
	 * Returns assigned empty marker.
	 *
	 * @see Text#setMarkerImage(String)
	 */
	public String getMarkerImage() {
		return m_emptyMarker;
	}


	/**
	 * Method can be used to show default marker icon (THEME/icon-search.png) with magnifier image in background of input. Image is hidden when input have focus or has any content.
	 * @return
	 */
	public void setMarker() {
		setMarkerImage(MarkerImagePart.getBackgroundIconOnly());
	}

	/**
	 * Method can be used to show custom marker icon as image in background of input. Image is hidden when input have focus or has any content.
	 *
	 * @param iconUrl
	 * @return
	 */
	public void setMarker(String iconUrl) {
		setMarkerImage(MarkerImagePart.getBackgroundIconOnly(iconUrl));
	}

	/**
	 * Method can be used to show default marker icon (THEME/icon-search.png) with magnifier and custom label as image in background of input. Image is hidden when input have focus or has any content.
	 *
	 * @param caption
	 * @return
	 */
	public void setMarkerText(String caption) {
		setMarkerImage(MarkerImagePart.getBackgroundImage(caption));
	}

	/**
	 * Method can be used to show custom marker icon and custom label as image in background of input. Image is hidden when input have focus or has any content.
	 *
	 * @param iconUrl
	 * @param caption
	 * @return
	 */
	public void setMarker(String iconUrl, String caption) {
		setMarkerImage(MarkerImagePart.getBackgroundImage(iconUrl, caption));
	}

}
