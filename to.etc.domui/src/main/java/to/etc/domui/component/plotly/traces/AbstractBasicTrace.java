package to.etc.domui.component.plotly.traces;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.util.javascript.JsonBuilder;

import java.io.IOException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
class AbstractBasicTrace<T extends AbstractBasicTrace<T>> {
	@Nullable
	protected String m_name;

	private int m_domainX;

	private int m_domainY;

	private boolean m_hasDomain;

	protected void renderBase(JsonBuilder b) throws IOException {
		b.objFieldOpt("name", m_name);

		if(m_hasDomain) {
			b.objObjField("domain");
			b.objField("column", m_domainX);
			b.objField("row", m_domainY);
			b.objEnd();
		}
	}

	@Nullable
	public String getName() {
		return m_name;
	}

	public T name(String name) {
		m_name = name;
		return (T) this;
	}

	public int getDomainX() {
		return m_domainX;
	}

	public int getDomainY() {
		return m_domainY;
	}

	public T domain(int x, int y) {
		m_domainX = x;
		m_domainY = y;
		m_hasDomain = true;
		return (T) this;
	}




}
