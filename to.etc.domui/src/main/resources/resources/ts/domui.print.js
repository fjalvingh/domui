var WebUI;
(function (WebUI) {
    var _frmIdCounter = 0;
    function backgroundPrint(url) {
        try {
            var frmname = priparePrintDiv(url);
            framePrint(frmname);
        }
        catch (x) {
            alert("Failed: " + x);
        }
    }
    function framePrint(frmname) {
        if (jQuery.browser.msie) {
            documentPrintIE(frmname);
        }
        else {
            documentPrintNonIE(frmname);
        }
    }
    function documentPrintIE(frmname) {
        var ex = undefined;
        try {
            var frm = window.frames[frmname];
            $("#" + frmname).on("load", function () {
                try {
                    frm.focus();
                    setTimeout(function () {
                        if (!frm.document.execCommand('print', true, null)) {
                            alert('cannot print: ' + ex);
                        }
                    }, 1000);
                }
                catch (x) {
                    ex = x;
                    alert('cannot print: ' + x);
                }
            });
        }
        catch (x) {
            ex = x;
            alert("Failed: " + x);
        }
    }
    function documentPrintNonIE(frmname) {
        try {
            var frm = window.frames[frmname];
            $("#" + frmname).on("load", function () {
                try {
                    frm.focus();
                    setTimeout(function () {
                        frm.print();
                    }, 1000);
                }
                catch (x) {
                    alert('cannot print: ' + x);
                }
            });
        }
        catch (x) {
            alert("Failed: " + x);
        }
    }
    function priparePrintDiv(url) {
        var div = document.getElementById('domuiprif');
        if (div)
            div.innerHTML = "";
        else {
            div = document.createElement('div');
            div.id = 'domuiprif';
            div.className = 'ui-printdiv';
            document.body.appendChild(div);
        }
        var frmname = "dmuifrm" + (_frmIdCounter++);
        if (url.trim() !== "") {
            $(div).html('<iframe id="' + frmname + '" name="' + frmname + '" src="' + url + '">');
        }
        else {
            $(div).html('<iframe id="' + frmname + '" name="' + frmname + '" width="1000px" height="1000px"/>');
        }
        return frmname;
    }
    function printtext(id) {
        var item = document.getElementById(id);
        var textData;
        if (item && (item.tagName == "input" || item.tagName == "INPUT" || item.tagName == "textarea" || item.tagName == "TEXTAREA")) {
            textData = item.value;
        }
        if (textData) {
            try {
                var div = document.getElementById('domuiprif');
                if (div)
                    div.innerHTML = "";
                else {
                    div = document.createElement('div');
                    div.id = 'domuiprif';
                    div.className = 'ui-printdiv';
                    document.body.appendChild(div);
                }
                var frmname = "dmuifrm" + _frmIdCounter++;
                $(div).html('<iframe id="' + frmname + '" name="' + frmname + '" width="1000px" height="1000px"/>');
                var frm = window.frames[frmname];
                frm.document.open();
                frm.document.write('<html></head></head><body style="margin:0px;">');
                frm.document.write(textData.replace(/\n/g, '<br/>'));
                frm.document.write('</body></html>');
                frm.document.close();
                frm.focus();
                frm.print();
            }
            catch (x) {
                alert("Failed: " + x);
            }
        }
    }
})(WebUI || (WebUI = {}));
//# sourceMappingURL=domui.print.js.map