package to.etc.domui.component.dynaima;

/**
 * ChartField defines value of the field that will be displayed on the chart as well as it's label.
 *
 * @author <a href="mailto:nmaksimovic@execom.eu">Nemanja Maksimovic</a>
 * Created on 11 Jul 2011
 */
public class ChartField {

	private double value;
	private String label;
	
	public ChartField(double value, String label) {
		super();
		this.value = value;
		this.label = label;
	}
	public double getValue() {
		return value;
	}
	public String getLabel() {
		return label;
	}

}
