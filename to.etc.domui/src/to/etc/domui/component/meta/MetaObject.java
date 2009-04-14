package to.etc.domui.component.meta;

import java.lang.annotation.*;

import to.etc.domui.util.*;

/**
 * Annotation to describe the default columns to show for an
 * object.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2008
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaObject {
	MetaDisplayProperty[]	defaultColumns() default {};
	String					defaultSortColumn() default Constants.NONE;
	SortableType			defaultSortOrder() default SortableType.SORTABLE_ASC;

	String					tableName() default "";
	String					tablePrefix() default "";
}
