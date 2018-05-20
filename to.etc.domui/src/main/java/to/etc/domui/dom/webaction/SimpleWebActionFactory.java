package to.etc.domui.dom.webaction;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.RequestContextImpl;
import to.etc.util.ClassUtil;
import to.etc.util.WrappedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This recognizes all "webAction"+actionName methods that accept IRequestContext as a parameter.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 18, 2013
 */
public class SimpleWebActionFactory implements WebActionRegistry.IFactory {
	@Override
	@Nullable
	public IWebActionHandler createHandler(@NonNull Class< ? extends NodeBase> nodeClass, @NonNull String actionCode) {
		Method method = ClassUtil.findMethod(nodeClass, actionCode, RequestContextImpl.class);
		if(null == method) {
			method = ClassUtil.findMethod(nodeClass, actionCode, IRequestContext.class);
			if(null == method)
				return null;
		}
		final Method theMethod = method;
		return new IWebActionHandler() {
			@Override
			public void handleWebAction(@NonNull NodeBase nodein, @NonNull RequestContextImpl context, boolean responseExpected) throws Exception {
				try {
					Object response = theMethod.invoke(nodein, context);
					if(responseExpected) {
						if(theMethod.getReturnType() != Void.TYPE) {
							JsonWebActionFactory.renderResponse(theMethod, context, response);
						}
					}
				} catch(InvocationTargetException itx) {
					throw WrappedException.unwrap(itx);
				}
			}
		};
	}
}
