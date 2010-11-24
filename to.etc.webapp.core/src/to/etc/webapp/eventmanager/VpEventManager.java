/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.webapp.eventmanager;

import java.io.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import javax.sql.*;

import org.slf4j.*;

/**
 * <h1>Overview</h1>
 * <p>This is an implementation of an event manager. It will handle two
 * forms of events:
 * <ul>
 *  <li>Notifications. These are indications to all servers in a VP
 *      clusters that something has happened. Notifications are used
 *      to synchronize server(s) state. Notifications are not guarantueed
 *      to be delivered around server startup/shutdown/abort, but they do
 *      pass to each server</li>
 *  <li>Events. An event is a server-local passing around of a notification. Each
 *      notification is also an event, and these are always passed synchronously
 *      with the call. Events only occur within the server that posts the
 *      notification. Events are typically used for interfacing with the
 *      outer world.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>De event manager wordt gebruikt om gebeurtenissen die plaatsvinden ergens
 * in ViewPoint te melden aan geinteresseerde stukken code. Neem als voorbeeld
 * de koppeling met Casper of met MOCA. Casper en MOCA willen alle wijzigingen
 * die een ViewPoint gebruiker maakt op een werkbon zo snel mogelijk weten. Een
 * "flut"methode om dit voor elkaar te krijgen is de methode die nu voor de Casper
 * koppeling gebruikt wordt: laat om de paar seconden iets in de database "pollen"
 * om te zien of er iets gewijzigd is. Dit is erg duur en niet herbruikbaar: voor
 * ieder pakket wat dezelfde informatie nodig heeft moet opnieuw zo'n stuk geschreven
 * worden wat pollt.</p>
 *
 * <p>Het Event mechanisme gaat als alternatief hiervoor dienen, en wordt later verder
 * uitgebouwd om SOA events mogelijk te maken vanuit ViewPoint. Met dit event mechanisme
 * is de koppeling naar Casper op een veel eenvoudiger manier mogelijk, als volgt:
 * <ul>
 *  <li>Op alle plaatsen binnen ViewPoint waar een voor de koppeling belangrijk gegeven
 *      wordt aangepast wordt de Event manager gebruikt om een Event af te vuren. Zo'n
 *      event is van een bepaalt type (bijvoorbeeld een WerkbonChangedEvent) en heeft
 *      een aantal gegevens aan boord over de gebeurtenis. In dit geval o.a. de primary
 *      key van het gewijzigde werkbon-record, en een update-type (delete, add, modify).</li>
 *  <li>Binnen ViewPoint is niet bekend <i>waarvoor</i> het event gebruikt wordt. Iedere
 *      belangstellende kan zich bij de Event Manager <b>abonneren</b> op dit specifieke
 *      event. Dit doet men door een <i>Listener</i> te registreren (en op SOA door een
 *      Publish/Subscribe mechanisme). Wanneer een Event wordt afgevuurd door ViewPoint
 *      dan zorgt de Event manager ervoor dat alle geregistreerde Listeners aangeroepen
 *      worden met de data van het Event.</li>
 *  <li>In het geval van de Casper koppeling zou deze zich registreren als geinteresseerd
 *      in het "WerkbonChangedEvent". De Listener voor dit event zou de primary key in
 *      de Event gebruiken om het betreffende Werkbon record op te vragen en door te sturen
 *      naar de Casper database, idealiter via een vastgestelde API.</li>
 * </ul>
 * </p>
 *
 * <p>Dit mechanisme ontkoppelt code door gebeurtenissen te scheiden van het afhandelen
 * van de gevolgen voor de verschillende koppelingen. Daarnaast is de koppeling goedkoop
 * (geen polling*), herbruikbaar en interactief.
 * </p>
 *
 * <h2>Events versus Notifications</h2>
 * <p>Hierbij worden events gebruikt voor code wat <i>altijd</i> uitgevoerd moet worden
 * en alleen op de server waarop het event optreed. Notifications dienen om VP servers
 * onderling te synchroniseren, en notifications worden niet gegarandeerd afgeleverd rond
 * server startup/shutdown.</p>
 *
 * <h2>Implementatie</h2>
 * <p>De huidige implementatie is suboptimaal omdat nog steeds polling gebruikt wordt, maar
 * ze is later in geval van problemen te vervangen mits alleen de publieke interface in deze
 * class gebruikt wordt. Hoewel er gepolled wordt is de overhead tamelijk miniem omdat slechts
 * een enkele polling gebruikt wordt voor alle notifications.</p>
 *
 * <p>De huidige implementatie creeert een Oracle table RED_VP_EVENTS in de database. Deze table
 * ziet er uit als:
 * <pre>
 *      UPID        NUMERIC(20, 0) not null primary key,
 *      UTIME       DATE not null,
 *      EVNAME      VARCHAR(80) not null,
 *      SERVER      VARCHAR(80) not null,
 *      OBJ         BLOB
 * </pre>
 * De UPID PK is monotoon stijgend gevuld vanuit de sequence RED_VP_EVENTS_SQ. Zowel tabel als
 * sequence worden door de code zelf gecreeerd. Ieder event wat wordt gegenereerd door de code
 * wordt als enkel record in deze tabel opgenomen.</p>
 *
 * <p>Iedere ViewPoint applicatie-server in het cluster heeft een eigen instance van de EventManager
 * singleton. Tijdens initialisatie start deze instance een thread die iedere 4 seconden een
 * select doet op deze tabel: select ... from RED_VP_EVENTS where upid > :last:.
 * Door telkens de laatst ingelezen UPID te onthouden is dit een erg goedkope query die via de index
 * alleen de voor de server nieuwe events leest. Doordat alle servers dezelfde tabel lezen worden
 * events die op server A gegenereerd worden ook door alle andere servers gezien, zodat alle servers
 * de bijbehorende event listeners aanroepen.</p>
 *
 * <p>Het aanroepen van de event listeners is normaliter <b>asynchroon</b>: de event polling thread
 * roept ze pas aan wanneer de events in de database gezien worden. Dit geldt zelfs voor de server
 * die de event genereert. Er bestaat echter wel een call waarmee de postende server synchroon de
 * event handlers aanroept, dus wanneer de postEventSynchronous() call terugkeert dan zijn alle
 * handlers aangeroepen geweest. Dit synchrone gedrag geldt echter allen voor de postende server; de
 * andere servers voeren de handlers pas uit wanneer de polling thread het event in de database
 * tegenkomt.</p>
 *
 * <p>De code controleert dat een event slechts eenmaal uitgevoerd wordt door de UPID van het laatst
 * geziene event steeds te bewaren. Dit voorkomt tevens dat de event tabel steeds in het geheel
 * doorgelezen moet worden om nieuwe events te vinden.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 12, 2006
 */
