package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TH;
import to.etc.domui.dom.html.THead;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Recognizes a data table, and gives accessors for the column things inside it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21yhggg.
 */
final public class PogDataTable extends AbstractPoProxyGenerator implements IPoAcceptNullTestid {
	static private final Pair<String, String> COLUMNCLASS = new Pair<>(PROXYPACKAGE, "CpDataTableColumn");

	private List<Col> m_colList = new ArrayList<>();

	private String m_baseName;

	public PogDataTable(NodeBase node) {
		super(node);
	}

	@Override
	public void generateCode(PoGeneratorContext context, PoClass pc, String baseName) throws Exception {
		//-- Generate a row class
		String rowClassName = context.getRootClass().getClassName() + m_baseName + "Row";
		PoClass rowClass = context.addClass(rowClassName, null, Collections.emptyList());

		//-- Generate the table class
		PoClass baseClass = new PoClass(PROXYPACKAGE, "CpDataTableBase");
		baseClass.addGenericParameter(rowClass);

		String tableClassName = context.getRootClass().getClassName() + m_baseName;
		PoClass tableClass = context.addClass(new PoClass(context.getRootClass().getPackageName(), tableClassName, baseClass));

		//-- Generate thingies per column
		for(int i = 0; i < m_colList.size(); i++) {
			Col col = m_colList.get(i);
			generateTableClassColumn(context, tableClass, col, i);
			generateRowClassColumn(context, rowClass, col, i);
		}
	}

	/**
	 * Create a base name in case the testid is null.
	 */
	@NonNull
	@Override
	public String getProposedBaseName(PoGeneratorContext context, NodeBase node) {
		String baseName = m_baseName = m_node.getTestID() == null
			? "Tbl" + context.nextCounter()
			: context.getRootClass().getBaseName(m_node.getTestID());
		return baseName;
	}

	/**
	 * Generate the column accessor(s) for a single column. The accessor accesses whatever is hidden in the cell.
	 * We can have multiple controls inside a column, and a separate accessor method will be generated for each of
	 * them as good as it gets.
	 */
	private void generateRowClassColumn(PoGeneratorContext context, PoClass pc, Col col, int index) throws Exception {
		String baseName = col.getColumnName();

		List<IPoProxyGenerator> controlList = col.getContentModelList();
		if(controlList.size() == 0) {
			context.error(m_node, "Column " + col + " has no content model, it is not generated");
			return;
		}

		boolean uniqueNames = hasUniqueNames(controlList);

		for(int i = 0; i < controlList.size(); i++) {
			IPoProxyGenerator pg = controlList.get(i);

			/*
			 * Calculate a name as follows:
			 * If we are the ONLY control in the cell: use the name of the column, nothing more (i.e. col2).
			 * If we are the nth but names are unique: use column name + control identifier (i.e. col2Button).
			 * If names are not unique then use column name + control identifier + sequence #.
			 */
			String controlBaseName;
			if(controlList.size() == 1) {
				controlBaseName = baseName;
			} else if(uniqueNames) {
				controlBaseName = pg.identifier();
			} else {
				controlBaseName = baseName + pg.identifier() + index;
			}

			generateCellControl(context, pc, col, index, pg, controlBaseName);
		}
	}

	private void generateCellControl(PoGeneratorContext context, PoClass pc, Col col, int index, IPoProxyGenerator pg, String baseName) throws Exception {
		pg.generateCode(context, pc, baseName);
	}

	/**
	 * Return the selector expression needed for the control proxy for a control
	 * inside a table cell. The selector asks the row for a base selector.
	 * FIXME We need a way to uniquely identify a control inside a cell using its testID.
	 */
	private String getCellControlSelector(Col col, IPoProxyGenerator pg) {
		return "() -> columnControlSelector(" + col.getIndex() + ", \"fixme-control-testid-in-row-" + pg + "\")";
	}

	private boolean hasUniqueNames(List<IPoProxyGenerator> list) {
		Set<String> nameSet = new HashSet<>();
		for(IPoProxyGenerator pg : list) {
			if(! nameSet.add(pg.identifier()))
				return false;
		}
		return true;
	}

	/**
	 * Generates a column accessor in the class, i.e. column[name], returning the general things that can be done with a column.
	 */
	private void generateTableClassColumn(PoGeneratorContext context, PoClass pc, Col col, int index) throws Exception {
		String fieldName = PoGeneratorContext.fieldName(col.getColumnName());
		String methodName = PoGeneratorContext.methodName(col.getColumnName());

		PoField field = pc.addField(COLUMNCLASS, fieldName);
		PoMethod getter = pc.addMethod(field.getType(), methodName);
		getter.appendLazyInit(field, variable -> {
			getter.append(variable).append(" = ").append("new ");
			getter.appendType(pc, field.getType()).append("(this, ").append(Integer.toString(index)).append(");").nl();
		});
	}


	/*----------------------------------------------------------------------*/
	/*	CODING:	Detection part - calculate how it looks						*/
	/*----------------------------------------------------------------------*/

