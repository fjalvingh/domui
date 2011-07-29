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

import to.etc.domui.component.meta.*;

/**
 * Generic factory to accept double numeric values. This has a low priority so monetary values
 * can override this.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 17, 2009
 */
final public class DoubleFactory implements IConverterFactory {
	@Override
	public int accept(final Class< ? > clz, final PropertyMetaModel< ? > pmm) {
		if(!Double.class.isAssignableFrom(clz) && double.class != clz)
			return -1;
		return 5;
	}

	/**
	 *
	 * @see to.etc.domui.converter.IConverterFactory#createConverter(java.lang.Class, to.etc.domui.component.meta.PropertyMetaModel)
	 */
	@Override
	public <X, T extends IConverter<X>> T createConverter(final Class<X> clz, final PropertyMetaModel<X> pmm) {
		if(null == pmm)
			return (T) ConverterRegistry.getConverterInstance(DoubleConverter.class);

		//-- Try to allot a properly formatting converter.
		return (T) new NumberConverter<Double>(Double.class, pmm.getNumericPresentation(), pmm.getScale());
	}
}
