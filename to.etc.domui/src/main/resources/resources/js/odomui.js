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

( function($) {
	$.fn.center = function() {
		if(this.css("position") != "fixed") {
			this.css("position", "absolute");
			this.css("top", Math.max(0, ( ($(window).height() - this.outerHeight()) / 2) + $(window).scrollTop()) + "px");
			this.css("left", Math.max(0, ( ($(window).width() - this.outerWidth()) / 2) + $(window).scrollLeft()) + "px");
		} else {
			//-- For fixed: do not include scroll.
			this.css("top", Math.max(0, ( ($(window).height() - this.outerHeight()) / 2)) + "px");
			this.css("left", Math.max(0, ( ($(window).width() - this.outerWidth()) / 2)) + "px");
		}

		return this;
	};

	/**
	 * Read or set a cookie.
	 */
	$.cookie = function(name, value, options) {
		if(value !== undefined) {
			if(value === null)
				options.expires = -1;
			if(typeof options.expires === 'number') {
				var dt = new Date();
				dt.setDate(dt.getDate() + options.expires);
				options.expires = dt;
			}
			value= String(value);
			var c = [
				encodeURIComponent(name), '=', encodeURIComponent(value),
				options.expires ? '; expires=' + options.expires.toUTCString() : '',
				options.path    ? '; path=' + options.path : '',
				options.domain  ? '; domain=' + options.domain : '',
				options.secure  ? '; secure' : ''
			].join('');
			return (document.cookie = c);
		}

		var cookar= document.cookie.split("; ");
		for(var i = cookar.length; --i >= 0;) {
			var par = cookar[i].split('=');
			if(par.length < 2)
				continue;
			var rname = decodeURIComponent(par.shift().replace(/\+/g, ' '));
			if(rname === name) {
				return decodeURIComponent(par.join('=').replace(/\+/g, ' '));
			}
		}
		return null;
	};

	$.fn.executeDeltaXML = executeXML;
})(jQuery);

/**
 * jQuery scroll overflow fixerydoo for IE7's "let's create huge problems by putting a scrollbar inside the scrolling area" blunder. It
 * locates all scrolled-in area's and adds 20px of padding at the bottom.
 */
(function ($) {
	$.fn.fixOverflow = function () {
		if(! $.browser.msie || $.browser.version.substring(0, 1) != "7")
			return this;

		return this.each(function () {
			if (this.scrollWidth > this.offsetWidth) {
				$(this).css({ 'padding-bottom' : '20px' });
				if (this.scrollHeight <= this.offsetHeight ){
					//hide vertical scroller only if it is not needed after padding is increased.
					$(this).css({ 'overflow-y' : 'hidden' });
				}
			}

			//-- jal 20110727 Do the same for height?
			if(this.scrollHeight > this.offsetHeight) {
				$(this).css({ 'margin-right' : '17px' });
				if(this.scrollWidth <= this.offsetWidth) {
					$(this).css({ 'overflow-x' : 'hidden' });
				}
			}

		});
	};
})(jQuery);

(function ($) {
	$.fn.doStretch = function () {
		return this.each(function () {
			WebUI.stretchHeightOnNode(this);
		});
	};
})(jQuery);

(function ($) {
	$.fn.setBackgroundImageMarker = function () {
		return this.each(function () {
			if($(this).markerTransformed){
				return;
			}
			var imageUrl = 'url(' + $(this).attr('marker') + ')';
			// Wrap this in a try/catch block since IE9 throws "Unspecified error" if document.activeElement
			// is undefined when we are in an IFrame. TODO: better solution?
			try {
				if((!(this == document.activeElement)) && $(this).val().length == 0){
					$(this).css('background-image', imageUrl);
				}
			} catch(e) {}
			$(this).css('background-repeat', 'no-repeat');
			$(this).bind('focus',function(e){
				$(this).css('background-image', 'none');
			});
			$(this).bind('blur',function(e){
				if($(this).val().length == 0){
					$(this).css('background-image', imageUrl);
				} else {
					$(this).css('background-image', 'none');
				}
			});
			$(this).markerTransformed = true;
		});
	};
})(jQuery);

