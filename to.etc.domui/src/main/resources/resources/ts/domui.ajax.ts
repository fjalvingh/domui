/// <reference types="jquery" />
/// <reference types="jqueryui" />
/// <reference path="domui.jquery.d.ts" />
// <reference path="domui.webui.d.ts" />
/// <reference path="domui.webui.ts" />
let _ajaxRequestId = Date.now();
let _ajaxReplyId = Date.now();
namespace WebUI {
	let _inputFieldList: any[] = [];

	export function getReplyId() {
		return _ajaxReplyId;
	}

	export function getInputFields(fields: any): object {
		// Collect all input, then create input.
		let q1 = $("input").get();
		for(let i = q1.length; --i >= 0;) {
			let t: any = q1[i];
			if(t.type == 'file')
				continue;
			if(t.type == 'hidden' && !t.getAttribute('s')) // All hidden input nodes are created directly in browser java-script and because that are filtered out from server requests.
				continue;

			let val = undefined;
			if(t.type == 'checkbox' || t.type == 'radio') {
				val = t.checked ? "y" : "n";
			} else {
				val = t.value;
			}

			if(t.id.length > 0)
				fields[t.id] = val;
		}

		q1 = $("select").get();
		for(let i = q1.length; --i >= 0;) {
			let sel: HTMLSelectElement = q1[i] as HTMLSelectElement;
			let val = undefined;
			if(sel.selectedIndex != -1) {
				val = sel.options[sel.selectedIndex].value;
			}

			if(val != undefined && sel.id.length > 0)
				fields[sel.id] = val;
		}
		q1 = $("textarea").get();
		for(let i = q1.length; --i >= 0;) {
			let sel = q1[i] as HTMLTextAreaElement;
			let val;
			if(sel.className == 'ui-ckeditor') {
				//-- Locate the variable for this editor.
				let editor = (window as any).CKEDITOR.instances[sel.id];
				if(null == editor)
					throw "Cannot locate editor with id=" + sel.id;
				val = editor.getData();
			} else {
				val = sel.value;
			}
			if(sel.id.length > 0)
				fields[sel.id] = val;
		}

		//-- Handle all registered controls
		let list = _inputFieldList;
		for(let i = list.length; --i >= 0;) {
			let item = list[i];
			if(!document.getElementById(item.id)) {
				//-- Node gone - remove input
				list.splice(i, 1);
			} else {
				let data = item.control.getInputField(fields);
				if(item.id.length > 0)
					fields[item.id] = data;
			}
		}

		return fields;
	}

	/**
	 * This registers a non-html control as a source of input for {@link getInputFields}. The control
	 * must have a method "getInputFields(fields: Map)" which defines the inputs to send for the control.
	 */
	export function registerInputControl(id, control) {
		let list = _inputFieldList;
		for(let i = list.length; --i >= 0;) {
			let item = list[i];
			if(item.id == id) {
				item.control = control;
				return;
			}
		}
		list.push({id: id, control: control});
	}

	export function visibilityChanged() {
		let list = _inputFieldList;
		for(let i = list.length; --i >= 0;) {
			let item = list[i];
			if(item.control.onVisibilityChanged) {
				item.control.onVisibilityChanged();
			}
		}
	}

	export function propagateResize() {
		let list = _inputFieldList;
		for(let i = list.length; --i >= 0;) {
			let item = list[i];
			if(item.control.onResize) {
				item.control.onResize();
			}
		}
	}

	export function findInputControl(id) {
		//-- return registered component by id, if not found returns null
		let list = _inputFieldList;
		for(let i = list.length; --i >= 0;) {
			let item = list[i];
			if(item.id == id && document.getElementById(item.id)) {
				return item.control;
			}
		}
		return null;
	}

