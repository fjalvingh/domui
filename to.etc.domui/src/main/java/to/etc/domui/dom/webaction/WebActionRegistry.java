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
		@Nullable IWebActionHandler createHandler(@NonNull Class<? extends NodeBase> node, @NonNull String actionCode);
	}

	@NonNull
	final private List<IFactory> m_factoryList = new ArrayList<IFactory>();

	@NonNull
	static private final IWebActionHandler DUMMY = new IWebActionHandler() {
		@Override
		public void handleWebAction(@NonNull NodeBase node, @NonNull RequestContextImpl context, boolean responseExpected) throws Exception {
			throw new IllegalStateException("Stop calling me!");
		}
	};

	@NonNull
	private final Map<Class< ? extends NodeBase>, Map<String, IWebActionHandler>> m_map = new HashMap<Class< ? extends NodeBase>, Map<String, IWebActionHandler>>();

	public WebActionRegistry() {}

	public synchronized void register(@NonNull IFactory f) {
		m_factoryList.add(f);
	}

	@Nullable
	public synchronized IWebActionHandler findActionHandler(@NonNull Class< ? extends NodeBase> node, @NonNull String actionCode) {
		Map<String, IWebActionHandler> map = m_map.get(node);
		if(null != map) {
			IWebActionHandler ah = map.get(actionCode);
			if(null != ah) {
				return ah == DUMMY ? null : ah;
			}
		} else {
			//-- Create a new map.
			map = new HashMap<String, IWebActionHandler>();
			m_map.put(node, map);
		}
		for(IFactory f : m_factoryList) {
			IWebActionHandler handler = f.createHandler(node, actionCode);
			if(null != handler) {
				map.put(actionCode, handler);
				return handler;
			}
		}

		//-- Nothing found. Put the DUMMY handler in the map so next lookup is fast-fail.
		map.put(actionCode, DUMMY);
		return null;
	}
}
