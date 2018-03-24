package to.etc.domuidemo.pages.overview.delayed;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.delayed.AsyncContainer;
import to.etc.domui.component.delayed.IActivity;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.Progress;

public class DemoAsyncContainer extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div d = new Div();
		d.setText("The content will be calculated in a separate thread");
		add(d);

		DefaultButton sb = new DefaultButton("Calculate", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				addDelayed();
			}
		});
		add(sb);
	}

	public void	addDelayed() {
		IActivity	act	= new IActivity() {
			@Override
			public Div run(Progress p) throws Exception {
				p.setTotalWork(10);
				for(int i = 0; i < 10; i++) {
					Thread.sleep(1500);
					p.setCompleted(i);
					System.out.println("Delayed action: count= "+i);
					if(p.isCancelled()) {
						System.out.println("Cancelled");
						return null;
					}
				}
				Div d = new Div();
				d.setText("De actie is afgerond!!!! Wat een mooi resultaat: 42");
				return d;
			}
		};

		AsyncContainer	asc = new AsyncContainer(act);
		asc.setHeight("200px");
		asc.setBorder(1);
		asc.setBorderColor("#ff0000");
		asc.setBorderStyle("solid");
		add(asc);
	}
}
