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

import java.lang.ref.*;
import java.net.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.*;

import javax.annotation.*;
import javax.sql.*;

import org.slf4j.*;

import to.etc.util.*;
import to.etc.webapp.testsupport.*;

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
 *      OBJ         VARCHAR(4000)
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
	}

	@Nullable
	static private VpEventManager m_instance;

	/** If initialized in test mode this contains the per-thread instances of this singleton. */
	@Nullable
	static private ThreadLocal<VpEventManager> m_testInstances;

	@Nonnull
	private DataSource m_ds;

	@Nonnull
	private String m_tableName;

	@Nonnull
	private IEventMarshaller m_eventMarshaller;

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
	}

	private DbType m_dbtype;

	private long m_lastHandled;

	/*--------------------------------------------------------------*/
	/*	CODING:	Singleton init.                                  	*/
	/*--------------------------------------------------------------*/

	private VpEventManager(@Nonnull final DataSource ds, @Nonnull final String tableName, @Nonnull final IEventMarshaller eventMarshaller) {
		m_ds = ds;
		m_tableName = tableName;
		m_eventMarshaller = eventMarshaller;
		try {
			m_serverName = InetAddress.getLocalHost().getCanonicalHostName();
		} catch(Exception x) {
			m_serverName = "unknown";
		}
	}

	/**
	 * Get the instance.
	 *
	 * @return
	 */
	static synchronized public VpEventManager getInstance() {
		ThreadLocal<VpEventManager> tl = m_testInstances;
		VpEventManager em;
		if(null != tl) {
			em = tl.get();
			if(null == em) {
				em = initDummyEventManagerForTest();
				tl.set(em);
			}
		} else {
			em = m_instance;
		}
		if(null == em)
			throw new IllegalStateException("The VpEventManager has not been initialized");
		return em;
	}

	private static VpEventManager initDummyEventManagerForTest() {
		IEventMarshaller dummyEM = new IEventMarshaller() {
			@Override
			public <T extends AppEventBase> T unmarshalEvent(String varchar) throws Exception {
				return null;
			}

			@Override
			public String marshalEvent(AppEventBase event) throws Exception {
				return "";
			}
		};
		return new VpEventManager(new TestDataSourceStub(), "sys_vp_events", dummyEM);
	}

	/**
	 * Initialize for production mode.
	 * @param ds
	 * @param tableName
	 * @param eventMarshaller
	 * @throws Exception
	 */
	static public synchronized void initialize(final DataSource ds, final String tableName, @Nonnull final IEventMarshaller eventMarshaller) throws Exception {
		ThreadLocal<VpEventManager> tl = m_testInstances;
		if(null != tl)
			throw new IllegalStateException("The VpEventManager has already been initialized for TEST mode");
		if(m_instance != null)
			return;

		VpEventManager em = new VpEventManager(ds, tableName, eventMarshaller);
		em.init();
		m_instance = em;
	}

	static public synchronized void initializeForTest() {
		if(m_instance != null)
			throw new IllegalStateException("The VpEventManager has already been initialized for PRODUCTION mode");
		ThreadLocal<VpEventManager> tl = m_testInstances;
		if(null == tl) {
			m_testInstances = tl = new ThreadLocal<VpEventManager>();
		}
	}

	static public synchronized boolean inJUnitTestMode() {
		return m_testInstances != null;
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
		Thread ht;
		synchronized(this) {
			ht = m_handlerThread;
			if(ht == null)
				return;
			m_stop = true;
			notifyAll();
		}
		try {
			ht.join(10000);
		} catch(InterruptedException x) {}
		if(ht.isAlive())
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
					tbl = "create table " + m_tableName
						+ "( upid numeric(20,0) not null primary key, utime date not null, evname varchar(80) not null, server varchar(32) not null, obj varchar2(4000 char))";
					seq = "create sequence " + m_tableName + "_SQ start with 1 increment by 1";
					break;

				case POSTGRES:
					tbl = "create table " + m_tableName
						+ "( upid numeric(20,0) not null primary key, utime date not null, evname varchar(80) not null, server varchar(32) not null, obj varchar(4000))";
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
	private synchronized void init() throws Exception {
		Connection dbc = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			dbc = m_ds.getConnection();
			createTable(dbc); // Make sure a database table exists

			//-- Get the last update #
			ps = dbc.prepareStatement("select max(upid) from " + m_tableName);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new IllegalStateException("?? Cannot get max update number");
			m_upid = rs.getLong(1);
			m_delete_upid = 0;
			m_ts_nextdelete = System.currentTimeMillis() + DELETEINTERVAL;
			checkPendingDeletes(dbc);
		} finally {
			FileTool.closeAll(rs, ps, dbc);
		}
	}

	/**
	 * Must be called after init to actually start handling events.
	 */
	public synchronized void start() {
		if(!DeveloperOptions.getBool("domui.eventmanager", true) || DeveloperOptions.isBackGroundDisabled())
			return;
		if(inJUnitTestMode())
			return;

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
			if(m_delete_upid >= m_upid)			// Do nothing if nothing happened.
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
			String sql = "delete from " + m_tableName + " where upid < ? or utime < ?";
			ps = dbc.prepareStatement(sql);
			ps.setLong(1, deleteupid);
			Date offsetDate = new Date(System.currentTimeMillis() - 10 * 60 * 1000);
			ps.setDate(2, offsetDate);			// Everything older than this
			LOG.debug(sql + " | " + deleteupid + ", " + offsetDate);
			ps.executeUpdate();
			dbc.commit();
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
		long upid;
		synchronized(this) {
			upid = m_upid;
		}
		try {
			String sql = "select upid,evname,utime,server,obj from " + m_tableName + " where upid > ? order by upid";
			LOG.debug(sql);
			ps = dbc.prepareStatement(sql);
			ps.setLong(1, upid);
			rs = ps.executeQuery();
			while(rs.next()) {
				readEventObject(rs, al);
			}
			if(al.size() > 0) {
//				StringBuilder sb = new StringBuilder();
//				sb.append("EV: read ");
//				for(AppEventBase ae : al) {
//					sb.append(ae.getUpid()).append("/");
//				}
//				System.out.println(sb.toString());

				//-- Remove all saved "locally generated" events up to the event we've just read,
				synchronized(this) {
					Iterator<Long> it = m_localEvents.iterator();
					while(it.hasNext()) {
						Long v = it.next();
						if(v.longValue() <= upid) {
							it.remove();
							localeventset.add(v);
						} else
							break;
					}
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
		String objectString = rs.getString(5);

		//-- Unserialize
		try {
			AppEventBase act = m_eventMarshaller.unmarshalEvent(objectString);
			if(act == null) {
				log("Event " + upid + " skipped: the embedded object is null");
				return;
			}


			//-- Update the AppEvent with the data read (should not be necessary)
			AppEventBase e = act;
			e.setServer(server);
			e.setTimestamp(ts);
			e.setUpid(upid);
			al.add(e);
		} catch(Exception x) {
			log("Event " + upid + ": serialization got exception " + x);
			//			x.printStackTrace();
		}
	}

	private void handleEvents(final List<AppEventBase> list, final Set<Long> localeventset) {
		for(int i = 0; i < list.size(); i++) {
			AppEventBase ae = list.get(i);
//			System.out.println("EV: Handle event " + ae.getUpid() + ", " + ae.getClass().getName());
			callListeners(ae, false, localeventset.contains(Long.valueOf(ae.getUpid()))); // Call all handlers that need delayed notification
			synchronized(this) {
				m_lastHandled = ae.getUpid();
				notifyAll();
			}
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
	@Override
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
			boolean warn = false;
			synchronized(this) {
				m_handlerThread = null;
				if(!m_stop)
					warn = true;
			}
			if(warn)
				log("Handler thread EXITED!?");
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Event Poster.                                    	*/
	/*--------------------------------------------------------------*/
	@Nonnull
	static private Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}

	/**
	 * NOT FOR COMMON USE - Primitive event poster. This adds the event to the listener queue (the database) and
	 * adds it to the "local" event queue *if* the event is an immediate event (an event whose
	 * handler will be called immediately).
	 */
	public long sendEventMain(@Nonnull final Connection dbc, @Nonnull final AppEventBase ae, final boolean commit, final boolean isimmediate) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
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
			ps = dbc.prepareStatement("insert into " + m_tableName + "(upid,evname,utime,server,obj) values(?,?,?,?,?)");
			ps.setLong(1, id);
			ps.setString(2, ae.getClass().getCanonicalName());
			ps.setTimestamp(3, (Timestamp) ae.getTimestamp());
			ps.setString(4, ae.getServer());
			ps.setString(5, m_eventMarshaller.marshalEvent(ae));
			ps.executeUpdate();
			ps.close();

			rs.close();
			ps.close();
			if(commit) {
				dbc.commit();
			}
			ok = true;
			return id;
		} finally {
			try {
				if(!ok)
					dbc.rollback();
			} catch(Exception x) {}
			try {
				if(ac && commit)
					dbc.setAutoCommit(true);
			} catch(Exception x) {}
			FileTool.closeAll(rs, ps);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Listener caller                                  	*/
	/*--------------------------------------------------------------*/

	private synchronized void addListener(@Nonnull final Class< ? > cl, @Nonnull final ListenerType lt, @Nonnull final AppEventListener< ? > listener, final boolean weak) {
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
	public synchronized void removeListener(@Nonnull final Class< ? > cl, @Nonnull final AppEventListener< ? > listener) {
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

	private synchronized void getListeners(@Nonnull final List<AppEventListener<AppEventBase>> list, @Nonnull final AppEventBase ae, final boolean ateventtime, final boolean islocalevent) {
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
	private void callListeners(@Nonnull final AppEventBase ae, final boolean immediate, final boolean islocalevent) {
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

	public <T extends AppEventBase> void addListener(@Nonnull final Class<T> cl, @Nonnull final ListenerType lt, @Nonnull final AppEventListener<T> listener) {
		addListener(cl, lt, listener, false);
	}

	public <T extends AppEventBase> void addWeakListener(@Nonnull final Class<T> cl, @Nonnull final ListenerType lt, @Nonnull final AppEventListener<T> listener) {
		addListener(cl, lt, listener, true);
	}

	public <T extends AppEventBase> void removeWeakListener(@Nonnull final Class<T> cl, @Nonnull final AppEventListener<T> listener) {
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
	public void postEvent(@Nonnull final Connection dbc, @Nonnull final AppEventBase ae) throws Exception {
		if(!inJUnitTestMode())
			sendEventMain(dbc, ae, true, true); // First save the thingy everywhere, ORDER IMPORTANT!!
		callListeners(ae, true, true); // Call all listeners that need the event immediately. ORDER IMPORTANT: must be after sendEvent.
	}

	/**
	 * Post an event asynchronously. The event gets added to the database but not commited, and no local listeners
	 * get called at this time. When the event gets commited the scanner will see it and call the local handlers. This
	 * call is typically done when an event needs to be commited lazily.
	 *
	 * @param dbc
	 * @param ae
	 * @throws Exception
	 */
	public void postDelayedEvent(@Nonnull final Connection dbc, @Nonnull final AppEventBase ae) throws Exception {
		if(!inJUnitTestMode())
			sendEventMain(dbc, ae, false, false); // First save the thingy everywhere, ORDER IMPORTANT!!
		else
			callListeners(ae, true, true);
		/*
		 * jal 20120911 Just sending the event to the db is not enough. The idea is to delay the events until the time that
		 * the underlying transaction is commited. But at that time "local" events will not be called in time (if at all):
		 * - if the underlying transaction takes time, then we might "skip" events (fixed elsewhere)
		 * - actually the local code might expect that all side effects of the commit take place after that commit. So
		 *   local events should fire at that time.
		 */
	}

	/**
	 * Post a list of events asynchronously. The event gets added to the database but not committed, and no local listeners
	 * get called at this time. When the event gets commited the scanner will see it and call the local handlers. This
	 * call is typically done when an event needs to be commited lazily.
	 * @param dbc
	 * @param ae
	 * @throws Exception
	 */
	public void postDelayedEvent(@Nonnull final Connection dbc, @Nonnull final List< ? extends AppEventBase> ae) throws Exception {
		for(AppEventBase a : ae) {
			if(inJUnitTestMode()) {
				callListeners(a, true, true); 			// Call all listeners that need the event immediately. ORDER IMPORTANT: must be after sendEvent.
			} else {
				sendEventMain(dbc, a, false, false);	// First save the thingy everywhere, ORDER IMPORTANT!!
			}
		}
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
	public void postEvent(@Nonnull final Connection dbc, @Nonnull final List< ? extends AppEventBase> aelist) throws Exception {
		if(!inJUnitTestMode()) {
			for(AppEventBase ae : aelist) {
				sendEventMain(dbc, ae, false, true); // First save the thingy everywhere, ORDER IMPORTANT!!
			}
		}
		dbc.commit();

		//-- Call all local handlers immediately.
		for(AppEventBase ae : aelist) {
			callListeners(ae, true, true); // Call all listeners that need the event immediately. ORDER IMPORTANT: must be after sendEvent.
		}
	}

	private synchronized long getLastHandled() {
		return m_lastHandled;
	}

	/**
	 * Sleep until the specified event has been handled. This waits for max. one minute.
	 * @param value
	 */
	public void waitUntilHandled(long value) throws Exception {
		if(getLastHandled() >= value)
			return;
		long ets = System.currentTimeMillis() + 60 * 1000;
		for(;;) {
			synchronized(this) {
				if(m_lastHandled >= value)
					return;
				wait(10000);
			}
			if(System.currentTimeMillis() >= ets)
				throw new TimeoutException();
		}
	}
}
