package to.etc.domui.component.meta;

import java.lang.annotation.*;

import to.etc.domui.util.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaLookup {
	Class<? extends INodeContentRenderer<?>>		nodeRenderer() default UndefinedLabelStringRenderer.class;

	/**
	 * The list of properties that should be shown. This is needed ONLY when the class metadata of the
	 * parent record does not specify a default display column or columnset.
	 * @return
	 */
	public MetaDisplayProperty[]	properties() default {};
}
