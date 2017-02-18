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

abstract public class InputNodeContainer extends NodeContainer implements IHasChangeListener {
	private IValueChanged< ? > m_onValueChanged;

	private boolean m_readOnly;

	private boolean m_mandatory;

	private boolean m_immediate;

	@Override
	abstract public void visit(INodeVisitor v) throws Exception;

	public InputNodeContainer(String tag) {
		super(tag);
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		IValueChanged< ? > vc = m_onValueChanged;
		if(null == vc && isImmediate()) {
			return IValueChanged.DUMMY;
		}
		return vc;
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#setOnValueChanged(to.etc.domui.dom.html.IValueChanged)
	 */
	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	protected void callOnValueChanged() throws Exception {
		if(m_onValueChanged != null) {
			IValueChanged<InputNodeContainer> vc = (IValueChanged<InputNodeContainer>) m_onValueChanged;
			vc.onValueChanged(this);
		}
	}

	public boolean isReadOnly() {
		return m_readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		if(m_readOnly == readOnly)
			return;
		m_readOnly = readOnly;
		changed();
	}

	public boolean isMandatory() {
		return m_mandatory;
	}

	public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;
	}

	public boolean isImmediate() {
		return m_immediate;
	}

	public void immediate(boolean immediate) {
		m_immediate = immediate;
	}

	public void immediate() {
		m_immediate = true;
	}
}
