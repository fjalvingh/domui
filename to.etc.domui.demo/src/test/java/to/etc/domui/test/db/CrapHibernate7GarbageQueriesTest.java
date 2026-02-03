package to.etc.domui.test.db;

import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaPath;
import org.hibernate.query.criteria.JpaRoot;
import org.junit.Test;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.hibernate.generic.HibernateLongSessionContext;

/**
 * The idiots at Hibernate have removed support for many important features in Hibernate 7,
 * which means we'll have to create workarounds for them. As the documentation is spectacularly
 * bad around JPA queries and connection management this test class creates tests to see how
 * this crap actually works.
 */
public class CrapHibernate7GarbageQueriesTest extends AbstractDbTest {
	/**
	 * see if we can query a field of a parent relation, e.g. Album.artist.name = 'AC/DC'.
	 */
	@Test
	public void queryFieldOfParentRelation() throws Exception {
		HibernateLongSessionContext dc = (HibernateLongSessionContext) dc();
		Session session = dc.getSession();
		HibernateCriteriaBuilder cb = session.getCriteriaBuilder();
		JpaCriteriaQuery<Album> query = cb.createQuery(Album.class);
		JpaRoot<Album> root = query.from(Album.class);				// Lather, rinse, repeat

		JpaPath<Object> path = root.get("artist").get("name");		// Navigate to parent relation field
		query.select(root).where(cb.equal(path, "AC/DC"));
		var result = session.createQuery(query).getResultList();
		System.out.println("Got " + result.size() + " results");
	}


}
