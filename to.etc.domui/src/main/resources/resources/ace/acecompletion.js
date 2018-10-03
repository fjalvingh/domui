var langTools = require("ace/ext/language_tools");
var rhymeCompleter = {
	getCompletions: function(editor, session, pos, prefix, callback) {

		WebUI.jsoncall('$ID$', {
				"$ID$_col": pos.column,
				"$ID$_row": pos.row,
				"$ID$_prefix": prefix
			}, function(data) {
				callback(null, data);
			}
		);
	}
};
// langTools.addCompleter(rhymeCompleter);
langTools.setCompleters([rhymeCompleter]);
ed.setOptions({
	enableBasicAutocompletion: true,
	enableSnippets: false,
	enableLiveAutocompletion: false
});
