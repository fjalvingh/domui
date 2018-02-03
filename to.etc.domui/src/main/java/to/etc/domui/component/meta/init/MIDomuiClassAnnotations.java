package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.MetaCombo;
import to.etc.domui.component.meta.MetaObject;
import to.etc.domui.component.meta.MetaSearchItem;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyType;
import to.etc.domui.component.meta.impl.DefaultClassMetaModel;
import to.etc.domui.component.meta.impl.SearchPropertyMetaModelImpl;
import to.etc.domui.component.meta.impl.UndefinedComboDataSet;
import to.etc.domui.util.Constants;
import to.etc.domui.util.UndefinedLabelStringRenderer;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-10-17.
 */
public class MIDomuiClassAnnotations extends AbstractClassAnnotationProvider {
	@Override protected void decodeClassAnnotation(MetaInitContext context, DefaultClassMetaModel cmm, Annotation an, String ana) throws Exception {
		if(an instanceof MetaCombo) {
			final MetaCombo c = (MetaCombo) an;
			if(c.dataSet() != UndefinedComboDataSet.class)
				cmm.setComboDataSet(c.dataSet());
			if(c.labelRenderer() != UndefinedLabelStringRenderer.class)
				cmm.setComboLabelRenderer(c.labelRenderer());
			if(c.nodeRenderer() != UndefinedLabelStringRenderer.class)
				cmm.setComboNodeRenderer(c.nodeRenderer());
			if(c.properties() != null && c.properties().length > 0) {
				cmm.setComboDisplayProperties(MetaInitializer.decode(cmm, c.properties()));

				//colli.later(new Runnable() {
				//	@Override
				//	public void run() {
				//	}
				//});
			}
			if(c.preferred())
				cmm.setComponentTypeHint(Constants.COMPONENT_COMBO);
		} else if(an instanceof MetaObject) {
			final MetaObject mo = (MetaObject) an;

			if(mo.defaultColumns().length > 0) {
				cmm.setTableDisplayProperties(MetaInitializer.decode(cmm, mo.defaultColumns()));

				//colli.later(new Runnable() {
				//	@Override
				//	public void run() {
				//		cmm.setTableDisplayProperties(DisplayPropertyMetaModel.decode(cmm, mo.defaultColumns()));
				//	}
				//});
			}
			if(!mo.defaultSortColumn().equals(Constants.NONE))
				cmm.setDefaultSortProperty(mo.defaultSortColumn());
			cmm.setDefaultSortDirection(mo.defaultSortOrder());

			if(mo.selectedRenderer() != UndefinedLabelStringRenderer.class)
				cmm.setLookupSelectedRenderer(mo.selectedRenderer());
			if(mo.selectedProperties().length != 0) {
				cmm.setLookupSelectedProperties(MetaInitializer.decode(cmm, mo.selectedProperties()));

				//colli.later(new Runnable() {
				//	@Override
				//	public void run() {
				//		cmm.setLookupSelectedProperties(DisplayPropertyMetaModel.decode(cmm, mo.selectedProperties()));
				//	}
				//});
			}

			//-- Handle search
			int index = 0;

			List<SearchPropertyMetaModel> searchList = new ArrayList<>(cmm.getSearchProperties());
			List<SearchPropertyMetaModel> keySearchList = new ArrayList<>(cmm.getKeyWordSearchProperties());
			for(MetaSearchItem msi : mo.searchProperties()) {
				index++;
				PropertyMetaModel<?> pmm = cmm.getProperty(msi.name());
				SearchPropertyMetaModelImpl mm = new SearchPropertyMetaModelImpl(cmm, pmm);
				mm.setIgnoreCase(msi.ignoreCase());
				mm.setOrder(msi.order() == -1 ? index : msi.order());
				mm.setMinLength(msi.minLength());
				mm.setLookupLabelKey(msi.lookupLabelKey().length() == 0 ? null : msi.lookupLabelKey());
				mm.setLookupHintKey(msi.lookupHintKey().length() == 0 ? null : msi.lookupHintKey());
				if(msi.searchType() == SearchPropertyType.SEARCH_FIELD || msi.searchType() == SearchPropertyType.BOTH) {
					searchList.add(mm);
				}
				if(msi.searchType() == SearchPropertyType.KEYWORD || msi.searchType() == SearchPropertyType.BOTH) {
					keySearchList.add(mm);
				}
			}

			searchList.sort(SearchPropertyMetaModel.BY_ORDER);
			keySearchList.sort(SearchPropertyMetaModel.BY_ORDER);
			cmm.setSearchProperties(searchList);
			cmm.setKeyWordSearchProperties(keySearchList);
		}
	}
}
