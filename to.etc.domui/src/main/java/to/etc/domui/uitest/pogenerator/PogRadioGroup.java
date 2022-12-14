package to.etc.domui.uitest.pogenerator;

import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.RadioButton;
import to.etc.util.Pair;
import to.etc.util.StringTool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 03-02-22.
 */
public class PogRadioGroup extends AbstractPoProxyGenerator implements IPoProxyGenerator {
	private List<Pair<Object, RadioButton<?>>> m_valueList = new ArrayList<>();

	private Class<?> m_typeClass;

	public PogRadioGroup(NodeBase node) {
		super(node);
	}

	@Override
	public void generateCode(PoGeneratorContext context, PoClass rc, String baseName, IPoSelector selector) throws Exception {
		String fieldName = PoGeneratorContext.fieldName(baseName);
		String methodName = PoGeneratorContext.methodName(baseName);

		RefType valueType = new RefType(m_typeClass);

		RefType poClass = new RefType(PROXYPACKAGE, "CpRadioGroup", valueType);

		PoField field = rc.addField(poClass, fieldName);
		PoMethod getter = rc.addMethod(field.getType(), baseName);

		//-- Create a list of selector, value pairs.
		StringBuilder sb = new StringBuilder();
		for(Pair<Object, RadioButton<?>> pair : m_valueList) {
			if(sb.length() > 0)
				sb.append(',');
			renderObjectValue(sb, pair.get1(), rc);
			sb.append(',');
			renderSelector(sb, pair.get2());
		}
		getter.appendLazyInit(field, variable -> {
			getter.append(variable).append(" = ").append("new ");
			getter.appendType(rc, field.getType()).append("(this.wd(), ").append(selector.selectorAsCode())
				.append(", ").append(sb.toString())
				.append(");").nl();
		});
	}

	private void renderSelector(StringBuilder sb, RadioButton<?> rb) {
		String testID = rb.getTestID();
		if(null == testID) {
			throw new IllegalStateException();
		}
		sb.append("\"*[testId='").append(testID).append("']\"");
	}

	private void renderObjectValue(StringBuilder sb, Object value, PoClass rc) {
		if(value == null) {
			sb.append("null");
			return;
		}

		if(value instanceof Boolean) {
			Boolean bo = (Boolean) value;
			sb.append(String.valueOf(bo));
			return;
		}

		if(value instanceof Enum) {
			Class<?> clz = value.getClass();
			RefType enumc = new RefType(clz.getPackageName(), clz.getSimpleName());
			rc.addImport(enumc);

			Enum<?> env = (Enum<?>) value;
			sb.append(clz.getSimpleName()).append('.').append(env.name());
			return;
		}

		if(value instanceof String) {
			StringTool.strToJavascriptString(sb, (String) value, true);
			return;
		}
		throw new IllegalStateException("Don't know how to render a value of type " + value.getClass().getCanonicalName());
	}

	/**
	 * Scan all values for the rg, and add them as choices.
	 */
	@Override
	public GeneratorAccepted acceptChildren(PoGeneratorContext ctx) throws Exception {
		NodeContainer nc = (NodeContainer) m_node;
		List<RadioButton<?>> valueList = (List<RadioButton<?>>) (List<?>) nc.getDeepChildren(RadioButton.class);

		Class<?> typeClass = null;
		List<Object> values = new ArrayList<>();
		for(RadioButton<?> vl : valueList) {
			String testID = vl.getTestID();
			if(null == testID) {
				ctx.error("No test ID for radiobutton " + vl);
				return GeneratorAccepted.RefusedIgnoreChildren;
			}

			Object buttonValue = vl.getButtonValue();
			m_valueList.add(new Pair<>(buttonValue, vl));
			values.add(buttonValue);
			if(buttonValue != null) {
				Class<?> valueClass = buttonValue.getClass();
				if(null == typeClass) {
					typeClass = valueClass;
				} else {
					if(! typeClass.isAssignableFrom(valueClass)) {
						if(valueClass.isAssignableFrom(typeClass)) {
							typeClass = valueClass;
						} else {
							ctx.error("Cannot recognise data type for Radio group: classes " + typeClass + " and " + valueClass);
							return GeneratorAccepted.RefusedIgnoreChildren;
						}
					}
				}
			}
		}
		if(null == typeClass) {
			ctx.error("Cannot recognise data type for Radio group - no common types");
			return GeneratorAccepted.RefusedIgnoreChildren;
		}

		//System.out.println("rg type is " + typeClass);
		m_typeClass = typeClass;
		return GeneratorAccepted.Accepted;
	}

	@Override
	public String identifier() {
		throw new IllegalStateException();
	}
}
