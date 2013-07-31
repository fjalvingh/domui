package to.etc.launcher.runner;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.resource.spi.IllegalStateException;

import to.etc.util.*;

/**
 * Runs TestNG with provided run options.
 * Multithreaded, consumes provided suite + browserString pairs, until all are run.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Jul 31, 2013
 */
public class SuiteRunner implements Runnable {

	private @Nonnull
	final IRunnablePairProvider	m_dataProvider;

	private @Nonnull
	final IRunnableArgumentsProvider	m_argumentsProvider;

	private @Nonnull
	final String						m_name;

	/**
	 * Internal use -> to redirect process stdout.
	 *
	 *
	 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
	 * Created on Jul 31, 2013
	 */
	private static class StreamGrabber extends Thread {
		private final @Nonnull
		InputStream	m_is;

		private final @Nonnull
		String		m_name;

		StreamGrabber(@Nonnull InputStream is, @Nonnull String name) {
			m_is = is;
			m_name = name;
		}

		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(m_is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while((line = br.readLine()) != null) {
					System.out.println(m_name + "> " + line);
				}
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * Createds runner with specified parameter providers.
	 *
	 * @param argumentsProvider
	 * @param dataProvider
	 * @param name
	 */
	public SuiteRunner(@Nonnull IRunnableArgumentsProvider argumentsProvider, @Nonnull IRunnablePairProvider dataProvider, @Nonnull String name) {
		m_argumentsProvider = argumentsProvider;
		m_dataProvider = dataProvider;
		m_name = name;
	}

	@Override
	public void run() {
		RunnablePair data = null;
		do {
			data = m_dataProvider.getNext();
			if(null != data) {
				try {
					runTestNg(data.getSuite(), data.getBrowserString());
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		} while(null != data);
	}

	private int runTestNg(@Nonnull File suite, @Nonnull String browserString) throws IOException, InterruptedException, IllegalStateException {
/*		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
*/
		List<String> args = assableArguments(suite, browserString);
		ProcessBuilder builder = new ProcessBuilder(args);
		Process process = builder.start();
		StreamGrabber errorGobbler = new StreamGrabber(process.getErrorStream(), "ERROR_" + m_name);
		StreamGrabber outputGobbler = new StreamGrabber(process.getInputStream(), "OUTPUT_" + m_name);

		outputGobbler.start();
		errorGobbler.start();
		process.waitFor();
		return process.exitValue();
	}

	private List<String> assableArguments(@Nonnull File suite, @Nonnull String browserString) throws IllegalStateException {
		List<String> args = new ArrayList<String>();
		add(args, "java");
		addAsVMOptionalArgument(args, "testng.browser.string", browserString);
		addAsVMOptionalArgument(args, "testng.remote.hub", m_argumentsProvider.getRemoteHub());
		addAsVMOptionalArgument(args, "testng.server.url", m_argumentsProvider.getServerUrl());
		addAsVMOptionalArgument(args, "testng.username", m_argumentsProvider.getUserName());
		addAsVMOptionalArgument(args, "testng.password", m_argumentsProvider.getPassword());
		addAsVMOptionalArgument(args, "testProperties", m_argumentsProvider.getUnitTestProperties());
		addAsVMOptionalArgument(args, "testng.report.root", new File(m_argumentsProvider.getReportRoot(), browserString).getAbsolutePath());
		add(args, "-cp").add(args, m_argumentsProvider.getClassPath());
		add(args, "org.testng.TestNG");
		add(args, "-usedefaultlisteners").add(args, "false");
		add(args, suite.getAbsolutePath());
		return args;
	}

	private SuiteRunner add(@Nonnull List<String> args, @Nonnull String param) {
		args.add(param);
		return this;
	}

	private SuiteRunner addAsVMOptionalArgument(@Nonnull List<String> args, @Nonnull String key, @Nonnull String value) {
		if(!StringTool.isBlank(value)) {
			args.add("-D" + key + "=" + value);
		}
		return this;
	}
}