/** WebUI helper namespace */
WebUI._DEFAULT_DROPZONE_HANDLER = {
	checkRerender : function(dz) {
	},
	hover : function(dz) {
		$(dz._dropTarget).addClass("ui-drp-hover");
	},
	unmark : function(dz) {
		if (dz)
			$(dz._dropTarget).removeClass("ui-drp-hover");
	},

	drop : function(dz) {
		this.unmark(dz);
		WebUI.scall(dz._dropTarget.id, "WEBUIDROP", {
			_dragid :WebUI._dragNode.id,
			_index :0
		});
		WebUI.dragReset();
	}
};

/**
 * This handles ROW mode drops. It locates the nearest row in the TBody for this
 * dropTarget and decides to put the thingy BEFORE or AFTER that row. The
 * boundary there then gets highlighted.
 */
WebUI._ROW_DROPZONE_HANDLER = {
	locateBest : function(dz) {
		var tbody = dz._tbody;
		if (!tbody)
			throw "No TBody!";

		// -- Use the current mouseish Y position to distinguish between rows.
		var mousePos = WebUI._dragLastY;
		var mouseX = WebUI._dragLastX;
		//console.debug("Starting position det: drag Y = "+mousePos);
		var gravity = 0; // Prefer upward gravity
		var lastrow = null;
		var rowindex = 0;
		var position = { top: 0, index: 0};
		for ( var i = 0; i < tbody.childNodes.length; i++) {
			var tr = tbody.childNodes[i];
			if (tr.nodeName != 'TR')
				continue;
			lastrow = tr;
			var off = $(tr).offset();
			var prevPosition = position;
			position = { top: off.top, index: i };
			if (position) {
				//			console.debug('mouse:' +mousePos+','+mouseX+' row: prevPosition.top='+prevPosition.top+", position.top="+position.top+", index="+position.index);

				// -- Is the mouse IN the Y range for this row?
				if (mousePos >= prevPosition.top && mousePos < position.top) {
					// -- Cursor is WITHIN this node. Is it near the TOP or near the
					// BOTTOM?
					gravity = 0;
					if(prevPosition.top + position.top != 0){
						var hy = (prevPosition.top + position.top) / 2;
						gravity = mousePos < hy ? 0 : 1;
					}
					//				console.debug('ACCEPTED top='+prevPosition.top+', bottom='+position.top+', hy='+hy+', rowindex='+(rowindex-1));
					//				console.debug('index='+prevPosition.index+', gravety='+gravity);

					var colIndex = this.getColIndex(tr, mouseX);
					return {
						index :rowindex-1,
						iindex : prevPosition.index,
						gravity :gravity,
						row :tr,
						colIndex : colIndex
					};
				}

				// -- Is the thing between this row and the PREVIOUS one?
				//			if (mousePos < position.top) {
				//				// -- Use this row with gravity 0 (should insert BEFORE this row).
				//				//MVE
				//				console.debug('ACCEPTED BEFORE node by='+prevPosition.top+', ey='+position.top+', rowindex='+rowindex-1);
				//				return {
				//					index :rowindex,
				//					iindex :position.index,
				//					gravity :0,
				//					row :tr
				//				};
				//			}
				//console.debug('REFUSED by='+prevPosition.top+", ey="+position.top+", rowindex="+rowindex);
			} else {
				//			console.debug("row: no location.");
			}
			rowindex++;
		}
		//console.debug("ACCEPTED last one");

		// -- If we're here we must insert at the last location
		var colIndex = this.getColIndex(lastrow, mouseX);
		return {
			index :rowindex,
			iindex :position.index,
			gravity :1,
			row :lastrow,
			colIndex : colIndex
		};
	},

	getColIndex : function(tr, mouseX) {
		//determine the collumn
		var left = 0;
		var right = 0;
		var j;
		for ( j = 0; j < tr.childNodes.length; j++) {
			var td = tr.childNodes[j];
			if (td.nodeName != 'TD')
				continue;
			left = right;
			right = $(td).offset().left;
			if(mouseX >= left && mouseX < right ){
				//because only the left position can be asked, the check is done for the previous collumn
				return j-1;
			}

		}
		//TODO MVE should return maxColumn
		return 2;

	},

	checkRerender : function(dz) {
		var b = this.locateBest(dz);
		// console.debug("checkRerender: "+b.iindex+", "+b.index+", g="+b.gravity);
		if (b.iindex == WebUI._dropRowIndex)
			return;

		this.unmark(dz);
		this.renderTween(dz, b);
	},

	renderTween : function(dz, b) {
		var body = dz._tbody;

		var colCount = 0;
		if(dz._tbody.rows.length > 0){
			var temp = dz._tbody.rows[0].cells;
			$(temp).each(function() {
				colCount += $(this).attr('colspan') ? parseInt($(this).attr('colspan')) : 1;
			});
		}


		// -- To mark, we insert a ROW at the insert location and visualize that
		var tr = document.createElement('tr');
		//b.colIndex should define the correct collumn
		var colIndex = b.colIndex;
		for(var i = 0; i<colCount;i++ ){
			this.appendPlaceHolderCell(tr, colIndex == i);
		}
		if (b.iindex >= body.childNodes.length)
			body.appendChild(tr);
		else
			body.insertBefore(tr, body.childNodes[b.iindex]);
		WebUI._dropRow = tr;
		WebUI._dropRowIndex = b.iindex;
	},

	appendPlaceHolderCell : function(tr, appendPlaceholder) {
		var td = document.createElement('td');
		if(appendPlaceholder){
			td.appendChild(document.createTextNode(WebUI._T.dndInsertHere));
			td.className = 'ui-drp-ins';
		}
		tr.appendChild(td);

	},

	hover : function(dz) {
		var b = this.locateBest(dz);
		//	console.debug("hover: "+b.iindex+", "+b.index+", g="+b.gravity + ", col=" +b.colIndex);
		this.renderTween(dz, b);
	},

	unmark : function(dz) {
		if (WebUI._dropRow) {
			$(WebUI._dropRow).remove();
			delete WebUI._dropRow;
			delete WebUI._dropRowIndex;
		}
	},

	drop : function(dz) {
		this.unmark(dz);
		var b = this.locateBest(dz);
		WebUI.scall(dz._dropTarget.id, "WEBUIDROP", {
			_dragid :WebUI._dragNode.id,
			_index :(b.index+b.gravity),
			_colIndex :b.colIndex
		});
		WebUI.dragReset();
	}
};

