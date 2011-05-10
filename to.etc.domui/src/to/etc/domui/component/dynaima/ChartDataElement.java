package to.etc.domui.component.dynaima;

public class ChartDataElement {
	private double value;
	private String label;
	
	public ChartDataElement(double value, String label) {
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
