package to.etc.domui.component.lookup;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.domui.util.query.*;
import to.etc.webapp.nls.*;

/**
 * Default Registry of Lookup control factories.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 23, 2008
 */
public class LookupControlRegistry {
	private List<LookupControlFactory>		m_factoryList = new ArrayList<LookupControlFactory>();

	public LookupControlRegistry() {
		register(TEXT_CF);
		register(DATE_CF);
	}
	public synchronized List<LookupControlFactory>	getFactoryList() {
		return m_factoryList;
	}

	public synchronized void	register(LookupControlFactory f) {
		m_factoryList = new ArrayList<LookupControlFactory>(m_factoryList);
		m_factoryList.add(f);
	}

	public LookupControlFactory	findFactory(PropertyMetaModel pmm) {
		LookupControlFactory	best = null;
		int score = 0;
		for(LookupControlFactory cf : m_factoryList) {
			int v = cf.accepts(pmm);
			if(v > score) {
				score = v;
				best = cf;
			}
		}
		return best;
	}

	public LookupControlFactory	getControlFactory(PropertyMetaModel pmm) {
		LookupControlFactory cf = findFactory(pmm);
		if(cf == null)
			throw new IllegalStateException("Cannot get a Lookup Control factory for "+pmm);
		return cf;
	}

	/**
	 * Default factory for most non-relational fields. This treats the property as a convertable
	 * text input thingy.
	 */
	@SuppressWarnings("unchecked")
	static public final LookupControlFactory	TEXT_CF	= new LookupControlFactory() {
		public LookupFieldQueryBuilderThingy createControl(final SearchPropertyMetaModel spm, final PropertyMetaModel pmm) {
			Class<?>	iclz	= pmm.getActualType();

			//-- Boolean/boolean types? These need a tri-state checkbox 
			if(iclz == Boolean.class || iclz == Boolean.TYPE) {
				throw new IllegalStateException("I need a tri-state checkbox component to handle boolean lookup thingies.");
			}

			//-- Treat everything else as a String using a converter.
			final Text<?>	txt	= new Text(iclz);
			if(pmm.getDisplayLength() > 0)
				txt.setSize(pmm.getDisplayLength());
			else {
				//-- We must decide on a length....
				int sz = 0;
				if(pmm.getLength() > 0) {
					sz = pmm.getLength();
					if(sz > 40)
						sz	= 40;
				}
				if(sz != 0)
					txt.setSize(sz);
			}
			if(pmm.getConverterClass() != null)
				txt.setConverterClass(pmm.getConverterClass());
			if(pmm.getLength() > 0)
				txt.setMaxLength(pmm.getLength());

			//-- Converter thingy is known. Now add a 
			return new DefaultLookupThingy(txt) {
				@Override
				public boolean appendCriteria(QCriteria crit) throws Exception {
					Object value	= null;
					try {
						value = txt.getValue();
					} catch(Exception x) {
						return false;						// Has validation error -> exit.
					}
					if(value == null || (value instanceof String && ((String)value).trim().length() == 0))
						return true;						// Is okay but has no data

					// FIXME Handle minimal-size restrictions on input (search field metadata

					
					//-- Put the value into the criteria..
					if(value instanceof String) {
						String str = (String)value;
						str	= str.trim()+"%";
						crit.ilike(pmm.getName(), str);
					} else {
						crit.eq(pmm.getName(), value);		// property == value
					}
					return true;
				}
			};
		}
	
		public int accepts(PropertyMetaModel pmm) {
			return 1;							// Accept all properties (will fail on incompatible ones @ input time)
		}
	};
	

	static public final LookupControlFactory	DATE_CF	= new LookupControlFactory() {
	
		public LookupFieldQueryBuilderThingy createControl(SearchPropertyMetaModel spm, final PropertyMetaModel pmm) {
			final DateInput	df	= new DateInput();
			TextNode	tn	= new TextNode(NlsContext.getGlobalMessage(Msgs.UI_LOOKUP_DATE_TILL));
			final DateInput	dt	= new DateInput();
			return new DefaultLookupThingy(df, tn, dt) {
				@Override
				public boolean appendCriteria(QCriteria<?> crit) throws Exception {
					Date	from, till;
					try {
						from = df.getValue();
					} catch(Exception x) {
						return false;
					}
					try {
						till = dt.getValue();
					} catch(Exception x) {
						return false;
					}
					if(from == null && till == null)
						return true;
					if(from != null && till != null) {
						if(from.getTime() > till.getTime()) {
							//-- Swap vals
							df.setValue(till);
							dt.setValue(from);
							from = till;
							till = dt.getValue();
						}

						//-- Between query
						crit.between(pmm.getName(), from, till);
					} else if(from != null) {
						crit.ge(pmm.getName(), from);
					} else {
						crit.lt(pmm.getName(), till);
					}
					return true;
				}
			};
		}
	
		public int accepts(PropertyMetaModel pmm) {
			if(Date.class.isAssignableFrom(pmm.getActualType()))
				return 2;
			return 0;
		}
	};
	
	
}
