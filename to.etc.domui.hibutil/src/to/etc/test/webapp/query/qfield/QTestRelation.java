package to.etc.test.webapp.query.qfield;
import javax.annotation.*;

import to.etc.webapp.query.*;
/**
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 */
@Generated(value = { "This is a generated file. It will be overwritten during compilation. It is therefore useless to make any modifications." })
public class QTestRelation<R extends QField<R, ? >> extends QField<R,to.etc.test.webapp.query.qfield.TestRelation> {

	QTestRelation(@Nullable R root, @Nullable QField<R, ? > parent, @Nullable String name) {
		super(root, parent, name);
	}

	public final @Nonnull
	QFieldDouble<R> anum() {
		return new QFieldDouble<R>(new QField<R, double[]>(m_root, this, "anum"));
	}

	/**
	 * Shortcut eq
	 * @param anum
	 * @return
	 */
	public final @Nonnull
	R anum(@Nonnull double... anum) {
		return anum().eq(anum);
	}

	public final @Nonnull
	QList<R, QTestBankRoot> banks() throws Exception {
		return new QList<R, QTestBankRoot>(new QTestBankRoot(), this, "banks");
	}

	public final @Nonnull
	QTestBankAccount<R> preferredAccount() {
		return new QTestBankAccount<R>(m_root, this, "preferredAccount");
	}

	/**
	 * Shortcut eq
	 * @param preferredAccount
	 * @return
	 */
	public final @Nonnull
	R preferredAccount(@Nonnull to.etc.test.webapp.query.qfield.TestBankAccount... preferredAccount) {
		return preferredAccount().eq(preferredAccount);
	}

	public final @Nonnull
	QField<R,java.lang.String> properName() {
		return new QField<R,java.lang.String>(m_root, this, "properName");
	}

	/**
	 * Shortcut eq
	 * @param properName
	 * @return
	 */
	public final @Nonnull
	R properName(@Nonnull java.lang.String... properName) {
		return properName().eq(properName);
	}

	public final @Nonnull
	QField<R,java.util.Date> logDate() {
		return new QField<R,java.util.Date>(m_root, this, "logDate");
	}

	/**
	 * Shortcut eq
	 * @param logDate
	 * @return
	 */
	public final @Nonnull
	R logDate(@Nonnull java.util.Date... logDate) {
		return logDate().eq(logDate);
	}

	public final @Nonnull
	QField<R,java.lang.String> logModule() {
		return new QField<R,java.lang.String>(m_root, this, "logModule");
	}

	/**
	 * Shortcut eq
	 * @param logModule
	 * @return
	 */
	public final @Nonnull
	R logModule(@Nonnull java.lang.String... logModule) {
		return logModule().eq(logModule);
	}

	public final @Nonnull
	QField<R,java.lang.String> logTime() {
		return new QField<R,java.lang.String>(m_root, this, "logTime");
	}

	/**
	 * Shortcut eq
	 * @param logTime
	 * @return
	 */
	public final @Nonnull
	R logTime(@Nonnull java.lang.String... logTime) {
		return logTime().eq(logTime);
	}

	public final @Nonnull
	QField<R,java.lang.String> logUser() {
		return new QField<R,java.lang.String>(m_root, this, "logUser");
	}

	/**
	 * Shortcut eq
	 * @param logUser
	 * @return
	 */
	public final @Nonnull
	R logUser(@Nonnull java.lang.String... logUser) {
		return logUser().eq(logUser);
	}

	public final @Nonnull
	QField<R,java.lang.Long> tcn() {
		return new QField<R,java.lang.Long>(m_root, this, "tcn");
	}

	/**
	 * Shortcut eq
	 * @param tcn
	 * @return
	 */
	public final @Nonnull
	R tcn(@Nonnull java.lang.Long... tcn) {
		return tcn().eq(tcn);
	}

	public static final @Nonnull
	QTestRelationRoot get() {
		return new QTestRelationRoot();
	}
}