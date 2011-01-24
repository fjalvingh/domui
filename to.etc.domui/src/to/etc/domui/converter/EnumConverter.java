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
package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.trouble.*;
import to.etc.webapp.nls.*;

/**
 * This generic converter for enums should be used only as convertObjectToString renderer.
 * It is used for default rendering of enum fields inside table results.
 * Backward conversion using  convertStringToObject is not supported and would throw exception.
 *
 * @author vmijic
 * Created on 29 Jul 2009
 */
public class EnumConverter<E extends Enum<E>> implements IConverter<E> {

	@Override
	public String convertObjectToString(Locale loc, E in) throws UIException {
		if(in == null)
			return "";
		ClassMetaModel ecmm = MetaManager.findClassMeta(in.getClass());
		String v = ecmm.getDomainLabel(NlsContext.getLocale(), in);
		return v == null ? in.toString() : v;
	}

	@Override
	public E convertStringToObject(Locale loc, String input) throws UIException {
		throw new IllegalStateException("Enum values should not be input using a string");
	}
}
