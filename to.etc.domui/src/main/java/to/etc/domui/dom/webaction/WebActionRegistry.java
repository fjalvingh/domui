package to.etc.domui.dom.webaction;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.server.RequestContextImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This factory-based web action registry finds a handler method to use for a web action, i.e.
 * something that enters {@link NodeBase#componentHandleWebAction(RequestContextImpl, String)}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 18, 2013
 */
public class WebActionRegistry {
	public interface IFactory {
		@Nullable
		IWebActionHandler createHandler(@NonNull Class<? extends NodeBase> node, @NonNull String actionMethodName);
	}

	@NonNull
	final private List<IFactory> m_factoryList = new ArrayList<IFactory>();

	@NonNull
	static private final IWebActionHandler DUMMY = (node, context, responseExpected) -> {
		throw new IllegalStateException("Stop calling me!");
	};

	@NonNull
	private final Map<Class<? extends NodeBase>, Map<String, IWebActionHandler>> m_map = new HashMap<>();

	public WebActionRegistry() {
	}

	public synchronized void register(@NonNull IFactory f) {
		m_factoryList.add(f);
	}

	@Nullable
	public synchronized IWebActionHandler findActionHandler(@NonNull Class<? extends NodeBase> node, @NonNull String actionMethodName) {
		Map<String, IWebActionHandler> map = m_map.get(node);
		if(null != map) {
			IWebActionHandler ah = map.get(actionMethodName);
			if(null != ah) {
				return ah == DUMMY ? null : ah;
			}
		} else {
			//-- Create a new map.
			map = new HashMap<String, IWebActionHandler>();
			m_map.put(node, map);
		}
		for(IFactory f : m_factoryList) {
			IWebActionHandler handler = f.createHandler(node, actionMethodName);
			if(null != handler) {
				map.put(actionMethodName, handler);
				return handler;
			}
		}

		//-- Nothing found. Put the DUMMY handler in the map so next lookup is fast-fail.
		map.put(actionMethodName, DUMMY);
		return null;
	}
}
