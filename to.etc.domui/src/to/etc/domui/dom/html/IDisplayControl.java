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

import to.etc.domui.trouble.*;

public interface IDisplayControl<T> {
	/**
	 * Set a new value into this control. Setting a value to null means the control holds no value. This
	 * value is converted to a presentable form using any (implicitly) defined converters; it will not
	 * be validated though! This means that if you set an invalid value for a validator this will not
	 * be seen until the value is gotten from the control again.
	 * @param v
	 */
	void setValue(T v);

	/**
	 * Returns the current value of this input component. If the component contains no value
	 * this returns null. All text input components will return null when their value is the
	 * empty string! If the component, during conversion or validation of the input value,
	 * discovers that the input is invalid it will set itself in "error" mode, post an error
	 * message up the form, and throw a {@link ValidationException}. This means that this
	 * call either delivers correct input (as defined by it's converter and validators), null
	 * (when empty) or throws an exception. When a ValidationException occurs the framework
	 * mostly ignores it - it does not produce a stacktrace or error in the client. Instead
	 * the resulting error as posted by the error handling framework gets displayed on the
	 * form when the request completes.
	 * <p>To get the value of a component while ignoring exceptions call {@link #getValueSafe()}.</p>
	 * @return
	 */
	T getValue();

	/**
	 * Set the testID for external test software.
	 * @param testID
	 */
	void setTestID(String testID);
}
