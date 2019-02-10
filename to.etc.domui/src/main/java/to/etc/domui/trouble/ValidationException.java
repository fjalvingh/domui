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

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.BundleRef;
import to.etc.webapp.nls.IBundleCode;

public class ValidationException extends UIException {
	public ValidationException(IBundleCode code, Object... parameters) {
		super(code, parameters);
	}

	public ValidationException(Throwable t, IBundleCode code, Object... parameters) {
		super(t, code, parameters);
	}

	@Deprecated
	public ValidationException() {
		super(Msgs.notValid);
	}

	@Deprecated
	public ValidationException(BundleRef bundle, String code, Object... parameters) {
		super(bundle, code, parameters);
	}

	@Deprecated
	public ValidationException(String code, Object... param) {
		super(Msgs.BUNDLE, code, param);
	}

	public ValidationException(@NonNull UIMessage msg) {
		super(msg.getBundle(), msg.getCode(), msg.getParameters());
	}
}
