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

import java.util.*;

import to.etc.domui.util.*;

/**
 *
 * @author <a href="mailto:jo.seaton@itris.nl">Jo Seaton</a>
 * Created on Aug 20, 2008
 */

public class RadioButton extends NodeBase implements IHasModifiedIndication {
	//public class RadioButton extends NodeContainer {

	private boolean m_checked;

	private boolean m_disabled;

	private boolean m_readOnly;

	private String m_name;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	public RadioButton() {
		super("input");
	}

	public RadioButton(String name) {
		super("input");
		m_name = name;
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitRadioButton(this);
	}

	public String getName() {
		return m_name;
	}

	public void setName(String s) {
		m_name = s;
	}

	public boolean isChecked() {
		return m_checked;
	}

	public void setChecked(boolean checked) {
		if(m_checked != checked)
			changed();
		m_checked = checked;
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		if(m_disabled != disabled)
			changed();
		m_disabled = disabled;
	}

	public boolean isReadOnly() {
		return m_readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		if(m_readOnly != readOnly)
			changed();
		m_readOnly = readOnly;
	}

	@Override
	public boolean acceptRequestParameter(String[] values) {
		if(values == null || values.length != 1)
			throw new IllegalStateException("RadioButton: expecting a single input value, not " + Arrays.toString(values));

		//		System.out.println("Value=" + values[0]);
		String s = values[0].trim();
		boolean on = "y".equalsIgnoreCase(s);
		if(on == m_checked)
			return false; // Unchanged
		DomUtil.setModifiedFlag(this);
		m_checked = on;
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

}
