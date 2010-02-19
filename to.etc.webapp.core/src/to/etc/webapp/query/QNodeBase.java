package to.etc.webapp.query;

abstract public class QNodeBase {
	abstract public void visit(QNodeVisitor v) throws Exception;


	/**
	 * Render all nodes using the default string renderer.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		QQueryRenderer qr = new QQueryRenderer();
		try {
			String cn = getClass().getName();
			cn = cn.substring(cn.lastIndexOf('.') + 1);
			qr.append(cn);
			qr.append("[");
			visit(qr);
			qr.append("]");
			return qr.toString();
		} catch(Exception x) {
			return getClass().getName() + ": exception " + x + "]";
		}
	}
}
