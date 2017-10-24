package to.etc.domuidemo.pages.overview.misc;

import to.etc.domui.component.headers.GenericHeader;
import to.etc.domui.component.headers.GenericHeader.Type;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.misc.MsgBoxButton;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domuidemo.pages.Lorem;

public class DemoMsgBox extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new GenericHeader(Type.HEADER_1, "Message box variants"));

		ButtonBar bb = new ButtonBar();
		cp.add(bb);

		bb.addButton("Information", "img/btnSmileyQuestion.gif", clickednode -> MsgBox.message(this, MsgBox.Type.INFO, "Information message"));
		bb.addButton("Warning", "img/btnSmileyTongue.gif", clickednode -> MsgBox.message(this, MsgBox.Type.WARNING, "Warning message"));
		bb.addButton("Error", "img/btnSmileySad.gif", clickednode -> MsgBox.message(this, MsgBox.Type.ERROR, "Error message"));

		bb.addButton("Continuation", "img/btnSmileyWink.png",
			clickednode -> MsgBox.continueCancel(DemoMsgBox.this, "Click Continuation or Cancel<BR/><BR/>(Cancel has no action)", (IClicked<MsgBox>) clickednode1 -> {
				MsgBox.info(this, "You chose continue");
			}));

		bb.addButton("Yes/No", "img/btnReload.gif", clickednode -> MsgBox.yesNo(DemoMsgBox.this, "Click Yes or No<BR/><BR/>(No has no action)", (IClicked<MsgBox>) clickednode1 -> {
			MsgBox.info(this, "You chose YES");
		}));

		cp.add(new VerticalSpacer(20));
		cp.add(new GenericHeader(Type.HEADER_1, "More complex answer handling"));
		bb = new ButtonBar();
		cp.add(bb);


		bb.addButton("Yes/No", "img/btnSmileyGrin.gif", clickednode -> {
			MsgBox.IAnswer onDeleteHandler = result -> {
				if(result == MsgBoxButton.YES) {
					MsgBox.info(this, "==> You choose Yes");
				} else if(result == MsgBoxButton.NO) {
					MsgBox.info(this, "==> You choose No");
				}
			};
			MsgBox.yesNo(DemoMsgBox.this, "Answer handling on both<BR/>", onDeleteHandler);
		});

		bb.addButton("Yes/No/Cancel", "img/btnSmileySing.gif", clickednode -> {
			MsgBox.IAnswer onSaveConfirmHandler = result -> {
				if(result == MsgBoxButton.YES) {
					MsgBox.info(this, "==> You choose Yes");
				} else if(result == MsgBoxButton.NO) {
					MsgBox.info(this, "==> You choose No");
				} else if(result == MsgBoxButton.CANCEL) {
					MsgBox.info(this, "==> You choose Cancel");
				}
			};
			MsgBox.yesNoCancel(DemoMsgBox.this, "Answer handling on all of them", onSaveConfirmHandler);
		});


		cp.add(new VerticalSpacer(20));
		cp.add(new GenericHeader(Type.HEADER_1, "Content area sizing"));
		bb = new ButtonBar();
		cp.add(bb);

		bb.addButton("Big content", FaIcon.faHandODown, a -> {
			MsgBox.info(this, Lorem.getSentences(8192));
		});

		bb.addButton("Simple markup", FaIcon.faExpand, a -> {
			//MsgBox.info(this, "You should not that not <b>all</b> people are good, <ul><li>Not evil ones</li><li>Not even Java's architects</li></ul>");
			MsgBox.info(this, "You should know that not <b>all</b> people are good");
		});

	}
}
