package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.*;

public class DemoRadioButton extends UrlPage {
	private enum Gender {
		MALE, FEMALE, UNKNOWN
	}

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		final RadioGroup<Gender> g = new RadioGroup<Gender>();
		RadioButton<Gender> a = new RadioButton<Gender>(g, Gender.MALE);
		cp.add(a);
		cp.add("Male");
		cp.add(new BR());

		RadioButton<Gender> b = new RadioButton<Gender>(g, Gender.FEMALE);
		cp.add(b);
		cp.add("Female");
		cp.add(new BR());

		RadioButton<Gender> c = new RadioButton<Gender>(g, Gender.UNKNOWN);
		cp.add(c);
		cp.add("Unknown");
		cp.add(new BR());

		g.setOnValueChanged(new IValueChanged<RadioGroup<Gender>>() {
			@Override
			public void onValueChanged(RadioGroup<Gender> component) throws Exception {
				DemoRadioButton.this.add(new MsgDiv("Selected a " + component.getValue()));
			}
		});


		cp.add(new DefaultButton("Change Group Value", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				Gender v = g.getValue();
				if(v == null)
					g.setValue(Gender.UNKNOWN);
				else {
					switch(v){
						case FEMALE:
							v = Gender.MALE;
							break;
						case MALE:
							v = Gender.UNKNOWN;
							break;
						case UNKNOWN:
							v = Gender.FEMALE;
							break;
					}
					g.setValue(v);
				}
			}
		}));
	}
}
