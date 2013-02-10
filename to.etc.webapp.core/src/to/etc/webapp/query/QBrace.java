package to.etc.webapp.query;
import static to.etc.webapp.query.QOperation.AND;
import static to.etc.webapp.query.QOperation.OR;

import java.util.*;

import javax.annotation.*;

/**
 * A simple nested brace construction, objects can be added to the object list.
 * Accepeted objects are {@link QOperatorNode}, {@link QBrace}, {@link QList} and {@link QOperation}
 *
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * Created on Feb 10, 2013
 */
public class QBrace {
	private @Nonnull
	List<Object> m_objects = new ArrayList<Object>();
	public QBrace(@Nullable QBrace parent) {
		super();
		m_parent = parent;
	}
	public @Nullable
	QBrace getParent() {
		return m_parent;
	}
	private @Nullable
	QBrace m_parent;
	public void add(Object o) {
		m_objects.add(o);
	}
	@Override
	public String toString() {
		boolean render = getParent() == null;
		StringBuilder sb = new StringBuilder(render ? "" : "(");
		Object lastObject = null;
		for(Object object : m_objects) {
			if(object instanceof QOperatorNode || object instanceof QList || object instanceof QBrace) {
				if(lastObject instanceof QOperatorNode || lastObject instanceof QBrace || lastObject instanceof QList) {
					sb.append(AND);
					sb.append(" ");
				}
				if(object instanceof QOperatorNode) {
					try {
						QOperatorNode comp = (QOperatorNode) object;
						QQueryRenderer qr = new QQueryRenderer();
						comp.visit(qr);
						sb.append(qr.toString());
					} catch(Exception x) {
						sb.append(getClass().getName() + ": exception " + x + "]");
					}
				} else if(object instanceof QList) {
					sb.append("exist ");
					sb.append(((QList< ? >) object).getRoot().qbrace());
				} else {
					sb.append(object);
				}
			} else {
				sb.append(object);
			}
			sb.append(" ");
			lastObject = object;
		}
		sb.append(render ? "" : ")");
		return sb.toString();
	}

	/**
	 * Walks the brace tree and constructs {@link QOperatorNode}s where needed. Two nodes without an explicit OR in between will have an implicit AND in between them.
	 * The m_objects are read from right to left and when an AND operation is found it will create a {@link QMultiNode} of type {@link QOperation}.AND.
	 * The 2 nodes that are and-ed together will be replaced by the {@link QMultiNode} in the m_objects list or if one of the nodes is already a
	 * {@link QMultiNode} of type {@link QOperation}.AND the node that has to be AND-ed will join that {@link QMultiNode}.
	 * Now the modified m_objects list will be read again from right to left. This will repeat until no AND operations are found anymore.
	 * After that there are either only OR operations or no operations at all.
	 * If there are OR operations all node will be added to a {@link QMultiNode} of type {@link QOperation}.OR and that {@link QMultiNode}
	 * will be returned.
	 * If there are no operations found then there is only one {@link QOperatorNode} which will be returned.
	 * @return
	 */
	public @Nonnull
	QOperatorNode toQOperatorNode() {
		QMultiNode m = null;
		Object lastObject = null;
		QOperatorNode node = null;
		QOperatorNode lastnode = null;
		if(m_objects.size() == 1 && m_objects.get(0) instanceof QOperatorNode) {
			return (QOperatorNode) m_objects.get(0);
		}
		if(m_objects.size() == 1 && m_objects.get(0) instanceof QList) {
			QExistsSubquery< ? > subquery = fixSub((QList< ? >) m_objects.get(0));
			return subquery;
		}
		if(m_objects.size() == 1 && m_objects.get(0) instanceof QBrace) {
			QBrace brace = (QBrace) m_objects.get(0);
			return brace.toQOperatorNode();
		}
		List<Object> objects = m_objects;
		List<Object> newObjects = new ArrayList<Object>();
		Set<Object> remove = new HashSet<Object>();
		Set<Object> replace = new HashSet<Object>();
		Map<Object, QOperatorNode> toNode = new HashMap<Object, QOperatorNode>();
		while(objects.size() > 1) {
			newObjects = new ArrayList<Object>();
			for(int i = objects.size() - 1; i > -1; i--) {
				Object object = objects.get(i);
				if(object instanceof QBrace) {
					QBrace brace = (QBrace) object;
					node = brace.toQOperatorNode();
					object = node;
					toNode.put(brace, node);
				} else if(object instanceof QList) {
					node = fixSub((QList< ? >) object);
					toNode.put(object, node);
					object = node;
				} else if(object instanceof QOperatorNode) {
					node = (QOperatorNode) object;
				}
				if(lastObject != null && lastObject instanceof QOperatorNode && object instanceof QOperatorNode) {
					if(lastnode instanceof QMultiNode && ((QMultiNode) lastnode).getOperation() == AND) {
						m = (QMultiNode) lastnode;
						m.add(node);
					} else {
						m = new QMultiNode(AND);
						m.add(node);
						m.add(lastnode);
					}
					remove.add(node);
					remove.add(lastnode);
					replace.add(node);

					break;
				}
				lastnode = node;
				lastObject = object;
			}
			if(m != null) {
				for(int i = 0; i < objects.size(); i++) {
					Object object = objects.get(i);
					QOperatorNode converted = toNode.get(object);
					if(!remove.contains(object) && !remove.contains(converted)) {
						newObjects.add(object);
					}
					if(replace.contains(object) || replace.contains(converted)) {
						newObjects.add(m);
					}
				}
				if(newObjects.size() == 1 && newObjects.get(0) instanceof QOperatorNode) {
					return (QOperatorNode) newObjects.get(0);
				}
			} else {
				m = new QMultiNode(OR);
				for(int i = 0; i < objects.size(); i++) {
					Object object = objects.get(i);
					QOperatorNode converted = toNode.get(object);
					if(object instanceof QOperatorNode) {
						m.add((QOperatorNode) object);
					} else if(converted != null) {
						m.add(converted);
					}
				}
				return m;
			}
			objects = newObjects;
			m = null;
			lastObject = null;
			lastnode = null;
			replace.clear();
			remove.clear();
		}
		return m;
	}

	private QExistsSubquery< ? > fixSub(@Nonnull QList< ? > qList) {
		QExistsSubquery< ? > subquery = qList.getSubquery();
		subquery.setRestrictions(qList.getRoot().qbrace().toQOperatorNode());
		return subquery;
	}
}