package to.etc.domuidemo.pages.test.binding.binderrors;

import to.etc.domui.converter.IValueValidator;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;

/**
 * Test validator which accepts only the value "good".
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-11-22.
 */
final public class TestValueValidator implements IValueValidator<String> {
	@Override
	public void validate(String input) throws Exception {
		if(null == input)
			return;
		if(input.equals("good"))
			return;
		throw new ValidationException(Msgs.vInvalid, input);
	}
}
