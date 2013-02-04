package to.etc.webapp.query;

import static to.etc.webapp.query.QOperation.AND;
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

	private @Nullable
	QField<R, ? > m_parent;

	protected @Nonnull
	R m_root;

	private @Nullable
	String m_propertyNameInParent;

	boolean m_isSub = false;

	public QField(@Nullable R root, @Nullable QField<R, ? > parent, @Nullable String propertyNameInParent) {
		m_parent = parent;
		if(root == null) {
			Class<T> cls = (Class<T>) ((ParameterizedType) getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[1];
			m_criteria = QCriteria.create(cls);
			m_stack = new Stack<QMultiNode>();
		}
		m_root = (R) (root == null ? this : root);
		m_propertyNameInParent = propertyNameInParent;
	}

	final @Nullable
	String getPath() {
		List<QField<R, ? >> fields = new ArrayList<QField<R, ? >>();
		QField<R, ? > parent = this;
		while(parent.m_parent != null) {
			fields.add(parent);
			parent = parent.m_parent;
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
			public QOperatorNode restrict(@Nonnull T value) {
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
			public QOperatorNode restrict(@Nonnull T value) {
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
			public QOperatorNode restrict(@Nonnull T value) {
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
		QOperatorNode node = node();
		if(node != null) {
			if(!(node instanceof QMultiNode && node.getOperation() == OR)) {
				QMultiNode or = new QMultiNode(OR);
				or.add(node);
				if(!(node instanceof QMultiNode)) {
					criteria().setRestrictions(or);
					node(or);
				}
				setOr(or);
			}
		} else {
			throw new ProgrammerErrorException("Nothing to or yet");
		}
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

		} else {
			QMultiNode or = new QMultiNode(OR);
			node = or;
			for(int i = 0; i < length; i++) {
				if(v[0] instanceof double[]) {
					or.add(irestrictor.restrict((T) new double[]{((double[]) v[0])[i]}));
				} else if(v[0] instanceof long[]) {
					or.add(irestrictor.restrict((T) new long[]{((long[]) v[0])[i]}));
				} else if(v[0] instanceof boolean[]) {
					or.add(irestrictor.restrict((T) new boolean[]{((boolean[]) v[0])[i]}));
				} else {
					or.add(irestrictor.restrict(v[i]));
				}
			}
		}

		addNode(node);
		releaseOr();
	}

	void addNode(@Nonnull QOperatorNode newNode) {
		QOperatorNode node = node();
		if(node == null) {
			node(newNode);
			if(criteria().getRestrictions() == null) {
				criteria().setRestrictions(newNode);
			}
		} else if(node instanceof QMultiNode) {
			QMultiNode multiNode = (QMultiNode) node;
			if(!isOr() && multiNode.getOperation() == OR) {
				QOperatorNode pop = pop(multiNode);
				QMultiNode and = new QMultiNode(AND);
				and.add(pop);
				and.add(newNode);
				multiNode.add(and);
			} else {
				if(newNode instanceof QMultiNode && newNode.getOperation() == OR) {
					multiNode.getChildren().addAll(((QMultiNode) newNode).getChildren());
				} else {
					multiNode.add(newNode);
				}
			}
		} else {
			QMultiNode multiNode = new QMultiNode(AND);
			multiNode.add(node);
			multiNode.add(newNode);
			node(multiNode);
			criteria().setRestrictions(multiNode);
		}
	}

	/**
	 * Brace open, can be places anywhere natural. Can also be placed unnatural, that will give error's where possible or strange results.
	 * So use with care. Place them as if you are writing an sql statement or any other conditional structure.
	 * @return
	 */
	public final @Nonnull
	R $_() {
		brace(1);
		QOperatorNode node = node();
		if(node != null && node instanceof QMultiNode) {
			stack().push((QMultiNode) node);
			node(new QMultiNode(AND));
		}
		return m_root;
	}

	/**
	 * Brace close, can be places anywhere natural. Can also be placed unnatural, that will give error's where possible or strange results.
	 * So use with care. Place them as if you are writing an sql statement or any other conditional structure.
	 * @return
	 */
	public final @Nonnull
	R _$() {
		brace(-1);
		if(stack().size() > 0) {
			QOperatorNode pop = stack().pop();
			node(pop);
		} else {
			QOperatorNode node = node();
			if(!(node instanceof QMultiNode)) {
				throw new ProgrammerErrorException("Useless bracing at level " + stack().size() + " : " + getPath());
			} else {
				QMultiNode multiNode = (QMultiNode) node;
				QMultiNode next = new QMultiNode(AND);
				next.add(multiNode);
				node(next);
				criteria().setRestrictions(next);
			}
		}
		return m_root;
	}

	private final @Nullable
	QOperatorNode pop(@Nonnull QMultiNode multiNode) {
		if(multiNode.getChildren().size() == 0) {
			return null;
		}
		return multiNode.getChildren().remove(multiNode.getChildren().size() - 1);
	}


	protected @Nonnull
	QRestrictor<T> m_criteria;



	final @Nonnull
	QRestrictor< ? > criteria() {
		return m_root.m_criteria;
	}

	protected @Nonnull
	QOperatorNode m_node;

	private final @Nonnull
	QOperatorNode node() {
		return m_root.m_node;
	}

	final void node(QOperatorNode node) {
		m_root.m_node = node;
	}

	protected @Nonnull
	QOperatorNode m_or;

	protected int m_brace = 0;

	private final void brace(int brace) {
		m_root.m_brace += brace;
		if(m_root.m_brace < 0) {
			if(brace() != 0) {
				throw new ProgrammerErrorException("No matching open brace found  : " + getPath());
			}
		}
	}

	private final int brace() {
		return m_root.m_brace;
	}


	private final @Nonnull
	QOperatorNode getOr() {
		return m_root.m_or;
	}

	private final boolean isOr() {
		return m_root.m_or != null;
	}

	private final void setOr(@Nonnull QMultiNode or) {
		m_root.m_or = m_root.m_node;
		m_root.m_node = or;
	}

	private final void releaseOr() {
		if(m_root.m_or != null) {
			m_root.m_node = m_root.m_or;
			m_root.m_or = null;
		}
	}


	protected @Nonnull
	Stack<QMultiNode> m_stack;

	private final @Nonnull
	Stack<QMultiNode> stack() {
		return m_root.m_stack;
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
		return dc.query((QCriteria<T>) m_criteria);
	}

	/**
	 * Call this on the root query, any other attempt will give a runtime exception.
	 * @param dc
	 * @return
	 * @throws Exception
	 */
	public final @Nullable
	T queryOne(@Nonnull QDataContext dc) throws Exception {
		validateGetCriteria();
		return dc.queryOne((QCriteria<T>) m_criteria);
	}

	protected final void validateGetCriteria() {
		if(m_criteria == null) {
			throw new ProgrammerErrorException("Can only call this on the root field.");
		}
		if(!(m_criteria instanceof QCriteria)) {
			throw new ProgrammerErrorException("Can only call this on the root field.");
		}
		if(m_isSub) {
			throw new ProgrammerErrorException("Cannot get criteria from subselect.");
		}
		if(stack().size() != 0) {
			throw new ProgrammerErrorException("Missing close brace at level " + stack().size() + " : " + getPath());
		}
		if(brace() != 0) {
			throw new ProgrammerErrorException("Number of open braces does not match number of closing braces (" + brace() + ") : " + getPath());
		}
	}
}