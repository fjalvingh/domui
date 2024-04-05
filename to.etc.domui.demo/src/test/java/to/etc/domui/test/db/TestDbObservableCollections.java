package to.etc.domui.test.db;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.hibernate.types.PersistentObservableList;
import to.etc.util.FileTool;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;

import java.util.List;

@Ignore("The BeforeImages code is not used and has rotted")
public class TestDbObservableCollections {
	@BeforeClass
	static public void beforeClass() throws Exception {
		InitTestDB.require();
	}

	@Test
	public void testPdaCollection() throws Exception {
		QDataContext dc = InitTestDB.createContext();
		try {
			List<Artist> res = dc.query(QCriteria.create(Artist.class));
			for(Artist artist : res) {
				List<Album> list = artist.getAlbumList();
				Assert.assertTrue(list instanceof PersistentObservableList);
				list.size();
			}
		} finally {
			FileTool.closeAll(dc);
		}
	}
}
