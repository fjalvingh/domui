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

import to.etc.function.IExecute;


/**
 * DomUI nodes that have a change listener.
 * <p>20091120 jal This originally extended INodeErrorDelegate; I removed this because these have nothing to do with each other.</p>
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 4 Sep 2009
 */
public interface IHasChangeListener {
	IValueChanged< ? > getOnValueChanged();

	void setOnValueChanged(IValueChanged< ? > onValueChanged);

	/**
	 * Set the change handler as an action that does not need the component: the normal way to
	 * respond to a change, because the control is a local variable of createContent() that the
	 * lambda already captures.
	 */
	default void setOnValueChanged(IExecute onValueChanged) {
		setOnValueChanged(IValueChanged.wrap(onValueChanged));
	}

	/**
	 * Remove the change handler set on this component, if any.
	 */
	default void clearOnValueChanged() {
		setOnValueChanged((IValueChanged< ? >) null);
	}

	/**
	 * Call the change handler, if one is set. For component implementations: this is how a
	 * control reports that the user changed its value.
	 */
	@SuppressWarnings("unchecked")
	default void callOnValueChanged() throws Exception {
		IValueChanged<Object> vc = (IValueChanged<Object>) getOnValueChanged();
		if(null != vc) {
			vc.onValueChanged(this);
		}
	}
}
