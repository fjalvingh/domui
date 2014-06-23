package to.etc.util;

import java.sql.*;
import java.util.*;

import javax.annotation.*;

/**
 * This determines whether an exception is severe or not. The users of this class will most likely use the outcome of this severity to log the exception or not.
 *
 * There are roughly two classes of exceptions:
 * <ol>
 * <li>Errors from services (SOAP, PDA, etc.)</li>
 * <li>Errors from the user interface</li>
 * </ol>
 * <p>
 * The errors from services are always severe, because no direct user interaction is available. The classes that handle these errors
 * will probably not use this helper class.
 * <p><p>
 * The errors from the user interface can also be divided into two types:
 * <ol>
 * <li>Normal errors; e.g. invalid data entry, a package throws an exception because of invalid combinations or configurations.
 * 		These errors will be shown to the user on the page and are not needed to log, because the user will have to take some action to solve these errors.</li>
 * <li>All other errors: field is too long or other errors that the user has no influence upon must be logged.</li>
 * </ol>
 * <p><p>
 * This class determines the type of error and reports back to the caller whether it is severe or not.
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since Jun 10, 2014
 */
public final class ExceptionClassifier implements IExceptionClassifier {

	/**
	 * The severity of the exception
	 *
	 *
	 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
	 * @since Jun 23, 2014
	 */
	public enum Severity {
		/**
		 * This is a serious exception that should be handled in some way, e.g. printing to the log
		 */
		SEVERE,
		/**
		 * This exception is considered not severe and may be ignored
		 */
		UNSEVERE,
		/**
		 * This is an exception that is not classified as a known exception, either SEVERE or UNSEVERE
		 */
		UNKNOWN
	}

	@Nullable
	private static ExceptionClassifier m_instance;

	@Nonnull
	private static final Map<String, Severity> m_knownExceptions = new HashMap<String, Severity>();

	@Nonnull
	private static final List<IExceptionClassifier> m_customExceptions = new ArrayList<IExceptionClassifier>();

	@Nonnull
	public static ExceptionClassifier getInstance() {
		ExceptionClassifier instance = m_instance;
		if(instance == null) {
			instance = m_instance = new ExceptionClassifier();
		}
		return m_instance;
	}

	/**
	 * By registering a known exception you make clear to this class which exception-message should be considered severe or not.
	 *
	 * The message should be as short as possible without any language specific additions if possible. <br>
	 * E.g. registering "ORA-02292" is better than "ORA-02292: integrity constraint" because the text might be translated in another language
	 * @param String message
	 * @param Boolean severe
	 * @throws Exception
	 */
	public void registerKnownException(@Nonnull String message, @Nonnull Severity severity) throws Exception {
		m_knownExceptions.put(message, severity);
	}

	public void registerCustomExceptions(@Nonnull IExceptionClassifier exceptionClassifier) {
		m_customExceptions.add(exceptionClassifier);
	}

	@Override
	@Nonnull
	public Severity getExceptionSeverity(@Nonnull Throwable e) {
		List<Throwable> thrownExceptions = new ArrayList<Throwable>();

		addExceptionsToList(e, thrownExceptions);

		for(Throwable t : thrownExceptions) {
			String message = t.getMessage();
			if(message != null) {
				for(String exceptionMsg : m_knownExceptions.keySet()) {
					if(message.startsWith(exceptionMsg)) {
						return m_knownExceptions.get(exceptionMsg);
					}
				}
			}
			for(IExceptionClassifier exceptionClassifier : m_customExceptions) {
				Severity severity = exceptionClassifier.getExceptionSeverity(t);
				if(severity == Severity.UNKNOWN) {
					continue;
				}
				return severity;
			}
		}
		return Severity.UNKNOWN;
	}

	private void addExceptionsToList(@Nonnull Throwable e, @Nonnull List<Throwable> thrownExceptions) {
		thrownExceptions.add(e);
		if(e instanceof SQLException) {
			SQLException sqle = ((SQLException) e).getNextException();
			if(sqle != null) {
				addExceptionsToList(sqle, thrownExceptions);
			}
		}

		Throwable cause = e.getCause();
		if(cause == e) {
			cause = null;
		}
		if(cause != null) {
			addExceptionsToList(cause, thrownExceptions);
		}
	}
}
