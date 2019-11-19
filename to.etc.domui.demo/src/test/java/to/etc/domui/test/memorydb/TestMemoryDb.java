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
			Track originalTrack = mdb.getOriginal(track);
			Assert.assertNotNull("There must be an original track loaded", originalTrack);
			Assert.assertTrue("The original track and the loaded one cannot be the same", track != originalTrack);

			//-- Check fields
			Assert.assertTrue("The genre field must be a proxy", MemoryDb.isMdbProxy(track.getGenre()));
			Album album = track.getAlbum();
			Assert.assertTrue("The album field must be a proxy", MemoryDb.isMdbProxy(album));
			Assert.assertTrue("The mediaType field must be a proxy", MemoryDb.isMdbProxy(track.getMediaType()));

			Album originalAlbum = mdb.getOriginal(album);
			Assert.assertNull("The original Album should not have been loaded, as we did not call any of its members", originalAlbum);

			//-- This should cause the proxy to be loaded.
			String title = album.getTitle();
			System.out.println(">> album title=" + title);

			//-- Now the original album must have loaded
			originalAlbum = mdb.getOriginal(album);
			Assert.assertNotNull("The original album must now have been loaded", originalAlbum);
			Assert.assertTrue("The original album must be different from the loaded one", originalAlbum != album);

		}
	}
}