public class VpEventManager implements Runnable {
	static private final Logger LOG = LoggerFactory.getLogger(VpEventManager.class);

	static private final long DELETEINTERVAL = 10 * 60 * 1000;

	static private final long POLLINTERVAL = 1 * 1000;

	static private class Item {
		public Object m_obj;

		public ListenerType m_type;

		public Item(final ListenerType t, final Object o) {
			m_type = t;
			m_obj = o;
		}
	};

	static private final VpEventManager m_instance = new VpEventManager();

	private DataSource m_ds;

	private String m_tableName;

	/** The last update ID that was encountered while scanning the set. */
	private long m_upid = -1;

	/** The cached local DNS name for this server, info pps */
	private String m_serverName;

	/** The time that we need to delete stuff again, */
	private long m_ts_nextdelete;

	/** The upid to delete up to */
	private long m_delete_upid;

	/** When set the event manager will stop. */
	private boolean m_stop;

	/** The thread executing the event handler's main loop. */
	private Thread m_handlerThread;

	private final TreeSet<Long> m_localEvents = new TreeSet<Long>();

	/**
	 * The listeners to events, indexed by their event name.
	 */
	private final Map<String, List<Item>> m_listenerList = new HashMap<String, List<Item>>();

	enum DbType {
		ORACLE, POSTGRES
	};

