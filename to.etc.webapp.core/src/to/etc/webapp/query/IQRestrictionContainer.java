package to.etc.webapp.query;

/**
 * Helper interface for constructing restriction trees.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 23, 2009
 */
interface IQRestrictionContainer {
	void setRestrictions(QOperatorNode r);

	QOperatorNode getRestrictions();
}
