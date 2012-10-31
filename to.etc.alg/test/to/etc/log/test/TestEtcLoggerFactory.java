package to.etc.log.test;

import java.io.*;

import javax.annotation.*;
import javax.xml.parsers.*;

import org.junit.*;
import org.slf4j.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import to.etc.log.*;

public class TestEtcLoggerFactory {
	static private EtcLoggerFactory	m_testLoggerFactory;

	static private String			m_testHome	= System.getProperty("user.home") + File.separatorChar + "testlog";

	@BeforeClass
	public static void setup() throws Exception {
		m_testLoggerFactory = new EtcLoggerFactory();
		m_testLoggerFactory.initialize(new File(m_testHome));
	}

	private static String getConfig(@Nonnull Level level) {
		StringBuilder sb = new StringBuilder();
		sb.append("<config logLocation=\"$user.home$/testlog/logs\">");
		sb.append("<handler type=\"stdout\">");
		sb.append("<log level=\"" + level.name() + "\" name=\"to.etc.log.test\"/>");
		sb.append("</handler>");
		sb.append("</config>");
		return sb.toString();
	}

	@Test
	public void testLogger() throws Exception {
		initTestLoggerFactory(Level.INFO);

		PrintStream stdout = System.out;
		boolean stdoutReturned = false;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//redirect console output for test results inspection
			System.setOut(new PrintStream(baos));

			Logger log = m_testLoggerFactory.getLogger(this.getClass().getName());
			log.trace("trace");
			log.debug("debug");
			log.info("info");
			log.warn("warn");
			log.error("error");

			System.setOut(stdout);
			stdoutReturned = true;
			String outRes = new String(baos.toByteArray(), "utf-8");
			System.out.print(outRes);

			Assert.assertTrue(outRes.contains("info"));
			Assert.assertTrue(outRes.contains("warn"));
			Assert.assertTrue(outRes.contains("error"));
			Assert.assertFalse(outRes.contains("trace"));
			Assert.assertFalse(outRes.contains("debug"));
		} finally {
			if(!stdoutReturned) {
				//be sure that standard console output is set back
				System.setOut(stdout);
			}
		}
	}

	private void initTestLoggerFactory(Level level) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(getConfig(level))));
		m_testLoggerFactory.loadConfig(doc);
	}

}
