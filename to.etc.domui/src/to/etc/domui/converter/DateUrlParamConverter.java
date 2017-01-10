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

import javax.annotation.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * Simple converter that converts date values to long 'time' value, used in internal url param conversion. It is locale agnostic.  
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijić</a>
 * Created on Jul 27, 2016
 */
class DateUrlParamConverter implements IConverter<Date> {

	@Nonnull
	static private final DateUrlParamConverter INSTANCE = new DateUrlParamConverter();
	
	@Nonnull
	static public final DateUrlParamConverter getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String convertObjectToString(final Locale loc, final Date in) throws UIException {
		if(in == null) {
			return "";
		}
		
		return in.getTime() + "";
	}

	@Override
	public Date convertStringToObject(final Locale loc, String input) throws UIException {
		if(StringTool.isBlank(input)){
			return null;
		}
		
		input = DomUtil.nullChecked(input.trim());
		
		try{
			return new Date(Long.parseLong(input));
		} catch(NumberFormatException x) {
			throw new IllegalStateException("Unreasonable text conversion from string to Date, expected long value while found:" + input, x);
		}
	}
}
