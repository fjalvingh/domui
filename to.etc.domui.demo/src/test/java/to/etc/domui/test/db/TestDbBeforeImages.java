package to.etc.domui.test.db;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.hibernate.beforeimages.QBeforeCollectionNotLoadedException;
import to.etc.domui.hibernate.generic.BuggyHibernateBaseContext;
import to.etc.util.FileTool;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;

import java.util.List;

/**
 * Tests related to getting before images for data.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 15, 2014
 */
@Ignore("The BeforeImages code is not used and has rotted")
public class TestDbBeforeImages {
	private QDataContext m_dc;

	@BeforeClass
	static public void setupClass() throws Exception {
		InitTestDB.require();
	}

	@Before
	public void setup() throws Exception {
		QDataContext dc = m_dc = InitTestDB.createContext();
		dc.setKeepOriginals();
	}

	@After
	public void teardown() {
		FileTool.closeAll(m_dc);
	}

	@NonNull
	private Album getTestableAlbum() throws Exception {
		List<Album> list = m_dc.query(QCriteria.create(Album.class).isnotnull("artist").limit(1));
		Assert.assertTrue(!list.isEmpty());
		return list.get(0);
	}

	/**
	 * Objects returned by a query must have a before image, and ManyToOne objects inside them too.
	 * @throws Exception
	 */
	@Test
	public void testSimpleLoad() throws Exception {
		List<Album> list = m_dc.query(QCriteria.create(Album.class).isnotnull("artist").limit(10));
		Assert.assertTrue(!list.isEmpty());

		BuggyHibernateBaseContext dc = (BuggyHibernateBaseContext) m_dc;

		for(Album np : list) {
			Album op = m_dc.original(np);
			Artist bc = np.getArtist();						// Get lazy object

			bc.getName();								// Force load
			Artist bi = dc.getBeforeCache().findBeforeImage(bc);
			Assert.assertNotNull("There should be a before image for a loaded lazy entity", bi);

			Assert.assertNotNull(op);
			Assert.assertNotSame(op, np);
		}
	}

	/**
	 * When before images are built for lazy-loaded ManyToOne properties creating the proxy may not
	 * cause the lazy property to be loaded (initialized).
	 * @throws Exception
	 */
	@Test
	public void testLazy() throws Exception {
		System.out.println("proxy test start");
		Album np = getTestableAlbum();
		Artist bc = np.getArtist();
		Assert.assertNotNull("We must have a parent", bc);

		Assert.assertFalse("Proxy must be unloaded", Hibernate.isInitialized(bc));
	}

	/**
	 * Lazily-loaded collections cannot be initialized either after the before image is created.
	 * @throws Exception
	 */
	@Test
	public void testCollection1() throws Exception {
		Album np = getTestableAlbum();
		Album op = m_dc.original(np);
		List<Track> addrl = np.getTrackList();
		Assert.assertFalse("Lazy collection proxy is loaded by creating a before-image", Hibernate.isInitialized(addrl));
	}

	/**
	 * Lazily-loaded collections must throw an error if their before-image counterpart is requested.
	 * @throws Exception
	 */
	@Test(expected = QBeforeCollectionNotLoadedException.class)
	public void testCollection2() throws Exception {
		Album np = getTestableAlbum();
		Album op = m_dc.original(np);
		if(null == op)
			throw new IllegalStateException("Before-image cannot be null");
		List<Track> addrl = op.getTrackList();
		int size = addrl.size();								// Access the before image of an unloaded list should throw exception
	}

	/**
	 * Lazy-loaded collections must be accessible after the original has been loaded.
	 * @throws Exception
	 */
	@Test
	public void testCollection4() throws Exception {
		Album np = getTestableAlbum();
		Album op = m_dc.original(np);
		if(null == op)
			throw new IllegalStateException("Before-image cannot be null");
		List<Track> addrl = np.getTrackList();
		Assert.assertFalse("Lazy collection proxy is loaded by creating a before-image", Hibernate.isInitialized(addrl));

		int origsz = addrl.size();
		Assert.assertTrue("Lazy collection proxy is not loaded after access", Hibernate.isInitialized(addrl));

		//-- Now access the before-image's collection.
		List<Track> copyl = op.getTrackList();
		int befsz = copyl.size();
		Assert.assertEquals(origsz, befsz);
	}


}
