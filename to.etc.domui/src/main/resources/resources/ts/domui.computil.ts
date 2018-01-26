/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";

namespace WebUI {
	export function returnKeyPress(evt, node) : boolean {
		var keyCode = WebUI.normalizeKey(evt);
		if (keyCode != 13000 && keyCode != 13)
			return true;
		// Be sure 'onblur' is executed before next action (like submit)
		try {
			evt.target.onblur(evt);
		} catch (err) {
			// Ignore any error in case that target does not support onblur...
		}
		WebUI.scall(evt.currentTarget ? evt.currentTarget.id : node.id, 'returnpressed');
		return false;
	}

	export function wtMouseDown(e) {
		alert(e);
	}

	//alignment methods
	//sticks top of element with nodeId to bottom of element with alignToId, with extra offsetY.
	export function alignTopToBottom(nodeId, alignToId, offsetY, doCallback) : void {
		var alignNode = $('#' + alignToId);
		var node = $('#' + nodeId);
		var myTopPos;
		if (node.css('position') == 'fixed'){
			myTopPos = $(alignNode).offset().top - $(document).scrollTop() + offsetY + $(alignNode).outerHeight(true);
		}else{
			myTopPos = $(alignNode).position().top + offsetY + $(alignNode).outerHeight(true);
		}
		$(node).css('top', myTopPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	}

	//align top of element with nodeId to top of element with alignToId, with extra offsetY
	export function alignToTop(nodeId, alignToId, offsetY, doCallback) : void {
		var alignNode = $('#' + alignToId);
		var node = $('#' + nodeId);
		var myTopPos;
		if (node.css('position') == 'fixed'){
			myTopPos = $(alignNode).offset().top - $(document).scrollTop() + offsetY;
		}else{
			myTopPos = $(alignNode).position().top + offsetY;
		}
		var nodeHeight = $(node).outerHeight(true);
		if (myTopPos + nodeHeight > $(window).height()){
			myTopPos = $(window).height() - nodeHeight;
		}
		$(node).css('top', myTopPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	}

	//align left edge of element with nodeId to left edge of element with alignToId, with extra offsetX
	export function alignToLeft(nodeId, alignToId, offsetX, doCallback) : void {
		var node = $('#' + nodeId);
		var alignNode = $('#' + alignToId);
		var myLeftPos;
		if (node.css('position') == 'fixed'){
			myLeftPos = $(alignNode).offset().left - $(document).scrollLeft() + offsetX;
		}else{
			myLeftPos = $(alignNode).position().left + offsetX;
		}
		var nodeWidth = $(node).outerWidth(true);
		if (myLeftPos + nodeWidth > $(window).width()){
			myLeftPos = $(window).width() - nodeWidth;
			if (myLeftPos < 1){
				myLeftPos = 1;
			}
		}
		$(node).css('left', myLeftPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	}

	//align right edge of element with nodeId to right edge of element with alignToId, with extra offsetX
	export function alignToRight(nodeId, alignToId, offsetX, doCallback) : void {
		var node = $('#' + nodeId);
		var alignNode = $('#' + alignToId);
		var myLeftPos;
		if (node.css('position') == 'fixed'){
			myLeftPos = $(alignNode).offset().left - $(document).scrollLeft() + offsetX - $(node).outerWidth(true) + $(alignNode).outerWidth(true) - 3;
		}else{
			myLeftPos = $(alignNode).position().left + offsetX - $(node).outerWidth(true) + $(alignNode).outerWidth(true) - 3;
		}
		if (myLeftPos < 1){
			myLeftPos = 1;
		}
		$(node).css('left', myLeftPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	}

	//align horizontally middle of element with nodeId to middle of element with alignToId, with extra offsetX
	export function alignToMiddle(nodeId, alignToId, offsetX, doCallback) : void {
		var node = $('#' + nodeId);
		var alignNode = $('#' + alignToId);
		var myLeftPos;
		if (node.css('position') == 'fixed'){
			myLeftPos = $(alignNode).offset().left - $(document).scrollLeft() + ($(alignNode).outerWidth(true) / 2) - ($(node).outerWidth(true) / 2);
		}else{
			myLeftPos = $(alignNode).position().left + ($(alignNode).outerWidth(true) / 2) - ($(node).outerWidth(true) / 2);
		}
		if (myLeftPos < 1){
			myLeftPos = 1;
		}
		$(node).css('left', myLeftPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	}

	/**
	 * Copies the specified text to the clipboard. Can only be called
	 * directly on a user action, i.e. on a Javascript onclick event.
	 * See https://stackoverflow.com/questions/400212/how-do-i-copy-to-the-clipboard-in-javascript
	 */
	export function copyTextToClipboard(text: string): void {
		let textArea = document.createElement("textarea");

		textArea.style.position = 'fixed';
		textArea.style.top = '0';
		textArea.style.left = '0';
		textArea.style.width = '2em';						// Must have size
		textArea.style.height = '2em';

		// We don't need padding, reducing the size if it does flash render.
		textArea.style.padding = '0';

		// Clean up any borders.
		textArea.style.border = 'none';
		textArea.style.outline = 'none';
		textArea.style.boxShadow = 'none';

		// Avoid flash of white box if rendered for any reason.
		textArea.style.background = 'transparent';
		textArea.value = text;

		document.body.appendChild(textArea);
		textArea.select();
		try {
			let successful = document.execCommand('copy');
			var msg = successful ? 'successful' : 'unsuccessful';
			console.log('Copying text command was ' + msg);
		} catch (err) {
			console.log('Oops, unable to copy');
		}

		document.body.removeChild(textArea);
	}
}
