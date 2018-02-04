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

import to.etc.domui.component.input.ComboFixed;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.input.ValueLabelPair;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.MetaUtils;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.PropertyMetaValidator;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.IConverter;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.util.DomUtil;
import to.etc.webapp.nls.NlsContext;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents factory for numeric values lookup. For lookup condition uses
 * combo with numeric relation trailed by one or two fields (when between
 * relation is selected) for definition of numeric parameters.
 *
 * FIXME The condition must be mandatory and may not have an empty value; the default MUST be "equals". The lookup is to be considered "empty" when the value field is empty.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 13 Aug 2009
 */
@Deprecated
final class LookupFactoryNumber implements ILookupControlFactory {
	@Override
	public <T, X extends IControl<T>> ILookupControlInstance<?> createControl(final @Nonnull SearchPropertyMetaModel spm, final X control) {
		if(control != null)
			throw new IllegalStateException();

		final PropertyMetaModel< ? > pmm = spm.getProperty();
		final List<ValueLabelPair<NumericRelationType>> values = new ArrayList<>();
		for(NumericRelationType relationEnum : NumericRelationType.values()) {
			values.add(new ValueLabelPair<>(relationEnum, MetaManager.findClassMeta(NumericRelationType.class).getDomainLabel(NlsContext.getLocale(), relationEnum)));
		}

		final Text2< ? > numA = createNumericInput(pmm);
		final Text2< ? > numB = createNumericInput(pmm);
		numB.setDisplay(DisplayType.NONE);

		final ComboFixed<NumericRelationType> relationCombo = new ComboFixed<NumericRelationType>(values);

		relationCombo.setOnValueChanged((IValueChanged<ComboFixed<NumericRelationType>>) component -> {
			if(component.getValue() == NumericRelationType.BETWEEN) {
				if(numB.getDisplay() == DisplayType.NONE) {
					numB.setDisplay(DisplayType.INLINE);
				}
			} else {
				if(numB.getDisplay() != DisplayType.NONE) {
					numB.setDisplay(DisplayType.NONE);
				}
			}
		});

		String hint = MetaUtils.findHintText(spm);
		if(hint != null) {
			numA.setTitle(hint);
			numB.setTitle(hint);
		}

		return new AbstractLookupControlImpl(relationCombo, numA, numB) {
			@Override
			public @Nonnull AppendCriteriaResult appendCriteria(@Nonnull QCriteria< ? > crit) throws Exception {
				NumericRelationType relation;
				relation = relationCombo.getValue();
				if(relation == null) {
					return AppendCriteriaResult.EMPTY;
				}
				if(null == numA.getValueSafe()) {
					return AppendCriteriaResult.INVALID;
				}
				if(relation == NumericRelationType.BETWEEN && numB.getValueSafe() == null) {
					return AppendCriteriaResult.INVALID;
				}
				Object vala = numA.getValue();
				if(vala == null)
					return AppendCriteriaResult.EMPTY;
				switch(relation){
					default:
						throw new IllegalStateException(relation + ": unhandled case");
					case EQ:
						crit.eq(spm.getProperty().getName(), vala);
						break;
					case LT:
						crit.lt(spm.getProperty().getName(), vala);
						break;
					case LE:
						crit.le(spm.getProperty().getName(), vala);
						break;
					case GT:
						crit.gt(spm.getProperty().getName(), vala);
						break;
					case GE:
						crit.ge(spm.getProperty().getName(), vala);
						break;
					case NOT_EQ:
						crit.ne(spm.getProperty().getName(), vala);
						break;
					case BETWEEN:
						Object numb = numB.getValue();
						if(null == numb)
							return AppendCriteriaResult.INVALID;
						crit.between(spm.getProperty().getName(), vala, numb);
						break;
				}
				return AppendCriteriaResult.VALID;
			}
		};
	}

	private <T> Text2<T> createNumericInput(final PropertyMetaModel<T> pmm) {
		Class<T> iclz = pmm.getActualType();

		//-- Create first text control that accept any numeric type.
		final Text2<T> numText = new Text2<T>(iclz);
		int size = MetaManager.calculateTextSize(pmm);
		if(size > 0)
			numText.setSize(size);

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
	public <T, X extends IControl<T>> int accepts(final @Nonnull SearchPropertyMetaModel spm, final X control) {
		if(control != null)
			return -1;
		PropertyMetaModel< ? > pmm = spm.getProperty();
		if(DomUtil.isIntegerType(pmm.getActualType()) || DomUtil.isRealType(pmm.getActualType()) || pmm.getActualType() == BigDecimal.class) {
			String cth = pmm.getComponentTypeHint();
			if(cth != null && cth.toLowerCase().contains("numberlookupcombo"))
				return 8;
			return 2;
		}
		return -1;
	}
}
