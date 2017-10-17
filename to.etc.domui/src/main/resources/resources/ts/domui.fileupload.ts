/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";
namespace WebUI {
	export function fileUploadChange(e) {
		var tgt = e.currentTarget || e.srcElement;
		var vv = tgt.value.toString();

		if (!vv) { //IE when setting the value to empty..
			return;
		}

		// -- Check extensions,
		var allowed = tgt.getAttribute('fuallowed');
		if (allowed) {
			var ok = false;
			var spl = allowed.split(',');

			vv = vv.toLowerCase();
			for(var i = 0; i < spl.length; i++) {
				var ext = spl[i].toLowerCase();
				if(ext.substring(0, 1) != ".")
					ext = "."+ext;
				if(ext == ".*") {
					ok = true;
					break;
				} else if(vv.indexOf(ext, vv.length - ext.length) !== -1) {
					ok = true;
					break;
				}
			}

			if (!ok) {
				var parts = vv.split('.');
				alert(WebUI.format(WebUI._T.uploadType, (parts.length > 1) ? parts.pop() : '', allowed));
				tgt.value = "";
				return;
			}
		}

		if(typeof tgt.files[0] !== "undefined") {
			var s = tgt.getAttribute('fumaxsize');
			var maxSize = 0;
			try {
				maxSize = Number(s);
			} catch(x) {
			}
			if(maxSize <= 0 || maxSize === NaN)
				maxSize = 100 * 1024 * 1024;						// Default max size is 100MB

			var size = tgt.files[0].size;
			if(size > maxSize) {
				alert(WebUI.format(WebUI._T.buplTooBig, maxSize));
				tgt.value = "";
				return;
			}
		}

		// -- Step 2: create or locate an iframe to handle the upload;
		var iframe = document.getElementById('webuiif') as HTMLIFrameElement;
		if (iframe) {
			iframe.parentNode.removeChild(iframe);
			iframe = undefined;
		}
		if (!iframe) {
			if(jQuery.browser.msie && ! WebUI.isNormalIE9plus()) {			// MicroMorons report ie9 for ie7 emulation of course
				// -- IE's below 8 of course have trouble. What else.
				iframe = document.createElement('<iframe name="webuiif" id="webuiif" src="#" style="display:none; width:0; height:0; border:none" onload="WebUI.ieUpdateUpload(event)">') as HTMLIFrameElement;
				document.body.appendChild(iframe);
			} else {
				iframe = document.createElement('iframe');
				iframe.id = 'webuiif';
				iframe.name = "webuiif";
				iframe.src = "#";
				iframe.style.display = "none";
				iframe.style.width = "0px";
				iframe.style.height = "0px";
				iframe.style.border = "none";
				iframe.onload = function() {
					updateUpload(iframe.contentDocument);
				};
				document.body.appendChild(iframe);
			}
		}

		// -- replace the input with a busy indicator
		var form = tgt.parentNode; // Get form to submit to later on,

		var img = document.createElement('img');
		img.border = "0";
		img.src = (window as any).DomUIProgressURL;
		form.parentNode.insertBefore(img, form);
		form.style.display = 'none';

		// -- Target the iframe
		form.target = "webuiif"; // Fake a new thingy,
		form.submit(); // Force submit of the thingerydoo
		WebUI.blockUI();									// since 20160226: block UI during upload
	}

