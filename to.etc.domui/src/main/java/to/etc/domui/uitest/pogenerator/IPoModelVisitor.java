package to.etc.domui.uitest.pogenerator;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
public interface IPoModelVisitor {
	void visitClass(PoClass n) throws Exception;

	void visitMethod(PoMethod n) throws Exception;

	void visitField(PoField n) throws Exception;
}
