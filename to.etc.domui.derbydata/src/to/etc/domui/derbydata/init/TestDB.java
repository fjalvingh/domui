package to.etc.domui.derbydata.init;

import to.etc.dbpool.ConnectionPool;
import to.etc.dbpool.PoolManager;
import to.etc.util.DeveloperOptions;
import to.etc.webapp.query.QContextManager;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-6-17.
 */
final public class TestDB {
    private TestDB() {
    }

    static public void initialize() throws Exception {
        String poolid = DeveloperOptions.getString("domuidemo.poolid"); // Is a poolid defined in .developer.proeprties? Then use that,
        ConnectionPool p;
        if(poolid != null) {
            //-- Local configuration. Init using local.
            System.out.println("** WARNING: Using local database configuration, pool=" + poolid);
            p = PoolManager.getInstance().initializePool(poolid);
        } else {
            p = PoolManager.getInstance().definePool(
                "demo"
                , "org.apache.derby.jdbc.EmbeddedDriver"
                , "jdbc:derby:/tmp/demoDb;create=true"
                , ""
                , ""
                , null
            );
            p.initialize();
        }
        DBInitialize.fillDatabase(p.getUnpooledDataSource());
        DbUtil.initialize(p.getPooledDataSource());

        //-- Tell the generic layer how to create default DataContext's.
        QContextManager.setImplementation(QContextManager.DEFAULT, DbUtil.getContextSource()); // Prime factory with connection source
    }

    static public void main(String[] args) {
        try {
            TestDB.initialize();
        } catch(Exception x) {
            x.printStackTrace();
        }
    }
}
