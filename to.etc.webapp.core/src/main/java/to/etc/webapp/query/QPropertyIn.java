package to.etc.webapp.query;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/24/15.
 */
public class QPropertyIn extends QOperatorNode {
	@NonNull
	final private String m_property;

	@NonNull
	final private QOperatorNode m_expr;

	public QPropertyIn(@NonNull QOperation operation, @NonNull String property, @NonNull QOperatorNode expr) {
		super(operation);
		m_property = property;
		m_expr = expr;
	}

	@Override
	@NonNull
	public QPropertyIn dup() {
		return new QPropertyIn(getOperation(), getProperty(), getExpr().dup());
	}

	@Override
	public void visit(@NonNull QNodeVisitor v) throws Exception {
		v.visitPropertyIn(this);
	}

	@NonNull
	public QOperatorNode getExpr() {
		return m_expr;
	}

	@NonNull
	public String getProperty() {
		return m_property;
	}
}
