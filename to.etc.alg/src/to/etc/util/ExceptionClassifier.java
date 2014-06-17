package to.etc.util;

import java.sql.*;
import java.util.*;

import javax.annotation.*;

/**
 * This determines whether an exception should be logged or not.
 *
 * There are roughly two classes of exceptions:
 * <ol>
 * <li>Errors from services (SOAP, PDA, etc.)</li>
 * <li>Errors from the user interface</li>
 * </ol>
 * <p>
 * The errors from services should always be logged, because no direct user interaction is available. The classes that handle these errors
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
	private static final Map<String, Boolean> m_severeExceptions = new HashMap<String, Boolean>(); //Boolean.TRUE means that it is severe, Boolean.FALSE means it is not severe.

	static {
		m_severeExceptions.put("ORA-02292: integrity constraint", Boolean.FALSE);
		m_severeExceptions.put("ORA-20000: Gegevens zijn gewijzigd door een andere gebruiker", Boolean.FALSE);

		m_severeExceptions.put("ORA-12899: value too large for column", Boolean.TRUE);
	}

	@Nonnull
	public static boolean isSevereException(@Nonnull Throwable e) {
		List<Throwable> thrownExceptions = new ArrayList<Throwable>();

		createListOfExceptions(e, thrownExceptions);

		boolean foundUnsevereException = false;
		for(Throwable t : thrownExceptions) {
			String message = t.getMessage();
			if(message != null) {
				for(String exceptionMsg : m_severeExceptions.keySet()) {
					if(message.startsWith(exceptionMsg)) {
						if(m_severeExceptions.get(exceptionMsg).booleanValue()) {
							return true;
						} else {
							foundUnsevereException = true;
						}
					}
				}
			}
		}
		return foundUnsevereException ? false : true;
	}

	private static void createListOfExceptions(@Nonnull Throwable e, @Nonnull List<Throwable> thrownExceptions) {
		thrownExceptions.add(e);
		if(e instanceof SQLException) {
			SQLException sqle = ((SQLException) e).getNextException();
			if(sqle != null) {
				createListOfExceptions(sqle, thrownExceptions);
			}
		}

		Throwable cause = e.getCause();
		if(cause != null) {
			createListOfExceptions(cause, thrownExceptions);
		}
	}
}
