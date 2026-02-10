package to.etc.webapp.mailer;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import to.etc.dbpool.ConnectionPool;
import to.etc.dbpool.PoolManager;
import to.etc.dbutil.DbLockKeeper;
import to.etc.dbutil.DbLockKeeper.LockHandle;
import to.etc.dbutil.GenericDB;
import to.etc.smtp.Address;
import to.etc.smtp.Message;
import to.etc.smtp.SmtpTransport;
import to.etc.util.DeveloperOptions;
import to.etc.util.FileTool;
import to.etc.util.StringTool;
import to.etc.webapp.pendingoperations.IPollQueueTaskProvider;
import to.etc.webapp.pendingoperations.PollingWorkerQueue;
import to.etc.webapp.query.QDataContext;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Bulk mailer storing messages into the database for repeated delivery.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 3, 2011
 */
public class BulkMailer {
	static private final Logger LOG = org.slf4j.LoggerFactory.getLogger(BulkMailer.class);

	static private final BulkMailer m_instance = new BulkMailer();

	private SmtpTransport m_transport;

	private DataSource m_ds;

	private long m_tsNextCleanup = 0;

	private enum DstType {
		TO,
		CC,
		BCC
	}

	private enum RState {
		SEND,
		DONE,
		RTRY,
		FATL
	}

	static public BulkMailer getInstance() {
		return m_instance;
	}

	static public void initialize(DataSource ds, SmtpTransport t) throws Exception {
		try {
			DbLockKeeper.init(ds);
		} catch(Exception x) {
//			x.printStackTrace();
		}
		getInstance().init(ds, t);
	}

	final private class PollTaskProvider implements IPollQueueTaskProvider {
		private long m_tsNext = System.currentTimeMillis() + 20 * 1000;

		@Override
		public void initializeOnRegistration(PollingWorkerQueue pwq) throws Exception {
		}

		@Override
		public Runnable getRunnableTask() throws Exception {
			long cts = System.currentTimeMillis();
			synchronized(this) {
				if(cts < m_tsNext)
					return null;
				m_tsNext = cts + 60 * 1000;                // Try again in 1 minute.
			}
			return () -> scanMailRun();
		}
	}

	private synchronized void init(DataSource ds, SmtpTransport t) throws Exception {
		if(m_ds != null)
			throw new IllegalStateException("Already initialized");
		m_ds = ds;
		m_transport = t;

		createTables();

		//-- Register with the task executor
		if(DeveloperOptions.getBool("domui.mailer", !DeveloperOptions.isDeveloperWorkstation()))
			PollingWorkerQueue.getInstance().registerProvider(new PollTaskProvider());
	}

