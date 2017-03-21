package to.etc.test.webapp.query.qfield;
import to.etc.webapp.query.*;
import javax.annotation.*;
/**
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 */
@Generated(value = { "This is a generated file. It will be overwritten during compilation. It is therefore useless to make any modifications." })
public class QTestBank<R extends QField<R, ? >> extends QField<R,to.etc.test.webapp.query.qfield.TestBank> {

	QTestBank(@Nullable R root, @Nullable QField<R, ? > parent, @Nullable String name) {
		super(root, parent, name);
	}

	public final @Nonnull
	QField<R,java.lang.String> bankname() {
		return new QField<R,java.lang.String>(m_root, this, "bankname");
	}

	/**
	 * Shortcut eq
	 * @param bankname
	 * @return
	 */
	public final @Nonnull
	R bankname(@Nonnull java.lang.String... bankname) {
		return bankname().eq(bankname);
	}

	public final @Nonnull
	QField<R,java.lang.String> bic() {
		return new QField<R,java.lang.String>(m_root, this, "bic");
	}

	/**
	 * Shortcut eq
	 * @param bic
	 * @return
	 */
	public final @Nonnull
	R bic(@Nonnull java.lang.String... bic) {
		return bic().eq(bic);
	}

	public final @Nonnull
	QField<R,java.lang.String> cityname() {
		return new QField<R,java.lang.String>(m_root, this, "cityname");
	}

	/**
	 * Shortcut eq
	 * @param cityname
	 * @return
	 */
	public final @Nonnull
	R cityname(@Nonnull java.lang.String... cityname) {
		return cityname().eq(cityname);
	}

	public final @Nonnull
	QField<R,java.lang.String> filename() {
		return new QField<R,java.lang.String>(m_root, this, "filename");
	}

	/**
	 * Shortcut eq
	 * @param filename
	 * @return
	 */
	public final @Nonnull
	R filename(@Nonnull java.lang.String... filename) {
		return filename().eq(filename);
	}

	public final @Nonnull
	QField<R,java.lang.String> filenameSepa() {
		return new QField<R,java.lang.String>(m_root, this, "filenameSepa");
	}

	/**
	 * Shortcut eq
	 * @param filenameSepa
	 * @return
	 */
	public final @Nonnull
	R filenameSepa(@Nonnull java.lang.String... filenameSepa) {
		return filenameSepa().eq(filenameSepa);
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
	QField<R,java.lang.String> identification() {
		return new QField<R,java.lang.String>(m_root, this, "identification");
	}

	/**
	 * Shortcut eq
	 * @param identification
	 * @return
	 */
	public final @Nonnull
	R identification(@Nonnull java.lang.String... identification) {
		return identification().eq(identification);
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
	QTestBankRoot get() {
		return new QTestBankRoot();
	}
}