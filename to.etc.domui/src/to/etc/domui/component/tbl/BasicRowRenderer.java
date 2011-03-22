package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.converter.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * Highly customizable RowRenderer. This has many ways to customize the row output, often using
 * metadata. The definition for this renderer can be set until it's first use; it's actual definition
 * gets calculated at the time it's first used.
 * The possible specifications used in property modifiers are:
 * <ul>
 *	<li>"%28": a String starting with % denotes a width in percents. %28 gets translated to setWidth("28%");</li>
 *	<li>"^Title": a String starting with ^ denotes the header caption to use. Use ^~key to internationalize.</li>
 *	<li>"$cssclass": a String denoting a CSS class.</li>
 *	<li>Class&lt;? extends IConverter&gt;: the converter to use to convert the value to a string</li>
 *	<li>IConverter: an instance of a converter</li>
 *	<li>Class&lt;? extends INodeContentRenderer&lt;T&gt;&gt;: the class to use to render the content of the column.</li>
 *	<li>INodeContentRenderer&lt;T&gt;: an instance of a node renderer to use to render the content of the column.</li>
 *	<li>BasicRowRenderer.NOWRAP: forces a 'nowrap' on the column</li>
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
public class BasicRowRenderer<T> extends AbstractRowRenderer<T> implements IRowRenderer<T> {
	static public final String NOWRAP = "-NOWRAP";

	/** The column name to sort on by default, set by metadata. This is only used to keep it for a while until the actual column list is known; at that point the column def to sort on is determined and used. */
	private String m_sortColumnName;

	//	private boolean					m_sortableModel;

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple renderer initialization && parameterisation	*/
	/*--------------------------------------------------------------*/
	/**
	 * Create a renderer by handling the specified class and a list of properties off it.
	 * @param dataClass
	 * @param cols
	 */
	public BasicRowRenderer(final Class<T> dataClass, final Object... cols) throws Exception {
		super(dataClass);
		if(cols.length != 0)
			addColumns(cols);
		m_sortColumnName = model().getDefaultSortProperty();
	}

	/**
	 * Add the specified list of property names and presentation options to the column definitions. The items passed in the
	 * columns object can be multiple property definitions followed by specifications. A property name is a string starting
	 * with a letter always. All other Strings and objects are treated as specifications for display. The possible specifications
	 * are:
	 * <ul>
	 *	<li>"%28": a String starting with % denotes a width in percents. %28 gets translated to setWidth("28%");</li>
	 *	<li>"^Title": a String starting with ^ denotes the header caption to use. Use ^~key~ to internationalize.</li>
	 *	<li>"$cssclass": a String denoting a CSS class.</li>
	 *	<li>Class&lt;? extends IConverter&gt;: the converter to use to convert the value to a string</li>
	 *	<li>IConverter: an instance of a converter</li>
	 *	<li>Class&lt;? extends INodeContentRenderer&lt;T&gt;&gt;: the class to use to render the content of the column.</li>
	 *	<li>INodeContentRenderer&lt;T&gt;: an instance of a node renderer to use to render the content of the column.</li>
	 *	<li>BasicRowRenderer.NOWRAP: forces a 'nowrap' on the column</li>
	 * </ul>
	 *
	 * @param clz
	 * @param cols
	 * <X, C extends IConverter<X>, R extends INodeContentRenderer<X>>
	 */
	@SuppressWarnings("fallthrough")
	public BasicRowRenderer<T> addColumns(final Object... cols) throws Exception {
		check();
		if(cols == null || cols.length == 0)
			throw new IllegalStateException("The list-of-columns is empty or null; I need at least one column to continue.");
		String property = null;
		String width = null;
		IConverter< ? > conv = null;
		Class< ? > convclz = null;
		String caption = null;
		String cssclass = null;
		boolean nowrap = false;
		SortableType sort = null;
		INodeContentRenderer< ? > nodeRenderer = null;
		Class< ? > nrclass = null;
		ICellClicked< ? > clickHandler = null;

		for(final Object val : cols) {
			if(property == null) { // Always must start with a property.
				if(!(val instanceof String))
					throw new IllegalArgumentException("Expecting a 'property' path expression, not a " + val);
				property = (String) val;
			} else if(NOWRAP == val) {
				nowrap = true;
			} else if(val instanceof String) {
				final String s = (String) val;
				final char c = s.length() == 0 ? 0 : s.charAt(0); // The empty string is used to denote a node renderer that takes the entire record as a parameter
				switch(c){
					default:
						if(!Character.isLetter(c))
							throw new IllegalArgumentException("Unexpected 'string' parameter: '" + s + "'");
						//-- FALL THROUGH
					case 0:
						internalAddProperty(property, width, conv, convclz, caption, cssclass, nodeRenderer, nrclass, nowrap, sort, clickHandler);
						property = s;
						width = null;
						conv = null;
						convclz = null;
						caption = null;
						cssclass = null;
						nodeRenderer = null;
						nrclass = null;
						nowrap = false;
						sort = null;
						break;

					case '%':
						//-- Width specification, in percents;
						width = s.substring(1) + "%";
						break;
					case '$':
						cssclass = s.substring(1);
						break;
					case '^':
						caption = DomUtil.nlsLabel(s.substring(1));
						break;
				}
			} else if(val instanceof IConverter< ? >)
				conv = (IConverter< ? >) val;
			else if(val instanceof INodeContentRenderer< ? >)
				nodeRenderer = (INodeContentRenderer< ? >) val;
			else if(val instanceof ICellClicked< ? >)
				clickHandler = (ICellClicked< ? >) val;
			else if(val instanceof Class< ? >) {
				final Class< ? > c = (Class< ? >) val;
				if(INodeContentRenderer.class.isAssignableFrom(c))
					nrclass = c;
				else if(IConverter.class.isAssignableFrom(c))
					convclz = c;
				else
					throw new IllegalArgumentException("Invalid 'class' argument: " + c);
			} else if(val instanceof SortableType) {
				sort = (SortableType) val;
			} else
				throw new IllegalArgumentException("Invalid column modifier argument: " + val);
		}
		internalAddProperty(property, width, conv, convclz, caption, cssclass, nodeRenderer, nrclass, nowrap, sort, clickHandler);
		return this;
	}