	export function clicked(h, id : string, evt: any) {
		//-- Trigger the before-clicked event on body
		// console.debug("clicked " + id);
		$(document.body).trigger("beforeclick", [$("#" + id), evt]);

		// Collect all input, then create input.
		let fields: any = {};
		this.getInputFields(fields);
		fields.webuia = "clicked";
		fields.webuic = id;
		fields["$pt"] = (window as any).DomUIpageTag;
		fields["$cid"] = (window as any).DomUICID;
		cancelPolling();

		//-- Do not call upward handlers too.
		if(!evt)
			evt = window.event;

		// jal 20131107 Cancelling the event means that you cannot click items inside a clickable item
		if(evt) {
			evt.cancelBubble = true;
			if(evt.stopPropagation)
				evt.stopPropagation();
		}
		let e = ($ as any).event.fix(evt);		// Convert to jQuery event
		//e.preventDefault(); // jal 20110216 DO NOT PREVENTDEFAULT- it will disable checkbox enable/disable

		//-- add click-related parameters
		fields._pageX = e.pageX;
		fields._pageY = e.pageY;
		fields._controlKey = e.ctrlKey == true;
		fields._shiftKey = e.shiftKey == true;
		fields._altKey = e.altKey == true;

		_ajaxRequestId++;
		$.ajax({
			url: WebUI.getPostURL(),
			dataType: "*",
			data: fields,
			cache: false,
			type: "POST",
			error: handleError,
			success: handleResponse,
		});
		return false;						// jal 20131107 Was false, but inhibits clicking on radiobutton inside a table in Chrome.
	}

	export function prepareAjaxCall(id, action, fields?) {
		_ajaxRequestId++;
		if(!fields)
			fields = {};
		// Collect all input, then create input.
		WebUI.getInputFields(fields);
		fields.webuia = action;
		fields.webuic = id === "" ? "_1" : id;
		fields["$pt"] = (window as any).DomUIpageTag;
		fields["$cid"] = (window as any).DomUICID;

		return {
			url: WebUI.getPostURL(),
			dataType: "*",
			data: fields,
			cache: false,
			type: "POST",
			success: handleResponse,
			error: handleError
		};
	}

	export function scall(id: string, action: string, fields? : any) : void {
		let call = prepareAjaxCall(id, action, fields);
		cancelPolling();
		$.ajax(call);
	}


	export function jsoncall(id, fields, callback = undefined) {
		if(!fields)
			fields = {};
		WebUI.getInputFields(fields);

		fields["webuia"] = "$pagejson";
		fields["webuic"] = id;
		fields["$pt"] = (window as any).DomUIpageTag;
		fields["$cid"] = (window as any).DomUICID;

		let response = "";
		_ajaxRequestId++;
		$.ajax({
			url: WebUI.getPostURL(),
			dataType: "*",
			data: fields,
			cache: false,
			async: callback != undefined,
			type: "POST",
			success: function(data, state) {
				response = data;
				if(callback) {
					callback(data);
				}
				_ajaxReplyId++;
			},
			error: handleError
		});
		if(callback)
			return;
		return JSON.parse(response);
//		console.debug("jsoncall-", response);
//		try {
// 		return eval("(" + response + ")");
//		} catch(x) {
//			console.debug("json data error", x);
//		}
	}

	/**
	 * Send a server request to a component, which will be handled by that component's componentHandleWebAction
	 * method. The json data is sent as a string parameter with the name "json"; the response is handled as a normal
	 * DomUI page request: the page is updated and any delta is returned.
	 * @returns void
	 */
	export function sendJsonAction(id, action, json) {
		let fields = {};
		fields["json"] = JSON.stringify(json);
		scall(id, action, fields);
	}

