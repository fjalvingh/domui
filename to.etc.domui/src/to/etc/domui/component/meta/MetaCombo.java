package to.etc.domui.component.meta;

import java.lang.annotation.*;

import to.etc.domui.component.meta.impl.*;
import to.etc.domui.util.*;

/**
 * TEMP Specifies that a parent relation is set using a default
 * combobox. This is part of a working test for metadata, do not use
 * because it can change heavily (or be deleted alltogether).
 *
 * This annotation can also be added to a PARENT class, in which case it will
 * define the defaults would that class be used in an "Up" relation combobox.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 13, 2008
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaCombo {
	/**
	 * The thingy which is resposible for returning the set-of-objects to select a value
	 * from. The default will use the data type and create a generic full-dataset query.
	 *
	 * @return
	 */
	Class<? extends IComboDataSet<?>>		dataSet() default UndefinedComboDataSet.class;

	Class<? extends ILabelStringRenderer<?>>	labelRenderer() default UndefinedLabelStringRenderer.class;

	Class<? extends INodeContentRenderer<?>>		nodeRenderer() default UndefinedLabelStringRenderer.class;

	/**
	 * The list of properties that should be shown. This is needed ONLY when the class metadata of the
	 * parent record does not specify a default display column or columnset.
	 * @return
	 */
	public MetaDisplayProperty[]	properties() default {};

	/**
	 * Allow no value to be selected here. Used when there's a need to override the default.
	 * @return
	 */
	public ComboOptionalType		optional() default ComboOptionalType.INHERITED;
}
