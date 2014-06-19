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

	@Test
	public void testIsSevereException() {
		Assert.assertTrue("This is not a known exception. It should be severe!", ExceptionClassifier.isSevereException(new Exception()));
		Assert
.assertTrue("This is a known severe exception!", ExceptionClassifier.isSevereException(new SQLException("ORA-12899: value too large for column")));

		Assert.assertFalse("This is a known unsevere exception!", ExceptionClassifier.isSevereException(new SQLException("ORA-02292: integrity constraint")));
		Assert.assertFalse("This is a known unsevere exception!", ExceptionClassifier.isSevereException(new SQLException("ORA-20000: Gegevens zijn gewijzigd door een andere gebruiker")));
		//Assert.assertFalse("This is a known unsevere exception!", ExceptionClassifier.isSevereException(new SQLException("ClientAbortException:  java.net.SocketException: Connection reset")));
		Assert.assertFalse("This is a known unsevere exception!", ExceptionClassifier.isSevereException(new SQLException("ORA-20023: tda_general.check_beperking: Combinatie <B>Elementcode:</B>:")));
		Assert.assertFalse("This is a known unsevere exception!", ExceptionClassifier.isSevereException(new Exception("De PDA is niet toegewezen aan een persoon")));
	}

	@Test
	public void testNestedExceptions() {
		SQLException severeException = new SQLException("ORA-12899: value too large for column");
		SQLException notSevereException = new SQLException("ORA-02292: integrity constraint");
		severeException.setNextException(notSevereException);

		Assert.assertTrue("There is a severe exception", ExceptionClassifier.isSevereException(severeException));
	}

	@Test
	public void testNestedNotSevereExceptions() {
		SQLException notSevereException1 = new SQLException("ORA-02292: integrity constraint");
		SQLException notSevereException2 = new SQLException("ORA-20000: Gegevens zijn gewijzigd door een andere gebruiker");
		notSevereException1.setNextException(notSevereException2);

		Assert.assertFalse("This is a known unsevere exception!", ExceptionClassifier.isSevereException(notSevereException1));
	}
}
