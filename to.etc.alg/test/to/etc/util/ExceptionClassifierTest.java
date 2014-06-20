package to.etc.util;

import java.sql.*;

import org.junit.*;

/**
 * Tests whether the ExceptionClassifier does its work correctly
 *
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since Jun 17, 2014
 */
public class ExceptionClassifierTest {

	@BeforeClass
	public static void setUp() throws Exception {
		ExceptionClassifier.getInstance().registerKnownException("ORA-02292", Boolean.FALSE);
		ExceptionClassifier.getInstance().registerKnownException("ORA-20000: Gegevens zijn gewijzigd door een andere gebruiker", Boolean.FALSE);				// Concurrency exception, shown on the screen, not severe.
		ExceptionClassifier.getInstance().registerKnownException("ORA-20023: tda_general.check_beperking: Combinatie <B>Elementcode:</B>:", Boolean.FALSE);	// Misconfiguration of elementcode/werksoort/fonds combination, shown on screen, not severe.
		ExceptionClassifier.getInstance().registerKnownException("De PDA is niet toegewezen aan een persoon", Boolean.FALSE);									// Thrown when PDA is reconnected to another environment, not severe.
		ExceptionClassifier.getInstance().registerKnownException("ORA-12899", Boolean.TRUE);
	}

	@Test
	public void testIsSevereException() {
		Assert.assertTrue("This is not a known exception. It should be severe!", ExceptionClassifier.getInstance().isSevereException(new Exception()));
		Assert
.assertTrue("This is a known severe exception!", ExceptionClassifier.getInstance().isSevereException(new SQLException("ORA-12899: value too large for column")));

		Assert.assertFalse("This is a known unsevere exception!", ExceptionClassifier.getInstance().isSevereException(new SQLException("ORA-02292: integrity constraint")));
		Assert
			.assertFalse("This is a known unsevere exception!", ExceptionClassifier.getInstance().isSevereException(new SQLException("ORA-20000: Gegevens zijn gewijzigd door een andere gebruiker")));
		//Assert.assertFalse("This is a known unsevere exception!", ExceptionClassifier.isSevereException(new SQLException("ClientAbortException:  java.net.SocketException: Connection reset")));
		Assert.assertFalse("This is a known unsevere exception!",
			ExceptionClassifier.getInstance().isSevereException(new SQLException("ORA-20023: tda_general.check_beperking: Combinatie <B>Elementcode:</B>:")));
		Assert.assertFalse("This is a known unsevere exception!", ExceptionClassifier.getInstance().isSevereException(new Exception("De PDA is niet toegewezen aan een persoon")));
	}

	@Test
	public void testNestedExceptions() {
		SQLException severeException = new SQLException("ORA-12899: value too large for column");
		SQLException notSevereException = new SQLException("ORA-02292: integrity constraint");
		severeException.setNextException(notSevereException);

		Assert.assertTrue("There is a severe exception", ExceptionClassifier.getInstance().isSevereException(severeException));
	}

	@Test
	public void testNestedNotSevereExceptions() {
		SQLException notSevereException1 = new SQLException("ORA-02292: integrity constraint");
		SQLException notSevereException2 = new SQLException("ORA-20000: Gegevens zijn gewijzigd door een andere gebruiker");
		notSevereException1.setNextException(notSevereException2);

		Assert.assertFalse("This is a known unsevere exception!", ExceptionClassifier.getInstance().isSevereException(notSevereException1));
	}
}
