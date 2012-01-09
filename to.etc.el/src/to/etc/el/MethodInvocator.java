package to.etc.el;

import java.lang.reflect.*;

public interface MethodInvocator {
	public Method getMethod();

	public Object getBean();

	public Object invoke(Object[] param) throws Exception;
}
