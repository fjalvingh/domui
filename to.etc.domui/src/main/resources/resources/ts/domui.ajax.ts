/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
// <reference path="domui.webui.d.ts" />
/// <reference path="domui.webui.ts" />
namespace WebUI {
	let _inputFieldList: any[] = [];

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

			fields[t.id] = val;
		}

		q1 = $("select").get();
		for(let i = q1.length; --i >= 0;) {
			let sel: HTMLSelectElement = q1[i] as HTMLSelectElement;
			let val = undefined;
			if(sel.selectedIndex != -1) {
				val = sel.options[sel.selectedIndex].value;
			}

			if(val != undefined)
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
//				if($.browser.msie) { // The MS idiots remove newlines from value....
//					val = sel.innerText;
//					//alert("inner value="+sel.innerText);
//				} else
				val = sel.value;
			}
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
				let data = item.control.getInputField();
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

		$.ajax({
			url: WebUI.getPostURL(),
			dataType: "*",
			data: fields,
			cache: false,
			type: "POST",
			error: handleError,
			success: handleResponse
		});
		return false;						// jal 20131107 Was false, but inhibits clicking on radiobutton inside a table in Chrome.
	}

	export function prepareAjaxCall(id, action, fields?) {
		if(!fields)
			fields = {};
		// Collect all input, then create input.
		WebUI.getInputFields(fields);
		fields.webuia = action;
		fields.webuic = id;
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

	export function jsoncall(id, fields) {
		if(!fields)
			fields = {};
		fields["webuia"] = "$pagejson";
		fields["webuic"] = id;
		fields["$pt"] = (window as any).DomUIpageTag;
		fields["$cid"] = (window as any).DomUICID;

		let response = "";
		$.ajax({
			url: WebUI.getPostURL(),
			dataType: "text/xml",
			data: fields,
			cache: false,
			async: false,
			type: "POST",
			success: function(data, state) {
				response = data;
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
		$.ajax({
			url: WebUI.getPostURL(),
			dataType: "text/xml",
			data: fields,
			cache: false,
			async: false,
			type: "POST",
			success: function(data, state) {
				response = data;
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
		clearErrorAsy();
		// if (false && window.console && window.console.debug)
		// 	console.debug("data is ", data);
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
			alert("ERROR: DomUI server returned invalid XML");
			let hr = window.location.href;					// Force reload
			window.location.href = hr;
			return;
		}

		document.write(txt);
		document.close();
		window.setTimeout('document.body.style.cursor="default"', 1000);
		return true;
	}

	let _asyalerted = false;
	let _asyDialog = null;
	let _ignoreErrors = false;
	let _asyHider = undefined;

	export function handleErrorAsy(request, status, exc): void {
		if(_asyalerted) {
			//-- We're still in error.. Silently redo the poll.
			startPolling(_pollInterval);
			return;
		}
//		$.dbg("Got into error state - start "+request.responseText);
		if(status === "abort")
			return;

		_asyalerted = true;

		let txt = request.responseText || "No response - status=" + status;
		if(txt.length > 512)
			txt = txt.substring(0, 512) + "...";
		if(txt.length == 0)
			txt = WebUI._T.sysPollFailMsg + status;

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
			d.appendChild(document.createTextNode(txt));	// Server unreachable

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
			url: window.location.href,
			dataType: "*", // "text/xml",
			data: fields,
			cache: false,
			global: false, // jal 20091015 prevent block/unblock on polling call.
			success: handleResponse,
			error: handleErrorAsy
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
		$.ajax( {
			url: url,
			dataType: "*",
			data: fields,
			cache: false,
			global: false, // jal 20091015 prevent block/unblock on polling call.
			success: function(data, state) {
				executePollCommands(data);
			},
			error : function() {
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
