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
	/**
	 * Represents the result of a call to createControl.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jul 2, 2009
	 */
	static final public class Result {
		/** The list of nodes forming the control */
		private NodeBase[]			m_nodeList;

		/** The binding of the control to it's model and property */
		private ModelBinding		m_binding;

		/** The node to be used as the target for a "label" */
		private NodeBase			m_labelNode;

		public Result(ModelBinding binding, NodeBase labelNode, NodeBase[] nodeList) {
			m_binding = binding;
			m_labelNode = labelNode;
			m_nodeList = nodeList;
		}
		public Result(ModelBinding binding, NodeBase control) {
			m_binding = binding;
			m_labelNode = control;
			m_nodeList = new NodeBase[] { control };
		}
		public <T extends NodeBase & IInputNode<?>> Result(T control, IReadOnlyModel<?> model, PropertyMetaModel pmm) {
			m_labelNode = control;
			m_nodeList = new NodeBase[] { control };
			m_binding = new SimpleComponentPropertyBinding(model, pmm, control);
		}
		public NodeBase[] getNodeList() {
			return m_nodeList;
		}
		public ModelBinding getBinding() {
			return m_binding;
		}
		public NodeBase getLabelNode() {
			return m_labelNode;
		}
	}

	/**
	 * This must return a +ve value when this factory accepts the specified property; the returned value
	 * is an eagerness score. The factory returning the highest eagerness wins.
	 * @param pmm
	 * @param editable
	 * @return
	 */
	int		accepts(PropertyMetaModel pmm, boolean editable);

	/**
	 * This MUST create all nodes necessary for a control to edit the specified item. The nodes must be added
	 * to the container; this <i>must</i> return a ModelBinding to bind and unbind a value to the control
	 * created.
	 *
	 * @param container
	 * @param pmm
	 * @param editable
	 * @return
	 */
	Result createControl(IReadOnlyModel<?> model, PropertyMetaModel pmm, boolean editable);

	static public final ControlFactory	TEXTAREA_CF = new ControlFactoryTextArea();

	/**
	 * This is a fallback factory; it accepts anything and shows a String edit component for it. It
	 * hopes that the Text<?> control can convert the string input value to the actual type using the
	 * registered Converters. This is also the factory for regular Strings.
	 */
	static public final ControlFactory	STRING_CF	= new ControlFactoryString();

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
				c.setLiteralTitle(s);
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
				di.setLiteralTitle(s);
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
				co.setLiteralTitle(s);
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
				li.setLiteralTitle(s);
			return li;
		}
	};
}