	/**
	 * Detect the structure of the datatable, its columns, and what is in each column and row.
	 */
	@Override
	public boolean acceptChildren(PoGeneratorContext context) throws Exception {
		NodeContainer nc = (NodeContainer) m_node;
		List<Table> tables = nc.getChildren(Table.class);
		if(tables.size() == 0) {
			context.error(m_node, "The data table is empty; fill it to generate its content model");
			return false;
		} else if(tables.size() > 1) {
			context.error(m_node, "?? > 1 table in data table??");
		}
		Table tbl = tables.get(0);

		List<THead> heads = tbl.getChildren(THead.class);
		if(heads.size() > 1) {
			context.error(m_node, "Too many THEAD items in DataTable");
		} else if(heads.size() == 0) {
			context.error(m_node, "No data in table, cannot really do anything");
			return false;
		}
		THead head = heads.get(0);
		m_colList = scanColumnNames(context, head);
		if(m_colList.size() == 0) {
			context.error(m_node, "No columns recognized");
			return false;
		}

		//-- Ok, we have columns; now try to calculate a content model for each column by looking at all rows.
		TBody body = tbl.getBody();

		//-- Scan the content model for each cell and collect them per column.
		boolean warned = false;
		for(TR row : body.getChildren(TR.class)) {
			List<TD> children = row.getChildren(TD.class);
			for(int i = 0; i < children.size(); i++) {
				TD td = children.get(i);

				if(i >= m_colList.size()) {
					//-- We have a td without a th -> bugger. For now error it and ignore.
					if(! warned) {
						warned = true;
						context.error(m_node, "Row has a column (index=" + i + ") for which we have no header - ignoring that column");
					}
				} else {
					scanContentModel(context, td, m_colList.get(i));
				}
			}
		}

		//-- Now, for each column, try to find out the final content model.
		for(Col col : m_colList) {
			calculateCellContentModel(context, col);
		}
		return true;
	}

	/**
	 * This code assumes more or less the same content model in each cell, and assumes
	 * similar pg's would be the same in the cell. This is error prone, and probably should
	 * be merged on a better testid (one that does not change per row).
	 */
	private void calculateCellContentModel(PoGeneratorContext context, Col col) throws Exception {
		Map<String, List<IPoProxyGenerator>> perTypeMap = new HashMap<>();

		for(List<IPoProxyGenerator> list : col.getPerRowContentList()) {
			for(IPoProxyGenerator pg : list) {
				perTypeMap.computeIfAbsent(pg.identifier(), a -> new ArrayList<>()).add(pg);
			}
		}
		if(perTypeMap.size() == 0) {
			//-- No content model found. Just get a "cell content" model.
			col.addContentModel(PoGeneratorRegistry.getDisplayTextGenerator(context, m_node));		// FIXME Node is odd
		} else {
			//-- Use the actual models
			for(List<IPoProxyGenerator> pgl : perTypeMap.values()) {
				col.addContentModel(pgl.get(0));
			}
		}
	}

	/**
	 * Scans the TD for components.
	 */
	private void scanContentModel(PoGeneratorContext context, TD td, Col col) throws Exception {
		List<NodeGeneratorPair> generators = context.createGenerators(td);
		col.addGeneratorSet(generators.stream().map(a -> a.getGenerator()).collect(Collectors.toList()));
	}

	private List<Col> scanColumnNames(PoGeneratorContext context, THead head) {
		List<Col> colList = new ArrayList<>();

		for(TR tr : head.getChildren(TR.class)) {
			int currentHeadIndex = 0;
			for(TH th : tr.getChildren(TH.class)) {
				currentHeadIndex = handleTh(colList, th, currentHeadIndex);

			}
		}

		//-- We should now have all columns in the table, with their headers. Calculate column names and indices for each.
		for(int i = 0; i < colList.size(); i++) {
			Col col = colList.get(i);
			col.setColumnName(calculateColumnName(col, i));
			col.setIndex(i);
		}
		return colList;
	}

	private String calculateColumnName(Col col, int index) {
		StringBuilder sb = new StringBuilder();
		for(TH th : col.getHeader()) {
			String tc = th.getTextOnly();
			if(null != tc && tc.length() > 0) {
				sb.append(tc);
			}
		}
		if(sb.length() > 2) {
			return PoGeneratorContext.makeName(sb.toString());
		}
		return "column" + (index + 1);
	}

	private int handleTh(List<Col> colList, TH th, int currentHeadIndex) {
		int colspan = th.getColspan();
		if(colspan <= 0)
			colspan = 1;

		for(int i = 0; i < colspan; i++) {
			while(colList.size() <= currentHeadIndex) {
				colList.add(new Col());
			}
			Col c = colList.get(currentHeadIndex);
			c.add(th);
			currentHeadIndex++;
		}
		return currentHeadIndex;
	}

	@Override
	public String identifier() {
		return "DataTable";
	}

	static private final class Col {
		private List<TH> m_header = new ArrayList<>(3);

		private String m_columnName;

		private List<List<IPoProxyGenerator>> m_perRowContentList = new ArrayList<>();

		private List<IPoProxyGenerator> m_contentModelList = new ArrayList<>();

		private int m_index;

		public void add(TH th) {
			m_header.add(th);
		}

		public String getColumnName() {
			return m_columnName;
		}

		public void setColumnName(String columnName) {
			m_columnName = columnName;
		}

		public List<TH> getHeader() {
			return m_header;
		}

		public void addGeneratorSet(List<IPoProxyGenerator> generators) {
			m_perRowContentList.add(generators);
		}

		public List<List<IPoProxyGenerator>> getPerRowContentList() {
			return m_perRowContentList;
		}

		public void addContentModel(IPoProxyGenerator pg) {
			m_contentModelList.add(pg);
		}

		public List<IPoProxyGenerator> getContentModelList() {
			return m_contentModelList;
		}

		public int getIndex() {
			return m_index;
		}

		public void setIndex(int index) {
			m_index = index;
		}
	}
}
