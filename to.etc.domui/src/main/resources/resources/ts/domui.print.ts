/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";

namespace WebUI {
	/*-- Printing support --*/
	let _frmIdCounter = 0;

	export function backgroundPrint(url: string) : void {
		try {
			var frmname = priparePrintDiv(url);
			framePrint(frmname);
		} catch (x) {
			alert("Failed: " + x);
		}
	}

	export function framePrint(frmname: string) : void {
		if (jQuery.browser.msie) {
			documentPrintIE(frmname);
		} else {
			documentPrintNonIE(frmname);
		}
	}

	export function documentPrintIE(frmname: string) : void {
		let ex = undefined;
		try {
			var frm = window.frames[frmname];

			$("#"+frmname).on("load", function() {
				try {
					frm.focus();
					setTimeout(function() {
						if (!frm.document.execCommand('print', true, null)){
							alert('cannot print: ' + ex);
						}
					}, 1000);
				} catch(x) {
					ex = x;
					alert('cannot print: '+x);
				}
			});
		} catch(x) {
			ex = x;
			alert("Failed: "+x);
		}
	}

	export function documentPrintNonIE(frmname: string) : void {
		try {
			var frm = window.frames[frmname];
			$("#"+frmname).on("load", function() {
				try {
					frm.focus();
					setTimeout(function() {
						frm.print();
					}, 1000);
				} catch(x) {
					alert('cannot print: '+x);
				}
			});
		} catch(x) {
			alert("Failed: "+x);
		}
	}

	export function priparePrintDiv(url: string) : string {
		// Create embedded sizeless div to contain the iframe, invisibly.
		var div = document.getElementById('domuiprif');
		if (div)
			div.innerHTML = "";
		else {
			div = document.createElement('div');
			div.id = 'domuiprif';
			div.className = 'ui-printdiv';
			document.body.appendChild(div);
		}

		// -- Create an iframe loading the required thingy.
		var frmname = "dmuifrm" + (_frmIdCounter++); // Create unique
		// name to
		// circumvent
		// ffox "print
		// only once"
		// bug
		if (url.trim() !== "") {
			$(div).html('<iframe id="' + frmname + '" name="' + frmname + '" src="' + url + '">');
		} else {
			//well, this is simple element printing, so we have some size limitations
			$(div).html('<iframe id="' + frmname + '" name="' + frmname + '" width="1000px" height="1000px"/>');
		}
		return frmname;
	}

	/*-- Printing support for simple text messages. Parameter is id of input/textarea tag that contrains text to be printed out. --*/
	export function printtext(id : string) : void {
		var item = document.getElementById(id);
		var textData;
		if(item && (item.tagName == "input" || item.tagName == "INPUT" || item.tagName == "textarea" || item.tagName == "TEXTAREA")) {
			textData = (item as any).value;
		}
		if (textData){
			try {
				// Create embedded sizeless div to contain the iframe, invisibly.
				var div = document.getElementById('domuiprif');
				if(div)
					div.innerHTML = "";
				else {
					div = document.createElement('div');
					div.id = 'domuiprif';
					div.className = 'ui-printdiv';
					document.body.appendChild(div);
				}

				//-- Create an iframe loading the required thingy.
				var frmname = "dmuifrm" + _frmIdCounter++;		// Create unique name to circumvent ffox "print only once" bug

				$(div).html('<iframe id="'+frmname+'" name="'+frmname+'" width="1000px" height="1000px"/>'); //well, this is simple text printing, so we have some size limitations ;)
				var frm = window.frames[frmname];
				frm.document.open();
				frm.document.write('<html></head></head><body style="margin:0px;">');
				frm.document.write(textData.replace(/\n/g, '<br/>'));
				frm.document.write('</body></html>');
				frm.document.close();
				frm.focus();
				frm.print();
			} catch(x) {
				alert("Failed: "+x);
			}
		}
	}
}
