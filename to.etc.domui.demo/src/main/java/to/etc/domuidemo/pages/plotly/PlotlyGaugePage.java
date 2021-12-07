package to.etc.domuidemo.pages.plotly;

import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component.plotly.PlotlyGraph;
import to.etc.domui.component.plotly.traces.GaugeDataSource;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlotlyGaugePage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		PlotlyGraph.initialize(this);
		add(new HTag(1, "Gauges"));

		PlotlyGraph graph = new PlotlyGraph();
		add(graph);
		graph.setDisplay(DisplayType.INLINE_BLOCK);
		graph.setHeight("300px");
		graph.setWidth("300px");
		graph.setSource(new GaugeDataSource(98, "Valid"));

		add(new VerticalSpacer(20));
		add(new HTag(2, "And a more formatted one"));
		graph = new PlotlyGraph();
		add(graph);
		graph.setDisplay(DisplayType.INLINE_BLOCK);
		graph.setHeight("500px");
		graph.setWidth("500px");
		GaugeDataSource g = new GaugeDataSource(420, "Speed");
		g.gauge().axis()
			.range(0, 500)
			.tickWidth(1)
			.tickColor("darkblue");
		g.gauge().bar().color("darkblue");
		g.gauge()
			.bgColor("white")
			.borderWidth(2)
			.borderColor("gray")
			.step(0, 250, "cyan")
			.step(250, 400, "royalblue")
		;
		g.gauge().threshold()
			.thickness(0.75)
			.value(490)
			.line().color("red").width(4)
			;

		graph.setSource(g);

	}

}
