package to.etc.domui.derbydata.init;

import java.io.*;

import javax.sql.*;

import org.hibernate.*;

import to.etc.dbpool.*;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Employee;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.derbydata.db.InvoiceLine;
import to.etc.domui.derbydata.db.MediaType;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.dom.html.*;
import to.etc.domui.hibernate.config.*;
import to.etc.domui.hibernate.generic.*;
import to.etc.webapp.query.*;

public class DbUtil {
	/**
	 * Main worker to initialize the database layer, using Hibernate, with a user-specified core data source. This
	 * code also enables SQL logging when .developer.properties option hibernate.sql=true.
	 * @param ds
	 * @throws Exception
	 */
    public static void  initialize(final DataSource ds) throws Exception {
		HibernateConfigurator.addClasses(Artist.class, Album.class, MediaType.class, Track.class);
		HibernateConfigurator.addClasses(Genre.class, Customer.class, Employee.class, Invoice.class, InvoiceLine.class);

		HibernateConfigurator.enableBeforeImages(true);
		HibernateConfigurator.enableObservableCollections(true);

		HibernateConfigurator.initialize(ds);
    }

	/**
	 * Alternate entrypoint: initialize the layer using a poolID in the default poolfile.
	 * @param poolname
	 * @throws Exception
	 */
    public static void  _initialize(final String poolname) throws Exception {
        ConnectionPool  p = PoolManager.getInstance().definePool(poolname);
        initialize(p.getPooledDataSource());
    }

	/**
	 * Initialize the layer using a poolid in the specified poolfile.
	 * @param poolfile
	 * @param poolname
	 * @throws Exception
	 */
    public static void  initialize(final File poolfile, final String poolname) throws Exception {
        ConnectionPool  p = PoolManager.getInstance().definePool(poolfile, poolname);
        initialize(p.getPooledDataSource());
    }

    public static Session	internalGetSession(final QDataContext dc) throws Exception {
		return ((BuggyHibernateBaseContext) dc).getSession();
    }

	static public QDataContextFactory getContextSource() {
		return HibernateConfigurator.getDataContextFactory();
    }

    public static SessionFactory getDbContextFactory() {
		return HibernateConfigurator.getSessionFactory();
    }

	public static QDataContext getContext(final Page pg) throws Exception {
		return QContextManager.getContext(QContextManager.DEFAULT, pg);
    }
}
