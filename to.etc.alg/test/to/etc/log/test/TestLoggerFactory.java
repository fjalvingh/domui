package to.etc.log.test;

import java.io.*;

import org.junit.*;
import org.slf4j.*;

import to.etc.log.*;
import to.etc.log.MyLogger.Level;

public class TestLoggerFactory {
	private static MyLoggerFactory	m_instance;

	@BeforeClass
	public static void setup() throws Exception {
		String home = System.getProperty("user.home") + File.separatorChar + "testlog";
		MyLoggerFactory.loadConfig(new File(home));
		m_instance = new MyLoggerFactory();
	}

	public void initLoggerFactory() throws Exception {
		MyLoggerFactory.setOut("to.etc.log.test", "logger1");
		MyLoggerFactory.setLevel("to.etc.log.test", Level.DEBUG);
		MyLoggerFactory.addMarker("to.etc.log.test", MarkerFactory.getMarker("Proba"), Level.INFO);
		MyLoggerFactory.addMarker("to.etc.log.test", MarkerFactory.getMarker("Proba2"), Level.WARN);
		MyLoggerFactory.setDisabled("to.etc.log.test", true);
		MyLoggerFactory.save();
	}

	@Test
	public void testLogger() throws Exception {
		initLoggerFactory();
		Logger log = LoggerFactory.getLogger("to.etc.log.test");
		log.error("error1");
		log.info(MarkerFactory.getMarker("Proba"), "info1");
		log.warn(MarkerFactory.getMarker("Proba2"), "warn1");
		log.info(MarkerFactory.getMarker("Proba2"), "info2");
		log.error(MarkerFactory.getMarker("Proba"), "error2");
		m_instance.setDisabled("to.etc.log.test", false);
		log.error("error3");
		Logger log2 = LoggerFactory.getLogger("to.etc.log.test2");
		log2.error("log2 error");
	}

}
