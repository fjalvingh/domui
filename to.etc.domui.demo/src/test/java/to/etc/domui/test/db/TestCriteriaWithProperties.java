package to.etc.domui.test.db;

import org.junit.Assert;
import org.junit.Test;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Artist_;
import to.etc.webapp.query.QCriteria;

import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-4-18.
 */
public class TestCriteriaWithProperties extends AbstractDbTest {
	@Test
	public void testFindArtists() throws Exception {
		List<Artist> query = dc().query(QCriteria.create(Artist.class));
		System.out.println("Got " + query.size() + " results");
		Assert.assertNotEquals(0, query.size());
	}

	@Test
	public void testFindArtistsByLike() throws Exception {
		List<Artist> query = dc().query(QCriteria.create(Artist.class).like(Artist_.name(), "A%"));
		System.out.println("Got " + query.size() + " results");
		Assert.assertNotEquals(0, query.size());
	}

	@Test
	public void testFindArtistsByILike() throws Exception {
		List<Artist> query = dc().query(QCriteria.create(Artist.class).ilike(Artist_.name(), "a%"));
		System.out.println("Got " + query.size() + " results");
		Assert.assertNotEquals(0, query.size());
	}

	@Test
	public void testFindArtistsByEquals() throws Exception {
		List<Artist> query = dc().query(QCriteria.create(Artist.class).eq(Artist_.name(), "AC/DC"));
		System.out.println("Got " + query.size() + " results");
		Assert.assertNotEquals(0, query.size());
	}

	@Test
	public void testFindAlbumsByArtist() throws Exception {
		List<Album> query = dc().query(QCriteria.create(Album.class).eq(Album_.artist().name(), "AC/DC"));
		System.out.println("Got " + query.size() + " results");
		Assert.assertNotEquals(0, query.size());
	}
}
