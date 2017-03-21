package to.etc.webapp.query;

import javax.annotation.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/24/15.
 */
public class QPropertyIn extends QOperatorNode {
	@Nonnull
	final private String m_property;

	@Nonnull
	final private QOperatorNode m_expr;

	public QPropertyIn(@Nonnull QOperation operation, @Nonnull String property, @Nonnull QOperatorNode expr) {
		super(operation);
		m_property = property;
		m_expr = expr;
	}

	@Override
	@Nonnull
	public QPropertyIn dup() {
		return new QPropertyIn(getOperation(), getProperty(), getExpr().dup());
	}

	@Override
	public void visit(@Nonnull QNodeVisitor v) throws Exception {
		v.visitPropertyIn(this);
	}

	@Nonnull
	public QOperatorNode getExpr() {
		return m_expr;
	}

	@Nonnull
	public String getProperty() {
		return m_property;
	}
}
