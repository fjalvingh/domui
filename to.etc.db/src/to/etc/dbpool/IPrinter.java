package to.etc.dbpool;

/**
 * Helps with reporting state.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 3, 2010
 */
public interface IPrinter {
	IPrinter header(String cssclass, String name);

	IPrinter warning(String what);

	IPrinter nl();

	IPrinter pre(String css, String pre);

	IPrinter text(String s);

}
