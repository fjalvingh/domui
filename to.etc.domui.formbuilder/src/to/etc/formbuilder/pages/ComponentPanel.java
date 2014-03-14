package to.etc.formbuilder.pages;

import java.util.*;
import java.util.Map.Entry;

import javax.annotation.*;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;

public class ComponentPanel extends Div {
	final private List<IFbComponent> m_componentList;

	public ComponentPanel(@Nonnull List<IFbComponent> componentList) {
		m_componentList = componentList;

	}

	@Override
	public void createContent() throws Exception {
		setCssClass("fb-cp");

//		HTag ht = new HTag(4);
//		add(ht);
//		ht.add("Components");
//		ht.setCssClass("fb-comp");

		//-- Create all categories.
		Map<String, List<IFbComponent>> cmap = new HashMap<String, List<IFbComponent>>();
		for(IFbComponent comp : m_componentList) {
			String cat = comp.getCategoryName();
			if(cat.toLowerCase().contains("misc"))
				cat = "Miscellaneous";
			else if(cat.equals("layout"))
				cat = "Panel Stuff";
			else if(cat.equals("panellayout"))
				cat = "Layout Panels";

			List<IFbComponent> l = cmap.get(cat);
			if(l == null) {
				l = new ArrayList<IFbComponent>();
				cmap.put(cat, l);
			}
			l.add(comp);
		}

		//-- All categories having < 2 components are collected in "miscellaneous"
		List<IFbComponent> miscl = new ArrayList<IFbComponent>();
		for(Iterator<Map.Entry<String, List<IFbComponent>>> me = cmap.entrySet().iterator(); me.hasNext();) {
			Entry<String, List<IFbComponent>> entry = me.next();
			if(entry.getValue().size() < 2) {
				miscl.addAll(entry.getValue());
				me.remove();
			}
		}

		if(miscl.size() > 0)
			cmap.put("Miscellaneous", miscl);

		//-- Now render per category
		List<String> catlist = new ArrayList<String>(cmap.keySet());
		Collections.sort(catlist);
		for(String cat : catlist) {
			if("Html".equals(cat))
				continue;

			List<IFbComponent> list = cmap.get(cat);
			Collections.sort(list, new Comparator<IFbComponent>() {
				@Override
				public int compare(IFbComponent a, IFbComponent b) {
					return a.getShortName().compareTo(b.getShortName());
				}
			});
			add(new CaptionedHeader(cat));

			for(IFbComponent comp : list) {
				PnlComponent pc = new PnlComponent(comp);
				add(pc);
			}


		}

	}
}
