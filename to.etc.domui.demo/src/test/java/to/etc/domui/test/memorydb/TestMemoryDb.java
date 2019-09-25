package to.etc.domui.test.memorydb;

import org.junit.Assert;
import org.junit.Test;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.hibernate.memorydb.MemoryDb;
import to.etc.domui.test.db.AbstractDbTest;
import to.etc.webapp.query.QDataContext;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-09-19.
 */
public class TestMemoryDb extends AbstractDbTest {
	@Test
	public void testParentAreProxies() throws Exception {
		MemoryDb mdb = new MemoryDb(dc());
		try(QDataContext mdc = mdb.createDataContext()) {
			Track track = mdc.get(Track.class, Long.valueOf(10));
			System.out.println("track = " + track);
			Assert.assertTrue("The genre field must be a proxy", MemoryDb.isMdbProxy(track.getGenre()));
			Album album = track.getAlbum();
			Assert.assertTrue("The album field must be a proxy", MemoryDb.isMdbProxy(album));
			Assert.assertTrue("The mediaType field must be a proxy", MemoryDb.isMdbProxy(track.getMediaType()));

			Album originalAlbum = mdb.getOriginal(album);
			Assert.assertTrue("The album and the original album cannot be the same", album != originalAlbum);
		}
	}


}
