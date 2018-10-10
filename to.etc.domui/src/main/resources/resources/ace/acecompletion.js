var langTools = require("ace/ext/language_tools");
var completer = {
	getCompletions: function(editor, session, pos, prefix, callback) {
		let fields = {};
		fields[editor.__id + "_row"] = pos.row;
		fields[editor.__id + "_col"] = pos.column;
		fields[editor.__id + "_prefix"] = prefix;

		WebUI.jsoncall(editor.__id,fields, function(data) {
			callback(null, data);
		});
	}
};
langTools.setCompleters([completer]);		// GLOBAL!
ed.setOptions({
	enableBasicAutocompletion: true,
	enableSnippets: false,
	enableLiveAutocompletion: false
});
