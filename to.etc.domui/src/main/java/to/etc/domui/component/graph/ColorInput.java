package to.etc.domui.component.graph;

import to.etc.domui.component.input.Text;

/**
 * Input of HTML5 type color.
 */
public class ColorInput extends Text<String> {

	public ColorInput() {
		super(String.class);
		setValue("#ffffff");
		setInputType("color");
	}

}
