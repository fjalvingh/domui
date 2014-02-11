package to.etc.test.webapp.query.qfield;
import javax.annotation.*;

import to.etc.webapp.query.*;
/**
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 */
@Generated(value = { "This is a generated file. It will be overwritten during compilation. It is therefore useless to make any modifications." })
public class QTestBankAccount<R extends QField<R, ? >> extends QField<R,to.etc.test.webapp.query.qfield.TestBankAccount> {

	QTestBankAccount(@Nullable R root, @Nullable QField<R, ? > parent, @Nullable String name) {
		super(root, parent, name);
	}

	public final @Nonnull
	QTestBank<R> bank() {
		return new QTestBank<R>(m_root, this, "bank");
	}

	/**
	 * Shortcut eq
	 * @param bank
	 * @return
	 */
	public final @Nonnull
	R bank(@Nonnull TestBank... bank) {
		return bank().eq(bank);
	}

	public final @Nonnull
	QField<R,java.lang.String> bban() {
		return new QField<R,java.lang.String>(m_root, this, "bban");
	}

	/**
	 * Shortcut eq
	 * @param bban
	 * @return
	 */
	public final @Nonnull
	R bban(@Nonnull java.lang.String... bban) {
		return bban().eq(bban);
	}

	public final @Nonnull
	QTestRelation<R> copiedFromPayer() {
		return new QTestRelation<R>(m_root, this, "copiedFromPayer");
	}

	/**
	 * Shortcut eq
	 * @param copiedFromPayer
	 * @return
	 */
	public final @Nonnull
	R copiedFromPayer(@Nonnull TestRelation... copiedFromPayer) {
		return copiedFromPayer().eq(copiedFromPayer);
	}

	public final @Nonnull
	QTestBankAccount<R> gAccount() {
		return new QTestBankAccount<R>(m_root, this, "gAccount");
	}

	/**
	 * Shortcut eq
	 * @param gAccount
	 * @return
	 */
	public final @Nonnull
	R gAccount(@Nonnull to.etc.test.webapp.query.qfield.TestBankAccount... gAccount) {
		return gAccount().eq(gAccount);
	}

	public final @Nonnull
	QField<R,java.lang.Double> gPercentage() {
		return new QField<R,java.lang.Double>(m_root, this, "gPercentage");
	}

	/**
	 * Shortcut eq
	 * @param gPercentage
	 * @return
	 */
	public final @Nonnull
	R gPercentage(@Nonnull java.lang.Double... gPercentage) {
		return gPercentage().eq(gPercentage);
	}

	public final @Nonnull
	QField<R,java.lang.String> iban() {
		return new QField<R,java.lang.String>(m_root, this, "iban");
	}

	/**
	 * Shortcut eq
	 * @param iban
	 * @return
	 */
	public final @Nonnull
	R iban(@Nonnull java.lang.String... iban) {
		return iban().eq(iban);
	}

	public final @Nonnull
	QField<R,java.lang.Long> id() {
		return new QField<R,java.lang.Long>(m_root, this, "id");
	}

	/**
	 * Shortcut eq
	 * @param id
	 * @return
	 */
	public final @Nonnull
	R id(@Nonnull java.lang.Long... id) {
		return id().eq(id);
	}

	public final @Nonnull
	QTestRelation<R> relation() {
		return new QTestRelation<R>(m_root, this, "relation");
	}

	/**
	 * Shortcut eq
	 * @param relation
	 * @return
	 */
	public final @Nonnull
	R relation(@Nonnull to.etc.test.webapp.query.qfield.TestRelation... relation) {
		return relation().eq(relation);
	}

	public final @Nonnull
	QFieldBoolean<R> blocked() {
		return new QFieldBoolean<R>(new QField<R, boolean[]>(m_root, this, "blocked"));
	}

	/**
	 * Shortcut eq
	 * @param blocked
	 * @return
	 */
	public final @Nonnull
	R blocked(@Nonnull boolean... blocked) {
		return blocked().eq(blocked);
	}

	public final @Nonnull
	QFieldBoolean<R> frkIsGAccount() {
		return new QFieldBoolean<R>(new QField<R, boolean[]>(m_root, this, "frkIsGAccount"));
	}

	/**
	 * Shortcut eq
	 * @param frkIsGAccount
	 * @return
	 */
	public final @Nonnull
	R frkIsGAccount(@Nonnull boolean... frkIsGAccount) {
		return frkIsGAccount().eq(frkIsGAccount);
	}

	public final @Nonnull
	QField<R,java.lang.Long> organisationId() {
		return new QField<R,java.lang.Long>(m_root, this, "organisationId");
	}

	/**
	 * Shortcut eq
	 * @param organisationId
	 * @return
	 */
	public final @Nonnull
	R organisationId(@Nonnull java.lang.Long... organisationId) {
		return organisationId().eq(organisationId);
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
	QTestBankAccountRoot get() {
		return new QTestBankAccountRoot();
	}
}