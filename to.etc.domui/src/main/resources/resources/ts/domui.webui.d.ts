declare const WebUI: WebUIStatic;

interface WebUIStatic {
	// ajax
	clicked(h, id : string, evt: any);
	getInputFields(fields: object): object;
	cancelPolling(): void;
	handleError(request, status, exc): boolean;
	handleResponse(data, state): void;


	//-- util
	refreshElement(id: string) : void;
	blockUI() : void;
	getPostURL() : string;
	_T: any;
	getPostURL() : string;
	_hideExpiredMessage: boolean;
	isReallyIE7() : boolean;
	isIE8orIE8c() : boolean;
	isIE8orNewer() : boolean;
	isNormalIE9plus() : boolean;
	normalizeKey(evt: any): number;
	stretchHeightOnNode(elem : HTMLElement) : void;
}
