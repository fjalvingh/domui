package to.etc.domui.component.meta;

import java.lang.annotation.*;

import to.etc.domui.converter.*;
import to.etc.domui.util.*;

/**
 * Annotation to add metadata to a property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MetaProperty {
	public String				defaultLabel() default Constants.NO_DEFAULT_LABEL;
	public SortableType			defaultSortable() default SortableType.UNKNOWN;
	public int					displaySize() default -1;
	public int					length() default -1;
	public YesNoType			required() default YesNoType.UNKNOWN;
	public Class<? extends IConverter>		converterClass() default IConverter.class;
	public YesNoType			readOnly() default YesNoType.UNKNOWN;
	public TemporalPresentationType			temporal() default TemporalPresentationType.UNKNOWN;

//	public DateType				dateType() default DateType.UNKNOWN;

	/**
	 * A set of strings that indicate the roles a user must 
	 * have to view this field. The permissions 
	 * @return
	 */
	public String[]		viewpermissions() default {};
	public String[]		editpermissions() default {};
	public String		componentTypeHint() default "";

	/**
	 * Defines one or more parameterless validators for this field.
	 * @return
	 */
	public Class<? extends IValueValidator<?>>[]	validator() default {};
	public MetaValueValidator[]						parameterizedValidator() default {};
}
