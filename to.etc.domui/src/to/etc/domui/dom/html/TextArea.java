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

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;

import javax.annotation.*;

public class TextArea extends InputNodeContainer implements INativeChangeListener, IControl<String>, IHasModifiedIndication, IHtmlInput {
	/** Hint to use in property meta data to select this component. */
	static public final String HINT = "textarea";

	private int m_cols = -1;

	private int m_rows = -1;

	private String m_value;

	private boolean m_disabled;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	public TextArea() {
		super("textarea");
	}

	public TextArea(int cols, int rows) {
		this();
		m_cols = cols;
		m_rows = rows;
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

	public boolean validate() {
		if(StringTool.isBlank(m_value)) {
			if(isMandatory()) {
				setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY));
				return false;
			}
		}
		clearMessage();
		return true;
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#getValue()
	 */
	@Override
	public String getValue() {
		if(!validate())
			throw new ValidationException(Msgs.NOT_VALID, m_value);
		return m_value;
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
		fireModified("disabled", Boolean.valueOf(!disabled), Boolean.valueOf(disabled));
	}

	@Override
	public void setValue(@Nullable String v) {
		String value = m_value;
		if(DomUtil.isEqual(v, value))
			return;
		m_value = v;
		setText(v);
		fireModified("value", value, v);
	}

	@Override
	public boolean acceptRequestParameter(@Nonnull String[] values) throws Exception {
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
		return ta;
	}
}
