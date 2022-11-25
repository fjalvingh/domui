package to.etc.domuidemo.pages.special.ace;

import to.etc.domui.component.ace.AceEditor;
import to.etc.domui.component.ace.AceEditor.Completion;
import to.etc.domui.component.ace.PositionCalculator;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.FileTool;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 23-12-17.
 */
public class AcePage extends UrlPage {
	@Override public void createContent() throws Exception {
		AceEditor.initialize(this);

		AceEditor editor = new AceEditor();
		editor.setMode("ace/mode/pgsql");
		editor.setTheme("iplastic");
		//editor.setTheme("iplastic");
		editor.setHeight("200px");
		editor.setWidth("600px");
		add(editor);

		String text = FileTool.readResourceAsString(getClass(), "demojs.js", "utf-8");
		editor.setValue(text);

		editor.setCompletionHandler(this::completeCode);


		ButtonBar bb = new ButtonBar();
		add(bb);
		bb.addButton("SetText", a -> {
			String t2 = FileTool.readResourceAsString(getClass(), "demo2js.js", "utf-8");
			editor.setValue(t2);
		});

		bb.addButton("Clear", a -> editor.setValue(null));

		bb.addButton("Toggle RO", a -> editor.setReadOnly(! editor.isReadOnly()));

		bb.addButton("Show Value", a -> MsgBox.info(this, editor.getValue()));

		bb.addButton("Mark 'var'", a -> {
			markVars(editor);
		});

		bb.addButton("Clear Markers", a -> editor.markerClear());
	}

	private void markVars(AceEditor ae) {
		String value = ae.getValue();
		if(null == value)
			return;

		ae.markerClear();
		PositionCalculator pc = new PositionCalculator(value);
		Point p = new Point();
		Point ep= new Point();
		int ix = 0;
		int len = value.length();
		while(ix < len) {
			int pos = value.indexOf("var", ix);
			if(pos == -1)
				break;

			pc.getXYPosition(p, pos);					// Convert offset into x, y coordinates
			pc.getXYPosition(ep, pos + 3);		// End of the word "var"
			ae.markerAdd(MsgType.WARNING, p.y, p.x, ep.y, ep.x, "Use 'let' instead of 'var', it's safer");

			ix = pos + 3;
		}
	}

	private List<Completion> completeCode(String text, int row, int col, String prefix) {
		String[] split = text.split("\\W+");				// All words in the document
		Set<String> set = new HashSet<>(Arrays.asList(split));

		//-- Now find all words starting with prefix
		String prefixLC = prefix.toLowerCase();
		List<Completion> res = set.stream()
			.filter(a -> a.toLowerCase().contains(prefixLC))
			.map(a -> new Completion(a, a, "Word", 10))
			.collect(Collectors.toList());
		//res.forEach(a -> System.out.println(a.getName()));

		return res;
	}
}
