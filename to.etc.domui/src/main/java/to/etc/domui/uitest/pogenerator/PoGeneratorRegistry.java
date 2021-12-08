package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.dom.html.NodeBase;
import to.etc.function.BiFunctionEx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
@NonNullByDefault
final public class PoGeneratorRegistry {
	static private final Logger LOG = LoggerFactory.getLogger(PoGeneratorRegistry.class);

	static private final Map<Class<?>, BiFunctionEx<PoGeneratorContext, NodeBase, IPoProxyGenerator>> m_factoryMap = new ConcurrentHashMap<>();

	static public void register(Class<?> componentClass, BiFunctionEx<PoGeneratorContext, NodeBase, IPoProxyGenerator> generatorFactory) {
		if(null != m_factoryMap.put(componentClass, generatorFactory))
			LOG.warn("Overwriting PO proxy generator for " + componentClass);
	}

	@Nullable
	static public IPoProxyGenerator find(PoGeneratorContext ctx, NodeBase node) throws Exception {
		Class<? extends NodeBase> clz = node.getClass();
		BiFunctionEx<PoGeneratorContext, NodeBase, IPoProxyGenerator> factory = m_factoryMap.get(clz);
		if(null == factory)
			return null;
		return factory.apply(ctx, node);
	}
}
