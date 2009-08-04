package to.etc.domui.component.lookup;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.input.ComboFixed.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

final class LookupFactoryNumber implements LookupControlFactory {
	public ILookupControlInstance createControl(SearchPropertyMetaModel spm, final PropertyMetaModel pmm) {
		final List<Pair<NumericRelationType>> values = new ArrayList<Pair<NumericRelationType>>();
		for(NumericRelationType relationEnum : NumericRelationType.values()) {
			values.add(new Pair<NumericRelationType>(relationEnum, MetaManager.findClassMeta(NumericRelationType.class).getDomainLabel(NlsContext.getLocale(), relationEnum)));
		}

		final Text< ? > numA = createNumericInput(pmm);
		final Text< ? > numB = createNumericInput(pmm);
		numB.setDisabled(true);

		final ComboFixed<NumericRelationType> relationCombo = new ComboFixed<NumericRelationType>(values);

		final AbstractLookupControlImpl result = new AbstractLookupControlImpl(relationCombo, numA, numB) {
			@Override
			public boolean appendCriteria(QCriteria< ? > crit) throws Exception {
				NumericRelationType relation;
				relation = relationCombo.getValue();
				if(relation == null) {
					return true;
				}
				if(!numA.validate()) {
					return false;
				}
				if(relation == NumericRelationType.BETWEEN && !numB.validate()) {
					return false;
				}
				switch(relation){
					case EQ:
						crit.eq(pmm.getName(), numA.getValue());
						break;
					case LT:
						crit.lt(pmm.getName(), numA.getValue());
						break;
					case LE:
						crit.le(pmm.getName(), numA.getValue());
						break;
					case GT:
						crit.gt(pmm.getName(), numA.getValue());
						break;
					case GE:
						crit.ge(pmm.getName(), numA.getValue());
						break;
					case NOT_EQ:
						crit.ne(pmm.getName(), numA.getValue());
						break;
					case BETWEEN:
						crit.between(pmm.getName(), numA.getValue(), numB.getValue());
						break;
				}
				return true;
			}
		};

		relationCombo.setClicked(new IClicked<ComboFixed<NumericRelationType>>() {

			public void clicked(ComboFixed<NumericRelationType> b) throws Exception {
				if(b.getValue() == NumericRelationType.BETWEEN) {
					if(numB.isDisabled()) {
						numB.setDisabled(false);
					}
				} else if(!numB.isDisabled()) {
					numB.setDisabled(true);
					numB.setValue(null);
				}
			}

		});
		return result;
	}

	@SuppressWarnings("unchecked")
	private Text< ? > createNumericInput(final PropertyMetaModel pmm) {
		Class< ? > iclz = pmm.getActualType();

		//-- Create first text control that accept any numeric type.
		final Text< ? > numText = new Text(iclz);
		/*
		 * Length calculation using the metadata. This uses the "length" field as LAST, because it is often 255 because the
		 * JPA's column annotation defaults length to 255 to make sure it's usability is bloody reduced. Idiots.
		 */
		if(pmm.getDisplayLength() > 0)
			numText.setSize(pmm.getDisplayLength());
		else if(pmm.getPrecision() > 0) {
			// FIXME This should be localized somehow...
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

		if(pmm.getConverterClass() != null)
			numText.setConverterClass((Class) pmm.getConverterClass());
		if(pmm.getLength() > 0)
			numText.setMaxLength(pmm.getLength());
		String s = pmm.getDefaultHint();
		if(s != null)
			numText.setTitle(s);
		for(PropertyMetaValidator mpv : pmm.getValidators())
			numText.addValidator(mpv);

		return numText;
	}

	public int accepts(PropertyMetaModel pmm) {
		//-- Return a low value; special format input line monetary needs different factory?
		if(Integer.class == pmm.getActualType() || Double.class == pmm.getActualType() || pmm.getActualType() == double.class) {
			return 2;
		}
		return 0;
	}
}