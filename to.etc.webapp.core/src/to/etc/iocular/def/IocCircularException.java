package to.etc.iocular.def;

import java.util.Stack;

/**
 * Circular reference exception.
 *
 * @author jal
 * Created on Apr 9, 2007
 */
public class IocCircularException extends IocConfigurationException {
	public IocCircularException(ComponentBuilder cb, Stack<ComponentBuilder> stack, String what) {
		super(cb, what);
	}

}
