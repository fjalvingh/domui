package to.etc.domui.component.plotly.traces;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.plotly.layout.PlFont;
import to.etc.domui.util.javascript.JsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
final public class PlSunBurstTrace implements IPlotlyTrace {
	private final List<SunburstValue> m_list = new ArrayList<>();

	private final Map<String, SunburstValue> m_parentMap = new HashMap<>();

	private double m_leafOpacity;

	private PlBranchValues m_branchValues;

	private int m_maxDepth;

	private Boolean m_valueLess;

	private PlTextPosition m_textPosition;

	private PlTextOrientation m_insideTextOrientation;

	private PlFont m_insideTextFont = new PlFont();

	private PlFont m_outsideTextFont = new PlFont();

	private Set<PlSunburstTextInfo> m_textInfo = new HashSet<>();

	/**
	 * Add items with an associated value, which will indicate the width of the item.
	 */
	public PlSunBurstTrace add(String id, String parentId, String label, double value) {
		Boolean vless = m_valueLess;
		if(vless == null) {
			m_valueLess = false;
		} else if(vless)
			throw new IllegalStateException("You cannot mix valueless and with-value sunburst items");

		if(parentId != null && parentId.length() > 0) {
			//-- Parent must exist
			if(! m_parentMap.containsKey(parentId))
				throw new IllegalStateException("No parent found with ID=" + parentId);
		}

		SunburstValue v = new SunburstValue(id, label, value, parentId);
		if(m_parentMap.put(id, v) != null)
			throw new IllegalStateException("Duplicate ID=" + id + " in sunburst dataset");
		m_list.add(v);
		return this;
	}

	/**
	 * Add items without any value.
	 */
	public PlSunBurstTrace add(String id, String parentId, String label) {
		Boolean vless = m_valueLess;
		if(vless == null) {
			m_valueLess = true;
		} else if(! vless)
			throw new IllegalStateException("You cannot mix valueless and with-value sunburst items");

		if(parentId != null && parentId.length() > 0) {
			//-- Parent must exist
			if(! m_parentMap.containsKey(parentId))
				throw new IllegalStateException("No parent found with ID=" + parentId);
		}

		SunburstValue v = new SunburstValue(id, label, 0.0, parentId);
		if(m_parentMap.put(id, v) != null)
			throw new IllegalStateException("Duplicate ID=" + id + " in sunburst dataset");
		m_list.add(v);
		return this;
	}

	@Override
	public void render(@NonNull JsonBuilder b) throws Exception {
		b.objField("type", "sunburst");
		if(m_maxDepth != 0)
			b.objField("maxdepth", m_maxDepth);
		PlBranchValues bv = m_branchValues;
		if(null != bv) {
			b.objField("branchvalues", bv.name().toLowerCase());
		}
		if(m_leafOpacity > 0.0D) {
			b.objObjField("leaf");
			b.objField("opacity", m_leafOpacity);
			b.objEnd();
		}

		b.objArrayField("ids");
		for(SunburstValue v : m_list) {
			b.item(v.getId());
		}
		b.arrayEnd();

		b.objArrayField("parents");
		for(SunburstValue v : m_list) {
			String parent = v.getParentId();
			b.item(parent == null ? "" : parent);
		}
		b.arrayEnd();

		Boolean vl = m_valueLess;
		if(vl != null && ! vl) {
			b.objArrayField("values");
			for(SunburstValue v : m_list) {
				b.item(v.getValue());
			}
			b.arrayEnd();
		}

		b.objArrayField("labels");
		for(SunburstValue v : m_list) {
			b.item(v.getLabel());
		}
		b.arrayEnd();

		PlTextPosition t = m_textPosition;
		if(null != t) {
			b.objField("textposition", t.name().toLowerCase());
		}
		PlTextOrientation or = m_insideTextOrientation;
		if(null != or)
			b.objField("insidetextorientation", or.name().toLowerCase());
		if(!m_insideTextFont.isEmpty()) {
			b.objObjField("insidetextfont");
			m_insideTextFont.render(b);
			b.objEnd();
		}
		if(!m_outsideTextFont.isEmpty()) {
			b.objObjField("outsidetextfont");
			m_outsideTextFont.render(b);
			b.objEnd();
		}
		if(m_textInfo.size() > 0) {
			b.objField("textinfo", m_textInfo.stream().map(a -> a.getCode()).collect(Collectors.joining("+")));
		}

	}

	/**
	 * Sets the opacity of the leaves. With colorscale it is defaulted to 1; otherwise it is defaulted to 0.7.
	 */
	public PlSunBurstTrace leafOpacity(double v) {
		m_leafOpacity = v;
		return this;
	}

	public PlSunBurstTrace branchValues(PlBranchValues v) {
		m_branchValues = v;
		return this;
	}

	/**
	 * Sets the number of rendered sectors from any given `level`. Set `maxdepth` to "-1" to
	 * render all the levels in the hierarchy.
	 */
	public PlSunBurstTrace maxDepth(int depth) {
		m_maxDepth = depth;
		return this;
	}

	public PlTextPosition getTextPosition() {
		return m_textPosition;
	}

	/**
	 * Specifies the location of the `textinfo`.
	 */
	public PlSunBurstTrace textPosition(PlTextPosition textPosition) {
		m_textPosition = textPosition;
		return this;
	}

	public PlTextOrientation getInsideTextOrientation() {
		return m_insideTextOrientation;
	}

	/**
	 * Controls the orientation of the text inside chart sectors. When set to "auto", text
	 * may be oriented in any direction in order to be as big as possible in the middle of
	 * a sector. The "horizontal" option orients text to be parallel with the bottom of the
	 * chart, and may make text smaller in order to achieve that goal. The "radial" option
	 * orients text along the radius of the sector. The "tangential" option orients text
	 * perpendicular to the radius of the sector.
	 */
	public PlSunBurstTrace insideTextOrientation(PlTextOrientation insideTextOrientation) {
		m_insideTextOrientation = insideTextOrientation;
		return this;
	}

	/**
	 * Sets the font used for `textinfo` lying inside the sector.
	 */
	public PlFont insideTextFont() {
		return m_insideTextFont;
	}

	/**
	 * Sets the font used for `textinfo` lying outside the sector.
	 */
	public PlFont outsideTextFont() {
		return m_outsideTextFont;
	}

	/**
	 * Determines which trace information appear on the graph. Can be any combination
	 * of the TextInfo enum.
	 */
	public PlSunBurstTrace textInfo(PlSunburstTextInfo... ti) {
		m_textInfo.addAll(Arrays.asList(ti));
		return this;
	}

}
