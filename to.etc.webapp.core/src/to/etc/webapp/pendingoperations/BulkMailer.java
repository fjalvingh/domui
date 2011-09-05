package to.etc.webapp.pendingoperations;

import java.io.*;
import java.lang.reflect.*;
import java.sql.*;

import javax.sql.*;

import to.etc.dbpool.*;
import to.etc.dbutil.*;
import to.etc.dbutil.DbLockKeeper.LockHandle;
import to.etc.smtp.*;
import to.etc.util.*;

/**
 * Bulk mailer storing messages into the database for repeated delivery.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 3, 2011
 */
public class BulkMailer {
	static private final BulkMailer m_instance = new BulkMailer();

	private SmtpTransport m_transport;

	private DataSource m_ds;

	private boolean m_inerror;

	private long m_ts_nextcleanup = 0;

	private long m_ts_nextscan = 0;

	static private enum DstType {
		TO, CC, BCC
	};

	static private enum RState {
		SEND, DONE, RTRY, FATL
	}

	static public BulkMailer getInstance() {
		return m_instance;
	}

	static public void initialize(DataSource ds, SmtpTransport t) {
		try {
			DbLockKeeper.init(ds);
		} catch(Exception x) {
			x.printStackTrace();
		}
		getInstance().init(ds, t);
	}

	private synchronized void init(DataSource ds, SmtpTransport t) {
		if(m_ds != null)
			throw new IllegalStateException("Already initialized");
		m_ds = ds;
		m_transport = t;

		//-- Register with the task executor
		PollingWorkerQueue.getInstance().registerProvider(new IPollQueueTaskProvider() {
			@Override
			public void initializeOnRegistration(PollingWorkerQueue pwq) throws Exception {}

			@Override
			public Runnable getRunnableTask() throws Exception {
				return checkForNextScan();
			}
		});
	}

	protected synchronized Runnable checkForNextScan() {
		long cts = System.currentTimeMillis();
		if(cts < m_ts_nextscan)
			return null;

		return new Runnable() {
			@Override
			public void run() {
				scanMailRun();
			}
		};
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Queueing email in the database.						*/
	/*--------------------------------------------------------------*/
	/**
	 * This stores the message into the database. This will cause the message to be sent asap.
	 * @param m
	 */
	public void store(Message m) throws Exception {
		Connection dbc = m_ds.getConnection();
		CallableStatement cs = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		OutputStream os = null;
		boolean ok = false;
		try {
			dbc.setAutoCommit(false);
			cs = dbc
				.prepareCall("begin insert into sys_mail_messages(smm_id, smm_date, smm_subject, smm_from_address, smm_from_name, smm_data) values(sys_smm_seq.nextval, sysdate, ?, ?, ?, empty_blob()) returning smm_id into ?; end;");
			cs.setString(1, StringTool.strTrunc(m.getSubject(), 240));
			cs.setString(2, StringTool.strTrunc(m.getFrom().getEmail(), 128));
			cs.setString(3, StringTool.strTrunc(m.getFrom().getName(), 64));
			cs.registerOutParameter(4, Types.NUMERIC);
			cs.executeUpdate();
			long key = cs.getLong(4);
			cs.close();
			cs = null;

			//-- Insert message binary stream.
			ps = dbc.prepareStatement("select smm_data from sys_mail_messages where smm_id=? for update");
			ps.setLong(1, key);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("Cannot relocate record I just stored");
			Blob b = rs.getBlob(1);
			os = (OutputStream) callObjectMethod(b, "getBinaryOutputStream");

			//-- Marshal mime data to the stream.
			SmtpTransport.writeMime(os, m); // Output mime body
			os.close();
			rs.close();
			ps.close();

			//-- Write recipient record(s).
			ps = dbc
				.prepareStatement("insert into sys_mail_recipients(smr_id, smr_address, smr_type, smr_date_posted, smr_retries, smr_nextretry, smr_state,smr_name,smm_id) values(sys_smr_seq.nextval, ?, ?, sysdate, 0, sysdate, 'SEND', ?, ?)");
			for(Address a : m.getTo()) {
				writeRecipient(ps, dbc, a, key, DstType.TO);
			}
			for(Address a : m.getCc()) {
				writeRecipient(ps, dbc, a, key, DstType.CC);
			}
			for(Address a : m.getBcc()) {
				writeRecipient(ps, dbc, a, key, DstType.BCC);
			}
			ps.close();
			dbc.commit();
			ok = true;
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(cs != null)
					cs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(!ok)
					dbc.rollback();
			} catch(Exception x) {}
			try {
				dbc.close();
			} catch(Exception x) {}
		}
	}

	private void writeRecipient(PreparedStatement ps, Connection dbc, Address a, long key, DstType type) throws SQLException {
		ps.setString(1, StringTool.strTrunc(a.getEmail(), 128));
		ps.setString(2, type.name());
		ps.setString(3, StringTool.strTrunc(a.getName(), 64));
		ps.setLong(4, key);
		ps.executeUpdate();
	}

	/**
	 * Generic caller of a method using reflection. This prevents us from having
	 * to link to the stupid Oracle driver.
	 * @param src
	 * @param name
	 * @return
	 * @throws Exception
	 */
	static private Object callObjectMethod(Object src, String name) throws SQLException {
		try {
			Method m = src.getClass().getMethod(name, new Class[0]);
			return m.invoke(src, new Object[0]);
		} catch(InvocationTargetException itx) {
			if(itx.getCause() instanceof SQLException)
				throw (SQLException) itx.getCause();
			throw new RuntimeException(itx.getCause().toString(), itx.getCause());
		} catch(Exception x) {
			throw new RuntimeException("Exception calling " + name + " on " + src + ": " + x, x);
		}
	}

