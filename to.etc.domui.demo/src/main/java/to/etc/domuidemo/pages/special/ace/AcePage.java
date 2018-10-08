package to.etc.domuidemo.pages.special.ace;

import to.etc.domui.component.ace.AceEditor;
import to.etc.domui.component.ace.AceEditor.Completion;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.FileTool;

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
		editor.setTheme("ace/theme/iplastic");
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

		bb.addButton("Show Val", a -> MsgBox.info(this, editor.getValue()));
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
