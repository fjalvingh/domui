import {EditorView, basicSetup} from "codemirror"
import {javascript} from "@codemirror/lang-javascript"
import {sql} from "@codemirror/lang-sql"

import 'codemirror-graphql/hint';
import 'codemirror-graphql/lint';
import 'codemirror-graphql/mode';

let editor = new EditorView({
	extensions: [basicSetup, javascript(), sql()],
	parent: document.body,
	mode: 'graphql'
})