	/**
	 * Called for ie upload garbage only, this tries to decode the utter devastating mess that
	 * ie makes from xml uploads into an iframe in ie8+. Sigh. The main problem with IE is that
	 * the idiots that built it mess up XML documents sent to it: when the iframe we use receives
	 * an xml document the idiot translates it into an html document- with formatted tags and minus
	 * signs before it to collapse them. Instead of just /rendering/ that they actually /generate/
	 * that as the content document of the iframe- so getting that means you get no XML at all.
	 * This abomination was more or less fixed in ie9 - but of course they fucked up compatibility mode
	 * badly.
	 *
	 * Everything below ie9
	 * ====================
	 * Everything below ie9 has a special property "XMLDocument" attached to the "document" property of
	 * the window. This property contains the original XML that was used as an XML tree. So for these
	 * browsers we just get that and move on.
	 *
	 * IE9 in native mode
	 * ==================
	 * IE9 in native mode does not mess up the iframe content document, so this mode takes the path of
	 * all browsers worth the name. Since the original problem was solved the XMLDocument property no
	 * longer exists.
	 *
	 * IE9 in compatibility mode (IE7)
	 * ===============================
	 * This gets funny. In this mode the browser /still/ messes up the iframe's content model like the
	 * old IE's it emulates. But of course the XMLDocument property is helpfully removed - EVEN IN THIS
	 * MODE.
	 * Remember: this mode is also entered if you are part of a frameset or iframe that is part of an
	 * mode: the topmost frameset/page determines the mode for the entire set of pages - they are that
	 * stupid.
	 *
	 * I know no other "workaround" than the horrible concoction below; please turn away if you have
	 * a weak stomach....
	 *
	 * In this case we get the "innerText" content of the iframe. This differers from innerHTML in that
	 * all html tags that IE added are removed, and only the text is retained. Since the html was generated
	 * by IE in such a way that the xml was presented to the user - this is actually most of our XML...
	 * But it contains some problems:
	 * - there is extra whitespace around it's edges, so we need to remove that..
	 * - All tags start on a new line with a '-' sign before them... Remove all that...
	 * - The result will have the &amp; entity replaced by & because they are really stupid, again. So replace
	 *   that too.
	 * The resulting thing can sometimes be parsed as XML and then processing continues. But it is far
	 * from perfect. The biggest problem is that the resulting xml has not properly reserved the original
	 * whitespace; this may lead to rendering problems.
	 *
	 * But as far as I know no other solution to this stupifying bug is possible. Please prove me wrong.
	 *
	 * @param e
	 */
	export function ieUpdateUpload(e) { // Piece of crap
		var iframe = document.getElementById('webuiif') as HTMLIFrameElement;
		var xml;
		let cw = iframe.contentWindow as any;
		if(cw && cw.document.XMLDocument) {
			xml = cw.document.XMLDocument; // IMPORTANT MS fuckup - See http://p2p.wrox.com/topic.asp?whichpage=1&TOPIC_ID=62981&#153594
		} else if(iframe.contentDocument) {
			var crap = iframe.contentDocument.body.innerText;
			crap = crap.replace(/^\s+|\s+$/g, ''); // trim
			crap = crap.replace(/(\n|\r)-*/g, ''); // remove '\r\n-'. The dash is optional.
			crap = crap.replace(/&/g, '&amp;');		// Replace & with entity
//			alert('crap='+crap);
			if((window as any).DOMParser) {
				var parser = new DOMParser() as any;
				xml = parser.parseFromString(crap);
			} else if((window as any).ActiveXObject) {
				xml = new ActiveXObject("Microsoft.XMLDOM");
				if(! xml.loadXML(crap)) {
					alert('ie9 in emulation mode unfixable bug: cannot parse xml');
					window.location.href = window.location.href;
					WebUI.unblockUI();
					return;
				}
			} else {
				alert('No idea how to parse xml today.');
			}
		} else
			alert('IE error: something again changed in xml source structure of the iframe, sigh');

		updateUpload(xml, iframe);
	}

	export function updateUpload(doc, ifr?) {
		try {
			$.executeXML(doc);
		} catch (x) {
			alert(x);
			throw x;
		} finally {
			WebUI.unblockUI();
		}
		/*
		 * 20081015 jal De iframe zou verwijderd moeten worden, maar als ik dat
		 * doe onder Firefox dan blijft de browser in "busy" mode staan, met een
		 * rode enabled "stop" knop en een "busy" mouse.. Voor nu hergebruiken
		 * we de bestaande iframe dan maar en accepteren de verloren resources.
		 */
		// iframe.parentNode.removeChild(iframe); // Suicide..
	}


}