	static private enum FailLoc {
		OKAY, DATABASE, DBLOCK,

	}


	/**
	 * This does a single mail scanning run. It will read recipients to mail to and
	 * mail them until the queue is empty.
	 */
	public void scanMailRun() {
		Connection dbc = null;
		LockHandle lock = null;
		FailLoc location = FailLoc.DATABASE;
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		InputStream is = null;

		byte[] lastbody = null;
		long lastmsgid = -1;
		String subject = null;
		String fromaddress = null;
		String fromname = null;
		Address froma = null;
		try {
			dbc = m_ds.getConnection();
			lock = DbLockKeeper.getInstance().lockNowait(getClass().getName());
			if(null == lock) {
				//-- Another server is already sending mail - we'll try it next time.
				location = FailLoc.OKAY;
				return;
			}

			//-- Ok: we own the lock.
			long cts = System.currentTimeMillis();
			if(m_ts_nextcleanup == 0)
				m_ts_nextcleanup = cts;
			else if(m_ts_nextcleanup < cts) {
				m_ts_nextcleanup = cts + 2l * 60l * 60l * 1000l;
				cleanup(dbc);
			}

			//-- Get all recipients that need a message sent,
			ps = dbc
				.prepareStatement(
					"select smr_id,smr_address,smr_type,smr_retries,smr_state,smr_name,smm_id,smr_lasterror,smr_nextretry from sys_mail_recipients where smr_state in ('RTRY', 'SEND') and smr_nextretry <= sysdate order by smm_id",
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			rs = ps.executeQuery();
			while(rs.next()) {
				long rid = rs.getLong(1);
				String email = rs.getString(2);
				DstType dt = DstType.valueOf(rs.getString(3));
				int retries = rs.getInt(4);
				RState ds = RState.valueOf(rs.getString(5));
				String name = rs.getString(6);
				long msgid = rs.getLong(7);

				if(lastmsgid != msgid) {
					lastmsgid = msgid;
					if(ps2 == null) {
						ps2 = dbc.prepareStatement("select smm_subject, smm_from_address, smm_from_name, smm_data from sys_mail_messages where smm_id=?");
					}

					ps2.setLong(1, msgid);
					rs2 = ps2.executeQuery();
					if(!rs2.next())
						throw new IllegalStateException("Cannot locate message record - integrity failure!?");
					subject = rs2.getString(1);
					fromaddress = rs2.getString(2);
					fromname = rs2.getString(3);
					froma = new Address(fromaddress, fromname);
					is = rs2.getBinaryStream(4);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					FileTool.copyFile(baos, is);
					baos.close();
					is.close();
					is = null;
					lastbody = baos.toByteArray();
				}

				Address a = new Address(email, name);

				System.out.println(">trying " + a + ": " + subject + ", " + (lastbody != null ? lastbody.length + " bytes" : ""));
				String error = sendMessage(froma, a, subject, lastbody);

				if(null == error) {
					//-- This send has worked- set to DONE.
					rs.updateString(5, RState.DONE.name());
				} else {
					//-- Failed, sigh. Store failure reason et al.
					retries++;
					rs.updateString(8, StringTool.strTrunc(error, 128));

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
							ft = 8 * 60; // Every 8 hours.
						}

						ft *= 60 * 1000;
						rs.updateTimestamp(9, new Timestamp(System.currentTimeMillis() + ft));
					}
				}
				rs.updateRow();
			}
			rs.close();
			rs = null;
			dbc.commit();
		} catch(Exception x) {
			x.printStackTrace();
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(rs2 != null)
					rs2.close();
			} catch(Exception x) {}
			try {
				if(ps2 != null)
					ps2.close();
			} catch(Exception x) {}
			try {
				if(lock != null)
					lock.release();
			} catch(Exception x) {}
			try {
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}
	}

	private void cleanup(Connection dbc) {
		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement("delete from sys_mail_recipients where (smr_state='DONE' and smr_nextretry<sysdate-2) or (smr_nextretry < sysdate-7)");
			int rc = ps.executeUpdate();
			System.out.println("bulkMail: deleted " + rc + " outdated recipients");
			if(rc != 0) {
				ps.close();
				ps = dbc.prepareStatement("delete from sys_mail_messages m where not exists (select 1 from sys_mail_recipients r where r.smm_id=m.smm_id)");
				rc = ps.executeUpdate();
				System.out.println("bulkMail: deleted " + rc + " outdated message bodies");
			}
		} catch(Exception x) {
			System.out.println("bulkMail: cannot cleanup recipients: " + x);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}


	/**
	 * Do the real message sending.
	 * @param froma
	 * @param a
	 * @param subject
	 * @param lastbody
	 * @return
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
			return x.toString();
		}
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ConnectionPool p = PoolManager.getInstance().definePool("voja");

			DataSource ds = p.getUnpooledDataSource();
			PollingWorkerQueue.initialize();

			BulkMailer.initialize(ds, new SmtpTransport("localhost"));

			if(false) {
				Message m = new Message();
				m.setFrom(new Address("jal@etc.to", "Frits Jalvingh"));
				m.addTo(new Address("jo.seaton@itris.nl", "Sea Joton"));
				m.addCc(new Address("marc.mol@itris.nl", "Morc Mal"));
				m.setSubject("[vp] Test email from the bulk mailer");
				m.setBody("Dit is een kleine test-email");
				getInstance().store(m);
			} else if(true) {
				getInstance().scanMailRun();


			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
