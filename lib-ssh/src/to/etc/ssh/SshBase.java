package to.etc.ssh;

import java.io.*;
import java.net.*;
import java.util.*;

import ch.ethz.ssh2.*;

import to.etc.util.*;

/**
 * Base class for handling SSH commands. It encapsulates a connection to some
 * machine and has simple helper commands to handle simple machine calls.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 17, 2010
 */
public class SshBase {
	private String m_hostName;

	private String m_userId;

	private String m_privateKey;

	private String m_passPhrase;

	private Connection m_connection;

	private String m_stdout;

	private String m_stderr;

	private int m_resultCode;

	public SshBase() {}

	public SshBase(String hostName, String userId, String privateKey, String pass) {
		m_hostName = hostName;
		m_userId = userId;
		m_privateKey = privateKey;
		m_passPhrase = pass;
	}

	public String getPassPhrase() {
		return m_passPhrase;
	}

	public void setPassPhrase(String passPhrase) {
		m_passPhrase = passPhrase;
	}

	public String getHostName() {
		return m_hostName;
	}

	public void setHostName(String hostName) {
		m_hostName = hostName;
	}

	public String getUserId() {
		return m_userId;
	}

	public void setUserId(String userId) {
		m_userId = userId;
	}

	public String getPrivateKey() {
		return m_privateKey;
	}

	public void setPrivateKey(String privateKey) {
		m_privateKey = privateKey;
	}

	public void detail(String s) {}

	protected void log(String string) {}

	/**
	 * Connect if not already connected.
	 */
	public void connect() {
		if(m_connection != null)
			return;

		boolean ok = false;
		Connection sshc = null;
		try {
			detail("Connecting to " + getHostName() + " using ssh");
			sshc = new Connection(getHostName());

			/*ConnectionInfo cif = */sshc.connect();
			if(!sshc.authenticateWithDSA(getUserId(), getPrivateKey(), getPassPhrase())) {
				throw new SshException("Connection (authentication) to " + getUserId() + "@" + getHostName() + " has failed");
			}
			log("Connected and authenticated to " + getUserId() + "@" + getHostName());
			m_connection = sshc;
			ok = true;
		} catch(Exception x) {
			log("ssh connection to " + getUserId() + "@" + getHostName() + " failed: " + x);
			throw new SshException("ssh connection to " + getUserId() + "@" + getHostName() + " failed: " + x);
		} finally {
			try {
				if(sshc != null && !ok)
					sshc.close();
			} catch(Exception x) {}
		}
	}

	public void close() {
		if(m_connection == null)
			return;
		try {
			m_connection.close();
		} catch(Exception x) {
			log("Failed to close connection to " + getUserId() + "@" + getHostName() + ": " + x);
		} finally {
			m_connection = null;
		}
	}

	/**
	 * Copy some file to the remove server, to a given target file name.
	 * @param sshc
	 * @param ds
	 * @param src
	 * @param remotename
	 * @return
	 */
	public void putFile(File src, String remotename) {
		//- Make remoteName absolute, if needed
		if(!remotename.startsWith("/"))
			remotename = "/home/" + getUserId() + "/" + remotename;
		File base = new File(remotename);

		connect();
		try {
			detail("Copying to " + getHostName() + ":" + remotename + " (" + src.length() + " bytes)");
			SCPClient client = new SCPClient(m_connection);
			client.put(src.getAbsolutePath(), base.getName(), base.getParent(), "0660");
		} catch(Exception x) {
			log("ssh copy to " + getHostName() + ":" + remotename + " failed: " + x);
			throw new SshException("ssh copy to " + getHostName() + ":" + remotename + " failed: " + x);
		}
	}

