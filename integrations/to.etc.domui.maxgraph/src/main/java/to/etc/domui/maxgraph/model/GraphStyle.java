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
 * <p>The style of a cell reports every change to that cell, so restyling a drawing that
 * is already on screen restyles it there too. A style made on its own - to hold a set of
 * properties to copy from - reports to nobody.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphStyle {
	/** The cell this is the style of, or null for a style that is not part of a drawing. */
	@Nullable
	private final GraphCell m_owner;

	private final Map<String, Object> m_properties = new LinkedHashMap<>();

	public GraphStyle() {
		this(null);
	}

	GraphStyle(@Nullable GraphCell owner) {
		m_owner = owner;
	}

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
		record();
		if(null == value) {
			m_properties.remove(name);
		} else {
			m_properties.put(name, value);
		}
		GraphCell owner = m_owner;
		if(null != owner) {
			owner.internalStyleChanged();
		}
		return this;
	}

	/**
	 * Replace everything in this style with what is in the other one. Used when the browser
	 * sends a cell's complete style back.
	 */
	public GraphStyle replaceWith(GraphStyle style) {
		if(style == this) {
			return this;
		}
		record();
		m_properties.clear();
		m_properties.putAll(style.m_properties);
		GraphCell owner = m_owner;
		if(null != owner) {
			owner.internalStyleChanged();
		}
		return this;
	}

	/**
	 * What this style holds now, as the change that puts it back - one whole style, because
	 * that is what a style change is: the browser sends the complete style of a cell too.
	 */
	private void record() {
		GraphCell owner = m_owner;
		if(null != owner) {
			GraphStyle old = new GraphStyle();
			old.m_properties.putAll(m_properties);
			owner.getModel().record(() -> replaceWith(old));
		}
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
