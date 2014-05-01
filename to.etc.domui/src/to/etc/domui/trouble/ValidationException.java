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
package to.etc.domui.trouble;

import javax.annotation.*;

import to.etc.domui.dom.errors.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

public class ValidationException extends UIException {
	public ValidationException() {
		super(Msgs.BUNDLE, Msgs.NOT_VALID);
	}

	public ValidationException(BundleRef bundle, String code, Object... parameters) {
		super(bundle, code, parameters);
	}

	public ValidationException(String code, Object... param) {
		super(Msgs.BUNDLE, code, param);
	}

	public ValidationException(@Nonnull UIMessage msg) {
		super(msg.getBundle(), msg.getCode(), msg.getParameters());
	}
}