	/**
	 * Call a JSON handler on a component. This is "out of bound": the current browser state of
	 * the page is /not/ sent, and the response must be a JSON document which will be the return
	 * value of this function.
	 *
	 * @param id
	 * @param fields
	 * @returns
	 */
	export function callJsonFunction(id, action, fields) {
		if(!fields)
			fields = {};
		fields.webuia = "#" + action;
		fields.webuic = id;
		fields["$pt"] = (window as any).DomUIpageTag;
		fields["$cid"] = (window as any).DomUICID;

		let response = "";
		_ajaxRequestId++;
		$.ajax({
			url: WebUI.getPostURL(),
			dataType: "text/xml",
			data: fields,
			cache: false,
			async: false,
			type: "POST",
			success: function(data, state) {
				response = data;
				_ajaxReplyId++;
			},
			error: handleError
		});
//		console.debug("jsoncall-", response);
//		try {
		return eval("(" + response + ")");
//		} catch(x) {
//			console.debug("json data error", x);
//		}
	}

	export function clickandchange(h, id, evt) {
		//-- Do not call upward handlers too.
		if(!evt)
			evt = window.event;
		if(evt) {
			evt.cancelBubble = true;
			if(evt.stopPropagation)
				evt.stopPropagation();
		}
		scall(id, 'clickandvchange');
	}

	export function valuechanged(unknown : string, id: string) : void{
		// FIXME 20100315 jal Temporary fix for bug 680: if a DateInput has a value changed listener the onblur does not execute. So handle it here too.... The fix is horrible and needs generalization.
		let item = document.getElementById(id);
		if(item && (item.tagName == "input" || item.tagName == "INPUT") && item.className == "ui-di") {
			//-- DateInput control: manually call the onblur listener.
			this.dateInputRepairValueIn(item);
		}

		// Collect all input, then create input.
		let fields = {};
		this.getInputFields(fields);
		fields["webuia"] = "vchange";
		fields["webuic"] = id;
		fields["$pt"] = (window as any).DomUIpageTag;
		fields["$cid"] = (window as any).DomUICID;
		cancelPolling();

		_ajaxRequestId++;
		$.ajax({
			url: WebUI.getPostURL(),
			dataType: "*",
			data: fields,
			cache: false,
			type: "POST",
			success: handleResponse,
			error: handleError
		});
	}

	export function handleResponse(data, state): void {
		_ajaxReplyId++;
		clearErrorAsy();
		// if (false && window.console && window.console.debug)
		// console.log("data is ", data);
		$.webui(data);
	}

	export function handlePollResponse(data, state): void {
		clearErrorAsy();
		// if (false && window.console && window.console.debug)
		// console.log("data is ", data);
		$.webui(data);
	}

	export function handleError(request, status, exc): boolean {
		let txt = request.responseText;
		if(document.body)
			document.body.style.cursor = 'default';
		// alert('Server error: '+status+", len="+txt.length+", val="+txt);
		if(txt.length == 0) {
			//-- Firefox fix: if the page is unloading but a request is pending this may cause an status=ERROR for that page. Prevent us from then overwriting the new document....
			if(status == "error")
				return;

			txt = "De server is niet bereikbaar 1, status=" + status + ", " + request.statusText;
		}
		if(status === "parsererror") {
			console.log("INVALID XML: ", txt);
			alert("ERROR: DomUI server returned invalid XML");
			let hr = window.location.href;					// Force reload
			window.location.href = hr;
			return;
		}

		try {
			document.write(txt);
			document.close();
			window.setTimeout('document.body.style.cursor="default"', 1000);
		} catch(x) {
			alert("Error: " + txt);
		}
		return true;
	}

	let _asyalerted = false;
	let _asyDialog = null;
	let _ignoreErrors = false;
	let _asyHider = undefined;

	export function handleErrorAsy(request, status, exc): void {
		_ajaxReplyId++;
		handleErrorAsyMain(request, status, exc);
	}

