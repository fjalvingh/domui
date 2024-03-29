package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.buttons.CheckboxButton;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.input.ComboFixed;
import to.etc.domui.component.input.ComboLookup;
import to.etc.domui.component.input.LookupInput;
import to.etc.domui.component.input.Text;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.Window;
import to.etc.domui.component.misc.DisplaySpan;
import to.etc.domui.component.searchpanel.lookupcontrols.NumberLookupControl;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.lookupinput.LookupInput2;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.Input;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.RadioGroup;
import to.etc.domui.dom.html.TextArea;
import to.etc.domui.uitest.pogenerator.PogSimple.AllowEmbedded;
import to.etc.function.BiFunctionEx;
import to.etc.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
@NonNullByDefault
final public class PoGeneratorRegistry {
	static private final Logger LOG = LoggerFactory.getLogger(PoGeneratorRegistry.class);

	static private final Map<Class<?>, BiFunctionEx<PoGeneratorContext, NodeBase, IPoProxyGenerator>> m_factoryMap = new ConcurrentHashMap<>();

	static private final List<Pair<Class<?>, BiFunctionEx<PoGeneratorContext, NodeBase, IPoProxyGenerator>>> m_extendsMap = new CopyOnWriteArrayList<>();

	static public void register(Class<?> componentClass, BiFunctionEx<PoGeneratorContext, NodeBase, IPoProxyGenerator> generatorFactory) {
		if(null != m_factoryMap.put(componentClass, generatorFactory))
			LOG.warn("Overwriting PO proxy generator for " + componentClass);
	}

	static public void registerExtends(Class<?> componentClass, BiFunctionEx<PoGeneratorContext, NodeBase, IPoProxyGenerator> generatorFactory) {
		m_extendsMap.add(new Pair<>(componentClass, generatorFactory));
	}

	@Nullable
	static public IPoProxyGenerator find(PoGeneratorContext ctx, NodeBase node) throws Exception {
		Class<? extends NodeBase> clz = node.getClass();
		BiFunctionEx<PoGeneratorContext, NodeBase, IPoProxyGenerator> factory = m_factoryMap.get(clz);
		if(null != factory) {
			return factory.apply(ctx, node);
		}
		for(Pair<Class<?>, BiFunctionEx<PoGeneratorContext, NodeBase, IPoProxyGenerator>> pair : m_extendsMap) {
			if(pair.get1().isAssignableFrom(clz)) {
				return pair.get2().apply(ctx, node);
			}
		}

		return null;
	}

	/**
	 * A generator that just presents the node itself as something.
	 */
	static public IPoProxyGenerator getDisplayTextGenerator(PoGeneratorContext context, NodeBase from) {
		return new PogSimple(from, "CpNodeAsText");
	}

	static {
		register(DefaultButton.class, (ctx, node) -> new PogSimple(node, "CpButton"));
		register(Checkbox.class, (ctx, node) -> new PogSimple(node, "CpCheckbox"));
		register(CheckboxButton.class, (ctx, node) -> new PogSimple(node, "CpCheckboxButton"));

		register(ComboLookup.class, (ctx, node) -> new PogSimple(node, "CpComboLookup"));
		register(ComboLookup2.class, (ctx, node) -> new PogSimple(node, "CpComboLookup2"));
		register(ComboFixed.class, (ctx, node) -> new PogSimple(node, "CpComboFixed"));
		register(ComboFixed2.class, (ctx, node) -> new PogSimple(node, "CpComboFixed2"));

		register(DisplaySpan.class, (ctx, node) -> new PogSimple(node, "CpDisplaySpan", AllowEmbedded.DisallowEmbedded));

		register(LinkButton.class, (ctx, node) -> new PogSimple(node, "CpLinkButton"));
		register(LookupInput2.class, (ctx, node) -> new PogSimple(node, "CpLookupInput2"));
		register(LookupInput.class, (ctx, node) -> new PogSimple(node, "CpLookupInput"));


		register(NumberLookupControl.class, (ctx, node) -> new PogSimple(node, "CpNumberLookupControl"));

		register(RadioGroup.class, (ctx, node) -> new PogRadioGroup(node));

		register(SmallImgButton.class, (ctx, node) -> new PogSimple(node, "CpButton"));
		register(Text.class, (ctx, node) -> new PogSimple(node, "CpText"));
		register(Text2.class, (ctx, node) -> new PogSimple(node, "CpText2"));
		register(TextArea.class, (ctx, node) -> new PogSimple(node, "CpTextArea"));

		//-- Base HTML components
		register(Input.class, (ctx, node) -> new PogSimple(node, "CpHtmlInput"));

		//-- Complex
		register(DataTable.class, (ctx, node) -> new PogDataTable(node));
		registerExtends(Window.class, (ctx, node) -> new PogWindow(node));

	}
}
