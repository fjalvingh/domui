package to.etc.domui.converter;

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
		if(input.doubleValue() >= 0.0)
			return;
		throw new ValidationException(Msgs.V_TOOSMALL, "0");
	}
}