	export function handleErrorAsyMain(request, status, exc): void {
		if(_asyalerted) {
			//-- We're still in error.. Silently redo the poll.
			startPolling(_pollInterval);
			return;
		}
//		$.dbg("Got into error state - start "+request.responseText);
		if(status === "abort")
			return;

		_asyalerted = true;

		let txt = request.responseText;
		if(!txt || txt.length == 0)
			txt = WebUI._T.sysPollFailMsg + status;
		let contentType = typeof request.getResponseHeader === "function" ? request.getResponseHeader("Content-Type") : null;

		/*
		 * As usual there is a problem with error reporting: if the request is aborted because the browser reloads the page
		 * any pending request is cancelled and comes in here- but with the wrong error code of course. So to prevent us from
		 * showing an error message: set a timer to show that message 250 milli's later, and hope the stupid browser disables
		 * that timer.
		 */
		setTimeout(function() {
			if(_ignoreErrors)
				return;

			//-- Show an alert error on top of the screen
			document.body.style.cursor = 'default';
			let hdr = document.createElement('div');
			document.body.appendChild(hdr);
			hdr.className = 'ui-io-blk2';
			_asyHider = hdr;

			let ald = document.createElement('div');
			document.body.appendChild(ald);
			ald.className = 'ui-ioe-asy';
			_asyDialog = ald;

			let d = document.createElement('div');			// Title bar
			ald.appendChild(d);
			d.className = "ui-ioe-ttl";
			d.appendChild(document.createTextNode(WebUI._T.sysPollFailTitle));	// Server unreachable

			d = document.createElement('div');				// Message content
			ald.appendChild(d);
			d.className = "ui-ioe-msg";
			renderErrorResponse(d, txt, contentType);

			d = document.createElement('div');				// Message content
			ald.appendChild(d);
			d.className = "ui-ioe-msg2";

			let img = document.createElement('div');
			d.appendChild(img);
			img.className = "ui-ioe-img";
			d.appendChild(document.createTextNode(WebUI._T.sysPollFailCont));	// Waiting for the server to return.
			startPolling(_pollInterval);
		}, 250);
	}

	/**
	 * The maximum amount of text taken from a plain text error response; the error dialog is small
	 * so anything longer than this gets truncated.
	 */
	const MAX_ERROR_TEXT_LENGTH = 512;

	/**
	 * The maximum amount of text taken from a html error response. Html documents contain a lot of
	 * layout whitespace, so this is a lot bigger than the plain text limit; the dialog itself scrolls
	 * when the result does not fit.
	 */
	const MAX_ERROR_HTML_LENGTH = 4096;

	/**
	 * The elements that are allowed to survive inside a server supplied error document. Anything not
	 * in here is either dropped completely (see ERROR_DROPPED_ELEMENTS) or replaced by its own,
	 * sanitized, content.
	 */
	const ERROR_ALLOWED_ELEMENTS = [
		"A", "ABBR", "B", "BIG", "BLOCKQUOTE", "BR", "CAPTION", "CENTER", "CODE", "DD", "DIV", "DL", "DT", "EM", "FONT"
		, "H1", "H2", "H3", "H4", "H5", "H6", "HR", "I", "LI", "OL", "P", "PRE", "SMALL", "SPAN", "STRONG", "SUB", "SUP"
		, "TABLE", "TBODY", "TD", "TFOOT", "TH", "THEAD", "TR", "TT", "U", "UL"
	];

	/**
	 * Elements whose content is not human readable text: these are dropped including all of their content.
	 */
	const ERROR_DROPPED_ELEMENTS = [
		"EMBED", "HEAD", "IFRAME", "MATH", "NOSCRIPT", "OBJECT", "SCRIPT", "STYLE", "SVG", "TEMPLATE", "TITLE"
	];

	/**
	 * The only attributes that are copied from a server supplied error document. Everything else (event
	 * handlers, style, href, src) is dropped: those are either useless inside the error dialog or a
	 * security risk, as this data comes straight off the wire.
	 */
	const ERROR_ALLOWED_ATTRIBUTES = ["colspan", "rowspan", "title"];

