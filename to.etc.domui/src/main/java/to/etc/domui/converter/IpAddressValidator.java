package to.etc.domui.converter;

import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-03-22.
 */
final public class IpAddressValidator implements IValueValidator<String> {
	@Override
	public void validate(String input) throws Exception {
		if(null == input)
			return;
		input = input.trim();
		if(input.isEmpty())
			return;

		String[] segments = input.split("\\.");
		if(segments.length == 4) {
			for(String segment : segments) {
				segment = segment.trim();
				try {
					int val = Integer.parseInt(segment);
					if(val < 0 || val > 255)
						throw new ValidationException(Msgs.vInvalid, input);
				} catch(ValidationException vx) {
					throw vx;
				} catch(Exception x) {
					throw new ValidationException(Msgs.vInvalid, input);
				}
			}
			return;
		}
		throw new ValidationException(Msgs.vInvalid, input);
	}
}