	private DbType m_dbtype;

	/*--------------------------------------------------------------*/
	/*	CODING:	Singleton init.                                  	*/
	/*--------------------------------------------------------------*/

	private VpEventManager() {
		try {
			m_serverName = InetAddress.getLocalHost().getCanonicalHostName();
		} catch(Exception x) {
			m_serverName = "unknown";
		}
	}

	/**
	 * Get the instance. If the instance has not yet initialized
	 * this waits until init has commenced. If init does not
	 * complete in 60 secs this aborts.
	 *
	 * @return
	 */
	static public VpEventManager getInstance() {
		synchronized(m_instance) {
			int tries = 0;
			for(;;) {
				if(m_instance.m_ds != null)
					return m_instance;
				if(tries++ > 3)
					throw new IllegalStateException("Timeout waiting for VpEventManager to complete initialization [FATAL]");
				try {
					m_instance.wait(20000);
				} catch(Exception x) {}
				System.out.println("VpEventManager: Waiting for initialization");
			}
		}
	}

	static public void initialize(final DataSource ds, final String tableName) throws Exception {
		synchronized(m_instance) {
			if(m_instance.m_ds != null)
				return; // Already initialized
			m_instance.init(ds, tableName); // Do formal init
			m_instance.notify();
		}
	}

	private void log(final String s) {
		LOG.debug(s);
	}

	private void exception(final Throwable t, final String s) {
		LOG.error(s, t);
		System.out.println("VpEventManager: EXCEPTION " + s);
		t.printStackTrace();
	}

	public void stop() {
		System.out.println("KILLING EVENTMANAGER THREAD");
		synchronized(this) {
			m_stop = true;
			notifyAll();
		}
		try {
			m_handlerThread.join(10000);
		} catch(InterruptedException x) {}
		if(m_handlerThread.isAlive())
			log("The event manager's thread failed to die!?");
	}

