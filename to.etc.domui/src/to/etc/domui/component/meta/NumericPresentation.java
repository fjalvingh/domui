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

	/** Just a run-of-the-mill number, containing only a fraction separator when needed */
	NUMBER,

	/** Presented in full precision, moving to scientific (x.xxEyy) mode when needed */
	NUMBER_SCIENTIFIC,

	/** Fully embellished number, with thousand separators and such */
	NUMBER_FULL,

	/** Default monetary representation. */
	MONEY,

	/** Display as amount with currency symbol, thousands separators, decimal point and complete fraction (always) */
	MONEY_FULL,

	/** Display as full, but if the fraction (cents) is all zeroes remove them */
	MONEY_FULL_TRUNC,

	/** Display as a number having thousands separator and fraction but without currency symbol */
	MONEY_NO_SYMBOL,

	/** Just the amount, including faction, without any unnecessary adornments */
	MONEY_NUMERIC;

	static public boolean isMonetary(NumericPresentation np) {
		switch(np){
			default:
				return false;
			case MONEY:
			case MONEY_FULL:
			case MONEY_FULL_TRUNC:
			case MONEY_NO_SYMBOL:
			case MONEY_NUMERIC:
				return true;
		}
	}

}
