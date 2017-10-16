var WebUI;
(function (WebUI) {
    function fileUploadChange(e) {
        var tgt = e.currentTarget || e.srcElement;
        var vv = tgt.value.toString();
        if (!vv) {
            return;
        }
        var allowed = tgt.getAttribute('fuallowed');
        if (allowed) {
            var ok = false;
            var spl = allowed.split(',');
            vv = vv.toLowerCase();
            for (var i = 0; i < spl.length; i++) {
                var ext = spl[i].toLowerCase();
                if (ext.substring(0, 1) != ".")
                    ext = "." + ext;
                if (ext == ".*") {
                    ok = true;
                    break;
                }
                else if (vv.indexOf(ext, vv.length - ext.length) !== -1) {
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
        if (typeof tgt.files[0] !== "undefined") {
            var s = tgt.getAttribute('fumaxsize');
            var maxSize = 0;
            try {
                maxSize = Number(s);
            }
            catch (x) {
            }
            if (maxSize <= 0 || maxSize === NaN)
                maxSize = 100 * 1024 * 1024;
            var size = tgt.files[0].size;
            if (size > maxSize) {
                alert(WebUI.format(WebUI._T.buplTooBig, maxSize));
                tgt.value = "";
                return;
            }
        }
        var iframe = document.getElementById('webuiif');
        if (iframe) {
            iframe.parentNode.removeChild(iframe);
            iframe = undefined;
        }
        if (!iframe) {
            if (jQuery.browser.msie && !WebUI.isNormalIE9plus()) {
                iframe = document.createElement('<iframe name="webuiif" id="webuiif" src="#" style="display:none; width:0; height:0; border:none" onload="WebUI.ieUpdateUpload(event)">');
                document.body.appendChild(iframe);
            }
            else {
                iframe = document.createElement('iframe');
                iframe.id = 'webuiif';
                iframe.name = "webuiif";
                iframe.src = "#";
                iframe.style.display = "none";
                iframe.style.width = "0px";
                iframe.style.height = "0px";
                iframe.style.border = "none";
                iframe.onload = function () {
                    updateUpload(iframe.contentDocument);
                };
                document.body.appendChild(iframe);
            }
        }
        var form = tgt.parentNode;
        var img = document.createElement('img');
        img.border = "0";
        img.src = window.DomUIProgressURL;
        form.parentNode.insertBefore(img, form);
        form.style.display = 'none';
        form.target = "webuiif";
        form.submit();
        WebUI.blockUI();
    }
    function ieUpdateUpload(e) {
        var iframe = document.getElementById('webuiif');
        var xml;
        var cw = iframe.contentWindow;
        if (cw && cw.document.XMLDocument) {
            xml = cw.document.XMLDocument;
        }
        else if (iframe.contentDocument) {
            var crap = iframe.contentDocument.body.innerText;
            crap = crap.replace(/^\s+|\s+$/g, '');
            crap = crap.replace(/(\n|\r)-*/g, '');
            crap = crap.replace(/&/g, '&amp;');
            if (window.DOMParser) {
                var parser = new DOMParser();
                xml = parser.parseFromString(crap);
            }
            else if (window.ActiveXObject) {
                xml = new ActiveXObject("Microsoft.XMLDOM");
                if (!xml.loadXML(crap)) {
                    alert('ie9 in emulation mode unfixable bug: cannot parse xml');
                    window.location.href = window.location.href;
                    WebUI.unblockUI();
                    return;
                }
            }
            else {
                alert('No idea how to parse xml today.');
            }
        }
        else
            alert('IE error: something again changed in xml source structure of the iframe, sigh');
        updateUpload(xml, iframe);
    }
    function updateUpload(doc, ifr) {
        try {
            $.executeXML(doc);
        }
        catch (x) {
            alert(x);
            throw x;
        }
        finally {
            WebUI.unblockUI();
        }
    }
})(WebUI || (WebUI = {}));
//# sourceMappingURL=domui.fileupload.js.map