	/**
	 * Create all tables for this system function.
	 */
	private void createTables() throws Exception {
		try(Connection dbc = m_ds.getConnection()) {
			dbc.setAutoCommit(false);
			StringBuilder sb = new StringBuilder();
			GenericDB.runScriptResource(dbc, getClass(), "bulkmailer.sql", sb);

			if(sb.length() > 0)
				LOG.info(sb.toString());
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Queueing email in the database.						*/
	/*--------------------------------------------------------------*/

	/**
	 * This stores the message into the database. This will cause the message to be sent asap.
	 */
	public void store(@NonNull Message m) throws Exception {
		try(Connection dbc = m_ds.getConnection()) {
			store(dbc, m);
		}
	}

	/**
	 * This stores the message into the database. This will cause the message to be sent asap.
	 */
	public void store(@NonNull QDataContext dc, @NonNull Message m) throws Exception {
		store(dc.getConnection(), m);
	}

	/**
	 * This stores the message into the database. This will cause the message to be sent as soon as the connection is committed.
	 */
	public void store(@NonNull Connection dbc, @NonNull Message m) throws Exception {
		dbc.setAutoCommit(false);
		long key;
		try(PreparedStatement cs = dbc.prepareStatement("insert into sys_mail_messages(smm_id, smm_date, smm_subject, smm_from_address, smm_from_name) values(?, ?, ?, ?, ?)")) {
			int i = 1;
			key = GenericDB.getFullSequenceID(dbc, "sys_smm_seq");
			cs.setLong(i++, key);
			cs.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
			cs.setString(i++, StringTool.strTrunc(m.getSubject(), 240));
			cs.setString(i++, StringTool.strTrunc(m.getFrom().getEmail(), 128));
			cs.setString(i++, StringTool.strTrunc(m.getFrom().getName(), 64));
			cs.executeUpdate();
		}

		//-- Insert message binary stream.
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		//-- Marshal mime data to the stream.
		SmtpTransport.writeMime(os, m); // Output mime body
		os.close();

		byte[] data = os.toByteArray();
		GenericDB.setBlob(dbc, "sys_mail_messages", "smm_data", "smm_id=" + key, new ByteArrayInputStream(data), data.length);

		//-- Write recipient record(s).
		String sql = "insert into sys_mail_recipients(smr_id, smr_address, smr_type, smr_date_posted, smr_retries, smr_nextretry, smr_state,smr_name,smm_id) values(?, ?, ?, ?, 0, ?, 'SEND', ?, ?)";
		try(PreparedStatement ps = dbc.prepareStatement(sql)) {
			for(Address a : m.getTo()) {
				writeRecipient(ps, dbc, a, key, DstType.TO);
			}
			for(Address a : m.getCc()) {
				writeRecipient(ps, dbc, a, key, DstType.CC);
			}
			for(Address a : m.getBcc()) {
				writeRecipient(ps, dbc, a, key, DstType.BCC);
			}
		}

		dbc.commit();
	}

	private void writeRecipient(PreparedStatement ps, Connection dbc, Address a, long key, BulkMailer.DstType type) throws SQLException {
		long sq = GenericDB.getFullSequenceID(dbc, "sys_smr_seq");
		Timestamp now = new Timestamp(System.currentTimeMillis());
		int i = 1;
		ps.setLong(i++, sq);
		ps.setString(i++, StringTool.strTrunc(DeveloperOptions.getString("debug.email", a.getEmail()), 128));
		ps.setString(i++, type.name());
		ps.setTimestamp(i++, now);
		ps.setTimestamp(i++, now);
		ps.setString(i++, StringTool.strTrunc(a.getName(), 64));
		ps.setLong(i, key);
		ps.executeUpdate();
	}

	/**
	 * This does a single mail scanning run. It will read recipients to mail to and
	 * mail them until the queue is empty.
	 */
	public void scanMailRun() {
		byte[] lastbody = null;
		long lastmsgid = -1;
		String subject = null;
		String fromaddress = null;
		String fromname = null;
		Address froma = null;
		try(Connection dbc = m_ds.getConnection()) {
			dbc.setAutoCommit(false);
			try(LockHandle lock = DbLockKeeper.getInstance().lockNowait(getClass().getName())) {
				if(null == lock) {
					//-- Another server is already sending mail - we'll try it next time.
					LOG.debug("Bulk mailer lock is taken - done");
					return;
				}
				LOG.info("Scanning for email to send");
				//			System.out.println("Scanning for email to send");

				//-- Ok: we own the lock.
				long cts = System.currentTimeMillis();
				if(m_tsNextCleanup == 0)
					m_tsNextCleanup = cts;
				else if(m_tsNextCleanup < cts) {
					m_tsNextCleanup = cts + 2l * 60l * 60l * 1000l;
					cleanup(dbc);
				}

				//-- Get all recipients that need a message sent,
				String sql = "select smr_id,smr_address,smr_type,smr_retries,smr_state,smr_name,smm_id,smr_lasterror,smr_nextretry from sys_mail_recipients where smr_state in ('RTRY', 'SEND') and smr_nextretry <= ? order by smm_id";
				String sql2 = "select smm_subject, smm_from_address, smm_from_name, smm_data from sys_mail_messages where smm_id=?";
				try(PreparedStatement ps = dbc.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
					PreparedStatement ps2 = dbc.prepareStatement(sql2)) {
					ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
					try(ResultSet rs = ps.executeQuery()) {
						while(rs.next()) {
							String email = rs.getString(2);
							int retries = rs.getInt(4);
							String name = rs.getString(6);
							long msgid = rs.getLong(7);

							if(lastmsgid != msgid) {
								lastmsgid = msgid;
								ps2.setLong(1, msgid);
								try(ResultSet rs2 = ps2.executeQuery()) {
									if(!rs2.next())
										throw new IllegalStateException("Cannot locate message record - integrity failure!?");
									subject = rs2.getString(1);
									fromaddress = rs2.getString(2);
									fromname = rs2.getString(3);
									froma = new Address(fromaddress, fromname);
									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									try(InputStream is = rs2.getBinaryStream(4)) {
										FileTool.copyFile(baos, is);
										baos.close();
									}
									lastbody = baos.toByteArray();
								}
							}

							Address a = name != null ? new Address(email, name) : new Address(email);

							String error = sendMessage(froma, a, subject, lastbody);

							if(null == error) {
								//-- This send has worked- set to DONE.
								rs.updateString(5, RState.DONE.name());
							} else {
								//-- Failed, sigh. Store failure reason et al.
								retries++;
								rs.updateString(8, StringTool.strTrunc(error, 128));
								rs.updateInt(4, retries);

								if(retries > 20)
									rs.updateString(5, RState.FATL.name());
								else {
									rs.updateString(5, RState.RTRY.name());

									//-- Calculate fallback time, in minutes
									long ft;
									if(retries < 5) {
										ft = 2; // Every 2 minutes
									} else if(retries < 10) {
										ft = 60; // Every hour: try for 10 hours every hour
									} else {
										ft = 8 * 60L; // Every 8 hours.
									}

									ft *= 60 * 1000;
									rs.updateTimestamp(9, new Timestamp(System.currentTimeMillis() + ft));
								}
							}
							rs.updateRow();
						}
					}
				}
				dbc.commit();
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	static private final long DAY = 1000l * 24 * 60 * 60;

	private void cleanup(Connection dbc) {
		int rc;
		try {
			try(PreparedStatement ps = dbc.prepareStatement("delete from sys_mail_recipients where (smr_state='DONE' and smr_nextretry<?) or (smr_nextretry < ?)")) {
				ps.setTimestamp(1, new Timestamp(System.currentTimeMillis() - DAY * 2));
				ps.setTimestamp(2, new Timestamp(System.currentTimeMillis() - DAY * 7));
				rc = ps.executeUpdate();
				if(rc > 0)
					System.out.println("bulkMail: deleted " + rc + " outdated recipients");
			}

			if(rc != 0) {
				try(PreparedStatement ps = dbc.prepareStatement("delete from sys_mail_messages m where not exists (select 1 from sys_mail_recipients r where r.smm_id=m.smm_id)")) {
					rc = ps.executeUpdate();
					if(rc > 0)
						System.out.println("bulkMail: deleted " + rc + " outdated message bodies");
				}
			}
		} catch(Exception x) {
			System.out.println("bulkMail: cannot cleanup recipients: " + x);
		}
	}

	/**
	 * Do the real message sending.
	 */
	private String sendMessage(Address froma, Address a, String subject, byte[] lastbody) {
		try {
			Message m = new Message();
			m.setFrom(froma);
			m.addTo(a);
			m.setSubject(subject);
			m_transport.send(m, new ByteArrayInputStream(lastbody));
			return null;
		} catch(Exception x) {
			x.printStackTrace();
			return x.toString();
		}
	}

	/**
	 *
	 */
	public static void main(String[] args) {
		try {
			ConnectionPool p = PoolManager.getInstance().definePool("pzlnew");

			DataSource ds = p.getUnpooledDataSource();
			PollingWorkerQueue.initialize();

			BulkMailer.initialize(ds, new SmtpTransport("localhost"));

			if(true) {
				Message m = new Message();
				m.setFrom(new Address("jal@etc.to", "Frits Jalvingh"));
				m.addTo(new Address("jo.seaton@itris.nl", "Sea Joton"));
				m.addCc(new Address("marc.mol@itris.nl", "Morc Mal"));
				m.setSubject("[vp] Test email from the bulk mailer");
				m.setBody("Dit is een kleine test-email");
				m.setHtmlBody("<h1>Hello, world</h1>\n");

				getInstance().store(m);

				Thread.sleep(60000);
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
