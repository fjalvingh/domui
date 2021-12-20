package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.input.ComboLookup;
import to.etc.domui.component.input.LookupInput;
import to.etc.domui.component.input.Text;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.misc.DisplaySpan;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.lookupinput.LookupInput2;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TextArea;
import to.etc.domui.uitest.pogenerator.PogSimple.AllowEmbedded;
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

	/**
	 * A generator that just presents the node itself as something.
	 */
	static public IPoProxyGenerator getDisplayTextGenerator(PoGeneratorContext context, NodeBase from) {
		return new PogSimple(from, "CpNodeAsText");
	}

	static {
		register(Checkbox.class, (ctx, node) -> new PogSimple(node, "CpCheckbox"));
		register(DefaultButton.class, (ctx, node) -> new PogSimple(node, "CpButton"));
		register(Text.class, (ctx, node) -> new PogSimple(node, "CpText"));
		register(Text2.class, (ctx, node) -> new PogSimple(node, "CpText2"));
		register(LookupInput2.class, (ctx, node) -> new PogSimple(node, "CpLookupInput2"));
		register(LookupInput.class, (ctx, node) -> new PogSimple(node, "CpLookupInput"));
		register(LinkButton.class, (ctx, node) -> new PogSimple(node, "CpLinkButton"));
		register(SmallImgButton.class, (ctx, node) -> new PogSimple(node, "CpButton"));
		register(ComboLookup.class, (ctx, node) -> new PogSimple(node, "CpComboLookup"));
		register(ComboLookup2.class, (ctx, node) -> new PogSimple(node, "CpComboLookup2"));
		register(DisplaySpan.class, (ctx, node) -> new PogSimple(node, "CpDisplaySpan", AllowEmbedded.DisallowEmbedded));
		register(TextArea.class, (ctx, node) -> new PogSimple(node, "CpTextArea"));

		register(DataTable.class, (ctx, node) -> new PogDataTable(node));

	}
}
