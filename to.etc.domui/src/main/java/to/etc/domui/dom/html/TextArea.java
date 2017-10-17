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

import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.MetaUtils;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.Msgs;
import to.etc.util.StringTool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class TextArea extends InputNodeContainer implements INativeChangeListener, IControl<String>, IHasModifiedIndication, IHtmlInput {
	/** Hint to use in property meta data to select this component. */
	static public final String HINT = "textarea";

	private int m_cols = -1;

	private int m_rows = -1;

	private String m_value;

	private boolean m_disabled;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	private int	m_maxLength;

	/** Oracle <= 11 has a hard limit of 4000 bytes in a varchar2. TextArea's bound to an Oracle column might need this second limit observed too. This assumes UTF-8 encoding in the database too. */
	private int m_maxByteLength;

	@Nullable
	private String m_disabledBecause;

	public TextArea() {
		super("textarea");
	}

	public TextArea(int cols, int rows) {
		this();
		m_cols = cols;
		m_rows = rows;
	}

	@Nullable @Override public NodeBase getForTarget() {
		return this;
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitTextArea(this);
	}

	public int getCols() {
		return m_cols;
	}

	public void setCols(int cols) {
		if(m_cols == cols)
			return;
		changed();
		m_cols = cols;
	}

	public int getRows() {
		return m_rows;
	}

	public void setRows(int rows) {
		if(m_rows == rows)
			return;
		changed();
		m_rows = rows;
	}

	/**
	 * Bind-capable version of getValue(). If called (usually from binding) this will act as follows:
	 * <ul>
	 * 	<li>If this component has an input error: throw the ValidationException for that error</li>
	 * 	<li>On no error this returns the value.</li>
	 * </ul>
	 * @return
	 */
	@Nullable
	public String getBindValue() {
		validate();												// Validate, and throw exception without UI change on trouble.
		return m_value;
	}

	public void setBindValue(@Nullable String value) {
		if(MetaManager.areObjectsEqual(m_value, value)) {
			return;
		}
		setValue(value);
	}

	private void validate() {

		if(StringTool.isBlank(m_value)) {
			if(isMandatory()) {
				throw new ValidationException(Msgs.MANDATORY);
			}
		}
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#getValue()
	 */
	@Override
	public String getValue() {
		try {
			validate();
			return m_value;
		} catch(ValidationException x) {
			setMessage(UIMessage.error(x));
			throw x;
		}
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#getValueSafe()
	 */
	@Override
	public String getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#hasError()
	 */
	@Override
	public boolean hasError() {
		getValueSafe();
		return super.hasError();
	}


	public String getRawValue() {
		return m_value;
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		changed();
		m_disabled = disabled;
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

	@Override
	public void setValue(@Nullable String v) {
		String value = m_value;
		if(DomUtil.isEqual(v, value))
			return;
		m_value = v;
		setMessage(null);
		setText(v);
	}

	@Override
	public boolean acceptRequestParameter(@Nonnull String[] values) throws Exception {
		if(isDisabled()) {
			return false;
		}

		String nw = (values == null || values.length != 1) ? null : values[0];
		//fixes problem when no data is entered on form and modified flag is raised
		if(nw != null && nw.length() == 0)
			nw = null;
		String cur = m_value != null && m_value.length() == 0 ? null : m_value; // Treat empty string and null the same

		//vmijic 20091124 - some existing entries have \r\n, but after client request roundtrip nw get values with \n instead. Prevent differences being raised because of this.
		if(cur != null) {
			cur = cur.replaceAll("\r\n", "\n");
		}
		//vmijic 20091126 - now IE returns \r\n, but FF returns \n... So, both nw and cur have to be compared with "\r\n" replaced by "\n"...
		String flattenLineBreaksNw = (nw != null) ? nw.replaceAll("\r\n", "\n") : null;

		//vmijic 20101117 - it is discovered (call 28340) that from some reason first \n is not rendered in TextArea on client side in initial page render. That cause that same \n is missing from unchanged text area input that comes through client request roundtrip and cause modified flag to be set... So as dirty fix we have to compare without that starting \n too...
		if(flattenLineBreaksNw != null && cur != null && cur.startsWith("\n") && !flattenLineBreaksNw.startsWith("\n")) {
			cur = cur.substring(1);
		}

		int maxLength = getMaxLength();
		if(maxLength > 0 && nw != null && nw.length() > maxLength)				// Be very sure we are limited even if javascript does not execute.
			nw = nw.substring(0, maxLength);

		int maxBytes = getMaxByteLength();
		if(maxBytes > 0) {
			if(maxLength <= 0)
				maxLength = maxBytes;
			nw = StringTool.strTruncateUtf8Bytes(nw, maxLength, maxBytes);
		}

		if(DomUtil.isEqual(flattenLineBreaksNw, cur))
			return false;

		setValue(nw);
		DomUtil.setModifiedFlag(this);
		return true;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IHasModifiedIndication impl							*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the modified-by-user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#isModified()
	 */
	@Override
	public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
	@Override
	public void setModified(boolean as) {
		m_modifiedByUser = as;
	}

	@Nonnull
	static public TextArea create(@Nonnull PropertyMetaModel< ? > pmm) {
		TextArea ta = new TextArea();
		String cth = pmm.getComponentTypeHint();
		if(cth != null) {
			String hint = cth.toLowerCase();
			ta.setCols(MetaUtils.parseIntParam(hint, MetaUtils.COL, 80));
			ta.setRows(MetaUtils.parseIntParam(hint, MetaUtils.ROW, 4));
		}
		if(pmm.isRequired())
			ta.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			ta.setTitle(s);
		int maxlen = pmm.getLength();
		if(maxlen > 0) {
			ta.setMaxLength(maxlen);
			ta.setMaxByteLength(DomApplication.getPlatformVarcharByteLimit());
		}

		return ta;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		if(readOnly == isReadOnly())
			return;
		super.setReadOnly(readOnly);
		if(isReadOnly())
			addCssClass("ui-textarea-ro");
		else
			removeCssClass("ui-textarea-ro");
	}

	/**
	 * This sets the max input length for the text area, or unlimited when &lt;= 0. This is not
	 * a valid HTML attribute for html < 5; the handling is done in Javascript.
	 * @return
	 */
	public int getMaxLength() {
		return m_maxLength;
	}

	public void setMaxLength(int maxLength) {
		m_maxLength = maxLength;
	}

	/**
	 * When &gt; 0, this sets the max length in UTF-8 bytes for the text area. This is a workaround
	 * for Oracle's 4000 byte varchar2 limit in versions &lt; 12c. When set, the text area will
	 * first apply the limit set by maxLength; if that still delivers more bytes than set in this
	 * property it will limit to the number of bytes here by removing characters.
	 * @return
	 */
	public int getMaxByteLength() {
		return m_maxByteLength;
	}

	public void setMaxByteLength(int maxByteLength) {
		m_maxByteLength = maxByteLength;
	}
}
