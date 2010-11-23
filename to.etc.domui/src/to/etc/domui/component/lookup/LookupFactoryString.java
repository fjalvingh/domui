package to.etc.domui.component.lookup;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

@SuppressWarnings("unchecked")
final class LookupFactoryString implements ILookupControlFactory {
	@Override
	public <X extends IInputNode< ? >> int accepts(final SearchPropertyMetaModel spm, final X control) {
		if(control != null) {
			if(!(control instanceof Text< ? >))
				return -1;
			Text< ? > t = (Text< ? >) control;
			if(t.getInputClass() != String.class)
				return -1;
		}
		return 1; // Accept all properties (will fail on incompatible ones @ input time)
	}

	@SuppressWarnings("rawtypes")
	@Override
	public <X extends IInputNode< ? >> ILookupControlInstance createControl(final SearchPropertyMetaModel spm, final X control) {
		final PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);
		Class< ? > iclz = pmm.getActualType();

		//-- Boolean/boolean types? These need a tri-state checkbox
		if(iclz == Boolean.class || iclz == Boolean.TYPE) {
			throw new IllegalStateException("I need a tri-state checkbox component to handle boolean lookup thingies.");
		}

		//-- Treat everything else as a String using a converter.
		final Text< ? > txt = new Text(iclz);
		if(pmm.getDisplayLength() > 0) {
			int sz = pmm.getDisplayLength();
			if(sz > 40)
				sz = 40;
			txt.setSize(sz);
		} else {
			//-- We must decide on a length....
			int sz = 0;
			if(pmm.getLength() > 0) {
				sz = pmm.getLength();
				if(sz > 40)
					sz = 40;
			}
			if(sz != 0)
				txt.setSize(sz);
		}
		if(pmm.getConverter() != null)
			txt.setConverter((IConverter) pmm.getConverter());
		if(pmm.getLength() > 0)
			txt.setMaxLength(pmm.getLength());
		String hint = MetaUtils.findHintText(spm);
		if(hint != null)
			txt.setTitle(hint);

		//-- Converter thingy is known. Now add a
		return new AbstractLookupControlImpl(txt) {
			@Override
			public boolean appendCriteria(QCriteria< ? > crit) throws Exception {
				Object value = null;
				try {
					value = txt.getValue();
				} catch(Exception x) {
					return false; // Has validation error -> exit.
				}
				if(value == null || (value instanceof String && ((String) value).trim().length() == 0))
					return true; // Is okay but has no data

				// FIXME Handle minimal-size restrictions on input (search field metadata


				//-- Put the value into the criteria..
				if(value instanceof String) {
					String str = (String) value;
					str = str.trim() + "%";
					crit.ilike(spm.getPropertyName(), str);
				} else {
					crit.eq(spm.getPropertyName(), value); // property == value
				}
				return true;
			}
		};
	}
}