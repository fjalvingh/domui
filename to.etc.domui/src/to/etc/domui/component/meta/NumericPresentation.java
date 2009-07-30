package to.etc.domui.component.meta;

/**
 * Default numeric representations that can be used.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 30, 2009
 */
public enum NumericPresentation {
	/** No explicit presentation set. */
	UNKNOWN,

	/** Just a run-of-the-mill number */
	NUMBER,

	/** Default monetary representation. */
	MONEY,

	/** Display as amount with currency symbol, thousands separators, decimal point and complete fraction (always) */
	MONEY_FULL,

	/** Display as full, but if the fraction (cents) is all zeroes remove them */
	MONEY_FULL_TRUNC,

	/** Display as a number having thousands separator and fraction but without currency symbol */
	MONEY_NO_SYMBOL,

	/** Just the amount, including faction, without any unnecessary adornments */
	MONEY_NUMERIC,
}
