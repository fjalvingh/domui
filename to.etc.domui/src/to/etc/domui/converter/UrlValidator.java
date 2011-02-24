package to.etc.domui.converter;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class UrlValidator implements IValueValidator<String> {

	@Override
	public void validate(String input) throws Exception {
		if(input != null) {
			if(!input.matches("^(http|https|ftp)\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&amp;%\\$#\\=~])*$")) {
				throw new ValidationException(Msgs.V_INVALID, input.toString());
			}
		}

	}

}
