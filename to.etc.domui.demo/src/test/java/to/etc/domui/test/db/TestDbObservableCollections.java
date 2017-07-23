package to.etc.domui.test.db;

import java.util.*;

import org.junit.*;

import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.hibernate.types.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

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
