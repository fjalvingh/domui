package to.etc.domuidemo.pages.overview.delayed;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.delayed.*;
import to.etc.domui.dom.html.*;

public class DemoAsyncContainer extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div d = new Div("The content will be calculated in a separate thread");
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
			public Div run(IProgress p) throws Exception {
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
				return new Div("De actie is afgerond!!!! Wat een mooi resultaat: 42");
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
