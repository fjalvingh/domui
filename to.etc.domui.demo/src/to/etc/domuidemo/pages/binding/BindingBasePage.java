package to.etc.domuidemo.pages.binding;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.input.*;
import to.etc.domui.converter.*;
import to.etc.domui.databinding.*;
import to.etc.domui.databinding.observables.*;
import to.etc.domui.dom.html.*;
import to.etc.domuidemo.db.*;

public class BindingBasePage extends UrlPage {
	@Nonnull
	final private ObservableValue<Long> m_durationO = new ObservableValue<Long>(Long.class);

	@Override
	public void createContent() throws Exception {
		Text<Long> durtext = new Text<Long>(Long.class);
		durtext.setConverter(new MsDurationConverter());					// This converter requires a special format, causing conversion errors on bad input
		durtext.setMandatory(true);

		Label lbl = new Label(durtext, "Input a duration");
		add(lbl);
		add(durtext);

		add(new DefaultButton("Click", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				hoppa();
			}
		}));
		BindingContext	bc = new BindingContext();
		bc.joinbinding(m_durationO, durtext, "value");

		Artist art = getSharedContext().get(Artist.class, Long.valueOf(58));

		Div d = new Div();
		add(d);
		Label artl = new Label("Artist: " + art.getName());
		d.add(artl);

	}

	private void hoppa() throws Exception {

	}
}
