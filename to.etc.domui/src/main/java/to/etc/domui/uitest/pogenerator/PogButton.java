package to.etc.domui.uitest.pogenerator;

import to.etc.domui.dom.html.NodeBase;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
public class PogButton extends AbstractPoProxyGenerator implements IPoProxyGenerator {
	public PogButton(NodeBase node) {
		super(node);
	}

	@Override
	public void generateCode(PoGeneratorContext context, PoClass rc, String baseName) throws Exception {
		String fieldName = PoGeneratorContext.fieldName(baseName);
		String methodName = PoGeneratorContext.methodName(baseName);

		PoField field = rc.addField(PROXYPACKAGE, "ButtonPO", fieldName);
		PoMethod getter = rc.addMethod(field.getType(), "get" + methodName);
		getter.appendLazyInit(field, variable -> {
			getter.append(variable).append(" = ").append("new ");
			getter.appendType(rc, field.getType()).append("(this, ").append(getSelector().selectorAsCode()).append(");").nl();
		});
	}

	@Override
	public String identifier() {
		return "Button";
	}
}