/**
 * Make a structure a color button.
 */
WebUI.colorPickerButton = function(btnid, inid, value,onchange) {
	$(btnid).ColorPicker({
		color: '#'+value,
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$(btnid+' div').css('backgroundColor', '#' + hex);
			$(inid).val(hex);
			if(onchange)
				WebUI.colorPickerOnchange(btnid, hex);
		}
	});
};

WebUI.colorPickerInput = function(inid, divid, value, onchange) {
	$(inid).ColorPicker({
		color: '#'+value,
		flat: false,
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onBeforeShow: function() {
			$(this).ColorPickerSetColor(this.value);
		},
		onChange: function (hsb, hex, rgb) {
			$(divid).css('backgroundColor', '#' + hex);
			$(inid).val(hex);
			if(onchange)
				WebUI.colorPickerOnchange(btnid, hex);
		}
	});
};
WebUI.colorPickerDisable = function(id) {
	$(id).die();
};

WebUI.colorPickerOnchange= function(id, last) {
	if(WebUI._colorLast == last && WebUI._colorLastID == id)
		return;

	if(WebUI._colorTimer) {
		window.clearTimeout(WebUI._colorTimer);
		window._colorTimer = undefined;
	}
	WebUI._colorLastID = id;
	WebUI._colorTimer = window.setTimeout("WebUI.colorPickerChangeEvent('" + id + "')", 500);
};

WebUI.colorPickerChangeEvent = function(id) {
	window.clearTimeout(WebUI._colorTimer);
	window._colorTimer = undefined;
	WebUI.valuechanged('eh', id);
};

var DomUI = WebUI;

WebUI._customUpdatesContributors = $.Callbacks("unique");

WebUI._customUpdatesContributorsTimerID = null;

