package to.etc.webapp.query;

import javax.annotation.*;

/**
 * boolean wrapper field.
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * Created on Feb 3, 2013
 */
public class QFieldBoolean<R extends QField<R, ? >> {


	QField<R, boolean[]> m_field;

	public QFieldBoolean(@Nonnull QField<R, boolean[]> field) {
		m_field = field;

	}

	public @Nonnull
	R gt(@Nonnull boolean... t) {
		m_field.eqOrOr(new IRestrictor<boolean[]>() {
			@Override
			public @Nonnull QOperatorNode restrict(@Nonnull boolean[] value) {
				return QRestriction.gt(m_field.getPath(), Boolean.valueOf(value[0]));
			}
		}, t);
		return m_field.m_root;
	}

	public @Nonnull
	R eq(@Nonnull boolean... t) {
		m_field.eqOrOr(new IRestrictor<boolean[]>() {
			@Override
			public @Nonnull QOperatorNode restrict(@Nonnull boolean[] value) {
				return QRestriction.eq(m_field.getPath(), Boolean.valueOf(value[0]));
			}
		}, t);
		return m_field.m_root;
	}

	public @Nonnull
	R ne(@Nonnull boolean... t) {
		m_field.eqOrOr(new IRestrictor<boolean[]>() {
			@Override
			public @Nonnull QOperatorNode restrict(@Nonnull boolean[] value) {
				return QRestriction.ne(m_field.getPath(), Boolean.valueOf(value[0]));
			}
		}, t);
		return m_field.m_root;
	}

	final @Nonnull
	String getPath() {
		return m_field.getPath();
	}


}
