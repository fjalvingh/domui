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

import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.IExecute;


/**
 * DomUI nodes that have a change listener.
 * <p>20091120 jal This originally extended INodeErrorDelegate; I removed this because these have nothing to do with each other.</p>
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 4 Sep 2009
 */
public interface IHasChangeListener {
	/**
	 * The handler used to mark a control as "immediate": it has no real change handler but its
	 * changes must still be reported to the server as they happen.
	 */
	IExecute DUMMY = () -> {};

	@Nullable
	IExecute getOnValueChanged();

	/**
	 * Set the change handler: an action, because the control it is set on is a local variable of
	 * createContent() that the lambda already captures.
	 */
	void setOnValueChanged(@Nullable IExecute onValueChanged);

	/**
	 * Remove the change handler set on this component, if any.
	 */
	default void clearOnValueChanged() {
		setOnValueChanged(null);
	}

	/**
	 * Call the change handler, if one is set. For component implementations: this is how a
	 * control reports that the user changed its value.
	 */
	default void callOnValueChanged() throws Exception {
		IExecute vc = getOnValueChanged();
		if(null != vc) {
			vc.execute();
		}
	}
}