	private INodeContentRenderer< ? > tryRenderer(final INodeContentRenderer< ? > nodeRenderer, final Class< ? > nrclass) throws Exception {
		if(nodeRenderer != null) {
			if(nrclass != null)
				throw new IllegalArgumentException("Both a NodeContentRenderer instance AND a class specified: " + nodeRenderer + " + " + nrclass);
			return nodeRenderer;
		}
		if(nrclass == null)
			return null;
		return (INodeContentRenderer< ? >) DomApplication.get().createInstance(nrclass);
	}

	/**
	 *
	 * @param <X>
	 * @param <T>
	 * @param cclz
	 * @param ins
	 * @return
	 * <X, T extends IConverter<X>>
	 */
	@SuppressWarnings("unchecked")
	private IConverter< ? > tryConverter(final Class< ? > cclz, final IConverter< ? > ins) {
		if(cclz != null) {
			if(ins != null)
				throw new IllegalArgumentException("Both a IConverter class AND an instance specified: " + cclz + " and " + ins);
			return ConverterRegistry.getConverterInstance((Class) cclz);
		}
		return ins;
	}

	/**
	 * Internal worker to add a field using the specified optional modifiers.
	 * @param property
	 * @param width
	 * @param conv
	 * @param convclz
	 * @param caption
	 * @param cssclass
	 * @param nodeRenderer
	 * @param nrclass
	 * <X, C extends IConverter<X>, R extends INodeContentRenderer<X>>
	 */
	private void internalAddProperty(final String property, final String width, final IConverter< ? > conv, final Class< ? > convclz, final String caption, final String cssclass,
		final INodeContentRenderer< ? > nodeRenderer, final Class< ? > nrclass, final boolean nowrap, SortableType sort, ICellClicked< ? > clickHandler) throws Exception {
		if(property == null)
			throw new IllegalStateException("? property name is empty?!");

		/*
		 * If this is propertyless we need to add a column directly, and use it to assign to.
		 */
		if(property.length() == 0) {
			final SimpleColumnDef cd = new SimpleColumnDef();
			m_columnList.add(cd);
			cd.setColumnLabel(caption);
			cd.setColumnType(getActualClass()); // By definition, the data value is the record instance,
			cd.setContentRenderer(tryRenderer(nodeRenderer, nrclass));
			cd.setPropertyName("");
			cd.setPresentationConverter(tryConverter(convclz, conv));
			cd.setWidth(width);
			cd.setCssClass(cssclass);
			cd.setNowrap(nowrap);
			if(sort != null)
				cd.setSortable(sort);
			if(clickHandler != null) {
				cd.setCellClicked(clickHandler);
			}
			return;
		}

		//-- Property must refer a property, so get it;
		final ClassMetaModel cmm = model();
		final PropertyMetaModel pmm = cmm.findProperty(property);
		if(pmm == null)
			throw new IllegalArgumentException("Undefined property path: '" + property + "' in classModel=" + cmm);

		//-- If a NodeRenderer is present we always use that, so property expansion is unwanted.
		final INodeContentRenderer< ? > ncr = tryRenderer(nodeRenderer, nrclass);
		if(ncr != null) {
			final SimpleColumnDef cd = new SimpleColumnDef();
			m_columnList.add(cd);
			cd.setValueTransformer(pmm.getAccessor());
			cd.setColumnLabel(caption == null ? pmm.getDefaultLabel() : caption);
			cd.setColumnType(pmm.getActualType());
			cd.setContentRenderer(tryRenderer(nodeRenderer, nrclass));
			cd.setPropertyName(property);
			cd.setPresentationConverter(tryConverter(convclz, conv)); // FIXME Not used as per the definition on content renderers??
			cd.setWidth(width);
			cd.setCssClass(cssclass);
			cd.setNowrap(nowrap);
			if(sort != null)
				cd.setSortable(sort);
			if(pmm.getNumericPresentation() != null && pmm.getNumericPresentation() != NumericPresentation.UNKNOWN) {
				cd.setCssClass("ui-numeric");
				cd.setHeaderCssClass("ui-numeric");
			}
			if(clickHandler != null) {
				cd.setCellClicked(clickHandler);
			}
			return;
		}

		//-- This is a property to display. Expand it into DisplayProperties to get the #of columns to append.
		final ExpandedDisplayProperty xdpt = ExpandedDisplayProperty.expandProperty(pmm);
		final List<ExpandedDisplayProperty> flat = new ArrayList<ExpandedDisplayProperty>();
		ExpandedDisplayProperty.flatten(flat, xdpt); // Expand any compounds;

		//-- If we have >1 columns here we cannot apply many of the parameters, so error on them
		if(flat.size() > 1) {
			if(width != null)
				throw new IllegalStateException("Cannot apply a WIDTH to a multicolumn property: " + pmm);
			if(conv != null || convclz != null)
				throw new IllegalStateException("Cannot apply an IConverter to a multicolumn property: " + pmm);
			if(caption != null)
				throw new IllegalStateException("Cannot apply a caption to a multicolumn property: " + pmm);
		}

		//-- And finally: add all columns ;-)
		for(final ExpandedDisplayProperty xdp : flat) {
			if(xdp.getName() == null)
				throw new IllegalStateException("All columns MUST have some name");

			//-- Create a column def from the metadata
			final SimpleColumnDef scd = new SimpleColumnDef(xdp);
			m_columnList.add(scd);
			scd.setDisplayLength(xdp.getDisplayLength());
			if(width != null)
				scd.setWidth(width);
			if(cssclass != null)
				scd.setCssClass(cssclass);
			scd.setColumnLabel(caption == null ? xdp.getDefaultLabel() : caption);
			scd.setColumnType(xdp.getActualType());
			scd.setValueTransformer(xdp.getAccessor()); // Thing which can obtain the value from the property
			scd.setPresentationConverter(tryConverter(convclz, conv));
			if(scd.getPresentationConverter() == null && xdp.getConverter() != null)
				scd.setPresentationConverter(xdp.getConverter());
			if(scd.getPresentationConverter() == null) {
				/*
				 * Try to get a converter for this, if needed.
				 */
				if(xdp.getActualType() != String.class) {
					final IConverter<?> c = ConverterRegistry.getConverter(xdp.getActualType(), xdp);
					scd.setPresentationConverter(c);
				}
			}
			if(sort != null)
				scd.setSortable(sort);
			else
				scd.setSortable(xdp.getSortable());
			scd.setPropertyName(xdp.getName());
			scd.setNowrap(nowrap);
			scd.setNumericPresentation(xdp.getNumericPresentation());
			if(scd.getNumericPresentation() != null && scd.getNumericPresentation() != NumericPresentation.UNKNOWN) {
				scd.setCssClass("ui-numeric");
				scd.setHeaderCssClass("ui-numeric");
			}
			if(clickHandler != null) {
				scd.setCellClicked(clickHandler);
			}
		}
	}