	/**
	 *
	 * @param command
	 * @return
	 */
	public int command(String command) {
		connect();
		Session ses = null;
		try {
			ses = m_connection.openSession();
			ses.requestDumbPTY();
			ses.execCommand(command);
			StringBuffer stdoutsb = new StringBuffer(8192);
			StringBuffer stderrsb = new StringBuffer(8192);
			ProcessTools.StreamReaderThread ot = new ProcessTools.StreamReaderThread(stdoutsb, "stdout", ses.getStdout());
			ProcessTools.StreamReaderThread et = new ProcessTools.StreamReaderThread(stderrsb, "stderr", ses.getStderr());
			ot.start();
			et.start();

			//-- Wait till all output is sent and complete
			ot.join(2 * 60 * 1000);
			et.join(2 * 60 * 1000);

			if(ot.isAlive())
				ot.interrupt();
			if(et.isAlive())
				et.interrupt();

			ses.waitForCondition(ChannelCondition.CLOSED, 60000); // 20100517 jal This makes the exitstatus call work properly, mostly.
			m_stdout = stdoutsb.toString();
			m_stderr = stderrsb.toString();
			Integer xl = ses.getExitStatus();
			//			System.out.println("ssh command, exitcode=" + xl + " output-\n------ stdout-------");
			//			System.out.println(m_stdout);
			//			System.out.println("\n-----stderr-------");
			//			System.out.println(m_stderr);
			//			System.out.println("----- end -----");

			if(xl == null) {
				log("Unknown return value for ssh command " + command);
				m_resultCode = -1;
				return -1;
			}
			m_resultCode = xl.intValue();
			if(xl.intValue() != 0)
				log("ssh command '" + command + "' failed with errorlevel " + xl);

			return xl.intValue();
		} catch(Exception x) {
			log("ssh command '" + command + "' failed: " + x);
			throw new SshException("ssh command '" + command + "' failed: " + x);
		} finally {
			try {
				if(ses != null)
					ses.close();
			} catch(Exception x) {}
		}
	}

	public String getStderr() {
		return m_stderr;
	}

	public String getStdout() {
		return m_stdout;
	}

