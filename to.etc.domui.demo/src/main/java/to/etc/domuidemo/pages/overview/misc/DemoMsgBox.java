package to.etc.domuidemo.pages.overview.misc;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoMsgBox extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);

		Label l = new Label("DemoMsgBox");
		d.add(l);
		DefaultButton db1 = new DefaultButton("Information", "img/btnSmileyQuestion.gif", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				MsgBox.message(d, MsgBox.Type.INFO, "Information message");
			}
		});
		d.add(db1);
		DefaultButton db2 = new DefaultButton("Warning", "img/btnSmileyTongue.gif", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				MsgBox.message(d, MsgBox.Type.WARNING, "Warning message");
			}
		});
		d.add(db2);
		DefaultButton db3 = new DefaultButton("Error", "img/btnSmileySad.gif", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				MsgBox.message(d, MsgBox.Type.ERROR, "Error message");
			}
		});
		d.add(db3);

		d.add(new VerticalSpacer(20));
		d.add("Messages with simple Dialog handling");
		final Div d1 = new Div();
		add(d1);
		DefaultButton db7 = new DefaultButton("Continuation", "img/btnSmileyWink.png", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				MsgBox.continueCancel(DemoMsgBox.this, "Click Continuation or Cancel<BR/><BR/>(Cancel has no action)", new IClicked<MsgBox>() {
					@Override
					public void clicked(MsgBox clickednode1) throws Exception {
						d1.add("==> You choose Continue");
					}
				});
			};
		});
		d1.add(db7);

		final Div d2 = new Div();
		add(d2);
		DefaultButton db4 = new DefaultButton("Yes/No", "img/btnReload.gif", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				MsgBox.yesNo(DemoMsgBox.this, "Click Yes or No<BR/><BR/>(No has no action)", new IClicked<MsgBox>() {
					@Override
					public void clicked(MsgBox clickednode1) throws Exception {
						d2.add("==> You choose Yes");
					}
				});
			}
		});
		d2.add(db4);

		Div d3 = new Div();
		add(d3);
		d3.add(new BR());
		d3.add("More complex answer handling");
		final Div d4 = new Div();
		add(d4);
		DefaultButton db5 = new DefaultButton("Yes/No", "img/btnSmileyGrin.gif", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				MsgBox.IAnswer onDeleteHandler = new MsgBox.IAnswer() {
					@Override
					public void onAnswer(MsgBoxButton result) throws Exception {
						if(result == MsgBoxButton.YES) {
							d4.add("==> You choose Yes");
						} else if(result == MsgBoxButton.NO) {
							d4.add("==> You choose No");
						}
					}
				};
				MsgBox.yesNo(DemoMsgBox.this, "Answer handling on both<BR/>", onDeleteHandler);
			}
		});
		d4.add(db5);

		final Div d5 = new Div();
		add(d5);
		DefaultButton db6 = new DefaultButton("Yes/No/Cancel", "img/btnSmileySing.gif", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				MsgBox.IAnswer onSaveConfirmHandler = new MsgBox.IAnswer() {
					@Override
					public void onAnswer(MsgBoxButton result) throws Exception {
						if(result == MsgBoxButton.YES) {
							d5.add("==> You choose Yes");
						} else if(result == MsgBoxButton.NO) {
							d5.add("==> You choose No");
						} else if(result == MsgBoxButton.CANCEL) {
							d5.add("==> You choose Cancel");
						}
					}
				};
				MsgBox.yesNoCancel(DemoMsgBox.this, "Answer handling on all of them", onSaveConfirmHandler);
			}
		});
		d5.add(db6);
	}
}
