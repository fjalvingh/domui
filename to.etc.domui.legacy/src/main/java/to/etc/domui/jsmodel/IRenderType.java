package to.etc.domui.jsmodel;

/**
* @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
* Created on 12/25/14.
*/
interface IRenderType<T> {
	void render(Appendable a, T value) throws Exception;
}
