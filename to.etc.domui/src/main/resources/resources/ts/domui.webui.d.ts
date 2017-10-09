declare const WebUI: WebUIStatic;

interface WebUIStatic {
	refreshElement(id: string) : void;
	blockUI() : void;
	_T: any;
	_hideExpiredMessage: boolean;
	isReallyIE7() : boolean;
	isIE8orIE8c() : boolean;
	isIE8orNewer() : boolean;
	isNormalIE9plus() : boolean;
	getPostURL() : string;
	stretchHeightOnNode(elem : HTMLElement) : void;
}
