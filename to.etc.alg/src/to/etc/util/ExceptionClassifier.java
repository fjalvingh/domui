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

	@Nonnull
	private static final Map<String, Boolean> m_knownExceptions = new HashMap<String, Boolean>(); //Boolean.TRUE means that it is severe, Boolean.FALSE means it is not severe.

	static {
		m_knownExceptions.put("ORA-02292", Boolean.FALSE);											// Violation of integrity constraint(s), shown on screen, not severe.
		m_knownExceptions.put("ORA-20000: Gegevens zijn gewijzigd door een andere gebruiker", Boolean.FALSE);				// Concurrency exception, shown on the screen, not severe.
		// TODO: check and test this error message on 5.0
		//m_severeExceptions.put("ClientAbortException:  java.net.SocketException: Connection reset", Boolean.FALSE);			// Exception when planboard is closed before it's fully loaded. Not severe.
		m_knownExceptions.put("ORA-20023: tda_general.check_beperking: Combinatie <B>Elementcode:</B>:", Boolean.FALSE);	// Misconfiguration of elementcode/werksoort/fonds combination, shown on screen, not severe.
		m_knownExceptions.put("De PDA is niet toegewezen aan een persoon", Boolean.FALSE);									// Thrown when PDA is reconnected to another environment, not severe.

		m_knownExceptions.put("ORA-12899", Boolean.TRUE);
	}

	@Nonnull
	public static boolean isSevereException(@Nonnull Throwable e) {
		List<Throwable> thrownExceptions = new ArrayList<Throwable>();

		addAcceptionsToList(e, thrownExceptions);

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

	private static void addAcceptionsToList(@Nonnull Throwable e, @Nonnull List<Throwable> thrownExceptions) {
		thrownExceptions.add(e);
		if(e instanceof SQLException) {
			SQLException sqle = ((SQLException) e).getNextException();
			if(sqle != null) {
				addAcceptionsToList(sqle, thrownExceptions);
			}
		}

		Throwable cause = e.getCause();
		if(cause != null) {
			addAcceptionsToList(cause, thrownExceptions);
		}
	}
}
