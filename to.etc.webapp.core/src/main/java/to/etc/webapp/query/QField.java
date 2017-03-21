package to.etc.webapp.query;

import static to.etc.webapp.query.QOperation.OR;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.webapp.*;

/**
 * Class that provides means for making type safe queries. It always start at a certain root.
 * Eg if you want to query Users mapped by User.class then look for the QUser.class. There is a
 * static method get() that will return a QUserRoot object. The QUser.class or any other QField
 * type cannot be instantiated otherwise. It makes sure you cannot query on wrong paths.
 *
 * When you want to restrict on a property of eg the User.class you can simply call the corresponding
 * method and use that in queries, formbuilders and datatables. An annotation processor, TableAnnotationProcessor,
 * has a hook in the compiler that will generate all fields for all classes that have the Entity annotation.
 * Those Entity classes can be refactored at will, any paths that break will generate compiler warnings.
 *
 * Also provides a light wrapper interface around the QCriteria api to make queries look more natural.
 * See also QFieldCompileTest and QFieldRunTest for how this works compared to the original api.
 * There is no intention to replace anything, there is full compatibility and no obligation to use one or the other.
 * The same query can be made by combining the two api's if necessary.
 *
 * The concept is that on every field restriction and operating methods can be called. Because
 * eq is the default values can be added directly in the call to the property. If more values are supplied
 * they ar or-ed. See also comment on eq,ne,or etc.
 *
 * The goal is the mirror the natural order of operators as much as possible with the possibility to brace parts of the
 * query where desired without having to restructure the query.
 *
 *
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * Created on Feb 3, 2013
 */
public class QField<R extends QField<R, ? >, T> {

	@Nullable
	private QField<R, ? > m_parent;

	@Nonnull
	protected R m_root;

	@Nullable
	private String m_propertyNameInParent;

	boolean m_isSub = false;

	@Nullable
	protected QCriteria<T> m_criteria;

	protected QBrace m_qBrace;