	public int getResultCode() {
		return m_resultCode;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple Locking and unlocking code.					*/
	/*--------------------------------------------------------------*/

	/**
	 * Represents a lock gotten (or not)
	 */
	public class Lock {
		private String m_ident;

		private String m_path;

		public Lock(String path) {
			m_path = path;
		}

		void setIdent(String ident) {
			m_ident = ident;
		}

		public void unlock() {
			unlockLock(this);
		}

		public String getPath() {
			return m_path;
		}

		String getIdent() {
			return m_ident;
		}
	}

	/** All locks currently taken using this channel. */
	private Map<String, Lock> m_lockMap = new HashMap<String, Lock>();

	/**
	 * Try to lock the specified version. This connects to the remote, creates a ".fixmaker/lock" directory
	 * and file in the version's patch root. If that file already exists a lock exists; the file is then read
	 * to see who has locked.
	 *
	 * @param v
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public Lock lock(String lockname, String dirpath, long timeout) throws Exception {
		Lock l = m_lockMap.get(dirpath);
		if(l != null)
			return l;

		connect();
		long ets = System.currentTimeMillis() + timeout; // When do we stop?
		boolean warned = false;
		l = new Lock(dirpath);
		for(;;) {
			String locker = tryLock(l);
			if(locker == null) {
				//-- Lock aquired!!
				m_lockMap.put(dirpath, l);
				return l;
			}
			if(System.currentTimeMillis() >= ets) // Lock timed out.
				return null;

			if(!warned) {
				warned = true;
				lockWaiting(".. waiting for lock on " + lockname + " currently held by " + locker);
			}
			Thread.sleep(5000);
		}
	}

	/**
	 * Unlock an existing lock.
	 */
	void unlockLock(Lock lock) {
		//-- Make sure the thing is not already unlocked: then we have a nesting error.
		if(m_lockMap.get(lock.getPath()) != lock || lock.getIdent() == null)
			throw new IllegalStateException("SSH Lock for " + lock.getPath() + " already released - possible nesting error?");

		//-- Try to unlock and make sure we're the one that locked it last;
		String locker = getCurrentLocker(lock.getPath());
		if(locker == null)
			throw new SshException("The lock for " + lock.getPath() + " appears to have been BROKEN");

		if(!locker.equalsIgnoreCase(lock.getIdent()))
			throw new SshException("The lock for " + lock.getPath() + " has been re-allocated by " + locker);

		//-- Now we can really remove it.
		int rc = command("rm -rf " + lock.getPath());
		if(rc != 0)
			throw new SshException("Error while unlocking " + lock.getPath() + ": " + getStdout());
		m_lockMap.remove(lock.getPath());
		lock.setIdent(null);
	}

	/**
	 * Get the contents of the lock file which defines whom is locking that path. It can return null if
	 * the lock file cannot be read.
	 * @param path
	 * @return
	 */
	public String getCurrentLocker(String path) {
		int rc = command("cat " + path + "/lock");
		if(rc == -1)
			throw new IllegalStateException("ssh server does not send a command exitcode.");
		if(rc == 0)
			return getSingleLine(getStdout());
		return null;
	}

	/**
	 * Force the lock broken.
	 * @param path
	 */
	public boolean breakLock(String path) {
		String locker = getCurrentLocker(path);
		if(locker == null)
			return false;
		detail("breaking lock by " + locker);
		return command("rm -rf " + path) == 0;
	}

	static private String getSingleLine(String in) {
		StringBuilder sb = new StringBuilder(in.length());
		int used = 0;
		LineNumberReader lr = new LineNumberReader(new StringReader(in));
		String line;
		try {
			while(null != (line = lr.readLine())) {
				line = line.trim();
				if(line.length() == 0)
					continue;
				if(used > 0)
					sb.append("\n");
				sb.append(line);
				used++;
			}
		} catch(IOException x) {
			// Cannot happen; checked exceptions are idiocy.
		}
		return sb.toString();
	}

	protected void lockWaiting(String msg) {
		System.out.println(msg);
	}

	/**
	 * Single-shot attempt to lock a version. If it fails it returns the details of the current locker
	 * by reading it's lockfile. We try to make locks atomically mostly by creating a directory remotely
	 * which will fail if it already exists.
	 * @param v
	 * @return
	 */
	private String tryLock(Lock pendingLock) {
		String lasterr = null;
		String ident = me();
		for(int tries = 0; tries < 3; tries++) {
			int rc = command("/bin/mkdir " + pendingLock.getPath());
			if(rc == -1)
				throw new IllegalStateException("ssh server does not send a command exitcode.");

			if(rc != 0) {
				lasterr = getStdout();

				//-- Houston, we have a locking problem... Try to get the contents of the lockfile;
				rc = command("cat " + pendingLock.getPath() + "/lock");
				if(rc == 0) {
					return getSingleLine(getStdout());
				} else
					lasterr = getStdout();
			} else {
				//-- Create the lockfile containing my details.
				rc = command("echo >" + pendingLock.getPath() + "/lock '" + ident + "'");
				if(rc == 0) {
					pendingLock.setIdent(ident);
					return null;
				}
			}
		}
		lockWaiting("!! Cannot get lock details: " + lasterr);
		throw new SshException("Cannot get lock details from server");
	}

	protected String me() {
		Date now = new Date();
		String me = System.getProperty("user.name");
		String net = "unknown";
		try {
			InetAddress a = InetAddress.getLocalHost();
			net = a.getHostAddress();
			String hn = a.getCanonicalHostName();
			if(net.startsWith("127.")) {
				try {
					InetAddress a2 = InetAddress.getByName(hn);
					net = a2.getHostAddress();
				} catch(Exception x) {}
			}
			net = hn + "(" + net + ")";
		} catch(Exception x) {}
		return me + " on " + net + " at " + now;
	}


}
