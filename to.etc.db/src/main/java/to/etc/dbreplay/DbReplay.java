package to.etc.dbreplay;

import to.etc.dbpool.ConnectionPool;
import to.etc.dbpool.DbPoolUtil;
import to.etc.dbpool.PoolManager;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This utility will replay a database logfile, for performance evaluation purposes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 31, 2011
 */
public class DbReplay {
	private File m_inputFile;

	private File m_driverPath;

	/** The buffered input file containing statements. */
	private BufferedInputStream m_bis;

	private long m_firstTime;

	private long m_lastRecordTime;

	private long m_startTime;

	private long m_endTime;

	private File m_poolFile;

	private String m_poolId;

	private ConnectionPool m_pool;

	private String m_runSchema;

	private String m_dbHost, m_dbSid, m_dbUser, m_dbPass;

	/** The #of separate executor threads to start. */
	private int m_executors = 20;

	/** The #of executors that are actually running/ready. */
	private int m_runningExecutors;

	/** When set by -maxwait, this limits the max time to wait between statements, ignoring the time delta's in the log file. */
	private long m_maxStatementDelay = Long.MAX_VALUE;

	private XType m_runType;

	private PrintWriter m_log;

	private boolean m_stopped;

	private IReplayer m_replayer;

	private enum XType {
		DUMP, RUN
	}

	private void run(String[] args) throws Exception {
		if(!decodeOptions(args))
			return;

		try {
			openSource();
			switch(m_runType){
				default:
					throw new IllegalStateException(m_runType + ": unknown");

				case DUMP:
					runDump();
					break;
				case RUN:
					runEmulation();
					break;
			}
		} catch(Exception x) {
			System.err.println("Error: " + x);
			System.err.println("   -at record " + m_recordNumber + ", file offset " + m_fileOffset);
			x.printStackTrace();
		} finally {
			releaseAll();
		}
	}