/**
 * registers function that gets called after doCustomUpdates sequence of calls ends, with 500 delay - doCustomUpdates can trigger new doCustomUpdates etc...
 * @param contributorFunction
 */
WebUI.registerCustomUpdatesContributor = function(contributorFunction) {
	WebUI._customUpdatesContributors.add(contributorFunction);
}

WebUI.unregisterCustomUpdatesContributor = function(contributorFunction) {
	WebUI._customUpdatesContributors.remove(contributorFunction);
}

WebUI.doCustomUpdates = function() {
	$('.floatThead-wrapper').each(
		function (index, node){
			$(node).attr('stretch', $(node).find('>:first-child').attr('stretch'));
		}
	);
	$('[stretch=true]').doStretch();
	$('.ui-dt, .ui-fixovfl').fixOverflow();
	$('input[marker]').setBackgroundImageMarker();

	//-- Limit textarea size on paste events
	$("textarea[mxlength], textarea[maxbytes]").unbind("input.domui").unbind("propertychange.domui").bind('input.domui propertychange.domui', function() {
		var maxLength = Number($(this).attr('mxlength'));				// Use mxlength because Chrome improperly implements maxlength (issue 252613)
		var maxBytes = Number($(this).attr('maxbytes'));
		var val = $(this).val();
		var newlines = (val.match(/\r\n/g) || []).length;				// Count the #of 2-char newlines, as they will be replaced by 1 newline character
		if(maxBytes === NaN) {
			if(maxLength === NaN)
				return;
		} else if(maxLength === NaN) {
			maxLength = maxBytes;
		}

		if(val.length + newlines > maxLength) {
			val = val.substring(0, maxLength - newlines);
			$(this).val(val);
		}
		if(maxBytes !== NaN) {
			var cutoff = WebUI.truncateUtfBytes(val, maxBytes);
			if(cutoff < val.length) {
				val = val.substring(0, cutoff);
				$(this).val(val);
			}
		}
	});

	//-- Limit textarea size on key presses
	$("textarea[mxlength], textarea[maxbytes]").unbind("keypress.domui").bind('keypress.domui', function(evt) {
		if(evt.which == 0 || evt.which == 8)
			return true;

		//-- Is the thing too long already?
		var maxLength = Number($(this).attr('mxlength'));
		var maxBytes = Number($(this).attr('maxbytes'));
		var val = $(this).val();
		var newlines = (val.match(/\r\n/g) || []).length;				// Count the #of 2-char newlines, as they will be replaced by 1 newline character
		if(maxBytes === NaN) {
			if(maxLength === NaN)
				return true;
		} else if(maxLength === NaN) {
			maxLength = maxBytes;
		}
		if(val.length - newlines >= maxLength)							// Too many chars -> not allowed
			return false;
		if(maxBytes !== NaN) {
			var bytes = WebUI.utf8Length(val);
			if(bytes >= maxBytes)
				return false;
		}
		return true;
	});

	//custom updates may fire several times in sequence, se we fire custom contributors only after it gets steady for a while (500ms)
	if (WebUI._customUpdatesContributorsTimerID) {
		window.clearTimeout(WebUI._customUpdatesContributorsTimerID);
	}
	WebUI._customUpdatesContributorsTimerID = window.setTimeout(function(){ try{ WebUI._customUpdatesContributors.fire()}catch(ex) {}}, 500);
	//$('.ui-dt-ovflw-tbl').floatThead('reflow');
};

WebUI.truncateUtfBytes = function(str, nbytes) {
	//-- Loop characters and calculate running length
	var bytes = 0;
	var length = str.length;
	for(var ix = 0; ix < length; ix++) {
		var c = str.charCodeAt(ix);
		if(c < 0x80)
			bytes++;
		else if(c < 0x800)
			bytes += 2;
		else
			bytes += 3;
		if(bytes > nbytes)
			return ix;
	}
	return length;
};

WebUI.utf8Length = function(str) {
	var bytes = 0;
	var length = str.length;
	for(var ix = 0; ix < length; ix++) {
		var c = str.charCodeAt(ix);
		if(c < 0x80)
			bytes++;
		else if(c < 0x800)
			bytes += 2;
		else
			bytes += 3;
	}
	return bytes;
};


