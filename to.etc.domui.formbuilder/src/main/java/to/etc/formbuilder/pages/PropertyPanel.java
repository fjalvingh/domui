package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.layout.CaptionedHeader;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.util.DomUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shows and allows editing of the currently-selected component instance(s).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 8, 2013
 */
public class PropertyPanel extends Div {
	@NonNull
	private Set<ComponentInstance> m_selected = new HashSet<ComponentInstance>();

	@NonNull
	private Map<PropertyDefinition, IPropertyEditor> m_editorMap = new HashMap<PropertyDefinition, IPropertyEditor>();

	@Override
	public void createContent() throws Exception {
		setCssClass("fb-prp");
		Set<PropertyDefinition> all = getSharedProperties();
		Map<String, List<PropertyDefinition>> map = new HashMap<String, List<PropertyDefinition>>();
		for(PropertyDefinition pd : all) {
			String cat = pd.getCategory();
			List<PropertyDefinition> list = map.get(cat);
			if(null == list) {
				list = new ArrayList<PropertyDefinition>();
				map.put(cat, list);
			}
			list.add(pd);
		}

		//-- Render all property fragments, per item
		TBody b = addTable();

		for(Map.Entry<String, List<PropertyDefinition>> me: map.entrySet()) {
			List<PropertyDefinition>	pdlist = me.getValue();

			renderCategory(b, me.getKey(), me.getValue());
		}
	}

	private void renderCategory(@NonNull TBody b, @NonNull String cat, @NonNull List<PropertyDefinition> props) throws Exception {
		Collections.sort(props, new Comparator<PropertyDefinition>() {
			@Override
			public int compare(PropertyDefinition a, PropertyDefinition b) {
				return a.getName().compareTo(b.getName());
			}
		});

		TD td = b.addRowAndCell();
		td.setColspan(2);
		td.add(new CaptionedHeader(cat));
		for(PropertyDefinition pd: props) {
			renderPropertyDef(b, pd);
		}
	}

	private void renderPropertyDef(@NonNull TBody b, @NonNull PropertyDefinition pd) throws Exception {
		IPropertyEditor pe = pd.getEditor().createEditor(pd);
		m_editorMap.put(pd, pe);
		TD namecell = b.addRowAndCell();
		namecell.add(pd.getName());
		TD editcell = b.addCell();
		Object value = getPropertyValue(pd);
		if(value != VALUE_NOT_SAME)
			pe.setValue(value);
		pe.renderValue(editcell);
	}

	static private final Object VALUE_NOT_SAME = "($notsame$)";

	/**
	 * Get the combined value of a property for all selected objects.
	 * @param pd
	 * @return
	 */
	@Nullable
	private Object getPropertyValue(@NonNull PropertyDefinition pd) throws Exception {
		Object theValue = null;

		int count = 0;
		for(ComponentInstance ci : m_selected) {
			try {
				Object pv = ci.getPropertyValue(pd);
				if(count++ == 0)
					theValue = pv;
				else {
					if(!DomUtil.isEqual(theValue, pv))
						return VALUE_NOT_SAME;
				}
			} catch(Exception x) {
				x.printStackTrace();
				return VALUE_NOT_SAME;
			}
		}
		return theValue;
	}

	public void selectionChanged(Set<ComponentInstance> newSelection) {
		if(newSelection.size() == 0) {
			m_selected.clear();
			m_editorMap.clear();
		} else if(newSelection.size() == 1) {
			m_selected = newSelection;
		}
		forceRebuild();
	}

	/**
	 * Return all properties shared between all selected components.
	 * @return
	 */
	private Set<PropertyDefinition> getSharedProperties() {
		Set<PropertyDefinition> res = new HashSet<PropertyDefinition>();
		int count = 0;
		for(ComponentInstance pd : m_selected) {
			Set<PropertyDefinition> pdset = pd.getComponentType().getProperties();
			if(count++ == 0)
				res.addAll(pdset);
			else
				res.retainAll(pdset);
		}
		return res;
	}


}
