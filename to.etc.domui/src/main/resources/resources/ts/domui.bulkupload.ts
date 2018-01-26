/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";

namespace WebUI {
	var SWFUpload;

	/** Bulk upload code using swfupload */
	export function bulkUpload(id, buttonId, url) {
		var ctl = $('#'+id) as any;
		ctl.swfupload({
			upload_url: url,
			flash_url: (window as any).DomUIappURL + "$js/swfupload.swf",
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
			var uf = new UploadFile(file, target, function() {
				($ as any).swfupload.getInstance(ctl).cancelUpload(file.id);
			});
		});
		ctl.bind('uploadStart', function(event, file) {
			var uf = new UploadFile(file, target);
			uf.uploadStarted();
		});
		ctl.bind('uploadProgress', function(event, file, bytesdone, bytestotal) {
			var uf = new UploadFile(file, target);
			var pct = bytesdone * 100 / bytestotal;
			uf.setProgress(pct);
		});
		ctl.bind('uploadError', function(event, file, code, msg) {
			var uf = new UploadFile(file, target);
			uf.uploadError(msg);
		});
		ctl.bind('uploadSuccess', function(event, file, code, msg) {
			var uf = new UploadFile(file, target);
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
				var uf = new UploadFile(file, target);
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
	}

	/**
	 * The uploadFile object handles all progress handling for a swf file.
	 */
	class UploadFile {
		_id : string;
		private _ui: JQuery;

		constructor(file, target, cancelFn?) {
			this._id = file.id;

			//-- connect to pre-existing UI
			var ui = this._ui = $('#'+file.id);
			if(this._ui.length == 0) {
				//-- Create the UI.
				target.append("<div id='"+this._id+"' class='ui-bupl-file'><div class='ui-bupl-inner ui-bupl-pending'><a href='#' class='ui-bupl-cancl'> </a><div class='ui-bupl-name'>"+file.name+"</div><div class='ui-bupl-stat'>"+WebUI._T.buplPending+"</div><div class='ui-bupl-perc'></div></div></div>");
				this._ui = $('#'+file.id);
				if(cancelFn) {
					var me = this;
					$(".ui-bupl-cancl", this._ui).bind("click", function() {
						$(".ui-bupl-stat", ui).html(WebUI._T.buplCancelled);
						me.suicide();
						cancelFn();
					});
				}
			}
		}

		uploadStarted = function() {
			$(".ui-bupl-stat", this._ui).html(WebUI._T.buplRunning);
			$(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").addClass("ui-bupl-running");
		};
		setProgress = function(pct) {
			$(".ui-bupl-perc", this._ui).width(pct+"%");
		};
		uploadError = function(message) {
			$(".ui-bupl-stat", this._ui).html(WebUI._T.buplError+": "+message);
			$(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").removeClass("ui-bupl-running").addClass("ui-bupl-error");
			$(".ui-bupl-cancl", this._ui).remove();
			this.setProgress(0);
			this.suicide();
		};
		uploadComplete = function() {
			$(".ui-bupl-stat", this._ui).html(WebUI._T.buplComplete);
			$(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").removeClass("ui-bupl-running").removeClass("ui-bupl-error").addClass("ui-bupl-complete");
			this.setProgress(100);
			$(".ui-bupl-cancl", this._ui).remove();
			this.suicide();
		};
		suicide = function() {
			this._ui.delay(8000).fadeOut(500);
		}
	}




}
