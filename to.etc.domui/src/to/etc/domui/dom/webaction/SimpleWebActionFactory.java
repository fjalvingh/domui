package to.etc.domui.dom.webaction;

import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.util.*;

/**
 * This recognizes all "webAction"+actionName methods that accept IRequestContext as a parameter.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 18, 2013
 */
public class SimpleWebActionFactory implements WebActionRegistry.IFactory {
	@Override
	@Nullable
	public IWebActionHandler createHandler(@Nonnull Class< ? extends NodeBase> nodeClass, @Nonnull String actionCode) {
		String name = "webAction" + actionCode;
		Method method = ClassUtil.findMethod(nodeClass, name, RequestContextImpl.class);
		if(null == method) {
			method = ClassUtil.findMethod(nodeClass, name, IRequestContext.class);
			if(null == method)
				return null;
		}
		final Method drophandler = method;
		return new IWebActionHandler() {
			@Override
			public void handleWebAction(@Nonnull NodeBase nodein, @Nonnull IRequestContext context) throws Exception {
				try {
					drophandler.invoke(nodein, context);
				} catch(InvocationTargetException itx) {
					throw WrappedException.unwrap(itx);
				}

			}
		};
	}
}
