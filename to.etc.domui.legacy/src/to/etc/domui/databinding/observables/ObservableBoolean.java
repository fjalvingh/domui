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
package to.etc.domui.databinding.observables;

import javax.annotation.*;

import to.etc.util.*;

/**
 * Utility observable for booleans.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 20, 2013
 */
public class ObservableBoolean extends ObservableValue<Boolean> {
	public ObservableBoolean() {
		super(Boolean.FALSE);
	}

	public ObservableBoolean(boolean value) {
		super(Boolean.valueOf(value));
	}

	public ObservableBoolean(@Nonnull Boolean value) {
		super(value);
	}

	public boolean isSet() {
		try {
			Boolean val = getValue();
			return val == Boolean.TRUE;
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	public void set(boolean value) {
		try {
			setValue(Boolean.valueOf(value));
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}
}
