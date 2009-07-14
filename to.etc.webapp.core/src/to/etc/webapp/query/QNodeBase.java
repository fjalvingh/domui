package to.etc.webapp.query;

abstract public class QNodeBase {
	abstract public void visit(QNodeVisitor v) throws Exception;
}
