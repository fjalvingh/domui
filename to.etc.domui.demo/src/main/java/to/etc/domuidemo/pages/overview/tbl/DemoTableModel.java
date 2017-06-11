package to.etc.domuidemo.pages.overview.tbl;

import java.io.*;
import java.text.*;
import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;
import to.etc.util.*;

/**
 * Displays a DataTable.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class DemoTableModel extends UrlPage {
	DefaultTableModel<File>	m_model;

	File[]					m_rndsource;

	public DemoTableModel() {
		List<File>	l = findFiles(new File("/"));
		m_model = new FileModel(l);

		File f = File.separatorChar == '/' ? new File("/usr/bin") : new File("C:/windows");
		m_rndsource = f.listFiles();
	}

	private List<File>	findFiles(File in) {
		List<File> res = new ArrayList<File>();
		int count = 0;
		for(File f : in.listFiles()) {
			if(! f.getName().startsWith("."))
				res.add(f);
			if(count++ >= 5)
				break;
		}
		return res;
	}

	@Override
	public void createContent() throws Exception {

		IRowRenderer<File> rr = new IRowRenderer<File>() {
			/**
			 * Render [size], [last-modified], [filename]
			 * @see to.etc.domui.component.tbl.IRowRenderer#renderRow(to.etc.domui.component.tbl.ColumnContainer, int, java.lang.Object)
			 */
			@Override
			public void renderRow(TableModelTableBase<File> tbl, ColumnContainer<File> cc, int index, File f) throws Exception {
				cc.add(StringTool.strSize(f.length()));
				cc.add(DateFormat.getDateTimeInstance().format(new Date(f.lastModified())));
				TD v = cc.add(f.getName());
				v.setClicked(new IClicked<TD>() {
					@Override
					public void clicked(TD b) throws Exception {
						changeTd(b);
					}
				});
			}

			@Override
			public void renderHeader(TableModelTableBase<File> tbl, HeaderContainer<File> cc) throws Exception {
				cc.add("Size");
				cc.add("Last modified");
				cc.add("Filename");
			}

			@Override
			public void beforeQuery(TableModelTableBase<File> tbl) throws Exception {
			// TODO Auto-generated method stub

			}

			@Override
			public ICellClicked<File> getRowClicked() {
				return null;
			}
		};

		add(new Caption("Table and TableModel example"));

		Div d = new Div();
		add(d);
		DefaultButton	ib = new DefaultButton("Add last");
		d.add(ib);
		ib.setClicked(new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				m_model.add(rndFile());					// Add @ end
			}
		});
		ib = new DefaultButton("Add First");
		d.add(ib);
		ib.setClicked(new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				m_model.add(0, rndFile());				// Add @ start
			}
		});
		ib = new DefaultButton("Delete First");
		d.add(ib);
		ib.setClicked(new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				m_model.delete(0);
			}
		});
		ib = new DefaultButton("Delete Last");
		d.add(ib);
		ib.setClicked(new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				m_model.delete(m_model.getRows()-1);
			}
		});
		ib = new DefaultButton("Swap");
		d.add(ib);
		ib.setClicked(new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				if(m_lastTR != null) {
					TBody tb = m_lastTR.getParent(TBody.class);
					int row = tb.findChildIndex(m_lastTR);
					if(row == 0)
						row++;
					if(row < m_model.getRows()) {
						m_model.move(row-1, row);
					}
				}
			}
		});


		DataTable<File> dt = new DataTable<File>(m_model, rr);
		add(dt);
	}

	public File	rndFile() {
		File f = m_rndsource[(int)(Math.random() * m_rndsource.length)];
		System.out.println("************* Adding "+f);
		return f;
	}

	public TR		m_lastTR;

	public void changeTd(TD b) {
		b.setBackgroundColor("#0000ff");
		b.getTable().setTableBorder(2);
		m_lastTR = b.getParent(TR.class);
	}
}
