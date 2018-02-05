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
package to.etc.domui.component.lookup;

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.MetaUtils;
import to.etc.domui.component.meta.NumericPresentation;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.dom.css.TextAlign;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.util.DomUtil;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * This is a factory for numeric entry of values, where the value entered can be preceded with some kind of
 * operator. If a value is entered verbatim it will be scanned as-is and used in an "equals" query. If the
 * value is preceded by either &gt;, &lt;, &gt;=, &lt;= the query will be done using the appropriate operator. In addition
 * the field can also contain '!' to indicate that the field MUST be empty (db null). Between or not-between
 * queries can be done by entering two operator-value pairs, like "&gt; 10 &lt; 100" (between [10..100&lt;)
 * or "&lt; 10 &gt; 100" (meaning NOT between [10..100&lt;).
 *
 * This control is the default numeric input control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 28, 2009
 */
@Deprecated final class LookupFactoryNumber2 implements ILookupControlFactory {
	@Override
	public <T, X extends IControl<T>> int accepts(@Nonnull SearchPropertyMetaModel spm, X control) {
		if(control != null) {
			if(!(control instanceof Text2<?>))
				return -1;
			Text2<?> t = (Text2<?>) control;
			if(t.getActualType() != String.class)
				return -1;
		}

		final PropertyMetaModel<?> pmm = spm.getProperty();
		return DomUtil.isIntegerType(pmm.getActualType()) || DomUtil.isRealType(pmm.getActualType()) || pmm.getActualType() == BigDecimal.class ? 4 : -1;
	}

	/**
	 * Create the input control which is a text input.
	 * @see to.etc.domui.component.lookup.ILookupControlFactory#createControl(to.etc.domui.component.meta.SearchPropertyMetaModel, to.etc.domui.dom.html.IControl)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T, X extends IControl<T>> ILookupControlInstance<?> createControl(@Nonnull SearchPropertyMetaModel spm, X control) {
		final PropertyMetaModel<?> pmm = spm.getProperty();
		Text2<String> numText = (Text2<String>) control;
		if(numText == null) {
			numText = new Text2<String>(String.class);

			/*
			 * Calculate a "size=" for entering this number. We cannot assign a "maxlength" because not only the number but
			 * operators can be added to the string too. By default we size the field some 5 characters wider than the max size
			 * for the number as defined by scale and precision.
			 */
			int size = MetaManager.calculateTextSize(pmm);
			if(size > 0)
				numText.setSize(size);
			String s = pmm.getDefaultHint();
			if(s != null)
				numText.setTitle(s);
			String hint = MetaUtils.findHintText(spm);
			if(hint != null)
				numText.setTitle(hint);
		}
		Double minmax = Double.valueOf(calcMaxValue(pmm));
		boolean monetary = NumericPresentation.isMonetary(pmm.getNumericPresentation());

		if(monetary) {
			numText.setTextAlign(TextAlign.RIGHT);
		}

		//-- FIXME Generic bounds violation due to it's gross definition, ignored.
		//-- FIXME jal 20110415 Vladimir- transient here is wrong because transient usually means querying is impossible at all.
		return new LookupNumberControl<>((Class<Number>) pmm.getActualType(), numText, pmm.getName(), Double.valueOf(-minmax.doubleValue()), minmax, monetary, !pmm.isTransient(), pmm.getScale());
	}

	static private double calcMaxValue(PropertyMetaModel<?> pmm) {
		int prec = pmm.getPrecision();
		if(prec > 0) {
			int scale = pmm.getScale();
			if(scale > 0 && scale < prec)
				prec -= scale;
			double val = Math.pow(10, prec);
			return val;
		}
		return Double.MAX_VALUE;
	}


}
