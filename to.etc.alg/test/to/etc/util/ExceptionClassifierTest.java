package to.etc.util;

import java.sql.*;

import javax.annotation.*;

import org.junit.*;

import to.etc.util.ExceptionClassifier.Severity;


/**
 * Tests whether the ExceptionClassifier does its work correctly
 *
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since Jun 17, 2014
 */
public class ExceptionClassifierTest {

	public class CustomException extends Throwable {
		String m_code;

		public CustomException(@Nonnull String code) {
			m_code = code;
		}

		public String getCode() {
			return m_code;
		}
	}

	@BeforeClass
	public static void setUp() throws Exception {
		ExceptionClassifier.getInstance().registerKnownException("ORA-02292", Severity.UNSEVERE);
		ExceptionClassifier.getInstance().registerKnownException("ORA-20000: Gegevens zijn gewijzigd door een andere gebruiker", Severity.UNSEVERE);				// Concurrency exception, shown on the screen, not severe.
		ExceptionClassifier.getInstance().registerKnownException("ORA-20023: tda_general.check_beperking: Combinatie <B>Elementcode:</B>:", Severity.UNSEVERE);	// Misconfiguration of elementcode/werksoort/fonds combination, shown on screen, not severe.
		ExceptionClassifier.getInstance().registerKnownException("ORA-12899", Severity.SEVERE);

		ExceptionClassifier.getInstance().registerClassifier(new IExceptionClassifier() {

			@Override
			public Severity getExceptionSeverity(Throwable throwable) {
				if(throwable instanceof CustomException) {
					CustomException codeException = (CustomException) throwable;
					if("pda.not.assigned".equalsIgnoreCase(codeException.getCode())) {
						return Severity.UNSEVERE;
					}
					return Severity.UNKNOWN;
				}
				return Severity.UNKNOWN;
			}
		});
	}

	@Test
	public void testIsSevereException() {
		Assert.assertEquals("This is not a known exception. It should be severe!", Severity.UNKNOWN, ExceptionClassifier.getInstance().getExceptionSeverity(new Exception()));
		Assert
.assertEquals("This is a known severe exception!", Severity.SEVERE, ExceptionClassifier.getInstance().getExceptionSeverity(new SQLException("ORA-12899: value too large for column")));

		Assert.assertEquals("This is a known unsevere exception!", Severity.UNSEVERE, ExceptionClassifier.getInstance().getExceptionSeverity(new SQLException("ORA-02292: integrity constraint")));
		Assert
.assertEquals("This is a known unsevere exception!", Severity.UNSEVERE,
			ExceptionClassifier.getInstance().getExceptionSeverity(new SQLException("ORA-20000: Gegevens zijn gewijzigd door een andere gebruiker")));
		//Assert.assertFalse("This is a known unsevere exception!", ExceptionClassifier.isSevereException(new SQLException("ClientAbortException:  java.net.SocketException: Connection reset")));
		Assert.assertEquals("This is a known unsevere exception!", Severity.UNSEVERE,
			ExceptionClassifier.getInstance().getExceptionSeverity(new SQLException("ORA-20023: tda_general.check_beperking: Combinatie <B>Elementcode:</B>:")));
		Assert
.assertEquals("This is a known unsevere exception!", Severity.UNSEVERE,
 ExceptionClassifier.getInstance().getExceptionSeverity(new CustomException("pda.not.assigned")));
		Assert.assertEquals("This is not a known exception!", Severity.UNKNOWN, ExceptionClassifier.getInstance().getExceptionSeverity(new CustomException("unknown.excpetion")));
	}

	@Test
	public void testNestedUnsevereExceptions() {
		SQLException severeException = new SQLException("ORA-12899: value too large for column");
		SQLException notSevereException = new SQLException("ORA-02292: integrity constraint");
		severeException.setNextException(notSevereException);

		Assert.assertEquals("There is a severe exception", Severity.SEVERE, ExceptionClassifier.getInstance().getExceptionSeverity(severeException));
	}

	@Test
	public void testNestedNotSevereExceptions() {
		SQLException notSevereException1 = new SQLException("ORA-02292: integrity constraint");
		SQLException notSevereException2 = new SQLException("ORA-20000: Gegevens zijn gewijzigd door een andere gebruiker");
		notSevereException1.setNextException(notSevereException2);

		Assert.assertEquals("This is a known unsevere exception!", Severity.UNSEVERE, ExceptionClassifier.getInstance().getExceptionSeverity(notSevereException1));
	}
}
