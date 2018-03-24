package to.etc.domui.component2.form4;

import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 6-3-18.
 */
public class TableFormLayouter implements IFormLayouter {
	@Nonnull
	final private FormBuilder.IAppender m_appender;

	private boolean m_horizontal;

	private Table m_table;

	private TBody m_body;

	private TR m_labelRow;

	private TR m_controlRow;

	public TableFormLayouter(@Nonnull FormBuilder.IAppender appender) {
		m_appender = appender;
	}

	@Override
	public void setHorizontal(boolean horizontal) {
		m_horizontal = horizontal;
	}

	@Override
	public void clear() {
		m_table = null;
		m_body = null;
		m_labelRow = null;
		m_controlRow = null;
	}

	@Nonnull
	public TBody body() {
		if(m_body == null) {
			Table tbl = m_table = new Table();
			m_appender.add(tbl);
			tbl.setCssClass(m_horizontal ? "ui-f4 ui-f4-h" : "ui-f4 ui-f4-v");
			tbl.setCellPadding("0");
			tbl.setCellSpacing("0");
			TBody b = m_body = new TBody();
			tbl.add(b);
			return b;
		}
		return m_body;
	}

	@Override
	public void addControl(NodeBase control, NodeContainer lbl, String controlCss, String labelCss, boolean append) {
		if(m_horizontal)
			addHorizontal(control, lbl, controlCss, labelCss, append);
		else
			addVertical(control, lbl, controlCss, labelCss, append);
	}

	private void addVertical(NodeBase control, NodeBase lbl, String controlCss, String labelCss, boolean append) {
		TBody b = body();
		if(append) {
			TD cell = b.cell();
			if(lbl != null) {
				lbl.addCssClass("ui-f4-lbl");
				lbl.setMarginLeft("10px");
				lbl.setMarginRight("3px");
				cell.add(lbl);
			}
			cell.add(control);
			if(null != controlCss)
				cell.addCssClass(controlCss);
		} else {
			TR row = b.addRow("ui-f4-row ui-f4-row-v");

			TD labelcell = b.addCell("ui-f4-lbl ui-f4-lbl-v");
			if(null != lbl)
				labelcell.add(lbl);
			if(labelCss != null)
				labelcell.addCssClass(labelCss);

			TD controlcell = b.addCell("ui-f4-ctl ui-f4-ctl-v");
			controlcell.add(control);

			if(null != controlCss)
				controlcell.addCssClass(controlCss);
		}
		if(lbl instanceof Label)
			((Label) lbl).setForTarget(control);
	}

	private void addHorizontal(NodeBase control, NodeBase lbl, String controlCss, String labelCss, boolean append) {
		TBody b = body();
		if(append) {
			TR row = controlRow();
			TD cell;
			if(row.getChildCount() == 0) {
				cell = row.addCell();
				cell.setCssClass("ui-f4-ctl ui-f4-ctl-h");
			} else {
				cell = (TD) row.getChild(row.getChildCount() - 1);
			}
			cell.add(control);

			if(null != controlCss)
				cell.addCssClass(controlCss);
		} else {
			TD labelcell = labelRow().addCell();
			labelcell.setCssClass("ui-f4-lbl ui-f4-lbl-h");
			if(null != lbl)
				labelcell.add(lbl);

			if(labelCss != null)
				labelcell.addCssClass(labelCss);

			TD controlcell = controlRow().addCell();
			controlcell.setCssClass("ui-f4-ctl ui-f4-ctl-h");
			controlcell.add(control);

			if(null != controlCss)
				controlcell.addCssClass(controlCss);
		}
		if(lbl instanceof Label) {
			((Label) lbl).setForTarget(control);
		}
	}

	@Nonnull
	private TR controlRow() {
		TR row = m_controlRow;
		if(null == row) {
			labelRow();
			row = m_controlRow = body().addRow("ui-f4-row ui-f4-row-h ui-f4-crow");
		}
		return row;
	}

	@Nonnull
	private TR labelRow() {
		TR row = m_labelRow;
		if(null == row) {
			row = m_labelRow = body().addRow("ui-f4-row ui-f4-row-h ui-f4-lrow");
		}
		return row;
	}
}
