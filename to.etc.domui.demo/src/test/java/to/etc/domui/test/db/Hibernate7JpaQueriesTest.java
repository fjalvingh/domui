package to.etc.domui.test.db;

import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaPath;
import org.hibernate.query.criteria.JpaRoot;
import org.hibernate.query.criteria.JpaSubQuery;
import org.junit.Assert;
import org.junit.Test;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.hibernate.generic.HibernateLongSessionContext;

/**
 * The idiots at Hibernate have removed support for many important features in Hibernate 7,
 * which means we'll have to create workarounds for them. As the documentation is spectacularly
 * bad around JPA queries and connection management this test class creates tests to see how
 * this crap actually works.
 */
public class Hibernate7JpaQueriesTest extends AbstractDbTest {
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

	@Test
	public void existsQuery1() throws Exception {
		HibernateLongSessionContext dc = (HibernateLongSessionContext) dc();
		Session session = dc.getSession();
		HibernateCriteriaBuilder cb = session.getCriteriaBuilder();

		//-- Root query: we're returning ARTISTS
		JpaCriteriaQuery<Artist> query = cb.createQuery(Artist.class);
		JpaRoot<Artist> artistRoot = query.from(Artist.class);				// Lather, rinse, repeat

		//-- Subquery: albums with title "Back in Black" or "Highway to Hell"
		JpaSubQuery<Integer> subquery = query.subquery(Integer.class).select(cb.literal(1));		// Subquery selecting 1, just to check for existence
		JpaRoot<Album> subRoot = subquery.from(Album.class);
		subquery.where(
			subRoot.get("title").in("Back to Black", "Highway to Hell"),
			cb.equal(subRoot.get("artist"), artistRoot)
		);

		query.where(cb.exists(subquery));

		var result = session.createQuery(query).getResultList();
		System.out.println("Got " + result.size() + " results");
		Assert.assertEquals(1, result.size());
	}
}
