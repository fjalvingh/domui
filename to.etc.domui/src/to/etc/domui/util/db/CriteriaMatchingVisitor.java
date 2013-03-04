package to.etc.domui.util.db;

import java.math.*;
import java.util.*;
import java.util.regex.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.util.*;
import to.etc.webapp.qsql.*;
import to.etc.webapp.query.*;


/**
 * This visitor checks if a class instance obeys the criteria (matches the criteria).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 4, 2013
 */
public class CriteriaMatchingVisitor<T> extends QNodeVisitorBase {
	@Nonnull
	private T		m_instance;

	@Nonnull
	private ClassMetaModel m_cmm;

	private boolean m_lastResult;

	public CriteriaMatchingVisitor(@Nonnull T instance, @Nonnull ClassMetaModel cmm) {
		m_instance = instance;
		m_cmm = cmm;
	}

	public boolean isMatching() {
		return m_lastResult;
	}

	public void setInstance(@Nonnull T instance) {
		m_instance = instance;
		m_lastResult = true;
	}

	@Override
	public void visitPropertyComparison(@Nonnull QPropertyComparison n) throws Exception {
		QOperatorNode rhs = n.getExpr();
		String name = n.getProperty();
		QLiteral l = null;
		if(rhs.getOperation() == QOperation.LITERAL) {
			l = (QLiteral) rhs;
//		} else if(rhs.getOperation() == QOperation.SELECTION_SUBQUERY) {
//			handlePropertySubcriteriaComparison(n);
//			return;
		} else
			throw new IllegalStateException("Unknown operands to " + n.getOperation() + ": " + name + " and " + rhs.getOperation());

		//-- If prop refers to some relation (dotted pair):
		m_lastResult = false;
		PropertyMetaModel< ? > pmm = parseSubCriteria(name);
		Object lit = l.getValue();							// Get the literal we're comparing to
		Object val = pmm.getValue(m_instance);				// And the actual value inside the instance
		if(lit == null)										// Any compare with null will be false, always
			return;
		if(val == null)
			return;

		//-- We need to do integral promotions on the type if they differ.
		Class< ? > litc = lit.getClass();
		Class< ? > valc = val.getClass();					// Types differ?
		if(litc != valc) {
			Class< ? > endtype = getPromoted(litc, valc);	// If classes differ get a promoted thing.
			if(null == endtype)
				throw new QQuerySyntaxException("Cannot compare property " + n.getProperty() + " of type " + valc + " with a " + litc);

			if(endtype != litc)
				lit = RuntimeConversions.convertTo(lit, endtype);
			if(endtype != valc)
				val = RuntimeConversions.convertTo(val, endtype);
		}

		//-- Equal and not equal are handled by object-native equals, like and ilike are handled specially.
		switch(n.getOperation()){
			default:
				break;

			case LIKE:
				m_lastResult = compareLike(val.toString(), lit.toString());
				return;

			case ILIKE:
				m_lastResult = compareLike(val.toString().toLowerCase(), lit.toString().toLowerCase());
				return;

			case EQ:
				m_lastResult = val.equals(lit);
				return;

			case NE:
				m_lastResult = ! val.equals(lit);
				return;
		}

		//-- For the rest the values must implement Comparable
		if(! (val instanceof Comparable<?>))
			throw new QQuerySyntaxException("Cannot compare (" + n.getOperation() + ") property " + n.getProperty() + " of type " + valc + ": the class " + val.getClass().getName()
				+ " does not implement Comparable");

		int res = ((Comparable<Object>) val).compareTo(lit);
		switch(n.getOperation()){
			default:
				throw new IllegalStateException("Unexpected operation: " + n.getOperation());
			case GT:
				m_lastResult = res > 0;
				break;
			case GE:
				m_lastResult = res >= 0;
			case LT:
				m_lastResult = res < 0;
				break;
			case LE:
				m_lastResult = res <= 0;
				break;
		}
	}

	@Nullable
	private Map<String, Matcher> m_likeMatcher;

