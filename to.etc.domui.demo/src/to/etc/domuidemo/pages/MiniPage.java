package to.etc.domuidemo.pages;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;


public class MiniPage extends UrlPage {
	private int		m_id;
	private static int	m_nextid;

	public MiniPage() {
		m_id = nextID();
	}

	static synchronized int nextID() {
		return ++m_nextid;
	}

	@Override
	public void createContent() throws Exception {
		System.out.println("ClassLoader="+getClass().getClassLoader()+", inst="+this);
		Div	d	= new Div();
		add(d);
		d.setText("Input here:");
		final Text<Integer>	text = new Text<Integer>(Integer.class);
		d.add(text);
		text.setMandatory(true);

		DefaultButton	b = new DefaultButton("Press this", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton bah) throws Exception {
				Integer	val = text.getValue();
				Div nd = new Div();
				
				nd.setText("De waarde is "+val);
				MiniPage.this.add(nd);
			}
		});
		d.add(b);

		d.add(new DefaultButton("Rebuild", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton bx) throws Exception {
				MiniPage.this.forceRebuild();
			}
		}));
	}

	@Override
	public String toString() {
		return super.toString()+" #"+m_id;
	}
}
