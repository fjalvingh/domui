package to.etc.webapp.query;

import javax.annotation.*;

/**
 * long wrapper field.
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * Created on Feb 3, 2013
 */
public class QFieldLong<R extends QField<R, ? >> {


	QField<R, long[]> m_field;

	public QFieldLong(@Nonnull QField<R, long[]> field) {
		m_field = field;

	}

	public @Nonnull
	R gt(@Nonnull long... t) {
		m_field.eqOrOr(new IRestrictor<long[]>() {
			@Override
			public @Nonnull QOperatorNode restrict(@Nonnull long[] value) {
				return QRestriction.gt(m_field.getPath(), value[0]);
			}
		}, t);
		return m_field.m_root;
	}

	public @Nonnull
	R eq(@Nonnull long... t) {
		m_field.eqOrOr(new IRestrictor<long[]>() {
			@Override
			public @Nonnull QOperatorNode restrict(@Nonnull long[] value) {
				return QRestriction.eq(m_field.getPath(), value[0]);
			}
		}, t);
		return m_field.m_root;
	}

	public @Nonnull
	R ne(@Nonnull long... t) {
		m_field.eqOrOr(new IRestrictor<long[]>() {
			@Override
			public @Nonnull QOperatorNode restrict(@Nonnull long[] value) {
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
