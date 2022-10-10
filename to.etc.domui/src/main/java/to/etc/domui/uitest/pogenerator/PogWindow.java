package to.etc.domui.uitest.pogenerator;

import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.layout.Window;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Generated windows and dialogs into a separate class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-10-22.
 */
final public class PogWindow extends AbstractPoProxyGenerator implements IPoProxyGenerator {
	private static final RefType DIALOGBASECLASS = new RefType(PROXYPACKAGE, "CpWindow");

	static private final RefType SUPPLIER = new RefType(Supplier.class, RefType.STRING);

	private List<NodeGeneratorPair> m_contentGens;

	public PogWindow(NodeBase node) {
		super(node);
	}

	/**
	 * Get generators for the content parts of the window.
	 */
	@Override
	public GeneratorAccepted acceptChildren(PoGeneratorContext ctx) throws Exception {
		NodeContainer content = (NodeContainer) PoGeneratorContext.findNodeByCssClassUndelegated((NodeContainer) m_node, "ui-flw-c");
		if(null == content) {
			return GeneratorAccepted.RefusedIgnoreChildren;
		}
		NodeBase topContent = PoGeneratorContext.findNodeByCssClassUndelegated((NodeContainer) m_node, "ui-flw-tc");
		NodeBase bottomContent = PoGeneratorContext.findNodeByCssClassUndelegated((NodeContainer) m_node, "ui-flw-bc");

		//-- Collect generators for all children of the dialog
		List<NodeGeneratorPair> contentGenerators = ctx.createGenerators(content);
		if(contentGenerators.size() == 0)
			return GeneratorAccepted.RefusedIgnoreChildren;
		if(bottomContent instanceof NodeContainer) {
			ctx.createGenerators(contentGenerators, (NodeContainer) bottomContent);
		}
		m_contentGens = contentGenerators;
		return GeneratorAccepted.Accepted;
	}

	/**
	 * Delegate the prepare call to all components inside the window.
	 */
	@Override
	public void prepare(PoGeneratorContext context) throws Exception {
		for(NodeGeneratorPair pair : m_contentGens) {
			pair.getGenerator().prepare(context);
		}
	}

	/**
	 * Generates the Dialog's class and its content models.
	 */
	@Override
	public void generateCode(PoGeneratorContext context, PoClass intoClass, String baseName, IPoSelector selector) throws Exception {
		//-- Is the class subclassed?
		String dlgName;
		Class<? extends NodeBase> clz = m_node.getClass();
		if(clz == Window.class || clz == Dialog.class) {
			//-- Not subclassed; use a generic base name for now
			dlgName = "Dialog";
		} else {
			dlgName = clz.getSimpleName();
		}

		PoClass dialogClass = context.addClass(dlgName + "PO", DIALOGBASECLASS, Collections.emptyList());
		PoMethod cons = dialogClass.addConstructor();
		cons.addParameter(PoGeneratorContext.WDCONNECTOR, "connector");
		//cons.addParameter(SUPPLIER, "selectorProvider");
		cons.append("super(connector, ").append(selector.selectorAsCode()).append(");").nl();

		for(NodeGeneratorPair pair : m_contentGens) {
			generateCode(context, pair, dialogClass);
		}
	}

	private void generateCode(PoGeneratorContext context, NodeGeneratorPair pair, PoClass rc) throws Exception {
		String baseName = rc.getBaseName(pair.getNode());
		String testID = Objects.requireNonNull(pair.getNode().getTestID(), "Unexpected: testID should not be null here ever");
		pair.getGenerator().generateCode(context, rc, baseName, new PoSelectorTestId(testID));
	}

	@Override
	public String identifier() {
		return "Window";
	}
}