	/**
	 * Render the response of a failed request inside the error dialog. Servers, proxies and load balancers
	 * return either plain text or a (partial) html document; html must be rendered as html instead of
	 * showing all of its tags as literal text. Because this data comes straight off the wire it is
	 * sanitized before it is added to the document.
	 */
	function renderErrorResponse(into: HTMLElement, text: string, contentType: string): void {
		if(isHtmlResponse(text, contentType)) {
			let fragment = sanitizeHtmlResponse(text);
			if(fragment) {
				into.className += " ui-ioe-html";
				into.appendChild(fragment);
				return;
			}
		}

		//-- Either not html at all, or html without any readable content: show the response as-is.
		let txt = text;
		if(txt.length > MAX_ERROR_TEXT_LENGTH)
			txt = txt.substring(0, MAX_ERROR_TEXT_LENGTH) + "...";
		into.appendChild(document.createTextNode(txt));
	}

	/**
	 * Recognize markup in an error response: either the content type says it is html, or the response
	 * contains something that can only be a doctype or a html tag. Plain text must not be recognized as
	 * html, hence the whitelist of tag names: things like "List<String>" in a stack trace are text.
	 */
	function isHtmlResponse(text: string, contentType: string): boolean {
		if(contentType && contentType.toLowerCase().indexOf("html") >= 0)
			return true;
		return /<(!doctype\s+html|html|head|body|title|div|p|pre|br|hr|h[1-6]|span|table|tr|td|ul|ol|li|b|i|em|strong|font|center|a)(\s[^<>]*)?\/?>/i.test(text);
	}

	/**
	 * Parse a (partial) html document and return it as a sanitized fragment which is safe to add to the
	 * document. Returns null if the document has no readable text at all, so that the caller can fall
	 * back to rendering the raw response instead of showing an empty message.
	 */
	function sanitizeHtmlResponse(text: string): DocumentFragment {
		if(typeof DOMParser === "undefined")				// Ancient browser: no safe way to parse the html.
			return null;
		let doc = new DOMParser().parseFromString(text, "text/html");
		let root = doc.body || doc.documentElement;
		if(!root)
			return null;

		let fragment = document.createDocumentFragment();
		copySanitized(root, fragment, {left: MAX_ERROR_HTML_LENGTH});
		let content = fragment.textContent;
		if(!content || content.trim().length == 0)
			return null;
		return fragment;
	}

	/**
	 * Copy the children of source into target, keeping only whitelisted elements and attributes and
	 * stopping as soon as the text budget is exhausted.
	 */
	function copySanitized(source: Node, target: Node, budget: {left: number}): void {
		for(let child = source.firstChild; child; child = child.nextSibling) {
			if(budget.left <= 0)
				return;

			if(child.nodeType === Node.TEXT_NODE) {
				let text = child.nodeValue || "";
				if(text.length > budget.left) {
					text = text.substring(0, budget.left) + "...";
					budget.left = 0;
				} else {
					budget.left -= text.length;
				}
				target.appendChild(document.createTextNode(text));
			} else if(child.nodeType === Node.ELEMENT_NODE) {
				let element = child as Element;
				let name = element.tagName.toUpperCase();
				if(ERROR_DROPPED_ELEMENTS.indexOf(name) >= 0)
					continue;
				if(ERROR_ALLOWED_ELEMENTS.indexOf(name) < 0) {
					copySanitized(element, target, budget);		// Unknown element: keep its content, lose the element itself.
					continue;
				}

				let copy = document.createElement(name);
				for(let index = 0; index < element.attributes.length; index++) {
					let attribute = element.attributes[index];
					if(ERROR_ALLOWED_ATTRIBUTES.indexOf(attribute.name.toLowerCase()) >= 0)
						copy.setAttribute(attribute.name, attribute.value);
				}
				target.appendChild(copy);
				copySanitized(element, copy, budget);
			}
		}
	}

	export function clearErrorAsy(): void {
//		$.dbg("clear asy called");
		if(_asyDialog) {
			$(_asyDialog).remove();
		}
		if(_asyHider) {
			$(_asyHider).remove();
		}
		_asyDialog = null;
		_asyHider = null;
		_asyalerted = false;
	}


	/** *************** Polling code ************* */
	/**
	 * Will be set by startPolling to define the poll interval.
	 */
	let _pollInterval = 2500;