	public QField(@Nullable R root, @Nullable QField<R, ? > parent, @Nullable String propertyNameInParent) {
		m_parent = parent;
		if(root == null) {
			Class<T> cls = (Class<T>) ((ParameterizedType) getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[1];
			m_criteria = QCriteria.create(cls);
			m_qBrace = new QBrace(null);

		}
		m_root = (R) (root == null ? this : root);
		m_propertyNameInParent = propertyNameInParent;
	}

	@Nonnull
	final String getPath() {
		List<QField<R, ? >> fields = new ArrayList<QField<R, ? >>();
		QField<R, ? > parent = this;
		QField<R, ? > f;
		while((f = parent.m_parent) != null) {
			fields.add(parent);
			parent = f;
		}
		Collections.reverse(fields);
		StringBuilder sb = new StringBuilder();
		Iterator<QField<R, ? >> it = fields.iterator();
		while(it.hasNext()) {
			QField<R, ? > field = it.next();
			sb.append(field.m_propertyNameInParent);
			if(it.hasNext()) {
				sb.append(".");
			}
		}
		return sb.toString();
	}

	final @Nullable
	String getPropertyNameInParent() {
		return m_propertyNameInParent;
	}

	final @Nullable
	QField<R, ? > getParent() {
		return m_parent;
	}

	@Override
	public final String toString() {
		return getPath();
	}

	/**
	 * greater then
	 * @param t
	 * @return
	 */
	public final @Nonnull
	R gt(@Nonnull T... t) {
		eqOrOr(new IRestrictor<T>() {
			@Override
			public @Nonnull QOperatorNode restrict(@Nonnull T value) {
				return QRestriction.gt(getPath(), value);
			}
		}, t);
		return m_root;
	}

	/**
	 * equals
	 * @param t
	 * @return
	 */
	public final @Nonnull
	R eq(@Nonnull T... t) {
		eqOrOr(new IRestrictor<T>() {
			@Override
			public @Nonnull QOperatorNode restrict(@Nonnull T value) {
				return QRestriction.eq(getPath(), value);
			}
		}, t);
		return m_root;
	}

	/**
	 * not equals
	 * @param t
	 * @return
	 */
	public final @Nonnull
	R ne(@Nonnull T... t) {
		eqOrOr(new IRestrictor<T>() {
			@Override
			public @Nonnull QOperatorNode restrict(@Nonnull T value) {
				return QRestriction.ne(getPath(), value);
			}
		}, t);
		return m_root;
	}

	/**
	 * or's the next restriction to this one as opposed to the original api where
	 * you call or before any thing that has to be or-ed. There is no and every restriction leads
	 * back to the root. Any next direct call to a retsriction will be and-ed.
	 * @return
	 */
	public final @Nonnull
	R or() {
		qbrace().add(OR);
		return m_root;
	}

	/**
	 * One value will have it's natural way. More values will be or-ed together and added to the current level.
	 * @param irestrictor
	 * @param v
	 */
	protected final void eqOrOr(@Nonnull IRestrictor<T> irestrictor, @Nonnull T... v) {

		int length = v.length;

		if(v[0] instanceof double[]) {
			length = ((double[]) v[0]).length;
		} else if(v[0] instanceof long[]) {
			length = ((long[]) v[0]).length;
		} else if(v[0] instanceof boolean[]) {
			length = ((boolean[]) v[0]).length;
		}

		QOperatorNode node;

		if(length == 1) {

			if(v[0] instanceof double[]) {
				node = irestrictor.restrict((T) new double[]{((double[]) v[0])[0]});
			} else if(v[0] instanceof long[]) {
				node = irestrictor.restrict((T) new long[]{((long[]) v[0])[0]});
			} else if(v[0] instanceof boolean[]) {
				node = irestrictor.restrict((T) new boolean[]{((boolean[]) v[0])[0]});
			} else {
				node = irestrictor.restrict(v[0]);
			}
			qbrace().add(node);

		} else {
			for(int i = 0; i < length; i++) {
				QOperatorNode restrict;
				if(v[0] instanceof double[]) {
					restrict = irestrictor.restrict((T) new double[]{((double[]) v[0])[i]});
				} else if(v[0] instanceof long[]) {
					restrict = irestrictor.restrict((T) new long[]{((long[]) v[0])[i]});
				} else if(v[0] instanceof boolean[]) {
					restrict = irestrictor.restrict((T) new boolean[]{((boolean[]) v[0])[i]});
				} else {
					restrict = irestrictor.restrict(v[i]);
				}
				qbrace().add(restrict);
				if(i < length - 1) {
					qbrace().add(OR);
				}
			}
		}
	}


	/**
	 * Brace open, can be places anywhere natural. Can also be placed unnatural, that will give error's where possible or strange results.
	 * So use with care. Place them as if you are writing an sql statement or any other conditional structure.
	 * @return
	 */
	public final @Nonnull
	R $_() {
		QBrace child = new QBrace(qbrace());
		qbrace().add(child);
		qbrace(child);
		return m_root;
	}

	/**
	 * Brace close, can be places anywhere natural. Can also be placed unnatural, that will give error's where possible or strange results.
	 * So use with care. Place them as if you are writing an sql statement or any other conditional structure.
	 * @return
	 * @throws Exception
	 */
	public final @Nonnull
	R _$() throws Exception {
		QBrace parent = qbrace().getParent();
		if(parent == null) {
			throw new Exception("Trying to close a brace that is not opened.");
		}
		qbrace(parent);
		return m_root;
	}

	@Nonnull
	final QBrace qbrace() {
		return m_root.m_qBrace;
	}

	final void qbrace(@Nonnull QBrace brace) {
		m_root.m_qBrace = brace;
	}


	/**
	 * Call this on the root query, any other attempt will give a runtime exception.
	 * @param dc
	 * @return
	 * @throws Exception
	 */
	public final @Nonnull
	List<T> query(@Nonnull QDataContext dc) throws Exception {
		validateGetCriteria();
		QCriteria<T> criteria = m_criteria;
		if(criteria == null) {
			throw new ProgrammerErrorException("Can only call this on the root field.");
		}
		return dc.query(criteria);
	}

	/**
	 * Call this on the root query, any other attempt will give a runtime exception.
	 * @param dc
	 * @return
	 * @throws Exception
	 */
	@Nullable
	public final T queryOne(@Nonnull QDataContext dc) throws Exception {
		validateGetCriteria();
		QCriteria<T> criteria = m_criteria;
		if(criteria == null) {
			throw new ProgrammerErrorException("Can only call this on the root field.");
		}
		return dc.queryOne(criteria);
	}

	protected final void validateGetCriteria() throws Exception {
		QCriteria<T> criteria = m_criteria;
		if(criteria == null) {
			throw new ProgrammerErrorException("Can only call this on the root field.");
		}
		if(!(criteria instanceof QCriteria)) {
			throw new ProgrammerErrorException("Can only call this on the root field.");
		}
		if(m_isSub) {
			throw new ProgrammerErrorException("Cannot get criteria from subselect.");
		}
		if(criteria.getRestrictions() == null) {
			//System.out.println("qbrace:" + qbrace().toString());
			criteria.setRestrictions(qbrace().toQOperatorNode());
		}
	}

	@Nonnull
	final public QCriteria< ? > criteria() {
		QCriteria< ? > criteria = m_root.m_criteria;
		if(criteria == null) {
			throw new ProgrammerErrorException("Null criteria in root");
		}
		return criteria;
	}
}