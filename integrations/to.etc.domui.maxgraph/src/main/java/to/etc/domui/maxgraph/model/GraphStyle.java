package to.etc.domui.maxgraph.model;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * How a cell looks. The methods cover what is worth naming; anything else maxGraph
 * understands is set with {@link #raw(String, Object)}, which is also what the named
 * methods do underneath. Only the properties that were set are sent.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphStyle {
	private final Map<String, Object> m_properties = new LinkedHashMap<>();

	/*----------------------------------------------------------------------*/
	/*	CODING:	The shape and its colours								    */
	/*----------------------------------------------------------------------*/

	public GraphStyle shape(GraphShape shape) {
		return raw("shape", shape.getName());
	}

	public GraphStyle fillColor(String color) {
		return raw("fillColor", color);
	}

	public GraphStyle strokeColor(String color) {
		return raw("strokeColor", color);
	}

	public GraphStyle strokeWidth(double width) {
		return raw("strokeWidth", Double.valueOf(width));
	}

	public GraphStyle dashed(boolean dashed) {
		return raw("dashed", Boolean.valueOf(dashed));
	}

	public GraphStyle rounded(boolean rounded) {
		return raw("rounded", Boolean.valueOf(rounded));
	}

	public GraphStyle opacity(double percentage) {
		return raw("opacity", Double.valueOf(percentage));
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	The label											        */
	/*----------------------------------------------------------------------*/

	public GraphStyle fontColor(String color) {
		return raw("fontColor", color);
	}

	public GraphStyle fontSize(int points) {
		return raw("fontSize", Integer.valueOf(points));
	}

	public GraphStyle bold() {
		return raw("fontStyle", Integer.valueOf(1));
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Edges												        */
	/*----------------------------------------------------------------------*/

	public GraphStyle edgeStyle(GraphEdgeStyle style) {
		return raw("edgeStyle", style.getName());
	}

	public GraphStyle startArrow(String arrow) {
		return raw("startArrow", arrow);
	}

	public GraphStyle endArrow(String arrow) {
		return raw("endArrow", arrow);
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	The escape hatch, and reading back			                */
	/*----------------------------------------------------------------------*/

	/**
	 * Set any maxGraph style property by name. A null value removes the property.
	 */
	public GraphStyle raw(String name, @Nullable Object value) {
		if(null == value) {
			m_properties.remove(name);
		} else {
			m_properties.put(name, value);
		}
		return this;
	}

	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(m_properties);
	}

	public boolean isEmpty() {
		return m_properties.isEmpty();
	}

	@Override
	public String toString() {
		return m_properties.toString();
	}
}
