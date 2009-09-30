package to.etc.domui.component.meta;

import java.lang.annotation.*;

import to.etc.domui.converter.*;

/**
 * Annotation to add metadata to a property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MetaProperty {
	/**
	 * Defines whether the property should be <i>sortable</i> when used in a default table, and defines
	 * the initial sort direction of the property. This defaults to unsortable.
	 * @return
	 */
	public SortableType defaultSortable() default SortableType.UNKNOWN;

	/**
	 * The size, in characters, that should be used as the display size. This is not the max size of the input but only of the
	 * part that is seen in one go.
	 * @return
	 */
	public int displaySize() default -1;

	/**
	 * The actual maximal input length (for a string) or the precision to use (for a number).
	 * @return
	 */
	public int length() default -1;

	/**
	 * Whether the value is a required value. This sadly cannot use boolean because I need a value
	 * representing "not set here but somewhere else" - and annotations cannot use null ([irony]because that
	 * would be helpful and in conformance with the rest of the language[/irony]).
	 * @return
	 */
	public YesNoType required() default YesNoType.UNKNOWN;

	/**
	 * If you have special non-default string-object and object-string conversion rules for this
	 * property you can specify your own converter class doing those conversions here.
	 * @return
	 */
	public Class< ? extends IConverter< ? >> converterClass() default DummyConverter.class;

	/**
	 * Defines this as a readonly (displayonly) property (by default). It defaults to
	 * "unset" meaning that other metadata defines this property. If no other provider sets it
	 * the property is NOT readonly by default. Cannot use "boolean" - see {@link MetaProperty#required()}.
	 * @return
	 */
	public YesNoType readOnly() default YesNoType.UNKNOWN;

	/**
	 * If this is defined as some Date type this further defines the domain (date only, date time etc). It defaults
	 * to UNKNOWN, which is treated much the same as date-time.
	 * @return
	 */
	public TemporalPresentationType temporal() default TemporalPresentationType.UNKNOWN;

	//	public DateType				dateType() default DateType.UNKNOWN;

	/**
	 * A set of strings that indicate the roles a user must
	 * have to view this field. The permissions
	 * @return
	 */
	public String[] viewpermissions() default {};

	public String[] editpermissions() default {};

	/**
	 * This is a hint string which can help a component factory decide how to render a component. The content
	 * is fully free, and individual factories decide which hints they listen to.
	 * @return
	 */
	public String componentTypeHint() default "";

	/**
	 * Defines one or more parameterless validators for this field.
	 * @return
	 */
	public Class< ? extends IValueValidator< ? >>[] validator() default {};

	public MetaValueValidator[] parameterizedValidator() default {};

	/**
	 * Defines the number class AND it's presentation format for numeric values.
	 * @return
	 */
	public NumericPresentation numericPresentation() default NumericPresentation.UNKNOWN;
}