WebUI.onDocumentReady = function() {
	WebUI.checkBrowser();
	WebUI.handleCalendarChanges();
	if(DomUIDevel)
		WebUI.handleDevelopmentMode();
	WebUI.doCustomUpdates();
};

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

WebUI.replaceBrokenImageSrc = function(id, alternativeImage) {
	$('img#' + id).error(function() {
		$(this).attr("src", alternativeImage);
	});
};

/** In tables that have special class selectors that might cause text-overflow we show full text on hover */
WebUI.showOverflowTextAsTitle = function(id, selector) {
	var root = $("#" + id);
	if (root) {
		root.find(selector).each(function () {
			if (this.offsetWidth < this.scrollWidth) {
				var $this = $(this);
				$this.attr("title", $this.text());
			}
		});
	}
	return true;
};

/** Bulk upload code using swfupload */
WebUI.bulkUpload = function(id, buttonId, url) {
	var ctl = $('#'+id);
	ctl.swfupload({
		upload_url: url,
		flash_url: DomUIappURL+"$js/swfupload.swf",
		file_types: '*.*',
		file_upload_limit: 1000,
		file_queue_limit: 0,
		file_size_limit: "100 MB",
		button_width: 120,
		button_height: 24,
		button_placeholder_id: buttonId,
		button_window_mode: SWFUpload.WINDOW_MODE.TRANSPARENT,
		button_cursor: SWFUpload.CURSOR.HAND
	});
	var target = $("#"+id+" .ui-bupl-queue");

	ctl.bind('fileQueued', function(event, file) {
		var uf = new WebUI.UploadFile(file, target, function() {
			$.swfupload.getInstance(ctl).cancelUpload(file.id);
		});
	});
	ctl.bind('uploadStart', function(event, file) {
		var uf = new WebUI.UploadFile(file, target);
		uf.uploadStarted();
	});
	ctl.bind('uploadProgress', function(event, file, bytesdone, bytestotal) {
		var uf = new WebUI.UploadFile(file, target);
		var pct = bytesdone * 100 / bytestotal;
		uf.setProgress(pct);
	});
	ctl.bind('uploadError', function(event, file, code, msg) {
		var uf = new WebUI.UploadFile(file, target);
		uf.uploadError(msg);
	});
	ctl.bind('uploadSuccess', function(event, file, code, msg) {
		var uf = new WebUI.UploadFile(file, target);
		uf.uploadComplete();

		//-- Send a DomUI command so the UI can handle updates.
		WebUI.scall(id, "uploadDone", {});
	});
	ctl.bind('queueComplete', function(event, numUploaded) {
		//-- Send a DomUI command for queue complete.
		WebUI.scall(id, "queueComplete", {});
	});
//	ctl.bind('uploadComplete', function(event, file) {
//		var uf = new WebUI.UploadFile(file, target);
//	});
	ctl.bind('fileDialogComplete', function(nfiles) {
		if(0 == nfiles) {
			return;
		}

		//-- Autostart upload on dialog completion.
		ctl.swfupload('startUpload');

		//-- Send a DomUI command for queue start.
		WebUI.scall(id, "queueStart", {});
	});
	ctl.bind('fileQueueError', function(event, file, errorCode, message) {
		try {
			if(errorCode === SWFUpload.QUEUE_ERROR.QUEUE_LIMIT_EXCEEDED) {
				alert(WebUI._T.buplTooMany);
//				alert("You have attempted to queue too many files.\n" + (message === 0 ? "You have reached the upload limit." : "You may select " + (message > 1 ? "up to " + message + " files." : "one file.")));
				return;
			}
			var uf = new WebUI.UploadFile(file, target);
			switch (errorCode) {
				case SWFUpload.QUEUE_ERROR.FILE_EXCEEDS_SIZE_LIMIT:
					uf.uploadError(WebUI._T.buplTooBig);
					break;
				case SWFUpload.QUEUE_ERROR.ZERO_BYTE_FILE:
					uf.uploadError(WebUI._T.buplEmptyFile);
					break;
				case SWFUpload.QUEUE_ERROR.INVALID_FILETYPE:
					uf.uploadError(WebUI._T.buplInvalidType);
					break;
				default:
					if(file !== null) {
						uf.uploadError(WebUI._T.buplUnknownError);
					}
					break;
			}
		} catch (ex) {
			alert(ex);
		}
	});

//	//-- TEST
//	var file = {
//			id: 'jal',
//			name: 'upload.jpg'
//	};
//	var uf = new WebUI.UploadFile(file, target, function() {
//		$.swfupload.getInstance(ctl).cancelUpload(file.id);
//	});
//
//	setTimeout(function() {
//		uf.uploadStarted();
//
//	}, 2000);
//
//	setTimeout(function() {
//		uf.setProgress(10);
//
//	}, 3000);
//
//	setTimeout(function() {
//		uf.setProgress(20);
//
//	}, 4000);
//
////	setTimeout(function() {
////		uf.uploadError("Server IO error");
////
////	}, 6000);
//
//	setTimeout(function() {
//		uf.uploadComplete();
//	}, 6000);
};

