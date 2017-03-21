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
 * This accepts all properties that have a list of values as their domain model.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 17, 2009
 */
final public class DomainListConverterFactory implements IConverterFactory {
	@Override
	public int accept(final Class< ? > clz, final PropertyMetaModel< ? > pmm) {
		if(pmm == null)
			return -1;
		Object[] dv = pmm.getDomainValues();
		return dv != null && dv.length > 0 ? 10 : -1;
	}

	@Override
	public <X, T extends IConverter<X>> T createConverter(final Class<X> clz, final PropertyMetaModel<X> pmm) {
		return (T) new DomainListConverter(pmm);
	}
}
