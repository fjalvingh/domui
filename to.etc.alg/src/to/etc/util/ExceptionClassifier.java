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
public final class ExceptionClassifier {

	@Nullable
	private static ExceptionClassifier m_instance;

	@Nonnull
	public static ExceptionClassifier getInstance() {
		ExceptionClassifier instance = m_instance;
		if(instance == null) {
			instance = m_instance = new ExceptionClassifier();
		}
		return m_instance;
	}

	@Nonnull
	private static final Map<String, Boolean> m_knownExceptions = new HashMap<String, Boolean>(); //Boolean.TRUE means that it is severe, Boolean.FALSE means it is not severe.

	/**
	 * By registering a known exception you make clear to this class which exception-message should be considered severe or not.
	 *
	 * The message should be as short as possible without any language specific additions if possible. <br>
	 * E.g. registering "ORA-02292" is better than "ORA-02292: integrity constraint" because the text might be translated in another language
	 * @param String message
	 * @param Boolean severe
	 * @throws Exception
	 */
	public void registerKnownException(@Nonnull String message, @Nonnull Boolean severe) throws Exception {
		m_knownExceptions.put(message, severe);
	}

	@Nonnull
	public boolean isSevereException(@Nonnull Throwable e) {
		List<Throwable> thrownExceptions = new ArrayList<Throwable>();

		addExceptionsToList(e, thrownExceptions);

		boolean foundUnsevereException = false;
		for(Throwable t : thrownExceptions) {
			String message = t.getMessage();
			if(message == null) {
				continue;
			}
			for(String exceptionMsg : m_knownExceptions.keySet()) {
				if(message.startsWith(exceptionMsg)) {
					if(m_knownExceptions.get(exceptionMsg).booleanValue()) {
						return true;
					}
					foundUnsevereException = true;
				}
			}
		}
		return foundUnsevereException ? false : true;
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
		if(cause != null) {
			addExceptionsToList(cause, thrownExceptions);
		}
	}
}
