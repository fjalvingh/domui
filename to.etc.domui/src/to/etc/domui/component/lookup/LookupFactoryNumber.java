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

import java.math.*;
import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

/**
 * Represents factory for numeric values lookup. For lookup condition uses combo with numeric relation trailed by one or two fields (when between relation is selected) for definition of numeric parameters.
 *
 * FIXME The condition must be mandatory and may not have an empty value; the default MUST be "equals". The lookup is to be considered "empty" when the value field is empty.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 13 Aug 2009
 */
final class LookupFactoryNumber implements ILookupControlFactory {
	@Override
	public <X extends to.etc.domui.dom.html.IInputNode< ? >> ILookupControlInstance createControl(final SearchPropertyMetaModel spm, final X control) {
		if(control != null)
			throw new IllegalStateException();

		final PropertyMetaModel< ? > pmm = MetaUtils.getLastProperty(spm);
		final List<ValueLabelPair<NumericRelationType>> values = new ArrayList<ValueLabelPair<NumericRelationType>>();
		for(NumericRelationType relationEnum : NumericRelationType.values()) {
			values.add(new ValueLabelPair<NumericRelationType>(relationEnum, MetaManager.findClassMeta(NumericRelationType.class).getDomainLabel(NlsContext.getLocale(), relationEnum)));
		}

		final Text< ? > numA = createNumericInput(pmm);
		final Text< ? > numB = createNumericInput(pmm);
		numB.setDisplay(DisplayType.NONE);

		final ComboFixed<NumericRelationType> relationCombo = new ComboFixed<NumericRelationType>(values);

		relationCombo.setOnValueChanged(new IValueChanged<ComboFixed<NumericRelationType>>() {
			@Override
			public void onValueChanged(ComboFixed<NumericRelationType> component) throws Exception {
				if(component.getValue() == NumericRelationType.BETWEEN) {
					if(numB.getDisplay() == DisplayType.NONE) {
						numB.setDisplay(DisplayType.INLINE);
					}
				} else {
					if(numB.getDisplay() != DisplayType.NONE) {
						numB.setDisplay(DisplayType.NONE);
					}
				}
			}
		});

		String hint = MetaUtils.findHintText(spm);
		if(hint != null) {
			numA.setTitle(hint);
			numB.setTitle(hint);
		}

		return new AbstractLookupControlImpl(relationCombo, numA, numB) {
			/*			@Override
			public NodeBase[] getInputControls() {
				if(relationCombo.getValue() == NumericRelationType.BETWEEN)
					return new NodeBase[]{relationCombo, numA, numB};
				else
					return new NodeBase[]{relationCombo, numA};
			}*/

			@Override
			public AppendCriteriaResult appendCriteria(QCriteria< ? > crit) throws Exception {
				NumericRelationType relation;
				relation = relationCombo.getValue();
				if(relation == null) {
					return AppendCriteriaResult.EMPTY;
				}
				if(!numA.validate()) {
					return AppendCriteriaResult.INVALID;
				}
				if(relation == NumericRelationType.BETWEEN && !numB.validate()) {
					return AppendCriteriaResult.INVALID;
				}
				switch(relation){
					default:
						throw new IllegalStateException(relation + ": unhandled");
					case EQ:
						crit.eq(spm.getPropertyName(), numA.getValue());
						break;
					case LT:
						crit.lt(spm.getPropertyName(), numA.getValue());
						break;
					case LE:
						crit.le(spm.getPropertyName(), numA.getValue());
						break;
					case GT:
						crit.gt(spm.getPropertyName(), numA.getValue());
						break;
					case GE:
						crit.ge(spm.getPropertyName(), numA.getValue());
						break;
					case NOT_EQ:
						crit.ne(spm.getPropertyName(), numA.getValue());
						break;
					case BETWEEN:
						crit.between(spm.getPropertyName(), numA.getValue(), numB.getValue());
						break;
				}
				return AppendCriteriaResult.VALID;
			}
		};
	}

	private <T> Text<T> createNumericInput(final PropertyMetaModel<T> pmm) {
		Class<T> iclz = pmm.getActualType();

		//-- Create first text control that accept any numeric type.
		final Text<T> numText = new Text<T>(iclz);
		/*
		 * Length calculation using the metadata. This uses the "length" field as LAST, because it is often 255 because the
		 * JPA's column annotation defaults length to 255 to make sure it's usability is bloody reduced. Idiots.
		 */
		if(pmm.getDisplayLength() > 0)
			numText.setSize(pmm.getDisplayLength());
		else if(pmm.getPrecision() > 0) {
			//-- Calculate a size using scale and precision.
			int size = pmm.getPrecision();
			int d = size;
			if(pmm.getScale() > 0) {
				size++; // Inc size to allow for decimal point or comma
				d -= pmm.getScale(); // Reduce integer part,
				if(d >= 4) { // Can we get > 999? Then we can have thousand-separators
					int nd = (d - 1) / 3; // How many thousand separators could there be?
					size += nd; // Increment input size with that
				}
			}
			numText.setSize(size);
		} else if(pmm.getLength() > 0) {
			numText.setSize(pmm.getLength() < 40 ? pmm.getLength() : 40);
		}
		IConverter<T> cvt = ConverterRegistry.findBestConverter(pmm);
		numText.setConverter(cvt);

		if(pmm.getLength() > 0)
			numText.setMaxLength(pmm.getLength());
		String s = pmm.getDefaultHint();
		if(s != null)
			numText.setTitle(s);
		for(PropertyMetaValidator mpv : pmm.getValidators())
			numText.addValidator(mpv);

		return numText;
	}

	@Override
	public <X extends to.etc.domui.dom.html.IInputNode< ? >> int accepts(final SearchPropertyMetaModel spm, final X control) {
		if(control != null)
			return -1;
		PropertyMetaModel< ? > pmm = MetaUtils.getLastProperty(spm);
		if(DomUtil.isIntegerType(pmm.getActualType()) || DomUtil.isRealType(pmm.getActualType()) || pmm.getActualType() == BigDecimal.class) {
			if(pmm.getComponentTypeHint() != null && pmm.getComponentTypeHint().toLowerCase().contains("numberlookupcombo"))
				return 8;
			return 2;
		}
		return -1;
	}
}
