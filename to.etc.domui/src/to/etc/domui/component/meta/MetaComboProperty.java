package to.etc.domui.component.meta;

import java.lang.annotation.*;

import to.etc.domui.converter.*;
import to.etc.domui.util.*;

/**
 * Defines a display property for a combo control, to use in the {@link MetaCombo} annotation. It is functionally
 * equivalent to {@link MetaDisplayProperty} but has less parameters.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 20, 2010
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaComboProperty {
	/**
	 * The name of the property to show. You can also specify a dotted path to some
	 * parent entity's property here. This will be replaced with a Property reference once
	 * the JDK 7 team gets off it's ass and starts to bloody define something useful 8-(
	 * @return
	 */
	String name();

	/**
	 * The converter to use to convert this property's value to a string. Defaults to the actual property's
	 * converter when unset.
	 * @return
	 */
	Class< ? extends IConverter< ? >> converterClass() default DummyConverter.class;

	/**
	 * When set, this separator will be added between this property and the <i>next one</i> in the combo's
	 * property list, provided there is a next property value. It defaults to a single space.
	 * @return
	 */
	String join() default Constants.NO_JOIN;
}
