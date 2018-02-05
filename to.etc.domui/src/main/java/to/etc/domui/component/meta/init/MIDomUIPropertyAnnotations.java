package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.MetaCombo;
import to.etc.domui.component.meta.MetaObject;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.MetaSearch;
import to.etc.domui.component.meta.MetaSearchItem;
import to.etc.domui.component.meta.MetaValueValidator;
import to.etc.domui.component.meta.NumericPresentation;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.PropertyMetaValidator;
import to.etc.domui.component.meta.PropertyRelationType;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyType;
import to.etc.domui.component.meta.SortableType;
import to.etc.domui.component.meta.TemporalPresentationType;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.component.meta.impl.DefaultClassMetaModel;
import to.etc.domui.component.meta.impl.DefaultPropertyMetaModel;
import to.etc.domui.component.meta.impl.MetaModelException;
import to.etc.domui.component.meta.impl.MetaPropertyValidatorImpl;
import to.etc.domui.component.meta.impl.SearchPropertyMetaModelImpl;
import to.etc.domui.component.meta.impl.UndefinedComboDataSet;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.DummyConverter;
import to.etc.domui.converter.IConverter;
import to.etc.domui.converter.IValueValidator;
import to.etc.domui.util.Constants;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;
import to.etc.domui.util.UndefinedLabelStringRenderer;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-10-17.
 */
@DefaultNonNull
public class MIDomUIPropertyAnnotations implements IPropertyMetaProvider<DefaultClassMetaModel, DefaultPropertyMetaModel<?>> {
	@Nonnull
	final private List<SearchPropertyMetaModel> m_searchList = new ArrayList<SearchPropertyMetaModel>();

	@Nonnull
	final private List<SearchPropertyMetaModel> m_keySearchList = new ArrayList<SearchPropertyMetaModel>();

	@Override public void provide(@Nonnull MetaInitContext context, @Nonnull DefaultClassMetaModel cmm, @Nonnull DefaultPropertyMetaModel<?> pmm) throws Exception {
		Annotation[] annar = pmm.getDescriptor().getGetter().getAnnotations();
		for(Annotation an : annar) {
			String ana = an.annotationType().getName();
			decodePropertyAnnotation(cmm, pmm, an);
		}
	}

	@Override public void afterPropertiesDone(@Nonnull MetaInitContext context, @Nonnull DefaultClassMetaModel cmm) {
		m_searchList.sort(SearchPropertyMetaModel.BY_ORDER);
		m_keySearchList.sort(SearchPropertyMetaModel.BY_ORDER);

		cmm.setSearchProperties(m_searchList);
		cmm.setKeyWordSearchProperties(m_keySearchList);
	}

	@SuppressWarnings({"unchecked"})
	protected <T> void decodePropertyAnnotation(DefaultClassMetaModel cmm, DefaultPropertyMetaModel<T> pmm, Annotation an) {
		if(an instanceof MetaProperty) {
			handleMetaProperty(pmm, (MetaProperty) an);
		} else if(an instanceof MetaCombo) {
			handleMetaCombo(cmm, pmm, (MetaCombo) an);
		} else if(an instanceof MetaSearch) {
			handleMetaSearch(cmm, pmm, (MetaSearch) an);
		} else if(an instanceof MetaObject) {
			handleMetaTable(cmm, pmm, (MetaObject) an);
		}
	}

	private <T> void handleMetaTable(DefaultClassMetaModel cmm, DefaultPropertyMetaModel<T> pmm, MetaObject an) {
		/*
		 * Table metamodel.
		 */
		if(an.selectedRenderer() != UndefinedLabelStringRenderer.class)
			pmm.setLookupSelectedRenderer((Class<? extends IRenderInto<T>>) an.selectedRenderer());

		if(an.selectedProperties().length != 0) {
			pmm.setLookupSelectedProperties(MetaInitializer.decode(cmm, an.selectedProperties()));
		}

		if(an.defaultColumns().length > 0) {
			pmm.setLookupTableProperties(MetaInitializer.decode(cmm, an.defaultColumns()));
		}

		if(an.defaultSortColumn() != Constants.NONE) {}
		if(an.defaultSortOrder() != SortableType.UNKNOWN) {
			// FIXME Missing metadata!!

		}

		if(an.searchProperties().length > 0) {
			int index = 0;
			List<SearchPropertyMetaModel> propsearchlist = new ArrayList<SearchPropertyMetaModel>();
			List<SearchPropertyMetaModel> propkeysearchlist = new ArrayList<SearchPropertyMetaModel>();

			for(MetaSearchItem msi : an.searchProperties()) {
				index++;

				PropertyMetaModel<?> prop = cmm.getProperty(msi.name());
				SearchPropertyMetaModelImpl mm = new SearchPropertyMetaModelImpl(cmm, prop);
				mm.setIgnoreCase(msi.ignoreCase());
				mm.setOrder(msi.order() == -1 ? index : msi.order());
				mm.setMinLength(msi.minLength());
				mm.setLookupLabelKey(msi.lookupLabelKey().length() == 0 ? null : msi.lookupLabelKey());
				mm.setLookupHintKey(msi.lookupHintKey().length() == 0 ? null : msi.lookupHintKey());
				if(msi.searchType() == SearchPropertyType.SEARCH_FIELD || msi.searchType() == SearchPropertyType.BOTH) {
					propsearchlist.add(mm);
				}
				if(msi.searchType() == SearchPropertyType.KEYWORD || msi.searchType() == SearchPropertyType.BOTH) {
					propkeysearchlist.add(mm);
				}
			}
			pmm.setLookupFieldKeySearchProperties(propkeysearchlist);
			pmm.setLookupFieldSearchProperties(propsearchlist);
		}
	}

