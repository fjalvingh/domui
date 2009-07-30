package to.etc.domui.component.meta;

/**
 * Represents a toplevel specification of what a numeric value represents. It's most important
 * use is to distinguish between things like simple numbers, monetary values, percentages
 * etc.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 30, 2009
 */
public enum NumericType {
	/** Not explicity defined; treated as just a number. */
	UNKNOWN,

	/** It's just a number. */
	NUMBER,

	/** This represents some monetary amount */
	AMOUNT,

	/** Represents a duration. */
	DURATION,

	/** Represents a percentage (0..100, fractional) */
	PERCENTAGE,

	/** Represents some factor (fractional) */
	FACTOR
}