/**
 * The uploadFile object handles all progress handling for a swf file.
 */
WebUI.UploadFile = function(file, target, cancelFn) {
	this._id = file.id;

	//-- connect to pre-existing UI
	this._ui = $('#'+file.id);
	if(this._ui.length == 0) {
		//-- Create the UI.
		target.append("<div id='"+this._id+"' class='ui-bupl-file'><div class='ui-bupl-inner ui-bupl-pending'><a href='#' class='ui-bupl-cancl'> </a><div class='ui-bupl-name'>"+file.name+"</div><div class='ui-bupl-stat'>"+DomUI._T.buplPending+"</div><div class='ui-bupl-perc'></div></div></div>");
		this._ui = $('#'+file.id);
		if(cancelFn) {
			var me = this;
			$(".ui-bupl-cancl", this._ui).bind("click", function() {
				$(".ui-bupl-stat", this._ui).html(WebUI._T.buplCancelled);
				me.suicide();
				cancelFn();
			});
		}
	}
};
WebUI.UploadFile.prototype.uploadStarted = function() {
	$(".ui-bupl-stat", this._ui).html(WebUI._T.buplRunning);
	$(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").addClass("ui-bupl-running");
};
WebUI.UploadFile.prototype.setProgress = function(pct) {
	$(".ui-bupl-perc", this._ui).width(pct+"%");
};
WebUI.UploadFile.prototype.uploadError = function(message) {
	$(".ui-bupl-stat", this._ui).html(WebUI._T.buplError+": "+message);
	$(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").removeClass("ui-bupl-running").addClass("ui-bupl-error");
	$(".ui-bupl-cancl", this._ui).remove();
	this.setProgress(0);
	this.suicide();
};
WebUI.UploadFile.prototype.uploadComplete = function() {
	$(".ui-bupl-stat", this._ui).html(WebUI._T.buplComplete);
	$(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").removeClass("ui-bupl-running").removeClass("ui-bupl-error").addClass("ui-bupl-complete");
	this.setProgress(100);
	$(".ui-bupl-cancl", this._ui).remove();
	this.suicide();
};
WebUI.UploadFile.prototype.suicide = function() {
	this._ui.delay(8000).fadeOut(500);
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

WebUI.deactivateHiddenAccessKeys = function(windowId) {
	$('button').each(function(index) {
		var iButton = $(this);
		if(isButtonChildOfElement(iButton, windowId)){
			var oldAccessKey = $(iButton).attr('accesskey');
			if(oldAccessKey != null ){
				$(iButton).attr('accesskey', $(windowId).attr('id') + '~' + oldAccessKey);
			}
		}
	});
};

WebUI.reactivateHiddenAccessKeys = function(windowId) {
	$("button[accesskey*='" + windowId + "~']" ).each(function(index){
		var accessKeyArray = $(this).attr('accesskey').split(windowId + '~');
		$(this).attr('accesskey', accessKeyArray[accessKeyArray.length - 1]);
	});
};

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



isButtonChildOfElement = function(buttonId, windowId){
	return $(buttonId).parents('#' + $(windowId).attr('id')).length == 0;
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
