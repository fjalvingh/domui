package to.etc.domui.converter;

import java.math.*;

import javax.annotation.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

/**
 * Checks if the number is zero or greater then zero. if the number is smaller
 * then zero, it will return a ValidationException.
 *
 * @author <a href="mailto:marc.mol@itris.nl">Marc Mol</a>
 * Created on Mar 25, 2011
 */
public class PositiveNumberValidator implements IValueValidator<Number> {

	@Override
	public void validate(@Nullable Number input) throws Exception {

		if(input == null)
			return;

		if(input instanceof Byte) {
			if(input.byteValue() >= 0)
				return;
		} else if(input instanceof Short) {
			if(input.shortValue() >= 0)
				return;
		} else if(input instanceof Integer) {
			if(input.intValue() >= 0)
				return;
		} else if(input instanceof Long) {
			if(input.longValue() >= 0)
				return;
		} else if(input instanceof Float) {
			if(input.floatValue() >= 0)
				return;
		} else if(input instanceof Double) {
			if(input.doubleValue() >= 0)
				return;
		} else if(input instanceof BigDecimal) {
			if(((BigDecimal) input).compareTo(BigDecimal.ZERO) >= 0)
				return;
		} else
			throw new IllegalArgumentException("PositiveNumberValidator not implemented for " + input.getClass());

		throw new ValidationException(Msgs.V_TOOSMALL, "0");
	}
}
