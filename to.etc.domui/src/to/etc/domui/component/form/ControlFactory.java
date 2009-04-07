package to.etc.domui.component.form;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * A factory which creates the correct EDITING control to edit a property, specified by the property's
 * PropertyMetaModel. The DomApplication will contain a list of ControlFactories. When an edit control
 * is needed this list is obtained and each ControlFactory in it has it's accepts() method called. This
 * returns a "score" for each control factory. The first factory with the highest score (which must be
 * > 0) will be used to create the control. If no factory returns a &gt; 0 score a control cannot be
 * created which usually results in an exception.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 8, 2008
 */
@SuppressWarnings("unchecked")
public interface ControlFactory {
	public int		accepts(PropertyMetaModel pmm);

	public NodeBase createControl(PropertyMetaModel pmm, boolean editable);

	/**
	 * This is a fallback factory; it accepts anything and shows a String edit component for it. It
	 * hopes that the Text<?> control can convert the string input value to the actual type using the
	 * registered Converters. This is also the factory for regular Strings.
	 */
	static public final ControlFactory	STRING_CF	= new ControlFactory() {
		public NodeBase createControl(PropertyMetaModel pmm, boolean editable) {
			Class<?>	iclz	= pmm.getActualType();

			//-- If this has a textArea hint create a textArea
			if(pmm.getComponentTypeHint() != null) {
				if(pmm.getComponentTypeHint().toLowerCase().contains("textarea")) {
					TextArea	ta = new TextArea();
					if(! editable)
						ta.setReadOnly(true);
					ta.setCols(80);
					ta.setRows(4);
					if(pmm.isRequired())
						ta.setMandatory(true);
					String s = pmm.getDefaultHint();
					if(s != null)
						ta.setTitle(s);
					return ta;
				}
			}

			//-- Treat everything else as a String using a converter.
			Text<?>	txt	= new Text(iclz);
			if(! editable)
				txt.setReadOnly(true);

			/*
			 * Length calculation using the metadata. This uses the "length" field as LAST, because it is often 255 because the
			 * JPA's column annotation defaults length to 255 to make sure it's usability is bloody reduced. Idiots.
			 */
			if(pmm.getDisplayLength() > 0)
				txt.setSize(pmm.getDisplayLength());
			else if(pmm.getPrecision() > 0) {
				// FIXME This should be localized somehow...
				//-- Calculate a size using scale and precision.
				int size = pmm.getPrecision();
				int d = size;
				if(pmm.getScale() > 0) {
					size++;						// Inc size to allow for decimal point or comma
					d -= pmm.getScale();		// Reduce integer part,
					if(d >= 4) {				// Can we get > 999? Then we can have thousand-separators
						int nd = (d-1) / 3;		// How many thousand separators could there be?
						size += nd;				// Increment input size with that
					}
				}
				txt.setSize(size);
			} else if(pmm.getLength() > 0) {
				txt.setSize(pmm.getLength() < 40 ? pmm.getLength() : 40);
			}

			if(pmm.getConverterClass() != null)
				txt.setConverterClass(pmm.getConverterClass());
			if(pmm.getLength() > 0)
				txt.setMaxLength(pmm.getLength());
			if(pmm.isRequired())
				txt.setMandatory(true);
			String s = pmm.getDefaultHint();
			if(s != null)
				txt.setTitle(s);
			for(PropertyMetaValidator mpv: pmm.getValidators())
				txt.addValidator(mpv);
			return txt;
		}

		/**
		 * Accept any type using a string.
		 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel)
		 */
		public int accepts(PropertyMetaModel pmm) {
			return 1;
		}
	};

