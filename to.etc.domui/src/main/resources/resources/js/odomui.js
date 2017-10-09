$(function(){
	$.getScript("$js/domui-date-checker.js");
});

function _block() {
	WebUI.blockUI();
}
function _unblock() {
	WebUI.unblockUI();
}
$(document).ajaxStart(_block).ajaxStop(_unblock);
$(window).bind('beforeunload', function() {
	WebUI.beforeUnload();
	return undefined;
});

//-- calculate browser major and minor versions
{
	try {
		var v = $.browser.version.split(".");
		$.browser.majorVersion = parseInt(v[0], 10);
		$.browser.minorVersion = parseInt(v[1], 10);

		//-- And like clockwork MS fucks up with IE 11: it no longer registers as msie. Fix that here.
		if(navigator.appName == 'Netscape') {
			var ua = navigator.userAgent;
			if(ua.indexOf("Trident/") != -1)
				$.browser.msie = true;
		}

		if (/Edge/.test(navigator.userAgent)) {
			$.browser.ieedge = true;
		}
	} catch(x) {}

//	alert('bmaj='+$.browser.majorVersion+", mv="+$.browser.minorVersion);
}

/** WebUI helper namespace */
var WebUI;
if(WebUI === undefined)
	WebUI = new Object();

$.extend(WebUI, {
};

var DomUI = WebUI;

WebUI._customUpdatesContributors = $.Callbacks("unique");

WebUI._customUpdatesContributorsTimerID = null;


WebUI.floatingDivResize = function(ev, ui) {
	$(ui.helper.get(0)).css('position', 'fixed');
	$('[stretch=true]').doStretch();
	$('.ui-dt, .ui-fixovfl').fixOverflow();
};

WebUI.onWindowResize = function() {
	WebUI.doCustomUpdates();
};

WebUI.flare = function(id) {
	$('#'+id).fadeIn('fast', function() {
		$('#'+id).delay(500).fadeOut(1000, function() {
			$('#'+id).remove();
		});
	});
};

WebUI.flareStay = function(id) {
	$('#'+id).fadeIn('fast', function() {
		$('body,html').bind('mousemove.' + id, function(e){
			$('body,html').unbind('mousemove.' + id);
			$('#'+id).delay(500).fadeOut(1000, function() {
				$('#'+id).remove();
			});
		});
	});
};

WebUI.flareStayCustom = function(id, delay, fadeOut) {
	$('#'+id).fadeIn('fast', function() {
		$('body,html').bind('mousemove.' + id, function(e){
			$('body,html').unbind('mousemove.' + id);
			$('#'+id).delay(delay).fadeOut(fadeOut, function() {
				$('#'+id).remove();
			});
		});
	});
};


$(document).ready(WebUI.onDocumentReady);
$(window).resize(WebUI.onWindowResize);
$(document).ajaxComplete( function() {
	WebUI.handleCalendarChanges();
	WebUI.doCustomUpdates();
});

$(document).keydown(function(e){
	addPaggerAccessKeys(e);
});

function addPaggerAccessKeys(e) {
	var KEY = {
		HOME		: 36,
		END			: 35,
		PAGE_UP		: 33,
		PAGE_DOWN	: 34
	};
	if ($('div.ui-dp-btns').size() > 0) {
		if (e.altKey){
			if(e.keyCode == KEY.HOME) {
				$("div.ui-dp-btns > a:nth-child(1)").click();
			} else if (e.keyCode == KEY.PAGE_UP) {
				$("div.ui-dp-btns > a:nth-child(2)").click();
			} else if (e.keyCode == KEY.PAGE_DOWN) {
				$("div.ui-dp-btns > a:nth-child(3)").click();
			} else if (e.keyCode == KEY.END) {
				$("div.ui-dp-btns > a:nth-child(4)").click();
			}
		}
	}
}

//piece of support needed for FCK editor to properly fix heights in IE8+
function FCKeditor_OnComplete(editorInstance){
	if (WebUI.isIE8orNewer()){
		for (var i = 0; i < WebUI._fckEditorIDs.length; i++) {
			var fckId = WebUI._fckEditorIDs[i];
			var fckIFrame = document.getElementById(fckId + '___Frame');
			if (fckIFrame){
				$(fckIFrame.contentWindow.window).bind('resize', function()
				{
					FCKeditor_fixLayout(fckIFrame, fckId);
				});
				$(fckIFrame.contentWindow.window).trigger('resize');
			};
		};
	};
}

WebUI.initScrollableTableOld = function(id) {
	$('#'+id+" table").fixedHeaderTable({});
	var sbody = $('#'+id+" .fht-tbody");
	sbody.scroll(function() {
		var bh = $(sbody).height();
		var st = $(sbody).scrollTop()
		var tbl = $('#'+id+" .fht-table tbody");
		var th = tbl.height();
		var left = tbl.height() - bh - st;
		//$.dbg("scrolling: bodyheight="+bh+" scrolltop="+st+" tableheight="+th+" left="+left);

		if(left > 100) {
			//$.dbg("Scrolling: area left="+left);
			return;
		}

		var lastRec = sbody.find("tr[lastRow]");
		if(lastRec.length != 0) {
			//$.dbg("scrolling: lastrec found");
			return;
		}
		WebUI.scall(id, "LOADMORE", {});
	});

};

WebUI.scrollableTableReset = function(id, tblid) {
	var tbl = $('#'+tblid);
	var container = $('#'+id);
	tbl.floatThead('reflow');
	WebUI.doCustomUpdates();

	$.dbg('recreate');

	//tbl.floatThead('destroy');
	//tbl.floatThead({
	//	scrollContainer: function() {
	//		return container;
	//	}
	//});

	container.scrollTop(0);
};

WebUI.initScrollableTable = function(id, tblid) {
	var container = $('#'+id);
	var tbl = $('#'+tblid);
	WebUI.doCustomUpdates();

	tbl.floatThead({
		scrollContainer: function() {
			return container;
		},
		getSizingRow: function($table){ // this is only called when using IE, we need any row without colspan, see http://mkoryak.github.io/floatThead/examples/row-groups/
			var rows = $table.find('tbody tr:visible').get();
			for (var i = 0; i < rows.length; i++){
				var cells = $(rows[i]).find('td');
				var isInvalidRow = false;
				for (var i = 0; i < cells.get().length; i++){
					if ($(cells[i]).attr('colspan') > 1){
						isInvalidRow = true;
					}
				}
				if (!isInvalidRow){
					return cells;
				}
			}
			if (rows.length > 0) {
				return $(rows[0]).find('td'); //as fallback we just return first row cells
			}else{
				return null; //or nothing -> but this should not be possible since getSizingRow is called only on table with rows
			}
		}
	});
	container.scroll(function() {
		var bh = $(container).height();
		var st = $(container).scrollTop()
		var tbl = $('#'+id+" tbody");
		var th = tbl.height();
		var left = tbl.height() - bh - st;
		$.dbg("scrolling: bodyheight="+bh+" scrolltop="+st+" tableheight="+th+" left="+left);

		if(left > 100) {
			//$.dbg("Scrolling: area left="+left);
			return;
		}

		var lastRec = tbl.find("tr[lastRow]");
		if(lastRec.length != 0) {
			//$.dbg("scrolling: lastrec found");
			return;
		}
		WebUI.scall(id, "LOADMORE", {

		});
	});

};




WebUI.notifyPage = function(command) {
	var bodyId = '_1';
	var pageBody = document.getElementById(bodyId);
	//check for exsistence, since it is delayed action component can be removed when action is executed.
	if (pageBody){
		var fields = new Object();
		fields.webuia = "notifyPage";
		fields[bodyId + "_command"] = command;
		WebUI.scall(bodyId, "notifyPage", fields);
	}
};

WebUI.closeOnClick = function(id) {
	this._id = id;
	var clickHandler = this._clickHandler = $.proxy(this.closeMenu, this);
	$(document).click(clickHandler);
	var keyUpHandler = this._keyUpHandler = $.proxy(this.buttonHandler, this);
	$(document).keyup(keyUpHandler);
	$('#' + id).data('inst', this);
};

$.extend(WebUI.closeOnClick.prototype, {
	closeMenu: function() {
		this.unbind();
		WebUI.scall(this._id, "CLOSEMENU?", {});
	},

	unbind: function() {
		$(document).unbind("click", this._clickHandler);
		$(document).unbind("keyup", this._keyUpHandler);
	},

	markClosed: function(id) {
		var inst = $('#' + id).data('inst');
		if(inst) {
			inst.unbind();
		}
	},

	isInputTagEvent: function(event) {
		var src = event.srcElement;
		if(src) {
			var tn = src.tagName.toUpperCase();
			if(tn === 'INPUT' || tn == 'SELECT' || tn == "TEXTAREA")
				return true;
		}
		return false;
	},

	buttonHandler: function(event) {
		if (this.isInputTagEvent(event))
			return;

		if (event.which == 27) {				// escape
			this.closeMenu();
		}
	}
});

DbPerformance = new Object();
DbPerformance.post = function(id,sessionid) {
	$(document).ready(function() {
		setTimeout(function() {
			$.get(DomUIappURL + "nl.itris.vp.parts.DbPerf.part?requestid=" + sessionid, function(data) {
				//-- Insert the div as the last in the body
				$('#' + id).html(data);
				$(".vp-lspf").draggable({ghosting: false, zIndex: 100, handle: '.vp-lspf-ttl'});
				$(".vp-lspf-close").click(function() {
					$(".vp-lspf").hide();
				});

			});
		}, 500);

	})
};
