package to.etc.domui.uitest.pogenerator;

import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

import java.util.List;

/**
 * A basic thing that generates just the reference to the specific proxy class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
final public class PogSimple extends AbstractPoProxyGenerator implements IPoProxyGenerator {
	private final RefType m_poClass;

	private final AllowEmbedded m_embedded;

	public enum AllowEmbedded {
		AllowEmbedded,
		DisallowEmbedded
	}

	public PogSimple(NodeBase node, RefType poClass) {
		super(node);
		m_poClass = poClass;
		m_embedded = AllowEmbedded.AllowEmbedded;
	}

	public PogSimple(NodeBase node, RefType poClass, AllowEmbedded embedded) {
		super(node);
		m_poClass = poClass;
		m_embedded = embedded;
	}

	public PogSimple(NodeBase node, String poClassName) {
		super(node);
		m_poClass = new RefType(PROXYPACKAGE, poClassName);
		m_embedded = AllowEmbedded.AllowEmbedded;
	}

	public PogSimple(NodeBase node, String poClassName, AllowEmbedded embedded) {
		super(node);
		m_poClass = new RefType(PROXYPACKAGE, poClassName);
		m_embedded = embedded;
	}

	@Override
	public void generateCode(PoGeneratorContext context, PoClass rc, String baseName, IPoSelector selector) throws Exception {
		String fieldName = PoGeneratorContext.fieldName(baseName);
		String methodName = PoGeneratorContext.methodName(baseName);

		PoField field = rc.addField(m_poClass, fieldName);
		PoMethod getter = rc.addMethod(field.getType(), baseName);
		getter.appendLazyInit(field, variable -> {
			getter.append(variable).append(" = ").append("new ");
			getter.appendType(rc, field.getType()).append("(this.wd(), ").append(selector.selectorAsCode()).append(");").nl();
		});
	}

	@Override
	public GeneratorAccepted acceptChildren(PoGeneratorContext ctx) throws Exception {
		switch(m_embedded) {
			default:
				throw new IllegalStateException(m_embedded + "??");		// Idiots.

			case AllowEmbedded:
				return GeneratorAccepted.Accepted;

			case DisallowEmbedded:
				//-- Do we have controls inside this node?
				if(m_node instanceof NodeContainer) {
					List<NodeGeneratorPair> list = ctx.createGenerators((NodeContainer) m_node);
					return list.isEmpty() ? GeneratorAccepted.Accepted : GeneratorAccepted.RefusedScanChildren;
				}
				return GeneratorAccepted.Accepted;
		}
	}

	@Override
	public String identifier() {
		return m_poClass.getTypeName();
	}
}
