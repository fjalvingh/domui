package to.etc.domuidemo.pages.graphs;

import java.awt.*;
import java.sql.*;

import to.etc.domui.component.dynaima.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

/**
 * Demo page for the dynamic image component. This merely displays a jGraphs graph.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public class GraphPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div	d	= new MsgDiv("Dynamische images: deze worden op de server gegenereerd via een factory");
		add(d);
		DynaIma		di	= new DynaIma();
		add(di);
		di.setBufferedSource(getHelpdeskSource());

		di	= new DynaIma();
		add(di);
		di.setBufferedSource(getDisSource());
	}

	static Color	makeColor(int i) {
		for(;;) {
			int j = (int)(Math.random() * (0xffffff+1));
			int r	= (j >> 16) & 0xff;
			int g	= (j >> 8) & 0xff;
			int b	= (j & 0xff);

			if(r+g+b < 70)
				continue;
			return new Color(r, g, b);
		}
	}

	private JGraphChartSource	getDisSource() throws Exception {
		return new JGraphChartSource() {
			@Override
			public void createGraph() throws Exception {
				QDataContext dc = getSharedContext();
				Connection	dbc	= dc.getConnection();
				PreparedStatement ps = null;
				ResultSet rs = null;
				try {
					ps	= dbc.prepareStatement(
						"select dds_source,count(*) from dis_documents group by dds_source"
					);
					rs	= ps.executeQuery();
					int ix = 0;
					ChartDimensions cd = new ChartDimensions(400, 350, 350);
					PieCharter pc = createPieChart(cd);
					while(rs.next()) {
						String lbl = rs.getString(1);
						double	val	= rs.getDouble(2);
						Color	col	= makeColor(ix++);
						pc.addChartField(new ChartField(val, lbl));

//						pc.addPoint(col, lbl, val);
					}
				} finally {
					try { if(rs != null) rs.close(); } catch(Exception x){}
					try { if(ps != null) ps.close(); } catch(Exception x){}
				}
			}
		};
	}

	private JGraphChartSource	getHelpdeskSource() throws Exception {
		return new JGraphChartSource() {
			@Override
			public void createGraph() throws Exception {
				QDataContext dc = getSharedContext();
				Connection	dbc	= dc.getConnection();
				PreparedStatement ps = null;
				ResultSet rs = null;
				try {
					//-- 1: all NEW calls added over the years
					ps	= dbc.prepareStatement(
						"select to_char(ktc_creation_date, 'YYYY'), count(*) from kbc_topics group by to_char(ktc_creation_date, 'YYYY')"
					);
					rs	= ps.executeQuery();
//					int ix = 0;
					ChartDimensions cd = new ChartDimensions(400, 350, 350);
					AreaCharter c = createAreaChart(cd, "Helpdesk: Gemeldde en opgeloste calls", "Jaar", "# calls");

					//-- Dataset 1.
					AreaCharter.DataSet	ds	= c.addDataSet(new Color( 153, 0, 255 ,100 ), "Gemeld");
					while(rs.next()) {
						int lbl = rs.getInt(1);						// Year as an int,
						double	val	= rs.getDouble(2);				// And the count,

						ds.add(Integer.toString(lbl), Integer.valueOf(lbl), val);
					}
					rs.close();
					ps.close();

					//-- 2: all FIXED calls over the years
					ps	= dbc.prepareStatement(
						"select to_char(ktc_last_modified, 'YYYY'), count(*) from kbc_topics where ktc_status = 'CLOSE' group by to_char(ktc_last_modified, 'YYYY')"
					);
					rs	= ps.executeQuery();

					//-- Dataset 2.
					ds	= c.addDataSet(new Color( 204,0,255, 150 ), "Opgelost");
					while(rs.next()) {
						int lbl = rs.getInt(1);						// Year as an int,
						double	val	= rs.getDouble(2);				// And the count,
						ds.add(Integer.toString(lbl), Integer.valueOf(lbl), val);
					}

					//-- Done.
				} finally {
					try { if(rs != null) rs.close(); } catch(Exception x){}
					try { if(ps != null) ps.close(); } catch(Exception x){}
				}
			}
		};
	}


}
