package to.etc.domui.uitest.pogenerator;

import to.etc.domui.dom.html.NodeBase;
import to.etc.util.Pair;

/**
 * A basic thing that generates just the reference to the specific proxy class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
final public class PogSimple extends AbstractPoProxyGenerator implements IPoProxyGenerator {
	private final Pair<String, String> m_poClass;

	public PogSimple(NodeBase node, Pair<String, String> poClass) {
		super(node);
		m_poClass = poClass;
	}

	public PogSimple(NodeBase node, String poClassName) {
		super(node);
		m_poClass = new Pair<>(PROXYPACKAGE, poClassName);
	}

	@Override
	public void generateCode(PoGeneratorContext context, PoClass rc, String baseName) throws Exception {
		String fieldName = PoGeneratorContext.fieldName(baseName);
		String methodName = PoGeneratorContext.methodName(baseName);

		PoField field = rc.addField(m_poClass, fieldName);
		PoMethod getter = rc.addMethod(field.getType(), "get" + methodName);
		getter.appendLazyInit(field, variable -> {
			getter.append(variable).append(" = ").append("new ");
			getter.appendType(rc, field.getType()).append("(this, ").append(getSelector().selectorAsCode()).append(");").nl();
		});
	}

	@Override
	public String identifier() {
		return m_poClass.get2();
	}
}