	static public final ControlFactory	BOOLEAN_AND_ENUM_CF	= new ControlFactory() {
		public NodeBase createControl(PropertyMetaModel pmm, boolean editable) {
			// Create a domainvalued combobox by default.
			Object[]	vals	= pmm.getDomainValues();
			ClassMetaModel	ecmm = null;
			List<ComboFixed.Pair<Object>>	vl = new ArrayList<ComboFixed.Pair<Object>>();
			for(Object o: vals) {
				String label = pmm.getDomainValueLabel(NlsContext.getLocale(), o);	// Label known to property?
				if(label == null) {
					if(ecmm == null)
						ecmm = MetaManager.findClassMeta(pmm.getActualType());		// Try to get the property's type.
					label = ecmm.getDomainLabel(NlsContext.getLocale(), o);
					if(label == null)
						label = o == null ? "" : o.toString();
				}
				vl.add(new ComboFixed.Pair<Object>(o, label));
			}

			ComboFixed<?>	c = new ComboFixed<Object>(vl);
			if(pmm.isRequired())
				c.setMandatory(true);
			if(! editable || pmm.getReadOnly() == YesNoType.YES)
				c.setDisabled(true);
			String s = pmm.getDefaultHint();
			if(s != null)
				c.setTitle(s);
			return c;
		}

		/**
		 * Accept boolean.
		 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel)
		 */
		public int accepts(PropertyMetaModel pmm) {
			Class<?>	iclz	= pmm.getActualType();
			return iclz == Boolean.class || iclz == Boolean.TYPE || Enum.class.isAssignableFrom(iclz) ? 2 : 0;
		}
	};


	static public final ControlFactory	DATE_CF	= new ControlFactory() {
		public NodeBase createControl(PropertyMetaModel pmm, boolean editable) {
			if(! editable) {
				Text<Date>	txt = new Text<Date>(Date.class);
				txt.setReadOnly(true);
				return txt;
			}

			DateInput	di	= new DateInput();
			if(pmm.isRequired())
				di.setMandatory(true);
			if(pmm.getTemporal() == TemporalPresentationType.DATETIME)
				di.setWithTime(true);
			String s = pmm.getDefaultHint();
			if(s != null)
				di.setTitle(s);
			return di;
		}

		public int accepts(PropertyMetaModel pmm) {
			Class<?>	iclz	= pmm.getActualType();
			if(Date.class.isAssignableFrom(iclz)) {
				return 2;
			}
			return 0;
		}
	};

	/**
	 * Factory for UP relations. This creates a combobox input if the property is an
	 * UP relation and has combobox properties set.
	 */
	static public final ControlFactory	RELATION_COMBOBOX_CF	= new ControlFactory() {
		public NodeBase createControl(PropertyMetaModel pmm, boolean editable) {
			if(! editable)
				throw new IllegalStateException("Implementation: please implement ReadOnly combobox thingy.");

			//-- We need to add a ComboBox. Do we have a combobox dataset provider?
			Class<? extends IComboDataSet<?>>	set = pmm.getComboDataSet();
			if(set == null) {
				set = pmm.getClassModel().getComboDataSet();
				if(set == null)
					throw new IllegalStateException("Missing Combo dataset provider for property "+pmm);
			}

			INodeContentRenderer<?>	r = MetaManager.createDefaultComboRenderer(pmm, null);
			ComboLookup<?>	co = new ComboLookup(set, r);
			if(pmm.isRequired())
				co.setMandatory(true);
			String s = pmm.getDefaultHint();
			if(s != null)
				co.setTitle(s);
			return co;
		}

		public int accepts(PropertyMetaModel pmm) {
			if(pmm.getRelationType() != PropertyRelationType.UP)
				return 0;
			if(Constants.COMPONENT_COMBO.equals(pmm.getComponentTypeHint()))
				return 10;
			return 2;
		}
	};

	static public final ControlFactory	RELATION_LOOKUP_CF	= new ControlFactory() {
		public int accepts(PropertyMetaModel pmm) {
			if(pmm.getRelationType() != PropertyRelationType.UP)
				return 0;
			if(Constants.COMPONENT_LOOKUP.equals(pmm.getComponentTypeHint()))
				return 10;
			return 3;						// Prefer a lookup above a combo if unspecified
		}

		public NodeBase createControl(PropertyMetaModel pmm, boolean editable) {
//			if(! editable)
//				throw new IllegalStateException("Implementation: please implement ReadOnly combobox thingy - Cannot create control for "+pmm);
//
			//-- We'll do a lookup thingy for sure.
			LookupInput	li = new LookupInput<Object>((Class<Object>)pmm.getActualType());
			li.setReadOnly(! editable);
			if(pmm.getLookupFieldRenderer() != null)
				li.setContentRenderer(DomApplication.get().createInstance(pmm.getLookupFieldRenderer()));
			if(pmm.isRequired())
				li.setMandatory(true);
			String s = pmm.getDefaultHint();
			if(s != null)
				li.setTitle(s);
			return li;
		}
	};
}