	/**
	 * Add all of the columns as defined by the metadata to the list.
	 */
	public void addDefaultColumns() {
		final ClassMetaModel cmm = model();
		final List<DisplayPropertyMetaModel> dpl = cmm.getTableDisplayProperties();
		if(dpl.size() == 0)
			throw new IllegalStateException("The list-of-columns to show is empty, and the class " + getActualClass()
				+ " has no @MetaObject definition defining a set of columns as default table columns, so there.");
		List<ExpandedDisplayProperty> xdpl = ExpandedDisplayProperty.expandDisplayProperties(dpl, cmm, null);
		xdpl = ExpandedDisplayProperty.flatten(xdpl); // Flatten the list: expand any compounds.
		for(final ExpandedDisplayProperty xdp : xdpl) {
			SimpleColumnDef scd = new SimpleColumnDef(xdp);
			if(scd.getNumericPresentation() != null && scd.getNumericPresentation() != NumericPresentation.UNKNOWN) {
				scd.setCssClass("ui-numeric");
				scd.setHeaderCssClass("ui-numeric");
			}

			m_columnList.add(scd);
		}
	}

	/**
	 * Complete this object if it is not already complete.
	 */
	@Override
	protected void complete(final TableModelTableBase<T> tbl) {
		if(isComplete())
			return;

		//-- If we have no columns at all we use a default column list.
		if(m_columnList.size() == 0)
			addDefaultColumns();

		//-- Is there a default sort thingy? Is that column present?
		if(m_sortColumnName != null) {
			for(final SimpleColumnDef scd : m_columnList) {
				if(scd.getPropertyName().equals(m_sortColumnName)) {
					setSortColumn(scd);
					break;
				}
			}
		}

		/*
		 * Width calculations. This tries to assign widths to columns that have no explicit width assigned. It starts
		 * by calculating all assigned widths in percents and in pixels. It then calculates widths for the columns that
		 * have no widths assigned.
		 */
		//-- Loop 1: calculate current size allocations for columns that have a width assigned.
		int totpct = 0;
		int totpix = 0;
		int ntoass = 0; // #columns that need a width
		int totdw = 0; // Total display width of all unassigned columns.
		for(final SimpleColumnDef scd : m_columnList) {
			if(scd.getWidth() == null || scd.getWidth().length() == 0) {
				ntoass++;
				totdw += scd.getDisplayLength();
			} else {
				final String s = scd.getWidth().trim();
				if(s.endsWith("%")) {
					final int w = StringTool.strToInt(s.substring(0, s.length() - 1), -1);
					if(w == -1)
						throw new IllegalArgumentException("Invalid width percentage: " + s + " for presentation column " + scd.getPropertyName());
					totpct += w;
				} else {
					//-- Should be numeric width, in pixels,
					final int w = StringTool.strToInt(s, -1);
					if(w == -1)
						throw new IllegalArgumentException("Invalid width #pixels: " + s + " for presentation column " + scd.getPropertyName());
					totpix += w;
				}
			}
		}

		//-- Is there something to assign, and are the numbers reasonable? If so calculate...
		final int pixwidth = 1280;
		if(ntoass > 0 && totpct < 100 && totpix < pixwidth) {
			int pctleft = 100 - totpct; // How many percents left?
			if(pctleft == 100 && totpix > 0) {
				//-- All widths assigned in pixels... Calculate a percentage of the #pixels left
				pctleft = (100 * (pixwidth - totpix)) / pixwidth;
			}

			//-- Reassign the percentage left over all unassigned columns.
			for(final SimpleColumnDef scd : m_columnList) {
				if(scd.getWidth() == null || scd.getWidth().length() == 0) {
					//-- Calculate a size factor, then use it to assign
					final double fact = (double) scd.getDisplayLength() / (double) totdw;
					final int pct = (int) (fact * pctleft + 0.5);
					scd.setWidth(pct + "%");
				}
			}
		}

		super.complete(tbl);
	}

}
