package to.etc.el;

public enum ElToken {
	EOF, LogicalAnd(20), LogicalOr(20), Plus(50), Minus(50), Multiply(60), Divide(60), Modulus(60), GreaterThan(40), LessThan(40), GreaterThanOrEqual(40), LessThanOrEqual(40), Equal(30), NotEqual(30), LogicalNot(
		70), QuestionMark(10), Colon(10), BinaryOr, BinaryAnd, Empty, // The "empty" function keyword
	ParentheseOpen, // (
	ParentheseClose, // )
	Dot, BraceOpen, // [
	BraceClose, // ]
	Comma, True, False, Null, // The 'null' keyword

	//-- Complex, constructed types
	Id, // Identifier
	StringLiteral, // String literal
	IntLiteral, // An integer literal
	FloatLiteral;

	/** If an operator: the precedence of the op */
	private int m_prec;

	ElToken() {
		m_prec = -1;
	}

	ElToken(int prec) {
		m_prec = prec;
	}

	final public int getPrec() {
		return m_prec;
	}
}
