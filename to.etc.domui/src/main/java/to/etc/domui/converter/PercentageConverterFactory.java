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

import to.etc.domui.component.meta.PropertyMetaModel;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PercentageConverterFactory implements IConverterFactory {

	static private Map<Integer, PercentageBigDecimalConverter> BIG_DECIMAL_PERCENTAGE_CONVERTERS = new ConcurrentHashMap<>();

	@Override
	public int accept(Class< ? > clz, PropertyMetaModel< ? > pmm) {
		//we never create these automatically
		return -1;
	}

	/**
	 * Create the appropriate converter.
	 *
	 * @see IConverterFactory#createConverter(Class, PropertyMetaModel)
	 */
	@Override
	public <X, T extends IConverter<X>> T createConverter(Class<X> clz, PropertyMetaModel<X> pmm) {
		if(! clz.isAssignableFrom(BigDecimal.class)) {
			throw new IllegalStateException("Unable to construct PercentageBigDecimalConverter for data class: " + clz);
		}
		return (T) new PercentageBigDecimalConverter(pmm.getScale() < 0 ? 0 : pmm.getScale());
	}

	/**
	 * Returns reusable {@link PercentageBigDecimalConverter} instance based on specified scale.
	 * @return
	 */
	static public PercentageBigDecimalConverter createPercentageBigDecimalConverter(int scale) {
		return BIG_DECIMAL_PERCENTAGE_CONVERTERS.computeIfAbsent(Integer.valueOf(scale), aKey -> new PercentageBigDecimalConverter(aKey));
	}
}
