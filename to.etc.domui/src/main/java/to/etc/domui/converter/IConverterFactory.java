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
 * A factory for creating IConverters to convert values.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 30, 2008
 */
public interface IConverterFactory {
	/**
	 * This must decide whether to accept the class and property model pair. This must return a score which is
	 * used to decide the best converter to use; the higher the score the better the chance of this converter
	 * being used.
	 * If this converter does not accept the class it <b>must</b> return -1. If the class is accepted <i>but</i>
	 * the PropertyMetaModel passed was unacceptable this <b>must</b> return 0.
	 *
	 * @param clz
	 * @param pmm
	 * @return
	 */
	int accept(Class<?> clz, PropertyMetaModel<?> pmm);

	/**
	 * Return the converter which properly converts the specified class and meta model.
	 * @param clz
	 * @param pmm
	 * @return
	 */
	<X, T extends IConverter<X>> T createConverter(Class<X> clz, PropertyMetaModel<X> pmm);
}
