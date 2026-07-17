package to.etc.domui.test.db;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import to.etc.domui.derbydata.db.Artist;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * Tests for JDBC connection handling through QDataContext / Hibernate Session.
 */
public class TestDbConnections {
	private QDataContext m_dc;

	@BeforeClass
	static public void setUp() throws Exception {
		InitTestDB.require();
	}

	@Before
	public void setUpConnection() throws Exception {
		m_dc = InitTestDB.createContext();
	}

	@After
	public void tearDownConnection() throws Exception {
		if(m_dc != null) {
			m_dc.close();
			m_dc = null;
		}
	}

	public QDataContext dc() {
		return m_dc;
	}

	/**
	 * Test that getConnection() returns a usable JDBC connection.
	 */
	@Test
	public void testGetConnection() throws Exception {
		Connection conn = dc().getConnection();
		Assert.assertNotNull("getConnection() should return a non-null connection", conn);
		Assert.assertFalse("Connection should not be closed", conn.isClosed());
	}

	/**
	 * Test that the JDBC connection can execute a query and return results.
	 */
	@Test
	public void testConnectionCanExecuteQuery() throws Exception {
		Connection conn = dc().getConnection();
		try(PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Artist")) {
			try(ResultSet rs = ps.executeQuery()) {
				Assert.assertTrue("Should have a result row", rs.next());
				int count = rs.getInt(1);
				Assert.assertTrue("Should have artists in the database", count > 0);
			}
		}
	}

	/**
	 * Test that the JDBC connection and QDataContext share the same transaction,
	 * i.e. data visible through one is visible through the other.
	 */
	@Test
	public void testConnectionSharesTransaction() throws Exception {
		//-- Get the count via QCriteria
		List<Artist> artists = dc().query(QCriteria.create(Artist.class));
		int criteriaCount = artists.size();

		//-- Get the count via JDBC on the same connection
		Connection conn = dc().getConnection();
		int jdbcCount;
		try(PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Artist")) {
			try(ResultSet rs = ps.executeQuery()) {
				Assert.assertTrue(rs.next());
				jdbcCount = rs.getInt(1);
			}
		}

		Assert.assertEquals("JDBC and QCriteria should see the same data", criteriaCount, jdbcCount);
	}
}
