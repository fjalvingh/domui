package to.etc.el;

import javax.servlet.jsp.el.*;

/**
 * This implements a proper expression language decoder for the EL expression
 * language. Besides all that's needed to handle JSP EL expressions this also
 * has code to handle EL expressions used in value binding expressions a la
 * JSF. For this reason it implements some extra interfaces.
 * This is the JSP version of the evaluator which forces the expression to
 * be contained in ${ ... }. This class merely strips the JSP shit and uses
 * the generic evaluator to evaluate what's left.
 *
 * The evaluator uses a cache to keep expressions from being parsed time
 * and time again.
 *
 * Created on May 17, 2005
 * @author jal
 */
public class ExpressionEvaluatorImpl extends ExpressionEvaluator {
	@Override
	public Expression parseExpression(String expression, Class expectedType, FunctionMapper mapper) throws ELException {
		String s = removeJspMuck(expression);
		return SharedEvaluator.getInstance().parseExpression(s, expectedType, mapper, expression);
	}

	@Override
	public Object evaluate(String expression, Class expectedType, VariableResolver resolver, FunctionMapper mapper) throws ELException {
		Expression x = parseExpression(expression, expectedType, mapper);
		return x.evaluate(resolver);
	}

	/**
	 * FIXME Need to use the cache!!
	 * @param property
	 * @return
	 * @throws ELException
	 */
	public PropertyExpression parseProperty(String property) throws ELException {
		CoreElEvaluator e = new CoreElEvaluator();
		return e.evaluatePropertyExpression(property, null);
	}

	static private final String removeJspMuck(String in) throws ELException {
		int len = in.length();
		if(len >= 4) {
			if(in.charAt(0) == '$' && in.charAt(1) == '{' && in.charAt(len - 1) == '}')
				return in.substring(2, len - 1); // Remove ${ and }
		}
		throw new ELException("The EL expression '" + in + "' is not embedded in the silly ${ .... } construct");
	}
}
