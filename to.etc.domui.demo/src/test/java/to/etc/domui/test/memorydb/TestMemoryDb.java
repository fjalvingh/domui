package to.etc.domui.test.memorydb;

import org.junit.Test;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.hibernate.memorydb.MemoryDb;
import to.etc.domui.test.db.AbstractDbTest;
import to.etc.webapp.query.QDataContext;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-09-19.
 */
public class TestMemoryDb extends AbstractDbTest {
	@Test
	public void testProxiedLoad() throws Exception {
		MemoryDb mdb = new MemoryDb(dc());
		try(QDataContext mdc = mdb.createDataContext()) {
			Artist artist = mdc.get(Artist.class, Long.valueOf(10));
		}
	}


}