	private <T> void handleMetaSearch(DefaultClassMetaModel cmm, DefaultPropertyMetaModel<T> pmm, MetaSearch an) {
		SearchPropertyMetaModelImpl mm = new SearchPropertyMetaModelImpl(cmm, pmm);
		mm.setIgnoreCase(an.ignoreCase());
		mm.setOrder(an.order());
		mm.setMinLength(an.minLength());
		if(an.searchType() == SearchPropertyType.SEARCH_FIELD || an.searchType() == SearchPropertyType.BOTH) {
			m_searchList.add(mm);
		}
		if(an.searchType() == SearchPropertyType.KEYWORD || an.searchType() == SearchPropertyType.BOTH) {
			m_keySearchList.add(mm);
		}
	}

	private <T> void handleMetaCombo(@Nonnull DefaultClassMetaModel cmm, @Nonnull DefaultPropertyMetaModel<T> pmm, MetaCombo an) {
		final MetaCombo c = an;
		if(c.dataSet() != UndefinedComboDataSet.class) {
			pmm.setRelationType(PropertyRelationType.UP);
			pmm.setComboDataSet(c.dataSet());
		}
		if(c.labelRenderer() != UndefinedLabelStringRenderer.class) {
			pmm.setRelationType(PropertyRelationType.UP);
			pmm.setComboLabelRenderer(c.labelRenderer());
		}
		if(c.nodeRenderer() != UndefinedLabelStringRenderer.class) {
			pmm.setRelationType(PropertyRelationType.UP);
			pmm.setComboNodeRenderer((Class<? extends IRenderInto<T>>) c.nodeRenderer());
		}
		pmm.setComponentTypeHint(Constants.COMPONENT_COMBO);
		if(c.properties() != null && c.properties().length > 0) {
			pmm.setRelationType(PropertyRelationType.UP);
			pmm.setComboDisplayProperties(MetaInitializer.decode(pmm.getValueModel(), c.properties()));
		}
	}

	private <T> void handleMetaProperty(@Nonnull DefaultPropertyMetaModel<T> pmm, MetaProperty an) {
		//-- Handle meta-assignments.
		MetaProperty mp = an;
		if(mp.defaultSortable() != SortableType.UNKNOWN)
			pmm.setSortable(mp.defaultSortable());
		if(mp.length() >= 0)
			pmm.setLength(mp.length());
		if(mp.displaySize() >= 0)
			pmm.setDisplayLength(mp.displaySize());
		if(mp.noWrap() != YesNoType.UNKNOWN)
			pmm.setNowrap(mp.noWrap());
		if(mp.required() != YesNoType.UNKNOWN)
			pmm.setRequired(mp.required() == YesNoType.YES);
		if(mp.converterClass() != DummyConverter.class)
			pmm.setConverter(ConverterRegistry.getConverterInstance((Class< ? extends IConverter<T>>) mp.converterClass()));
		if(mp.temporal() != TemporalPresentationType.UNKNOWN && pmm.getTemporal() == TemporalPresentationType.UNKNOWN)
			pmm.setTemporal(mp.temporal());
		if(mp.numericPresentation() != NumericPresentation.UNKNOWN)
			pmm.setNumericPresentation(mp.numericPresentation());
		if(pmm.getReadOnly() != YesNoType.YES) // Do not override readonlyness from missing write method
			pmm.setReadOnly(mp.readOnly());
		if(mp.componentTypeHint().length() != 0)
			pmm.setComponentTypeHint(mp.componentTypeHint());

		//-- Convert validators.
		List<MetaPropertyValidatorImpl> list = new ArrayList<MetaPropertyValidatorImpl>();
		for(Class< ? extends IValueValidator< ? >> vv : mp.validator()) {
			MetaPropertyValidatorImpl vi = new MetaPropertyValidatorImpl(vv);
			list.add(vi);
		}
		for(MetaValueValidator mvv : mp.parameterizedValidator()) {
			MetaPropertyValidatorImpl vi = new MetaPropertyValidatorImpl(mvv.validator(), mvv.parameters());
			list.add(vi);
		}
		pmm.setValidators(list.toArray(new PropertyMetaValidator[list.size()]));

		//-- Regexp validators.
		if(mp.regexpValidation().length() > 0) {
			try {
				//-- Precompile to make sure it's valid;
				Pattern.compile(mp.regexpValidation());
			} catch(Exception x) {
				throw new MetaModelException(Msgs.BUNDLE, Msgs.MM_BAD_REGEXP, mp.regexpValidation(), this.toString());
			}
			pmm.setRegexpValidator(mp.regexpValidation());
			if(mp.regexpUserString().length() > 0)
				pmm.setRegexpUserString(mp.regexpUserString());
		}
	}
}