	private void openLog() throws Exception {
		File log = new File("dbreplay.log");
		m_log = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(log), 65536), "utf-8"));
		m_log.println("Log file start @" + new Date());
	}

	public void log(String s) {
		if(m_log == null)
			return;
		m_log.println(s);
	}

	public boolean isLogging() {
		return m_log != null;
	}

	public long getMaxStatementDelay() {
		return m_maxStatementDelay;
	}

	private void runDump() throws Exception {
		// TODO Auto-generated method stub

	}


	private void runEmulation() throws Exception {
		initialize();

		//-- Input distributor loop.
		m_startTime = System.currentTimeMillis();
		for(; ; ) {
			ReplayRecord rr = ReplayRecord.readRecord(this);
			if(null == rr)
				break;
			if(m_recordNumber == 0) {
				m_firstTime = rr.getStatementTime();
			}
			m_lastRecordTime = rr.getStatementTime();
			m_recordNumber++;

			handleRecord(rr);
		}
		waitForIdle(60 * 1000);
		synchronized(this) {
			m_stopped = true;
		}

		m_endTime = System.currentTimeMillis();
		System.out.println("Normal EOF after " + m_recordNumber + " records and " + m_fileOffset + " file bytes");
		Date st = new Date(m_firstTime);
		Date et = new Date(m_lastRecordTime);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("  - input time from " + df.format(st) + " till " + df.format(et) + ", " + DbPoolUtil.strMillis(m_lastRecordTime - m_firstTime));
		System.out.println("  - real time spent: " + DbPoolUtil.strMillis(m_endTime - m_startTime));
	}


	private boolean decodeOptions(String[] args) throws Exception {
		int argc = 0;
		while(argc < args.length) {
			String s = args[argc++];
			if(s.startsWith("-")) {
				if("-pf".equals(s) || "-poolfile".equals(s)) {
					if(argc >= args.length)
						throw new IllegalArgumentException("Missing file name after -poolfile");
					m_poolFile = new File(args[argc++]);
					if(!m_poolFile.exists() || !m_poolFile.isFile())
						throw new IllegalArgumentException(m_poolFile + ": file not found");
				} else if("-dump".equals(s)) {
					m_runType = XType.DUMP;
				} else if("-schema".equals(s)) {
					if(argc >= args.length)
						throw new IllegalArgumentException("Missing name after -schema");
					m_runSchema = args[argc++];
				} else if("-db".equals(s)) {
					if(argc >= args.length)
						throw new IllegalArgumentException("Missing db string after -db");
					decodeDb(args[argc++]);
				} else if("-dp".equals(s) || "-driver".equals(s)) {
					if(argc >= args.length)
						throw new IllegalArgumentException("Missing driver path after " + s);
					m_driverPath = new File(args[argc++]);
					if(!m_driverPath.exists() || !m_driverPath.isFile())
						throw new IllegalArgumentException(m_driverPath + ": invalid path (not a file or does not exist)");
				} else if("-maxwait".equals(s)) {
					if(argc >= args.length)
						throw new IllegalArgumentException("Missing numeric value (milliseconds) after -maxwait");
					m_maxStatementDelay = Long.parseLong(args[argc++]);
				} else if("-log".equals(s)) {
					openLog();
				} else if("-speedy".equals(s)) {
					m_replayer = new SpeedyReplayer();
				} else if(m_replayer != null) {
					argc = m_replayer.decodeArgs(s, args, argc);
					if(-1 == argc) {
						usage("Unknown option: " + s);
						return false;
					}
				} else {
					usage("Unknown option: " + s);
					return false;
				}
			} else {
				if(m_inputFile == null) {
					m_inputFile = new File(s);
					if(!m_inputFile.exists() || !m_inputFile.isFile())
						throw new Exception(m_inputFile + ": file does not exist or is not a file.");
				} else if(m_poolId == null) {
					m_poolId = s;
				} else {
					usage("Unexpected extra argument on command line");
					return false;
				}
			}
		}

		if(m_inputFile == null) {
			usage("Missing input file name");
			return false;
		}
		if(m_poolId == null && m_dbHost == null) {
			usage("Missing pool ID or database (-db) specification");
			return false;
		}
		if(m_runType == null)
			m_runType = XType.RUN;
		if(m_replayer == null)
			m_replayer = new TimeBasedReplayer();
		return true;
	}

	private void decodeDb(String s) throws Exception {
		int pos = s.indexOf('@');
		if(pos != -1) {
			String a = s.substring(0, pos);
			String b = s.substring(pos + 1);

			pos = a.indexOf(':');
			if(pos != -1) {
				m_dbUser = a.substring(0, pos);
				m_dbPass = a.substring(pos + 1);

				pos = b.indexOf('/');
				if(pos != -1) {
					m_dbHost = b.substring(0, pos);
					m_dbSid = b.substring(pos + 1);
					return;
				}
			}
		}
		throw new RuntimeException("Bad DB string: format is user:password@host/sid");
	}

	private void usage(String msg) {
		System.out.println("Error: " + msg);
		System.out.println("Usage: DbReplay [options] filename poolID");
		System.out.println("Options are:\n" //
			+ "-poolfile|-pf [filename]: The name of the pool.properties defining the database connection.\n" //
			+ "-schema [name]: set the 'current schema' before starting the tests (useful to run test logged in as a different user). For instance when running as a user 'TEST' when tables in schema VIEWPOINT are needed\n"
			//
			+ "-db [userid:password@host/sid]: shorthand to connect to this specific database.\n" //
			+ "-driver|-dp [path]: path to the Oracle driver .jar file, if not present on the classpath\n" //
			+ "\n** replay options **\n" //
			+ "-maxwait [milliseconds]: set the max time to wait between successive statements to a #of milliseconds. This ignores the real times that statements were sent to the database.\n"
			+ "-log: create a log of statements in dbreplay.log\n" //
			+ "-speedy: run using the 'speedy' replayer\n" //
			+ "\nSpeedy executor options:\n"
			+ "-perwait n: schedule this many SQL commands per 'maxwait' period. Example: -maxwait 1 -perwait 10 will try to execute 10 SQL statements every millisecond\n"
		);
	}

	private void releaseAll() {
		try {
			if(m_log != null)
				m_log.close();
		} catch(Exception x) {
			System.err.println("Cannot close log: " + x);
		} finally {
			m_log = null;
		}

		try {
			if(m_bis != null)
				m_bis.close();
			m_bis = null;
		} catch(Exception x) {
			System.err.println("term: cannot close input file: " + x);
		}

		try {
			terminateAll();
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	synchronized public ConnectionPool getPool() {
		return m_pool;
	}

	synchronized public String getRunSchema() {
		return m_runSchema;
	}

	private void openSource() throws Exception {
		m_bis = new BufferedInputStream(new FileInputStream(m_inputFile), 65536);
	}

	private void initialize() throws Exception {
		if(m_dbHost != null) {
			//-- Use command line invocation
			String url = "jdbc:oracle:thin:@" + m_dbHost + ":1521:" + m_dbSid.toUpperCase();
			m_pool = PoolManager.getInstance().definePool("db", "oracle.jdbc.driver.OracleDriver", url, m_dbUser, m_dbPass, m_driverPath == null ? null : m_driverPath.toString());
		} else {
			if(m_poolFile == null)
				m_pool = PoolManager.getInstance().definePool(m_poolId);
			else
				m_pool = PoolManager.getInstance().definePool(m_poolFile, m_poolId);
		}

		startExecutors();
		startStatusReporter();
		waitForReady();
	}

	private synchronized boolean isStopped() {
		return m_stopped;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Accessing the data stream.							*/
	/*--------------------------------------------------------------*/

	private long m_fileOffset;

	private int m_recordNumber;

	/**
	 *
	 * @param is
	 * @return
	 * @throws Exception
	 */
	@Nullable
	public String readString() throws Exception {
		int len = readInt();
		if(len < 0)
			return null;
		byte[] data = new byte[len];
		int szrd = m_bis.read(data);
		if(szrd != len)
			throw new IOException("Unexpected EOF: got " + szrd + " bytes but needed " + len);
		m_fileOffset += len;
		return new String(data, "utf-8");
	}


	public long readLong() throws Exception {
		long a = (readInt() & 0xffffffffl);
		long b = (readInt() & 0xffffffffl);
		return (a << 32) | b;
	}

	public int readInt() throws Exception {
		int v = m_bis.read();
		if(v == -1)
			throw new EofException();
		int r = v << 24;

		v = m_bis.read();
		if(v == -1)
			throw new EofException();
		r |= v << 16;

		v = m_bis.read();
		if(v == -1)
			throw new EofException();
		r |= v << 8;

		v = m_bis.read();
		if(v == -1)
			throw new EofException();
		r |= v;

		m_fileOffset += 4;
		return r;
	}

	public int readByte() throws Exception {
		int b = m_bis.read();
		if(-1 == b)
			throw new EofException();
		return b;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Execution framework.								*/
	/*--------------------------------------------------------------*/

	/** List of all registered executors. */
	private List<ReplayExecutor> m_executorList = new ArrayList<ReplayExecutor>(100);

	/** All executors that are really doing nothing at all */
	private List<ReplayExecutor> m_freeExecutors = new ArrayList<ReplayExecutor>();

	/** All executors that have sufficient space in their executor queue to execute statements. */
	private Set<ReplayExecutor> m_idleExecutorList = new HashSet<ReplayExecutor>();

	private void startExecutors() {
		System.out.println("init: starting " + m_executors + " executor threads");
		for(int i = 0; i < m_executors; i++) {
			ReplayExecutor rx = new ReplayExecutor(this, i, m_idleExecutorList);
			synchronized(this) {
				m_executorList.add(rx);
				m_freeExecutors.add(rx);
			}
			rx.setDaemon(true);
			rx.setName("x#" + i);
			rx.start();
		}
	}

	synchronized private List<ReplayExecutor> getExecutorList() {
		return new ArrayList<ReplayExecutor>(m_executorList);
	}

	public void executorReady(ReplayExecutor replayExecutor) {
		synchronized(this) {
			m_runningExecutors++;
			notifyAll();
		}
	}

	public void executorStopped(ReplayExecutor rx) {
		synchronized(this) {
			m_runningExecutors--;
			notifyAll();
		}
	}

	/**
	 * Get a free executor from the executor free set, and return null if none available.
	 * @return
	 */
	public synchronized ReplayExecutor allocateExecutor() {
		if(m_freeExecutors.size() == 0) {
			//-- Nothing free... Add to ignore set, and increment error count
			m_missingConnections++;
			return null;
		}

		//-- Assign executor
		return m_freeExecutors.remove(0);
	}

	public synchronized void releaseExecutor(ReplayExecutor rx) {
		if(null != rx) {
			m_freeExecutors.add(rx);
			notify();
		}
	}

	/**
	 * Remove executor from the IDLE list.
	 * @param replayExecutor
	 */
	void removeIdle(ReplayExecutor replayExecutor) {
		synchronized(m_idleExecutorList) {
			m_idleExecutorList.remove(replayExecutor);
		}
	}

	void addIdle(ReplayExecutor rx) {
		synchronized(m_idleExecutorList) {
			if(!m_idleExecutorList.add(rx))
				throw new IllegalStateException("Executor already in idle set");
			m_idleExecutorList.notify();
		}
	}

	public void queueIdle(ReplayRecord rr) throws Exception {
		ReplayExecutor r = null;
		int tries = 20;
		for(; ; ) {
			synchronized(m_idleExecutorList) {
				if(m_idleExecutorList.size() > 0) {
					r = m_idleExecutorList.iterator().next();
					break;
				}
				tries--;
				if(tries <= 0)
					throw new IllegalStateException("No idle executors in 10 tries.");
				m_idleExecutorList.wait(5000);
			}
		}
		r.queue(rr);
	}

	Object getIdleLock() {
		return m_idleExecutorList;
	}

	/**
	 * Force all executors into termination asap.
	 */
	public void terminateAll() throws Exception {
		//-- Force all executors to terminate.
		synchronized(this) {
			if(m_executorList.size() == 0)
				return;

			m_freeExecutors.clear();
		}

		for(ReplayExecutor rx : getExecutorList()) {
			rx.terminate();
		}

		//-- Wait for all of them, max 30 secs.
		long ets = System.currentTimeMillis() + 30 * 1000; // Allow max. 30 seconds startup time.
		for(; ; ) {
			long ts = System.currentTimeMillis();
			if(ts >= ets) {
				//-- Failed to start!!! Abort.
				synchronized(this) {
					System.out.println("term: Timeout waiting for executors to terminate - " + m_runningExecutors + " of " + m_executors + " keep running");
				}
				return;
			}

			synchronized(this) {
				if(m_runningExecutors <= 0)
					break;
				System.out.println("term: waiting for " + m_runningExecutors + " of " + m_executors + " executors to terminate.");
				wait(5000);
			}
		}
		m_executorList.clear();
		System.out.println("term: all executors stopped");
	}

	/**
	 * Wait for all executors to become idle.
	 * @throws Exception
	 */
	public void waitForIdle(long timeout) throws Exception {
		System.out.println("exec: waiting for all executors to idle");
		long ets = System.currentTimeMillis() + timeout;
		long lmt = 0;
		int lastrunning = -1;
		for(; ; ) {
			long ts = System.currentTimeMillis();

			if(ts >= ets) {
				//-- Failed to start!!! Abort.
				throw new RuntimeException("Timeout: " + lastrunning + " executors do not become idle...");
			}

			lastrunning = 0;
			for(ReplayExecutor rx : getExecutorList()) {
				if(!rx.isIdle())
					lastrunning++;
			}
			if(lastrunning <= 0)
				break;

			synchronized(this) {
				wait(1000);
			}
		}
		System.out.println("exec: all executors have become idle.");
	}


	private void waitForReady() throws Exception {
		long ets = System.currentTimeMillis() + 30 * 1000; // Allow max. 30 seconds startup time.
		long lmt = 0;
		for(; ; ) {
			long ts = System.currentTimeMillis();
			if(ts >= ets) {
				//-- Failed to start!!! Abort.
				throw new RuntimeException("Timeout waiting for executors to start - aborting");
			}

			synchronized(this) {
				int ntodo = m_executors - m_runningExecutors;
				if(ntodo <= 0)
					break;
				if(ts >= lmt) {
					System.out.println("init: waiting for " + ntodo + " of " + m_executors + " executors to become ready.");
					lmt = ts + 5 * 1000;
				}
				wait(5000);
			}
		}
		System.out.println("init: ready for execution");
	}


	private long m_ignoredStatements;

	private long m_executedQueries;

	private long m_resultRows;

	private long m_execErrors;

	/** Parallel #of commands in execution. */
	private int m_inExecution;

	/** #of statements skipped due to missing connection. */
	private long m_connSkips;

	private long m_missingConnections;

	public synchronized void incIgnored() {
		m_ignoredStatements++;
	}

	public synchronized void startExecution() {
		m_inExecution++;
	}

	public synchronized void endExecution(int q, int u, int error, int rows) {
		m_inExecution--;
		m_executedQueries += q;
		m_execErrors += error;
		m_resultRows += rows;
	}

	public synchronized int getInExecution() {
		return m_inExecution;
	}

	public synchronized void incConnSkips() {
		m_connSkips++;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	assign records to executors.						*/
	/*--------------------------------------------------------------*/

	/**
	 *
	 * @param rr
	 */
	private void handleRecord(ReplayRecord rr) throws Exception {
		m_replayer.handleRecord(this, rr);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Status handling thread.								*/
	/*--------------------------------------------------------------*/

	/** The next time a status is to be run. */
	private int m_statusLines;

	private long m_ts_laststatus;

	private long m_previousRowCount;

	private long m_previousQueryCount;

	static public final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	static private final ThreadLocal<DateFormat> m_dateFormat = new ThreadLocal<DateFormat>();

	private StringBuilder m_status_sb = new StringBuilder(128);

	private void startStatusReporter() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				statusThreadRun();
			}
		});
		t.setName("status");
		t.setPriority(Thread.NORM_PRIORITY + 1);
		t.setDaemon(true);
		t.start();
	}

	private void statusThreadRun() {
		try {
			while(!isStopped()) {
				long ts = System.currentTimeMillis();
				long nts = ts + 5000; // Log every 5 seconds.
				displayStatus(ts);

				ts = System.currentTimeMillis();
				long delay = nts - ts;
				Thread.sleep(delay);
			}
		} catch(Exception x) {
			System.out.println("abnormal termination of status display thread: " + x);
		}
	}

	/**
	 * Format a date.
	 * @param dt
	 * @return
	 */
	static public final String format(Date dt) {
		DateFormat df = m_dateFormat.get();
		if(null == df) {
			df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			m_dateFormat.set(df);
		}
		return df.format(dt);
	}

	private void displayStatus(long ts) {
		long recnr, errs, xq, rr, skips;
		long lastrecordtime;
		long laststatustime;

		synchronized(this) {
			recnr = m_recordNumber;
			errs = m_execErrors;
			xq = m_executedQueries;
			rr = m_resultRows;
			skips = m_ignoredStatements;
			lastrecordtime = m_lastRecordTime;
			laststatustime = m_ts_laststatus;
			m_ts_laststatus = ts;
			if(xq == 0)
				return;
		}

		double qps;
		double rps;
		long sdt;
		if(laststatustime == 0) {
			//-- No previous measurement
			qps = 0.0;
			rps = 0.0;
			sdt = 0;
		} else {
			sdt = ts - laststatustime;
			qps = (xq - m_previousQueryCount) / (sdt / 1000.0);
			rps = (rr - m_previousRowCount) / (sdt / 1000.0);
		}


		if(m_statusLines++ % 20 == 0) {
			//--                0123 0123456789 0123456789 0123456789 0123456789 0123456789012345 0123456789 0123456789 0123456
			System.out.println("#act -#requests --#skipped ---#errors --#queries -----------#rows -queries/s ---#rows/s dT      realtime ");
			if(m_log != null)
				m_log.println("#act -#requests --#skipped ---#errors --#queries -----------#rows -queries/s ---#rows/s dT      realtime");
		}

		m_status_sb.setLength(0);
		m_status_sb.append(v(getInExecution(), 4));
		m_status_sb.append(v(recnr, 10));
		m_status_sb.append(v(skips, 10));
		m_status_sb.append(v(errs, 10));
		m_status_sb.append(v(xq, 10));
		m_status_sb.append(v(rr, 16));
		m_status_sb.append(dbl(qps, 10));
		m_status_sb.append(dbl(rps, 10));
		m_status_sb.append(dbl(sdt / 1000.0, 5));
		m_status_sb.append(DATEFORMAT.format(new Date(lastrecordtime)));
		String s = m_status_sb.toString();
		System.out.println(s);
		if(m_log != null)
			m_log.println(s);

		m_ts_laststatus = ts;
		m_previousQueryCount = xq;
		m_previousRowCount = rr;
	}

	static private final String SPACES = "                                     ";

	static private String v(long value, int npos) {
		String val = DbPoolUtil.strCommad(value);
		int nfill = npos - val.length();

		if(nfill <= 0)
			return val + " ";

		return SPACES.substring(0, nfill) + val + " ";
	}

	static private String dbl(double v, int npos) {
		String val = String.format("%g", Double.valueOf(v));
		int nfill = npos - val.length();

		if(nfill <= 0)
			return val + " ";

		return SPACES.substring(0, nfill) + val + " ";
	}

	public static void main(String[] args) {
		try {
			new DbReplay().run(args);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

}