	let _pollActive = false;

	let _pollTimer: number = undefined;

	export function startPolling(interval: number): void {
		if(interval < 100 || interval == undefined || interval == null) {
			alert("Bad interval: " + interval);
			return;
		}
		_pollInterval = interval;
		if(_pollActive)
			return;
		_pollActive = true;
		_pollTimer = setTimeout("WebUI.poll()", _pollInterval);
	}

	export function cancelPolling(): void {
		if(!_pollActive)
			return;
		clearTimeout(_pollTimer);
		_pollActive = false;
	}

	export function poll(): void {
		cancelPolling();

		/*
		 * Issue a pollasy request using ajax, then handle the result.
		 */
		let fields = {};
		fields["webuia"] = "pollasy";
		fields["$pt"] = (window as any).DomUIpageTag;
		fields["$cid"] = (window as any).DomUICID;

		$.ajax({
			url: WebUI.getPostURL(),
			dataType: "*", // "text/xml",
			data: fields,
			cache: false,
			global: false, // jal 20091015 prevent block/unblock on polling call.
			converters: { //bugfix: when special bytes are present, handlers are not called.
				'text xml': function (f) {
					return f;
				}
			},
			success: handlePollResponse,			// These two do not increment the ajaxReplyId
			error: handleErrorAsyMain
		});
	}

	/**
	 * Send Ajax request to the server every 2 minutes. This keeps the session
	 * alive. The response can contain commands to execute which will indicate
	 * important events or changes have taken place.
	 */
	export function pingServer(timeout : number) : void {
		let url = (window as any).DomUIappURL + "to.etc.domui.parts.PollInfo.part";
		let fields= {};
		fields["$pt"] = (window as any).DomUIpageTag;
		fields["$cid"] = (window as any).DomUICID;
		_ajaxRequestId++;
		$.ajax( {
			url: url,
			dataType: "*",
			data: fields,
			cache: false,
			global: false, // jal 20091015 prevent block/unblock on polling call.
			success: function(data, state) {
				_ajaxReplyId++;
				executePollCommands(data);
			},
			error : function() {
				_ajaxReplyId++;
				//-- Ignore all errors.
			}
		});
		startPingServer(timeout);
	}

	export function startPingServer(timeout: number) : void {
		if(timeout < 60*1000)
			timeout = 60*1000;
		setTimeout("WebUI.pingServer("+timeout+")", timeout);
	}

	function executePollCommands(data) {
		// TBD
	}

	export function unloaded() : void {
		_ignoreErrors = true;
		sendobituary();
	}

	export function beforeUnload() : void {
		//-- Make sure no "ajax" errors are reported.
		_ignoreErrors = true;
	}

	/**
	 * Called at page unload time, this quickly tries to send an obituary to the
	 * server. This is currently unconditional but can later be augmented to
	 * send the obituary only when the browser window closes.
	 */
	export function sendobituary() : void {
		try {
			let rq;
			let w = window as any;
			if (w.XMLHttpRequest) {
				rq = new XMLHttpRequest();
			} else if (w.ActiveXObject) {
				rq = new ActiveXObject("Microsoft.XMLHTTP");
			} else {
				alert("Cannot send obituary (no transport)");
				return;
			}
			rq.open("GET", WebUI.getObituaryURL() + "?$cid=" + w.DomUICID + "&webuia=OBITUARY&$pt=" + w.DomUIpageTag, false);
			rq.send(null);
		} catch(ex) {
//			alert("Sending obit failed:"+ex);
		}
	}

	export function notifyPage(command) {
		let bodyId = '_1';
		let pageBody = document.getElementById(bodyId);
		//check for exsistence, since it is delayed action component can be removed when action is executed.
		if (pageBody){
			let fields = {};
			fields["webuia"] = "notifyPage";
			fields[bodyId + "_command"] = command;
			WebUI.scall(bodyId, "notifyPage", fields);
		}
	}

}
