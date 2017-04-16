package to.etc.webapp.query;

import javax.annotation.*;

/**
 * double wrapper field.
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * Created on Feb 3, 2013
 */
public class QFieldDouble<R extends QField<R, ? >> {


	QField<R, double[]> m_field;

	public QFieldDouble(@Nonnull QField<R, double[]> field) {
		m_field = field;

	}

	public @Nonnull
	R gt(@Nonnull double... t) {
		m_field.eqOrOr(new IRestrictor<double[]>() {
			@Override
			public @Nonnull QOperatorNode restrict(@Nonnull double[] value) {
				return QRestriction.gt(m_field.getPath(), value[0]);
			}
		}, t);
		return m_field.m_root;
	}

	public @Nonnull
	R eq(@Nonnull double... t) {
		m_field.eqOrOr(new IRestrictor<double[]>() {
			@Override
			public @Nonnull QOperatorNode restrict(@Nonnull double[] value) {
				return QRestriction.eq(m_field.getPath(), value[0]);
			}
		}, t);
		return m_field.m_root;
	}

	public @Nonnull
	R ne(@Nonnull double... t) {
		m_field.eqOrOr(new IRestrictor<double[]>() {
			@Override
			public @Nonnull QOperatorNode restrict(@Nonnull double[] value) {
				return QRestriction.ne(m_field.getPath(), value[0]);
			}
		}, t);
		return m_field.m_root;
	}

	final @Nonnull
	String getPath() {
		return m_field.getPath();
	}


}