	private boolean compareLike(@Nonnull String val, @Nonnull String match) {
		Map<String, Matcher> map = m_likeMatcher;
		if(null == map)
			map = m_likeMatcher = new HashMap<String, Matcher>();
		Matcher m = map.get(match);
		if(null == m) {
			//-- Convert to regexp
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < match.length(); i++) {
				char c = match.charAt(i);
				switch(c){
					default:
						sb.append(c);
						break;
					case '.':
					case '*':
					case '+':
					case '\\':
					case '?':
					case '(':
					case ')':
					case '[':
					case ']':
					case '|':
					case '-':
						sb.append("\\");				// Escape
						sb.append(c);
						break;

					case '%':
						sb.append(".*");				// Replace % with .* meta
						break;
				}

			}
			m = Pattern.compile(sb.toString()).matcher("");		// What incredible idiot made this matcher() function!?
			map.put(match, m);
		}
		return m.reset(val).matches();
	}

	/**
	 * Try to create a common base class for two values to compare.
	 * @param litc
	 * @param valc
	 * @return
	 */
	@Nullable
	private Class< ? > getPromoted(@Nonnull Class< ? > litc, @Nonnull Class< ? > valc) {
		if(litc == valc)
			return litc;

		int lito = getPromoOrder(litc);
		int valo = getPromoOrder(valc);
		if(lito == -1 || valo == -1)
			return null;
		return lito > valo ? litc : valc;
	}

	static private final Class<?>[] ORDERS = {	//
		Byte.class, byte.class,					//
		Short.class, short.class,				//
		Character.class, char.class,			//
		Integer.class, int.class,				//
		Long.class, long.class,					//
		Float.class, float.class,				//
		Double.class, double.class,				//
		BigInteger.class, null,					//
		BigDecimal.class, null,					//
		String.class, null						//
	};

	private int getPromoOrder(Class< ? > litc) {
		for(int i = 0; i < ORDERS.length; i++) {
			if(ORDERS[i] == litc)
				return i / 2;
		}
		return -1;
	}

	@Override
	public void visitUnaryNode(@Nonnull QUnaryNode n) throws Exception {
		switch(n.getOperation()){
			default:
				break;
			case NOT:
				n.getNode().visit(this);
				m_lastResult = !m_lastResult;
				return;
		}
		throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
	}

	@Override
	public void visitUnaryProperty(@Nonnull QUnaryProperty n) throws Exception {
		String name = n.getProperty();
		PropertyMetaModel< ? > pmm = parseSubCriteria(n.getProperty());

		Object val = pmm.getValue(m_instance);
		switch(n.getOperation()){
			default:
				throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());

			case ISNOTNULL:
				m_lastResult = val != null;
				break;
			case ISNULL:
				m_lastResult = val == null;
				break;
		}
	}

	@Nonnull
	private PropertyMetaModel< ? > parseSubCriteria(@Nonnull String property) {
		PropertyMetaModel< ? > pmm = m_cmm.getProperty(property);
		if(pmm instanceof PathPropertyMetaModel) {
			PathPropertyMetaModel< ? > m = (PathPropertyMetaModel< ? >) pmm;
			for(PropertyMetaModel< ? > pm : m.getAccessPath()) {
				if(pm.getRelationType() == PropertyRelationType.DOWN)
					throw new QQuerySyntaxException(property + ": contains reference to a child property - use an exists subquery instead.");
			}
		}
		return pmm;
	}


	@Override
	public void visitMulti(@Nonnull QMultiNode inn) throws Exception {
		//-- Walk all members, create nodes from 'm.
		boolean result = inn.getOperation() == QOperation.AND;		// For AND we start RESULT with TRUE, OR starts with FALSE.
		for(QOperatorNode n : inn.getChildren()) {
			n.visit(this);

			switch(inn.getOperation()){
				default:
					throw new IllegalStateException("Unexpected operation: " + inn.getOperation());

				case AND:
					result = result && m_lastResult;				// And
					if(!result) {
						m_lastResult = false;						// Short-circuit
						return;
					}
					break;

				case OR:
					result = result || m_lastResult;				// Or
					if(result) {
						m_lastResult = true;						// Short-circuit
						return;
					}
					break;
			}
		}
		m_lastResult = result;
	}


	@Override
	public void visitBetween(@Nonnull QBetweenNode n) throws Exception {
		if(n.getA().getOperation() != QOperation.LITERAL || n.getB().getOperation() != QOperation.LITERAL)
			throw new IllegalStateException("Expecting literals as 2nd and 3rd between parameter");
		QLiteral lita = (QLiteral) n.getA();
		QLiteral litb = (QLiteral) n.getB();
		String name = n.getProp();

		//-- If prop refers to some relation (dotted pair):
		m_lastResult = false;
		PropertyMetaModel< ? > pmm = parseSubCriteria(n.getProp());
		Object val = pmm.getValue(m_instance);				// And the actual value inside the instance
		if(val == null)
			return;

		Object av = lita.getValue();
		Object bv = litb.getValue();
		if(av == null)
			throw new QQuerySyntaxException("Second 'between' parameter is null in " + n);
		if(bv == null)
			throw new QQuerySyntaxException("Third 'between' parameter is null in " + n);

		//-- Promote to a common base.
		Class< ? > valc = val.getClass();
		Class< ? > ac = av.getClass();
		Class< ? > bc = bv.getClass();
		if(ac != valc || bc != valc) {
			Class< ? > endtype = getPromoted(valc, ac);
			if(endtype == null)
				throw new QQuerySyntaxException("Between of property " + n.getProp() + " of type " + valc + " with values " + ac + " and " + bc + " is not valid");
			endtype = getPromoted(endtype, bc);
			if(endtype == null)
				throw new QQuerySyntaxException("Between of property " + n.getProp() + " of type " + valc + " with values " + ac + " and " + bc + " is not valid");

			if(endtype != valc)
				val = RuntimeConversions.convertTo(val, endtype);
			if(endtype != ac)
				av = RuntimeConversions.convertTo(av, endtype);
			if(endtype != bc)
				bv = RuntimeConversions.convertTo(bv, endtype);
		}

		//-- Stuff now must implement Comparable.
		if(!(val instanceof Comparable< ? >))
			throw new QQuerySyntaxException("Between of property " + n.getProp() + " of promoted type " + val.getClass() + " must implement Comparable.");

		Comparable<Object> c = (Comparable<Object>) val;

		int ra = c.compareTo(av);				// Compare val - av
		if(ra < 0) {
			m_lastResult = false;
			return;
		}
		int rb = c.compareTo(bv);
		if(rb > 0) {
			m_lastResult = false;
			return;
		}
		m_lastResult = true;
	}

	@Override
	public void visitSelection(@Nonnull QSelection< ? > s) throws Exception {
		m_lastResult = true;
		throw new IllegalStateException("Selection not implemented");
	}

	@Override
	public void visitCriteria(@Nonnull QCriteria< ? > qc) throws Exception {
		m_lastResult = true;
		super.visitCriteria(qc);
	}

	@Override
	public void visitSelectionColumns(@Nonnull QSelection< ? > s) throws Exception {
		throw new IllegalStateException("Selection not implemented");
	}

	@Override
	public void visitOrderList(List<QOrder> orderlist) throws Exception {
	}

	@Override
	public void visitOrder(@Nonnull QOrder o) throws Exception {
		throw new IllegalStateException("Order: not implemented");
	}

	@Override
	public void visitPropertySelection(@Nonnull QPropertySelection n) throws Exception {
		throw new IllegalStateException("Selection not implemented");
	}

	@Override
	public void visitSelectionColumn(@Nonnull QSelectionColumn n) throws Exception {
		throw new IllegalStateException("Selection not implemented");
	}

	@Override
	public void visitSelectionItem(@Nonnull QSelectionItem n) throws Exception {
		throw new IllegalStateException("Selection not implemented");
	}

	@Override
	public void visitMultiSelection(@Nonnull QMultiSelection n) throws Exception {
		throw new IllegalStateException("Selection not implemented");
	}

	@Override
	public void visitExistsSubquery(@Nonnull QExistsSubquery< ? > q) throws Exception {
		throw new IllegalStateException("'exists' has no meaning here");
	}

	@Override
	public void visitSelectionSubquery(@Nonnull QSelectionSubquery n) throws Exception {
		throw new IllegalStateException("'subselection' has no meaning here");
	}
}
