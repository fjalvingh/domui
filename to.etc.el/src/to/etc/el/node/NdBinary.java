package to.etc.el.node;

import java.io.*;

import javax.servlet.jsp.el.*;

import to.etc.el.*;

abstract public class NdBinary extends NdBase {
	protected NdBase m_a;

	protected NdBase m_b;

	private ElToken m_t;

	/**
	 * Returns the language form of the expression.
	 * @return
	 */
	abstract protected String getOperator();

	public NdBinary(NdBase a, NdBase b) {
		m_a = a;
		m_b = b;
	}

	/**
	 * @see to.etc.el.node.NdBase#dump(to.etc.el.node.IndentWriter)
	 */
	@Override
	public void dump(IndentWriter w) throws IOException {
		w.println(getNodeName() + " " + m_t);
		w.inc();
		w.print("A=");
		m_a.dump(w);
		w.dec();
		w.forceNewline();
		w.inc();
		w.print("B=");
		m_b.dump(w);
		w.dec();
		w.forceNewline();
	}

	static public NdBase create(ElToken t, NdBase a, NdBase b) throws ELException {
		switch(t){
			default:
				throw new IllegalStateException("Unknown opcode " + t);
				//				NdBinary	res	= new NdBinary(a, b);
				//				res.m_t = t;
				//				return res;

				//-- Arithmetic
			case Minus:
				return new NdSubtract(a, b);
			case Plus:
				return new NdAdd(a, b);
			case Multiply:
				return new NdMultiply(a, b);
			case Divide:
				return new NdDivide(a, b);
			case Modulus:
				return new NdModulus(a, b);

				//-- Boolean comparators
			case GreaterThan:
				return new NdGreaterThan(a, b);
			case LessThan:
				return new NdLessThan(a, b);
			case LessThanOrEqual:
				return new NdLessThanOrEqual(a, b);
			case GreaterThanOrEqual:
				return new NdGreaterThanOrEqual(a, b);
			case Equal:
				return new NdEqual(a, b);
			case NotEqual:
				return new NdNotEqual(a, b);

				//-- Boolean combinators
			case LogicalAnd:
				return new NdLogicalAnd(a, b);
			case LogicalOr:
				return new NdLogicalOr(a, b);

				//-- Tertiary ? : operator.
			case QuestionMark:
				return new NdQuestion(a, b); // Initially forms [cond, ifpart, null]

			case Colon:
				return handleTertiary(a, b); // Merge tertiary
		}
	}

	/**
	 * This merges the tertiary operator when it's colon is encountered. When
	 * called we MUST have a 'Question' operation in a. We merge the new piece
	 * into the tertiary code.
	 *
	 * @param a
	 * @param b
	 * @return
	 * @throws ELException
	 */
	static private NdBase handleTertiary(NdBase a, NdBase b) throws ELException {
		if(!(a instanceof NdQuestion))
			throw new ELException("Unexpected ':'");
		NdQuestion q = (NdQuestion) a;
		q.setElse(b);
		return q;
	}


	@Override
	public void getExpression(Appendable a) throws IOException {
		a.append('(');
		m_a.getExpression(a);
		a.append(getOperator());
		m_b.getExpression(a);
		a.append(')');
	}
}
