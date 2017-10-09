declare const WebUI: WebUIStatic;

interface WebUIStatic {
	// ajax
	clicked(h, id : string, evt: any);
	getInputFields(fields: object): object;
	cancelPolling(): void;
	handleError(request, status, exc): boolean;
	handleResponse(data, state): void;
	scall(id: string, action: string, fields? : any) : void;

	// busy
	blockUI() : void;
	unblockUI() : void;

	//-- util
	refreshElement(id: string) : void;
	blockUI() : void;
	getPostURL() : string;
	_T: any;
	format(message: string, ...rest) : string;
	getPostURL() : string;
	getObituaryURL(): string;

	_hideExpiredMessage: boolean;
	isReallyIE7() : boolean;
	isIE8orIE8c() : boolean;
	isIE8orNewer() : boolean;
	isNormalIE9plus() : boolean;
	nearestID(elem: HTMLElement) : any;
	normalizeKey(evt: any): number;
	stretchHeightOnNode(elem : HTMLElement) : void;
	truncateUtfBytes(str: string, nbytes: number) : number;
	utf8Length(str: string) : number;

	// handler
	doCustomUpdates(): void;

	// dateinput
	handleCalendarChanges() : void;
}