	/**
	 * Tries to create the table if it doesn't exist. Ignores all errors.
	 *
	 * @param dbc
	 */
	private void createTable(final Connection dbc) {
		PreparedStatement ps = null;
		try {
			//-- Determine the database type
			String name = dbc.getMetaData().getDatabaseProductName().toLowerCase();
			if(name.contains("oracle"))
				m_dbtype = DbType.ORACLE;
			else if(name.contains("postgres"))
				m_dbtype = DbType.POSTGRES;
			else
				throw new IllegalStateException("Unsupported database type: " + name);

			String tbl, seq;
			switch(m_dbtype){
				default:
					throw new IllegalStateException("Unhandled DBTYPE: " + m_dbtype);
				case ORACLE:
					tbl = "create table " + m_tableName + "( upid numeric(20,0) not null primary key, utime date not null, evname varchar(80) not null, server varchar(32) not null, obj blob)";
					seq = "create sequence " + m_tableName + "_SQ start with 1 increment by 1";
					break;

				case POSTGRES:
					tbl = "create table " + m_tableName + "( upid numeric(20,0) not null primary key, utime date not null, evname varchar(80) not null, server varchar(32) not null, obj bytea)";
					seq = "create sequence " + m_tableName + "_SQ start with 1 increment by 1";
					break;
			}
			ps = dbc.prepareStatement(tbl);
			ps.executeUpdate();
			ps.close();

			//-- Create the sequence,
			ps = dbc.prepareStatement(seq);
			ps.executeUpdate();
		} catch(Exception x) {
			String msg = x.toString().toLowerCase();

			//-- Ignore silly errors.
			if(msg.contains("ora-00955"))
				return;
			if(msg.contains("exist"))
				return;
			System.out.println("SystemEventManager: table creation exception " + x + ", if this is just because the table already exists there is no problem.");
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * This initializes the event manager and should be called early in system startup. System startup should abort (fail) if this
	 * causes an exception. This creates the database table (if needed), opens the event manager for event registration
	 * and allows posting events. The event handler thread is *not* started though - it should be started by a call to start() after
	 * system initialization completes fully, to allow started events to use the entire system.
	 *
	 * @param ds
	 * @param tableName
	 * @throws Exception
	 */
	private synchronized void init(final DataSource ds, final String tableName) throws Exception {
		m_tableName = tableName;
		Connection dbc = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			dbc = ds.getConnection();
			createTable(dbc); // Make sure a database table exists

			//-- Get the last update #
			ps = dbc.prepareStatement("select max(upid) from " + m_tableName);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new IllegalStateException("?? Cannot get max update number");
			m_upid = rs.getLong(1);
			m_delete_upid = m_upid;
			m_ts_nextdelete = System.currentTimeMillis() + DELETEINTERVAL;
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}
		m_ds = ds;
	}

	/**
	 * Must be called after init to actually start handling events.
	 */
	public synchronized void start() {
		synchronized(m_instance) {
			if(m_handlerThread != null)
				return;
			m_handlerThread = new Thread(this);
			m_handlerThread.setName("SystemEventManager");
			m_handlerThread.setDaemon(true);
			m_handlerThread.start();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Reading and cleaning up events.                     */
	/*--------------------------------------------------------------*/
	private void checkPendingDeletes(final Connection dbc) throws Exception {
		long deleteupid = 0;
		synchronized(this) {
			if(m_delete_upid + 100 > m_upid) // Table smaller than 100 elements -> don't bother
				return;
			long ts = System.currentTimeMillis();
			if(ts < m_ts_nextdelete) // Timeout not expired
				return;
			m_ts_nextdelete = ts + DELETEINTERVAL; // Set new timeout
			deleteupid = m_delete_upid;
			m_delete_upid = m_upid;
		}

		//-- We must delete...
		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement("delete from " + m_tableName + " where upid < ?");
			ps.setLong(1, deleteupid);
			ps.executeUpdate();
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Checks for updates on this checker, and handles them if found. To prevent
	 * multiple threads from handling updates this locks the instance.
	 */
	private void scanNewEvents(final List<AppEventBase> al, final Set<Long> localeventset) throws Exception {
		Connection dbc = m_ds.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement("select upid,evname,utime,server,obj from " + m_tableName + " where upid > ? order by upid");
			ps.setLong(1, m_upid);
			rs = ps.executeQuery();
			while(rs.next()) {
				readEventObject(rs, al);
			}

			//-- Remove all saved "locally generated" events up to the event we've just read,
			synchronized(this) {
				Iterator<Long> it = m_localEvents.iterator();
				while(it.hasNext()) {
					Long v = it.next();
					if(v.longValue() <= m_upid) {
						it.remove();
						localeventset.add(v);
					} else
						break;
				}
			}

			checkPendingDeletes(dbc);
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Reads a single row from the event list. Skips serialization errors.
	 * @param rs
	 * @param al
	 * @throws Exception
	 */
	private void readEventObject(final ResultSet rs, final List<AppEventBase> al) throws Exception {
		//-- 1. Get fields
		long upid = rs.getLong(1);
		synchronized(this) {
			if(upid > m_upid)
				m_upid = upid;
		}
		//        String  evname  = rs.getString(2);
		Timestamp ts = rs.getTimestamp(3);
		String server = rs.getString(4);
		Blob b = rs.getBlob(5);

		//-- Unserialize
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(b.getBinaryStream());
			Object oo = ois.readObject();
			if(oo == null) {
				log("Event " + upid + " skipped: the embedded object is null");
				return;
			}

			if(!(oo instanceof AppEventBase)) {
				log("Event " + upid + ": The stored object is not an AppEvent but a " + oo.getClass().getCanonicalName());
				return;
			}

			//-- Update the AppEvent with the data read (should not be necessary)
			AppEventBase e = (AppEventBase) oo;
			e.setServer(server);
			e.setTimestamp(ts);
			e.setUpid(upid);
			al.add(e);
		} catch(Exception x) {
			log("Event " + upid + ": serialization got exception " + x);
			//			x.printStackTrace();
		} finally {
			try {
				if(ois != null)
					ois.close();
			} catch(Exception x) {}
		}
	}

	private void handleEvents(final List<AppEventBase> list, final Set<Long> localeventset) {
		for(int i = 0; i < list.size(); i++) {
			AppEventBase ae = list.get(i);
			callListeners(ae, false, localeventset.contains(Long.valueOf(ae.getUpid()))); // Call all handlers that need delayed notification
		}
	}

	/**
	 * Janitor-called entry which scans for new events and passes them on.
	 */
	private void scanOnce() {
		try {
			List<AppEventBase> list = new ArrayList<AppEventBase>();
			Set<Long> localeventset = new HashSet<Long>();
			scanNewEvents(list, localeventset);
			if(list.size() == 0)
				return;
			log("Forwarding " + list.size() + " events.");
			handleEvents(list, localeventset);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	/**
	 * Thread entry.
	 *
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			for(;;) {
				synchronized(this) {
					if(m_stop) {
						log("event manager terminates due to STOP request");
						return;
					}
					wait(POLLINTERVAL);
					if(m_stop)
						return;
				}
				scanOnce();
			}
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {
			if(!m_stop)
				log("Handler thread EXITED!?");
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Event Poster.                                    	*/
	/*--------------------------------------------------------------*/
	static private Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}

	/**
	 * Generic caller of a method using reflection. This prevents us from having
	 * to link to the stupid Oracle driver.
	 * @param src
	 * @param name
	 * @return
	 * @throws Exception
	 */
	static private Object callObjectMethod(final Object src, final String name) throws SQLException {
		try {
			Method m = src.getClass().getMethod(name, new Class[0]);
			return m.invoke(src, new Object[0]);
		} catch(InvocationTargetException itx) {
			if(itx.getCause() instanceof SQLException)
				throw (SQLException) itx.getCause();
			if(itx.getCause() instanceof RuntimeException)
				throw (RuntimeException) itx.getCause();
			throw new RuntimeException(itx.getCause());
		} catch(RuntimeException x) {
			throw x;
		} catch(Exception x) {
			throw new RuntimeException("Exception calling " + name + " on " + src + ": " + x, x);
		}
	}

	/**
	 * Primitive event poster. This adds the event to the listener queue (the database) and
	 * adds it to the "local" event queue *if* the event is an immediate event (an event whose
	 * handler will be called immediately).
	 */
	private void sendEventMain(final Connection dbc, final AppEventBase ae, final boolean commit, final boolean isimmediate) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
		OutputStream os = null;
		ObjectOutputStream oos = null;
		boolean ac = dbc.getAutoCommit(); // Do not autocommit when storing a blub
		boolean ok = false;
		try {
			if(ac)
				dbc.setAutoCommit(false);

			//-- Get a new upid
			ps = dbc.prepareStatement("select " + m_tableName + "_SQ.nextval from dual");
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("No result from select-from-sequence!?");
			long id = rs.getLong(1);
			rs.close();
			ps.close();

			//-- Update the event with it's info
			ae.setUpid(id); // Update the UPID,
			ae.setTimestamp(now());
			ae.setServer(m_serverName);

			if(isimmediate) { // Handlers will be called immediately after this?
				synchronized(this) {
					m_localEvents.add(Long.valueOf(id)); // Store this as a local event,
				}
			}

			//-- Store the record,
			ps = dbc.prepareStatement("insert into " + m_tableName + "(upid,evname,utime,server,obj) values(?,?,?,?,empty_blob())");
			ps.setLong(1, id);
			ps.setString(2, ae.getClass().getCanonicalName());
			ps.setTimestamp(3, (Timestamp) ae.getTimestamp());
			ps.setString(4, ae.getServer());
			ps.executeUpdate();
			ps.close();

			//-- Store the lob..
			ps = dbc.prepareStatement("select obj from " + m_tableName + " where upid=? for update of obj");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new IllegalStateException("Can't (re)find the record I just stored!?");
			Blob b = rs.getBlob(1);
			os = (OutputStream) callObjectMethod(b, "getBinaryOutputStream");
			oos = new ObjectOutputStream(os);
			oos.writeObject(ae);
			oos.close();
			oos = null;
			os = null;
			rs.close();
			ps.close();
			if(commit) {
				dbc.commit();
			}
			ok = true;
		} finally {
			try {
				if(oos != null)
					oos.close();
			} catch(Exception x) {}
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			try {
				if(!ok)
					dbc.rollback();
			} catch(Exception x) {}
			try {
				if(ac && commit)
					dbc.setAutoCommit(true);
			} catch(Exception x) {}
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Listener caller                                  	*/
	/*--------------------------------------------------------------*/

	private synchronized void addListener(final Class< ? > cl, final ListenerType lt, final AppEventListener< ? > listener, final boolean weak) {
		List<Item> l = m_listenerList.get(cl.getName());
		if(l == null) {
			l = new ArrayList<Item>(5);
			m_listenerList.put(cl.getName(), l);
		}

		//-- Already registered?
		for(int i = l.size(); --i >= 0;) {
			Item it = l.get(i);
			if(it.m_obj instanceof Reference< ? >) {
				Reference< ? > r = (Reference< ? >) it.m_obj;
				if(r.get() == listener) // Already registered as WEAK listener
					return;
				if(r.get() == null) {
					l.remove(i);
				}
			} else {
				if(it.m_obj == listener)
					return;
			}
		}
		if(weak)
			l.add(new Item(lt, new WeakReference<Object>(listener)));
		else
			l.add(new Item(lt, listener));
	}

	/**
	 * Remove a weak or normal listener from a map.
	 * @param cl
	 * @param listener
	 */
	public synchronized void removeListener(final Class< ? > cl, final AppEventListener< ? > listener) {
		List<Item> l = m_listenerList.get(cl.getName());
		if(l == null)
			return;
		for(int i = l.size(); --i >= 0;) {
			Item it = l.get(i);
			if(it.m_obj instanceof Reference< ? >) {
				Reference< ? > r = (Reference< ? >) it.m_obj;
				if(r.get() == listener) {
					l.remove(i);
					return;
				} else if(r.get() == null) {
					l.remove(i);
				}
			} else {
				if(it.m_obj == listener) {
					l.remove(i);
					return;
				}
			}
		}
	}

	private synchronized void getListeners(final List<AppEventListener<AppEventBase>> list, final AppEventBase ae, final boolean ateventtime, final boolean islocalevent) {
		Class< ? > cl = ae.getClass();
		for(;;) {
			List<Item> l = m_listenerList.get(cl.getName()); // List of registrations for the current type
			if(l != null) {
				for(int i = l.size(); --i >= 0;) {
					Item it = l.get(i); // Get listener desc
					if(ateventtime) {
						/*
						 * When the event fires we need all listeners that are to be delivered locally only
						 */
						if(it.m_type != ListenerType.IMMEDIATELY && it.m_type != ListenerType.LOCALLY)
							continue;
					} else {
						/*
						 * This is a call that came from the database. It needs to be sent if not locally.
						 */
						if(it.m_type == ListenerType.LOCALLY || (it.m_type == ListenerType.IMMEDIATELY && islocalevent))
							continue;
					}

					Object o = it.m_obj;
					if(o instanceof Reference< ? >) {
						Reference< ? > r = (Reference< ? >) o;
						Object lsnr = r.get();
						if(lsnr == null) {
							l.remove(i);
						} else {
							list.add((AppEventListener<AppEventBase>) l); // Found a weak listener; add.
						}
					} else {
						list.add((AppEventListener<AppEventBase>) o); // Found normal listener
					}
				}
			}

			if(cl == AppEventBase.class) // Reached topmost superclass?
				return; // Then we're done
			cl = cl.getSuperclass(); // Get my daddy and find it's handlers.
		}
	}

	/**
	 * Call all registered listeners for an event.
	 * @param ae        The event that occured
	 * @param immediate When T call all events that need to be called immediately.
	 */
	private void callListeners(final AppEventBase ae, final boolean immediate, final boolean islocalevent) {
		List<AppEventListener<AppEventBase>> list = new ArrayList<AppEventListener<AppEventBase>>();
		getListeners(list, ae, immediate, islocalevent);
		for(int i = list.size(); --i >= 0;) {
			AppEventListener<AppEventBase> l = list.get(i);
			try {
				l.handleEvent(ae);
			} catch(Exception x) {
				exception(x, "Event " + ae + " caused " + x + " in handler " + l);
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Public interface.                                	*/
	/*--------------------------------------------------------------*/

	public void addListener(final Class< ? > cl, final ListenerType lt, final AppEventListener< ? > listener) {
		addListener(cl, lt, listener, false);
	}

	public void addWeakListener(final Class< ? > cl, final ListenerType lt, final AppEventListener< ? > listener) {
		addListener(cl, lt, listener, true);
	}

	public synchronized void removeWeakListener(final Class< ? > cl, final AppEventListener< ? > listener) {
		removeListener(cl, listener);
	}

	/**
	 * Post an event synchronously. This posts the event, <b>commits it (commiting the transaction on the connection!!)</b> and calls all local handlers immediately. You
	 * may only call this version if all data pertaining to the event has been commited to the database or will
	 * be commited as a result of this call! If you do not the event may fire in other servers/listeners with stale
	 * data in the database; this will cause wrong results.
	 *
	 * @param dbc
	 * @param ae
	 * @throws Exception
	 */
	public void postEvent(final Connection dbc, final AppEventBase ae) throws Exception {
		sendEventMain(dbc, ae, true, true); // First save the thingy everywhere, ORDER IMPORTANT!!
		callListeners(ae, true, true); // Call all listeners that need the event immediately. ORDER IMPORTANT: must be after sendEvent.
	}

	/**
	 * Post an event asynchronously. The event gets added to the database but not commited, and no local listeners
	 * get called at this time. When the event gets commited the scanner will see it and call the local handlers. This
	 * call is typically done when an event needs to be commited lazily.
	 * @param dbc
	 * @param ae
	 * @throws Exception
	 */
	public void postDelayedEvent(final Connection dbc, final AppEventBase ae) throws Exception {
		sendEventMain(dbc, ae, false, false); // First save the thingy everywhere, ORDER IMPORTANT!!
	}

	/**
	 * Post a list of events asynchronously. The event gets added to the database but not commited, and no local listeners
	 * get called at this time. When the event gets commited the scanner will see it and call the local handlers. This
	 * call is typically done when an event needs to be commited lazily.
	 * @param dbc
	 * @param ae
	 * @throws Exception
	 */
	public void postDelayedEvent(final Connection dbc, final List<AppEventBase> ae) throws Exception {
		for(AppEventBase a : ae)
			sendEventMain(dbc, a, false, false); // First save the thingy everywhere, ORDER IMPORTANT!!
	}

	/**
	 * Post all of the events in the list synchronously. This posts the events, commits them, then calls all local handlers immediately. You
	 * may only call this version if all data pertaining to the events have been commited to the database or will
	 * be commited as a result of this call! If you do not the events may fire in other servers/listeners with stale
	 * data in the database; this will cause wrong results.
	 *
	 * @param dbc
	 * @param aelist
	 * @throws Exception
	 */
	public void postEvent(final Connection dbc, final List< ? extends AppEventBase> aelist) throws Exception {
		for(AppEventBase ae : aelist) {
			sendEventMain(dbc, ae, false, true); // First save the thingy everywhere, ORDER IMPORTANT!!
		}
		dbc.commit();

		//-- Call all local handlers immediately.
		for(AppEventBase ae : aelist) {
			callListeners(ae, true, true); // Call all listeners that need the event immediately. ORDER IMPORTANT: must be after sendEvent.
		}
	}
}
