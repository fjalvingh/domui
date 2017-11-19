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

/**
 * This is DIV that is used as user input modified flag fence.
 * Usually it is used to ignore input controls modification in some screen region.
 * When components are used for read only data presentation purposes (like viewers of selection filters)
 * those components does not modify any page value content.
 *
 * @author <a href="mailto:imilovanovic@execom.eu">Igor Milovanović</a>
 * Created on Dec 8, 2009
 */
public class ModificationFenceDiv extends Div implements IUserInputModifiedFence {

	private boolean m_finalUserInputModifiedFence = true;

	private boolean m_modified;

	private boolean m_ignoreModifiedInputs = true;

	/**
	 * Indicates wether component keep tracks on its childs modifications.
	 * By default set to true.
	 * @return
	 */
	public boolean isIgnoreModifiedInputs() {
		return m_ignoreModifiedInputs;
	}

	public void setIgnoreModifiedInputs(boolean ignoreModifiedInputs) {
		m_ignoreModifiedInputs = ignoreModifiedInputs;
	}

	/**
	 * @see to.etc.domui.dom.html.IUserInputModifiedFence#isFinalUserInputModifiedFence()
	 * By default set to true.
	 */
	@Override
	public boolean isFinalUserInputModifiedFence() {
		return m_finalUserInputModifiedFence;
	}

	/**
	 * @see to.etc.domui.dom.html.IUserInputModifiedFence#isFinalUserInputModifiedFence()
	 */
	public void setFinalUserInputModifiedFence(boolean finalUserInputModifiedFence) {
		m_finalUserInputModifiedFence = finalUserInputModifiedFence;
	}

	@Override
	public void onModifyFlagRaised() {
	//by default do nothing
	}

	@Override
	public boolean isModified() {
		return !m_ignoreModifiedInputs && m_modified;
	}

	@Override
	public void setModified(boolean as) {
		m_modified = as;
	}

}
