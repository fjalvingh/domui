package to.etc.domui.converter;

import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;
import to.etc.util.StringTool;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-05-20.
 */
final public class EmailValidator implements IValueValidator<String> {
	@Override
	public void validate(String input) throws Exception {
		if(!StringTool.isValidEmail(input)) {
			throw new ValidationException(Msgs.vInvalidEmail);
		}
	}
}
