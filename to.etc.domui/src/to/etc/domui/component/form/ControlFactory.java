package to.etc.domui.component.form;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;

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
public interface ControlFactory {
	/**
	 * This must return a +ve value when this factory accepts the specified property; the returned value
	 * is an eagerness score. The factory returning the highest eagerness wins.
	 * @param pmm
	 * @param editable
	 * @param controlClass When set the control factory *must* be able to return a component which is assignment-compatible with this class type. If it cannot it MUST refuse to create the control.
	 * @param context TODO
	 * @return
	 */
	int accepts(PropertyMetaModel pmm, boolean editable, Class< ? > controlClass, Object context);

	/**
	 * This MUST create all nodes necessary for a control to edit the specified item. The nodes must be added
	 * to the container; this <i>must</i> return a ModelBinding to bind and unbind a value to the control
	 * created.
	 * @param pmm
	 * @param editable
	 * @param controlClass	When set the control factory *must* return a component which is assignment-compatible with this
	 * 						class type. When this method is called it has already (by it's accept method) told us it can, so
	 * 						not creating the proper type is not an option.
	 * @param context TODO
	 * @param container
	 *
	 * @return
	 */
	ControlFactoryResult createControl(IReadOnlyModel< ? > model, PropertyMetaModel pmm, boolean editable, Class< ? > controlClass, Object context);

	static public final ControlFactory TEXTAREA_CF = new ControlFactoryTextArea();

	/**
	 * This is a fallback factory; it accepts anything and shows a String edit component for it. It
	 * hopes that the Text<?> control can convert the string input value to the actual type using the
	 * registered Converters. This is also the factory for regular Strings.
	 */
	static public final ControlFactory STRING_CF = new ControlFactoryString();

	static public final ControlFactory BOOLEAN_AND_ENUM_CF = new ControlFactoryEnumAndBool();

	static public final ControlFactory DATE_CF = new ControlFactoryDate();

	/**
	 * Factory for UP relations. This creates a combobox input if the property is an
	 * UP relation and has combobox properties set.
	 */
	static public final ControlFactory RELATION_COMBOBOX_CF = new ControlFactoryRelationCombo();

	static public final ControlFactory RELATION_LOOKUP_CF = new ControlFactoryRelationLookup();
}
