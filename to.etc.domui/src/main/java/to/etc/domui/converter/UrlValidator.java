package to.etc.domui.converter;

import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;

import java.net.MalformedURLException;
import java.net.URL;

final public class UrlValidator implements IValueValidator<String> {
	@Override public void validate(String input) throws Exception {
		if(null == input) {
			return;
		}
		try {
			new URL(input);
		} catch(MalformedURLException e) {
			throw new ValidationException(Msgs.notValid, input);
		}
	}
}
